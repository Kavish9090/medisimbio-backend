package com.medisimbio.backend.dispensing.service;

import com.medisimbio.backend.dispensing.dto.CreateDispensingOrderRequestDto;
import com.medisimbio.backend.dispensing.dto.DispensingOrderResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DispensingOrderService {

    DispensingOrderResponseDto createDispensingOrder(UUID prescriptionId, CreateDispensingOrderRequestDto requestDto);

    DispensingOrderResponseDto getDispensingOrderById(UUID orderId);

    Page<DispensingOrderResponseDto> getPatientDispensingOrders(Pageable pageable);

    DispensingOrderResponseDto cancelDispensingOrder(UUID orderId, String reason);
}
