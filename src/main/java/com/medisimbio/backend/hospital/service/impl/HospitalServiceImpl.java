package com.medisimbio.backend.hospital.service.impl;

import com.medisimbio.backend.common.exception.ResourceAlreadyExistsException;
import com.medisimbio.backend.common.exception.ResourceNotFoundException;
import com.medisimbio.backend.common.response.PageResponse;
import com.medisimbio.backend.hospital.dto.CreateHospitalRequestDto;
import com.medisimbio.backend.hospital.dto.HospitalResponseDto;
import com.medisimbio.backend.hospital.dto.HospitalSummaryDto;
import com.medisimbio.backend.hospital.dto.UpdateHospitalRequestDto;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.entity.HospitalType;
import com.medisimbio.backend.hospital.mapper.HospitalMapper;
import com.medisimbio.backend.hospital.repository.HospitalRepository;
import com.medisimbio.backend.hospital.repository.HospitalSpecification;
import com.medisimbio.backend.hospital.service.HospitalService;
import com.medisimbio.backend.provider.dto.ProviderSummaryDto;
import com.medisimbio.backend.provider.entity.ProviderHospitalEntity;
import com.medisimbio.backend.provider.mapper.ProviderMapper;
import com.medisimbio.backend.provider.repository.ProviderHospitalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Implementation of HospitalService.
 */
@Service
public class HospitalServiceImpl implements HospitalService {

    private static final Logger log = LoggerFactory.getLogger(HospitalServiceImpl.class);

    private final HospitalRepository hospitalRepository;
    private final HospitalMapper hospitalMapper;
    private final ProviderHospitalRepository providerHospitalRepository;
    private final ProviderMapper providerMapper;

    public HospitalServiceImpl(HospitalRepository hospitalRepository,
                               HospitalMapper hospitalMapper,
                               ProviderHospitalRepository providerHospitalRepository,
                               ProviderMapper providerMapper) {
        this.hospitalRepository = hospitalRepository;
        this.hospitalMapper = hospitalMapper;
        this.providerHospitalRepository = providerHospitalRepository;
        this.providerMapper = providerMapper;
    }

    @Override
    @Transactional
    public HospitalResponseDto createHospital(CreateHospitalRequestDto requestDto) {
        log.info("Creating new hospital facility: '{}' in '{}'", requestDto.getName(), requestDto.getCity());

        if (requestDto.getRegistrationIdentifier() != null && !requestDto.getRegistrationIdentifier().isBlank()) {
            String regId = requestDto.getRegistrationIdentifier().trim();
            if (hospitalRepository.existsByRegistrationIdentifier(regId)) {
                throw new ResourceAlreadyExistsException("Hospital with registration identifier '" + regId + "' already exists");
            }
        }

        HospitalEntity entity = hospitalMapper.toEntity(requestDto);
        HospitalEntity saved = hospitalRepository.save(entity);
        log.info("Hospital created successfully with ID: {}", saved.getId());
        return hospitalMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public HospitalResponseDto updateHospital(UUID id, UpdateHospitalRequestDto requestDto) {
        log.info("Updating hospital with ID: {}", id);

        HospitalEntity entity = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + id));

        if (requestDto.getRegistrationIdentifier() != null && !requestDto.getRegistrationIdentifier().isBlank()) {
            String newRegId = requestDto.getRegistrationIdentifier().trim();
            if (!newRegId.equalsIgnoreCase(entity.getRegistrationIdentifier())
                    && hospitalRepository.existsByRegistrationIdentifier(newRegId)) {
                throw new ResourceAlreadyExistsException("Hospital with registration identifier '" + newRegId + "' already exists");
            }
        }

        hospitalMapper.updateEntityFromDto(requestDto, entity);
        HospitalEntity updated = hospitalRepository.save(entity);
        log.info("Hospital updated successfully with ID: {}", updated.getId());
        return hospitalMapper.toResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public HospitalResponseDto getHospitalById(UUID id) {
        HospitalEntity entity = hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + id));
        return hospitalMapper.toResponseDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HospitalSummaryDto> getHospitals(HospitalType type, String city, String state,
                                                         Boolean active, String search, Pageable pageable) {
        Specification<HospitalEntity> spec = HospitalSpecification.withFilters(type, city, state, active, search);
        Page<HospitalEntity> page = hospitalRepository.findAll(spec, pageable);
        Page<HospitalSummaryDto> dtoPage = page.map(hospitalMapper::toSummaryDto);
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProviderSummaryDto> getProvidersForHospital(UUID hospitalId, Boolean active, Pageable pageable) {
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital not found with ID: " + hospitalId);
        }

        Page<ProviderHospitalEntity> affiliations;
        if (active != null) {
            affiliations = providerHospitalRepository.findByHospitalIdAndActive(hospitalId, active, pageable);
        } else {
            affiliations = providerHospitalRepository.findByHospitalId(hospitalId, pageable);
        }

        Page<ProviderSummaryDto> dtoPage = affiliations.map(aff -> providerMapper.toSummaryDto(aff.getProvider()));
        return PageResponse.from(dtoPage);
    }
}
