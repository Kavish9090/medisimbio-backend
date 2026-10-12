package com.medisimbio.backend.patient.entity;

/**
 * Gender enumeration for Patient profiles.
 */
public enum Gender {
    MALE,
    FEMALE,
    OTHER,
    UNSPECIFIED;

    public static Gender fromString(String value) {
        if (value == null || value.isBlank()) {
            return UNSPECIFIED;
        }
        try {
            return Gender.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNSPECIFIED;
        }
    }
}
