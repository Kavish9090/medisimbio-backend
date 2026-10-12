package com.medisimbio.backend.encounter.service.impl;

import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.common.exception.*;
import com.medisimbio.backend.common.response.PageResponse;
import com.medisimbio.backend.encounter.dto.*;
import com.medisimbio.backend.encounter.entity.EncounterEntity;
import com.medisimbio.backend.encounter.entity.EncounterStatus;
import com.medisimbio.backend.encounter.entity.EncounterType;
import com.medisimbio.backend.encounter.generator.EncounterReferenceGenerator;
import com.medisimbio.backend.encounter.mapper.EncounterMapper;
import com.medisimbio.backend.encounter.repository.EncounterRepository;
import com.medisimbio.backend.encounter.repository.EncounterSpecification;
import com.medisimbio.backend.encounter.service.EncounterService;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.repository.HospitalStaffRepository;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.patient.repository.PatientRepository;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.provider.repository.ProviderRepository;
import com.medisimbio.backend.security.AuthenticationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Implementation of EncounterService for clinical consultation session management.
 */
@Service
public class EncounterServiceImpl implements EncounterService {

    private static final Logger log = LoggerFactory.getLogger(EncounterServiceImpl.class);

    private final EncounterRepository encounterRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final ProviderRepository providerRepository;
    private final HospitalStaffRepository hospitalStaffRepository;
    private final AuthenticationFacade authenticationFacade;
    private final EncounterReferenceGenerator referenceGenerator;
    private final EncounterMapper encounterMapper;
    private final Clock clock;

    @Autowired
    public EncounterServiceImpl(EncounterRepository encounterRepository,
                                AppointmentRepository appointmentRepository,
                                PatientRepository patientRepository,
                                ProviderRepository providerRepository,
                                HospitalStaffRepository hospitalStaffRepository,
                                AuthenticationFacade authenticationFacade,
                                EncounterReferenceGenerator referenceGenerator,
                                EncounterMapper encounterMapper) {
        this(encounterRepository, appointmentRepository, patientRepository, providerRepository,
                hospitalStaffRepository, authenticationFacade, referenceGenerator, encounterMapper, Clock.systemDefaultZone());
    }

    public EncounterServiceImpl(EncounterRepository encounterRepository,
                                AppointmentRepository appointmentRepository,
                                PatientRepository patientRepository,
                                ProviderRepository providerRepository,
                                HospitalStaffRepository hospitalStaffRepository,
                                AuthenticationFacade authenticationFacade,
                                EncounterReferenceGenerator referenceGenerator,
                                EncounterMapper encounterMapper,
                                Clock clock) {
        this.encounterRepository = encounterRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.providerRepository = providerRepository;
        this.hospitalStaffRepository = hospitalStaffRepository;
        this.authenticationFacade = authenticationFacade;
        this.referenceGenerator = referenceGenerator;
        this.encounterMapper = encounterMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public EncounterResponseDto createEncounter(CreateEncounterRequestDto requestDto) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Creating encounter for appointment [{}] by user [{}]", requestDto.getAppointmentId(), currentUserId);

        AppointmentEntity appointment = appointmentRepository.findByIdWithDetails(requestDto.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + requestDto.getAppointmentId()));

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new BadRequestException("Cannot create an encounter for a CANCELLED appointment");
        }
        if (appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new BadRequestException("Cannot create an encounter for a NO_SHOW appointment");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Cannot create an encounter for an already COMPLETED appointment");
        }

        if (encounterRepository.existsByAppointmentId(appointment.getId())) {
            throw new ResourceAlreadyExistsException("A clinical encounter already exists for this appointment");
        }

        // Authorization: Verify caller is the patient, the doctor, or affiliated hospital staff
        boolean isPatient = appointment.getPatient() != null && currentUserId.equals(appointment.getPatient().getAuthUserId());
        boolean isProvider = isAuthorizedForProvider(currentUserId, appointment.getProvider());
        boolean isHospital = isAuthorizedForHospital(currentUserId, appointment.getHospital());

        if (!isPatient && !isProvider && !isHospital) {
            throw new ForbiddenException("Access denied: You are not authorized to create an encounter for this appointment");
        }

        String reference = generateUniqueReference();
        Instant startedAt = requestDto.getStartedAt() != null ? requestDto.getStartedAt() : Instant.now(clock);

        String chiefComplaint = requestDto.getChiefComplaint() != null && !requestDto.getChiefComplaint().isBlank()
                ? requestDto.getChiefComplaint().trim()
                : appointment.getVisitReason();

        EncounterEntity entity = new EncounterEntity(
                UUID.randomUUID(),
                reference,
                appointment.getPatient(),
                appointment.getProvider(),
                appointment.getHospital(),
                appointment,
                requestDto.getEncounterType() != null ? requestDto.getEncounterType() : EncounterType.INITIAL_CONSULTATION,
                EncounterStatus.OPEN,
                startedAt,
                chiefComplaint,
                currentUserId
        );

        try {
            EncounterEntity saved = encounterRepository.saveAndFlush(entity);
            log.info("Encounter created successfully with reference [{}] and ID [{}]", saved.getEncounterReference(), saved.getId());
            return encounterMapper.toResponseDto(saved);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Concurrent duplicate encounter creation detected: {}", ex.getMessage());
            throw new ResourceAlreadyExistsException("A clinical encounter already exists for this appointment");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EncounterResponseDto getEncounterById(UUID encounterId) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();

        EncounterEntity entity = encounterRepository.findByIdWithDetails(encounterId)
                .orElseThrow(() -> new ResourceNotFoundException("Encounter not found with ID: " + encounterId));

        validateAccess(currentUserId, entity);
        return encounterMapper.toResponseDto(entity);
    }

    @Override
    @Transactional
    public EncounterResponseDto updateEncounter(UUID encounterId, UpdateEncounterRequestDto requestDto) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Updating clinical notes on encounter [{}] by user [{}]", encounterId, currentUserId);

        EncounterEntity entity = encounterRepository.findByIdWithDetails(encounterId)
                .orElseThrow(() -> new ResourceNotFoundException("Encounter not found with ID: " + encounterId));

        validateProviderOrHospitalAccess(currentUserId, entity);

        if (!entity.getStatus().canModifyClinicalNotes()) {
            throw new BadRequestException("Cannot update clinical notes on an encounter with status: " + entity.getStatus());
        }

        if (requestDto.getChiefComplaint() != null) {
            entity.setChiefComplaint(requestDto.getChiefComplaint().trim());
        }
        if (requestDto.getClinicalNotes() != null) {
            entity.setClinicalNotes(requestDto.getClinicalNotes().trim());
        }
        if (requestDto.getDiagnosisSummary() != null) {
            entity.setDiagnosisSummary(requestDto.getDiagnosisSummary().trim());
        }
        if (requestDto.getTreatmentPlan() != null) {
            entity.setTreatmentPlan(requestDto.getTreatmentPlan().trim());
        }
        if (requestDto.getFollowUpInstructions() != null) {
            entity.setFollowUpInstructions(requestDto.getFollowUpInstructions().trim());
        }

        entity.setUpdatedBy(currentUserId);
        EncounterEntity updated = encounterRepository.save(entity);
        return encounterMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public EncounterResponseDto startEncounter(UUID encounterId) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Starting clinical encounter [{}] by user [{}]", encounterId, currentUserId);

        EncounterEntity entity = encounterRepository.findByIdWithDetails(encounterId)
                .orElseThrow(() -> new ResourceNotFoundException("Encounter not found with ID: " + encounterId));

        validateProviderOrHospitalAccess(currentUserId, entity);

        if (!entity.getStatus().canStart()) {
            throw new BadRequestException("Encounter cannot be started from status: " + entity.getStatus());
        }

        entity.setStatus(EncounterStatus.IN_PROGRESS);
        if (entity.getStartedAt() == null) {
            entity.setStartedAt(Instant.now(clock));
        }
        entity.setUpdatedBy(currentUserId);

        EncounterEntity saved = encounterRepository.save(entity);
        return encounterMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public EncounterResponseDto completeEncounter(UUID encounterId) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Completing clinical encounter [{}] by user [{}]", encounterId, currentUserId);

        EncounterEntity entity = encounterRepository.findByIdWithDetails(encounterId)
                .orElseThrow(() -> new ResourceNotFoundException("Encounter not found with ID: " + encounterId));

        validateProviderOrHospitalAccess(currentUserId, entity);

        if (!entity.getStatus().canComplete()) {
            throw new BadRequestException("Encounter cannot be completed from status: " + entity.getStatus());
        }

        entity.setStatus(EncounterStatus.COMPLETED);
        entity.setEndedAt(Instant.now(clock));
        entity.setUpdatedBy(currentUserId);

        // Synchronize appointment lifecycle
        if (entity.getAppointment() != null) {
            AppointmentEntity appointment = entity.getAppointment();
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
        }

        EncounterEntity saved = encounterRepository.save(entity);
        return encounterMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public EncounterResponseDto cancelEncounter(UUID encounterId, String cancellationReason) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Cancelling clinical encounter [{}] by user [{}]", encounterId, currentUserId);

        EncounterEntity entity = encounterRepository.findByIdWithDetails(encounterId)
                .orElseThrow(() -> new ResourceNotFoundException("Encounter not found with ID: " + encounterId));

        validateAccess(currentUserId, entity);

        if (!entity.getStatus().canCancel()) {
            throw new BadRequestException("Encounter cannot be cancelled from status: " + entity.getStatus());
        }

        entity.setStatus(EncounterStatus.CANCELLED);
        if (entity.getEndedAt() == null) {
            entity.setEndedAt(Instant.now(clock));
        }
        if (cancellationReason != null && !cancellationReason.isBlank()) {
            String note = (entity.getClinicalNotes() != null ? entity.getClinicalNotes() + "\n" : "") +
                    "[Cancellation Reason: " + cancellationReason.trim() + "]";
            entity.setClinicalNotes(note);
        }
        entity.setUpdatedBy(currentUserId);

        EncounterEntity saved = encounterRepository.save(entity);
        return encounterMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EncounterSummaryDto> getPatientEncounters(
            EncounterStatus status, EncounterType encounterType, Instant fromDate, Instant toDate, Pageable pageable) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        PatientEntity patient = patientRepository.findByAuthUserId(currentUserId)
                .orElseThrow(() -> new BusinessException("Patient profile not found for authenticated user"));

        Specification<EncounterEntity> spec = EncounterSpecification.withFilters(
                patient.getId(), null, null, status, encounterType, fromDate, toDate);

        Page<EncounterEntity> page = encounterRepository.findAll(spec, pageable);
        Page<EncounterSummaryDto> dtoPage = page.map(encounterMapper::toSummaryDto);
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EncounterSummaryDto> getProviderEncounters(
            UUID providerId, EncounterStatus status, EncounterType encounterType, Instant fromDate, Instant toDate, Pageable pageable) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();

        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + providerId));

        if (!isAuthorizedForProvider(currentUserId, provider)) {
            throw new ForbiddenException("Access denied: You are not authorized to view encounters for this provider");
        }

        Specification<EncounterEntity> spec = EncounterSpecification.withFilters(
                null, providerId, null, status, encounterType, fromDate, toDate);

        Page<EncounterEntity> page = encounterRepository.findAll(spec, pageable);
        Page<EncounterSummaryDto> dtoPage = page.map(encounterMapper::toSummaryDto);
        return PageResponse.from(dtoPage);
    }

    private void validateAccess(String currentUserId, EncounterEntity entity) {
        boolean isPatient = entity.getPatient() != null && currentUserId.equals(entity.getPatient().getAuthUserId());
        boolean isProvider = isAuthorizedForProvider(currentUserId, entity.getProvider());
        boolean isHospital = isAuthorizedForHospital(currentUserId, entity.getHospital());

        if (!isPatient && !isProvider && !isHospital) {
            throw new ForbiddenException("Access denied: You are not authorized to view or access this clinical encounter");
        }
    }

    private void validateProviderOrHospitalAccess(String currentUserId, EncounterEntity entity) {
        boolean isProvider = isAuthorizedForProvider(currentUserId, entity.getProvider());
        boolean isHospital = isAuthorizedForHospital(currentUserId, entity.getHospital());

        if (!isProvider && !isHospital) {
            throw new ForbiddenException("Access denied: Only attending healthcare providers or clinical staff may perform this action");
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

    private String generateUniqueReference() {
        for (int i = 0; i < 10; i++) {
            String ref = referenceGenerator.generateReference();
            if (!encounterRepository.existsByEncounterReference(ref)) {
                return ref;
            }
        }
        return "ENC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
