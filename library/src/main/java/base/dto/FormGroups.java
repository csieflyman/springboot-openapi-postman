package base.dto;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * @author csieflyman
 */
public interface FormGroups {

    interface CREATE{}
    interface UPDATE{}

    @GroupSequence({Default.class, FormGroups.CREATE_ORDERS.FIRST.class, FormGroups.CREATE_ORDERS.SECOND.class})
    interface CREATE_ORDERS{
        interface FIRST{}
        interface SECOND{}
    }

    @GroupSequence({Default.class, FormGroups.UPDATE_ORDERS.FIRST.class, FormGroups.UPDATE_ORDERS.SECOND.class})
    interface UPDATE_ORDERS{
        interface FIRST{}
        interface SECOND{}
    }
}
