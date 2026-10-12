package com.medisimbio.backend.dispensing.mapper;

import com.medisimbio.backend.dispensing.dto.DispensingOrderItemResponseDto;
import com.medisimbio.backend.dispensing.dto.DispensingOrderResponseDto;
import com.medisimbio.backend.dispensing.entity.DispensingOrderEntity;
import com.medisimbio.backend.dispensing.entity.DispensingOrderItemEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DispensingOrderMapper {

    public DispensingOrderItemResponseDto toItemResponseDto(DispensingOrderItemEntity entity) {
        if (entity == null) {
            return null;
        }
        return new DispensingOrderItemResponseDto(
                entity.getId(),
                entity.getPrescriptionItem() != null ? entity.getPrescriptionItem().getId() : null,
                entity.getMedicineName(),
                entity.getRequestedQuantity(),
                entity.getDispensedQuantity()
        );
    }

    public DispensingOrderResponseDto toResponseDto(DispensingOrderEntity entity) {
        if (entity == null) {
            return null;
        }

        List<DispensingOrderItemResponseDto> itemDtos = entity.getItems() != null
                ? entity.getItems().stream().map(this::toItemResponseDto).collect(Collectors.toList())
                : Collections.emptyList();

        return new DispensingOrderResponseDto(
                entity.getId(),
                entity.getDispensingReference(),
                entity.getPrescription() != null ? entity.getPrescription().getId() : null,
                entity.getPrescription() != null ? entity.getPrescription().getPrescriptionReference() : null,
                entity.getPatient() != null ? entity.getPatient().getId() : null,
                entity.getPatient() != null ? entity.getPatient().getFullName() : null,
                entity.getHospital() != null ? entity.getHospital().getId() : null,
                entity.getHospital() != null ? entity.getHospital().getName() : null,
                entity.getStatus(),
                entity.getOrderedAt(),
                entity.getNotes(),
                itemDtos,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
