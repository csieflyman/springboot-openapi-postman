package base.dto;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * @author csieflyman
 */
@GroupSequence({Default.class, UpdateFormGroups.UPDATE.class, UpdateFormGroups.UPDATE_SECOND.class})
public interface UpdateFormGroups {
    interface UPDATE{}
    interface UPDATE_SECOND{}
}
