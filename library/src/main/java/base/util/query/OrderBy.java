package base.util.query;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author csieflyman
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class OrderBy {

    private boolean asc;

    @EqualsAndHashCode.Include
    private String property;

    private OrderBy(String property, boolean asc) {
        this.property = property;
        this.asc = asc;
    }

    static OrderBy asc(String property) {
        return new OrderBy(property, true);
    }

    static OrderBy desc(String property) {
        return new OrderBy(property, false);
    }

    public boolean isAsc() {
        return asc;
    }

    public String getProperty() {
        return property;
    }
}
