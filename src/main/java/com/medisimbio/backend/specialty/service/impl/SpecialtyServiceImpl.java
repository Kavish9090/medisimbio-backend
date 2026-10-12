package com.medisimbio.backend.specialty.service.impl;

import com.medisimbio.backend.common.exception.ResourceAlreadyExistsException;
import com.medisimbio.backend.common.exception.ResourceNotFoundException;
import com.medisimbio.backend.common.response.PageResponse;
import com.medisimbio.backend.specialty.dto.CreateSpecialtyRequestDto;
import com.medisimbio.backend.specialty.dto.SpecialtyResponseDto;
import com.medisimbio.backend.specialty.entity.SpecialtyEntity;
import com.medisimbio.backend.specialty.mapper.SpecialtyMapper;
import com.medisimbio.backend.specialty.repository.SpecialtyRepository;
import com.medisimbio.backend.specialty.service.SpecialtyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of SpecialtyService.
 */
@Service
public class SpecialtyServiceImpl implements SpecialtyService {

    private static final Logger log = LoggerFactory.getLogger(SpecialtyServiceImpl.class);

    private final SpecialtyRepository specialtyRepository;
    private final SpecialtyMapper specialtyMapper;

    public SpecialtyServiceImpl(SpecialtyRepository specialtyRepository, SpecialtyMapper specialtyMapper) {
        this.specialtyRepository = specialtyRepository;
        this.specialtyMapper = specialtyMapper;
    }

    @Override
    @Transactional
    public SpecialtyResponseDto createSpecialty(CreateSpecialtyRequestDto requestDto) {
        String normalizedCode = requestDto.getCode().trim().toUpperCase();
        log.info("Creating new medical specialty with code: {}", normalizedCode);

        if (specialtyRepository.existsByCode(normalizedCode)) {
            throw new ResourceAlreadyExistsException("Specialty with code '" + normalizedCode + "' already exists");
        }

        SpecialtyEntity entity = specialtyMapper.toEntity(requestDto);
        SpecialtyEntity saved = specialtyRepository.save(entity);
        log.info("Specialty created successfully with ID: {} and code: {}", saved.getId(), saved.getCode());
        return specialtyMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SpecialtyResponseDto getSpecialtyById(UUID id) {
        SpecialtyEntity entity = specialtyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with ID: " + id));
        return specialtyMapper.toResponseDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public SpecialtyResponseDto getSpecialtyByCode(String code) {
        SpecialtyEntity entity = specialtyRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with code: " + code));
        return specialtyMapper.toResponseDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SpecialtyResponseDto> getSpecialties(Boolean active, Pageable pageable) {
        Page<SpecialtyEntity> page;
        if (active != null) {
            page = specialtyRepository.findByActive(active, pageable);
        } else {
            page = specialtyRepository.findAll(pageable);
        }
        Page<SpecialtyResponseDto> dtoPage = page.map(specialtyMapper::toResponseDto);
        return PageResponse.from(dtoPage);
    }
}
