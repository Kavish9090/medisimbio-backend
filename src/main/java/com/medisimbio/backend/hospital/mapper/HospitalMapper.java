package com.medisimbio.backend.hospital.mapper;

import com.medisimbio.backend.hospital.dto.CreateHospitalRequestDto;
import com.medisimbio.backend.hospital.dto.HospitalResponseDto;
import com.medisimbio.backend.hospital.dto.HospitalSummaryDto;
import com.medisimbio.backend.hospital.dto.UpdateHospitalRequestDto;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper between HospitalEntity and Hospital DTOs.
 */
@Component
public class HospitalMapper {

    public HospitalEntity toEntity(CreateHospitalRequestDto dto) {
        if (dto == null) {
            return null;
        }
        HospitalEntity entity = new HospitalEntity();
        entity.setId(UUID.randomUUID());
        entity.setName(dto.getName().trim());
        entity.setHospitalType(dto.getHospitalType());
        entity.setRegistrationIdentifier(dto.getRegistrationIdentifier() != null ? dto.getRegistrationIdentifier().trim() : null);
        entity.setPhone(dto.getPhone().trim());
        entity.setEmail(dto.getEmail() != null ? dto.getEmail().trim().toLowerCase() : null);
        entity.setAddress(dto.getAddress().trim());
        entity.setCity(dto.getCity().trim());
        entity.setState(dto.getState().trim());
        entity.setPostalCode(dto.getPostalCode().trim());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        entity.setVerificationStatus("VERIFIED");
        entity.setActive(true);
        return entity;
    }

    public void updateEntityFromDto(UpdateHospitalRequestDto dto, HospitalEntity entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getName() != null && !dto.getName().isBlank()) {
            entity.setName(dto.getName().trim());
        }
        if (dto.getHospitalType() != null) {
            entity.setHospitalType(dto.getHospitalType());
        }
        if (dto.getRegistrationIdentifier() != null) {
            entity.setRegistrationIdentifier(dto.getRegistrationIdentifier().trim());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            entity.setPhone(dto.getPhone().trim());
        }
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail().isBlank() ? null : dto.getEmail().trim().toLowerCase());
        }
        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            entity.setAddress(dto.getAddress().trim());
        }
        if (dto.getCity() != null && !dto.getCity().isBlank()) {
            entity.setCity(dto.getCity().trim());
        }
        if (dto.getState() != null && !dto.getState().isBlank()) {
            entity.setState(dto.getState().trim());
        }
        if (dto.getPostalCode() != null && !dto.getPostalCode().isBlank()) {
            entity.setPostalCode(dto.getPostalCode().trim());
        }
        if (dto.getLatitude() != null) {
            entity.setLatitude(dto.getLatitude());
        }
        if (dto.getLongitude() != null) {
            entity.setLongitude(dto.getLongitude());
        }
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
    }

    public HospitalResponseDto toResponseDto(HospitalEntity entity) {
        if (entity == null) {
            return null;
        }
        return new HospitalResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getHospitalType(),
                entity.getRegistrationIdentifier(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getAddress(),
                entity.getCity(),
                entity.getState(),
                entity.getPostalCode(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getVerificationStatus(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public HospitalSummaryDto toSummaryDto(HospitalEntity entity) {
        if (entity == null) {
            return null;
        }
        return new HospitalSummaryDto(
                entity.getId(),
                entity.getName(),
                entity.getHospitalType(),
                entity.getPhone(),
                entity.getCity(),
                entity.getState(),
                entity.getPostalCode(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.isActive()
        );
    }
}
