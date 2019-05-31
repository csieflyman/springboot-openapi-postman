package base.dao;

import base.model.Identifiable;
import base.util.query.Junction;
import base.util.query.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author csieflyman
 */
public interface GenericDao<T extends Identifiable<ID>, ID> {

    T create(T entity);

    void update(T entity);

    void delete(T entity);

    int executeUpdate(Map<String, Object> valueMap, Junction junction);

    int executeDelete(Junction junction);

    T getById(ID id);

    Optional<T> findById(ID id);

    Optional<T> findOne(Query query);

    List<T> find(Query query);

    long findSize(Query query);

    Set<ID> findIds(Query query);
}
