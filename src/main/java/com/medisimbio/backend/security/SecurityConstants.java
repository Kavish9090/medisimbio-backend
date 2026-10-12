package com.medisimbio.backend.security;

/**
 * Security Constants & Future Role Definitions.
 * 
 * NOTE: FOUNDATION ONLY / NOT PRODUCTION READY (Phase 1)
 */
public final class SecurityConstants {

    private SecurityConstants() {
        // Private constructor
    }

    // Public Unauthenticated Endpoints Whitelist
    public static final String[] PUBLIC_URL_PATTERNS = {
            "/api/v1/health/**",
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/v1/med-id/verify/**",
            "/favicon.ico"
    };

    // Planned Healthcare System Roles for Future Phases
    public static final String ROLE_PATIENT = "PATIENT";
    public static final String ROLE_DOCTOR = "DOCTOR";
    public static final String ROLE_HOSPITAL_ADMIN = "HOSPITAL_ADMIN";
    public static final String ROLE_LAB = "LAB";
    public static final String ROLE_PHARMACY = "PHARMACY";
    public static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";
}
