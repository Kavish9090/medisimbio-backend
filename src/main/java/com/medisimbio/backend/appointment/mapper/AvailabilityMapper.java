package com.medisimbio.backend.appointment.mapper;

import com.medisimbio.backend.appointment.dto.AvailabilityResponseDto;
import com.medisimbio.backend.appointment.dto.CreateAvailabilityRequestDto;
import com.medisimbio.backend.appointment.dto.UpdateAvailabilityRequestDto;
import com.medisimbio.backend.appointment.entity.DoctorAvailabilityEntity;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mapper between DoctorAvailabilityEntity and DTOs.
 */
@Component
public class AvailabilityMapper {

    public DoctorAvailabilityEntity toEntity(CreateAvailabilityRequestDto dto, ProviderEntity provider, HospitalEntity hospital) {
        if (dto == null) {
            return null;
        }
        DoctorAvailabilityEntity entity = new DoctorAvailabilityEntity();
        entity.setId(UUID.randomUUID());
        entity.setProvider(provider);
        entity.setHospital(hospital);
        entity.setDayOfWeek(dto.getDayOfWeek());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        entity.setSlotDurationMinutes(dto.getSlotDurationMinutes() != null ? dto.getSlotDurationMinutes() : 30);
        entity.setActive(true);
        return entity;
    }

    public void updateEntityFromDto(UpdateAvailabilityRequestDto dto, DoctorAvailabilityEntity entity, HospitalEntity newHospital) {
        if (dto == null || entity == null) {
            return;
        }
        if (newHospital != null) {
            entity.setHospital(newHospital);
        }
        if (dto.getDayOfWeek() != null) {
            entity.setDayOfWeek(dto.getDayOfWeek());
        }
        if (dto.getStartTime() != null) {
            entity.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            entity.setEndTime(dto.getEndTime());
        }
        if (dto.getSlotDurationMinutes() != null) {
            entity.setSlotDurationMinutes(dto.getSlotDurationMinutes());
        }
        if (dto.getActive() != null) {
            entity.setActive(dto.getActive());
        }
    }

    public AvailabilityResponseDto toResponseDto(DoctorAvailabilityEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AvailabilityResponseDto(
                entity.getId(),
                entity.getProvider() != null ? entity.getProvider().getId() : null,
                entity.getProvider() != null ? entity.getProvider().getFullName() : null,
                entity.getHospital() != null ? entity.getHospital().getId() : null,
                entity.getHospital() != null ? entity.getHospital().getName() : null,
                entity.getDayOfWeek(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getSlotDurationMinutes(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
