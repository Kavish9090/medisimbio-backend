package com.medisimbio.backend.hospital.service;

import com.medisimbio.backend.common.response.PageResponse;
import com.medisimbio.backend.hospital.dto.CreateHospitalRequestDto;
import com.medisimbio.backend.hospital.dto.HospitalResponseDto;
import com.medisimbio.backend.hospital.dto.HospitalSummaryDto;
import com.medisimbio.backend.hospital.dto.UpdateHospitalRequestDto;
import com.medisimbio.backend.hospital.entity.HospitalType;
import com.medisimbio.backend.provider.dto.ProviderSummaryDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for Hospital and Clinic management.
 */
public interface HospitalService {

    HospitalResponseDto createHospital(CreateHospitalRequestDto requestDto);

    HospitalResponseDto updateHospital(UUID id, UpdateHospitalRequestDto requestDto);

    HospitalResponseDto getHospitalById(UUID id);

    PageResponse<HospitalSummaryDto> getHospitals(HospitalType type, String city, String state,
                                                  Boolean active, String search, Pageable pageable);

    PageResponse<ProviderSummaryDto> getProvidersForHospital(UUID hospitalId, Boolean active, Pageable pageable);
}
