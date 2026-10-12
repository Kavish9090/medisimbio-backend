package com.medisimbio.backend.appointment.service.impl;

import com.medisimbio.backend.appointment.dto.*;
import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.entity.DoctorAvailabilityEntity;
import com.medisimbio.backend.appointment.generator.BookingReferenceGenerator;
import com.medisimbio.backend.appointment.mapper.AppointmentMapper;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.appointment.repository.AppointmentSpecification;
import com.medisimbio.backend.appointment.repository.DoctorAvailabilityRepository;
import com.medisimbio.backend.appointment.service.AppointmentService;
import com.medisimbio.backend.common.exception.*;
import com.medisimbio.backend.common.response.PageResponse;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.repository.HospitalRepository;
import com.medisimbio.backend.hospital.repository.HospitalStaffRepository;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.patient.repository.PatientRepository;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.provider.entity.ProviderHospitalEntity;
import com.medisimbio.backend.provider.repository.ProviderHospitalRepository;
import com.medisimbio.backend.provider.repository.ProviderRepository;
import com.medisimbio.backend.security.AuthenticationFacade;
import com.medisimbio.backend.specialty.entity.SpecialtyEntity;
import com.medisimbio.backend.specialty.repository.SpecialtyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of AppointmentService for patient booking and consultation scheduling.
 */
@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentServiceImpl.class);
    public static final ZoneId DEFAULT_HEALTHCARE_ZONE = ZoneId.of("Asia/Kolkata");

    private final AppointmentRepository appointmentRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final PatientRepository patientRepository;
    private final ProviderRepository providerRepository;
    private final HospitalRepository hospitalRepository;
    private final HospitalStaffRepository hospitalStaffRepository;
    private final SpecialtyRepository specialtyRepository;
    private final ProviderHospitalRepository providerHospitalRepository;
    private final AuthenticationFacade authenticationFacade;
    private final BookingReferenceGenerator bookingReferenceGenerator;
    private final AppointmentMapper appointmentMapper;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  DoctorAvailabilityRepository availabilityRepository,
                                  PatientRepository patientRepository,
                                  ProviderRepository providerRepository,
                                  HospitalRepository hospitalRepository,
                                  HospitalStaffRepository hospitalStaffRepository,
                                  SpecialtyRepository specialtyRepository,
                                  ProviderHospitalRepository providerHospitalRepository,
                                  AuthenticationFacade authenticationFacade,
                                  BookingReferenceGenerator bookingReferenceGenerator,
                                  AppointmentMapper appointmentMapper) {
        this(appointmentRepository, availabilityRepository, patientRepository,
                providerRepository, hospitalRepository, hospitalStaffRepository,
                specialtyRepository, providerHospitalRepository, authenticationFacade,
                bookingReferenceGenerator, appointmentMapper,
                Clock.system(DEFAULT_HEALTHCARE_ZONE));
    }

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  DoctorAvailabilityRepository availabilityRepository,
                                  PatientRepository patientRepository,
                                  ProviderRepository providerRepository,
                                  HospitalRepository hospitalRepository,
                                  HospitalStaffRepository hospitalStaffRepository,
                                  SpecialtyRepository specialtyRepository,
                                  ProviderHospitalRepository providerHospitalRepository,
                                  AuthenticationFacade authenticationFacade,
                                  BookingReferenceGenerator bookingReferenceGenerator,
                                  AppointmentMapper appointmentMapper,
                                  Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.availabilityRepository = availabilityRepository;
        this.patientRepository = patientRepository;
        this.providerRepository = providerRepository;
        this.hospitalRepository = hospitalRepository;
        this.hospitalStaffRepository = hospitalStaffRepository;
        this.specialtyRepository = specialtyRepository;
        this.providerHospitalRepository = providerHospitalRepository;
        this.authenticationFacade = authenticationFacade;
        this.bookingReferenceGenerator = bookingReferenceGenerator;
        this.appointmentMapper = appointmentMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public AppointmentResponseDto bookAppointment(BookAppointmentRequestDto requestDto) {
        return bookAppointment(authenticationFacade.getRequiredCurrentUserId(), requestDto);
    }

    @Override
    @Transactional
    public AppointmentResponseDto bookAppointment(String authUserId, BookAppointmentRequestDto requestDto) {
        PatientEntity patient = getPatientByAuthUserId(authUserId);

        log.info("Booking appointment for patient [{}] with provider [{}] at hospital [{}] on [{}] at [{}]",
                patient.getId(), requestDto.getProviderId(), requestDto.getHospitalId(),
                requestDto.getAppointmentDate(), requestDto.getStartTime());

        ProviderEntity provider = providerRepository.findById(requestDto.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + requestDto.getProviderId()));

        if (!provider.isActive()) {
            throw new BadRequestException("Provider is not currently active for consultations");
        }

        HospitalEntity hospital = hospitalRepository.findById(requestDto.getHospitalId())
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + requestDto.getHospitalId()));

        if (!hospital.isActive()) {
            throw new BadRequestException("Hospital is not currently active");
        }

        validateAffiliation(provider.getId(), hospital.getId());

        SpecialtyEntity specialty = null;
        if (requestDto.getSpecialtyId() != null) {
            specialty = specialtyRepository.findById(requestDto.getSpecialtyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with ID: " + requestDto.getSpecialtyId()));

            SpecialtyEntity finalSpecialty = specialty;
            boolean matches = provider.getSpecialties().stream()
                    .anyMatch(s -> s.getId().equals(finalSpecialty.getId()));
            if (!matches) {
                throw new BadRequestException("Doctor does not specialize in the requested specialty: " + specialty.getDisplayName());
            }
        }

        validateDateTimeNotInPast(requestDto.getAppointmentDate(), requestDto.getStartTime());

        DoctorAvailabilityEntity matchingSchedule = findMatchingAvailability(
                provider.getId(), hospital.getId(), requestDto.getAppointmentDate(), requestDto.getStartTime());

        LocalTime endTime = requestDto.getStartTime().plusMinutes(matchingSchedule.getSlotDurationMinutes());

        String doctorSlotLock = ("LOCK_DOCTOR_SLOT:" + provider.getId() + ":" + requestDto.getAppointmentDate() + ":" + requestDto.getStartTime()).intern();
        synchronized (doctorSlotLock) {
            validateNoOverlappingDoctorAppointment(provider.getId(), requestDto.getAppointmentDate(),
                    requestDto.getStartTime(), endTime, null);

            validateNoOverlappingPatientAppointment(patient.getId(), requestDto.getAppointmentDate(),
                    requestDto.getStartTime(), endTime, null);

            String bookingRef = generateUniqueBookingReference(requestDto.getAppointmentDate());

            AppointmentEntity entity = new AppointmentEntity(
                    UUID.randomUUID(),
                    patient,
                    provider,
                    hospital,
                    specialty,
                    requestDto.getAppointmentDate(),
                    requestDto.getStartTime(),
                    endTime,
                    AppointmentStatus.SCHEDULED,
                    bookingRef,
                    requestDto.getVisitReason() != null ? requestDto.getVisitReason().trim() : null
            );

            try {
                AppointmentEntity saved = appointmentRepository.saveAndFlush(entity);
                log.info("Appointment booked successfully with reference [{}] and ID [{}]", saved.getBookingReference(), saved.getId());
                return appointmentMapper.toResponseDto(saved);
            } catch (DataIntegrityViolationException ex) {
                log.warn("Concurrent double-booking detected for slot [{}] on [{}]: {}",
                        requestDto.getStartTime(), requestDto.getAppointmentDate(), ex.getMessage());
                throw new ResourceAlreadyExistsException("This appointment slot has already been booked by another patient. Please select another slot.");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentSummaryDto> getPatientAppointments(
            AppointmentStatus status, LocalDate date, LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        PatientEntity patient = getAuthenticatedPatient();

        Specification<AppointmentEntity> spec = AppointmentSpecification.withFilters(
                patient.getId(), null, null, status, date, fromDate, toDate);

        Page<AppointmentEntity> page = appointmentRepository.findAll(spec, pageable);
        Page<AppointmentSummaryDto> dtoPage = page.map(appointmentMapper::toSummaryDto);
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDto getPatientAppointmentById(UUID appointmentId) {
        PatientEntity patient = getAuthenticatedPatient();

        AppointmentEntity entity = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        if (!entity.getPatient().getId().equals(patient.getId())) {
            throw new ForbiddenException("Access denied: You are not authorized to view this appointment");
        }

        return appointmentMapper.toResponseDto(entity);
    }

    @Override
    @Transactional
    public AppointmentResponseDto cancelAppointment(UUID appointmentId, CancelAppointmentRequestDto requestDto) {
        PatientEntity patient = getAuthenticatedPatient();

        log.info("Cancelling appointment [{}] for patient [{}]", appointmentId, patient.getId());

        AppointmentEntity entity = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        if (!entity.getPatient().getId().equals(patient.getId())) {
            throw new ForbiddenException("Access denied: You are not authorized to cancel this appointment");
        }

        if (!entity.getStatus().canCancel()) {
            throw new BadRequestException("Appointment cannot be cancelled in status: " + entity.getStatus());
        }

        entity.setStatus(AppointmentStatus.CANCELLED);
        entity.setCancellationReason(requestDto != null && requestDto.getCancellationReason() != null
                ? requestDto.getCancellationReason().trim()
                : "Cancelled by patient");

        AppointmentEntity updated = appointmentRepository.save(entity);
        log.info("Appointment [{}] cancelled successfully", appointmentId);
        return appointmentMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public AppointmentResponseDto rescheduleAppointment(UUID appointmentId, RescheduleAppointmentRequestDto requestDto) {
        PatientEntity patient = getAuthenticatedPatient();

        log.info("Rescheduling appointment [{}] for patient [{}] to [{}] at [{}]",
                appointmentId, patient.getId(), requestDto.getNewDate(), requestDto.getNewStartTime());

        AppointmentEntity oldAppointment = appointmentRepository.findByIdWithDetails(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + appointmentId));

        if (!oldAppointment.getPatient().getId().equals(patient.getId())) {
            throw new ForbiddenException("Access denied: You are not authorized to reschedule this appointment");
        }

        if (!oldAppointment.getStatus().canReschedule()) {
            throw new BadRequestException("Appointment cannot be rescheduled in status: " + oldAppointment.getStatus());
        }

        ProviderEntity provider = oldAppointment.getProvider();
        UUID hospitalId = requestDto.getNewHospitalId() != null
                ? requestDto.getNewHospitalId()
                : oldAppointment.getHospital().getId();

        HospitalEntity hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + hospitalId));

        validateAffiliation(provider.getId(), hospital.getId());
        validateDateTimeNotInPast(requestDto.getNewDate(), requestDto.getNewStartTime());

        DoctorAvailabilityEntity matchingSchedule = findMatchingAvailability(
                provider.getId(), hospital.getId(), requestDto.getNewDate(), requestDto.getNewStartTime());

        LocalTime newEndTime = requestDto.getNewStartTime().plusMinutes(matchingSchedule.getSlotDurationMinutes());

        validateNoOverlappingDoctorAppointment(provider.getId(), requestDto.getNewDate(),
                requestDto.getNewStartTime(), newEndTime, oldAppointment.getId());

        validateNoOverlappingPatientAppointment(patient.getId(), requestDto.getNewDate(),
                requestDto.getNewStartTime(), newEndTime, oldAppointment.getId());

        String newBookingRef = generateUniqueBookingReference(requestDto.getNewDate());

        AppointmentEntity newAppointment = new AppointmentEntity(
                UUID.randomUUID(),
                patient,
                provider,
                hospital,
                oldAppointment.getSpecialty(),
                requestDto.getNewDate(),
                requestDto.getNewStartTime(),
                newEndTime,
                AppointmentStatus.SCHEDULED,
                newBookingRef,
                oldAppointment.getVisitReason()
        );
        newAppointment.setRescheduledFrom(oldAppointment);

        oldAppointment.setStatus(AppointmentStatus.CANCELLED);
        oldAppointment.setCancellationReason("Rescheduled to appointment " + newBookingRef);
        appointmentRepository.save(oldAppointment);

        try {
            AppointmentEntity saved = appointmentRepository.saveAndFlush(newAppointment);
            log.info("Appointment rescheduled successfully. Old [{}], New reference [{}]",
                    oldAppointment.getBookingReference(), saved.getBookingReference());
            return appointmentMapper.toResponseDto(saved);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Concurrent double-booking detected during reschedule: {}", ex.getMessage());
            throw new ResourceAlreadyExistsException("The new appointment slot has already been booked by another patient. Please choose another slot.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentSummaryDto> getProviderAppointments(
            UUID providerId, LocalDate date, AppointmentStatus status, UUID hospitalId, Pageable pageable) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();

        ProviderEntity provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found with ID: " + providerId));

        if (!isAuthorizedForProvider(currentUserId, provider)) {
            throw new ForbiddenException("Access denied: You are not authorized to view appointments for this provider");
        }

        Specification<AppointmentEntity> spec = AppointmentSpecification.withFilters(
                null, providerId, hospitalId, status, date, null, null);

        Page<AppointmentEntity> page = appointmentRepository.findAll(spec, pageable);
        Page<AppointmentSummaryDto> dtoPage = page.map(appointmentMapper::toSummaryDto);
        return PageResponse.from(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentSummaryDto> getHospitalAppointments(
            UUID hospitalId, LocalDate date, UUID providerId, AppointmentStatus status, Pageable pageable) {
        String currentUserId = authenticationFacade.getRequiredCurrentUserId();

        HospitalEntity hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with ID: " + hospitalId));

        if (!isAuthorizedForHospital(currentUserId, hospital)) {
            throw new ForbiddenException("Access denied: You are not authorized to view appointments for this hospital facility");
        }

        Specification<AppointmentEntity> spec = AppointmentSpecification.withFilters(
                null, providerId, hospitalId, status, date, null, null);

        Page<AppointmentEntity> page = appointmentRepository.findAll(spec, pageable);
        Page<AppointmentSummaryDto> dtoPage = page.map(appointmentMapper::toSummaryDto);
        return PageResponse.from(dtoPage);
    }

    private boolean isAuthorizedForProvider(String currentUserId, ProviderEntity provider) {
        if (currentUserId == null || currentUserId.isBlank() || provider == null) {
            return false;
        }
        return provider.getAuthUserId() != null && provider.getAuthUserId().equals(currentUserId);
    }

    private boolean isAuthorizedForHospital(String currentUserId, HospitalEntity hospital) {
        if (currentUserId == null || currentUserId.isBlank() || hospital == null || hospital.getId() == null) {
            return false;
        }
        return hospitalStaffRepository.existsByHospitalIdAndAuthUserIdAndActiveTrue(hospital.getId(), currentUserId);
    }

    private PatientEntity getPatientByAuthUserId(String authUserId) {
        if (authUserId == null || authUserId.isBlank()) {
            throw new BusinessException("Authentication is required to perform this action");
        }
        return patientRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new BusinessException("Patient profile not found. Please complete your profile before managing appointments."));
    }

    private PatientEntity getAuthenticatedPatient() {
        return getPatientByAuthUserId(authenticationFacade.getRequiredCurrentUserId());
    }

    private void validateAffiliation(UUID providerId, UUID hospitalId) {
        ProviderHospitalEntity affiliation = providerHospitalRepository
                .findByProviderIdAndHospitalId(providerId, hospitalId)
                .orElseThrow(() -> new BadRequestException("Doctor is not affiliated with the selected hospital"));

        if (!affiliation.isActive()) {
            throw new BadRequestException("Doctor affiliation with this hospital is currently inactive");
        }
    }

    private void validateDateTimeNotInPast(LocalDate date, LocalTime startTime) {
        LocalDate today = LocalDate.now(clock);
        LocalTime nowTime = LocalTime.now(clock);

        if (date.isBefore(today)) {
            throw new BadRequestException("Cannot schedule an appointment for a date in the past: " + date);
        }
        if (date.isEqual(today) && !startTime.isAfter(nowTime)) {
            throw new BadRequestException("Cannot schedule an appointment for a time slot in the past: " + startTime);
        }
    }

    private DoctorAvailabilityEntity findMatchingAvailability(UUID providerId, UUID hospitalId, LocalDate date, LocalTime startTime) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<DoctorAvailabilityEntity> schedules = availabilityRepository
                .findByProviderIdAndHospitalIdAndDayOfWeekAndActive(providerId, hospitalId, dayOfWeek, true);

        for (DoctorAvailabilityEntity schedule : schedules) {
            LocalTime shiftStart = schedule.getStartTime();
            LocalTime shiftEnd = schedule.getEndTime();
            int durationMinutes = schedule.getSlotDurationMinutes();

            LocalTime current = shiftStart;
            while (!current.plusMinutes(durationMinutes).isAfter(shiftEnd)) {
                if (current.equals(startTime)) {
                    return schedule;
                }
                current = current.plusMinutes(durationMinutes);
            }
        }

        throw new BadRequestException("The doctor is not available for consultation at this hospital on " +
                dayOfWeek + " at " + startTime);
    }

    private void validateNoOverlappingDoctorAppointment(UUID providerId, LocalDate date, LocalTime startTime,
                                                        LocalTime endTime, UUID excludedAppointmentId) {
        List<AppointmentEntity> doctorAppointments = appointmentRepository
                .findByProviderIdAndAppointmentDateAndStatusNot(providerId, date, AppointmentStatus.CANCELLED);

        for (AppointmentEntity appt : doctorAppointments) {
            if (excludedAppointmentId != null && appt.getId().equals(excludedAppointmentId)) {
                continue;
            }
            if (startTime.isBefore(appt.getEndTime()) && endTime.isAfter(appt.getStartTime())) {
                throw new ResourceAlreadyExistsException("This appointment slot has already been booked by another patient. Please select another slot.");
            }
        }
    }

    private void validateNoOverlappingPatientAppointment(UUID patientId, LocalDate date, LocalTime startTime,
                                                         LocalTime endTime, UUID excludedAppointmentId) {
        Specification<AppointmentEntity> spec = AppointmentSpecification.withFilters(
                patientId, null, null, null, date, null, null);

        List<AppointmentEntity> patientAppointments = appointmentRepository.findAll(spec);

        for (AppointmentEntity appt : patientAppointments) {
            if (appt.getStatus() == AppointmentStatus.CANCELLED) {
                continue;
            }
            if (excludedAppointmentId != null && appt.getId().equals(excludedAppointmentId)) {
                continue;
            }
            if (startTime.isBefore(appt.getEndTime()) && endTime.isAfter(appt.getStartTime())) {
                throw new BusinessException("You already have an active appointment scheduled on " +
                        date + " at " + appt.getStartTime() + " with " + appt.getProvider().getFullName());
            }
        }
    }

    private String generateUniqueBookingReference(LocalDate appointmentDate) {
        for (int i = 0; i < 10; i++) {
            String ref = bookingReferenceGenerator.generateReference(appointmentDate);
            if (!appointmentRepository.existsByBookingReference(ref)) {
                return ref;
            }
        }
        return "MSB-" + LocalDate.now(clock).toString().replace("-", "") + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
