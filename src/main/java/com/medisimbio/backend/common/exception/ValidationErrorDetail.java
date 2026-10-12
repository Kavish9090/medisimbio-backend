package com.medisimbio.backend.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Detailed information for a single field validation error.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationErrorDetail {

    private final String field;
    private final String message;
    private final Object rejectedValue;

    public ValidationErrorDetail(String field, String message, Object rejectedValue) {
        this.field = field;
        this.message = message;
        this.rejectedValue = rejectedValue;
    }

    public ValidationErrorDetail(String field, String message) {
        this(field, message, null);
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    public Object getRejectedValue() {
        return rejectedValue;
    }
}
