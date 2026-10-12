package com.medisimbio.backend.hospital.repository;

import com.medisimbio.backend.hospital.entity.HospitalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for HospitalEntity with dynamic Specification querying.
 */
@Repository
public interface HospitalRepository extends JpaRepository<HospitalEntity, UUID>, JpaSpecificationExecutor<HospitalEntity> {

    Optional<HospitalEntity> findByRegistrationIdentifier(String registrationIdentifier);

    boolean existsByRegistrationIdentifier(String registrationIdentifier);

    boolean existsByNameIgnoreCaseAndCityIgnoreCase(String name, String city);
}
