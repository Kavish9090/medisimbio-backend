package com.medisimbio.backend.medicalrecord.service.impl;

import com.medisimbio.backend.common.exception.BusinessException;
import com.medisimbio.backend.diagnosis.dto.DiagnosisResponseDto;
import com.medisimbio.backend.diagnosis.entity.DiagnosisEntity;
import com.medisimbio.backend.diagnosis.entity.DiagnosisStatus;
import com.medisimbio.backend.diagnosis.mapper.DiagnosisMapper;
import com.medisimbio.backend.diagnosis.repository.DiagnosisRepository;
import com.medisimbio.backend.encounter.dto.EncounterSummaryDto;
import com.medisimbio.backend.encounter.entity.EncounterEntity;
import com.medisimbio.backend.encounter.mapper.EncounterMapper;
import com.medisimbio.backend.encounter.repository.EncounterRepository;
import com.medisimbio.backend.encounter.repository.EncounterSpecification;
import com.medisimbio.backend.medicalrecord.dto.*;
import com.medisimbio.backend.medicalrecord.repository.MedicalTimelineRepository;
import com.medisimbio.backend.medicalrecord.service.MedicalRecordService;
import com.medisimbio.backend.patient.dto.PatientResponseDto;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.patient.mapper.PatientMapper;
import com.medisimbio.backend.patient.repository.PatientRepository;
import com.medisimbio.backend.security.AuthenticationFacade;
import com.medisimbio.backend.vitals.dto.VitalsResponseDto;
import com.medisimbio.backend.vitals.entity.VitalsEntity;
import com.medisimbio.backend.vitals.mapper.VitalsMapper;
import com.medisimbio.backend.vitals.repository.VitalsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of MedicalRecordService aggregating clinical domain entities into a unified read model.
 */
@Service
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private static final Logger log = LoggerFactory.getLogger(MedicalRecordServiceImpl.class);

    private final PatientRepository patientRepository;
    private final EncounterRepository encounterRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final VitalsRepository vitalsRepository;
    private final MedicalTimelineRepository medicalTimelineRepository;
    private final AuthenticationFacade authenticationFacade;
    private final PatientMapper patientMapper;
    private final EncounterMapper encounterMapper;
    private final DiagnosisMapper diagnosisMapper;
    private final VitalsMapper vitalsMapper;

    public MedicalRecordServiceImpl(PatientRepository patientRepository,
                                  EncounterRepository encounterRepository,
                                  DiagnosisRepository diagnosisRepository,
                                  VitalsRepository vitalsRepository,
                                  MedicalTimelineRepository medicalTimelineRepository,
                                  AuthenticationFacade authenticationFacade,
                                  PatientMapper patientMapper,
                                  EncounterMapper encounterMapper,
                                  DiagnosisMapper diagnosisMapper,
                                  VitalsMapper vitalsMapper) {
        this.patientRepository = patientRepository;
        this.encounterRepository = encounterRepository;
        this.diagnosisRepository = diagnosisRepository;
        this.vitalsRepository = vitalsRepository;
        this.medicalTimelineRepository = medicalTimelineRepository;
        this.authenticationFacade = authenticationFacade;
        this.patientMapper = patientMapper;
        this.encounterMapper = encounterMapper;
        this.diagnosisMapper = diagnosisMapper;
        this.vitalsMapper = vitalsMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordOverviewDto getPatientMedicalRecordOverview() {
        PatientEntity patient = getAuthenticatedPatient();
        log.info("Generating medical record overview for patient [{}]", patient.getId());

        // 1. Patient Profile DTO
        PatientResponseDto patientDto = patientMapper.toResponseDto(patient);

        // 2. Recent Encounters
        Specification<EncounterEntity> encSpec = EncounterSpecification.withFilters(
                patient.getId(), null, null, null, null, null, null);
        Page<EncounterEntity> encounterPage = encounterRepository.findAll(
                encSpec, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<EncounterSummaryDto> encounterSummaries = encounterPage.getContent().stream()
                .map(encounterMapper::toSummaryDto)
                .collect(Collectors.toList());

        // 3. Active Diagnoses
        List<DiagnosisEntity> activeDiagnosesList = diagnosisRepository.findByPatientIdAndStatus(
                patient.getId(), DiagnosisStatus.ACTIVE);
        List<DiagnosisResponseDto> activeDiagnoses = activeDiagnosesList.stream()
                .map(diagnosisMapper::toResponseDto)
                .collect(Collectors.toList());

        // 4. Latest Vitals (single element lookup without full table load)
        Optional<VitalsEntity> latestVitalsEntity = vitalsRepository.findFirstByPatientIdOrderByMeasuredAtDesc(patient.getId());
        VitalsResponseDto latestVitals = latestVitalsEntity.map(vitalsMapper::toResponseDto).orElse(null);

        // 5. Aggregate totals via database counts
        long totalEncounters = encounterRepository.count(encSpec);
        long totalDiagnoses = diagnosisRepository.countByPatientId(patient.getId());

        return new MedicalRecordOverviewDto(
                patientDto,
                encounterSummaries,
                activeDiagnoses,
                latestVitals,
                totalEncounters,
                totalDiagnoses
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalTimelineResponseDto getPatientMedicalTimeline(int page, int size) {
        if (page < 0) {
            page = 0;
        }
        if (size <= 0) {
            size = 20;
        }
        if (size > 100) {
            size = 100;
        }

        PatientEntity patient = getAuthenticatedPatient();
        log.info("Generating database-backed chronological medical timeline for patient [{}], page [{}], size [{}]",
                patient.getId(), page, size);

        int offset = page * size;
        long totalElements = medicalTimelineRepository.countPatientTimelineEvents(patient.getId());
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<MedicalTimelineEventProjection> projections = totalElements > 0 && offset < totalElements
                ? medicalTimelineRepository.findPatientTimelineEvents(patient.getId(), size, offset)
                : Collections.emptyList();

        List<MedicalTimelineEventDto> pageContent = projections.stream()
                .map(p -> new MedicalTimelineEventDto(
                        TimelineEventType.valueOf(p.getEventType()),
                        toUuid(p.getEventId()),
                        toInstant(p.getOccurredAt()),
                        p.getTitle(),
                        p.getSummary() != null && !p.getSummary().isBlank() ? p.getSummary() : "Clinical Record",
                        toUuid(p.getEncounterId()),
                        p.getEncounterReference(),
                        toUuid(p.getProviderId()),
                        p.getProviderName(),
                        toUuid(p.getHospitalId()),
                        p.getHospitalName(),
                        p.getStatus()
                ))
                .collect(Collectors.toList());

        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;

        String medIdNumber = patient.getMedId() != null ? patient.getMedId().getMedIdNumber() : null;

        return new MedicalTimelineResponseDto(
                patient.getId(),
                patient.getFullName(),
                medIdNumber,
                pageContent,
                page,
                size,
                (int) totalElements,
                totalPages,
                hasNext,
                hasPrevious
        );
    }

    private PatientEntity getAuthenticatedPatient() {
        String authUserId = authenticationFacade.getRequiredCurrentUserId();
        return patientRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException("Patient profile not found. Please create your profile before viewing medical records."));
    }

    private UUID toUuid(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof UUID uuid) {
            return uuid;
        }
        if (obj instanceof byte[] bytes) {
            if (bytes.length == 16) {
                java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(bytes);
                return new UUID(bb.getLong(), bb.getLong());
            }
            return UUID.fromString(new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
        }
        return UUID.fromString(obj.toString());
    }

    private Instant toInstant(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Instant inst) {
            return inst;
        }
        if (obj instanceof java.sql.Timestamp ts) {
            return ts.toInstant();
        }
        if (obj instanceof java.time.OffsetDateTime odt) {
            return odt.toInstant();
        }
        if (obj instanceof java.time.LocalDateTime ldt) {
            return ldt.atZone(java.time.ZoneId.systemDefault()).toInstant();
        }
        return Instant.parse(obj.toString());
    }
}
