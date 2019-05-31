package base.exception;

import base.dto.BaseResponseCode;

/**
 * @author csieflyman
 */
public class ObjectDuplicateException extends BaseException {

    public ObjectDuplicateException(String message) {
        super(message, null, BaseResponseCode.REQUEST_RESOURCE_CONFLICT);
    }
}
