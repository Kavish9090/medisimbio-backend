package com.medisimbio.backend.diagnosis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.diagnosis.dto.CreateDiagnosisRequestDto;
import com.medisimbio.backend.diagnosis.dto.UpdateDiagnosisRequestDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DiagnosisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

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
    private EncounterEntity testEncounter;

    private static final String AUTH_DOCTOR = "auth_doctor_diag";

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
                "auth_patient_diag",
                "Pooja Nair",
                "pooja.diag@test.com",
                "+919876543201",
                LocalDate.of(1992, 5, 15),
                Gender.FEMALE
        );
        testPatient = patientRepository.save(testPatient);

        testHospital = new HospitalEntity(
                UUID.randomUUID(),
                "Metro Care Hospital",
                HospitalType.PRIVATE,
                "+911188990011",
                "55 Medical Enclave",
                "Mumbai",
                "Maharashtra",
                "400001"
        );
        testHospital = hospitalRepository.save(testHospital);

        testProvider = new ProviderEntity(
                UUID.randomUUID(),
                AUTH_DOCTOR,
                "Dr. Vikram Sen",
                "MCI-DIAG-01",
                "MBBS, MD (Pulmonology)",
                15,
                "vikram.diag@test.com",
                "+919833445566"
        );
        testProvider = providerRepository.save(testProvider);

        testAppointment = new AppointmentEntity(
                UUID.randomUUID(),
                testPatient,
                testProvider,
                testHospital,
                null,
                LocalDate.now().plusDays(1),
                LocalTime.of(11, 0),
                LocalTime.of(11, 30),
                AppointmentStatus.CONFIRMED,
                "MSB-DIAG-APP-01",
                "Persistent productive cough"
        );
        testAppointment = appointmentRepository.save(testAppointment);

        testEncounter = new EncounterEntity(
                UUID.randomUUID(),
                "ENC-20261008-DIAG1",
                testPatient,
                testProvider,
                testHospital,
                testAppointment,
                EncounterType.INITIAL_CONSULTATION,
                EncounterStatus.IN_PROGRESS,
                null,
                "Productive cough and wheezing",
                "doctor"
        );
        testEncounter = encounterRepository.save(testEncounter);
    }

    @Test
    @DisplayName("POST /api/v1/encounters/{id}/diagnoses - Doctor records diagnosis successfully")
    void createDiagnosis_Success() throws Exception {
        CreateDiagnosisRequestDto request = new CreateDiagnosisRequestDto(
                "Acute Bronchitis",
                "J20.9",
                "Acute inflammation of bronchial mucosa",
                DiagnosisType.PRIMARY,
                DiagnosisStatus.ACTIVE,
                null,
                "Wheezing present, productive cough."
        );

        mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/diagnoses")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.diagnosisName", is("Acute Bronchitis")))
                .andExpect(jsonPath("$.data.diagnosisCode", is("J20.9")))
                .andExpect(jsonPath("$.data.diagnosisType", is("PRIMARY")))
                .andExpect(jsonPath("$.data.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("POST /api/v1/encounters/{id}/diagnoses - Unauthorized caller receives 403 Forbidden")
    void createDiagnosis_UnauthorizedCaller() throws Exception {
        CreateDiagnosisRequestDto request = new CreateDiagnosisRequestDto(
                "Acute Bronchitis",
                "J20.9",
                "Desc",
                DiagnosisType.PRIMARY,
                DiagnosisStatus.ACTIVE,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/diagnoses")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, "unauthorized_caller_777")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/encounters/{id}/diagnoses - Patient retrieves recorded diagnoses")
    void getDiagnosesByEncounter_Success() throws Exception {
        DiagnosisEntity diagnosis = new DiagnosisEntity(
                UUID.randomUUID(),
                testEncounter,
                testPatient,
                "J20.9",
                "Acute Bronchitis",
                "Bronchial inflammation",
                DiagnosisType.PRIMARY,
                DiagnosisStatus.ACTIVE,
                null,
                "Notes",
                "doctor"
        );
        diagnosisRepository.save(diagnosis);

        mockMvc.perform(get("/api/v1/encounters/" + testEncounter.getId() + "/diagnoses")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, testPatient.getAuthUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].diagnosisName", is("Acute Bronchitis")));
    }

    @Test
    @DisplayName("PATCH /api/v1/diagnoses/{id} - Doctor updates diagnosis status to RESOLVED")
    void updateDiagnosis_Success() throws Exception {
        DiagnosisEntity diagnosis = new DiagnosisEntity(
                UUID.randomUUID(),
                testEncounter,
                testPatient,
                "J20.9",
                "Acute Bronchitis",
                "Bronchial inflammation",
                DiagnosisType.PRIMARY,
                DiagnosisStatus.ACTIVE,
                null,
                "Initial notes",
                "doctor"
        );
        diagnosis = diagnosisRepository.save(diagnosis);

        UpdateDiagnosisRequestDto updateDto = new UpdateDiagnosisRequestDto(
                "Acute Bronchitis (Resolved)",
                "J20.9",
                "Fully resolved with treatment",
                DiagnosisType.PRIMARY,
                DiagnosisStatus.RESOLVED,
                "Symptoms subsided completely."
        );

        mockMvc.perform(patch("/api/v1/diagnoses/" + diagnosis.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("RESOLVED")))
                .andExpect(jsonPath("$.data.diagnosisName", is("Acute Bronchitis (Resolved)")));
    }
}
