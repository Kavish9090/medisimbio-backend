package com.medisimbio.backend.prescription.repository;

import com.medisimbio.backend.prescription.entity.PrescriptionItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItemEntity, UUID> {

    List<PrescriptionItemEntity> findByPrescriptionIdOrderBySequenceNumberAsc(UUID prescriptionId);
}
