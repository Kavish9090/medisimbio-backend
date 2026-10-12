package com.medisimbio.backend.provider.service.impl;

import com.medisimbio.backend.common.exception.ResourceAlreadyExistsException;
import com.medisimbio.backend.common.exception.ResourceNotFoundException;
import com.medisimbio.backend.common.response.PageResponse;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.repository.HospitalRepository;
import com.medisimbio.backend.provider.dto.*;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.provider.entity.ProviderHospitalEntity;
import com.medisimbio.backend.provider.mapper.ProviderMapper;
import com.medisimbio.backend.provider.repository.ProviderHospitalRepository;
import com.medisimbio.backend.provider.repository.ProviderRepository;
import com.medisimbio.backend.provider.repository.ProviderSpecification;
import com.medisimbio.backend.provider.service.ProviderService;
import com.medisimbio.backend.specialty.dto.SpecialtyResponseDto;
import com.medisimbio.backend.specialty.entity.SpecialtyEntity;
import com.medisimbio.backend.specialty.mapper.SpecialtyMapper;
import com.medisimbio.backend.specialty.repository.SpecialtyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of ProviderService.
 */
@Service
public class ProviderServiceImpl implements ProviderService {

    private static final Logger log = LoggerFactory.getLogger(ProviderServiceImpl.class);

    private final ProviderRepository providerRepository;
    private final ProviderHospitalRepository providerHospitalRepository;
    private final SpecialtyRepository specialtyRepository;
    private final HospitalRepository hospitalRepository;
    private final ProviderMapper providerMapper;
    private final SpecialtyMapper specialtyMapper;

    public ProviderServiceImpl(ProviderRepository providerRepository,
                               ProviderHospitalRepository providerHospitalRepository,
                               SpecialtyRepository specialtyRepository,
                               HospitalRepository hospitalRepository,
                               ProviderMapper providerMapper,
                               SpecialtyMapper specialtyMapper) {
        this.providerRepository = providerRepository;
        this.providerHospitalRepository = providerHospitalRepository;
        this.specialtyRepository = specialtyRepository;
        this.hospitalRepository = hospitalRepository;
        this.providerMapper = providerMapper;
        this.specialtyMapper = specialtyMapper;
    }

    @Override
    @Transactional
    public ProviderResponseDto createProvider(CreateProviderRequestDto requestDto) {
        String regNum = requestDto.getProfessionalRegistrationNumber().trim().toUpperCase();
        String email = requestDto.getEmail().trim().toLowerCase();
        String mobile = requestDto.getMobileNumber().trim();

        log.info("Registering healthcare provider with registration number: {}", regNum);

        if (providerRepository.existsByProfessionalRegistrationNumber(regNum)) {
            throw new ResourceAlreadyExistsException("Provider with registration number '" + regNum + "' already exists");
        }
        if (providerRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Provider with email '" + email + "' already exists");
        }
        if (providerRepository.existsByMobileNumber(mobile)) {
            throw new ResourceAlreadyExistsException("Provider with mobile number '" + mobile + "' already exists");
        }

        ProviderEntity entity = providerMapper.toEntity(requestDto);

        if (requestDto.getSpecialtyIds() != null && !requestDto.getSpecialtyIds().isEmpty()) {
            List<SpecialtyEntity> specialties = specialtyRepository.findAllById(requestDto.getSpecialtyIds());
            entity.setSpecialties(new HashSet<>(specialties));
        }

        ProviderEntity saved = providerRepository.save(entity);
        log.info("Healthcare provider registered successfully with ID: {}", saved.getId());
        return providerMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public ProviderResponseDto updateProvider(UUID id, UpdateProviderRequestDto requestDto) {
        log.info("Updating provider with ID: {}", id);

        ProviderEntity entity = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + id));

        if (requestDto.getEmail() != null && !requestDto.getEmail().isBlank()) {
            String newEmail = requestDto.getEmail().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(entity.getEmail()) && providerRepository.existsByEmail(newEmail)) {
                throw new ResourceAlreadyExistsException("Provider with email '" + newEmail + "' already exists");
            }
        }

        if (requestDto.getMobileNumber() != null && !requestDto.getMobileNumber().isBlank()) {
            String newMobile = requestDto.getMobileNumber().trim();
            if (!newMobile.equalsIgnoreCase(entity.getMobileNumber()) && providerRepository.existsByMobileNumber(newMobile)) {
                throw new ResourceAlreadyExistsException("Provider with mobile number '" + newMobile + "' already exists");
            }
        }

        providerMapper.updateEntityFromDto(requestDto, entity);
        ProviderEntity updated = providerRepository.save(entity);
        log.info("Provider updated successfully with ID: {}", updated.getId());
        return providerMapper.toResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ProviderResponseDto getProviderById(UUID id) {
        ProviderEntity entity = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + id));
        return providerMapper.toResponseDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProviderSummaryDto> getProviders(UUID specialtyId, UUID hospitalId, String city,
                                                         Boolean active, String search, Pageable pageable) {
        Specification<ProviderEntity> spec = ProviderSpecification.withFilters(specialtyId, hospitalId, city, active, search);
        Page<ProviderEntity> page = providerRepository.findAll(spec, pageable);
        Page<ProviderSummaryDto> dtoPage = page.map(providerMapper::toSummaryDto);
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional
    public ProviderResponseDto assignSpecialties(UUID providerId, AssignSpecialtyRequestDto requestDto) {
        log.info("Assigning specialties {} to provider ID: {}", requestDto.getSpecialtyIds(), providerId);

        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + providerId));

        List<SpecialtyEntity> specialties = specialtyRepository.findAllById(requestDto.getSpecialtyIds());
        if (specialties.isEmpty()) {
            throw new ResourceNotFoundException("No valid specialties found for provided IDs");
        }

        provider.getSpecialties().addAll(specialties);
        ProviderEntity saved = providerRepository.save(provider);
        return providerMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public void removeSpecialty(UUID providerId, UUID specialtyId) {
        log.info("Removing specialty {} from provider ID: {}", specialtyId, providerId);

        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + providerId));

        SpecialtyEntity specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with ID: " + specialtyId));

        provider.removeSpecialty(specialty);
        providerRepository.save(provider);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpecialtyResponseDto> getSpecialtiesForProvider(UUID providerId) {
        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + providerId));

        return provider.getSpecialties().stream()
                .map(specialtyMapper::toResponseDto)
                .sorted(Comparator.comparing(SpecialtyResponseDto::getDisplayName))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProviderHospitalResponseDto assignHospital(UUID providerId, AssignHospitalRequestDto requestDto) {
        log.info("Assigning hospital {} to provider ID: {}", requestDto.getHospitalId(), providerId);

        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + providerId));

        HospitalEntity hospital = hospitalRepository.findById(requestDto.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + requestDto.getHospitalId()));

        Optional<ProviderHospitalEntity> existingOpt = providerHospitalRepository
                .findByProviderIdAndHospitalId(providerId, requestDto.getHospitalId());

        ProviderHospitalEntity affiliation;
        if (existingOpt.isPresent()) {
            affiliation = existingOpt.get();
            affiliation.setDepartment(requestDto.getDepartment());
            affiliation.setConsultationFee(requestDto.getConsultationFee());
            affiliation.setActive(requestDto.getActive() != null ? requestDto.getActive() : true);
        } else {
            affiliation = new ProviderHospitalEntity();
            affiliation.setId(UUID.randomUUID());
            affiliation.setProvider(provider);
            affiliation.setHospital(hospital);
            affiliation.setDepartment(requestDto.getDepartment());
            affiliation.setConsultationFee(requestDto.getConsultationFee());
            affiliation.setActive(requestDto.getActive() != null ? requestDto.getActive() : true);
        }

        ProviderHospitalEntity saved = providerHospitalRepository.save(affiliation);
        return providerMapper.toAffiliationDto(saved);
    }

    @Override
    @Transactional
    public void removeHospital(UUID providerId, UUID hospitalId) {
        log.info("Removing hospital {} affiliation from provider ID: {}", hospitalId, providerId);

        if (!providerRepository.existsById(providerId)) {
            throw new ResourceNotFoundException("Provider not found with ID: " + providerId);
        }
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital not found with ID: " + hospitalId);
        }

        providerHospitalRepository.deleteByProviderIdAndHospitalId(providerId, hospitalId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProviderHospitalResponseDto> getHospitalsForProvider(UUID providerId) {
        if (!providerRepository.existsById(providerId)) {
            throw new ResourceNotFoundException("Provider not found with ID: " + providerId);
        }

        List<ProviderHospitalEntity> affiliations = providerHospitalRepository.findByProviderIdWithHospital(providerId);
        return affiliations.stream()
                .map(providerMapper::toAffiliationDto)
                .collect(Collectors.toList());
    }
}
