package base.exception;

import base.dto.BaseResponseCode;

/**
 * @author csieflyman
 */
public class BadRequestException extends BaseException {

    private String body;

    public BadRequestException(String message) {
        this(message, null);
    }

    public BadRequestException(String message, String body) {
        super(message, BaseResponseCode.REQUEST_BAD_REQUEST);
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}
