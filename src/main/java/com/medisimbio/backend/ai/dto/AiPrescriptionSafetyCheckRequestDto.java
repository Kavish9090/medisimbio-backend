package com.medisimbio.backend.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

/**
 * AI Clinical Prescription Safety Check Request Contract.
 * Validates candidate medications against active medical conditions and concurrent drugs.
 */
@Schema(description = "Prescription item safety validation payload")
public record AiPrescriptionSafetyCheckRequestDto(

        @Schema(description = "Patient UUID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID patientId,

        @NotEmpty(message = "At least one candidate medicine must be specified for safety verification")
        @Schema(description = "List of new medicine names to prescribe", example = "[\"Amoxicillin 500mg\", \"Clavulanate\"]")
        List<String> candidateMedicines,

        @Schema(description = "Active existing medications the patient is currently taking",
                example = "[\"Warfarin 5mg\"]")
        List<String> currentMedications,

        @Schema(description = "Known patient drug allergies", example = "[\"Penicillin\"]")
        List<String> knownAllergies,

        @Schema(description = "Patient diagnosed medical conditions", example = "[\"Chronic Kidney Disease Stage 3\"]")
        List<String> activeConditions
) {
}
