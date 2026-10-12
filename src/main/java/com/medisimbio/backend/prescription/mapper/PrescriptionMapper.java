package com.medisimbio.backend.prescription.mapper;

import com.medisimbio.backend.prescription.dto.PrescriptionItemResponseDto;
import com.medisimbio.backend.prescription.dto.PrescriptionResponseDto;
import com.medisimbio.backend.prescription.dto.PrescriptionSummaryDto;
import com.medisimbio.backend.prescription.entity.PrescriptionEntity;
import com.medisimbio.backend.prescription.entity.PrescriptionItemEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PrescriptionMapper {

    public PrescriptionItemResponseDto toItemResponseDto(PrescriptionItemEntity entity) {
        if (entity == null) {
            return null;
        }
        return new PrescriptionItemResponseDto(
                entity.getId(),
                entity.getMedicineName(),
                entity.getActiveIngredient(),
                entity.getStrength(),
                entity.getDosageForm(),
                entity.getDose(),
                entity.getRoute(),
                entity.getFrequency(),
                entity.getDurationValue(),
                entity.getDurationUnit(),
                entity.getQuantity(),
                entity.getQuantityUnit(),
                entity.getRefillsAllowed(),
                entity.getInstructions(),
                entity.getSequenceNumber()
        );
    }

    public PrescriptionResponseDto toResponseDto(PrescriptionEntity entity) {
        if (entity == null) {
            return null;
        }

        List<PrescriptionItemResponseDto> itemDtos = entity.getItems() != null
                ? entity.getItems().stream().map(this::toItemResponseDto).collect(Collectors.toList())
                : Collections.emptyList();

        return new PrescriptionResponseDto(
                entity.getId(),
                entity.getPrescriptionReference(),
                entity.getPatient() != null ? entity.getPatient().getId() : null,
                entity.getPatient() != null ? entity.getPatient().getFullName() : null,
                entity.getEncounter() != null ? entity.getEncounter().getId() : null,
                entity.getEncounter() != null ? entity.getEncounter().getEncounterReference() : null,
                entity.getProvider() != null ? entity.getProvider().getId() : null,
                entity.getProvider() != null ? entity.getProvider().getFullName() : null,
                entity.getHospital() != null ? entity.getHospital().getId() : null,
                entity.getHospital() != null ? entity.getHospital().getName() : null,
                entity.getStatus(),
                entity.getPrescribedAt(),
                entity.getFinalizedAt(),
                entity.getValidUntil(),
                entity.getClinicalInstructions(),
                entity.getVersionNumber(),
                entity.getReplacesPrescription() != null ? entity.getReplacesPrescription().getId() : null,
                entity.getCancellationReason(),
                entity.getCancelledAt(),
                entity.getCancelledBy(),
                itemDtos,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public PrescriptionSummaryDto toSummaryDto(PrescriptionEntity entity) {
        if (entity == null) {
            return null;
        }
        int itemCount = entity.getItems() != null ? entity.getItems().size() : 0;
        return new PrescriptionSummaryDto(
                entity.getId(),
                entity.getPrescriptionReference(),
                entity.getPatient() != null ? entity.getPatient().getId() : null,
                entity.getPatient() != null ? entity.getPatient().getFullName() : null,
                entity.getEncounter() != null ? entity.getEncounter().getId() : null,
                entity.getEncounter() != null ? entity.getEncounter().getEncounterReference() : null,
                entity.getProvider() != null ? entity.getProvider().getId() : null,
                entity.getProvider() != null ? entity.getProvider().getFullName() : null,
                entity.getHospital() != null ? entity.getHospital().getId() : null,
                entity.getHospital() != null ? entity.getHospital().getName() : null,
                entity.getStatus(),
                entity.getPrescribedAt(),
                entity.getFinalizedAt(),
                itemCount,
                entity.getVersionNumber()
        );
    }
}
