package com.medisimbio.backend.appointment.service;

import com.medisimbio.backend.appointment.dto.*;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.common.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service interface for Patient Appointment lifecycle and management.
 */
public interface AppointmentService {

    AppointmentResponseDto bookAppointment(BookAppointmentRequestDto requestDto);

    AppointmentResponseDto bookAppointment(String authUserId, BookAppointmentRequestDto requestDto);

    PageResponse<AppointmentSummaryDto> getPatientAppointments(
            AppointmentStatus status, LocalDate date, LocalDate fromDate, LocalDate toDate, Pageable pageable);

    AppointmentResponseDto getPatientAppointmentById(UUID appointmentId);

    AppointmentResponseDto cancelAppointment(UUID appointmentId, CancelAppointmentRequestDto requestDto);

    AppointmentResponseDto rescheduleAppointment(UUID appointmentId, RescheduleAppointmentRequestDto requestDto);

    PageResponse<AppointmentSummaryDto> getProviderAppointments(
            UUID providerId, LocalDate date, AppointmentStatus status, UUID hospitalId, Pageable pageable);

    PageResponse<AppointmentSummaryDto> getHospitalAppointments(
            UUID hospitalId, LocalDate date, UUID providerId, AppointmentStatus status, Pageable pageable);
}
