package base.exception;

import base.dto.BaseResponseCode;

/**
 * @author csieflyman
 */
public class AuthorizationException extends BaseException {

    public AuthorizationException(String message) {
        super(message, null, BaseResponseCode.REQUEST_FORBIDDEN);
    }
}
