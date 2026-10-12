package com.medisimbio.backend.encounter.mapper;

import com.medisimbio.backend.encounter.dto.EncounterResponseDto;
import com.medisimbio.backend.encounter.dto.EncounterSummaryDto;
import com.medisimbio.backend.encounter.entity.EncounterEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between EncounterEntity and DTO representations.
 */
@Component
public class EncounterMapper {

    public EncounterResponseDto toResponseDto(EncounterEntity entity) {
        if (entity == null) {
            return null;
        }

        EncounterResponseDto dto = new EncounterResponseDto();
        dto.setId(entity.getId());
        dto.setEncounterReference(entity.getEncounterReference());

        if (entity.getPatient() != null) {
            dto.setPatientId(entity.getPatient().getId());
            dto.setPatientName(entity.getPatient().getFullName());
            if (entity.getPatient().getMedId() != null) {
                dto.setPatientMedId(entity.getPatient().getMedId().getMedIdNumber());
            }
        }

        if (entity.getProvider() != null) {
            dto.setProviderId(entity.getProvider().getId());
            dto.setProviderName(entity.getProvider().getFullName());
        }

        if (entity.getHospital() != null) {
            dto.setHospitalId(entity.getHospital().getId());
            dto.setHospitalName(entity.getHospital().getName());
        }

        if (entity.getAppointment() != null) {
            dto.setAppointmentId(entity.getAppointment().getId());
            dto.setBookingReference(entity.getAppointment().getBookingReference());
        }

        dto.setEncounterType(entity.getEncounterType());
        dto.setStatus(entity.getStatus());
        dto.setStartedAt(entity.getStartedAt());
        dto.setEndedAt(entity.getEndedAt());
        dto.setChiefComplaint(entity.getChiefComplaint());
        dto.setClinicalNotes(entity.getClinicalNotes());
        dto.setDiagnosisSummary(entity.getDiagnosisSummary());
        dto.setTreatmentPlan(entity.getTreatmentPlan());
        dto.setFollowUpInstructions(entity.getFollowUpInstructions());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public EncounterSummaryDto toSummaryDto(EncounterEntity entity) {
        if (entity == null) {
            return null;
        }

        EncounterSummaryDto dto = new EncounterSummaryDto();
        dto.setId(entity.getId());
        dto.setEncounterReference(entity.getEncounterReference());

        if (entity.getPatient() != null) {
            dto.setPatientId(entity.getPatient().getId());
            dto.setPatientName(entity.getPatient().getFullName());
        }

        if (entity.getProvider() != null) {
            dto.setProviderId(entity.getProvider().getId());
            dto.setProviderName(entity.getProvider().getFullName());
        }

        if (entity.getHospital() != null) {
            dto.setHospitalId(entity.getHospital().getId());
            dto.setHospitalName(entity.getHospital().getName());
        }

        dto.setEncounterType(entity.getEncounterType());
        dto.setStatus(entity.getStatus());
        dto.setStartedAt(entity.getStartedAt());
        dto.setEndedAt(entity.getEndedAt());
        dto.setChiefComplaint(entity.getChiefComplaint());
        dto.setCreatedAt(entity.getCreatedAt());

        return dto;
    }
}
