package com.medisimbio.backend.prescription.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreatePrescriptionRequestDto(
        LocalDate validUntil,

        @Size(max = 2000, message = "Clinical instructions must not exceed 2000 characters")
        String clinicalInstructions,

        @NotEmpty(message = "Prescription must contain at least one medicine item")
        @Valid
        List<PrescriptionItemRequestDto> items,

        Boolean finalizeImmediately
) {
}
