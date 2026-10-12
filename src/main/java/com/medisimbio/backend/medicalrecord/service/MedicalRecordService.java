package com.medisimbio.backend.medicalrecord.service;

import com.medisimbio.backend.medicalrecord.dto.MedicalRecordOverviewDto;
import com.medisimbio.backend.medicalrecord.dto.MedicalTimelineResponseDto;

/**
 * Service interface for aggregating patient medical records and generating clinical timelines.
 */
public interface MedicalRecordService {

    MedicalRecordOverviewDto getPatientMedicalRecordOverview();

    MedicalTimelineResponseDto getPatientMedicalTimeline(int page, int size);
}
