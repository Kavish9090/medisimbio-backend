package com.medisimbio.backend.provider.repository;

import com.medisimbio.backend.provider.entity.ProviderHospitalEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for ProviderHospitalEntity affiliations.
 */
@Repository
public interface ProviderHospitalRepository extends JpaRepository<ProviderHospitalEntity, UUID> {

    List<ProviderHospitalEntity> findByProviderId(UUID providerId);

    List<ProviderHospitalEntity> findByProviderIdAndActive(UUID providerId, boolean active);

    Page<ProviderHospitalEntity> findByHospitalId(UUID hospitalId, Pageable pageable);

    Page<ProviderHospitalEntity> findByHospitalIdAndActive(UUID hospitalId, boolean active, Pageable pageable);

    Optional<ProviderHospitalEntity> findByProviderIdAndHospitalId(UUID providerId, UUID hospitalId);

    boolean existsByProviderIdAndHospitalId(UUID providerId, UUID hospitalId);

    void deleteByProviderIdAndHospitalId(UUID providerId, UUID hospitalId);

    @Query("SELECT ph FROM ProviderHospitalEntity ph JOIN FETCH ph.hospital WHERE ph.provider.id = :providerId")
    List<ProviderHospitalEntity> findByProviderIdWithHospital(@Param("providerId") UUID providerId);

    @Query("SELECT ph FROM ProviderHospitalEntity ph JOIN FETCH ph.provider WHERE ph.hospital.id = :hospitalId")
    List<ProviderHospitalEntity> findByHospitalIdWithProvider(@Param("hospitalId") UUID hospitalId);
}
