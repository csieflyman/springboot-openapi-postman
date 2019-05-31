package base.dto;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * @author csieflyman
 */
@GroupSequence({ Default.class, OrderedGroups.First.class, OrderedGroups.Second.class, OrderedGroups.LAST.class})
public class OrderedGroups {
    interface First{}
    interface Second{}
    interface LAST{}
}