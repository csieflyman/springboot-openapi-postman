package base.exception;

import base.dto.BaseResponseCode;

/**
 * @author csieflyman
 */
public class InvalidQueryException extends BaseException {

    public InvalidQueryException(String message) {
        super(message, null, BaseResponseCode.REQUEST_BAD_REQUEST);
    }
}
