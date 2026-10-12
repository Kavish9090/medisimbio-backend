package com.medisimbio.backend.provider.service;

import com.medisimbio.backend.common.response.PageResponse;
import com.medisimbio.backend.provider.dto.*;
import com.medisimbio.backend.specialty.dto.SpecialtyResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Healthcare Providers (Doctors) management.
 */
public interface ProviderService {

    ProviderResponseDto createProvider(CreateProviderRequestDto requestDto);

    ProviderResponseDto updateProvider(UUID id, UpdateProviderRequestDto requestDto);

    ProviderResponseDto getProviderById(UUID id);

    PageResponse<ProviderSummaryDto> getProviders(UUID specialtyId, UUID hospitalId, String city,
                                                  Boolean active, String search, Pageable pageable);

    ProviderResponseDto assignSpecialties(UUID providerId, AssignSpecialtyRequestDto requestDto);

    void removeSpecialty(UUID providerId, UUID specialtyId);

    List<SpecialtyResponseDto> getSpecialtiesForProvider(UUID providerId);

    ProviderHospitalResponseDto assignHospital(UUID providerId, AssignHospitalRequestDto requestDto);

    void removeHospital(UUID providerId, UUID hospitalId);

    List<ProviderHospitalResponseDto> getHospitalsForProvider(UUID providerId);
}
