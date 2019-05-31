package base.exception;

import base.dto.BaseResponseCode;

/**
 * @author csieflyman
 */
public class ObjectNotFoundException extends BaseException {

    public ObjectNotFoundException(String message) {
        super(message, null, BaseResponseCode.REQUEST_BAD_REQUEST);
    }
}
