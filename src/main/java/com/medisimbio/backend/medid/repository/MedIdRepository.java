package com.medisimbio.backend.medid.repository;

import com.medisimbio.backend.medid.entity.MedIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Med-ID entities.
 */
@Repository
public interface MedIdRepository extends JpaRepository<MedIdEntity, UUID> {

    @Query("SELECT m FROM MedIdEntity m JOIN FETCH m.patient WHERE m.patient.id = :patientId")
    Optional<MedIdEntity> findByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT m FROM MedIdEntity m JOIN FETCH m.patient WHERE m.medIdNumber = :medIdNumber")
    Optional<MedIdEntity> findByMedIdNumberWithPatient(@Param("medIdNumber") String medIdNumber);

    Optional<MedIdEntity> findByMedIdNumber(String medIdNumber);

    boolean existsByMedIdNumber(String medIdNumber);

    boolean existsByPatientId(UUID patientId);
}
