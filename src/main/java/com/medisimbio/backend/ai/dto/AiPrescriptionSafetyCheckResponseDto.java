package com.medisimbio.backend.ai.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * AI Clinical Prescription Safety Check Response Contract.
 * Alerts doctor if candidate medicines introduce high-risk drug-drug interactions or allergies.
 */
@Schema(description = "AI Safety analysis result for candidate prescription")
public record AiPrescriptionSafetyCheckResponseDto(

        @Schema(description = "Overall safety determination: SAFE, CAUTION, or CONTRAINDICATED", example = "CONTRAINDICATED")
        String safetyLevel,

        @Schema(description = "Drug-allergy conflicts detected", example = "[\"Cross-hypersensitivity: Patient allergic to Penicillin\"]")
        List<String> allergyAlerts,

        @Schema(description = "Drug-drug interaction warnings",
                example = "[\"Potential bleeding risk increase when co-administered with Warfarin\"]")
        List<String> interactionWarnings,

        @Schema(description = "Dosage or organ clearance adjustments recommended",
                example = "[\"Renal adjustment needed: GFR < 45 ml/min\"]")
        List<String> dosageRecommendations,

        @Schema(description = "AI clinical rationale summary")
        String summary
) {
}
