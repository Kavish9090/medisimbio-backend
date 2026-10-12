package com.medisimbio.backend.hospital;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.encounter.entity.EncounterEntity;
import com.medisimbio.backend.encounter.entity.EncounterStatus;
import com.medisimbio.backend.encounter.entity.EncounterType;
import com.medisimbio.backend.encounter.repository.EncounterRepository;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.entity.HospitalStaffEntity;
import com.medisimbio.backend.hospital.entity.HospitalType;
import com.medisimbio.backend.hospital.repository.HospitalRepository;
import com.medisimbio.backend.hospital.repository.HospitalStaffRepository;
import com.medisimbio.backend.patient.entity.Gender;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.patient.repository.PatientRepository;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.provider.repository.ProviderRepository;
import com.medisimbio.backend.security.DevAuthenticationFilter;
import com.medisimbio.backend.vitals.entity.VitalsEntity;
import com.medisimbio.backend.vitals.repository.VitalsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies explicit hospital staff membership and cross-facility IDOR isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class HospitalStaffAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private HospitalStaffRepository hospitalStaffRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    private VitalsRepository vitalsRepository;

    private HospitalEntity hospitalA;
    private HospitalEntity hospitalB;
    private ProviderEntity provider;
    private PatientEntity patient;
    private EncounterEntity encounterA;

    private static final String AUTH_STAFF_A_ACTIVE = "auth_staff_hospA_active";
    private static final String AUTH_STAFF_A_INACTIVE = "auth_staff_hospA_revoked";
    private static final String AUTH_STAFF_B = "auth_staff_hospB";
    private static final String AUTH_UNMAPPED = "auth_unmapped_random_user";

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

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

        hospitalA = hospitalRepository.save(new HospitalEntity(
                UUID.randomUUID(), "Apollo Hospital", HospitalType.PRIVATE,
                "+911144001100", "Apollo Road", "Delhi", "Delhi", "110076"
        ));

        hospitalB = hospitalRepository.save(new HospitalEntity(
                UUID.randomUUID(), "Max Healthcare", HospitalType.PRIVATE,
                "+911144002200", "Max Road", "Delhi", "Delhi", "110092"
        ));

        // Seed Active Staff at Hospital A
        hospitalStaffRepository.save(new HospitalStaffEntity(
                UUID.randomUUID(), hospitalA, AUTH_STAFF_A_ACTIVE, "Staff Member A", "ADMIN", true
        ));

        // Seed Inactive/Revoked Staff at Hospital A
        hospitalStaffRepository.save(new HospitalStaffEntity(
                UUID.randomUUID(), hospitalA, AUTH_STAFF_A_INACTIVE, "Former Staff Member", "STAFF", false
        ));

        // Seed Active Staff at Hospital B
        hospitalStaffRepository.save(new HospitalStaffEntity(
                UUID.randomUUID(), hospitalB, AUTH_STAFF_B, "Staff Member B", "ADMIN", true
        ));

        provider = providerRepository.save(new ProviderEntity(
                UUID.randomUUID(), "auth_doc_staff_test", "Dr. Rajesh K",
                "MCI-STAFF-01", "MBBS", 8, "rajesh.k@test.com", "+919811004455"
        ));

        patient = patientRepository.save(new PatientEntity(
                UUID.randomUUID(), "auth_pat_staff_test", "Rohit Verma",
                "rohit.staff@test.com", "+919876543111", LocalDate.of(1995, 3, 10), Gender.MALE
        ));

        AppointmentEntity appointmentA = appointmentRepository.save(new AppointmentEntity(
                UUID.randomUUID(), patient, provider, hospitalA, null,
                LocalDate.now().plusDays(1), LocalTime.of(11, 0), LocalTime.of(11, 30),
                AppointmentStatus.CONFIRMED, "MSB-STAFF-01", "General Checkup"
        ));

        encounterA = encounterRepository.save(new EncounterEntity(
                UUID.randomUUID(), "ENC-STAFF-001", patient, provider, hospitalA, appointmentA,
                EncounterType.INITIAL_CONSULTATION, EncounterStatus.IN_PROGRESS, null,
                "Routine checkup", "auth_doc_staff_test"
        ));

        VitalsEntity vitals = new VitalsEntity(
                UUID.randomUUID(), encounterA, patient, Instant.now(),
                new BigDecimal("98.4"), 70, 16, 115, 75,
                new BigDecimal("99.0"), new BigDecimal("68.0"), new BigDecimal("172.0"),
                new BigDecimal("23.0"), new BigDecimal("90.0"), "Normal", AUTH_STAFF_A_ACTIVE
        );
        vitalsRepository.save(vitals);
    }

    @Test
    @DisplayName("Active hospital staff member is authorized to view encounter and vitals for their facility")
    void activeHospitalStaff_CanAccessFacilityData() throws Exception {
        // 1. Access encounter details
        mockMvc.perform(get("/api/v1/encounters/" + encounterA.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_STAFF_A_ACTIVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(encounterA.getId().toString())));

        // 2. Access vitals
        mockMvc.perform(get("/api/v1/encounters/" + encounterA.getId() + "/vitals")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_STAFF_A_ACTIVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)));

        // 3. Access hospital appointments
        mockMvc.perform(get("/api/v1/hospitals/{hospitalId}/appointments", hospitalA.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_STAFF_A_ACTIVE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("Inactive (revoked) hospital staff member is rejected with 403 Forbidden")
    void inactiveHospitalStaff_IsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/encounters/" + encounterA.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_STAFF_A_INACTIVE))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/hospitals/{hospitalId}/appointments", hospitalA.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_STAFF_A_INACTIVE))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Staff of Hospital B is forbidden from accessing Hospital A clinical encounters and appointments")
    void crossHospitalStaff_IsForbidden() throws Exception {
        // Staff B attempting to view Encounter A
        mockMvc.perform(get("/api/v1/encounters/" + encounterA.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_STAFF_B))
                .andExpect(status().isForbidden());

        // Staff B attempting to view Hospital A appointments
        mockMvc.perform(get("/api/v1/hospitals/{hospitalId}/appointments", hospitalA.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_STAFF_B))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unmapped identity with valid authentication token is rejected with 403 Forbidden")
    void unmappedIdentity_IsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/encounters/" + encounterA.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_UNMAPPED))
                .andExpect(status().isForbidden());
    }
}
