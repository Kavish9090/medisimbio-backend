package com.medisimbio.backend.vitals.service.impl;

import com.medisimbio.backend.common.exception.BadRequestException;
import com.medisimbio.backend.common.exception.ForbiddenException;
import com.medisimbio.backend.common.exception.ResourceNotFoundException;
import com.medisimbio.backend.encounter.entity.EncounterEntity;
import com.medisimbio.backend.encounter.entity.EncounterStatus;
import com.medisimbio.backend.encounter.repository.EncounterRepository;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.repository.HospitalStaffRepository;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.security.AuthenticationFacade;
import com.medisimbio.backend.vitals.dto.CreateVitalsRequestDto;
import com.medisimbio.backend.vitals.dto.VitalsResponseDto;
import com.medisimbio.backend.vitals.entity.VitalsEntity;
import com.medisimbio.backend.vitals.mapper.VitalsMapper;
import com.medisimbio.backend.vitals.repository.VitalsRepository;
import com.medisimbio.backend.vitals.service.VitalsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of VitalsService for clinical vitals recording and BMI calculation.
 */
@Service
public class VitalsServiceImpl implements VitalsService {

    private static final Logger log = LoggerFactory.getLogger(VitalsServiceImpl.class);

    private final VitalsRepository vitalsRepository;
    private final EncounterRepository encounterRepository;
    private final AuthenticationFacade authenticationFacade;
    private final HospitalStaffRepository hospitalStaffRepository;
    private final VitalsMapper vitalsMapper;

    public VitalsServiceImpl(VitalsRepository vitalsRepository,
                             EncounterRepository encounterRepository,
                             AuthenticationFacade authenticationFacade,
                             HospitalStaffRepository hospitalStaffRepository,
                             VitalsMapper vitalsMapper) {
        this.vitalsRepository = vitalsRepository;
        this.encounterRepository = encounterRepository;
        this.authenticationFacade = authenticationFacade;
        this.hospitalStaffRepository = hospitalStaffRepository;
        this.vitalsMapper = vitalsMapper;
    }

    @Override
    @Transactional
    public VitalsResponseDto createVitals(UUID encounterId, CreateVitalsRequestDto requestDto) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Recording vitals on encounter [{}] by user [{}]", encounterId, currentUserId);

        EncounterEntity encounter = encounterRepository.findByIdWithDetails(encounterId)
                .orElseThrow(() -> new ResourceNotFoundException("Encounter not found with ID: " + encounterId));

        validateProviderOrHospitalAccess(currentUserId, encounter);

        if (encounter.getStatus() == EncounterStatus.CANCELLED) {
            throw new BadRequestException("Cannot record vitals on a CANCELLED encounter");
        }

        BigDecimal bmi = requestDto.getBmi();
        if (bmi == null && requestDto.getWeight() != null && requestDto.getHeight() != null) {
            bmi = calculateBmi(requestDto.getWeight(), requestDto.getHeight());
        }

        Instant measuredAt = requestDto.getMeasuredAt() != null ? requestDto.getMeasuredAt() : Instant.now();

        VitalsEntity entity = new VitalsEntity(
                UUID.randomUUID(),
                encounter,
                encounter.getPatient(),
                measuredAt,
                requestDto.getTemperature(),
                requestDto.getHeartRate(),
                requestDto.getRespiratoryRate(),
                requestDto.getSystolicBloodPressure(),
                requestDto.getDiastolicBloodPressure(),
                requestDto.getOxygenSaturation(),
                requestDto.getWeight(),
                requestDto.getHeight(),
                bmi,
                requestDto.getBloodGlucose(),
                requestDto.getNotes() != null ? requestDto.getNotes().trim() : null,
                currentUserId
        );

        VitalsEntity saved = vitalsRepository.save(entity);
        log.info("Vitals recorded successfully with ID [{}]", saved.getId());
        return vitalsMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VitalsResponseDto> getVitalsByEncounterId(UUID encounterId) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();

        EncounterEntity encounter = encounterRepository.findByIdWithDetails(encounterId)
                .orElseThrow(() -> new ResourceNotFoundException("Encounter not found with ID: " + encounterId));

        validateAccess(currentUserId, encounter);

        List<VitalsEntity> vitalsList = vitalsRepository.findByEncounterId(encounterId);
        return vitalsList.stream()
                .map(vitalsMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateBmi(BigDecimal weightKg, BigDecimal heightCm) {
        try {
            if (heightCm.compareTo(BigDecimal.ZERO) <= 0 || weightKg.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            BigDecimal heightMeters = heightCm.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            BigDecimal heightSquared = heightMeters.multiply(heightMeters);
            return weightKg.divide(heightSquared, 1, RoundingMode.HALF_UP);
        } catch (ArithmeticException ex) {
            log.warn("Could not calculate BMI due to arithmetic error: {}", ex.getMessage());
            return null;
        }
    }

    private void validateAccess(String currentUserId, EncounterEntity encounter) {
        boolean isPatient = encounter.getPatient() != null && currentUserId.equals(encounter.getPatient().getAuthUserId());
        boolean isProvider = isAuthorizedForProvider(currentUserId, encounter.getProvider());
        boolean isHospital = isAuthorizedForHospital(currentUserId, encounter.getHospital());

        if (!isPatient && !isProvider && !isHospital) {
            throw new ForbiddenException("Access denied: You are not authorized to view physiological vitals for this encounter");
        }
    }

    private void validateProviderOrHospitalAccess(String currentUserId, EncounterEntity encounter) {
        boolean isProvider = isAuthorizedForProvider(currentUserId, encounter.getProvider());
        boolean isHospital = isAuthorizedForHospital(currentUserId, encounter.getHospital());

        if (!isProvider && !isHospital) {
            throw new ForbiddenException("Access denied: Only attending healthcare providers or clinical staff may record vitals");
        }
    }

    private boolean isAuthorizedForProvider(String currentUserId, ProviderEntity provider) {
        if (currentUserId == null || currentUserId.isBlank() || provider == null) {
            return false;
        }
        return provider.getAuthUserId() != null && provider.getAuthUserId().equals(currentUserId);
    }

    private boolean isAuthorizedForHospital(String currentUserId, HospitalEntity hospital) {
        if (currentUserId == null || currentUserId.isBlank() || hospital == null || hospital.getId() == null) {
            return false;
        }
        return hospitalStaffRepository.existsByHospitalIdAndAuthUserIdAndActiveTrue(hospital.getId(), currentUserId);
    }
}
