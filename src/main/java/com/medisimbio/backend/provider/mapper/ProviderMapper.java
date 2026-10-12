package com.medisimbio.backend.provider.mapper;

import com.medisimbio.backend.hospital.mapper.HospitalMapper;
import com.medisimbio.backend.provider.dto.*;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.provider.entity.ProviderHospitalEntity;
import com.medisimbio.backend.specialty.dto.SpecialtyResponseDto;
import com.medisimbio.backend.specialty.mapper.SpecialtyMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper for Provider entities and DTOs.
 */
@Component
public class ProviderMapper {

    private final SpecialtyMapper specialtyMapper;
    private final HospitalMapper hospitalMapper;

    public ProviderMapper(SpecialtyMapper specialtyMapper, HospitalMapper hospitalMapper) {
        this.specialtyMapper = specialtyMapper;
        this.hospitalMapper = hospitalMapper;
    }

    public ProviderEntity toEntity(CreateProviderRequestDto dto) {
        if (dto == null) {
            return null;
        }
        ProviderEntity entity = new ProviderEntity();
        entity.setId(UUID.randomUUID());
        entity.setFullName(dto.getFullName().trim());
        entity.setProfessionalRegistrationNumber(dto.getProfessionalRegistrationNumber().trim().toUpperCase());
        entity.setQualification(dto.getQualification().trim());
        entity.setExperienceYears(dto.getExperienceYears() != null ? dto.getExperienceYears() : 0);
        entity.setProfilePhotoUrl(dto.getProfilePhotoUrl() != null ? dto.getProfilePhotoUrl().trim() : null);
        entity.setEmail(dto.getEmail().trim().toLowerCase());
        entity.setMobileNumber(dto.getMobileNumber().trim());
        entity.setVerificationStatus("VERIFIED");
        entity.setActive(true);
        if (dto.getAuthUserId() != null && !dto.getAuthUserId().isBlank()) {
            entity.setAuthUserId(dto.getAuthUserId().trim());
        }
        return entity;
    }

    public void updateEntityFromDto(UpdateProviderRequestDto dto, ProviderEntity entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            entity.setFullName(dto.getFullName().trim());
        }
        if (dto.getQualification() != null && !dto.getQualification().isBlank()) {
            entity.setQualification(dto.getQualification().trim());
        }
        if (dto.getExperienceYears() != null) {
            entity.setExperienceYears(dto.getExperienceYears());
        }
        if (dto.getProfilePhotoUrl() != null) {
            entity.setProfilePhotoUrl(dto.getProfilePhotoUrl().isBlank() ? null : dto.getProfilePhotoUrl().trim());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            entity.setEmail(dto.getEmail().trim().toLowerCase());
        }
        if (dto.getMobileNumber() != null && !dto.getMobileNumber().isBlank()) {
            entity.setMobileNumber(dto.getMobileNumber().trim());
        }
        if (dto.getVerificationStatus() != null && !dto.getVerificationStatus().isBlank()) {
            entity.setVerificationStatus(dto.getVerificationStatus().trim());
        }
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
        if (dto.getAuthUserId() != null) {
            entity.setAuthUserId(dto.getAuthUserId().isBlank() ? null : dto.getAuthUserId().trim());
        }
    }

    public ProviderResponseDto toResponseDto(ProviderEntity entity) {
        if (entity == null) {
            return null;
        }

        List<SpecialtyResponseDto> specialties = entity.getSpecialties() != null
                ? entity.getSpecialties().stream()
                .map(specialtyMapper::toResponseDto)
                .collect(Collectors.toList())
                : new ArrayList<>();

        List<ProviderHospitalResponseDto> hospitalAffiliations = entity.getHospitalAffiliations() != null
                ? entity.getHospitalAffiliations().stream()
                .map(this::toAffiliationDto)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new ProviderResponseDto(
                entity.getId(),
                entity.getFullName(),
                entity.getProfessionalRegistrationNumber(),
                entity.getQualification(),
                entity.getExperienceYears(),
                entity.getProfilePhotoUrl(),
                entity.getEmail(),
                entity.getMobileNumber(),
                entity.getVerificationStatus(),
                entity.isActive(),
                specialties,
                hospitalAffiliations,
                entity.getAuthUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public ProviderSummaryDto toSummaryDto(ProviderEntity entity) {
        if (entity == null) {
            return null;
        }

        List<SpecialtyResponseDto> specialties = entity.getSpecialties() != null
                ? entity.getSpecialties().stream()
                .map(specialtyMapper::toResponseDto)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new ProviderSummaryDto(
                entity.getId(),
                entity.getFullName(),
                entity.getProfessionalRegistrationNumber(),
                entity.getQualification(),
                entity.getExperienceYears(),
                entity.getProfilePhotoUrl(),
                entity.getVerificationStatus(),
                entity.isActive(),
                specialties
        );
    }

    public ProviderHospitalResponseDto toAffiliationDto(ProviderHospitalEntity entity) {
        if (entity == null) {
            return null;
        }

        return new ProviderHospitalResponseDto(
                entity.getId(),
                entity.getProvider() != null ? entity.getProvider().getId() : null,
                entity.getProvider() != null ? entity.getProvider().getFullName() : null,
                entity.getHospital() != null ? hospitalMapper.toSummaryDto(entity.getHospital()) : null,
                entity.getDepartment(),
                entity.getConsultationFee(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
