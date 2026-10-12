package com.medisimbio.backend.appointment.service;

import com.medisimbio.backend.appointment.dto.AvailabilityResponseDto;
import com.medisimbio.backend.appointment.dto.CreateAvailabilityRequestDto;
import com.medisimbio.backend.appointment.dto.UpdateAvailabilityRequestDto;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Doctor Availability management.
 */
public interface AvailabilityService {

    AvailabilityResponseDto createAvailability(UUID providerId, CreateAvailabilityRequestDto requestDto);

    AvailabilityResponseDto updateAvailability(UUID providerId, UUID availabilityId, UpdateAvailabilityRequestDto requestDto);

    List<AvailabilityResponseDto> getAvailabilities(UUID providerId, UUID hospitalId, DayOfWeek dayOfWeek, Boolean active);

    AvailabilityResponseDto getAvailabilityById(UUID providerId, UUID availabilityId);

    void deleteAvailability(UUID providerId, UUID availabilityId);
}
