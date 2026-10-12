package com.medisimbio.backend.encounter;

import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.common.exception.BadRequestException;
import com.medisimbio.backend.common.exception.ResourceAlreadyExistsException;
import com.medisimbio.backend.encounter.dto.CreateEncounterRequestDto;
import com.medisimbio.backend.encounter.dto.EncounterResponseDto;
import com.medisimbio.backend.encounter.entity.EncounterStatus;
import com.medisimbio.backend.encounter.entity.EncounterType;
import com.medisimbio.backend.encounter.repository.EncounterRepository;
import com.medisimbio.backend.encounter.service.EncounterService;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.entity.HospitalType;
import com.medisimbio.backend.hospital.repository.HospitalRepository;
import com.medisimbio.backend.patient.entity.Gender;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.patient.repository.PatientRepository;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.provider.repository.ProviderRepository;
import com.medisimbio.backend.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EncounterLifecycleAndConcurrencyTest {

    @Autowired
    private EncounterService encounterService;

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
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private PatientEntity testPatient;
    private ProviderEntity testProvider;
    private HospitalEntity testHospital;
    private AppointmentEntity testAppointment;

    private static final String AUTH_DOCTOR = "auth_doctor_lifecycle";

    @BeforeEach
    void setUp() {
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

        testPatient = new PatientEntity(
                UUID.randomUUID(),
                "auth_user_lifecycle_patient",
                "Alok Nath",
                "alok.life@test.com",
                "+919876543204",
                LocalDate.of(1975, 4, 1),
                Gender.MALE
        );
        testPatient = patientRepository.save(testPatient);

        testHospital = new HospitalEntity(
                UUID.randomUUID(),
                "Care Max Hospital",
                HospitalType.PRIVATE,
                "+911133221100",
                "99 Ring Road",
                "Pune",
                "Maharashtra",
                "411001"
        );
        testHospital = hospitalRepository.save(testHospital);

        testProvider = new ProviderEntity(
                UUID.randomUUID(),
                AUTH_DOCTOR,
                "Dr. Neha Kapoor",
                "MCI-LIFE-01",
                "MBBS, DNB",
                10,
                "neha.life@test.com",
                "+919877889900"
        );
        testProvider = providerRepository.save(testProvider);

        testAppointment = new AppointmentEntity(
                UUID.randomUUID(),
                testPatient,
                testProvider,
                testHospital,
                null,
                LocalDate.now().plusDays(1),
                LocalTime.of(14, 0),
                LocalTime.of(14, 30),
                AppointmentStatus.CONFIRMED,
                "MSB-LIFE-APP-01",
                "Knee pain"
        );
        testAppointment = appointmentRepository.save(testAppointment);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
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
    @DisplayName("Lifecycle: OPEN -> IN_PROGRESS -> COMPLETED transitions properly and syncs appointment")
    void fullEncounterLifecycleFlow() {
        authenticateUser(AUTH_DOCTOR);

        // 1. Create Encounter
        CreateEncounterRequestDto createRequest = new CreateEncounterRequestDto(
                testAppointment.getId(),
                EncounterType.INITIAL_CONSULTATION,
                "Knee pain after running",
                null
        );
        EncounterResponseDto created = encounterService.createEncounter(createRequest);
        assertEquals(EncounterStatus.OPEN, created.getStatus());

        // 2. Start Encounter
        EncounterResponseDto started = encounterService.startEncounter(created.getId());
        assertEquals(EncounterStatus.IN_PROGRESS, started.getStatus());
        assertNotNull(started.getStartedAt());

        // 3. Complete Encounter
        EncounterResponseDto completed = encounterService.completeEncounter(started.getId());
        assertEquals(EncounterStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getEndedAt());

        // 4. Verify linked appointment is COMPLETED
        AppointmentEntity linkedAppt = appointmentRepository.findById(testAppointment.getId()).orElseThrow();
        assertEquals(AppointmentStatus.COMPLETED, linkedAppt.getStatus());

        // 5. Verify invalid transition: Cannot restart a completed encounter
        assertThrows(BadRequestException.class, () -> encounterService.startEncounter(completed.getId()));
    }

    @Test
    @DisplayName("Duplicate Encounter Creation: Throws ResourceAlreadyExistsException")
    void duplicateEncounterCreation_ThrowsConflict() {
        authenticateUser(AUTH_DOCTOR);

        CreateEncounterRequestDto createRequest = new CreateEncounterRequestDto(
                testAppointment.getId(),
                EncounterType.INITIAL_CONSULTATION,
                "Knee pain",
                null
        );
        encounterService.createEncounter(createRequest);

        // Attempt second encounter for same appointment
        assertThrows(ResourceAlreadyExistsException.class, () -> encounterService.createEncounter(createRequest));
    }
}
