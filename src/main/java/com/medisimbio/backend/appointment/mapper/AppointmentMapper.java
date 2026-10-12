package com.medisimbio.backend.appointment.mapper;

import com.medisimbio.backend.appointment.dto.AppointmentResponseDto;
import com.medisimbio.backend.appointment.dto.AppointmentSummaryDto;
import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper for Appointment entities and DTOs.
 */
@Component
public class AppointmentMapper {

    public AppointmentResponseDto toResponseDto(AppointmentEntity entity) {
        if (entity == null) {
            return null;
        }

        return new AppointmentResponseDto(
                entity.getId(),
                entity.getBookingReference(),
                entity.getPatient() != null ? entity.getPatient().getId() : null,
                entity.getPatient() != null ? entity.getPatient().getFullName() : null,
                entity.getPatient() != null ? entity.getPatient().getMobileNumber() : null,
                entity.getProvider() != null ? entity.getProvider().getId() : null,
                entity.getProvider() != null ? entity.getProvider().getFullName() : null,
                entity.getProvider() != null ? entity.getProvider().getQualification() : null,
                entity.getHospital() != null ? entity.getHospital().getId() : null,
                entity.getHospital() != null ? entity.getHospital().getName() : null,
                entity.getHospital() != null ? entity.getHospital().getCity() : null,
                entity.getHospital() != null ? entity.getHospital().getAddress() : null,
                entity.getSpecialty() != null ? entity.getSpecialty().getId() : null,
                entity.getSpecialty() != null ? entity.getSpecialty().getDisplayName() : null,
                entity.getAppointmentDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus(),
                entity.getVisitReason(),
                entity.getCancellationReason(),
                entity.getRescheduledFrom() != null ? entity.getRescheduledFrom().getId() : null,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public AppointmentSummaryDto toSummaryDto(AppointmentEntity entity) {
        if (entity == null) {
            return null;
        }

        return new AppointmentSummaryDto(
                entity.getId(),
                entity.getBookingReference(),
                entity.getPatient() != null ? entity.getPatient().getId() : null,
                entity.getPatient() != null ? entity.getPatient().getFullName() : null,
                entity.getProvider() != null ? entity.getProvider().getId() : null,
                entity.getProvider() != null ? entity.getProvider().getFullName() : null,
                entity.getHospital() != null ? entity.getHospital().getId() : null,
                entity.getHospital() != null ? entity.getHospital().getName() : null,
                entity.getSpecialty() != null ? entity.getSpecialty().getDisplayName() : null,
                entity.getAppointmentDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
