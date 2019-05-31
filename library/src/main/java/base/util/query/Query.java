package base.util.query;

import base.exception.InvalidQueryException;
import base.util.DateTimeUtils;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
public class Query {

    private int pageNo = -1;
    private int pageSize = 10;
    private boolean onlySize = false;
    private Set<OrderBy> orderByList = new LinkedHashSet<>();
    private Junction junction;
    private Set<String> fetchRelations = new LinkedHashSet<>();
    private Set<String> fetchProperties = new LinkedHashSet<>();
    private Map<String, Object> paramMap = new HashMap<>(); //用來傳入自訂參數值

    private static final String Q_PAGE_NO = "pageNo";
    private static final String Q_PAGE_SIZE = "pageSize";
    private static final String Q_SORT = "sort";
    private static final String Q_ONLY_SIZE = "onlySize";
    private static final String Q_PREDICATES = "predicates";
    private static final String Q_PREDICATES_DISJUNCTION = "predicatesDisjunction";
    public static final String Q_FETCH_RELATIONS = "relations";
    private static final String Q_FETCH_FIELDS = "fields";

    private DateTimeFormatter dateTimeFormatter = DateTimeUtils.UTC_DATE_TIME_FORMATTER;

    private Query() {

    }

    public static Query create() {
        return new Query().where().end();
    }

    public static Query create(MultiValueMap<String, String> paramMap) {
        return create(paramMap, null);
    }

    public static Query create(MultiValueMap<String, String> paramMap, DateTimeFormatter dateTimeFormatter) {
        return create(paramMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toArray(new String[0]))), dateTimeFormatter);
    }

    public static Query create(Map<String, String[]> paramMap) {
        return create(paramMap, null);
        //return create(paramMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new String[]{e.getValue()})), null);
    }

    public static Query create(Map<String, String[]> paramMap, DateTimeFormatter dateTimeFormatter) {
        Preconditions.checkArgument(paramMap != null, "paramMap must not be null");

        Query query = Query.create();
        if(dateTimeFormatter != null) {
            query.dateTimeFormatter = dateTimeFormatter;
        }
        paramMap.forEach((key, value) -> query.put(key, value[0]));
        return query;
    }

    private void put(String param, String value) {
        Preconditions.checkArgument(param != null, "param must not be null");
        Preconditions.checkArgument(value != null, "value must not be null");

        switch (param) {
            case Q_PAGE_NO:
                pageNo(Integer.parseInt(value));
                break;
            case Q_PAGE_SIZE:
                pageSize(Integer.parseInt(value));
                break;
            case Q_SORT:
                parseSortString(value);
                break;
            case Q_ONLY_SIZE:
                onlySize = Boolean.parseBoolean(value);
                break;
            case Q_PREDICATES:
                parsePredicateString(value);
                break;
            case Q_PREDICATES_DISJUNCTION:
                junction.setConjunction(false);
                break;
            case Q_FETCH_RELATIONS:
                fetchRelations(Sets.newHashSet(Splitter.on(",").split(value))
                        .stream().map(String::trim).collect(Collectors.toSet()));
                break;
            case Q_FETCH_FIELDS:
                fetchProperties(Sets.newHashSet(Splitter.on(",").split(value))
                        .stream().map(String::trim).collect(Collectors.toSet()));
                break;
            default:
                paramMap.put(param, value);
                //throw new IllegalArgumentException("unknown query parameter: " + param);
        }
    }

    private void parseSortString(String s) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(s), "sort string can't be empty");

        String[] orderByProps = s.split(",");
        for (String orderByProp : orderByProps) {
            orderByProp = orderByProp.trim();
            if (orderByProp.startsWith("+")) {
                orderByList.add(OrderBy.asc(orderByProp.substring(1)));
            } else if (orderByProp.startsWith("-")) {
                orderByList.add(OrderBy.desc(orderByProp.substring(1)));
            } else {
                orderByList.add(OrderBy.asc(orderByProp));
            }
        }
    }

    private void parsePredicateString(String s) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(s), "predicate string can't be empty");
        Preconditions.checkArgument(s.startsWith("[") && s.endsWith("]"), "invalid q_predicate format: " + s);

        s = s.substring(1, s.length() - 1).trim();
        try {
            s = URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new UnsupportedOperationException(String.format("fail to decode predicateValue %s to UTF-8", s), e);
        }
        List<String> predicateStrings = Lists.newArrayList(Splitter.on(";").split(s));
        try {
            junction.addAll(predicateStrings.stream().map(String::trim).filter(StringUtils::isNotEmpty).map(ps -> Lists.newArrayList(Splitter.on(" ").split(ps)))
                    .map(psList -> new SimplePredicate(psList.get(0), Operator.exprValueOf(psList.get(1)),
                            psList.size() == 2 ? null : tryToConvertToDate(String.join(" ", psList.subList(2, psList.size())))))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));

            junction.getPredicates().stream().map(p -> (SimplePredicate) p).filter(ps -> ps.getOperator() == Operator.IN).forEach(predicate -> {
                String valueString = ((String) predicate.getValue()).trim();
                if (!(valueString.startsWith("(") && valueString.endsWith(")"))) {
                    throw new IllegalArgumentException(String.format("%s value string %s should be with the format: (e1, e2)", predicate.getProperty(), valueString));
                }
                valueString = valueString.substring(1, valueString.length() - 1);
                predicate.setValue(Sets.newHashSet(Splitter.on(",").split(valueString)));
            });
        } catch (Throwable e) {
            throw new IllegalArgumentException("invalid q_predicate format: " + s, e);
        }
    }

    private Object tryToConvertToDate(String s) {
        try {
            return Date.from(ZonedDateTime.parse(s, dateTimeFormatter).toInstant());
        } catch (DateTimeParseException e) {
            return s;
        }
    }

    public boolean isPagingQuery() {
        return pageNo > 0;
    }

    public int getPageNo() {
        return pageNo;
    }

    public Query pageNo(int pageNo) {
        Preconditions.checkArgument(pageNo >= 1);
        this.pageNo = pageNo;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Query pageSize(int pageSize) {
        Preconditions.checkArgument(pageSize > 0);
        this.pageSize = pageSize;
        return this;
    }

    public boolean isOnlySize() {
        return onlySize;
    }

    public Query setOnlySize(boolean onlySize) {
        this.onlySize = onlySize;
        return this;
    }

    public Set<OrderBy> getOrderByList() {
        return orderByList;
    }

    public boolean isOrderByEmpty() {
        return orderByList.isEmpty();
    }

    public Query orderByAsc(String property) {
        orderByList.add(OrderBy.asc(property));
        return this;
    }

    public Query orderByDesc(String property) {
        orderByList.add(OrderBy.desc(property));
        return this;
    }

    public Query orderBy(String property, boolean isAsc) {
        return isAsc ? orderByAsc(property) : orderByDesc(property);
    }

    public Junction or() {
        junction = new Junction(false, this);
        return junction;
    }

    public Junction where() {
        junction = new Junction(true, this);
        return junction;
    }

    public Junction setJunction(Junction junction) {
        junction.setQuery(this);
        this.junction = junction;
        return junction;
    }

    public Junction getJunction() {
        return junction;
    }

    public Query append(Junction junction) {
        junction.add(junction);
        return this;
    }

    public Query addParam(String property, Object value) {
        paramMap.put(property, value);
        return this;
    }

    public Object getParam(String property) {
        return paramMap.get(property);
    }

    public Map<String, Object> getParams() {
        return paramMap;
    }

    public Set<String> getFetchRelations() {
        return fetchRelations;
    }

    public Query fetchRelations(String... relations) {
        Preconditions.checkNotNull(relations);
        return fetchRelations(Sets.newHashSet(relations));
    }

    public Query fetchRelations(Set<String> relations) {
        Preconditions.checkNotNull(relations);
        this.fetchRelations.addAll(relations);
        return this;
    }

    public Set<String> getFetchProperties() {
        return fetchProperties;
    }

    public Query fetchProperties(String... properties) {
        Preconditions.checkNotNull(properties);
        return fetchProperties(Sets.newHashSet(properties));
    }

    public Query fetchProperties(Set<String> properties) {
        Preconditions.checkNotNull(properties);
        this.fetchProperties = properties;
        return this;
    }

    public String toQueryString() {
        Set<Predicate> predicates = junction.getPredicates();
        StringBuilder sb = new StringBuilder();
        if(!predicates.isEmpty()) {
            StringBuilder sb2 = new StringBuilder();
            for(Predicate p: predicates) {
                if(p instanceof Junction) {
                    throw new InvalidQueryException("junction is unsupported: " + p);
                }
                SimplePredicate sp = (SimplePredicate) p;
                sb2.append(sp.getProperty()).append(" ").append(Operator.getExprOfQueryString(sp.getOperator())).append(" ");
                if(!Operator.isNoValue(sp.getOperator())) {
                    sb2.append(convertValueToString(sp.getValue()));
                }
                else if(sp.getOperator() == Operator.IN) {
                    sb2.append("(").append(((Set<String>)sp.getValue()).stream().map(this::convertValueToString).collect(Collectors.joining(","))).append(")");
                }
                sb2.append(" ; ");
            }
            sb2.delete(sb2.lastIndexOf(";") - 1, sb2.length());
            String predicateValue = sb2.toString();
            try {
                predicateValue = URLEncoder.encode(predicateValue, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new UnsupportedOperationException(String.format("fail to encode predicateValue %s to UTF-8", predicateValue), e);
            }
            sb.append("&predicates=[").append(predicateValue).append("]");
        }
        if(pageNo != -1) {
            sb.append("&pageNo=").append(pageNo).append("&pageSize=").append(pageSize);
        }
        if(!orderByList.isEmpty()) {
            sb.append("&sort=");
            for(OrderBy orderBy: orderByList) {
                sb.append(orderBy.isAsc() ? "+" : "-").append(orderBy.getProperty()).append(",");
            }
            sb.delete(sb.length() - 1, sb.length());
        }
        if(onlySize) {
            sb.append("&onlySize=true");
        }
        if(!junction.isConjunction()) {
            sb.append("&predicatesDisjunction=true");
        }
        if(!fetchRelations.isEmpty()) {
            sb.append("&relations=").append(String.join(",", fetchRelations));
        }
        if(!fetchProperties.isEmpty()) {
            sb.append("&fields=").append(String.join(",", fetchProperties));
        }
        return sb.length() > 0 ? sb.substring(1) : null;
    }

    private String convertValueToString(Object o) {
        if (Date.class.isAssignableFrom(o.getClass())) {
            return dateTimeFormatter.format(((Date) o).toInstant());
        } else if (o.getClass() == ZonedDateTime.class) {
            return dateTimeFormatter.format((ZonedDateTime) o);
        } else {
            return o.toString();
        }
    }

    public void populatePredicate() {
        populatePredicate(junction);
    }

    public static void populatePredicate(Junction junction) {
        Map<String, Integer> propertyCountMap = new HashMap<>();
        populateQueryParameterName(propertyCountMap, junction);
        populateQueryParameterValue(junction);
    }

    private static void populateQueryParameterName(Map<String, Integer> propertyCountMap, Predicate predicate) {
        if(predicate instanceof Junction) {
            ((Junction)predicate).getPredicates().forEach(p -> populateQueryParameterName(propertyCountMap, predicate));
        }
        else {
            populateQueryParameterName(propertyCountMap, (SimplePredicate)predicate);
        }
    }

    private static void populateQueryParameterName(Map<String, Integer> propertyCountMap, SimplePredicate predicate) {
        String property = predicate.getProperty();
        if (propertyCountMap.containsKey(property)) {
            propertyCountMap.put(property, (propertyCountMap.get(property) + 1));
            predicate.setQueryParameterName(property + propertyCountMap.get(property));
        } else {
            propertyCountMap.put(property, 1);
            predicate.setQueryParameterName(property);
        }
        if (predicate.isNestedProperty()) {
            predicate.setQueryParameterName(predicate.getQueryParameterName().replace(".", "_"));
        }
    }

    //假設不必型別轉換 value
    private static void populateQueryParameterValue(Predicate predicate) {
        if(predicate instanceof Junction) {
            ((Junction)predicate).getPredicates().forEach(Query::populateQueryParameterValue);
        }
        else {
            populateQueryParameterValue((SimplePredicate)predicate);
        }
    }

    //假設不必型別轉換 value
    private static void populateQueryParameterValue(SimplePredicate predicate) {
        if (Operator.isNoValue(predicate.getOperator())) {
            return;
        }
        predicate.setQueryParameterValue(predicate.getValue());
    }

    @Override
    public String toString() {
        return toQueryString();
    }
}
