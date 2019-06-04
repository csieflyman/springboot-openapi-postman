package base.dto;

import base.exception.BaseException;
import base.exception.InternalServerErrorException;
import base.util.Json;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author csieflyman
 */
@Getter
@ToString
public class Response<T> {

    @JsonIgnore
    private Date responseTime = new Date();

    private String code;

    private String message;

    protected T result;

    public Response(T result) {
        this(BaseResponseCode.SUCCESS, BaseResponseCode.SUCCESS.getMessage(), result);
    }

    public Response(BaseException e) {
        this(e.getResponseCode(), e.getMessage(), (T)e.getResult());
    }

    public Response(ResponseCode responseCode, T result) {
        this(responseCode, responseCode.getMessage(), result);
    }

    public Response(ResponseCode responseCode, String message) {
        this(responseCode, message, null);
    }

    public Response(ResponseCode responseCode, String message, T result) {
        this.code = responseCode.getCode();
        this.message = message;
        this.result = result;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return code.equals(BaseResponseCode.SUCCESS.getCode());
    }

    @JsonIgnore
    private JsonNode resultJsonNode;
    @JsonIgnore
    private List<T> resultList;

    @JsonCreator
    public Response(@JsonProperty("code") String code, @JsonProperty("message") String message, @JsonProperty("result") JsonNode jsonNode) {
        this.code = code;
        this.message = message;
        this.resultJsonNode = jsonNode;
    }

    @JsonIgnore
    public boolean isArrayOfResponseResult() {
        if(resultJsonNode != null) {
            return resultJsonNode.isArray();
        }
        else {
            return false;
        }
    }

    @JsonIgnore
    public Object getResultFromResponse(Class<T> bindClass) {
        if(isArrayOfResponseResult()) {
            return getResultListFromResponse(bindClass);
        }
        else {
            return getResultObjectFromResponse(bindClass);
        }
    }

    @JsonIgnore
    public List<T> getResultListFromResponse(Class<T> bindClass) {
        if(resultList != null)
            return resultList;

        if(resultJsonNode != null) {
            try {
                bindClass.newInstance();
            } catch (Exception e) {
                throw new InternalServerErrorException(String.format("fail to initialize instance %s", bindClass.getName()), e);
            }
            if(resultJsonNode.isArray()) {
                resultList = ImmutableList.copyOf(resultJsonNode.iterator()).stream().map(jsonObject -> Json.toObject(jsonObject, bindClass)).collect(Collectors.toList());
            }
            else {
                throw new InternalServerErrorException(String.format("response json result is object: %s", resultJsonNode.toString()));
            }
            return resultList;
        }
        else {
            return Collections.emptyList();
        }
    }

    @JsonIgnore
    public T getResultObjectFromResponse(Class<T> bindClass) {
        if(result != null)
            return result;

        if(resultJsonNode != null) {
            try {
                bindClass.newInstance();
            } catch (Exception e) {
                throw new InternalServerErrorException(String.format("fail to initialize instance %s", bindClass.getName()), e);
            }
            if(resultJsonNode.isObject()) {
                result = Json.toObject(resultJsonNode, bindClass);
            }
            else {
                throw new InternalServerErrorException(String.format("response json result is array: %s", resultJsonNode.toString()));
            }
            return result;
        }
        else {
            return null;
        }
    }
}
