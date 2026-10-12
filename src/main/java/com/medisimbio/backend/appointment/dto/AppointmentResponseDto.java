package com.medisimbio.backend.appointment.dto;

import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Detailed Response DTO for Patient Appointments.
 */
@Schema(description = "Appointment detailed response payload")
public class AppointmentResponseDto {

    @Schema(description = "Appointment UUID", example = "44444444-4444-4444-4444-444444444001")
    private UUID id;

    @Schema(description = "Human-readable booking reference", example = "MSB-20261015-9K3A")
    private String bookingReference;

    @Schema(description = "Patient UUID", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d")
    private UUID patientId;

    @Schema(description = "Patient full name", example = "Sarah Connor")
    private String patientName;

    @Schema(description = "Patient contact mobile", example = "+919876543201")
    private String patientMobile;

    @Schema(description = "Provider / Doctor UUID", example = "33333333-3333-3333-3333-333333333001")
    private UUID providerId;

    @Schema(description = "Doctor full name", example = "Dr. Rajesh Sharma")
    private String providerName;

    @Schema(description = "Doctor qualification", example = "MBBS, MD - Cardiology")
    private String providerQualification;

    @Schema(description = "Hospital facility UUID", example = "22222222-2222-2222-2222-222222222001")
    private UUID hospitalId;

    @Schema(description = "Hospital name", example = "All India Institute of Medical Sciences (AIIMS)")
    private String hospitalName;

    @Schema(description = "Hospital city", example = "New Delhi")
    private String hospitalCity;

    @Schema(description = "Hospital address", example = "Sri Aurobindo Marg, Ansari Nagar")
    private String hospitalAddress;

    @Schema(description = "Medical specialty UUID", example = "11111111-1111-1111-1111-111111111002")
    private UUID specialtyId;

    @Schema(description = "Medical specialty name", example = "Cardiology")
    private String specialtyName;

    @Schema(description = "Appointment date", example = "2026-10-15")
    private LocalDate appointmentDate;

    @Schema(description = "Consultation start time", example = "09:30:00")
    private LocalTime startTime;

    @Schema(description = "Consultation end time", example = "10:00:00")
    private LocalTime endTime;

    @Schema(description = "Appointment lifecycle status", example = "SCHEDULED")
    private AppointmentStatus status;

    @Schema(description = "Visit reason summary", example = "Routine cardiac checkup")
    private String visitReason;

    @Schema(description = "Cancellation reason if cancelled", example = "Patient schedule conflict")
    private String cancellationReason;

    @Schema(description = "Prior appointment UUID if rescheduled")
    private UUID rescheduledFromId;

    @Schema(description = "Creation timestamp")
    private Instant createdAt;

    @Schema(description = "Update timestamp")
    private Instant updatedAt;

    public AppointmentResponseDto() {
    }

    public AppointmentResponseDto(UUID id, String bookingReference, UUID patientId, String patientName,
                                  String patientMobile, UUID providerId, String providerName,
                                  String providerQualification, UUID hospitalId, String hospitalName,
                                  String hospitalCity, String hospitalAddress, UUID specialtyId,
                                  String specialtyName, LocalDate appointmentDate, LocalTime startTime,
                                  LocalTime endTime, AppointmentStatus status, String visitReason,
                                  String cancellationReason, UUID rescheduledFromId, Instant createdAt,
                                  Instant updatedAt) {
        this.id = id;
        this.bookingReference = bookingReference;
        this.patientId = patientId;
        this.patientName = patientName;
        this.patientMobile = patientMobile;
        this.providerId = providerId;
        this.providerName = providerName;
        this.providerQualification = providerQualification;
        this.hospitalId = hospitalId;
        this.hospitalName = hospitalName;
        this.hospitalCity = hospitalCity;
        this.hospitalAddress = hospitalAddress;
        this.specialtyId = specialtyId;
        this.specialtyName = specialtyName;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.visitReason = visitReason;
        this.cancellationReason = cancellationReason;
        this.rescheduledFromId = rescheduledFromId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getPatientMobile() {
        return patientMobile;
    }

    public void setPatientMobile(String patientMobile) {
        this.patientMobile = patientMobile;
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

    public String getProviderQualification() {
        return providerQualification;
    }

    public void setProviderQualification(String providerQualification) {
        this.providerQualification = providerQualification;
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

    public String getHospitalCity() {
        return hospitalCity;
    }

    public void setHospitalCity(String hospitalCity) {
        this.hospitalCity = hospitalCity;
    }

    public String getHospitalAddress() {
        return hospitalAddress;
    }

    public void setHospitalAddress(String hospitalAddress) {
        this.hospitalAddress = hospitalAddress;
    }

    public UUID getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(UUID specialtyId) {
        this.specialtyId = specialtyId;
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

    public String getVisitReason() {
        return visitReason;
    }

    public void setVisitReason(String visitReason) {
        this.visitReason = visitReason;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public UUID getRescheduledFromId() {
        return rescheduledFromId;
    }

    public void setRescheduledFromId(UUID rescheduledFromId) {
        this.rescheduledFromId = rescheduledFromId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
