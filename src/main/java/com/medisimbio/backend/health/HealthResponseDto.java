package com.medisimbio.backend.health;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

/**
 * DTO for GET /api/v1/health response.
 */
public class HealthResponseDto {

    private final String service;
    private final HealthStatus status;
    private final String version;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant timestamp;

    public HealthResponseDto(String service, HealthStatus status, String version, Instant timestamp) {
        this.service = service;
        this.status = status;
        this.version = version;
        this.timestamp = timestamp;
    }

    public static HealthResponseDto up(String service, String version) {
        return new HealthResponseDto(service, HealthStatus.UP, version, Instant.now());
    }

    public String getService() {
        return service;
    }

    public HealthStatus getStatus() {
        return status;
    }

    public String getVersion() {
        return version;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
