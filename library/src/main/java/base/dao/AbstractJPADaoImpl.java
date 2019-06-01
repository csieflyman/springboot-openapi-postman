package base.dao;

import base.exception.InvalidEntityException;
import base.exception.ObjectNotFoundException;
import base.model.Identifiable;
import base.util.query.JPAUtils;
import base.util.query.Junction;
import base.util.query.Query;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
@Slf4j
public abstract class AbstractJPADaoImpl<T extends Identifiable<ID>, ID extends Serializable> implements GenericDao<T, ID> {

    private EntityManager em;

    protected Class<T> clazz;

    public AbstractJPADaoImpl() {
        clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    // can't inject EntityManager by constructor https://github.com/spring-projects/spring-framework/issues/15076
    protected void setEntityManager(EntityManager em) {
        this.em = em;
    }

    protected T newInstance() {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidEntityException(String.format("fail to initialize instance %s", clazz.getName()), e);
        }
    }

    protected String getEntityName() {
        return clazz.getSimpleName();
    }

    @Override
    public T create(T entity) {
        em.persist(entity);
        return entity;
    }

    @Override
    public void update(T entity) {
        em.merge(entity);
    }

    @Override
    public void delete(T entity) {
        em.remove(entity);
    }

    @Override
    public int executeUpdate(Map<String, Object> valueMap, Junction junction) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<T> updateQuery = cb.createCriteriaUpdate(clazz);
        valueMap.forEach(updateQuery::set);
        updateQuery.where(JPAUtils.toJPAPredicate(cb, updateQuery.from(clazz), junction));
        return em.createQuery(updateQuery).executeUpdate();
    }

    @Override
    public int executeDelete(Junction junction) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<T> deleteQuery = cb.createCriteriaDelete(clazz);
        deleteQuery.where(JPAUtils.toJPAPredicate(cb, deleteQuery.from(clazz), junction));
        return em.createQuery(deleteQuery).executeUpdate();
    }

    @Override
    public T getById(ID id) {
        T entity = em.find(clazz, id);
        if(entity == null)
            throw new ObjectNotFoundException(String.format("entity %s doesn't exist.", id));
        return entity;
    }

    @Override
    public Optional<T> findById(ID id) {
        return Optional.ofNullable(em.find(clazz, id));
    }

    @Override
    public Optional<T> findOne(Query query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> jpaQuery = JPAUtils.toJpaQuery(query, cb, clazz);
        try {
            Tuple tuple = em.createQuery(jpaQuery).getSingleResult();
            Map<String, Object> map = tuple.getElements().stream().collect(Collectors.toMap(TupleElement::getAlias, tuple::get));
            return Optional.of(JPAUtils.toEntity(clazz, Arrays.asList(map)).get(0));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<T> find(Query query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> jpaQuery = JPAUtils.toJpaQuery(query, cb, clazz);
        TypedQuery<Tuple> typedQuery = em.createQuery(jpaQuery);
        if(query.isPagingQuery()) {
            typedQuery.setFirstResult((query.getPageNo() - 1) * query.getPageSize());
            typedQuery.setMaxResults(query.getPageSize());
        }
        List<Tuple> tuples = typedQuery.getResultList();
        List<Map<String, Object>> results = tuples.stream().map(tuple -> tuple.getElements().stream()
                .collect(Collectors.toMap(TupleElement::getAlias, te -> tuple.get(te.getAlias())))).collect(Collectors.toList());
        List<T> entities = JPAUtils.toEntity(clazz, results);
        log.debug("entities size = {}", entities.size());
        return entities;
    }

    @Override
    public long findSize(Query query) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<T> root = countQuery.from(clazz);
        countQuery.select(cb.count(root));
        countQuery.where(JPAUtils.toJPAPredicate(cb, root, query.where()));
        Long count = em.createQuery(countQuery).getSingleResult();
        log.debug("entities size = {}", count);
        return count;
    }

    @Override
    public Set<ID> findIds(Query query) {
        query.fetchProperties(JPAUtils.getIdPropertyName(clazz));
        return find(query).stream().map(Identifiable::getId).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}