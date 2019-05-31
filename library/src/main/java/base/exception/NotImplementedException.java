package base.exception;

import base.dto.BaseResponseCode;

/**
 * @author csieflyman
 */
public class NotImplementedException extends BaseException {

    public NotImplementedException(String message) {
        super(message, null, BaseResponseCode.INTERNAL_SERVER_ERROR);
    }
}
