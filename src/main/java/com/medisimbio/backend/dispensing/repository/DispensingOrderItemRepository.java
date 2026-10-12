package com.medisimbio.backend.dispensing.repository;

import com.medisimbio.backend.dispensing.entity.DispensingOrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DispensingOrderItemRepository extends JpaRepository<DispensingOrderItemEntity, UUID> {

    List<DispensingOrderItemEntity> findByDispensingOrderId(UUID dispensingOrderId);
}
