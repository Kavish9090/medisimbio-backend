package com.medisimbio.backend.patient.mapper;

import com.medisimbio.backend.patient.dto.CreatePatientRequestDto;
import com.medisimbio.backend.patient.dto.PatientResponseDto;
import com.medisimbio.backend.patient.dto.UpdatePatientRequestDto;
import com.medisimbio.backend.patient.entity.PatientEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper for transforming between Patient entities and DTOs.
 */
@Component
public class PatientMapper {

    public PatientEntity toEntity(CreatePatientRequestDto dto, String authUserId) {
        if (dto == null) {
            return null;
        }

        PatientEntity entity = new PatientEntity();
        entity.setId(UUID.randomUUID());
        entity.setAuthUserId(authUserId);
        entity.setFullName(dto.getFullName().trim());
        entity.setEmail(dto.getEmail().trim().toLowerCase());
        entity.setMobileNumber(dto.getMobileNumber().trim());
        entity.setDateOfBirth(dto.getDateOfBirth());
        entity.setGender(dto.getGender());
        entity.setBloodGroup(dto.getBloodGroup());
        entity.setAllergies(dto.getAllergies());
        entity.setExistingConditions(dto.getExistingConditions());
        entity.setEmergencyContactName(dto.getEmergencyContactName());
        entity.setEmergencyContactRelationship(dto.getEmergencyContactRelationship());
        entity.setEmergencyContactMobile(dto.getEmergencyContactMobile());
        entity.setPhotoUrl(dto.getPhotoUrl());
        entity.setProfileCompleted(false);

        return entity;
    }

    public PatientResponseDto toResponseDto(PatientEntity entity) {
        if (entity == null) {
            return null;
        }

        PatientResponseDto dto = new PatientResponseDto();
        dto.setId(entity.getId());
        dto.setAuthUserId(entity.getAuthUserId());
        dto.setFullName(entity.getFullName());
        dto.setEmail(entity.getEmail());
        dto.setMobileNumber(entity.getMobileNumber());
        dto.setDateOfBirth(entity.getDateOfBirth());
        dto.setGender(entity.getGender());
        dto.setBloodGroup(entity.getBloodGroup());
        dto.setAllergies(entity.getAllergies());
        dto.setExistingConditions(entity.getExistingConditions());
        dto.setEmergencyContactName(entity.getEmergencyContactName());
        dto.setEmergencyContactRelationship(entity.getEmergencyContactRelationship());
        dto.setEmergencyContactMobile(entity.getEmergencyContactMobile());
        dto.setPhotoUrl(entity.getPhotoUrl());
        dto.setProfileCompleted(entity.isProfileCompleted());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getMedId() != null) {
            dto.setMedId(entity.getMedId().getMedIdNumber());
        }

        return dto;
    }

    public void updateEntityFromDto(UpdatePatientRequestDto dto, PatientEntity entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            entity.setFullName(dto.getFullName().trim());
        }
        if (dto.getDateOfBirth() != null) {
            entity.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getGender() != null) {
            entity.setGender(dto.getGender());
        }
        if (dto.getBloodGroup() != null) {
            entity.setBloodGroup(dto.getBloodGroup().trim());
        }
        if (dto.getAllergies() != null) {
            entity.setAllergies(dto.getAllergies().trim());
        }
        if (dto.getExistingConditions() != null) {
            entity.setExistingConditions(dto.getExistingConditions().trim());
        }
        if (dto.getEmergencyContactName() != null) {
            entity.setEmergencyContactName(dto.getEmergencyContactName().trim());
        }
        if (dto.getEmergencyContactRelationship() != null) {
            entity.setEmergencyContactRelationship(dto.getEmergencyContactRelationship().trim());
        }
        if (dto.getEmergencyContactMobile() != null) {
            entity.setEmergencyContactMobile(dto.getEmergencyContactMobile().trim());
        }
        if (dto.getPhotoUrl() != null) {
            entity.setPhotoUrl(dto.getPhotoUrl().trim());
        }
        if (dto.getProfileCompleted() != null) {
            entity.setProfileCompleted(dto.getProfileCompleted());
        }
    }
}
