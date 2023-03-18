package ir.farbod.producer.exception.handler;

import ir.farbod.producer.exception.RequestException;
import ir.farbod.producer.exception.model.ExceptionModel;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
@Log4j2
public class ExceptionHandler extends ResponseEntityExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(value = {RequestException.class})
    public ResponseEntity<Object> handleRequestException(RuntimeException ex) {
        HttpStatus status = HttpStatus.NOT_ACCEPTABLE;
        ExceptionModel exception = new ExceptionModel(ex.getMessage(), LocalDateTime.now(), status);
        log.log(Level.ERROR, ex);

        return ResponseEntity.status(status)
                .body(exception);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ExceptionModel(ex.getMessage(), LocalDateTime.now(), HttpStatus.METHOD_NOT_ALLOWED));
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        HttpStatus stat = HttpStatus.INTERNAL_SERVER_ERROR;
        ExceptionModel exception = new ExceptionModel(ex.getMessage(), LocalDateTime.now(), stat);
        return ResponseEntity.status(stat)
                .body(exception);
    }

}
