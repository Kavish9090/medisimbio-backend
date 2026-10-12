package com.medisimbio.backend.dispensing.dto;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record CreateDispensingOrderRequestDto(
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes,

        List<UUID> prescriptionItemIds
) {
}
