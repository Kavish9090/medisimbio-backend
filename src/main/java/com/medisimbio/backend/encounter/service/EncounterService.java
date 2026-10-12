package com.medisimbio.backend.encounter.service;

import com.medisimbio.backend.common.response.PageResponse;
import com.medisimbio.backend.encounter.dto.*;
import com.medisimbio.backend.encounter.entity.EncounterStatus;
import com.medisimbio.backend.encounter.entity.EncounterType;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

/**
 * Service interface for managing clinical encounters and consultation lifecycles.
 */
public interface EncounterService {

    EncounterResponseDto createEncounter(CreateEncounterRequestDto requestDto);

    EncounterResponseDto getEncounterById(UUID encounterId);

    EncounterResponseDto updateEncounter(UUID encounterId, UpdateEncounterRequestDto requestDto);

    EncounterResponseDto startEncounter(UUID encounterId);

    EncounterResponseDto completeEncounter(UUID encounterId);

    EncounterResponseDto cancelEncounter(UUID encounterId, String cancellationReason);

    PageResponse<EncounterSummaryDto> getPatientEncounters(
            EncounterStatus status, EncounterType encounterType, Instant fromDate, Instant toDate, Pageable pageable);

    PageResponse<EncounterSummaryDto> getProviderEncounters(
            UUID providerId, EncounterStatus status, EncounterType encounterType, Instant fromDate, Instant toDate, Pageable pageable);
}
