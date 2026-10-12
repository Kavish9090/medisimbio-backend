package com.medisimbio.backend.medicalrecord.controller;

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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MedicalRecordControllerTest {

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

    private PatientEntity testPatient;
    private ProviderEntity testProvider;
    private HospitalEntity testHospital;
    private AppointmentEntity testAppointment;
    private EncounterEntity testEncounter;

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

        testPatient = new PatientEntity(
                UUID.randomUUID(),
                "auth_patient_mr",
                "Suresh Raina",
                "suresh.mr@test.com",
                "+919876543203",
                LocalDate.of(1985, 7, 12),
                Gender.MALE
        );
        testPatient = patientRepository.save(testPatient);

        testHospital = new HospitalEntity(
                UUID.randomUUID(),
                "National Health Institute",
                HospitalType.GOVERNMENT,
                "+911122334455",
                "1 Medical Way",
                "New Delhi",
                "Delhi",
                "110029"
        );
        testHospital = hospitalRepository.save(testHospital);

        testProvider = new ProviderEntity(
                UUID.randomUUID(),
                "Dr. Rajesh Khanna",
                "MCI-MR-01",
                "MBBS, MS",
                20,
                "rajesh.mr@test.com",
                "+919811002233"
        );
        testProvider = providerRepository.save(testProvider);

        testAppointment = new AppointmentEntity(
                UUID.randomUUID(),
                testPatient,
                testProvider,
                testHospital,
                null,
                LocalDate.now().minusDays(5),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                AppointmentStatus.COMPLETED,
                "MSB-MR-APP-01",
                "Chest pain and fatigue"
        );
        testAppointment = appointmentRepository.save(testAppointment);

        testEncounter = new EncounterEntity(
                UUID.randomUUID(),
                "ENC-20261008-MR1",
                testPatient,
                testProvider,
                testHospital,
                testAppointment,
                EncounterType.INITIAL_CONSULTATION,
                EncounterStatus.COMPLETED,
                Instant.now().minusSeconds(86400 * 5),
                "Chest pain and fatigue",
                "doctor"
        );
        testEncounter.setDiagnosisSummary("Hypertension Stage 1");
        testEncounter = encounterRepository.save(testEncounter);

        DiagnosisEntity diagnosis = new DiagnosisEntity(
                UUID.randomUUID(),
                testEncounter,
                testPatient,
                "I10",
                "Essential (Primary) Hypertension",
                "Elevated BP readings on consecutive visits",
                DiagnosisType.PRIMARY,
                DiagnosisStatus.ACTIVE,
                Instant.now().minusSeconds(86400 * 5),
                "Prescribed dietary sodium reduction and lifestyle changes.",
                "doctor"
        );
        diagnosisRepository.save(diagnosis);

        VitalsEntity vitals = new VitalsEntity(
                UUID.randomUUID(),
                testEncounter,
                testPatient,
                Instant.now().minusSeconds(86400 * 5),
                new BigDecimal("98.4"),
                80,
                18,
                142,
                90,
                new BigDecimal("98.0"),
                new BigDecimal("82.00"),
                new BigDecimal("178.00"),
                new BigDecimal("25.9"),
                new BigDecimal("105.0"),
                "Resting BP elevated",
                "doctor"
        );
        vitalsRepository.save(vitals);
    }

    @Test
    @DisplayName("GET /api/v1/patients/me/medical-record - Patient gets consolidated medical record overview")
    void getPatientMedicalRecordOverview_Success() throws Exception {
        mockMvc.perform(get("/api/v1/patients/me/medical-record")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, testPatient.getAuthUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.patient.id", is(testPatient.getId().toString())))
                .andExpect(jsonPath("$.data.patient.fullName", is("Suresh Raina")))
                .andExpect(jsonPath("$.data.encounters", hasSize(1)))
                .andExpect(jsonPath("$.data.activeDiagnoses", hasSize(1)))
                .andExpect(jsonPath("$.data.activeDiagnoses[0].diagnosisName", is("Essential (Primary) Hypertension")))
                .andExpect(jsonPath("$.data.latestVitals.bloodPressureFormatted", is("142/90 mmHg")))
                .andExpect(jsonPath("$.data.totalEncountersCount", is(1)))
                .andExpect(jsonPath("$.data.totalDiagnosesCount", is(1)));
    }

    @Test
    @DisplayName("GET /api/v1/patients/me/medical-record/timeline - Chronological events returned and paginated")
    void getPatientMedicalTimeline_Success() throws Exception {
        mockMvc.perform(get("/api/v1/patients/me/medical-record/timeline?page=0&size=10")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, testPatient.getAuthUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.patientId", is(testPatient.getId().toString())))
                .andExpect(jsonPath("$.data.totalElements", is(3))) // 1 encounter + 1 diagnosis + 1 vitals
                .andExpect(jsonPath("$.data.events", hasSize(3)))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.size", is(10)));
    }

    @Test
    @DisplayName("GET /api/v1/patients/me/medical-record - Unauthenticated request receives 401")
    void getMedicalRecord_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/v1/patients/me/medical-record"))
                .andExpect(status().isUnauthorized());
    }
}
