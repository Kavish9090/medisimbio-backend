package com.medisimbio.backend.ai.service;

import com.medisimbio.backend.ai.dto.AiPrescriptionSafetyCheckRequestDto;
import com.medisimbio.backend.ai.dto.AiPrescriptionSafetyCheckResponseDto;
import com.medisimbio.backend.ai.dto.AiTriageRequestDto;
import com.medisimbio.backend.ai.dto.AiTriageResponseDto;

/**
 * Service contract interface for integrating MediSimbio with the external AI Inference Model service.
 * <p>
 * This interface bridges the core Spring Boot backend with the teammate's dedicated AI inference service.
 * Implementations may invoke an external HTTP microservice, Triton/vLLM server, or return structured fallback stubs.
 */
public interface AiIntegrationService {

    /**
     * Executes AI-assisted pre-consultation symptom triage and physician specialty recommendation.
     *
     * @param requestDto patient symptom intake details
     * @return structured triage level, recommended specialty, and clinical intake summary
     */
    AiTriageResponseDto performTriage(AiTriageRequestDto requestDto);

    /**
     * Performs automated clinical safety screening on candidate medications before prescription finalization.
     *
     * @param requestDto candidate drugs, concurrent medications, allergies, and diagnoses
     * @return drug-drug interaction warnings and contraindication alerts
     */
    AiPrescriptionSafetyCheckResponseDto checkPrescriptionSafety(AiPrescriptionSafetyCheckRequestDto requestDto);
}
