package com.medisimbio.backend.patient.service;

import com.medisimbio.backend.medid.dto.MedIdResponseDto;
import com.medisimbio.backend.patient.dto.CreatePatientRequestDto;
import com.medisimbio.backend.patient.dto.PatientResponseDto;
import com.medisimbio.backend.patient.dto.UpdatePatientRequestDto;

/**
 * Service interface for Patient Management operations.
 */
public interface PatientService {

    /**
     * Creates a new patient profile for the current authenticated user.
     *
     * @param requestDto Patient creation payload
     * @return Created patient profile DTO
     */
    PatientResponseDto createPatientProfile(CreatePatientRequestDto requestDto);

    /**
     * Retrieves the patient profile for the current authenticated user.
     *
     * @return Current patient profile DTO
     */
    PatientResponseDto getCurrentPatientProfile();

    /**
     * Updates the patient profile for the current authenticated user.
     *
     * @param requestDto Update payload
     * @return Updated patient profile DTO
     */
    PatientResponseDto updateCurrentPatientProfile(UpdatePatientRequestDto requestDto);

    /**
     * Generates or retrieves the Med-ID for the current authenticated patient.
     *
     * @return Med-ID details DTO
     */
    MedIdResponseDto generateOrGetMedIdForCurrentPatient();

    /**
     * Retrieves the Med-ID for the current authenticated patient.
     *
     * @return Med-ID details DTO
     */
    MedIdResponseDto getMedIdForCurrentPatient();
}
