package com.medisimbio.backend.diagnosis.repository;

import com.medisimbio.backend.diagnosis.entity.DiagnosisEntity;
import com.medisimbio.backend.diagnosis.entity.DiagnosisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for DiagnosisEntity.
 */
@Repository
public interface DiagnosisRepository extends JpaRepository<DiagnosisEntity, UUID> {

    @Query("SELECT d FROM DiagnosisEntity d " +
           "LEFT JOIN FETCH d.encounter e " +
           "LEFT JOIN FETCH d.patient p " +
           "WHERE d.encounter.id = :encounterId " +
           "ORDER BY d.diagnosedAt ASC")
    List<DiagnosisEntity> findByEncounterId(@Param("encounterId") UUID encounterId);

    @Query("SELECT d FROM DiagnosisEntity d " +
           "LEFT JOIN FETCH d.encounter e " +
           "LEFT JOIN FETCH d.patient p " +
           "WHERE d.patient.id = :patientId " +
           "ORDER BY d.diagnosedAt DESC")
    List<DiagnosisEntity> findByPatientIdOrderByDiagnosedAtDesc(@Param("patientId") UUID patientId);

    @Query("SELECT d FROM DiagnosisEntity d " +
           "LEFT JOIN FETCH d.encounter e " +
           "LEFT JOIN FETCH d.patient p " +
           "WHERE d.patient.id = :patientId AND d.status = :status " +
           "ORDER BY d.diagnosedAt DESC")
    List<DiagnosisEntity> findByPatientIdAndStatus(@Param("patientId") UUID patientId, @Param("status") DiagnosisStatus status);

    @Query("SELECT d FROM DiagnosisEntity d " +
           "LEFT JOIN FETCH d.encounter e " +
           "LEFT JOIN FETCH d.patient p " +
           "WHERE d.id = :id")
    Optional<DiagnosisEntity> findByIdWithDetails(@Param("id") UUID id);

    long countByPatientId(UUID patientId);
}
