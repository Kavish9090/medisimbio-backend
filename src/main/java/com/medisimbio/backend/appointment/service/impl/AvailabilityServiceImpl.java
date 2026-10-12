package com.medisimbio.backend.appointment.service.impl;

import com.medisimbio.backend.appointment.dto.AvailabilityResponseDto;
import com.medisimbio.backend.appointment.dto.CreateAvailabilityRequestDto;
import com.medisimbio.backend.appointment.dto.UpdateAvailabilityRequestDto;
import com.medisimbio.backend.appointment.entity.DoctorAvailabilityEntity;
import com.medisimbio.backend.appointment.mapper.AvailabilityMapper;
import com.medisimbio.backend.appointment.repository.DoctorAvailabilityRepository;
import com.medisimbio.backend.appointment.service.AvailabilityService;
import com.medisimbio.backend.common.exception.BadRequestException;
import com.medisimbio.backend.common.exception.ResourceAlreadyExistsException;
import com.medisimbio.backend.common.exception.ResourceNotFoundException;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.repository.HospitalRepository;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.provider.entity.ProviderHospitalEntity;
import com.medisimbio.backend.provider.repository.ProviderHospitalRepository;
import com.medisimbio.backend.provider.repository.ProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AvailabilityService for doctor scheduling.
 */
@Service
public class AvailabilityServiceImpl implements AvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(AvailabilityServiceImpl.class);

    private final DoctorAvailabilityRepository availabilityRepository;
    private final ProviderRepository providerRepository;
    private final HospitalRepository hospitalRepository;
    private final ProviderHospitalRepository providerHospitalRepository;
    private final AvailabilityMapper availabilityMapper;

    public AvailabilityServiceImpl(DoctorAvailabilityRepository availabilityRepository,
                                   ProviderRepository providerRepository,
                                   HospitalRepository hospitalRepository,
                                   ProviderHospitalRepository providerHospitalRepository,
                                   AvailabilityMapper availabilityMapper) {
        this.availabilityRepository = availabilityRepository;
        this.providerRepository = providerRepository;
        this.hospitalRepository = hospitalRepository;
        this.providerHospitalRepository = providerHospitalRepository;
        this.availabilityMapper = availabilityMapper;
    }

    @Override
    @Transactional
    public AvailabilityResponseDto createAvailability(UUID providerId, CreateAvailabilityRequestDto requestDto) {
        log.info("Creating availability schedule for provider [{}] at hospital [{}] on [{}]",
                providerId, requestDto.getHospitalId(), requestDto.getDayOfWeek());

        validateTimeRange(requestDto.getStartTime(), requestDto.getEndTime(), requestDto.getSlotDurationMinutes());

        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + providerId));

        HospitalEntity hospital = hospitalRepository.findById(requestDto.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + requestDto.getHospitalId()));

        validateAffiliation(providerId, requestDto.getHospitalId());

        validateNoOverlappingAvailability(providerId, null, requestDto.getDayOfWeek(),
                requestDto.getStartTime(), requestDto.getEndTime());

        DoctorAvailabilityEntity entity = availabilityMapper.toEntity(requestDto, provider, hospital);
        DoctorAvailabilityEntity saved = availabilityRepository.save(entity);

        log.info("Doctor availability created successfully with ID [{}]", saved.getId());
        return availabilityMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public AvailabilityResponseDto updateAvailability(UUID providerId, UUID availabilityId, UpdateAvailabilityRequestDto requestDto) {
        log.info("Updating availability schedule [{}] for provider [{}]", availabilityId, providerId);

        DoctorAvailabilityEntity entity = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability schedule not found with ID: " + availabilityId));

        if (!entity.getProvider().getId().equals(providerId)) {
            throw new BadRequestException("Availability schedule does not belong to provider ID: " + providerId);
        }

        HospitalEntity newHospital = null;
        if (requestDto.getHospitalId() != null) {
            newHospital = hospitalRepository.findById(requestDto.getHospitalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + requestDto.getHospitalId()));
            validateAffiliation(providerId, requestDto.getHospitalId());
        }

        LocalTime startTime = requestDto.getStartTime() != null ? requestDto.getStartTime() : entity.getStartTime();
        LocalTime endTime = requestDto.getEndTime() != null ? requestDto.getEndTime() : entity.getEndTime();
        int slotDuration = requestDto.getSlotDurationMinutes() != null ? requestDto.getSlotDurationMinutes() : entity.getSlotDurationMinutes();
        DayOfWeek dayOfWeek = requestDto.getDayOfWeek() != null ? requestDto.getDayOfWeek() : entity.getDayOfWeek();

        validateTimeRange(startTime, endTime, slotDuration);

        if (Boolean.TRUE.equals(requestDto.getActive()) || (requestDto.getActive() == null && entity.isActive())) {
            validateNoOverlappingAvailability(providerId, availabilityId, dayOfWeek, startTime, endTime);
        }

        availabilityMapper.updateEntityFromDto(requestDto, entity, newHospital);
        DoctorAvailabilityEntity updated = availabilityRepository.save(entity);

        log.info("Doctor availability [{}] updated successfully", updated.getId());
        return availabilityMapper.toResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AvailabilityResponseDto> getAvailabilities(UUID providerId, UUID hospitalId, DayOfWeek dayOfWeek, Boolean active) {
        if (!providerRepository.existsById(providerId)) {
            throw new ResourceNotFoundException("Provider not found with ID: " + providerId);
        }

        List<DoctorAvailabilityEntity> list = availabilityRepository.findAvailabilitiesWithFilters(
                providerId, hospitalId, dayOfWeek, active);

        return list.stream()
                .map(availabilityMapper::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AvailabilityResponseDto getAvailabilityById(UUID providerId, UUID availabilityId) {
        DoctorAvailabilityEntity entity = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability schedule not found with ID: " + availabilityId));

        if (!entity.getProvider().getId().equals(providerId)) {
            throw new BadRequestException("Availability schedule does not belong to provider ID: " + providerId);
        }

        return availabilityMapper.toResponseDto(entity);
    }

    @Override
    @Transactional
    public void deleteAvailability(UUID providerId, UUID availabilityId) {
        log.info("Deleting availability schedule [{}] for provider [{}]", availabilityId, providerId);

        DoctorAvailabilityEntity entity = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new ResourceNotFoundException("Availability schedule not found with ID: " + availabilityId));

        if (!entity.getProvider().getId().equals(providerId)) {
            throw new BadRequestException("Availability schedule does not belong to provider ID: " + providerId);
        }

        availabilityRepository.delete(entity);
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime, Integer slotDurationMinutes) {
        if (!startTime.isBefore(endTime)) {
            throw new BadRequestException("Availability start time (" + startTime + ") must be before end time (" + endTime + ")");
        }
        long totalMinutes = Duration.between(startTime, endTime).toMinutes();
        if (slotDurationMinutes != null && slotDurationMinutes > totalMinutes) {
            throw new BadRequestException("Slot duration (" + slotDurationMinutes + "m) cannot exceed total shift window (" + totalMinutes + "m)");
        }
    }

    private void validateAffiliation(UUID providerId, UUID hospitalId) {
        ProviderHospitalEntity affiliation = providerHospitalRepository
                .findByProviderIdAndHospitalId(providerId, hospitalId)
                .orElseThrow(() -> new BadRequestException("Provider is not affiliated with hospital ID: " + hospitalId));

        if (!affiliation.isActive()) {
            throw new BadRequestException("Provider hospital affiliation is currently inactive");
        }
    }

    private void validateNoOverlappingAvailability(UUID providerId, UUID currentAvailabilityId,
                                                   DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        List<DoctorAvailabilityEntity> existing = availabilityRepository
                .findByProviderIdAndDayOfWeekAndActive(providerId, dayOfWeek, true);

        for (DoctorAvailabilityEntity a : existing) {
            if (currentAvailabilityId != null && a.getId().equals(currentAvailabilityId)) {
                continue;
            }
            if (startTime.isBefore(a.getEndTime()) && endTime.isAfter(a.getStartTime())) {
                throw new ResourceAlreadyExistsException("Doctor already has an overlapping active schedule on " +
                        dayOfWeek + " between " + a.getStartTime() + " and " + a.getEndTime() +
                        " at hospital: " + a.getHospital().getName());
            }
        }
    }
}
