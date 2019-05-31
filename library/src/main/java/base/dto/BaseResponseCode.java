package base.dto;

import lombok.Getter;

/**
 * @author csieflyman
 */
public enum BaseResponseCode implements ResponseCode{

    SUCCESS(200, "0000", false),
    INTERNAL_SERVER_ERROR(500, "9999", true),

    // 通用錯誤 - Request
    REQUEST_BAD_REQUEST(400, "1000", false),
    REQUEST_UNAUTHENTICATED(401, "1001", false),
    REQUEST_FORBIDDEN(403, "1002", false),
    REQUEST_RESOURCE_CONFLICT(409, "1003", false),
    REQUEST_BATCH_FAILURE(200, "1004", true),

    SERVICE_NO_RESPONSE(500, "1005", true),
    SERVER_BAD_RESPONSE(500, "1006", true);

    @Getter
    private int statusCode;
    @Getter
    private String code;
    @Getter
    private String message;
    @Getter
    private boolean logError;

    BaseResponseCode(int statusCode, String code, boolean logError){
        this.statusCode = statusCode;
        this.code = code;
        this.message = name();
        this.logError = logError;
    }
}
