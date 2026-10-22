package com.medisimbio.backend.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * AI Pre-Consultation Triage Request Contract.
 * Used by Mobile/Web clients to submit preliminary symptoms to the AI inference service.
 */
@Schema(description = "Symptom intake payload for AI triage and specialty routing")
public record AiTriageRequestDto(

        @Schema(description = "Patient age in years", example = "35")
        Integer patientAge,

        @Schema(description = "Patient biological sex/gender", example = "MALE")
        String gender,

        @NotEmpty(message = "At least one symptom must be provided")
        @Schema(description = "Reported symptom list", example = "[\"Fever\", \"Dry cough\", \"Chest tightness\"]")
        List<String> symptoms,

        @NotBlank(message = "Detailed symptom description is required")
        @Schema(description = "Freeform description of symptoms", example = "Persistent dry cough for 4 days with mild shortness of breath.")
        String description,

        @Schema(description = "Duration of symptoms", example = "4 days")
        String duration,

        @Schema(description = "Self-reported pain/severity scale (1-10)", example = "6")
        Integer severityScale
) {
}
