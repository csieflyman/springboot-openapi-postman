package base.dto;

import javax.validation.GroupSequence;
import javax.validation.groups.Default;

/**
 * @author csieflyman
 */
@GroupSequence({Default.class, CreateFormGroups.CREATE.class, CreateFormGroups.CREATE_SECOND.class})
public interface CreateFormGroups {

    interface CREATE{}
    interface CREATE_SECOND{}
}
