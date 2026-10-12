package com.medisimbio.backend.dispensing.dto;

import com.medisimbio.backend.dispensing.entity.DispensingOrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DispensingOrderResponseDto(
        UUID id,
        String dispensingReference,
        UUID prescriptionId,
        String prescriptionReference,
        UUID patientId,
        String patientName,
        UUID hospitalId,
        String hospitalName,
        DispensingOrderStatus status,
        Instant orderedAt,
        String notes,
        List<DispensingOrderItemResponseDto> items,
        Instant createdAt,
        Instant updatedAt
) {
}
