package base.exception;

import base.dto.BaseResponseCode;

/**
 * @author csieflyman
 */
public class InvalidEntityException extends BaseException {

    public InvalidEntityException(String message) {
        this(message, null);
    }

    public InvalidEntityException(String message, Throwable cause) {
        super(message, cause, BaseResponseCode.INTERNAL_SERVER_ERROR);
    }
}
