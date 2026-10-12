package com.medisimbio.backend.provider.repository;

import com.medisimbio.backend.provider.entity.ProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for ProviderEntity.
 */
@Repository
public interface ProviderRepository extends JpaRepository<ProviderEntity, UUID>, JpaSpecificationExecutor<ProviderEntity> {

    Optional<ProviderEntity> findByProfessionalRegistrationNumber(String professionalRegistrationNumber);

    Optional<ProviderEntity> findByEmail(String email);

    Optional<ProviderEntity> findByMobileNumber(String mobileNumber);

    Optional<ProviderEntity> findByAuthUserId(String authUserId);

    boolean existsByAuthUserId(String authUserId);

    boolean existsByProfessionalRegistrationNumber(String professionalRegistrationNumber);

    boolean existsByEmail(String email);

    boolean existsByMobileNumber(String mobileNumber);
}
