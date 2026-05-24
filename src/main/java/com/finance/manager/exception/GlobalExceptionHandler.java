package com.finance.manager.exception;

import com.finance.manager.dto.response.ResponseDtos.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(message, 400));
    }

    // Handles invalid JSON values like bad date format or invalid enum (e.g. type="INVALID")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(HttpMessageNotReadableException ex) {
        String message = "Invalid request format or field value";
        String cause = ex.getMessage();
        if (cause != null && cause.contains("TransactionType")) {
            message = "Invalid type: must be INCOME or EXPENSE";
        } else if (cause != null && cause.contains("LocalDate")) {
            message = "Invalid date format: use YYYY-MM-DD";
        }
        return ResponseEntity.badRequest().body(new ErrorResponse(message, 400));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("Invalid parameter: " + ex.getName(), 400));
    }

    @ExceptionHandler(AppExceptions.ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(AppExceptions.ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(AppExceptions.DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(AppExceptions.DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(AppExceptions.UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AppExceptions.UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ex.getMessage(), 401));
    }

    @ExceptionHandler(AppExceptions.ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AppExceptions.ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage(), 403));
    }

    @ExceptionHandler(AppExceptions.BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(AppExceptions.BadRequestException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage(), 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred", 500));
    }
}