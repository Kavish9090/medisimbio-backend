package com.medisimbio.backend.prescription.dto;

import java.util.UUID;

public record PrescriptionItemResponseDto(
        UUID id,
        String medicineName,
        String activeIngredient,
        String strength,
        String dosageForm,
        String dose,
        String route,
        String frequency,
        Integer durationValue,
        String durationUnit,
        Integer quantity,
        String quantityUnit,
        Integer refillsAllowed,
        String instructions,
        Integer sequenceNumber
) {
}
