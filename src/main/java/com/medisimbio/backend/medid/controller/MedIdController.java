package com.medisimbio.backend.medid.controller;

import com.medisimbio.backend.common.response.ApiResponse;
import com.medisimbio.backend.medid.dto.MedIdVerificationResponseDto;
import com.medisimbio.backend.medid.service.MedIdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Med-ID Authority & Verification APIs.
 */
@RestController
@RequestMapping("/api/v1/med-id")
@Tag(name = "Med-ID Authority", description = "Endpoints for Med-ID verification and token validation")
public class MedIdController {

    private final MedIdService medIdService;

    public MedIdController(MedIdService medIdService) {
        this.medIdService = medIdService;
    }

    @GetMapping("/verify/{medId}")
    @Operation(
            summary = "Verify Med-ID Authenticity",
            description = "Validates a Med-ID (MD-XXXX-XXXX) for clinic check-ins, scans, and provider verification."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Med-ID is valid and active"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid Med-ID format"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Med-ID not found")
    })
    public ResponseEntity<ApiResponse<MedIdVerificationResponseDto>> verifyMedId(
            @Parameter(description = "Med-ID string (e.g. MD-S3AK-9Q8P)", example = "MD-S3AK-9Q8P")
            @PathVariable("medId") String medId) {
        MedIdVerificationResponseDto verification = medIdService.verifyMedId(medId);
        return ResponseEntity.ok(ApiResponse.success("Med-ID verification successful", verification));
    }
}
