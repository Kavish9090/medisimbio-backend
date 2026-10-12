package com.medisimbio.backend.vitals.service;

import com.medisimbio.backend.vitals.dto.CreateVitalsRequestDto;
import com.medisimbio.backend.vitals.dto.VitalsResponseDto;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for physiological vitals management.
 */
public interface VitalsService {

    VitalsResponseDto createVitals(UUID encounterId, CreateVitalsRequestDto requestDto);

    List<VitalsResponseDto> getVitalsByEncounterId(UUID encounterId);
}
