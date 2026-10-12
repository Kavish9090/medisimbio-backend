package com.medisimbio.backend.appointment.controller;

import com.medisimbio.backend.appointment.dto.SlotResponseDto;
import com.medisimbio.backend.appointment.service.SlotService;
import com.medisimbio.backend.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for dynamic consultation slot calculation.
 */
@RestController
@RequestMapping("/api/v1/providers/{providerId}/slots")
@Tag(name = "Consultation Slots", description = "Endpoints for discovering available booking slots for doctors")
public class SlotController {

    private final SlotService slotService;

    public SlotController(SlotService slotService) {
        this.slotService = slotService;
    }

    @GetMapping
    @Operation(summary = "Get available booking slots", description = "Calculates currently bookable consultation slots for a doctor at a specific hospital on a given date")
    public ResponseEntity<ApiResponse<List<SlotResponseDto>>> getAvailableSlots(
            @PathVariable UUID providerId,
            @RequestParam(required = false) UUID hospitalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SlotResponseDto> slots = slotService.getAvailableSlots(providerId, hospitalId, date);
        return ResponseEntity.ok(ApiResponse.success(slots));
    }
}
