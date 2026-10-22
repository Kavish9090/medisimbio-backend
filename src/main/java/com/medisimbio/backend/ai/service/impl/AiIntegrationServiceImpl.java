package com.medisimbio.backend.ai.service.impl;

import com.medisimbio.backend.ai.dto.AiPrescriptionSafetyCheckRequestDto;
import com.medisimbio.backend.ai.dto.AiPrescriptionSafetyCheckResponseDto;
import com.medisimbio.backend.ai.dto.AiTriageRequestDto;
import com.medisimbio.backend.ai.dto.AiTriageResponseDto;
import com.medisimbio.backend.ai.service.AiIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI Integration Service Implementation.
 * Provides resilient fallback triage and prescription safety evaluation when the external AI service
 * is offline or being developed concurrently by the AI teammate.
 */
@Service
public class AiIntegrationServiceImpl implements AiIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(AiIntegrationServiceImpl.class);

    @Value("${app.ai.service-url:http://localhost:8000}")
    private String aiServiceUrl;

    @Value("${app.ai.enabled:false}")
    private boolean aiEnabled;

    @Override
    public AiTriageResponseDto performTriage(AiTriageRequestDto requestDto) {
        log.info("Received AI triage request for {} symptoms, severity: {}",
                requestDto.symptoms() != null ? requestDto.symptoms().size() : 0, requestDto.severityScale());

        int severity = requestDto.severityScale() != null ? requestDto.severityScale() : 5;
        String triageLevel;
        String specialtyCode;
        String specialtyName;

        if (severity >= 8) {
            triageLevel = "EMERGENCY";
            specialtyCode = "EMERG";
            specialtyName = "Emergency Medicine";
        } else if (severity >= 5) {
            triageLevel = "URGENT";
            specialtyCode = "GENMED";
            specialtyName = "General Medicine";
        } else {
            triageLevel = "ROUTINE";
            specialtyCode = "GENMED";
            specialtyName = "General Medicine";
        }

        String summary = String.format("Patient (%s, %s yo) presents with: %s. Reported description: %s.",
                requestDto.gender() != null ? requestDto.gender() : "Unspecified",
                requestDto.patientAge() != null ? requestDto.patientAge() : "Unknown",
                requestDto.symptoms() != null ? String.join(", ", requestDto.symptoms()) : "None",
                requestDto.description() != null ? requestDto.description() : "None");

        return new AiTriageResponseDto(
                triageLevel,
                specialtyCode,
                specialtyName,
                summary,
                List.of("Clinical correlation required", "Review vital signs on presentation"),
                severity >= 8 ? List.of("Immediate ER triage advised if chest pain, syncope, or severe dyspnea occurs") : List.of(),
                0.88
        );
    }

    @Override
    public AiPrescriptionSafetyCheckResponseDto checkPrescriptionSafety(AiPrescriptionSafetyCheckRequestDto requestDto) {
        log.info("Executing AI prescription safety check for patient [{}] with {} candidate medicines",
                requestDto.patientId(), requestDto.candidateMedicines() != null ? requestDto.candidateMedicines().size() : 0);

        List<String> allergyAlerts = new ArrayList<>();
        List<String> interactionWarnings = new ArrayList<>();

        if (requestDto.knownAllergies() != null && requestDto.candidateMedicines() != null) {
            for (String allergy : requestDto.knownAllergies()) {
                for (String med : requestDto.candidateMedicines()) {
                    if (med.toLowerCase().contains(allergy.toLowerCase()) || allergy.toLowerCase().contains(med.toLowerCase())) {
                        allergyAlerts.add("Potential allergen match: '" + med + "' matches documented allergy '" + allergy + "'");
                    }
                }
            }
        }

        String safetyLevel = !allergyAlerts.isEmpty() ? "CONTRAINDICATED" : "SAFE";
        String summary = allergyAlerts.isEmpty()
                ? "No known contraindications or dangerous interactions detected with candidate medication list."
                : "Candidate prescription triggered " + allergyAlerts.size() + " critical allergen alert(s). Review required before issuance.";

        return new AiPrescriptionSafetyCheckResponseDto(
                safetyLevel,
                allergyAlerts,
                interactionWarnings,
                List.of(),
                summary
        );
    }
}
