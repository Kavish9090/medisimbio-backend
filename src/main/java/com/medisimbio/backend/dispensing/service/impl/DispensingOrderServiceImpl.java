package com.medisimbio.backend.dispensing.service.impl;

import com.medisimbio.backend.common.exception.BadRequestException;
import com.medisimbio.backend.common.exception.ConflictException;
import com.medisimbio.backend.common.exception.ForbiddenException;
import com.medisimbio.backend.common.exception.ResourceNotFoundException;
import com.medisimbio.backend.dispensing.dto.CreateDispensingOrderRequestDto;
import com.medisimbio.backend.dispensing.dto.DispensingOrderResponseDto;
import com.medisimbio.backend.dispensing.entity.DispensingOrderEntity;
import com.medisimbio.backend.dispensing.entity.DispensingOrderItemEntity;
import com.medisimbio.backend.dispensing.entity.DispensingOrderStatus;
import com.medisimbio.backend.dispensing.mapper.DispensingOrderMapper;
import com.medisimbio.backend.dispensing.repository.DispensingOrderRepository;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.repository.HospitalStaffRepository;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.patient.repository.PatientRepository;
import com.medisimbio.backend.prescription.entity.PrescriptionEntity;
import com.medisimbio.backend.prescription.entity.PrescriptionItemEntity;
import com.medisimbio.backend.prescription.entity.PrescriptionStatus;
import com.medisimbio.backend.prescription.repository.PrescriptionRepository;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.security.AuthenticationFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DispensingOrderServiceImpl implements com.medisimbio.backend.dispensing.service.DispensingOrderService {

    private static final Logger log = LoggerFactory.getLogger(DispensingOrderServiceImpl.class);

    private final DispensingOrderRepository dispensingOrderRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PatientRepository patientRepository;
    private final HospitalStaffRepository hospitalStaffRepository;
    private final AuthenticationFacade authenticationFacade;
    private final DispensingOrderMapper dispensingOrderMapper;

    public DispensingOrderServiceImpl(DispensingOrderRepository dispensingOrderRepository,
                                      PrescriptionRepository prescriptionRepository,
                                      PatientRepository patientRepository,
                                      HospitalStaffRepository hospitalStaffRepository,
                                      AuthenticationFacade authenticationFacade,
                                      DispensingOrderMapper dispensingOrderMapper) {
        this.dispensingOrderRepository = dispensingOrderRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.patientRepository = patientRepository;
        this.hospitalStaffRepository = hospitalStaffRepository;
        this.authenticationFacade = authenticationFacade;
        this.dispensingOrderMapper = dispensingOrderMapper;
    }

    @Override
    @Transactional
    public DispensingOrderResponseDto createDispensingOrder(UUID prescriptionId, CreateDispensingOrderRequestDto requestDto) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Creating dispensing order for prescription [{}] by user [{}]", prescriptionId, currentUserId);

        PrescriptionEntity prescription = prescriptionRepository.findByIdWithDetails(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with ID: " + prescriptionId));

        if (prescription.getStatus() != PrescriptionStatus.FINALIZED) {
            throw new BadRequestException("Cannot create a dispensing order for a prescription with status: "
                    + prescription.getStatus() + ". Only FINALIZED prescriptions can be ordered for dispensing.");
        }

        validateOrderingAccess(currentUserId, prescription);

        // Concurrency / Duplicate check: verify no active dispensing order already exists
        List<DispensingOrderStatus> activeStatuses = List.of(
                DispensingOrderStatus.PENDING,
                DispensingOrderStatus.ACCEPTED,
                DispensingOrderStatus.PARTIALLY_DISPENSED
        );
        if (dispensingOrderRepository.existsActiveOrderByPrescriptionId(prescriptionId, activeStatuses)) {
            throw new ConflictException("An active dispensing order is already open for prescription ID: " + prescriptionId);
        }

        DispensingOrderEntity order = new DispensingOrderEntity(
                UUID.randomUUID(),
                generateDispensingReference(),
                prescription,
                prescription.getPatient(),
                prescription.getHospital(),
                DispensingOrderStatus.PENDING,
                Instant.now(),
                requestDto.notes() != null ? requestDto.notes().trim() : null,
                currentUserId
        );

        Map<UUID, PrescriptionItemEntity> itemMap = prescription.getItems().stream()
                .collect(Collectors.toMap(PrescriptionItemEntity::getId, Function.identity()));

        if (requestDto.prescriptionItemIds() != null && !requestDto.prescriptionItemIds().isEmpty()) {
            for (UUID itemId : requestDto.prescriptionItemIds()) {
                PrescriptionItemEntity pItem = itemMap.get(itemId);
                if (pItem == null) {
                    throw new BadRequestException("Prescription item [" + itemId + "] does not belong to prescription [" + prescriptionId + "]");
                }
                order.addItem(new DispensingOrderItemEntity(
                        UUID.randomUUID(),
                        order,
                        pItem,
                        pItem.getMedicineName(),
                        pItem.getQuantity(),
                        0
                ));
            }
        } else {
            // Default: include all items from the prescription
            for (PrescriptionItemEntity pItem : prescription.getItems()) {
                order.addItem(new DispensingOrderItemEntity(
                        UUID.randomUUID(),
                        order,
                        pItem,
                        pItem.getMedicineName(),
                        pItem.getQuantity(),
                        0
                ));
            }
        }

        DispensingOrderEntity saved;
        try {
            saved = dispensingOrderRepository.saveAndFlush(order);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Active dispensing order conflict detected for prescription [{}]: {}", prescriptionId, ex.getMessage());
            throw new ConflictException("An active dispensing order is already open for prescription ID: " + prescriptionId);
        }

        log.info("Dispensing order [{}] created with reference [{}]", saved.getId(), saved.getDispensingReference());
        return dispensingOrderMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DispensingOrderResponseDto getDispensingOrderById(UUID orderId) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        DispensingOrderEntity order = dispensingOrderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispensing order not found with ID: " + orderId));

        validateOrderReadAccess(currentUserId, order);
        return dispensingOrderMapper.toResponseDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DispensingOrderResponseDto> getPatientDispensingOrders(Pageable pageable) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        PatientEntity patient = patientRepository.findByAuthUserId(currentUserId)
                .orElseThrow(() -> new ForbiddenException("Patient profile not found for authenticated user"));

        Page<DispensingOrderEntity> page = dispensingOrderRepository.findByPatientId(patient.getId(), pageable);
        return page.map(dispensingOrderMapper::toResponseDto);
    }

    @Override
    @Transactional
    public DispensingOrderResponseDto cancelDispensingOrder(UUID orderId, String reason) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();
        log.info("Cancelling dispensing order [{}] by user [{}]", orderId, currentUserId);

        DispensingOrderEntity order = dispensingOrderRepository.findByIdWithDetails(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispensing order not found with ID: " + orderId));

        validateOrderCancelAccess(currentUserId, order);

        if (order.getStatus() != DispensingOrderStatus.PENDING) {
            throw new ConflictException("Cannot cancel dispensing order in status: " + order.getStatus() + ". Only PENDING orders can be cancelled by the patient.");
        }

        order.setStatus(DispensingOrderStatus.CANCELLED);
        if (reason != null && !reason.isBlank()) {
            order.setNotes(order.getNotes() != null ? order.getNotes() + " | Cancelled: " + reason.trim() : "Cancelled: " + reason.trim());
        }
        order.setUpdatedBy(currentUserId);

        DispensingOrderEntity saved = dispensingOrderRepository.save(order);
        log.info("Dispensing order [{}] cancelled", saved.getId());
        return dispensingOrderMapper.toResponseDto(saved);
    }

    private void validateOrderingAccess(String currentUserId, PrescriptionEntity prescription) {
        boolean isPatient = prescription.getPatient() != null && currentUserId.equals(prescription.getPatient().getAuthUserId());
        if (!isPatient) {
            throw new ForbiddenException("Access denied: Only the patient for whom the prescription was issued may place a dispensing order");
        }
    }

    private void validateOrderCancelAccess(String currentUserId, DispensingOrderEntity order) {
        boolean isPatient = order.getPatient() != null && currentUserId.equals(order.getPatient().getAuthUserId());
        if (!isPatient) {
            throw new ForbiddenException("Access denied: Only the owning patient may cancel their dispensing order");
        }
    }

    private void validateOrderReadAccess(String currentUserId, DispensingOrderEntity order) {
        boolean isPatient = order.getPatient() != null && currentUserId.equals(order.getPatient().getAuthUserId());
        boolean isProvider = isAuthorizedForProvider(currentUserId, order.getPrescription().getProvider());
        boolean isHospital = isAuthorizedForHospital(currentUserId, order.getHospital());

        if (!isPatient && !isProvider && !isHospital) {
            throw new ForbiddenException("Access denied: You are not authorized to view this dispensing order");
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

    private String generateDispensingReference() {
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "DSP-" + datePart + "-" + randomPart;
    }
}
