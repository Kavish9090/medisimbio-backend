package com.medisimbio.backend.prescription.dto;

import com.medisimbio.backend.prescription.entity.PrescriptionStatus;

import java.time.Instant;
import java.util.UUID;

public record PrescriptionSummaryDto(
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
        Integer itemCount,
        Integer versionNumber
) {
}
