package config;

import base.dto.BaseResponseCode;
import base.dto.Response;
import base.dto.ResponseCode;
import base.exception.BadRequestException;
import base.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

//import log.service.ErrorLogService;

/**
 * @author csieflyman
 */
@Slf4j
@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

//    @Autowired
//    private ErrorLogService errorLogService;

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        log.info(String.format("[%s] ", ex.getResponseCode().getMessage()) + request.getMethod() + " " + request.getRequestURI());
        ResponseCode responseCode = ex.getResponseCode();
        return ResponseEntity.status(HttpStatus.valueOf(responseCode.getStatusCode())).body(new Response(ex));
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity handleBaseException(BaseException ex, HttpServletRequest request) {
        log.error(String.format("[%s] ", ex.getResponseCode().getMessage()) + request.getMethod() + " " + request.getRequestURI(), ex);
        ResponseCode responseCode = ex.getResponseCode();
        if(responseCode.isLogError()) {
//            errorLogService.create(request, ex);
        }
        return ResponseEntity.status(HttpStatus.valueOf(responseCode.getStatusCode())).body(new Response(ex));
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity handleDefaultException(Throwable ex, HttpServletRequest request) {
        log.error("[INTERNAL SERVER ERROR] " + request.getMethod() + " " + request.getRequestURI(), ex);
//        errorLogService.create(request, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(BaseResponseCode.INTERNAL_SERVER_ERROR, ex.getMessage()));
    }
}
