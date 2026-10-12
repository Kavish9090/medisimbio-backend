package com.medisimbio.backend.patient.service.impl;

import com.medisimbio.backend.common.exception.ResourceAlreadyExistsException;
import com.medisimbio.backend.common.exception.ResourceNotFoundException;
import com.medisimbio.backend.medid.dto.MedIdResponseDto;
import com.medisimbio.backend.medid.service.MedIdService;
import com.medisimbio.backend.patient.dto.CreatePatientRequestDto;
import com.medisimbio.backend.patient.dto.PatientResponseDto;
import com.medisimbio.backend.patient.dto.UpdatePatientRequestDto;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.patient.mapper.PatientMapper;
import com.medisimbio.backend.patient.repository.PatientRepository;
import com.medisimbio.backend.patient.service.PatientService;
import com.medisimbio.backend.security.AuthenticationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authoritative implementation of PatientService.
 */
@Service
public class PatientServiceImpl implements PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientServiceImpl.class);

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final AuthenticationFacade authenticationFacade;
    private final MedIdService medIdService;

    public PatientServiceImpl(PatientRepository patientRepository,
                              PatientMapper patientMapper,
                              AuthenticationFacade authenticationFacade,
                              MedIdService medIdService) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
        this.authenticationFacade = authenticationFacade;
        this.medIdService = medIdService;
    }

    @Override
    @Transactional
    public PatientResponseDto createPatientProfile(CreatePatientRequestDto requestDto) {
        String authUserId = authenticationFacade.getRequiredCurrentUserId();

        // 1. Check if profile already exists for this authenticated user
        if (patientRepository.existsByAuthUserId(authUserId)) {
            throw new ResourceAlreadyExistsException("Patient profile already exists for authenticated user: " + authUserId);
        }

        // 2. Check for unique email conflict
        String email = requestDto.getEmail().trim().toLowerCase();
        if (patientRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Patient", "email", email);
        }

        // 3. Check for unique mobile number conflict
        String mobile = requestDto.getMobileNumber().trim();
        if (patientRepository.existsByMobileNumber(mobile)) {
            throw new ResourceAlreadyExistsException("Patient", "mobileNumber", mobile);
        }

        // 4. Transform and persist entity
        PatientEntity entity = patientMapper.toEntity(requestDto, authUserId);
        PatientEntity saved = patientRepository.save(entity);

        log.info("Created new patient profile [ID: {}] for auth user [{}]", saved.getId(), authUserId);
        return patientMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDto getCurrentPatientProfile() {
        String authUserId = authenticationFacade.getRequiredCurrentUserId();

        PatientEntity patient = patientRepository.findByAuthUserIdWithMedId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for authenticated user: " + authUserId));

        return patientMapper.toResponseDto(patient);
    }

    @Override
    @Transactional
    public PatientResponseDto updateCurrentPatientProfile(UpdatePatientRequestDto requestDto) {
        String authUserId = authenticationFacade.getRequiredCurrentUserId();

        PatientEntity patient = patientRepository.findByAuthUserIdWithMedId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for authenticated user: " + authUserId));

        patientMapper.updateEntityFromDto(requestDto, patient);
        PatientEntity updated = patientRepository.save(patient);

        log.info("Updated patient profile [ID: {}] for auth user [{}]", updated.getId(), authUserId);
        return patientMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public MedIdResponseDto generateOrGetMedIdForCurrentPatient() {
        String authUserId = authenticationFacade.getRequiredCurrentUserId();

        PatientEntity patient = patientRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for authenticated user. Please create a profile first."));

        return medIdService.generateOrGetMedIdForPatient(patient.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public MedIdResponseDto getMedIdForCurrentPatient() {
        String authUserId = authenticationFacade.getRequiredCurrentUserId();

        PatientEntity patient = patientRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for authenticated user: " + authUserId));

        return medIdService.getMedIdForPatient(patient.getId());
    }
}
