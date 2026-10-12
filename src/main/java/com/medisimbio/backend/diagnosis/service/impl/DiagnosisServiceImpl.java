package com.medisimbio.backend.diagnosis.service.impl;

import com.medisimbio.backend.common.exception.BadRequestException;
import com.medisimbio.backend.common.exception.ForbiddenException;
import com.medisimbio.backend.common.exception.ResourceNotFoundException;
import com.medisimbio.backend.diagnosis.dto.CreateDiagnosisRequestDto;
import com.medisimbio.backend.diagnosis.dto.DiagnosisResponseDto;
import com.medisimbio.backend.diagnosis.dto.UpdateDiagnosisRequestDto;
import com.medisimbio.backend.diagnosis.entity.DiagnosisEntity;
import com.medisimbio.backend.diagnosis.entity.DiagnosisStatus;
import com.medisimbio.backend.diagnosis.entity.DiagnosisType;
import com.medisimbio.backend.diagnosis.mapper.DiagnosisMapper;
import com.medisimbio.backend.diagnosis.repository.DiagnosisRepository;
import com.medisimbio.backend.diagnosis.service.DiagnosisService;
import com.medisimbio.backend.encounter.entity.EncounterEntity;
import com.medisimbio.backend.encounter.entity.EncounterStatus;
import com.medisimbio.backend.encounter.repository.EncounterRepository;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.repository.HospitalStaffRepository;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.security.AuthenticationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of DiagnosisService for provider-entered clinical diagnoses.
 */
@Service
public class DiagnosisServiceImpl implements DiagnosisService {

    private static final Logger log = LoggerFactory.getLogger(DiagnosisServiceImpl.class);

    private final DiagnosisRepository diagnosisRepository;
    private final EncounterRepository encounterRepository;
    private final HospitalStaffRepository hospitalStaffRepository;
    private final AuthenticationFacade authenticationFacade;
    private final DiagnosisMapper diagnosisMapper;

    public DiagnosisServiceImpl(DiagnosisRepository diagnosisRepository,
                                EncounterRepository encounterRepository,
                                HospitalStaffRepository hospitalStaffRepository,
                                AuthenticationFacade authenticationFacade,
                                DiagnosisMapper diagnosisMapper) {
        this.diagnosisRepository = diagnosisRepository;
        this.encounterRepository = encounterRepository;
        this.hospitalStaffRepository = hospitalStaffRepository;
        this.authenticationFacade = authenticationFacade;
        this.diagnosisMapper = diagnosisMapper;
    }

    @Override
    @Transactional
    public DiagnosisResponseDto createDiagnosis(UUID encounterId, CreateDiagnosisRequestDto requestDto) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        // Sanitized logging: log only non-sensitive operational identifiers (Encounter ID and Auth User ID)
        log.info("Recording diagnosis on encounter [{}] by user [{}]", encounterId, currentUserId);

        EncounterEntity encounter = encounterRepository.findByIdWithDetails(encounterId)
                .orElseThrow(() -> new ResourceNotFoundException("Encounter not found with ID: " + encounterId));

        validateProviderOrHospitalAccess(currentUserId, encounter);

        if (encounter.getStatus() == EncounterStatus.CANCELLED) {
            throw new BadRequestException("Cannot record diagnosis on a CANCELLED encounter");
        }

        Instant diagnosedAt = requestDto.getDiagnosedAt() != null ? requestDto.getDiagnosedAt() : Instant.now();

        DiagnosisEntity entity = new DiagnosisEntity(
                UUID.randomUUID(),
                encounter,
                encounter.getPatient(),
                requestDto.getDiagnosisCode() != null ? requestDto.getDiagnosisCode().trim() : null,
                requestDto.getDiagnosisName().trim(),
                requestDto.getDiagnosisDescription() != null ? requestDto.getDiagnosisDescription().trim() : null,
                requestDto.getDiagnosisType() != null ? requestDto.getDiagnosisType() : DiagnosisType.PRIMARY,
                requestDto.getStatus() != null ? requestDto.getStatus() : DiagnosisStatus.ACTIVE,
                diagnosedAt,
                requestDto.getNotes() != null ? requestDto.getNotes().trim() : null,
                currentUserId
        );

        DiagnosisEntity saved = diagnosisRepository.save(entity);
        log.info("Diagnosis recorded successfully with ID [{}]", saved.getId());
        return diagnosisMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiagnosisResponseDto> getDiagnosesByEncounterId(UUID encounterId) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();

        EncounterEntity encounter = encounterRepository.findByIdWithDetails(encounterId)
                .orElseThrow(() -> new ResourceNotFoundException("Encounter not found with ID: " + encounterId));

        validateAccess(currentUserId, encounter);

        List<DiagnosisEntity> diagnoses = diagnosisRepository.findByEncounterId(encounterId);
        return diagnoses.stream()
                .map(diagnosisMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DiagnosisResponseDto updateDiagnosis(UUID diagnosisId, UpdateDiagnosisRequestDto requestDto) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Updating diagnosis [{}] by user [{}]", diagnosisId, currentUserId);

        DiagnosisEntity entity = diagnosisRepository.findByIdWithDetails(diagnosisId)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis not found with ID: " + diagnosisId));

        validateProviderOrHospitalAccess(currentUserId, entity.getEncounter());

        if (requestDto.getDiagnosisName() != null && !requestDto.getDiagnosisName().isBlank()) {
            entity.setDiagnosisName(requestDto.getDiagnosisName().trim());
        }
        if (requestDto.getDiagnosisCode() != null) {
            entity.setDiagnosisCode(requestDto.getDiagnosisCode().trim());
        }
        if (requestDto.getDiagnosisDescription() != null) {
            entity.setDiagnosisDescription(requestDto.getDiagnosisDescription().trim());
        }
        if (requestDto.getDiagnosisType() != null) {
            entity.setDiagnosisType(requestDto.getDiagnosisType());
        }
        if (requestDto.getStatus() != null) {
            entity.setStatus(requestDto.getStatus());
        }
        if (requestDto.getNotes() != null) {
            entity.setNotes(requestDto.getNotes().trim());
        }

        entity.setUpdatedBy(currentUserId);
        DiagnosisEntity updated = diagnosisRepository.save(entity);
        return diagnosisMapper.toResponseDto(updated);
    }

    private void validateAccess(String currentUserId, EncounterEntity encounter) {
        boolean isPatient = encounter.getPatient() != null && currentUserId.equals(encounter.getPatient().getAuthUserId());
        boolean isProvider = isAuthorizedForProvider(currentUserId, encounter.getProvider());
        boolean isHospital = isAuthorizedForHospital(currentUserId, encounter.getHospital());

        if (!isPatient && !isProvider && !isHospital) {
            throw new ForbiddenException("Access denied: You are not authorized to view clinical diagnoses for this encounter");
        }
    }

    private void validateProviderOrHospitalAccess(String currentUserId, EncounterEntity encounter) {
        boolean isProvider = isAuthorizedForProvider(currentUserId, encounter.getProvider());
        boolean isHospital = isAuthorizedForHospital(currentUserId, encounter.getHospital());

        if (!isProvider && !isHospital) {
            throw new ForbiddenException("Access denied: Only attending healthcare providers or clinical staff may record or modify diagnoses");
        }
    }

    private boolean isAuthorizedForProvider(String currentUserId, ProviderEntity provider) {
        if (currentUserId == null || currentUserId.isBlank() || provider == null) {
            return false;
        }
        return provider.getAuthUserId() != null && provider.getAuthUserId().equals(currentUserId);
    }

    private boolean isAuthorizedForHospital(String currentUserId, HospitalEntity hospital) {
        if (currentUserId == null || currentUserId.isBlank() || hospital == null) {
            return false;
        }
        return hospitalStaffRepository.existsByHospitalIdAndAuthUserIdAndActiveTrue(hospital.getId(), currentUserId);
    }
}
