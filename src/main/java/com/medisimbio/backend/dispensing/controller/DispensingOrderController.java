package com.medisimbio.backend.dispensing.controller;

import com.medisimbio.backend.common.response.ApiResponse;
import com.medisimbio.backend.dispensing.dto.CreateDispensingOrderRequestDto;
import com.medisimbio.backend.dispensing.dto.DispensingOrderResponseDto;
import com.medisimbio.backend.dispensing.service.DispensingOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Dispensing Orders", description = "Endpoints for medicine dispensing orders linked to finalized clinical prescriptions")
@SecurityRequirement(name = "bearerAuth")
public class DispensingOrderController {

    private final DispensingOrderService dispensingOrderService;

    public DispensingOrderController(DispensingOrderService dispensingOrderService) {
        this.dispensingOrderService = dispensingOrderService;
    }

    @PostMapping("/prescriptions/{prescriptionId}/dispensing-orders")
    @Operation(summary = "Create Dispensing Order", description = "Creates a medicine dispensing order for a finalized prescription")
    public ResponseEntity<ApiResponse<DispensingOrderResponseDto>> createDispensingOrder(
            @PathVariable UUID prescriptionId,
            @Valid @RequestBody(required = false) CreateDispensingOrderRequestDto requestDto) {
        CreateDispensingOrderRequestDto dto = requestDto != null ? requestDto : new CreateDispensingOrderRequestDto(null, null);
        DispensingOrderResponseDto response = dispensingOrderService.createDispensingOrder(prescriptionId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/dispensing-orders/{orderId}")
    @Operation(summary = "Get Dispensing Order by ID", description = "Retrieves dispensing order details including ordered medicine items")
    public ResponseEntity<ApiResponse<DispensingOrderResponseDto>> getDispensingOrder(
            @PathVariable UUID orderId) {
        DispensingOrderResponseDto response = dispensingOrderService.getDispensingOrderById(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/patients/me/dispensing-orders")
    @Operation(summary = "List My Dispensing Orders", description = "Retrieves paginated dispensing orders for authenticated patient")
    public ResponseEntity<ApiResponse<Page<DispensingOrderResponseDto>>> getMyDispensingOrders(
            @Parameter(description = "Page number (0-indexed)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (max 50)", example = "10") @RequestParam(defaultValue = "10") int size) {
        int safeSize = Math.min(Math.max(size, 1), 50);
        Page<DispensingOrderResponseDto> response = dispensingOrderService.getPatientDispensingOrders(
                PageRequest.of(page, safeSize, Sort.by(Sort.Direction.DESC, "orderedAt")));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/dispensing-orders/{orderId}/cancel")
    @Operation(summary = "Cancel Dispensing Order", description = "Cancels a pending or accepted dispensing order prior to fulfillment")
    public ResponseEntity<ApiResponse<DispensingOrderResponseDto>> cancelDispensingOrder(
            @PathVariable UUID orderId,
            @RequestParam(required = false) String reason) {
        DispensingOrderResponseDto response = dispensingOrderService.cancelDispensingOrder(orderId, reason);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
