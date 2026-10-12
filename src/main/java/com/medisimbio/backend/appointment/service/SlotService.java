package com.medisimbio.backend.appointment.service;

import com.medisimbio.backend.appointment.dto.SlotResponseDto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for dynamic consultation slot calculation.
 */
public interface SlotService {

    List<SlotResponseDto> getAvailableSlots(UUID providerId, UUID hospitalId, LocalDate date);
}
