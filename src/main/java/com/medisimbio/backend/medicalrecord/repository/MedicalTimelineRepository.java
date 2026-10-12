package com.medisimbio.backend.medicalrecord.repository;

import com.medisimbio.backend.encounter.entity.EncounterEntity;
import com.medisimbio.backend.medicalrecord.dto.MedicalTimelineEventProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository providing database-level paginated medical timeline aggregation.
 */
@Repository
public interface MedicalTimelineRepository extends JpaRepository<EncounterEntity, UUID> {

    @Query(value = """
        SELECT
            timeline_events.event_type AS eventType,
            timeline_events.event_id AS eventId,
            timeline_events.occurred_at AS occurredAt,
            timeline_events.title AS title,
            timeline_events.summary AS summary,
            timeline_events.encounter_id AS encounterId,
            timeline_events.encounter_reference AS encounterReference,
            timeline_events.provider_id AS providerId,
            timeline_events.provider_name AS providerName,
            timeline_events.hospital_id AS hospitalId,
            timeline_events.hospital_name AS hospitalName,
            timeline_events.status AS status
        FROM (
            SELECT
                'ENCOUNTER' AS event_type,
                e.id AS event_id,
                COALESCE(e.started_at, e.created_at) AS occurred_at,
                CASE WHEN p.full_name IS NOT NULL THEN CONCAT('Clinical Encounter — ', p.full_name) ELSE 'Clinical Encounter' END AS title,
                CONCAT(
                    CASE WHEN e.chief_complaint IS NOT NULL AND e.chief_complaint <> '' THEN CONCAT('Complaint: ', e.chief_complaint) ELSE 'Consultation' END,
                    CASE WHEN e.diagnosis_summary IS NOT NULL AND e.diagnosis_summary <> '' THEN CONCAT(' | Diagnosis: ', e.diagnosis_summary) ELSE '' END
                ) AS summary,
                e.id AS encounter_id,
                e.encounter_reference AS encounter_reference,
                p.id AS provider_id,
                p.full_name AS provider_name,
                h.id AS hospital_id,
                h.name AS hospital_name,
                e.status AS status,
                e.patient_id AS patient_id
            FROM encounters e
            LEFT JOIN providers p ON e.provider_id = p.id
            LEFT JOIN hospitals h ON e.hospital_id = h.id
            WHERE e.patient_id = :patientId

            UNION ALL

            SELECT
                'DIAGNOSIS' AS event_type,
                d.id AS event_id,
                COALESCE(d.diagnosed_at, d.created_at) AS occurred_at,
                CONCAT('Diagnosis: ', d.diagnosis_name, CASE WHEN d.diagnosis_code IS NOT NULL AND d.diagnosis_code <> '' THEN CONCAT(' (', d.diagnosis_code, ')') ELSE '' END) AS title,
                CONCAT(
                    CASE WHEN d.diagnosis_type IS NOT NULL THEN CONCAT(d.diagnosis_type, ' — ') ELSE '' END,
                    COALESCE(d.diagnosis_description, d.diagnosis_name)
                ) AS summary,
                e.id AS encounter_id,
                e.encounter_reference AS encounter_reference,
                p.id AS provider_id,
                p.full_name AS provider_name,
                h.id AS hospital_id,
                h.name AS hospital_name,
                d.status AS status,
                d.patient_id AS patient_id
            FROM diagnoses d
            LEFT JOIN encounters e ON d.encounter_id = e.id
            LEFT JOIN providers p ON e.provider_id = p.id
            LEFT JOIN hospitals h ON e.hospital_id = h.id
            WHERE d.patient_id = :patientId

            UNION ALL

            SELECT
                'VITALS' AS event_type,
                v.id AS event_id,
                COALESCE(v.measured_at, v.created_at) AS occurred_at,
                'Vitals Recorded' AS title,
                TRIM(BOTH ' | ' FROM CONCAT(
                    CASE WHEN v.temperature IS NOT NULL THEN CONCAT('Temp: ', CAST(v.temperature AS VARCHAR), '° | ') ELSE '' END,
                    CASE WHEN v.systolic_blood_pressure IS NOT NULL AND v.diastolic_blood_pressure IS NOT NULL THEN CONCAT('BP: ', CAST(v.systolic_blood_pressure AS VARCHAR), '/', CAST(v.diastolic_blood_pressure AS VARCHAR), ' mmHg | ') ELSE '' END,
                    CASE WHEN v.heart_rate IS NOT NULL THEN CONCAT('HR: ', CAST(v.heart_rate AS VARCHAR), ' bpm | ') ELSE '' END,
                    CASE WHEN v.oxygen_saturation IS NOT NULL THEN CONCAT('SpO2: ', CAST(v.oxygen_saturation AS VARCHAR), '% | ') ELSE '' END,
                    CASE WHEN v.bmi IS NOT NULL THEN CONCAT('BMI: ', CAST(v.bmi AS VARCHAR)) ELSE '' END
                )) AS summary,
                e.id AS encounter_id,
                e.encounter_reference AS encounter_reference,
                p.id AS provider_id,
                p.full_name AS provider_name,
                h.id AS hospital_id,
                h.name AS hospital_name,
                'RECORDED' AS status,
                v.patient_id AS patient_id
            FROM vitals v
            LEFT JOIN encounters e ON v.encounter_id = e.id
            LEFT JOIN providers p ON e.provider_id = p.id
            LEFT JOIN hospitals h ON e.hospital_id = h.id
            WHERE v.patient_id = :patientId

            UNION ALL

            SELECT
                'PRESCRIPTION' AS event_type,
                pr.id AS event_id,
                COALESCE(pr.finalized_at, pr.prescribed_at, pr.created_at) AS occurred_at,
                CONCAT('Prescription: ', pr.prescription_reference) AS title,
                CONCAT(
                    'Status: ', pr.status,
                    CASE WHEN pr.clinical_instructions IS NOT NULL AND pr.clinical_instructions <> '' THEN CONCAT(' | Instructions: ', pr.clinical_instructions) ELSE '' END
                ) AS summary,
                e.id AS encounter_id,
                e.encounter_reference AS encounter_reference,
                p.id AS provider_id,
                p.full_name AS provider_name,
                h.id AS hospital_id,
                h.name AS hospital_name,
                pr.status AS status,
                pr.patient_id AS patient_id
            FROM prescriptions pr
            LEFT JOIN encounters e ON pr.encounter_id = e.id
            LEFT JOIN providers p ON pr.provider_id = p.id
            LEFT JOIN hospitals h ON pr.hospital_id = h.id
            WHERE pr.patient_id = :patientId AND pr.status IN ('FINALIZED', 'SUPERSEDED')
        ) timeline_events
        ORDER BY timeline_events.occurred_at DESC, timeline_events.event_type ASC, timeline_events.event_id ASC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<MedicalTimelineEventProjection> findPatientTimelineEvents(
            @Param("patientId") UUID patientId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
        SELECT (
            (SELECT COUNT(*) FROM encounters WHERE patient_id = :patientId) +
            (SELECT COUNT(*) FROM diagnoses WHERE patient_id = :patientId) +
            (SELECT COUNT(*) FROM vitals WHERE patient_id = :patientId) +
            (SELECT COUNT(*) FROM prescriptions WHERE patient_id = :patientId AND status IN ('FINALIZED', 'SUPERSEDED'))
        )
        """, nativeQuery = true)
    long countPatientTimelineEvents(@Param("patientId") UUID patientId);
}
