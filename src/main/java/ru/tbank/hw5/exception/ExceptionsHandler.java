package ru.tbank.hw5.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "ru/tbank/hw5")
public class ExceptionsHandler {

    @ExceptionHandler(IntegrationException.class)
    public ResponseEntity<?> handleIntegrationException(IntegrationException e) {
        return ResponseEntity.internalServerError()
                .body(new ErrorResponseMessage(
                        ExceptionUtils.getMessage(e).isEmpty()
                                ? "В процессе обращения к внешней системе произошла ошибка."
                                : ExceptionUtils.getMessage(e),
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

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseMessage(
                        ExceptionUtils.getMessage(e).isEmpty()
                                ? "Были переданы некорректные параметры или необходимые параметры не были переданы вовсе."
                                : ExceptionUtils.getMessage(e),
                        ExceptionUtils.getStackTrace(e)));
    }

    private record ErrorResponseMessage(String message, String details) {
    }
}
