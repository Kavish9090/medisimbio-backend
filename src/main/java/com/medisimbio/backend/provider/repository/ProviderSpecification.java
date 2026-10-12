package com.medisimbio.backend.provider.repository;

import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.provider.entity.ProviderHospitalEntity;
import com.medisimbio.backend.specialty.entity.SpecialtyEntity;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Dynamic JPA Specification builder for Provider queries.
 */
public final class ProviderSpecification {

    private ProviderSpecification() {
    }

    public static Specification<ProviderEntity> withFilters(
            UUID specialtyId,
            UUID hospitalId,
            String city,
            Boolean active,
            String search) {

        return (root, query, cb) -> {
            // Ensure distinct results when joining collections
            if (query != null && Long.class != query.getResultType() && long.class != query.getResultType()) {
                query.distinct(true);
            }

            List<Predicate> predicates = new ArrayList<>();

            if (active != null) {
                predicates.add(cb.equal(root.get("active"), active));
            }

            if (specialtyId != null) {
                Join<ProviderEntity, SpecialtyEntity> specialtyJoin = root.join("specialties", JoinType.INNER);
                predicates.add(cb.equal(specialtyJoin.get("id"), specialtyId));
            }

            if (hospitalId != null || (city != null && !city.isBlank())) {
                Join<ProviderEntity, ProviderHospitalEntity> hospitalAffiliationJoin = root.join("hospitalAffiliations", JoinType.INNER);
                
                if (hospitalId != null) {
                    predicates.add(cb.equal(hospitalAffiliationJoin.get("hospital").get("id"), hospitalId));
                    predicates.add(cb.isTrue(hospitalAffiliationJoin.get("active")));
                }

                if (city != null && !city.isBlank()) {
                    Join<ProviderHospitalEntity, HospitalEntity> hospitalJoin = hospitalAffiliationJoin.join("hospital", JoinType.INNER);
                    predicates.add(cb.equal(cb.lower(hospitalJoin.get("city")), city.trim().toLowerCase()));
                    predicates.add(cb.isTrue(hospitalAffiliationJoin.get("active")));
                }
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("fullName")), pattern);
                Predicate qualMatch = cb.like(cb.lower(root.get("qualification")), pattern);
                Predicate regMatch = cb.like(cb.lower(root.get("professionalRegistrationNumber")), pattern);
                predicates.add(cb.or(nameMatch, qualMatch, regMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
