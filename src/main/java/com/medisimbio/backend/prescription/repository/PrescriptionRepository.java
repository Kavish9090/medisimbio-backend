package com.medisimbio.backend.prescription.repository;

import com.medisimbio.backend.prescription.entity.PrescriptionEntity;
import com.medisimbio.backend.prescription.entity.PrescriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<PrescriptionEntity, UUID> {

    @Query("""
        SELECT p FROM PrescriptionEntity p
        LEFT JOIN FETCH p.patient pat
        LEFT JOIN FETCH p.encounter e
        LEFT JOIN FETCH p.provider pr
        LEFT JOIN FETCH p.hospital h
        LEFT JOIN FETCH p.items items
        WHERE p.id = :id
    """)
    Optional<PrescriptionEntity> findByIdWithDetails(@Param("id") UUID id);

    Page<PrescriptionEntity> findByPatientId(UUID patientId, Pageable pageable);

    Page<PrescriptionEntity> findByPatientIdAndStatusIn(UUID patientId, Collection<PrescriptionStatus> statuses, Pageable pageable);

    Page<PrescriptionEntity> findByEncounterId(UUID encounterId, Pageable pageable);

    Page<PrescriptionEntity> findByEncounterIdAndStatusIn(UUID encounterId, Collection<PrescriptionStatus> statuses, Pageable pageable);

    @Query("SELECT COUNT(p) > 0 FROM PrescriptionEntity p WHERE p.replacesPrescription.id = :replacesId")
    boolean existsByReplacesPrescriptionId(@Param("replacesId") UUID replacesId);

    long countByPatientIdAndStatusIn(UUID patientId, Collection<PrescriptionStatus> statuses);
}
