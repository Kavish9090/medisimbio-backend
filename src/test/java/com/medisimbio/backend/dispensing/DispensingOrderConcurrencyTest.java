package com.medisimbio.backend.dispensing;

import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.common.exception.ConflictException;
import com.medisimbio.backend.dispensing.dto.CreateDispensingOrderRequestDto;
import com.medisimbio.backend.dispensing.dto.DispensingOrderResponseDto;
import com.medisimbio.backend.dispensing.entity.DispensingOrderStatus;
import com.medisimbio.backend.dispensing.repository.DispensingOrderRepository;
import com.medisimbio.backend.dispensing.service.DispensingOrderService;
import com.medisimbio.backend.encounter.entity.EncounterEntity;
import com.medisimbio.backend.encounter.entity.EncounterStatus;
import com.medisimbio.backend.encounter.entity.EncounterType;
import com.medisimbio.backend.encounter.repository.EncounterRepository;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.entity.HospitalType;
import com.medisimbio.backend.hospital.repository.HospitalRepository;
import com.medisimbio.backend.patient.entity.Gender;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.patient.repository.PatientRepository;
import com.medisimbio.backend.prescription.entity.PrescriptionEntity;
import com.medisimbio.backend.prescription.entity.PrescriptionItemEntity;
import com.medisimbio.backend.prescription.entity.PrescriptionStatus;
import com.medisimbio.backend.prescription.repository.PrescriptionRepository;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.provider.repository.ProviderRepository;
import com.medisimbio.backend.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class DispensingOrderConcurrencyTest {

    @Autowired
    private DispensingOrderService dispensingOrderService;

    @Autowired
    private DispensingOrderRepository dispensingOrderRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PatientEntity testPatient;
    private ProviderEntity testProvider;
    private HospitalEntity testHospital;
    private AppointmentEntity testAppointment;
    private EncounterEntity testEncounter;
    private PrescriptionEntity finalizedPrescription;

    private static final String AUTH_PATIENT = "auth_pat_conc_dsp";
    private static final String AUTH_DOCTOR = "auth_doc_conc_dsp";

    @BeforeEach
    void setUp() {
        cleanDb();

        testPatient = patientRepository.save(new PatientEntity(
                UUID.randomUUID(), AUTH_PATIENT, "Vikrant Rao", "vikrant.conc@test.com",
                "+919876543000", LocalDate.of(1991, 3, 15), Gender.MALE
        ));

        testHospital = hospitalRepository.save(new HospitalEntity(
                UUID.randomUUID(), "Apollo Conc Hub", HospitalType.PRIVATE,
                "+911188776655", "7 Concurrency Road", "Bengaluru", "Karnataka", "560001"
        ));

        testProvider = providerRepository.save(new ProviderEntity(
                UUID.randomUUID(), AUTH_DOCTOR, "Dr. Sunita Rao", "MCI-CONC-01",
                "MBBS, MD", 12, "sunita.conc@test.com", "+919811887766"
        ));

        testAppointment = appointmentRepository.save(new AppointmentEntity(
                UUID.randomUUID(), testPatient, testProvider, testHospital, null,
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentStatus.CONFIRMED, "MSB-CONC-APP-01", "Consultation"
        ));

        testEncounter = encounterRepository.save(new EncounterEntity(
                UUID.randomUUID(), "ENC-CONC-001", testPatient, testProvider, testHospital, testAppointment,
                EncounterType.INITIAL_CONSULTATION, EncounterStatus.IN_PROGRESS, null,
                "Hypertension management", AUTH_DOCTOR
        ));

        PrescriptionEntity rx = new PrescriptionEntity(
                UUID.randomUUID(), "RX-CONC-001", testPatient, testEncounter, testProvider, testHospital,
                PrescriptionStatus.DRAFT, Instant.now(), LocalDate.now().plusMonths(1),
                "Daily medication", 1, null, AUTH_DOCTOR
        );
        PrescriptionItemEntity item = new PrescriptionItemEntity(
                UUID.randomUUID(), rx, "Amlodipine 5mg", "Amlodipine", "5 mg",
                "TABLET", "1 tablet", "ORAL", "ONCE_DAILY", 30, "DAYS", 30, "TABLETS", 0, "Morning", 1
        );
        rx.addItem(item);
        rx.setStatus(PrescriptionStatus.FINALIZED);
        rx.setFinalizedAt(Instant.now());
        finalizedPrescription = prescriptionRepository.save(rx);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        cleanDb();
    }

    private void cleanDb() {
        jdbcTemplate.execute("DELETE FROM dispensing_order_items");
        jdbcTemplate.execute("DELETE FROM dispensing_orders");
        jdbcTemplate.execute("DELETE FROM prescription_items");
        jdbcTemplate.execute("DELETE FROM prescriptions");
        jdbcTemplate.execute("DELETE FROM diagnoses");
        jdbcTemplate.execute("DELETE FROM vitals");
        jdbcTemplate.execute("DELETE FROM encounters");
        jdbcTemplate.execute("DELETE FROM appointments");
        jdbcTemplate.execute("DELETE FROM med_ids");
        jdbcTemplate.execute("DELETE FROM patients");
        jdbcTemplate.execute("DELETE FROM provider_hospitals");
        jdbcTemplate.execute("DELETE FROM provider_specialties");
        jdbcTemplate.execute("DELETE FROM providers");
        jdbcTemplate.execute("DELETE FROM hospital_staff");
        jdbcTemplate.execute("DELETE FROM hospitals");
    }

    private void authenticateUser(String authUserId) {
        UserPrincipal principal = new UserPrincipal(
                authUserId, authUserId, authUserId + "@test.com",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), true
        );
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("Concurrent Dispensing Orders: Exactly 1 order succeeds, rest receive 409 Conflict without 500 error")
    void concurrentDispensingOrderCreation_ExactlyOneSucceeds() throws InterruptedException {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<Throwable> unexpectedExceptions = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    authenticateUser(AUTH_PATIENT);
                    startGate.await();

                    CreateDispensingOrderRequestDto request = new CreateDispensingOrderRequestDto(
                            "Concurrent order attempt", null
                    );
                    DispensingOrderResponseDto response = dispensingOrderService.createDispensingOrder(
                            finalizedPrescription.getId(), request
                    );
                    if (response != null && response.id() != null) {
                        successCount.incrementAndGet();
                    }
                } catch (ConflictException ex) {
                    conflictCount.incrementAndGet();
                } catch (Throwable ex) {
                    unexpectedExceptions.add(ex);
                } finally {
                    SecurityContextHolder.clearContext();
                    endGate.countDown();
                }
            });
        }

        // Release all threads simultaneously
        startGate.countDown();
        boolean finished = endGate.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "All concurrent requests should finish within timeout");
        assertEquals(1, successCount.get(), "Exactly 1 dispensing order must succeed for the prescription");
        assertEquals(threadCount - 1, conflictCount.get(), "All other concurrent attempts must receive 409 Conflict");
        assertTrue(unexpectedExceptions.isEmpty(), "No unhandled 500 errors or unexpected exceptions allowed: " + unexpectedExceptions);

        // Verify database state: exactly 1 dispensing order exists
        assertEquals(1, dispensingOrderRepository.count(), "Database must contain exactly 1 active dispensing order");
    }

    @Test
    @DisplayName("Partial Index Semantics: Cancelled order frees prescription to accept a new dispensing order")
    void cancelledOrderAllowsSubsequentDispensingOrder() {
        authenticateUser(AUTH_PATIENT);

        // 1. Create initial dispensing order
        CreateDispensingOrderRequestDto request = new CreateDispensingOrderRequestDto("First order", null);
        DispensingOrderResponseDto firstOrder = dispensingOrderService.createDispensingOrder(
                finalizedPrescription.getId(), request
        );
        assertNotNull(firstOrder.id());
        assertEquals(DispensingOrderStatus.PENDING, firstOrder.status());

        // 2. Second attempt while first is PENDING fails with 409 Conflict
        assertThrows(ConflictException.class, () ->
                dispensingOrderService.createDispensingOrder(finalizedPrescription.getId(), request));

        // 3. Cancel first order
        DispensingOrderResponseDto cancelled = dispensingOrderService.cancelDispensingOrder(
                firstOrder.id(), "Patient opted to collect in person"
        );
        assertEquals(DispensingOrderStatus.CANCELLED, cancelled.status());

        // 4. Now placing a new dispensing order must succeed because partial unique index excludes CANCELLED
        DispensingOrderResponseDto secondOrder = dispensingOrderService.createDispensingOrder(
                finalizedPrescription.getId(), new CreateDispensingOrderRequestDto("Second order rebooking", null)
        );
        assertNotNull(secondOrder.id());
        assertEquals(DispensingOrderStatus.PENDING, secondOrder.status());

        // 5. Database contains both records (1 CANCELLED, 1 PENDING)
        assertEquals(2, dispensingOrderRepository.count(), "Database must preserve both cancelled and new active dispensing order");
    }
}
