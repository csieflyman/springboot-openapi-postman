package base.exception;

import base.dto.BaseResponseCode;

/**
 * @author csieflyman
 */
public class InternalServerErrorException extends BaseException {

    public InternalServerErrorException(String message) {
        super(message, null);
    }

    public InternalServerErrorException(String message, Throwable cause) {
        super(message, cause, BaseResponseCode.INTERNAL_SERVER_ERROR);
    }
}
