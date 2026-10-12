package com.medisimbio.backend.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Generic domain business rule violation exception (HTTP 422 or 400).
 */
public class BusinessException extends BaseException {

    public BusinessException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION");
    }

    public BusinessException(String message, String errorCode) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, errorCode);
    }

    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message, status, errorCode);
    }
}
