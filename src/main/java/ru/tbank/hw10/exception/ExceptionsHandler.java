package ru.tbank.hw10.exception;

import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice(name = "homeworkTenExceptionsHandler", basePackages = "ru.tbank.hw10")
public class ExceptionsHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(new ErrorResponseMessage(
                        status.value(),
                        ExceptionUtils.getMessage(e).isEmpty()
                                ? "Были переданы некорректные параметры или необходимые параметры не были переданы вовсе."
                                : e.getMessage())
                );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException e) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(new ErrorResponseMessage(
                        status.value(),
                        ExceptionUtils.getMessage(e).isEmpty()
                                ? "Запрошенный ресурс не был найден."
                                : e.getMessage())
                );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<String> errors = new ArrayList<>();
        errors.add("Были переданные некорректные парамтеры запроса:");
        e.getAllErrors().forEach(err -> errors.add(err.getDefaultMessage()));
        String errorMessage = String.join("\n", errors);
        return ResponseEntity.status(status)
                .body(new ErrorResponseMessage(status.value(), errorMessage));
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<String> errors = new ArrayList<>();
        e.getConstraintViolations().forEach(violation -> errors.add(violation.getMessage()));
        String errorMessage = "Были переданные некорректные парамтеры запроса: " + String.join(", ", errors);
        return ResponseEntity.status(status)
                .body(new ErrorResponseMessage(
                        status.value(),
                        errorMessage)
                );
    }

    public record ErrorResponseMessage(Integer code, String message) {
    }
}
