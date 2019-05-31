package base.exception;

import base.dto.ResponseCode;

/**
 * @author csieflyman
 */
public abstract class BaseException extends RuntimeException {

    protected ResponseCode responseCode;

    protected Object result;

    public BaseException(String message, ResponseCode responseCode) {
        this(message, null, responseCode);
    }

    public BaseException(String message, Throwable cause, ResponseCode responseCode) {
        this(message, cause, responseCode, null);
    }

    public BaseException(String message, Throwable cause, ResponseCode responseCode, Object result) {
        super(message, cause);
        this.responseCode = responseCode;
        this.result = result;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    public Object getResult() {
        return result;
    }
}
