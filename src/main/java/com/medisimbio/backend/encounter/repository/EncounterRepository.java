package com.medisimbio.backend.encounter.repository;

import com.medisimbio.backend.encounter.entity.EncounterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for EncounterEntity.
 */
@Repository
public interface EncounterRepository extends JpaRepository<EncounterEntity, UUID>, JpaSpecificationExecutor<EncounterEntity> {

    boolean existsByAppointmentId(UUID appointmentId);

    boolean existsByEncounterReference(String encounterReference);

    Optional<EncounterEntity> findByAppointmentId(UUID appointmentId);

    @Query("SELECT e FROM EncounterEntity e " +
           "LEFT JOIN FETCH e.patient p " +
           "LEFT JOIN FETCH p.medId " +
           "LEFT JOIN FETCH e.provider " +
           "LEFT JOIN FETCH e.hospital " +
           "LEFT JOIN FETCH e.appointment " +
           "WHERE e.id = :id")
    Optional<EncounterEntity> findByIdWithDetails(@Param("id") UUID id);
}
