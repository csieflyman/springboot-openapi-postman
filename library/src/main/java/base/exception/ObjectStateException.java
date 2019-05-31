package base.exception;

import base.dto.BaseResponseCode;

/**
 * @author csieflyman
 */
public class ObjectStateException extends BaseException {

    public ObjectStateException(String message) {
        this(message, BaseResponseCode.REQUEST_RESOURCE_CONFLICT);
    }

    public ObjectStateException(String message, BaseResponseCode code) {
        this(message, code, null);
    }

    public ObjectStateException(String message, BaseResponseCode code, Object result) {
        super(message, null, code, result);
    }
}
