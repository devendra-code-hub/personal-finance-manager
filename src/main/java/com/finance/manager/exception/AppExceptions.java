package com.finance.manager.exception;

/**
 * Custom exceptions for clean, specific error messages.
 * Why custom exceptions instead of generic RuntimeException?
 * The GlobalExceptionHandler catches these by type and maps them to the correct
 * HTTP status code (404, 409, 403, etc.) as required by the assignment.
 */
public class AppExceptions {

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class DuplicateResourceException extends RuntimeException {
        public DuplicateResourceException(String message) {
            super(message);
        }
    }

    public static class UnauthorizedException extends RuntimeException {
        public UnauthorizedException(String message) {
            super(message);
        }
    }

    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) {
            super(message);
        }
    }

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }
}