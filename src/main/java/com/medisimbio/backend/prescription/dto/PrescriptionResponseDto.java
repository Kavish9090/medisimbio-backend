package com.medisimbio.backend.prescription.dto;

import com.medisimbio.backend.prescription.entity.PrescriptionStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PrescriptionResponseDto(
        UUID id,
        String prescriptionReference,
        UUID patientId,
        String patientName,
        UUID encounterId,
        String encounterReference,
        UUID providerId,
        String providerName,
        UUID hospitalId,
        String hospitalName,
        PrescriptionStatus status,
        Instant prescribedAt,
        Instant finalizedAt,
        LocalDate validUntil,
        String clinicalInstructions,
        Integer versionNumber,
        UUID replacesPrescriptionId,
        String cancellationReason,
        Instant cancelledAt,
        String cancelledBy,
        List<PrescriptionItemResponseDto> items,
        Instant createdAt,
        Instant updatedAt
) {
}
