package base.exception;

import base.dto.BaseResponseCode;
import lombok.Getter;

/**
 * @author csieflyman
 */
public class MyRestClientNoResponseException extends BaseException {

    @Getter
    private String serviceName;

    @Getter
    private String url;

    @Getter
    private String requestBody;

    public MyRestClientNoResponseException(Throwable cause, String serviceName, String url, String requestBody) {
        super(String.format("No Response [service = %s, url = %s, requestBody = %s]", serviceName, url, requestBody), cause, BaseResponseCode.SERVICE_NO_RESPONSE);
        this.serviceName = serviceName;
        this.url = url;
        this.requestBody = requestBody;
    }
}
