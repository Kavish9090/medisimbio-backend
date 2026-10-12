package com.medisimbio.backend.appointment;

import com.medisimbio.backend.appointment.dto.BookAppointmentRequestDto;
import com.medisimbio.backend.appointment.dto.AppointmentResponseDto;
import com.medisimbio.backend.appointment.entity.DoctorAvailabilityEntity;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.appointment.repository.DoctorAvailabilityRepository;
import com.medisimbio.backend.appointment.service.AppointmentService;
import com.medisimbio.backend.common.exception.ResourceAlreadyExistsException;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.entity.HospitalType;
import com.medisimbio.backend.hospital.repository.HospitalRepository;
import com.medisimbio.backend.patient.entity.Gender;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.patient.repository.PatientRepository;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.provider.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AppointmentConcurrencyTest {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private com.medisimbio.backend.provider.repository.ProviderHospitalRepository providerHospitalRepository;

    private ProviderEntity testProvider;
    private HospitalEntity testHospital;
    private final List<PatientEntity> testPatients = new ArrayList<>();

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        providerHospitalRepository.deleteAll();
        patientRepository.deleteAll();
        providerRepository.deleteAll();
        hospitalRepository.deleteAll();
        testPatients.clear();

        // Seed hospital
        testHospital = new HospitalEntity(
                UUID.randomUUID(),
                "Max Healthcare Concurrency Hub",
                HospitalType.PRIVATE,
                "+91-20-99887766",
                "100 Concurrency Ave",
                "Pune",
                "Maharashtra",
                "411001"
        );
        testHospital = hospitalRepository.save(testHospital);

        // Seed provider
        testProvider = new ProviderEntity(
                UUID.randomUUID(),
                "Dr. Neha Kulkarni",
                "MMC-TEST-CONC-1",
                "MBBS, MD (Pediatrics)",
                8,
                "dr.neha@example.com",
                "+91-9988776655"
        );
        testProvider = providerRepository.save(testProvider);

        // Affiliate provider with hospital
        com.medisimbio.backend.provider.entity.ProviderHospitalEntity affiliation =
                new com.medisimbio.backend.provider.entity.ProviderHospitalEntity(
                        UUID.randomUUID(),
                        testProvider,
                        testHospital,
                        "Pediatrics",
                        new java.math.BigDecimal("550.00"),
                        true
                );
        providerHospitalRepository.save(affiliation);

        // Seed availability for Friday 09:00 - 12:00
        DoctorAvailabilityEntity availability = new DoctorAvailabilityEntity(
                testProvider,
                testHospital,
                DayOfWeek.FRIDAY,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                30
        );
        availabilityRepository.save(availability);

        // Seed 8 distinct patients for concurrent booking attempts
        for (int i = 1; i <= 8; i++) {
            PatientEntity patient = new PatientEntity(
                    UUID.randomUUID(),
                    "auth_conc_user_" + i,
                    "Concurrent Patient " + i,
                    "conc.patient" + i + "@example.com",
                    "+91-980000000" + i,
                    LocalDate.of(1990, 1, i),
                    Gender.OTHER
            );
            testPatients.add(patientRepository.save(patient));
        }
    }

    @Test
    @DisplayName("High Concurrency: Simultaneous booking attempts for identical slot result in exactly 1 winner and 7 conflicts")
    void shouldPreventDoubleBookingUnderConcurrentLoad() throws InterruptedException {
        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        LocalDate targetFriday = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        LocalTime startTime = LocalTime.of(9, 30);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            final String authUserId = testPatients.get(i).getAuthUserId();
            executor.submit(() -> {
                try {
                    startGate.await(); // Synchronize all threads to shoot at the exact same instant
                    BookAppointmentRequestDto request = new BookAppointmentRequestDto(
                            testProvider.getId(),
                            testHospital.getId(),
                            targetFriday,
                            startTime,
                            "Concurrent load test booking"
                    );
                    AppointmentResponseDto response = appointmentService.bookAppointment(authUserId, request);
                    if (response != null && response.getId() != null) {
                        successCount.incrementAndGet();
                    }
                } catch (ResourceAlreadyExistsException e) {
                    conflictCount.incrementAndGet();
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    endGate.countDown();
                }
            });
        }

        // Fire all threads simultaneously
        startGate.countDown();

        // Await completion with timeout
        boolean finished = endGate.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "All concurrent booking requests should complete within timeout");
        assertEquals(1, successCount.get(), "Exactly 1 booking must succeed for the contested slot");
        assertEquals(threadCount - 1, conflictCount.get(), "All other simultaneous bookings must be rejected as conflicts");
        assertTrue(exceptions.isEmpty(), "No unexpected exceptions should occur. Encountered: " + exceptions);

        // Verify database state: exactly 1 active appointment exists for this slot
        long totalAppointmentsInDb = appointmentRepository.count();
        assertEquals(1, totalAppointmentsInDb, "Database must contain exactly 1 appointment record");
    }

    @Test
    @DisplayName("Partial Index Semantics: Cancelled appointment frees the slot and allows immediate rebooking")
    void shouldAllowRebookingOfCancelledSlot() {
        LocalDate targetFriday = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        LocalTime slotTime = LocalTime.of(10, 0);

        // 1. Patient 1 books slot
        BookAppointmentRequestDto request = new BookAppointmentRequestDto(
                testProvider.getId(),
                testHospital.getId(),
                targetFriday,
                slotTime,
                "First booking"
        );
        AppointmentResponseDto firstBooking = appointmentService.bookAppointment(testPatients.get(0).getAuthUserId(), request);
        assertNotNull(firstBooking.getId());

        // 2. Patient 2 attempting same slot is rejected with 409 Conflict
        assertThrows(ResourceAlreadyExistsException.class, () ->
                appointmentService.bookAppointment(testPatients.get(1).getAuthUserId(), request));

        // 3. Patient 1 cancels appointment
        authenticateUser(testPatients.get(0).getAuthUserId());
        appointmentService.cancelAppointment(firstBooking.getId(), null);

        // 4. Patient 2 books the same slot - must succeed because partial index ignores CANCELLED status
        AppointmentResponseDto secondBooking = appointmentService.bookAppointment(testPatients.get(1).getAuthUserId(), request);
        assertNotNull(secondBooking.getId());
        assertEquals(testPatients.get(1).getId(), secondBooking.getPatientId());

        // 5. Verify database contains both appointments (1 CANCELLED, 1 SCHEDULED)
        assertEquals(2, appointmentRepository.count(), "Database should preserve both historical cancelled and new active appointments");
    }

    @Test
    @DisplayName("Atomic Rescheduling: Conflict on target slot rolls back transaction and preserves original appointment")
    void shouldRollbackRescheduleWhenTargetSlotIsOccupied() {
        LocalDate targetFriday = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        LocalTime slot1 = LocalTime.of(10, 30);
        LocalTime slot2 = LocalTime.of(11, 0);

        // Patient 1 books slot 1
        AppointmentResponseDto p1Appt = appointmentService.bookAppointment(
                testPatients.get(0).getAuthUserId(),
                new BookAppointmentRequestDto(testProvider.getId(), testHospital.getId(), targetFriday, slot1, "Patient 1 Slot 1"));

        // Patient 2 books slot 2
        appointmentService.bookAppointment(
                testPatients.get(1).getAuthUserId(),
                new BookAppointmentRequestDto(testProvider.getId(), testHospital.getId(), targetFriday, slot2, "Patient 2 Slot 2"));

        // Patient 1 tries to reschedule into Slot 2 (occupied by Patient 2)
        com.medisimbio.backend.appointment.dto.RescheduleAppointmentRequestDto rescheduleDto =
                new com.medisimbio.backend.appointment.dto.RescheduleAppointmentRequestDto(targetFriday, slot2);

        authenticateUser(testPatients.get(0).getAuthUserId());
        assertThrows(ResourceAlreadyExistsException.class, () ->
                appointmentService.rescheduleAppointment(p1Appt.getId(), rescheduleDto));

        // Verify Patient 1's original appointment in Slot 1 was NOT cancelled and remains SCHEDULED
        var originalAppt = appointmentRepository.findById(p1Appt.getId()).orElseThrow();
        assertEquals(com.medisimbio.backend.appointment.entity.AppointmentStatus.SCHEDULED, originalAppt.getStatus(),
                "Original appointment must remain SCHEDULED when rescheduling fails due to slot collision");
    }

    private void authenticateUser(String authUserId) {
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        new com.medisimbio.backend.security.UserPrincipal(
                                authUserId,
                                authUserId,
                                authUserId + "@medisimbio.local",
                                java.util.Set.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PATIENT")),
                                true
                        ),
                        null,
                        java.util.Set.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PATIENT"))
                )
        );
    }
}
