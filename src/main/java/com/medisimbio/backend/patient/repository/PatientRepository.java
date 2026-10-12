package com.medisimbio.backend.patient.repository;

import com.medisimbio.backend.patient.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Patient entities.
 */
@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, UUID> {

    @Query("SELECT p FROM PatientEntity p LEFT JOIN FETCH p.medId WHERE p.authUserId = :authUserId")
    Optional<PatientEntity> findByAuthUserIdWithMedId(@Param("authUserId") String authUserId);

    Optional<PatientEntity> findByAuthUserId(String authUserId);

    Optional<PatientEntity> findByEmail(String email);

    Optional<PatientEntity> findByMobileNumber(String mobileNumber);

    boolean existsByAuthUserId(String authUserId);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);
}
