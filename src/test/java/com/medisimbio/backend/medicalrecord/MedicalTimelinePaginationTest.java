package com.medisimbio.backend.medicalrecord;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.diagnosis.entity.DiagnosisEntity;
import com.medisimbio.backend.diagnosis.entity.DiagnosisStatus;
import com.medisimbio.backend.diagnosis.entity.DiagnosisType;
import com.medisimbio.backend.diagnosis.repository.DiagnosisRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies SQL UNION ALL medical timeline pagination, ordering stability, and patient data isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MedicalTimelinePaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    private VitalsRepository vitalsRepository;

    @Autowired
    private com.medisimbio.backend.prescription.repository.PrescriptionRepository prescriptionRepository;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    private PatientEntity patientA;
    private PatientEntity patientB;
    private ProviderEntity provider;
    private HospitalEntity hospital;

    private static final String AUTH_PATIENT_A = "auth_timeline_patA";
    private static final String AUTH_PATIENT_B = "auth_timeline_patB";

    @BeforeEach
    void setUp() {
        cleanDb();

        patientA = patientRepository.save(new PatientEntity(
                UUID.randomUUID(), AUTH_PATIENT_A, "Patient Alpha",
                "alpha.timeline@test.com", "+919876543001", LocalDate.of(1990, 1, 1), Gender.MALE
        ));

        patientB = patientRepository.save(new PatientEntity(
                UUID.randomUUID(), AUTH_PATIENT_B, "Patient Beta",
                "beta.timeline@test.com", "+919876543002", LocalDate.of(1992, 2, 2), Gender.FEMALE
        ));

        hospital = hospitalRepository.save(new HospitalEntity(
                UUID.randomUUID(), "Central Hospital", HospitalType.PRIVATE,
                "+911188009900", "Central Road", "Delhi", "Delhi", "110001"
        ));

        provider = providerRepository.save(new ProviderEntity(
                UUID.randomUUID(), "auth_doc_timeline", "Dr. Timeline Specialist",
                "MCI-TL-01", "MBBS, MD", 10, "timeline.doc@test.com", "+919811009988"
        ));
    }

    @Test
    @DisplayName("Empty timeline returns 200 OK with totalElements 0 and empty events list")
    void emptyTimeline_ReturnsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/v1/patients/me/medical-record/timeline?page=0&size=10")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.totalElements", is(0)))
                .andExpect(jsonPath("$.data.totalPages", is(0)))
                .andExpect(jsonPath("$.data.events", hasSize(0)))
                .andExpect(jsonPath("$.data.hasNext", is(false)))
                .andExpect(jsonPath("$.data.hasPrevious", is(false)));
    }

    @Test
    @DisplayName("Pagination slices large clinical history properly across multiple pages")
    void paginatedTimeline_SlicesCorrectly() throws Exception {
        Instant baseTime = Instant.now().minus(30, ChronoUnit.DAYS);

        // Seed 15 events for Patient A: 5 encounters, 5 diagnoses, 5 vitals
        for (int i = 0; i < 5; i++) {
            Instant encounterTime = baseTime.plus(i * 5, ChronoUnit.DAYS);

            AppointmentEntity appt = appointmentRepository.save(new AppointmentEntity(
                    UUID.randomUUID(), patientA, provider, hospital, null,
                    LocalDate.now().plusDays(i + 1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                    AppointmentStatus.CONFIRMED, "MSB-TL-" + i, "Visit " + i
            ));

            EncounterEntity enc = new EncounterEntity(
                    UUID.randomUUID(), "ENC-TL-" + i, patientA, provider, hospital, appt,
                    EncounterType.INITIAL_CONSULTATION, EncounterStatus.COMPLETED, null,
                    "Complaint " + i, "auth_doc_timeline"
            );
            enc.setStartedAt(encounterTime);
            enc = encounterRepository.save(enc);

            DiagnosisEntity diag = new DiagnosisEntity(
                    UUID.randomUUID(), enc, patientA, "J00", "Common Cold " + i,
                    "Diagnosis " + i, DiagnosisType.PRIMARY, DiagnosisStatus.ACTIVE,
                    null, "Notes " + i, "auth_doc_timeline"
            );
            diag.setDiagnosedAt(encounterTime.plus(1, ChronoUnit.HOURS));
            diagnosisRepository.save(diag);

            VitalsEntity vit = new VitalsEntity(
                    UUID.randomUUID(), enc, patientA, encounterTime.plus(30, ChronoUnit.MINUTES),
                    new BigDecimal("98.6"), 72, 16, 120, 80,
                    new BigDecimal("98.5"), new BigDecimal("70.0"), new BigDecimal("175.0"),
                    new BigDecimal("22.9"), new BigDecimal("90.0"), "Vitals " + i, "auth_doc_timeline"
            );
            vitalsRepository.save(vit);
        }

        // Page 0, Size 6 -> expects 6 events, total 15, totalPages 3, hasNext true, hasPrevious false
        mockMvc.perform(get("/api/v1/patients/me/medical-record/timeline?page=0&size=6")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(15)))
                .andExpect(jsonPath("$.data.totalPages", is(3)))
                .andExpect(jsonPath("$.data.events", hasSize(6)))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(6)))
                .andExpect(jsonPath("$.data.hasNext", is(true)))
                .andExpect(jsonPath("$.data.hasPrevious", is(false)));

        // Page 1, Size 6 -> expects 6 events, hasNext true, hasPrevious true
        mockMvc.perform(get("/api/v1/patients/me/medical-record/timeline?page=1&size=6")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events", hasSize(6)))
                .andExpect(jsonPath("$.data.page", is(1)))
                .andExpect(jsonPath("$.data.hasNext", is(true)))
                .andExpect(jsonPath("$.data.hasPrevious", is(true)));

        // Page 2, Size 6 -> expects remaining 3 events, hasNext false, hasPrevious true
        mockMvc.perform(get("/api/v1/patients/me/medical-record/timeline?page=2&size=6")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.events", hasSize(3)))
                .andExpect(jsonPath("$.data.page", is(2)))
                .andExpect(jsonPath("$.data.hasNext", is(false)))
                .andExpect(jsonPath("$.data.hasPrevious", is(true)));
    }

    @Test
    @DisplayName("Cross-patient isolation: Patient A never sees Patient B events in timeline")
    void patientTimeline_IsStrictlyIsolated() throws Exception {
        // Create 1 encounter for Patient B
        AppointmentEntity apptB = appointmentRepository.save(new AppointmentEntity(
                UUID.randomUUID(), patientB, provider, hospital, null,
                LocalDate.now().plusDays(1), LocalTime.of(15, 0), LocalTime.of(15, 30),
                AppointmentStatus.CONFIRMED, "MSB-PATB-01", "Beta Visit"
        ));

        EncounterEntity encB = encounterRepository.save(new EncounterEntity(
                UUID.randomUUID(), "ENC-PATB-001", patientB, provider, hospital, apptB,
                EncounterType.INITIAL_CONSULTATION, EncounterStatus.COMPLETED, null,
                "Patient B secret complaint", "auth_doc_timeline"
        ));

        diagnosisRepository.save(new DiagnosisEntity(
                UUID.randomUUID(), encB, patientB, "B20", "Patient B Sensitive Diagnosis",
                "Confidential", DiagnosisType.PRIMARY, DiagnosisStatus.ACTIVE,
                null, "Confidential notes", "auth_doc_timeline"
        ));

        // When Patient A queries their timeline, it must return 0 elements
        mockMvc.perform(get("/api/v1/patients/me/medical-record/timeline?page=0&size=10")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(0)))
                .andExpect(jsonPath("$.data.events", hasSize(0)));

        // When Patient B queries, it returns their 2 elements
        mockMvc.perform(get("/api/v1/patients/me/medical-record/timeline?page=0&size=10")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_B))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(2)))
                .andExpect(jsonPath("$.data.events", hasSize(2)));
    }

    @Test
    @DisplayName("Timeline includes FINALIZED prescriptions and excludes DRAFT prescriptions")
    void timelineIncludesFinalizedPrescriptions_ExcludesDraft() throws Exception {
        AppointmentEntity appt = appointmentRepository.save(new AppointmentEntity(
                UUID.randomUUID(), patientA, provider, hospital, null,
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentStatus.CONFIRMED, "MSB-PATA-RX", "Visit"
        ));

        EncounterEntity enc = encounterRepository.save(new EncounterEntity(
                UUID.randomUUID(), "ENC-PATA-RX", patientA, provider, hospital, appt,
                EncounterType.INITIAL_CONSULTATION, EncounterStatus.COMPLETED, null,
                "Clinical checkup", "auth_doc_timeline"
        ));

        // 1. Create a DRAFT prescription -> should NOT increase timeline count
        com.medisimbio.backend.prescription.entity.PrescriptionEntity draftRx =
                new com.medisimbio.backend.prescription.entity.PrescriptionEntity(
                        UUID.randomUUID(), "RX-TEST-DRAFT", patientA, enc, provider, hospital,
                        com.medisimbio.backend.prescription.entity.PrescriptionStatus.DRAFT,
                        java.time.Instant.now(), null, "Draft instructions", 1, null, "auth_doc_timeline"
                );
        prescriptionRepository.save(draftRx);

        // Patient A timeline should only show the 1 encounter, NOT the draft prescription
        mockMvc.perform(get("/api/v1/patients/me/medical-record/timeline?page=0&size=10")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(1)))
                .andExpect(jsonPath("$.data.events", hasSize(1)))
                .andExpect(jsonPath("$.data.events[0].eventType", is("ENCOUNTER")));

        // 2. Create a FINALIZED prescription -> SHOULD appear on timeline
        com.medisimbio.backend.prescription.entity.PrescriptionEntity activeRx =
                new com.medisimbio.backend.prescription.entity.PrescriptionEntity(
                        UUID.randomUUID(), "RX-TEST-ACTIVE", patientA, enc, provider, hospital,
                        com.medisimbio.backend.prescription.entity.PrescriptionStatus.FINALIZED,
                        java.time.Instant.now(), null, "Active instructions", 1, null, "auth_doc_timeline"
                );
        activeRx.setFinalizedAt(java.time.Instant.now());
        prescriptionRepository.save(activeRx);

        // Now Patient A timeline must show 2 elements: ENCOUNTER and PRESCRIPTION
        mockMvc.perform(get("/api/v1/patients/me/medical-record/timeline?page=0&size=10")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_A))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(2)))
                .andExpect(jsonPath("$.data.events", hasSize(2)))
                .andExpect(jsonPath("$.data.events[0].eventType", is("PRESCRIPTION")))
                .andExpect(jsonPath("$.data.events[0].title", startsWith("Prescription: RX-")))
                .andExpect(jsonPath("$.data.events[1].eventType", is("ENCOUNTER")));
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
}
