package guldilin.controller;

import guldilin.dto.ErrorDTO;
import guldilin.dto.ValidationErrorDTO;
import guldilin.errors.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ControllerAdvice
public class ErrorController {

    private final Map<String, ErrorCode> errorsMap;
    private final Map<String, HttpStatus> statusesMap;

    public ErrorController() {
        this.errorsMap = new HashMap<>();
        this.statusesMap = new HashMap<>();
        this.errorsMap.put(NumberFormatException.class.getName(), ErrorCode.INCORRECT_NUMBER_FORMAT);
        this.errorsMap.put(HttpMessageNotReadableException.class.getName(), ErrorCode.INCORRECT_DATA_FORMAT);

        this.statusesMap.put(NumberFormatException.class.getName(), HttpStatus.BAD_REQUEST);
        this.statusesMap.put(HttpMessageNotReadableException.class.getName(), HttpStatus.BAD_REQUEST);
    }

    protected Object handleConstraintViolationException(Throwable throwable) {
        ConstraintViolationException validationError = (ConstraintViolationException) throwable;
        Map<String, String> validationErrors = new HashMap<>();
        validationError.getConstraintViolations().forEach(
                c -> validationErrors.put(c.getPropertyPath().toString(), c.getMessage()));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ValidationErrorDTO.builder()
                                .error(ErrorCode.VALIDATION_ERROR.name())
                                .message(validationErrors)
                                .build()
                );
    }

    protected Object handleDefaultError(Throwable throwable, String errorName) {
        HttpStatus statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

        if (this.statusesMap.containsKey(errorName)) statusCode = this.statusesMap.get(errorName);
        if (this.errorsMap.containsKey(errorName)) errorCode = this.errorsMap.get(errorName);
        return ResponseEntity
                .status(statusCode)
                .body(
                        ErrorDTO.builder()
                                .error(errorCode.name())
                                .message(throwable.getMessage())
                                .build()
                );
    }

    protected Object handleCauseException(Throwable throwable)
            throws IOException {
        Exception causedException = (Exception) throwable;
        if (causedException.getCause() != null) return handleException(causedException.getCause());
        else return handleDefaultError(throwable, throwable.getClass().getName());
    }

    protected Object handleJsonException(Throwable throwable, Boolean isCaused) {
        isCaused = Optional.ofNullable(isCaused).orElse(false);
        ErrorCode code = ErrorCode.JSON_SYNTAX_ERROR;
        String message = throwable.getMessage();
        if (isCaused) {
            String cause = throwable.getCause().getClass().getName();
            if (this.errorsMap.containsKey(cause)) {
                code = errorsMap.get(cause);
                message = throwable.getCause().getMessage();
            }
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorDTO.builder()
                                .error(code.name())
                                .message(message)
                                .build()
                );
    }

    protected Object handleDataFormatException(Throwable throwable) {
        ErrorCode code = ErrorCode.INCORRECT_DATA_FORMAT;
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ErrorDTO.builder()
                                .error(code.name())
                                .message(throwable.getMessage())
                                .build()
                );
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    protected Object handleException(Throwable throwable) throws IOException {
        throwable.printStackTrace();
        String errorName = throwable.getClass().getName();
        switch (errorName) {
            case "javax.validation.ConstraintViolationException":
                return handleConstraintViolationException(throwable);
            case "java.lang.IllegalArgumentException":
            case "org.springframework.http.converter.HttpMessageNotReadableException":
                return handleCauseException(throwable);
            case "com.fasterxml.jackson.core.io.JsonEOFException":
            case "com.fasterxml.jackson.core.JsonParseException":
                return handleJsonException(throwable, false);
            case "com.google.gson.JsonSyntaxException":
                return handleJsonException(throwable, true);
            case "com.fasterxml.jackson.databind.exc.InvalidFormatException":
            case "org.springframework.web.bind.MethodArgumentNotValidException":
                return handleDataFormatException(throwable);
            default:
                return handleDefaultError(throwable, errorName);
        }
    }
}
