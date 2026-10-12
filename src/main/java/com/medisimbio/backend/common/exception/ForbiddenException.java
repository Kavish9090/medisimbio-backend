package com.medisimbio.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an authenticated user does not have permission for the requested resource (HTTP 403).
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    public ForbiddenException(String message, String errorCode) {
        super(message, HttpStatus.FORBIDDEN, errorCode);
    }
}
