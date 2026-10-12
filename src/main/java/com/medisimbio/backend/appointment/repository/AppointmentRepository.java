package com.medisimbio.backend.appointment.repository;

import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for AppointmentEntity with Specification support.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<AppointmentEntity, UUID>, JpaSpecificationExecutor<AppointmentEntity> {

    Optional<AppointmentEntity> findByBookingReference(String bookingReference);

    boolean existsByBookingReference(String bookingReference);

    Page<AppointmentEntity> findByPatientId(UUID patientId, Pageable pageable);

    Page<AppointmentEntity> findByProviderId(UUID providerId, Pageable pageable);

    Page<AppointmentEntity> findByHospitalId(UUID hospitalId, Pageable pageable);

    List<AppointmentEntity> findByProviderIdAndAppointmentDateAndStatusNot(
            UUID providerId, LocalDate appointmentDate, AppointmentStatus status);

    boolean existsByProviderIdAndAppointmentDateAndStartTimeAndStatusNot(
            UUID providerId, LocalDate appointmentDate, LocalTime startTime, AppointmentStatus status);

    boolean existsByPatientIdAndAppointmentDateAndStartTimeAndStatusNot(
            UUID patientId, LocalDate appointmentDate, LocalTime startTime, AppointmentStatus status);

    @Query("SELECT a FROM AppointmentEntity a " +
           "JOIN FETCH a.patient " +
           "JOIN FETCH a.provider " +
           "JOIN FETCH a.hospital " +
           "LEFT JOIN FETCH a.specialty " +
           "WHERE a.id = :id")
    Optional<AppointmentEntity> findByIdWithDetails(@Param("id") UUID id);
}
