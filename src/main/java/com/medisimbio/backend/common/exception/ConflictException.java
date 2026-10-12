package com.medisimbio.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when an operation causes a state or resource conflict (HTTP 409 Conflict).
 */
public class ConflictException extends BaseException {

    public ConflictException(String message) {
        super(message, HttpStatus.CONFLICT, "CONFLICT");
    }

    public ConflictException(String message, String errorCode) {
        super(message, HttpStatus.CONFLICT, errorCode != null ? errorCode : "CONFLICT");
    }
}
