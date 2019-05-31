package base.dto;

public interface ResponseCode {

    int getStatusCode();

    String getCode();

    String getMessage();

    boolean isLogError();
}
