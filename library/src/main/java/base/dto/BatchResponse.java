package base.dto;

import base.exception.BadBatchRequestException;
import base.exception.BaseException;
import base.model.Identifiable;
import base.util.Json;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Throwables;
import com.google.common.collect.LinkedHashMultimap;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
public class BatchResponse extends Response<BatchResponse.BatchResult> {

    public static BatchResponse empty() {
        return new BatchResponse(new ArrayList<>(), LinkedHashMultimap.create());
    }

    public BatchResponse(List<String> successIds, LinkedHashMultimap<String, Throwable> failureResultMap) {
        super(failureResultMap != null && failureResultMap.size() > 0 ?
                BaseResponseCode.REQUEST_BATCH_FAILURE : BaseResponseCode.SUCCESS, new BatchResult(successIds, failureResultMap));
    }

    public BatchResponse(Map<String, Object> successResultMap, LinkedHashMultimap<String, Throwable> failureResultMap) {
        super(failureResultMap != null && failureResultMap.size() > 0 ?
                BaseResponseCode.REQUEST_BATCH_FAILURE : BaseResponseCode.SUCCESS, new BatchResult(successResultMap, failureResultMap));
    }

    private BatchResponse(List<String> successIds, BadBatchRequestException e) {
        super(BaseResponseCode.REQUEST_BATCH_FAILURE, new BatchResult(successIds, e));
    }

    public void addFailure(BadBatchRequestException e) {
        for(Identifiable Identifiable: e.getInvalidObjects()) {
            FailureResult failureResult = new FailureResult(Identifiable.getId().toString());
            failureResult = getFailureResult(failureResult);
            failureResult.addError(e.getResponseCode(), e.getErrorMsg(Identifiable));
        }
    }

    public void addFailure(String id, Throwable e) {
        FailureResult failureResult = new FailureResult(id);
        failureResult = getFailureResult(failureResult);
        failureResult.addError(e);
    }

    private FailureResult getFailureResult(FailureResult failureResult) {
        int index = this.result.failure.indexOf(failureResult);
        if(index != -1) {
            failureResult = this.result.failure.get(index);
        }
        else {
            result.failure.add(failureResult);
        }
        return failureResult;
    }

    public void add(BatchResponse response) {
        this.result.success.addAll(response.result.success);
        this.result.failure.addAll(response.result.failure);
    }

    public boolean isFailureObject(String id) {
        return result.failure.stream().anyMatch(failureResult -> failureResult.id.equals(id));
    }

    @JsonIgnore
    public boolean hasFailure() {
        return !result.failure.isEmpty();
    }

    //可參考 facebook Graph API範例 https://developers.facebook.com/docs/graph-api/making-multiple-requests/
    @JsonIgnore
    public int getStatusCode() {
        if(result.success.size() + result.failure.size() == 1) {
            if(hasFailure()) {
                return result.failure.iterator().next().statusCode;
            }
            else {
                return 200;
            }
        }
        else {
            return 200;
        }
    }

    static class BatchResult {

        private List<SuccessResult> success = new ArrayList<>();
        private List<FailureResult> failure = new ArrayList<>();

        private BatchResult(List<String> successIds, BadBatchRequestException e) {
            if(successIds != null) {
                this.success = successIds.stream().map(successId -> new SuccessResult(successId, new Object())).collect(Collectors.toList());
            }
            for(base.model.Identifiable Identifiable: e.getInvalidObjects()) {
                FailureResult failureResult = new FailureResult(Identifiable.getId().toString());
                failureResult.addError(e.getResponseCode(), e.getErrorMsg(Identifiable));
                failure.add(failureResult);
            }
        }

        private BatchResult(List<String> successIds, LinkedHashMultimap<String, Throwable> failureResultMap) {
            this(successIds.stream().collect(Collectors.toMap(id -> id, id -> Json.newObject())), failureResultMap);
        }

        private BatchResult(Map<String, Object> successResultMap, LinkedHashMultimap<String, Throwable> failureResultMap) {
            if(successResultMap != null) {
                this.success = successResultMap.entrySet().stream().map(entry ->
                        new SuccessResult(entry.getKey(), entry.getValue())).collect(Collectors.toList());
            }
            if(failureResultMap != null) {
                for (String id : failureResultMap.keySet()) {
                    FailureResult result = new FailureResult(id);
                    failureResultMap.get(id).forEach(result::addError);
                    failure.add(result);
                }
            }
        }
    }

    @Getter
    @AllArgsConstructor
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @ToString
    private static class SuccessResult{
        @NonNull
        @EqualsAndHashCode.Include
        private String id;
        private Object data;
    }

    @Getter
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @ToString
    private static class FailureResult {

        @NonNull
        @EqualsAndHashCode.Include
        private String id;
        private List<FailureCodeMessage> errors = new ArrayList<>();
        @JsonIgnore
        private Integer statusCode;

        private FailureResult(String id) {
            this.id = id;
        }

        private void addError(Throwable e) {
            ResponseCode responseCode = e instanceof BaseException ? ((BaseException) e).getResponseCode() : BaseResponseCode.INTERNAL_SERVER_ERROR;
            String message = responseCode.getMessage() + " " + (e instanceof BaseException ? e.getMessage() : Throwables.getRootCause(e).getMessage());
            errors.add(new FailureCodeMessage(responseCode.getCode(), message));
            if(statusCode == null) {
                statusCode = responseCode.getStatusCode();
            }
        }

        private void addError(ResponseCode responseCode, String message) {
            errors.add(new FailureCodeMessage(responseCode.getCode(), message));
            if(statusCode == null) {
                statusCode = responseCode.getStatusCode();
            }
        }
    }

    @Value
    @NonNull
    private static class FailureCodeMessage {
        private String code;
        private String message;
    }
}
