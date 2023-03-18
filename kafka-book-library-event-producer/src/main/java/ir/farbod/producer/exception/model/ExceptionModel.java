package ir.farbod.producer.exception.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class ExceptionModel {

    private String message;
    private LocalDateTime timestamp;
    private HttpStatus status;
}
