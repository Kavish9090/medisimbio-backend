package com.medisimbio.backend.medid.service;

import com.medisimbio.backend.medid.dto.MedIdResponseDto;
import com.medisimbio.backend.medid.dto.MedIdVerificationResponseDto;

import java.util.UUID;

/**
 * Service interface for Med-ID Authority operations.
 */
public interface MedIdService {

    /**
     * Generates a new unique Med-ID for a patient or returns their existing Med-ID.
     *
     * @param patientId Internal patient UUID
     * @return Med-ID details
     */
    MedIdResponseDto generateOrGetMedIdForPatient(UUID patientId);

    /**
     * Retrieves the existing Med-ID for a patient.
     *
     * @param patientId Internal patient UUID
     * @return Med-ID details
     */
    MedIdResponseDto getMedIdForPatient(UUID patientId);

    /**
     * Verifies the authenticity and status of a Med-ID number.
     *
     * @param medIdNumber The Med-ID string (MD-XXXX-XXXX)
     * @return Verification details
     */
    MedIdVerificationResponseDto verifyMedId(String medIdNumber);
}
