package com.medisimbio.backend.vitals.repository;

import com.medisimbio.backend.vitals.entity.VitalsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for VitalsEntity.
 */
@Repository
public interface VitalsRepository extends JpaRepository<VitalsEntity, UUID> {

    @Query("SELECT v FROM VitalsEntity v " +
           "LEFT JOIN FETCH v.encounter e " +
           "LEFT JOIN FETCH v.patient p " +
           "WHERE v.encounter.id = :encounterId " +
           "ORDER BY v.measuredAt ASC")
    List<VitalsEntity> findByEncounterId(@Param("encounterId") UUID encounterId);

    @Query("SELECT v FROM VitalsEntity v " +
           "LEFT JOIN FETCH v.encounter e " +
           "LEFT JOIN FETCH v.patient p " +
           "WHERE v.patient.id = :patientId " +
           "ORDER BY v.measuredAt DESC")
    List<VitalsEntity> findByPatientIdOrderByMeasuredAtDesc(@Param("patientId") UUID patientId);

    @Query("SELECT v FROM VitalsEntity v " +
           "LEFT JOIN FETCH v.encounter e " +
           "LEFT JOIN FETCH v.patient p " +
           "WHERE v.id = :id")
    Optional<VitalsEntity> findByIdWithDetails(@Param("id") UUID id);

    Optional<VitalsEntity> findFirstByPatientIdOrderByMeasuredAtDesc(UUID patientId);
}
