package base.exception;

import base.dto.ResponseCode;
import base.dto.BatchResponse;

/**
 * @author csieflyman
 */
public class BatchResposeException extends BaseException {

    private BatchResponse response;

    public BatchResposeException(BatchResponse response, ResponseCode responseCode) {
        super(null, null, responseCode);
        this.response = response;
    }

    public BatchResponse getResponse() {
        return response;
    }
}
