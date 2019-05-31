package base.exception;

import base.dto.BaseResponseCode;

/**
 * @author csieflyman
 */
public class ConversionException extends BaseException {

    public ConversionException(String message) {
        this(message, null);
    }

    public ConversionException(String message, Throwable cause) {
        super(message, cause, BaseResponseCode.REQUEST_BAD_REQUEST);
    }
}
