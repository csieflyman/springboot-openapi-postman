package base.util.query;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Set;

/**
 * @author csieflyman
 */
public class SimplePredicate implements Predicate{

    private String property;

    private Operator operator;

    private Object value;

    private String queryParameterName;

    private Object queryParameterValue;

    private String literalSql;

    SimplePredicate(String property, Operator operator, Object value) {
        Preconditions.checkArgument(property != null, "property is null");
        Preconditions.checkArgument(operator != null, "operator is null");
        if (!Operator.isNoValue(operator)) {
            Preconditions.checkArgument(value != null, "value is null");
        }
        if (operator == Operator.IN && Set.class.isAssignableFrom(value.getClass())) {
            Preconditions.checkArgument(!((Set) value).isEmpty(), property + " with in operator can't has empty collection value");
        }

        this.property = property;
        this.operator = operator;
        this.value = value;
    }

    private SimplePredicate(String literalSql) {
        this.literalSql = literalSql;
    }

    String getLiteralValue() {
        Preconditions.checkState(isLiteralSql());
        return literalSql;
    }

    boolean isLiteralSql() {
        return literalSql != null;
    }

    String getProperty() {
        Preconditions.checkState(!isLiteralSql());
        return property;
    }

    Operator getOperator() {
        Preconditions.checkState(!isLiteralSql());
        return operator;
    }

    Object getValue() {
        Preconditions.checkState(!isLiteralSql());
        return value;
    }

    void setValue(Object value) {
        Preconditions.checkState(!isLiteralSql());
        this.value = value;
    }

    String getQueryParameterName() {
        Preconditions.checkState(!isLiteralSql());
        return queryParameterName;
    }

    void setQueryParameterName(String queryParameterName) {
        Preconditions.checkState(!isLiteralSql());
        Preconditions.checkNotNull(queryParameterName, "null value of predicate queryParameterName " + this);
        this.queryParameterName = queryParameterName;
    }

    Object getQueryParameterValue() {
        Preconditions.checkState(!isLiteralSql());
        return queryParameterValue;
    }

    void setQueryParameterValue(Object queryParameterValue) {
        Preconditions.checkState(!isLiteralSql());
        Preconditions.checkNotNull(queryParameterValue, "null value of predicate queryParameterValue " + this);
        this.queryParameterValue = queryParameterValue;
    }

    boolean isNestedProperty() {
        Preconditions.checkState(!isLiteralSql());
        return property.contains(".");
    }

    static SimplePredicate literal(String literalSql) {
        return new SimplePredicate(literalSql);
    }

    static SimplePredicate eq(String property, Object value) {
        return new SimplePredicate(property, Operator.EQ, value);
    }

    static SimplePredicate ne(String property, Object value) {
        return new SimplePredicate(property, Operator.NE, value);
    }

    static SimplePredicate lt(String property, Comparable value) {
        return new SimplePredicate(property, Operator.LT, value);
    }

    static SimplePredicate le(String property, Comparable value) {
        return new SimplePredicate(property, Operator.LE, value);
    }

    static SimplePredicate gt(String property, Comparable value) {
        return new SimplePredicate(property, Operator.GT, value);
    }

    static SimplePredicate ge(String property, Comparable value) {
        return new SimplePredicate(property, Operator.GE, value);
    }

    static SimplePredicate in(String property, Set<?> values) {
        return new SimplePredicate(property, Operator.IN, values);
    }

    static SimplePredicate like(String property, String value) {
        return new SimplePredicate(property, Operator.LIKE, value);
    }

    static SimplePredicate isNull(String property) {
        return new SimplePredicate(property, Operator.IS_NULL, null);
    }

    static SimplePredicate isNotNull(String property) {
        return new SimplePredicate(property, Operator.IS_NOT_NULL, null);
    }

    static SimplePredicate isEmpty(String property) {
        return new SimplePredicate(property, Operator.IS_EMPTY, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimplePredicate other = (SimplePredicate) o;
        if(isLiteralSql()) {
            return new EqualsBuilder().append(this.literalSql, other.literalSql).isEquals();
        }
        else {
            return new EqualsBuilder().append(this.getProperty(), other.getProperty())
                    .append(this.getOperator(), other.getOperator()).isEquals();
        }
    }

    @Override
    public int hashCode() {
        if(isLiteralSql()) {
            return new HashCodeBuilder().append(this.literalSql).toHashCode();
        }
        else {
            return new HashCodeBuilder().append(this.getProperty()).append(this.getOperator()).toHashCode();
        }
    }

    @Override
    public String toString() {
        if(isLiteralSql()) {
            return literalSql;
        }
        else {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
