package com.medisimbio.backend.specialty.service;

import com.medisimbio.backend.common.response.PageResponse;
import com.medisimbio.backend.specialty.dto.CreateSpecialtyRequestDto;
import com.medisimbio.backend.specialty.dto.SpecialtyResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service interface for Medical Specialties.
 */
public interface SpecialtyService {

    SpecialtyResponseDto createSpecialty(CreateSpecialtyRequestDto requestDto);

    SpecialtyResponseDto getSpecialtyById(UUID id);

    SpecialtyResponseDto getSpecialtyByCode(String code);

    PageResponse<SpecialtyResponseDto> getSpecialties(Boolean active, Pageable pageable);
}
