package com.medisimbio.backend.prescription.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record UpdatePrescriptionRequestDto(
        LocalDate validUntil,

        @Size(max = 2000, message = "Clinical instructions must not exceed 2000 characters")
        String clinicalInstructions,

        @Valid
        List<PrescriptionItemRequestDto> items
) {
}
