package com.medisimbio.backend.specialty.mapper;

import com.medisimbio.backend.specialty.dto.CreateSpecialtyRequestDto;
import com.medisimbio.backend.specialty.dto.SpecialtyResponseDto;
import com.medisimbio.backend.specialty.entity.SpecialtyEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper between SpecialtyEntity and Specialty DTOs.
 */
@Component
public class SpecialtyMapper {

    public SpecialtyEntity toEntity(CreateSpecialtyRequestDto dto) {
        if (dto == null) {
            return null;
        }
        SpecialtyEntity entity = new SpecialtyEntity();
        entity.setId(UUID.randomUUID());
        entity.setCode(dto.getCode().trim().toUpperCase());
        entity.setDisplayName(dto.getDisplayName().trim());
        entity.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        entity.setActive(true);
        return entity;
    }

    public SpecialtyResponseDto toResponseDto(SpecialtyEntity entity) {
        if (entity == null) {
            return null;
        }
        return new SpecialtyResponseDto(
                entity.getId(),
                entity.getCode(),
                entity.getDisplayName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
