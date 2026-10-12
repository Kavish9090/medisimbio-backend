package com.medisimbio.backend.hospital.repository;

import com.medisimbio.backend.hospital.entity.HospitalStaffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for HospitalStaffEntity.
 */
@Repository
public interface HospitalStaffRepository extends JpaRepository<HospitalStaffEntity, UUID> {

    boolean existsByHospitalIdAndAuthUserIdAndActiveTrue(UUID hospitalId, String authUserId);

    Optional<HospitalStaffEntity> findByHospitalIdAndAuthUserId(UUID hospitalId, String authUserId);

    List<HospitalStaffEntity> findByAuthUserIdAndActiveTrue(String authUserId);

    List<HospitalStaffEntity> findByHospitalId(UUID hospitalId);
}
