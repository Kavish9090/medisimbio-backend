package com.medisimbio.backend.prescription.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PrescriptionItemRequestDto(
        @NotBlank(message = "Medicine name is required")
        @Size(max = 255, message = "Medicine name must not exceed 255 characters")
        String medicineName,

        @Size(max = 255, message = "Active ingredient must not exceed 255 characters")
        String activeIngredient,

        @Size(max = 100, message = "Strength must not exceed 100 characters")
        String strength,

        @Size(max = 50, message = "Dosage form must not exceed 50 characters")
        String dosageForm,

        @NotBlank(message = "Dose is required")
        @Size(max = 100, message = "Dose must not exceed 100 characters")
        String dose,

        @NotBlank(message = "Route is required")
        @Size(max = 50, message = "Route must not exceed 50 characters")
        String route,

        @NotBlank(message = "Frequency is required")
        @Size(max = 50, message = "Frequency must not exceed 50 characters")
        String frequency,

        @Positive(message = "Duration value must be positive")
        Integer durationValue,

        @Size(max = 20, message = "Duration unit must not exceed 20 characters")
        String durationUnit,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @Size(max = 50, message = "Quantity unit must not exceed 50 characters")
        String quantityUnit,

        @Min(value = 0, message = "Refills allowed cannot be negative")
        Integer refillsAllowed,

        @Size(max = 50, message = "Instructions must not exceed 500 characters")
        String instructions,

        Integer sequenceNumber
) {
}
