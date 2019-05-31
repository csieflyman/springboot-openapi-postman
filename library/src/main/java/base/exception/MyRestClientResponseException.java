package base.exception;

import base.dto.BaseResponseCode;
import lombok.Getter;
import org.springframework.web.client.RestClientResponseException;

/**
 * @author csieflyman
 */
public class MyRestClientResponseException extends BaseException {

    @Getter
    private String serviceName;

    @Getter
    private String url;

    @Getter
    private int statusCode;

    @Getter
    private String requestBody;

    @Getter
    private String responseBody;

    @Getter
    private String responseHeader;

    public MyRestClientResponseException(String message, RestClientResponseException ex, String serviceName, String url, String requestBody) {
        this(message, ex, serviceName, url, ex.getRawStatusCode(), requestBody, ex.getResponseBodyAsString(), ex.getResponseHeaders() != null ? ex.getResponseHeaders().toString() : null);
    }

    public MyRestClientResponseException(String message, Throwable cause, String serviceName, String url, int statusCode, String requestBody, String responseBody, String responseHeader) {
        super(String.format("[%s] %s [service = %s, url = %s, requestBody = %s, responseBody = %s, responseHeader = %s]",
                statusCode, message, serviceName, url, requestBody, responseBody, responseHeader), cause, BaseResponseCode.SERVER_BAD_RESPONSE);
        this.serviceName = serviceName;
        this.url = url;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
        this.responseHeader = responseHeader;
    }
}
