package base.util.query;

import base.exception.InternalServerErrorException;
import base.exception.InvalidEntityException;
import base.exception.InvalidQueryException;
import com.google.common.base.CaseFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 *  此類別最好僅能有 JPA dependency，不能參考到任一 ORM Framework
 */
@Slf4j
public class JPAUtils {

    private JPAUtils() {

    }

    public static String buildNativeSql(String tableAlias, Class rootClass, Query query, String fromStatement) {
        return buildSelectClause(tableAlias, rootClass, query) + " from " + fromStatement +
                " " + buildWhereClause(tableAlias, query.where()) +
                " " + buildOrderByClause(tableAlias, query.getOrderByList());
    }

    private static String buildSelectClause(String tableAlias, Class rootClass, Query query) {
        if(query.isOnlySize()) {
            return "select count(*) as count";
        }

        Set<String> fetchProperties = query.getFetchProperties();
        Map<String, String> columnNameAliasMap = new LinkedHashMap<>();
        if(fetchProperties.isEmpty()) {
            columnNameAliasMap.putAll(buildColumnNameAliasMap(tableAlias, rootClass));
        }
        else {
            columnNameAliasMap.putAll(buildIdColumnNameAliasMap(tableAlias, rootClass));
            columnNameAliasMap.putAll(fetchProperties.stream().collect(Collectors.toMap(p -> getColumnName(tableAlias, p), p -> getColumnAlias(tableAlias, p))));

            Map<String, Class> relationPathClassMap = new LinkedHashMap<>();
            fetchProperties.stream().filter(p -> p.contains(".")).map(p -> buildRelationPathClassMap(rootClass, p.substring(0, p.lastIndexOf("."))))
                    .forEach(relationPathClassMap::putAll);
            columnNameAliasMap.putAll(relationPathClassMap.entrySet().stream()
                    .flatMap(entry -> buildIdColumnNameAliasMap(tableAlias + "." + entry.getKey(), entry.getValue()).entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

        for(String relation: query.getFetchRelations()) {
            Map<String, Class> relationPathClassMap = buildRelationPathClassMap(rootClass, relation);
            int index = 0;
            for(Map.Entry<String, Class> entry: relationPathClassMap.entrySet()) {
                if(index == relationPathClassMap.size() - 1) {
                    columnNameAliasMap.putAll(buildColumnNameAliasMap(tableAlias + "." + entry.getKey(), entry.getValue()));
                }
                else {
                    columnNameAliasMap.putAll(buildIdColumnNameAliasMap(tableAlias + "." + entry.getKey(), entry.getValue()));
                }
                index++;
            }
        }
        return "select " + columnNameAliasMap.entrySet().stream().map(entry -> entry.getKey() + " as " + entry.getValue()).collect(Collectors.joining(", "));
    }

    private static String buildWhereClause(String tableAlias, Junction junction) {
        if (junction.getPredicates().isEmpty())
            return "";

        Query.populatePredicate(junction);

        StringBuilder sb = new StringBuilder();
        sb.append("and ");
        for (Predicate predicate : junction.getPredicates()) {
            sb.append(predicateToSql(tableAlias, predicate));
            if (junction.isConjunction())
                sb.append(" and ");
            else
                sb.append(" or ");
        }

        if (junction.isConjunction()) {
            sb.delete(sb.length() - 5, sb.length());
        } else {
            sb.delete(sb.length() - 4, sb.length());
        }
        return sb.toString();
    }

    private static String buildOrderByClause(String tableAlias, Set<OrderBy> orderByList) {
        if (orderByList.isEmpty())
            return "";

        StringBuilder sb = new StringBuilder();
        sb.append("order by ");
        for (OrderBy orderBy : orderByList) {
            // SQL Function
            if(orderBy.getProperty().contains("(")) {
                sb.append(orderBy.getProperty());
            }
            else {
                sb.append(getColumnName(tableAlias, orderBy.getProperty()));
            }
            sb.append(" ").append(orderBy.isAsc() ? "" : "DESC ").append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        return sb.toString();
    }

    public static Set<String> getPropertyNames(Class clazz) {
        return FieldUtils.getAllFieldsList(clazz).stream().filter(JPAUtils::isColumn).map(Field::getName).collect(Collectors.toSet());
    }

    public static String getIdPropertyName(Class clazz) {
        return FieldUtils.getAllFieldsList(clazz).stream().filter(JPAUtils::isIdColumn).map(Field::getName).findFirst().orElseThrow(() -> new InvalidEntityException("id isn't defined"));
    }

    public static <T> CriteriaQuery<Tuple> toJpaQuery(Query query, CriteriaBuilder cb, Class<T> clazz) {
        CriteriaQuery<Tuple> jpaQuery = cb.createTupleQuery();
        Root<T> root = jpaQuery.from(clazz);

        if(query.getFetchProperties().isEmpty()) {
            jpaQuery.multiselect(JPAUtils.getPropertyNames(clazz).stream().map(p -> (Selection<?>)toPath(root, p).alias(p)).collect(Collectors.toList()));
        }
        else {
            jpaQuery.multiselect(query.getFetchProperties().stream().map(p -> (Selection<?>)toPath(root, p).alias(p)).collect(Collectors.toList()));
        }
        if(!query.getFetchRelations().isEmpty()) {
            jpaQuery.multiselect(query.getFetchRelations().stream().map(r -> (Selection<?>)toJoin(root, r).alias(r)).collect(Collectors.toList()));
        }

        jpaQuery.where(toJPAPredicate(cb, root, query.where()));

        if(!query.isOrderByEmpty()) {
            jpaQuery.orderBy(query.getOrderByList().stream().map(orderBy -> orderBy.isAsc() ?
                    cb.asc(toPath(root, orderBy.getProperty())) : cb.desc(toPath(root, orderBy.getProperty()))).collect(Collectors.toList()));
        }
        return jpaQuery;
    }

    public static <T> javax.persistence.criteria.Predicate toJPAPredicate(CriteriaBuilder cb, Root<T> root, Junction junction) {
        javax.persistence.criteria.Predicate[] pArray = junction.getPredicates().stream().map(p -> ToJPAPredicate(cb, root, p))
                .toArray(javax.persistence.criteria.Predicate[]::new);
        return junction.isConjunction() ? cb.and(pArray) : cb.or(pArray);
    }

    private static <T> javax.persistence.criteria.Predicate ToJPAPredicate(CriteriaBuilder cb, Root<T> root, Predicate predicate) {
        if(predicate instanceof Junction) {
            return toJPAPredicate(cb, root, (Junction) predicate);
        }
        else {
            SimplePredicate simplePredicate = (SimplePredicate) predicate;
            if(simplePredicate.isLiteralSql()) {
                throw new InvalidQueryException("literal sql is unsupported: " + predicate);
            }
            String property = simplePredicate.getProperty();
            Object value = simplePredicate.getValue();
            switch (simplePredicate.getOperator()) {
                case EQ:
                    return cb.equal(toPath(root, property), value);
                case NE:
                    return cb.notEqual(toPath(root, property), value);
                case GT:
                    return cb.greaterThan(toPath(root, property), (Comparable) value);
                case GE:
                    return cb.greaterThanOrEqualTo(toPath(root, property), (Comparable) value);
                case LT:
                    return cb.lessThan(toPath(root, property), (Comparable) value);
                case LE:
                    return cb.lessThanOrEqualTo(toPath(root, property), (Comparable) value);
                case IN:
                    return toPath(root, property).in((Collection) value);
                case LIKE:
                    return cb.like(toPath(root, property), (String)value);
                case IS_NULL:
                    return cb.isNull(toPath(root, property));
                case IS_NOT_NULL:
                    return cb.isNotNull(toPath(root, property));
                default:
                    throw new InvalidQueryException("invalid predicate operator: " + predicate);
            }
        }
    }

    private static <T> Path toPath(Root<T> root, String propertyPath) {
        String[] segments = propertyPath.split("\\.");
        Path path = root.get(segments[0]);
        if(segments.length > 1) {
            for(int i = 1; i < segments.length; i++) {
                path = path.get(segments[i]);
            }
        }
        return path;
    }

    private static <T> Join toJoin(Root<T> root, String relationPath) {
        String[] segments = relationPath.split("\\.");
        Join join = Collection.class.isAssignableFrom(root.get(segments[0]).getJavaType()) ?
                root.join(segments[0], JoinType.LEFT) : root.join(segments[0]);
        if(segments.length > 1) {
            for(int i = 1; i < segments.length; i++) {
                join = Collection.class.isAssignableFrom(join.get(segments[i]).getJavaType()) ?
                        join.join(segments[0], JoinType.LEFT) : join.join(segments[0]);
            }
        }
        return join;
    }

    private static String predicateToSql(String tableAlias, Predicate predicate) {
        if(predicate instanceof Junction) {
            Junction junction = (Junction) predicate;
            return "(" + (junction.getPredicates().stream().map(p -> predicateToSql(tableAlias, p))
                    .collect(Collectors.joining(junction.isConjunction() ? " and " : " or "))) + ")";
        }
        else {
            return simplePredicateToSql(tableAlias, (SimplePredicate)predicate);
        }
    }

    private static String simplePredicateToSql(String tableAlias, SimplePredicate predicate) {
        if(predicate.isLiteralSql()) {
            return predicate.getLiteralValue();
        }
        else {
            StringBuilder sb = new StringBuilder();
            Operator operator = predicate.getOperator();
            String operatorExpr = Operator.getExpr(operator);
            String columnName = getColumnName(tableAlias, predicate.getProperty());

            if (Operator.isNoValue(operator)) {
                sb.append(columnName).append(" ").append(operatorExpr);
            } else {
                sb.append(columnName).append(" ").append(operatorExpr).append(" ");
                if (operator == Operator.IN) {
                    sb.append("(:").append(predicate.getQueryParameterName()).append(")");
                } else {
                    sb.append(":").append(predicate.getQueryParameterName());
                }
            }
            return sb.toString();
        }
    }

    private static LinkedHashMap<String, Class> buildRelationPathClassMap(Class clazz, String path) {
        return buildRelationPathClassMap(clazz, path, 1);
    }

    private static LinkedHashMap<String, Class> buildRelationPathClassMap(Class clazz, String path, int depth) {
        try {
            String[] segments = path.split("\\.");
            Field field = clazz.getField(segments[depth - 1]);
            Class relationClass;
            if(Collection.class.isAssignableFrom(field.getType())) {
                relationClass = (Class) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
            }
            else {
                relationClass = field.getType();
            }
            LinkedHashMap<String, Class> result = new LinkedHashMap<>();
            if(depth < segments.length) {
                result.put(path.substring(0, StringUtils.ordinalIndexOf(path, ".", depth)), relationClass);
                depth++;
                result = buildRelationPathClassMap(relationClass, path, depth);
            }
            else {
                result.put(path, relationClass);
            }
            return result;
        } catch (NoSuchFieldException e) {
            throw new InternalServerErrorException(String.format("Class %s property %s doesn't exist", clazz.getName(), path), e);
        }
    }

    private static Map<String, String> buildIdColumnNameAliasMap(String relation, Class clazz) {
        return buildColumnNameAliasMap(relation, clazz, JPAUtils::isIdColumn);
    }

    private static Map<String, String> buildColumnNameAliasMap(String relation, Class clazz) {
        return buildColumnNameAliasMap(relation, clazz, null);
    }

    private static Map<String, String> buildColumnNameAliasMap(String relation, Class clazz, java.util.function.Predicate<Field> predicate) {
        Set<String> properties = new HashSet<>();
        for(Field field: FieldUtils.getAllFieldsList(clazz)) {
            if(predicate != null && !predicate.test(field)) {
                continue;
            }
            if(isNotEmbeddedIdColumn(field)) {
                properties.add(field.getName());
            }
            else if(isEmbeddedIdColumn(field)) {
                for(Field idField: field.getType().getDeclaredFields()) {
                    if(isNotEmbeddedIdColumn(idField)) {
                        properties.add(idField.getName());
                    }
                }
            }
        }
        return properties.stream().collect(Collectors.toMap(p -> getColumnName(relation, p), p -> getColumnAlias(relation, p)));
    }

    private static String getColumnName(String tableAlias, String property) {
        if(property.contains(".")) {
            return "`" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,
                    property.substring(StringUtils.lastOrdinalIndexOf(property, ".", 2) + 1))
                    .replaceFirst("\\.", "`.");
        }
        else {
            return tableAlias == null ? CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, property) :
                    "`" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,
                            tableAlias.contains(".") ? tableAlias.substring(tableAlias.lastIndexOf(".") + 1) : tableAlias) + "`"
                             + "." + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, property);
        }
    }

    private static String getColumnAlias(String tableAlias, String property) {
        return tableAlias == null ? "`" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, property) + "`" :
                "`" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, tableAlias) + "." +
                        CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, property) + "`";
    }

    private static boolean isColumn(Field field) {
        return field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Basic.class)
                || field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class);
    }

    private static boolean isIdColumn(Field field) {
        return field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class);
    }

    private static boolean isEmbeddedIdColumn(Field field) {
        return field.isAnnotationPresent(EmbeddedId.class);
    }

    private static boolean isNotEmbeddedIdColumn(Field field) {
        return field.isAnnotationPresent(Basic.class) || field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(Id.class);
    }

    public static <T> List<T> toEntity(Class<T> clazz, List<Map<String, Object>> rows) {
        log.debug("rows = {}", rows);
        Set<T> entitySet = new LinkedHashSet<>();
        boolean hasRelation = false;
        for(Map<String, Object> row: rows) {
            boolean onlyEntity = row.keySet().stream().noneMatch(columnName -> columnName.split("\\.").length >= 2);
            if(onlyEntity) {
                Object entity = newInstance(clazz);
                populateEntityProperties(entity, row);
                entitySet.add((T)entity);
            }
            else {
                if (entitySet.isEmpty()) {
                    hasRelation = row.size() > row.keySet().stream().filter(columnName -> columnName.split("\\.").length == 2).count();
                }
                // 注意: entity 一定要有 default constructor，否則須傳入 newInstance function
                Object entity = newInstance(clazz);
                populateEntityProperties(entity, row.entrySet().stream().filter(entry -> entry.getKey().split("\\.").length == 2)
                        .collect(Collectors.toMap(entry -> entry.getKey().split("\\.")[1], Map.Entry::getValue)));
                entitySet.add((T)entity);
                if (hasRelation) {
                    Map<String, Object> relationMap = row.entrySet().stream().filter(entry -> entry.getKey().split("\\.").length >= 3)
                            .collect(Collectors.toMap(entry -> entry.getKey().substring(entry.getKey().indexOf(".") + 1), Map.Entry::getValue));
                    populateRelationEntities(entity, relationMap);
                }
            }
        }
        return new ArrayList<>(entitySet);
    }

    private static void populateEntityProperties(Object entity, Map<String, Object> valueMap) {
        try {
            BeanUtils.populate(entity, valueMap);
        } catch (Throwable e) {
            throw new InvalidEntityException(String.format("fail to populate entity %s", entity), e);
        }
    }

    //僅支援單向 binding
    private static void populateRelationEntities(Object entity, Map<String, Object> map){
        if(map.isEmpty())
            return;

        Map<String, Map<String, Object>> relationMap = map.entrySet().stream().filter(entry -> entry.getKey().split("\\.").length == 2)
                .collect(Collectors.groupingBy(entry -> entry.getKey().split("\\.")[0], Collectors.toMap(entry -> entry.getKey().split("\\.")[1], Map.Entry::getValue)));
        for(Map.Entry<String, Map<String, Object>> relationMapEntry: relationMap.entrySet()) {
            String relation = relationMapEntry.getKey();
            Map<String, Object> relationPropMap = relationMapEntry.getValue();
            try {
                Class relationClass = PropertyUtils.getPropertyType(entity, relation);
                //OneToOne, ManyToOne
                if (!Collection.class.isAssignableFrom(relationClass)) {
                    Object relationEntity = PropertyUtils.getProperty(entity, relation);
                    if (relationEntity == null) {
                        relationEntity = newInstance(relationClass);
                        populateEntityProperties(relationEntity, relationPropMap);
                        PropertyUtils.setProperty(entity, relation, relationEntity);
                    }
                    Map<String, Object> nestedRelationMap = map.entrySet().stream().filter(entry -> entry.getKey().split("\\.").length >= 3)
                            .collect(Collectors.toMap(entry -> entry.getKey().split("\\.")[2], Map.Entry::getValue));
                    populateRelationEntities(relationEntity, nestedRelationMap);
                } else {
                    Collection<Object> values = (Collection<Object>) PropertyUtils.getProperty(entity, relation);
                    if (values == null) {
                        if (List.class.isAssignableFrom(relationClass)) {
                            values = new ArrayList<>();
                        } else if (Set.class.isAssignableFrom(relationClass)) {
                            values = new HashSet<>();
                        } else {
                            throw new InvalidEntityException(String.format("Unsupported collection type %s", relationClass.getSimpleName()));
                        }
                    }
                    relationClass = (Class)((ParameterizedType)entity.getClass().getField(relation).getGenericType()).getActualTypeArguments()[0];
                    Object relationEntity = newInstance(relationClass);
                    populateEntityProperties(relationEntity, relationPropMap);
                    if (!values.contains(relationEntity)) {
                        values.add(relationEntity);
                        Map<String, Object> nestedRelationMap = map.entrySet().stream().filter(entry -> entry.getKey().split("\\.").length >= 3)
                                .collect(Collectors.toMap(entry -> entry.getKey().split("\\.")[2], Map.Entry::getValue));
                        populateRelationEntities(relationEntity, nestedRelationMap);
                    }
                }
            } catch (Throwable e) {
                throw new InvalidEntityException(String.format("fail to populate relation %s of entity %s", relation, entity), e);
            }
        }
    }

    private static Object newInstance(Class clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new InvalidEntityException(String.format("fail to initialize instance %s", clazz.getName()), e);
        }
    }
}
