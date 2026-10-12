package com.medisimbio.backend.diagnosis.service;

import com.medisimbio.backend.diagnosis.dto.CreateDiagnosisRequestDto;
import com.medisimbio.backend.diagnosis.dto.DiagnosisResponseDto;
import com.medisimbio.backend.diagnosis.dto.UpdateDiagnosisRequestDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for clinical diagnosis management.
 */
public interface DiagnosisService {

    DiagnosisResponseDto createDiagnosis(UUID encounterId, CreateDiagnosisRequestDto requestDto);

    List<DiagnosisResponseDto> getDiagnosesByEncounterId(UUID encounterId);

    DiagnosisResponseDto updateDiagnosis(UUID diagnosisId, UpdateDiagnosisRequestDto requestDto);
}
