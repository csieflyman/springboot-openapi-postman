package base.exception;

import base.dto.BaseResponseCode;

/**
 * @author csieflyman
 */
public class ConfigurationException extends BaseException {

    public ConfigurationException(String message) {
        this(message, null);
    }

    public ConfigurationException(String message, Throwable e) {
        super(message, e, BaseResponseCode.INTERNAL_SERVER_ERROR);
    }
}
