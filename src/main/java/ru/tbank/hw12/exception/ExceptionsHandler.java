package ru.tbank.hw12.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice(name = "homeworkTwelveExceptionsHandler", basePackages = "ru.tbank.hw12")
public class ExceptionsHandler {


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(new ErrorResponseMessage(
                        ExceptionUtils.getMessage(e).isEmpty()
                                ? "Были переданы некорректные параметры или необходимые параметры не были переданы вовсе."
                                : e.getMessage(),
                        ExceptionUtils.getStackTrace(e)
                ));
    }

    @ExceptionHandler(NotAuthorizedAccessException.class)
    public ResponseEntity<?> handleNotAuthorizedAccessException(NotAuthorizedAccessException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseMessage(
                        ExceptionUtils.getMessage(e).isEmpty()
                                ? "У вас нет доступа к данному ресурсу, так как вы не авторизованы."
                                : e.getMessage(),
                        ExceptionUtils.getStackTrace(e)));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity.internalServerError()
                .body(new ErrorResponseMessage(
                        ExceptionUtils.getMessage(e).isEmpty()
                                ? "В процессе обработки данных было получено недопустимое состояние."
                                : ExceptionUtils.getMessage(e),
                        ExceptionUtils.getStackTrace(e)));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponseMessage(
                        ExceptionUtils.getMessage(e).isEmpty()
                                ? "Были переданы некорректные параметры."
                                : ExceptionUtils.getMessage(e),
                        ExceptionUtils.getStackTrace(e)));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFoundException(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseMessage(
                        ExceptionUtils.getMessage(e).isEmpty()
                                ? "Запрошенный ресурс не был найден."
                                : ExceptionUtils.getMessage(e),
                        ExceptionUtils.getStackTrace(e)));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentialsException(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseMessage(
                        "Логин и/или пароль были введены неверно!",
                        ExceptionUtils.getMessage(e)));
    }

    public record ErrorResponseMessage(String message, String details) {
    }
}
