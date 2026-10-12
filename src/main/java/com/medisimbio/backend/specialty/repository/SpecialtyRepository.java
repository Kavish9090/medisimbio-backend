package com.medisimbio.backend.specialty.repository;

import com.medisimbio.backend.specialty.entity.SpecialtyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for SpecialtyEntity.
 */
@Repository
public interface SpecialtyRepository extends JpaRepository<SpecialtyEntity, UUID> {

    Optional<SpecialtyEntity> findByCode(String code);

    boolean existsByCode(String code);

    Page<SpecialtyEntity> findByActive(boolean active, Pageable pageable);
}
