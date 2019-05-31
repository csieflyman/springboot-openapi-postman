package base.util.query;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
public class Junction implements Predicate {

    private Query query;

    private Set<Predicate> predicates = new LinkedHashSet<>();

    private boolean isConjunction;

    Junction(boolean isConjunction, Query query) {
        this(isConjunction, new HashSet<>(), query);
    }

    Junction(boolean isConjunction, Collection<Predicate> predicates, Query query) {
        this.isConjunction = isConjunction;
        this.predicates.addAll(predicates);
        this.query = query;
    }

    private Junction(boolean isConjunction) {
        this.isConjunction = isConjunction;
    }

    public static Junction and() {
        return new Junction(true);
    }

    public static Junction or() {
        return new Junction(false);
    }

    void setConjunction(boolean isConjunction) {
        this.isConjunction = isConjunction;
    }

    public boolean isConjunction() {
        return isConjunction;
    }

    void setQuery(Query query) {
        Preconditions.checkNotNull(query);
        this.query = query;
    }

    public Query end() {
        Preconditions.checkNotNull(query);
        return query;
    }

    public Set<Predicate> getPredicates() {
        return predicates;
    }

    public void add(Predicate... predicates) {
        addAll(Arrays.stream(predicates).collect(Collectors.toList()));
    }

    public void addAll(Collection<Predicate> predicates) {
        this.predicates.addAll(predicates);
    }

    public Junction literal(String literalSql) {
        add(SimplePredicate.literal(literalSql));
        return this;
    }

    public Junction eq(String property, Object value) {
        add(SimplePredicate.eq(property, value));
        return this;
    }

    public Junction ne(String property, Object value) {
        add(SimplePredicate.ne(property, value));
        return this;
    }

    public Junction lt(String property, Comparable value) {
        add(SimplePredicate.lt(property, value));
        return this;
    }

    public Junction le(String property, Comparable value) {
        add(SimplePredicate.le(property, value));
        return this;
    }

    public Junction ge(String property, Comparable value) {
        add(SimplePredicate.ge(property, value));
        return this;
    }

    public Junction gt(String property, Comparable value) {
        add(SimplePredicate.gt(property, value));
        return this;
    }

    public <T> Junction between(String property, Comparable<T> geValue, Comparable<T> ltValue) {
        add(SimplePredicate.ge(property, geValue));
        add(SimplePredicate.lt(property, ltValue));
        return this;
    }

    public Junction in(String property, Set<?> values) {
        add(SimplePredicate.in(property, values));
        return this;
    }

    public Junction like(String property, String value) {
        add(SimplePredicate.like(property, value));
        return this;
    }

    public Junction isNull(String property) {
        add(SimplePredicate.isNull(property));
        return this;
    }

    public Junction isNotNull(String property) {
        add(SimplePredicate.isNotNull(property));
        return this;
    }

    public Junction isEmpty(String property) {
        add(SimplePredicate.isEmpty(property));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Junction other = (Junction) o;
        return new EqualsBuilder().append(isConjunction, other.isConjunction).append(predicates, other.predicates).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isConjunction).append(predicates).toHashCode();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
