package com.medisimbio.backend.appointment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request payload for cancelling an existing appointment.
 */
@Schema(description = "Request payload for cancelling an appointment")
public class CancelAppointmentRequestDto {

    @Schema(description = "Reason for cancellation", example = "Patient unavailable due to travel conflict")
    @Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
    private String cancellationReason;

    public CancelAppointmentRequestDto() {
    }

    public CancelAppointmentRequestDto(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
}
