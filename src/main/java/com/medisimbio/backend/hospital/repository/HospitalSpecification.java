package com.medisimbio.backend.hospital.repository;

import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.entity.HospitalType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Dynamic JPA Specification builder for Hospital queries.
 */
public final class HospitalSpecification {

    private HospitalSpecification() {
    }

    public static Specification<HospitalEntity> withFilters(
            HospitalType type,
            String city,
            String state,
            Boolean active,
            String search) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(cb.equal(root.get("hospitalType"), type));
            }

            if (city != null && !city.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("city")), city.trim().toLowerCase()));
            }

            if (state != null && !state.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("state")), state.trim().toLowerCase()));
            }

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
                Predicate cityMatch = cb.like(cb.lower(root.get("city")), pattern);
                Predicate addressMatch = cb.like(cb.lower(root.get("address")), pattern);
                predicates.add(cb.or(nameMatch, cityMatch, addressMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
