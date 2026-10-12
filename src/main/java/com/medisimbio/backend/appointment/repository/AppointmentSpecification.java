package com.medisimbio.backend.appointment.repository;

import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Dynamic JPA Specification builder for Appointment queries.
 */
public final class AppointmentSpecification {

    private AppointmentSpecification() {
    }

    public static Specification<AppointmentEntity> withFilters(
            UUID patientId,
            UUID providerId,
            UUID hospitalId,
            AppointmentStatus status,
            LocalDate date,
            LocalDate fromDate,
            LocalDate toDate) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (patientId != null) {
                predicates.add(cb.equal(root.get("patient").get("id"), patientId));
            }

            if (providerId != null) {
                predicates.add(cb.equal(root.get("provider").get("id"), providerId));
            }

            if (hospitalId != null) {
                predicates.add(cb.equal(root.get("hospital").get("id"), hospitalId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (date != null) {
                predicates.add(cb.equal(root.get("appointmentDate"), date));
            }

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("appointmentDate"), fromDate));
            }

            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("appointmentDate"), toDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
