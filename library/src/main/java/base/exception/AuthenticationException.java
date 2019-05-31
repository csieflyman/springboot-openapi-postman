package base.exception;

import base.dto.BaseResponseCode;
import base.exception.BaseException;

/**
 * @author csieflyman
 */
public class AuthenticationException extends BaseException {

    public AuthenticationException(String message) {
        super(message, null, BaseResponseCode.REQUEST_UNAUTHENTICATED);
    }
}
