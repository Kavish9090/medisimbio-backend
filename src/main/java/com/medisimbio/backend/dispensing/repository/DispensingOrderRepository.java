package com.medisimbio.backend.dispensing.repository;

import com.medisimbio.backend.dispensing.entity.DispensingOrderEntity;
import com.medisimbio.backend.dispensing.entity.DispensingOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DispensingOrderRepository extends JpaRepository<DispensingOrderEntity, UUID> {

    @Query("""
        SELECT d FROM DispensingOrderEntity d
        LEFT JOIN FETCH d.prescription p
        LEFT JOIN FETCH d.patient pat
        LEFT JOIN FETCH d.hospital h
        LEFT JOIN FETCH d.items items
        WHERE d.id = :id
    """)
    Optional<DispensingOrderEntity> findByIdWithDetails(@Param("id") UUID id);

    Page<DispensingOrderEntity> findByPatientId(UUID patientId, Pageable pageable);

    List<DispensingOrderEntity> findByPrescriptionId(UUID prescriptionId);

    @Query("""
        SELECT COUNT(d) > 0 FROM DispensingOrderEntity d
        WHERE d.prescription.id = :prescriptionId AND d.status IN :statuses
    """)
    boolean existsActiveOrderByPrescriptionId(@Param("prescriptionId") UUID prescriptionId,
                                              @Param("statuses") Collection<DispensingOrderStatus> statuses);
}
