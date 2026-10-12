package com.medisimbio.backend.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Check REST Controller.
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health Check", description = "System and service liveness & readiness check endpoints")
public class HealthController {

    @Value("${spring.application.name:medisimbio-backend}")
    private String serviceName;

    @Value("${app.api.version:v1}")
    private String apiVersion;

    @GetMapping
    @Operation(
            summary = "Get Service Health Status",
            description = "Returns current backend liveness status, service metadata, and server timestamp.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Service is healthy and operational",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = HealthResponseDto.class))
                    )
            }
    )
    public ResponseEntity<HealthResponseDto> getHealth() {
        HealthResponseDto response = HealthResponseDto.up(serviceName, apiVersion);
        return ResponseEntity.ok(response);
    }
}
