package com.medisimbio.backend.medid.service.impl;

import com.medisimbio.backend.common.exception.BadRequestException;
import com.medisimbio.backend.common.exception.BusinessException;
import com.medisimbio.backend.common.exception.ResourceNotFoundException;
import com.medisimbio.backend.medid.dto.MedIdResponseDto;
import com.medisimbio.backend.medid.dto.MedIdVerificationResponseDto;
import com.medisimbio.backend.medid.entity.MedIdEntity;
import com.medisimbio.backend.medid.generator.MedIdGenerator;
import com.medisimbio.backend.medid.repository.MedIdRepository;
import com.medisimbio.backend.medid.service.MedIdService;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.patient.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Authoritative implementation of MedIdService.
 */
@Service
public class MedIdServiceImpl implements MedIdService {

    private static final Logger log = LoggerFactory.getLogger(MedIdServiceImpl.class);
    private static final Pattern MED_ID_PATTERN = Pattern.compile("^MD-[A-Z0-9]{4}-[A-Z0-9]{4}$");
    private static final int MAX_COLLISION_RETRIES = 10;

    private final MedIdRepository medIdRepository;
    private final PatientRepository patientRepository;
    private final MedIdGenerator medIdGenerator;

    public MedIdServiceImpl(MedIdRepository medIdRepository,
                            PatientRepository patientRepository,
                            MedIdGenerator medIdGenerator) {
        this.medIdRepository = medIdRepository;
        this.patientRepository = patientRepository;
        this.medIdGenerator = medIdGenerator;
    }

    @Override
    @Transactional
    public MedIdResponseDto generateOrGetMedIdForPatient(UUID patientId) {
        if (patientId == null) {
            throw new BadRequestException("Patient ID must not be null");
        }

        // 1. Check if Med-ID already exists for patient (idempotent retrieval)
        Optional<MedIdEntity> existingMedId = medIdRepository.findByPatientId(patientId);
        if (existingMedId.isPresent()) {
            MedIdEntity medId = existingMedId.get();
            log.info("Returning existing Med-ID [{}] for patient [{}]", medId.getMedIdNumber(), patientId);
            return mapToResponseDto(medId);
        }

        // 2. Retrieve patient entity
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", patientId));

        // 3. Generate unique Med-ID with collision avoidance
        String uniqueMedIdNumber = null;
        for (int attempt = 1; attempt <= MAX_COLLISION_RETRIES; attempt++) {
            String candidate = medIdGenerator.generateMedIdNumber();
            if (!medIdRepository.existsByMedIdNumber(candidate)) {
                uniqueMedIdNumber = candidate;
                break;
            }
            log.warn("Med-ID collision detected on candidate [{}]. Retrying (attempt {}/{})", candidate, attempt, MAX_COLLISION_RETRIES);
        }

        if (uniqueMedIdNumber == null) {
            throw new BusinessException("Failed to generate a unique Med-ID after multiple attempts. Please retry.");
        }

        // 4. Create and persist MedIdEntity
        String qrToken = medIdGenerator.generateQrToken(uniqueMedIdNumber, patient.getId());
        MedIdEntity medIdEntity = new MedIdEntity(UUID.randomUUID(), patient, uniqueMedIdNumber, qrToken);
        MedIdEntity saved = medIdRepository.save(medIdEntity);
        patient.setMedId(saved);

        log.info("Authoritatively issued new Med-ID [{}] for patient [{}]", uniqueMedIdNumber, patientId);
        return mapToResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MedIdResponseDto getMedIdForPatient(UUID patientId) {
        if (patientId == null) {
            throw new BadRequestException("Patient ID must not be null");
        }

        MedIdEntity medId = medIdRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Med-ID not found for patient with ID: " + patientId));

        return mapToResponseDto(medId);
    }

    @Override
    @Transactional(readOnly = true)
    public MedIdVerificationResponseDto verifyMedId(String medIdNumber) {
        if (medIdNumber == null || medIdNumber.isBlank()) {
            throw new BadRequestException("Med-ID number is required for verification");
        }

        String normalized = medIdNumber.trim().toUpperCase();
        if (!MED_ID_PATTERN.matcher(normalized).matches()) {
            throw new BadRequestException("Invalid Med-ID format. Expected format: MD-XXXX-XXXX");
        }

        MedIdEntity medId = medIdRepository.findByMedIdNumberWithPatient(normalized)
                .orElseThrow(() -> new ResourceNotFoundException("Med-ID", "medIdNumber", normalized));

        String maskedName = maskName(medId.getPatient().getFullName());

        return new MedIdVerificationResponseDto(
                medId.getMedIdNumber(),
                true,
                medId.getStatus(),
                maskedName,
                medId.getIssuedAt()
        );
    }

    private MedIdResponseDto mapToResponseDto(MedIdEntity entity) {
        return new MedIdResponseDto(
                entity.getMedIdNumber(),
                entity.getPatient().getId(),
                entity.getPatient().getFullName(),
                entity.getStatus(),
                entity.getQrToken(),
                entity.getIssuedAt()
        );
    }

    private String maskName(String name) {
        if (name == null || name.isBlank()) {
            return "***";
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.length() <= 2) {
                sb.append(part.charAt(0)).append("*");
            } else {
                sb.append(part.charAt(0))
                        .append("*".repeat(part.length() - 2))
                        .append(part.charAt(part.length() - 1));
            }
            if (i < parts.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
