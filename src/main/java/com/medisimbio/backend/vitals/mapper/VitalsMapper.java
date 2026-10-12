package com.medisimbio.backend.vitals.mapper;

import com.medisimbio.backend.vitals.dto.VitalsResponseDto;
import com.medisimbio.backend.vitals.entity.VitalsEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting VitalsEntity to response DTOs.
 */
@Component
public class VitalsMapper {

    public VitalsResponseDto toResponseDto(VitalsEntity entity) {
        if (entity == null) {
            return null;
        }

        VitalsResponseDto dto = new VitalsResponseDto();
        dto.setId(entity.getId());

        if (entity.getEncounter() != null) {
            dto.setEncounterId(entity.getEncounter().getId());
            dto.setEncounterReference(entity.getEncounter().getEncounterReference());
        }

        if (entity.getPatient() != null) {
            dto.setPatientId(entity.getPatient().getId());
            dto.setPatientName(entity.getPatient().getFullName());
        }

        dto.setMeasuredAt(entity.getMeasuredAt());
        dto.setTemperature(entity.getTemperature());
        dto.setHeartRate(entity.getHeartRate());
        dto.setRespiratoryRate(entity.getRespiratoryRate());
        dto.setSystolicBloodPressure(entity.getSystolicBloodPressure());
        dto.setDiastolicBloodPressure(entity.getDiastolicBloodPressure());

        if (entity.getSystolicBloodPressure() != null && entity.getDiastolicBloodPressure() != null) {
            dto.setBloodPressureFormatted(entity.getSystolicBloodPressure() + "/" + entity.getDiastolicBloodPressure() + " mmHg");
        }

        dto.setOxygenSaturation(entity.getOxygenSaturation());
        dto.setWeight(entity.getWeight());
        dto.setHeight(entity.getHeight());
        dto.setBmi(entity.getBmi());
        dto.setBloodGlucose(entity.getBloodGlucose());
        dto.setNotes(entity.getNotes());
        dto.setRecordedBy(entity.getRecordedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }
}
