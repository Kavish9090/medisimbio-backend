package com.medisimbio.backend.appointment.service.impl;

import com.medisimbio.backend.appointment.dto.SlotResponseDto;
import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.entity.DoctorAvailabilityEntity;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.appointment.repository.DoctorAvailabilityRepository;
import com.medisimbio.backend.appointment.service.SlotService;
import com.medisimbio.backend.common.exception.BadRequestException;
import com.medisimbio.backend.common.exception.ResourceNotFoundException;
import com.medisimbio.backend.hospital.repository.HospitalRepository;
import com.medisimbio.backend.provider.repository.ProviderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of SlotService for calculating bookable consultation intervals.
 */
@Service
public class SlotServiceImpl implements SlotService {

    private static final Logger log = LoggerFactory.getLogger(SlotServiceImpl.class);
    public static final ZoneId DEFAULT_HEALTHCARE_ZONE = ZoneId.of("Asia/Kolkata");

    private final DoctorAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProviderRepository providerRepository;
    private final HospitalRepository hospitalRepository;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public SlotServiceImpl(DoctorAvailabilityRepository availabilityRepository,
                           AppointmentRepository appointmentRepository,
                           ProviderRepository providerRepository,
                           HospitalRepository hospitalRepository) {
        this(availabilityRepository, appointmentRepository, providerRepository, hospitalRepository, Clock.system(DEFAULT_HEALTHCARE_ZONE));
    }

    public SlotServiceImpl(DoctorAvailabilityRepository availabilityRepository,
                           AppointmentRepository appointmentRepository,
                           ProviderRepository providerRepository,
                           HospitalRepository hospitalRepository,
                           Clock clock) {
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
        this.providerRepository = providerRepository;
        this.hospitalRepository = hospitalRepository;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponseDto> getAvailableSlots(UUID providerId, UUID hospitalId, LocalDate date) {
        log.debug("Calculating available slots for provider [{}], hospital [{}], date [{}]", providerId, hospitalId, date);

        if (!providerRepository.existsById(providerId)) {
            throw new ResourceNotFoundException("Provider not found with ID: " + providerId);
        }
        if (hospitalId != null && !hospitalRepository.existsById(hospitalId)) {
            throw new ResourceNotFoundException("Hospital not found with ID: " + hospitalId);
        }

        LocalDate today = LocalDate.now(clock);
        LocalTime nowTime = LocalTime.now(clock);

        if (date.isBefore(today)) {
            throw new BadRequestException("Cannot query consultation slots for a date in the past: " + date);
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailabilityEntity> schedules;
        if (hospitalId != null) {
            schedules = availabilityRepository.findByProviderIdAndHospitalIdAndDayOfWeekAndActive(providerId, hospitalId, dayOfWeek, true);
        } else {
            schedules = availabilityRepository.findByProviderIdAndDayOfWeekAndActive(providerId, dayOfWeek, true);
        }

        if (schedules.isEmpty()) {
            return List.of();
        }

        // Fetch all active (non-cancelled) appointments for the doctor across ALL hospitals on this date
        List<AppointmentEntity> existingAppointments = appointmentRepository
                .findByProviderIdAndAppointmentDateAndStatusNot(providerId, date, AppointmentStatus.CANCELLED);

        List<SlotResponseDto> availableSlots = new ArrayList<>();

        for (DoctorAvailabilityEntity schedule : schedules) {
            LocalTime current = schedule.getStartTime();
            LocalTime shiftEnd = schedule.getEndTime();
            int durationMinutes = schedule.getSlotDurationMinutes();

            while (true) {
                LocalTime slotStart = current;
                LocalTime slotEnd = slotStart.plusMinutes(durationMinutes);

                // Slot end exceeds shift end window
                if (slotEnd.isAfter(shiftEnd)) {
                    break;
                }

                // If date is today, check if slot start is in the past
                boolean isPast = date.isEqual(today) && !slotStart.isAfter(nowTime);

                // Check if slot overlaps with any active appointment
                boolean isBooked = existingAppointments.stream().anyMatch(appt ->
                        slotStart.isBefore(appt.getEndTime()) && slotEnd.isAfter(appt.getStartTime()));

                boolean isAvailable = !isPast && !isBooked;
                availableSlots.add(new SlotResponseDto(date, slotStart, slotEnd, durationMinutes, isAvailable));

                current = slotEnd;
            }
        }

        availableSlots.sort(Comparator.comparing(SlotResponseDto::getStartTime));
        return availableSlots;
    }
}
