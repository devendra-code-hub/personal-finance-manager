package com.finance.manager.exception;

import com.finance.manager.dto.response.ResponseDtos.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Central error handling for ALL controllers.
 * Why @RestControllerAdvice instead of try-catch in each controller?
 * - Single place to maintain all error logic
 * - Controllers stay clean (no error handling code)
 * - Consistent response format across all endpoints
 * - Assignment explicitly requires @ControllerAdvice
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handles validation errors from @Valid on request bodies
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(message, 400));
    }

    @ExceptionHandler(AppExceptions.ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(AppExceptions.ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), 404));
    }

    @ExceptionHandler(AppExceptions.DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(AppExceptions.DuplicateResourceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), 409));
    }

    @ExceptionHandler(AppExceptions.UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(AppExceptions.UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage(), 401));
    }

    @ExceptionHandler(AppExceptions.ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(AppExceptions.ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getMessage(), 403));
    }

    @ExceptionHandler(AppExceptions.BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(AppExceptions.BadRequestException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ex.getMessage(), 400));
    }

    // Catch-all — prevents 5xx errors for known runtime issues
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        // Log it in real apps, but don't expose internal details to client
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred", 500));
    }
}