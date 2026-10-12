package com.medisimbio.backend.prescription.service;

import com.medisimbio.backend.prescription.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PrescriptionService {

    PrescriptionResponseDto createPrescription(UUID encounterId, CreatePrescriptionRequestDto requestDto);

    PrescriptionResponseDto getPrescriptionById(UUID prescriptionId);

    Page<PrescriptionSummaryDto> getPrescriptionsByEncounter(UUID encounterId, Pageable pageable);

    Page<PrescriptionSummaryDto> getPatientPrescriptions(Pageable pageable);

    PrescriptionResponseDto updateDraftPrescription(UUID prescriptionId, UpdatePrescriptionRequestDto requestDto);

    PrescriptionResponseDto finalizePrescription(UUID prescriptionId);

    PrescriptionResponseDto cancelPrescription(UUID prescriptionId, CancelPrescriptionRequestDto requestDto);

    PrescriptionResponseDto replacePrescription(UUID prescriptionId, ReplacePrescriptionRequestDto requestDto);
}
