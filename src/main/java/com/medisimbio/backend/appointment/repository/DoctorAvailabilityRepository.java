package com.medisimbio.backend.appointment.repository;

import com.medisimbio.backend.appointment.entity.DoctorAvailabilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for DoctorAvailabilityEntity schedules.
 */
@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailabilityEntity, UUID> {

    List<DoctorAvailabilityEntity> findByProviderId(UUID providerId);

    List<DoctorAvailabilityEntity> findByProviderIdAndActive(UUID providerId, boolean active);

    List<DoctorAvailabilityEntity> findByProviderIdAndHospitalId(UUID providerId, UUID hospitalId);

    List<DoctorAvailabilityEntity> findByProviderIdAndHospitalIdAndActive(UUID providerId, UUID hospitalId, boolean active);

    List<DoctorAvailabilityEntity> findByProviderIdAndHospitalIdAndDayOfWeekAndActive(
            UUID providerId, UUID hospitalId, DayOfWeek dayOfWeek, boolean active);

    List<DoctorAvailabilityEntity> findByProviderIdAndDayOfWeekAndActive(
            UUID providerId, DayOfWeek dayOfWeek, boolean active);

    @Query("SELECT a FROM DoctorAvailabilityEntity a " +
           "JOIN FETCH a.provider " +
           "JOIN FETCH a.hospital " +
           "WHERE a.provider.id = :providerId " +
           "AND (:hospitalId IS NULL OR a.hospital.id = :hospitalId) " +
           "AND (:dayOfWeek IS NULL OR a.dayOfWeek = :dayOfWeek) " +
           "AND (:active IS NULL OR a.active = :active) " +
           "ORDER BY a.dayOfWeek ASC, a.startTime ASC")
    List<DoctorAvailabilityEntity> findAvailabilitiesWithFilters(
            @Param("providerId") UUID providerId,
            @Param("hospitalId") UUID hospitalId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("active") Boolean active);
}
