package com.medisimbio.backend.diagnosis.mapper;

import com.medisimbio.backend.diagnosis.dto.DiagnosisResponseDto;
import com.medisimbio.backend.diagnosis.entity.DiagnosisEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting DiagnosisEntity to response DTOs.
 */
@Component
public class DiagnosisMapper {

    public DiagnosisResponseDto toResponseDto(DiagnosisEntity entity) {
        if (entity == null) {
            return null;
        }

        DiagnosisResponseDto dto = new DiagnosisResponseDto();
        dto.setId(entity.getId());

        if (entity.getEncounter() != null) {
            dto.setEncounterId(entity.getEncounter().getId());
            dto.setEncounterReference(entity.getEncounter().getEncounterReference());
        }

        if (entity.getPatient() != null) {
            dto.setPatientId(entity.getPatient().getId());
            dto.setPatientName(entity.getPatient().getFullName());
        }

        dto.setDiagnosisName(entity.getDiagnosisName());
        dto.setDiagnosisCode(entity.getDiagnosisCode());
        dto.setDiagnosisDescription(entity.getDiagnosisDescription());
        dto.setDiagnosisType(entity.getDiagnosisType());
        dto.setStatus(entity.getStatus());
        dto.setDiagnosedAt(entity.getDiagnosedAt());
        dto.setNotes(entity.getNotes());
        dto.setRecordedBy(entity.getRecordedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}
