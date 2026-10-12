package com.medisimbio.backend.dispensing.dto;

import java.util.UUID;

public record DispensingOrderItemResponseDto(
        UUID id,
        UUID prescriptionItemId,
        String medicineName,
        Integer requestedQuantity,
        Integer dispensedQuantity
) {
}
