package com.medisimbio.backend.appointment.dto;

import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Lightweight Summary DTO for Appointment listings (Patient, Doctor, Hospital views).
 */
@Schema(description = "Appointment summary payload for directory listings")
public class AppointmentSummaryDto {

    @Schema(description = "Appointment UUID", example = "44444444-4444-4444-4444-444444444001")
    private UUID id;

    @Schema(description = "Booking reference", example = "MSB-20261015-9K3A")
    private String bookingReference;

    @Schema(description = "Patient UUID", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
    private UUID patientId;

    @Schema(description = "Patient full name", example = "Sarah Connor")
    private String patientName;

    @Schema(description = "Doctor UUID", example = "33333333-3333-3333-3333-333333333001")
    private UUID providerId;

    @Schema(description = "Doctor full name", example = "Dr. Rajesh Sharma")
    private String providerName;

    @Schema(description = "Hospital facility UUID", example = "22222222-2222-2222-2222-222222222001")
    private UUID hospitalId;

    @Schema(description = "Hospital name", example = "All India Institute of Medical Sciences (AIIMS)")
    private String hospitalName;

    @Schema(description = "Medical specialty name", example = "Cardiology")
    private String specialtyName;

    @Schema(description = "Appointment date", example = "2026-10-15")
    private LocalDate appointmentDate;

    @Schema(description = "Consultation start time", example = "09:30:00")
    private LocalTime startTime;

    @Schema(description = "Consultation end time", example = "10:00:00")
    private LocalTime endTime;

    @Schema(description = "Appointment status", example = "SCHEDULED")
    private AppointmentStatus status;

    @Schema(description = "Booking timestamp")
    private Instant createdAt;

    public AppointmentSummaryDto() {
    }

    public AppointmentSummaryDto(UUID id, String bookingReference, UUID patientId, String patientName,
                                 UUID providerId, String providerName, UUID hospitalId, String hospitalName,
                                 String specialtyName, LocalDate appointmentDate, LocalTime startTime,
                                 LocalTime endTime, AppointmentStatus status, Instant createdAt) {
        this.id = id;
        this.bookingReference = bookingReference;
        this.patientId = patientId;
        this.patientName = patientName;
        this.providerId = providerId;
        this.providerName = providerName;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.specialtyName = specialtyName;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
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

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public UUID getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(UUID hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getSpecialtyName() {
        return specialtyName;
    }

    public void setSpecialtyName(String specialtyName) {
        this.specialtyName = specialtyName;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
