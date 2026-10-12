package com.medisimbio.backend.encounter.repository;

import com.medisimbio.backend.encounter.entity.EncounterEntity;
import com.medisimbio.backend.encounter.entity.EncounterStatus;
import com.medisimbio.backend.encounter.entity.EncounterType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA Specifications for dynamic filtering of clinical encounters.
 */
public final class EncounterSpecification {

    private EncounterSpecification() {
    }

    public static Specification<EncounterEntity> withFilters(
            UUID patientId, UUID providerId, UUID hospitalId, EncounterStatus status,
            EncounterType encounterType, Instant fromDate, Instant toDate) {

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

            if (encounterType != null) {
                predicates.add(cb.equal(root.get("encounterType"), encounterType));
            }

            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
            }

            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
