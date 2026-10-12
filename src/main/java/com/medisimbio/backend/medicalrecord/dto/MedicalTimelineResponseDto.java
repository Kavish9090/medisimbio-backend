package com.medisimbio.backend.medicalrecord.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * Paginated response containing a patient's chronological clinical events timeline.
 */
@Schema(description = "Paginated patient clinical timeline response")
public class MedicalTimelineResponseDto {

    @Schema(description = "Patient UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID patientId;

    @Schema(description = "Patient Full Name", example = "Rahul Sharma")
    private String patientName;

    @Schema(description = "Patient Med-ID Number", example = "MD-9842-1084")
    private String patientMedId;

    @Schema(description = "Chronological list of clinical events")
    private List<MedicalTimelineEventDto> events;

    @Schema(description = "Current page index (0-based)", example = "0")
    private int page;

    @Schema(description = "Page size", example = "20")
    private int size;

    @Schema(description = "Total number of timeline events", example = "42")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "3")
    private int totalPages;

    @Schema(description = "Indicates if subsequent pages exist", example = "true")
    private boolean hasNext;

    @Schema(description = "Indicates if preceding pages exist", example = "false")
    private boolean hasPrevious;

    public MedicalTimelineResponseDto() {
    }

    public MedicalTimelineResponseDto(UUID patientId, String patientName, String patientMedId,
                                      List<MedicalTimelineEventDto> events, int page, int size,
                                      long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientMedId = patientMedId;
        this.events = events;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientMedId() {
        return patientMedId;
    }

    public void setPatientMedId(String patientMedId) {
        this.patientMedId = patientMedId;
    }

    public List<MedicalTimelineEventDto> getEvents() {
        return events;
    }

    public void setEvents(List<MedicalTimelineEventDto> events) {
        this.events = events;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}
