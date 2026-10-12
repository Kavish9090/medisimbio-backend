package com.medisimbio.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown for malformed or invalid client requests (HTTP 400).
 */
public class BadRequestException extends BaseException {

    public BadRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "BAD_REQUEST");
    }

    public BadRequestException(String message, String errorCode) {
        super(message, HttpStatus.BAD_REQUEST, errorCode);
    }
}
