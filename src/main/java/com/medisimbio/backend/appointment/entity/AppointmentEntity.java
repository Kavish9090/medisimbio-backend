package com.medisimbio.backend.appointment.entity;

import com.medisimbio.backend.common.audit.BaseEntity;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.specialty.entity.SpecialtyEntity;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Entity representing a patient consultation appointment.
 */
@Entity
@Table(name = "appointments")
public class AppointmentEntity extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientEntity patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "provider_id", nullable = false)
    private ProviderEntity provider;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private HospitalEntity hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id")
    private SpecialtyEntity specialty;

    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Column(name = "booking_reference", length = 30, nullable = false, unique = true)
    private String bookingReference;

    @Column(name = "visit_reason", length = 500)
    private String visitReason;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rescheduled_from_id")
    private AppointmentEntity rescheduledFrom;

    public AppointmentEntity() {
    }

    public AppointmentEntity(UUID id, PatientEntity patient, ProviderEntity provider,
                             HospitalEntity hospital, SpecialtyEntity specialty,
                             LocalDate appointmentDate, LocalTime startTime, LocalTime endTime,
                             AppointmentStatus status, String bookingReference, String visitReason) {
        this.id = id != null ? id : UUID.randomUUID();
        this.patient = patient;
        this.provider = provider;
        this.hospital = hospital;
        this.specialty = specialty;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status != null ? status : AppointmentStatus.SCHEDULED;
        this.bookingReference = bookingReference;
        this.visitReason = visitReason;
    }

    public AppointmentEntity(String bookingReference, PatientEntity patient, ProviderEntity provider,
                             HospitalEntity hospital, LocalDate appointmentDate, LocalTime startTime,
                             LocalTime endTime, AppointmentStatus status, String visitReason) {
        this(UUID.randomUUID(), patient, provider, hospital, null, appointmentDate, startTime, endTime, status, bookingReference, visitReason);
    }

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PatientEntity getPatient() {
        return patient;
    }

    public void setPatient(PatientEntity patient) {
        this.patient = patient;
    }

    public ProviderEntity getProvider() {
        return provider;
    }

    public void setProvider(ProviderEntity provider) {
        this.provider = provider;
    }

    public HospitalEntity getHospital() {
        return hospital;
    }

    public void setHospital(HospitalEntity hospital) {
        this.hospital = hospital;
    }

    public SpecialtyEntity getSpecialty() {
        return specialty;
    }

    public void setSpecialty(SpecialtyEntity specialty) {
        this.specialty = specialty;
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

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
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

    public AppointmentEntity getRescheduledFrom() {
        return rescheduledFrom;
    }

    public void setRescheduledFrom(AppointmentEntity rescheduledFrom) {
        this.rescheduledFrom = rescheduledFrom;
    }
}
