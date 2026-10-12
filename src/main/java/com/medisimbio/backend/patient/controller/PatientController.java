package com.medisimbio.backend.patient.controller;

import com.medisimbio.backend.common.response.ApiResponse;
import com.medisimbio.backend.medid.dto.MedIdResponseDto;
import com.medisimbio.backend.patient.dto.CreatePatientRequestDto;
import com.medisimbio.backend.patient.dto.PatientResponseDto;
import com.medisimbio.backend.patient.dto.UpdatePatientRequestDto;
import com.medisimbio.backend.patient.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Patient Management & Self-Service Profile APIs.
 */
@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "Patient Management", description = "Endpoints for managing patient identity, demographics, and Med-ID")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @Operation(
            summary = "Create Patient Profile",
            description = "Creates a new authoritative patient profile for the currently authenticated user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Patient profile created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error in request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Profile or identity conflict (email/mobile/auth user already exists)")
    })
    public ResponseEntity<ApiResponse<PatientResponseDto>> createPatientProfile(
            @Valid @RequestBody CreatePatientRequestDto requestDto) {
        PatientResponseDto created = patientService.createPatientProfile(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Patient profile created successfully", created));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get Current Patient Profile",
            description = "Retrieves the authoritative patient profile and assigned Med-ID for the currently authenticated user."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient profile not found for authenticated user")
    })
    public ResponseEntity<ApiResponse<PatientResponseDto>> getCurrentPatientProfile() {
        PatientResponseDto profile = patientService.getCurrentPatientProfile();
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PatchMapping("/me")
    @Operation(
            summary = "Update Current Patient Profile",
            description = "Partially updates profile demographics, emergency contact, or completion status for current patient."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error in request payload"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient profile not found")
    })
    public ResponseEntity<ApiResponse<PatientResponseDto>> updateCurrentPatientProfile(
            @Valid @RequestBody UpdatePatientRequestDto requestDto) {
        PatientResponseDto updated = patientService.updateCurrentPatientProfile(requestDto);
        return ResponseEntity.ok(ApiResponse.success("Patient profile updated successfully", updated));
    }

    @PostMapping("/me/med-id")
    @Operation(
            summary = "Generate or Retrieve Med-ID",
            description = "Authoritatively issues a unique MediSimbio Health ID (MD-XXXX-XXXX) and QR token for current patient."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Med-ID retrieved or generated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Patient profile not found")
    })
    public ResponseEntity<ApiResponse<MedIdResponseDto>> generateOrGetMedId() {
        MedIdResponseDto medId = patientService.generateOrGetMedIdForCurrentPatient();
        return ResponseEntity.ok(ApiResponse.success("Med-ID processed successfully", medId));
    }

    @GetMapping("/me/med-id")
    @Operation(
            summary = "Get Current Patient's Med-ID",
            description = "Retrieves the Med-ID and QR code verification token for current patient."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Med-ID retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthenticated request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Med-ID or profile not found")
    })
    public ResponseEntity<ApiResponse<MedIdResponseDto>> getMedId() {
        MedIdResponseDto medId = patientService.getMedIdForCurrentPatient();
        return ResponseEntity.ok(ApiResponse.success(medId));
    }
}
