package com.medisimbio.backend.prescription.service.impl;

import com.medisimbio.backend.common.exception.BadRequestException;
import com.medisimbio.backend.common.exception.ConflictException;
import com.medisimbio.backend.common.exception.ForbiddenException;
import com.medisimbio.backend.common.exception.ResourceNotFoundException;
import com.medisimbio.backend.dispensing.entity.DispensingOrderStatus;
import com.medisimbio.backend.dispensing.repository.DispensingOrderRepository;
import com.medisimbio.backend.encounter.entity.EncounterEntity;
import com.medisimbio.backend.encounter.entity.EncounterStatus;
import com.medisimbio.backend.encounter.repository.EncounterRepository;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.repository.HospitalStaffRepository;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.patient.repository.PatientRepository;
import com.medisimbio.backend.prescription.dto.*;
import com.medisimbio.backend.prescription.entity.PrescriptionEntity;
import com.medisimbio.backend.prescription.entity.PrescriptionItemEntity;
import com.medisimbio.backend.prescription.entity.PrescriptionStatus;
import com.medisimbio.backend.prescription.mapper.PrescriptionMapper;
import com.medisimbio.backend.prescription.repository.PrescriptionRepository;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.security.AuthenticationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PrescriptionServiceImpl implements com.medisimbio.backend.prescription.service.PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionServiceImpl.class);

    private final PrescriptionRepository prescriptionRepository;
    private final EncounterRepository encounterRepository;
    private final PatientRepository patientRepository;
    private final HospitalStaffRepository hospitalStaffRepository;
    private final AuthenticationFacade authenticationFacade;
    private final PrescriptionMapper prescriptionMapper;
    private final DispensingOrderRepository dispensingOrderRepository;

    public PrescriptionServiceImpl(PrescriptionRepository prescriptionRepository,
                                   EncounterRepository encounterRepository,
                                   PatientRepository patientRepository,
                                   HospitalStaffRepository hospitalStaffRepository,
                                   AuthenticationFacade authenticationFacade,
                                   PrescriptionMapper prescriptionMapper,
                                   DispensingOrderRepository dispensingOrderRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.encounterRepository = encounterRepository;
        this.patientRepository = patientRepository;
        this.hospitalStaffRepository = hospitalStaffRepository;
        this.authenticationFacade = authenticationFacade;
        this.prescriptionMapper = prescriptionMapper;
        this.dispensingOrderRepository = dispensingOrderRepository;
    }

    @Override
    @Transactional
    public PrescriptionResponseDto createPrescription(UUID encounterId, CreatePrescriptionRequestDto requestDto) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Creating prescription for encounter [{}] by user [{}]", encounterId, currentUserId);

        EncounterEntity encounter = encounterRepository.findByIdWithDetails(encounterId)
                .orElseThrow(() -> new ResourceNotFoundException("Encounter not found with ID: " + encounterId));

        if (encounter.getStatus() == EncounterStatus.CANCELLED) {
            throw new BadRequestException("Cannot create a prescription on a CANCELLED encounter");
        }

        validateAuthoringProvider(currentUserId, encounter);

        if (requestDto.items() == null || requestDto.items().isEmpty()) {
            throw new BadRequestException("Prescription must contain at least one medicine item");
        }

        boolean finalize = Boolean.TRUE.equals(requestDto.finalizeImmediately());
        Instant now = Instant.now();

        PrescriptionEntity prescription = new PrescriptionEntity(
                UUID.randomUUID(),
                generatePrescriptionReference(),
                encounter.getPatient(),
                encounter,
                encounter.getProvider(),
                encounter.getHospital(),
                PrescriptionStatus.DRAFT,
                now,
                requestDto.validUntil(),
                requestDto.clinicalInstructions() != null ? requestDto.clinicalInstructions().trim() : null,
                1,
                null,
                currentUserId
        );

        int seq = 1;
        for (PrescriptionItemRequestDto itemDto : requestDto.items()) {
            PrescriptionItemEntity item = mapItemDtoToEntity(itemDto, prescription, seq++);
            prescription.addItem(item);
        }

        if (finalize) {
            prescription.setStatus(PrescriptionStatus.FINALIZED);
            prescription.setFinalizedAt(now);
        }

        PrescriptionEntity saved = prescriptionRepository.save(prescription);
        log.info("Prescription [{}] created with status [{}] and reference [{}]",
                saved.getId(), saved.getStatus(), saved.getPrescriptionReference());

        return prescriptionMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PrescriptionResponseDto getPrescriptionById(UUID prescriptionId) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        PrescriptionEntity prescription = prescriptionRepository.findByIdWithDetails(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + prescriptionId));

        validateReadAccess(currentUserId, prescription);
        return prescriptionMapper.toResponseDto(prescription);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionSummaryDto> getPrescriptionsByEncounter(UUID encounterId, Pageable pageable) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        EncounterEntity encounter = encounterRepository.findByIdWithDetails(encounterId)
                .orElseThrow(() -> new ResourceNotFoundException("Encounter not found with ID: " + encounterId));

        boolean isPatient = encounter.getPatient() != null && currentUserId.equals(encounter.getPatient().getAuthUserId());
        boolean isProvider = isAuthorizedForProvider(currentUserId, encounter.getProvider());
        boolean isHospital = isAuthorizedForHospital(currentUserId, encounter.getHospital());

        if (!isPatient && !isProvider && !isHospital) {
            throw new ForbiddenException("Access denied: You are not authorized to view prescriptions for this encounter");
        }

        if (isPatient) {
            // Patients only see finalized, superseded, or cancelled prescriptions; draft prescriptions are not exposed
            List<PrescriptionStatus> visibleStatuses = List.of(
                    PrescriptionStatus.FINALIZED,
                    PrescriptionStatus.SUPERSEDED,
                    PrescriptionStatus.CANCELLED
            );
            Page<PrescriptionEntity> page = prescriptionRepository.findByEncounterIdAndStatusIn(encounterId, visibleStatuses, pageable);
            return page.map(prescriptionMapper::toSummaryDto);
        }

        Page<PrescriptionEntity> page = prescriptionRepository.findByEncounterId(encounterId, pageable);
        return page.map(prescriptionMapper::toSummaryDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PrescriptionSummaryDto> getPatientPrescriptions(Pageable pageable) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        PatientEntity patient = patientRepository.findByAuthUserId(currentUserId)
                .orElseThrow(() -> new ForbiddenException("Patient profile not found for authenticated user"));

        // Only return finalized, superseded, or cancelled prescriptions for patient self-service
        List<PrescriptionStatus> visibleStatuses = List.of(
                PrescriptionStatus.FINALIZED,
                PrescriptionStatus.SUPERSEDED,
                PrescriptionStatus.CANCELLED
        );

        Page<PrescriptionEntity> page = prescriptionRepository.findByPatientIdAndStatusIn(
                patient.getId(), visibleStatuses, pageable);

        return page.map(prescriptionMapper::toSummaryDto);
    }

    @Override
    @Transactional
    public PrescriptionResponseDto updateDraftPrescription(UUID prescriptionId, UpdatePrescriptionRequestDto requestDto) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Updating draft prescription [{}] by user [{}]", prescriptionId, currentUserId);

        PrescriptionEntity prescription = prescriptionRepository.findByIdWithDetails(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + prescriptionId));

        validateAuthoringProvider(currentUserId, prescription.getEncounter());

        if (prescription.getStatus() != PrescriptionStatus.DRAFT) {
            throw new ConflictException("Cannot edit prescription: status is " + prescription.getStatus()
                    + ". Only DRAFT prescriptions may be modified in place.");
        }

        if (requestDto.validUntil() != null) {
            prescription.setValidUntil(requestDto.validUntil());
        }
        if (requestDto.clinicalInstructions() != null) {
            prescription.setClinicalInstructions(requestDto.clinicalInstructions().trim());
        }

        if (requestDto.items() != null && !requestDto.items().isEmpty()) {
            prescription.clearItems();
            int seq = 1;
            for (PrescriptionItemRequestDto itemDto : requestDto.items()) {
                PrescriptionItemEntity item = mapItemDtoToEntity(itemDto, prescription, seq++);
                prescription.addItem(item);
            }
        }

        prescription.setUpdatedBy(currentUserId);
        PrescriptionEntity saved = prescriptionRepository.save(prescription);
        return prescriptionMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public PrescriptionResponseDto finalizePrescription(UUID prescriptionId) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Finalizing prescription [{}] by user [{}]", prescriptionId, currentUserId);

        PrescriptionEntity prescription = prescriptionRepository.findByIdWithDetails(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + prescriptionId));

        validateAuthoringProvider(currentUserId, prescription.getEncounter());

        if (prescription.getStatus() != PrescriptionStatus.DRAFT) {
            throw new ConflictException("Cannot finalize prescription: current status is " + prescription.getStatus()
                    + ". Only DRAFT prescriptions can be finalized.");
        }

        if (prescription.getItems() == null || prescription.getItems().isEmpty()) {
            throw new BadRequestException("Cannot finalize an empty prescription. At least one medicine item is required.");
        }

        prescription.setStatus(PrescriptionStatus.FINALIZED);
        prescription.setFinalizedAt(Instant.now());
        prescription.setUpdatedBy(currentUserId);

        PrescriptionEntity saved = prescriptionRepository.save(prescription);
        log.info("Prescription [{}] successfully finalized", saved.getId());
        return prescriptionMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public PrescriptionResponseDto cancelPrescription(UUID prescriptionId, CancelPrescriptionRequestDto requestDto) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Cancelling prescription [{}] by user [{}]", prescriptionId, currentUserId);

        PrescriptionEntity prescription = prescriptionRepository.findByIdWithDetails(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + prescriptionId));

        validateAuthoringProvider(currentUserId, prescription.getEncounter());

        if (prescription.getStatus() == PrescriptionStatus.CANCELLED) {
            throw new ConflictException("Prescription is already CANCELLED");
        }
        if (prescription.getStatus() == PrescriptionStatus.SUPERSEDED) {
            throw new ConflictException("Cannot cancel a SUPERSEDED prescription");
        }

        List<DispensingOrderStatus> activeStatuses = List.of(
                DispensingOrderStatus.PENDING,
                DispensingOrderStatus.ACCEPTED,
                DispensingOrderStatus.PARTIALLY_DISPENSED
        );
        if (dispensingOrderRepository.existsActiveOrderByPrescriptionId(prescriptionId, activeStatuses)) {
            throw new ConflictException("Cannot cancel prescription with an active dispensing order (PENDING, ACCEPTED, or PARTIALLY_DISPENSED)");
        }

        prescription.setStatus(PrescriptionStatus.CANCELLED);
        prescription.setCancellationReason(requestDto.reason().trim());
        prescription.setCancelledAt(Instant.now());
        prescription.setCancelledBy(currentUserId);
        prescription.setUpdatedBy(currentUserId);

        PrescriptionEntity saved = prescriptionRepository.save(prescription);
        log.info("Prescription [{}] successfully cancelled", saved.getId());
        return prescriptionMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public PrescriptionResponseDto replacePrescription(UUID prescriptionId, ReplacePrescriptionRequestDto requestDto) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Replacing prescription [{}] by user [{}]", prescriptionId, currentUserId);

        PrescriptionEntity original = prescriptionRepository.findByIdWithDetails(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Original prescription not found with ID: " + prescriptionId));

        validateAuthoringProvider(currentUserId, original.getEncounter());

        if (original.getStatus() != PrescriptionStatus.FINALIZED) {
            throw new ConflictException("Cannot replace prescription: status is " + original.getStatus()
                    + ". Only FINALIZED prescriptions can be replaced/superseded.");
        }

        // Prevent race conditions: verify this prescription has not already been superseded
        if (prescriptionRepository.existsByReplacesPrescriptionId(original.getId())) {
            throw new ConflictException("Prescription has already been replaced by a newer version");
        }

        List<DispensingOrderStatus> activeStatuses = List.of(
                DispensingOrderStatus.PENDING,
                DispensingOrderStatus.ACCEPTED,
                DispensingOrderStatus.PARTIALLY_DISPENSED
        );
        if (dispensingOrderRepository.existsActiveOrderByPrescriptionId(original.getId(), activeStatuses)) {
            throw new ConflictException("Cannot replace prescription with an active dispensing order (PENDING, ACCEPTED, or PARTIALLY_DISPENSED)");
        }

        if (requestDto.items() == null || requestDto.items().isEmpty()) {
            throw new BadRequestException("Replacement prescription must contain at least one medicine item");
        }

        // Mark original as SUPERSEDED
        original.setStatus(PrescriptionStatus.SUPERSEDED);
        original.setCancellationReason("Superseded by replacement: " + requestDto.reason().trim());
        original.setUpdatedBy(currentUserId);
        prescriptionRepository.save(original);

        // Create the replacement prescription
        boolean finalize = !Boolean.FALSE.equals(requestDto.finalizeImmediately());
        Instant now = Instant.now();

        PrescriptionEntity replacement = new PrescriptionEntity(
                UUID.randomUUID(),
                generatePrescriptionReference(),
                original.getPatient(),
                original.getEncounter(),
                original.getProvider(),
                original.getHospital(),
                PrescriptionStatus.DRAFT,
                now,
                requestDto.validUntil(),
                requestDto.clinicalInstructions() != null ? requestDto.clinicalInstructions().trim() : null,
                original.getVersionNumber() + 1,
                original,
                currentUserId
        );

        int seq = 1;
        for (PrescriptionItemRequestDto itemDto : requestDto.items()) {
            PrescriptionItemEntity item = mapItemDtoToEntity(itemDto, replacement, seq++);
            replacement.addItem(item);
        }

        if (finalize) {
            replacement.setStatus(PrescriptionStatus.FINALIZED);
            replacement.setFinalizedAt(now);
        }

        PrescriptionEntity savedReplacement = prescriptionRepository.save(replacement);
        log.info("Prescription [{}] superseded by replacement [{}] (version {})",
                original.getId(), savedReplacement.getId(), savedReplacement.getVersionNumber());

        return prescriptionMapper.toResponseDto(savedReplacement);
    }

    private void validateAuthoringProvider(String currentUserId, EncounterEntity encounter) {
        if (currentUserId == null || currentUserId.isBlank()) {
            throw new ForbiddenException("Authentication required");
        }

        // Explicit check: patients cannot author prescriptions
        if (encounter.getPatient() != null && currentUserId.equals(encounter.getPatient().getAuthUserId())) {
            throw new ForbiddenException("Patients are not permitted to author or modify clinical prescriptions");
        }

        ProviderEntity provider = encounter.getProvider();
        if (provider == null || provider.getAuthUserId() == null || !provider.getAuthUserId().equals(currentUserId)) {
            throw new ForbiddenException("Access denied: Only the attending healthcare provider assigned to this encounter may author or modify prescriptions");
        }
    }

    private void validateReadAccess(String currentUserId, PrescriptionEntity prescription) {
        boolean isPatient = prescription.getPatient() != null && currentUserId.equals(prescription.getPatient().getAuthUserId());
        boolean isProvider = isAuthorizedForProvider(currentUserId, prescription.getProvider());
        boolean isHospital = isAuthorizedForHospital(currentUserId, prescription.getHospital());

        if (!isPatient && !isProvider && !isHospital) {
            throw new ForbiddenException("Access denied: You are not authorized to view this prescription");
        }

        if (isPatient && prescription.getStatus() == PrescriptionStatus.DRAFT) {
            throw new ForbiddenException("Draft prescriptions are not accessible to patients until finalized by the provider");
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

    private PrescriptionItemEntity mapItemDtoToEntity(PrescriptionItemRequestDto dto, PrescriptionEntity parent, int seq) {
        return new PrescriptionItemEntity(
                UUID.randomUUID(),
                parent,
                dto.medicineName().trim(),
                dto.activeIngredient() != null ? dto.activeIngredient().trim() : null,
                dto.strength() != null ? dto.strength().trim() : null,
                dto.dosageForm() != null ? dto.dosageForm().trim() : null,
                dto.dose().trim(),
                dto.route().trim(),
                dto.frequency().trim(),
                dto.durationValue(),
                dto.durationUnit() != null ? dto.durationUnit().trim() : null,
                dto.quantity(),
                dto.quantityUnit() != null ? dto.quantityUnit().trim() : null,
                dto.refillsAllowed() != null ? dto.refillsAllowed() : 0,
                dto.instructions() != null ? dto.instructions().trim() : null,
                dto.sequenceNumber() != null ? dto.sequenceNumber() : seq
        );
    }

    private String generatePrescriptionReference() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "RX-" + datePart + "-" + randomPart;
    }
}
