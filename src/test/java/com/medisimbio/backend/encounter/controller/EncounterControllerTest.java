package com.medisimbio.backend.encounter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.encounter.dto.CreateEncounterRequestDto;
import com.medisimbio.backend.encounter.dto.UpdateEncounterRequestDto;
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
class EncounterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    private static final String AUTH_DOCTOR = "auth_doctor_100";

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
                "auth_patient_100",
                "Rahul Sharma",
                "rahul.encounter@test.com",
                "+919876543200",
                LocalDate.of(1990, 1, 1),
                Gender.MALE
        );
        testPatient = patientRepository.save(testPatient);

        testHospital = new HospitalEntity(
                UUID.randomUUID(),
                "City Care Hospital",
                HospitalType.PRIVATE,
                "+911144556677",
                "123 Healthcare Ave",
                "Delhi",
                "Delhi",
                "110001"
        );
        testHospital = hospitalRepository.save(testHospital);

        testProvider = new ProviderEntity(
                UUID.randomUUID(),
                AUTH_DOCTOR,
                "Dr. Amit Verma",
                "MCI-ENC-100",
                "MBBS, MD (General Medicine)",
                12,
                "amit.enc@test.com",
                "+919811223344"
        );
        testProvider = providerRepository.save(testProvider);

        testAppointment = new AppointmentEntity(
                UUID.randomUUID(),
                testPatient,
                testProvider,
                testHospital,
                null,
                LocalDate.now().plusDays(2),
                LocalTime.of(10, 0),
                LocalTime.of(10, 30),
                AppointmentStatus.CONFIRMED,
                "MSB-ENC-APP-100",
                "Persistent high fever"
        );
        testAppointment = appointmentRepository.save(testAppointment);
    }

    @Test
    @DisplayName("POST /api/v1/encounters - Success creates encounter from appointment")
    void createEncounter_Success() throws Exception {
        CreateEncounterRequestDto request = new CreateEncounterRequestDto(
                testAppointment.getId(),
                EncounterType.INITIAL_CONSULTATION,
                "Fever and severe cough for 3 days",
                null
        );

        mockMvc.perform(post("/api/v1/encounters")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.encounterReference", startsWith("ENC-")))
                .andExpect(jsonPath("$.data.status", is("OPEN")))
                .andExpect(jsonPath("$.data.patientId", is(testPatient.getId().toString())))
                .andExpect(jsonPath("$.data.providerId", is(testProvider.getId().toString())))
                .andExpect(jsonPath("$.data.appointmentId", is(testAppointment.getId().toString())))
                .andExpect(jsonPath("$.data.chiefComplaint", is("Fever and severe cough for 3 days")));
    }

    @Test
    @DisplayName("POST /api/v1/encounters - Returns 401 when unauthenticated")
    void createEncounter_Unauthenticated() throws Exception {
        CreateEncounterRequestDto request = new CreateEncounterRequestDto(
                testAppointment.getId(),
                EncounterType.INITIAL_CONSULTATION,
                "Fever",
                null
        );

        mockMvc.perform(post("/api/v1/encounters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/encounters - Returns 403 when caller is unauthorized third party")
    void createEncounter_UnauthorizedCaller() throws Exception {
        CreateEncounterRequestDto request = new CreateEncounterRequestDto(
                testAppointment.getId(),
                EncounterType.INITIAL_CONSULTATION,
                "Fever",
                null
        );

        mockMvc.perform(post("/api/v1/encounters")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, "unauthorized_user_random")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/encounters - Returns 400 when appointment is CANCELLED")
    void createEncounter_CancelledAppointment() throws Exception {
        testAppointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(testAppointment);

        CreateEncounterRequestDto request = new CreateEncounterRequestDto(
                testAppointment.getId(),
                EncounterType.INITIAL_CONSULTATION,
                "Fever",
                null
        );

        mockMvc.perform(post("/api/v1/encounters")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")));
    }

    @Test
    @DisplayName("POST /api/v1/encounters - Returns 409 when duplicate encounter created for same appointment")
    void createEncounter_DuplicateEncounter() throws Exception {
        EncounterEntity existing = new EncounterEntity(
                UUID.randomUUID(),
                "ENC-20261008-EXIST",
                testPatient,
                testProvider,
                testHospital,
                testAppointment,
                EncounterType.INITIAL_CONSULTATION,
                EncounterStatus.OPEN,
                null,
                "Fever",
                "doctor"
        );
        encounterRepository.save(existing);

        CreateEncounterRequestDto request = new CreateEncounterRequestDto(
                testAppointment.getId(),
                EncounterType.INITIAL_CONSULTATION,
                "Second encounter",
                null
        );

        mockMvc.perform(post("/api/v1/encounters")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("RESOURCE_ALREADY_EXISTS")));
    }

    @Test
    @DisplayName("GET /api/v1/encounters/{id} - Patient self access succeeds")
    void getEncounterById_PatientAccess() throws Exception {
        EncounterEntity encounter = new EncounterEntity(
                UUID.randomUUID(),
                "ENC-20261008-VIEW",
                testPatient,
                testProvider,
                testHospital,
                testAppointment,
                EncounterType.INITIAL_CONSULTATION,
                EncounterStatus.OPEN,
                null,
                "Fever",
                "doctor"
        );
        encounter = encounterRepository.save(encounter);

        mockMvc.perform(get("/api/v1/encounters/" + encounter.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, testPatient.getAuthUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(encounter.getId().toString())))
                .andExpect(jsonPath("$.data.patientName", is(testPatient.getFullName())));
    }

    @Test
    @DisplayName("GET /api/v1/encounters/{id} - Unauthorized patient receives 403 Forbidden")
    void getEncounterById_UnauthorizedPatient() throws Exception {
        EncounterEntity encounter = new EncounterEntity(
                UUID.randomUUID(),
                "ENC-20261008-IDOR",
                testPatient,
                testProvider,
                testHospital,
                testAppointment,
                EncounterType.INITIAL_CONSULTATION,
                EncounterStatus.OPEN,
                null,
                "Fever",
                "doctor"
        );
        encounter = encounterRepository.save(encounter);

        mockMvc.perform(get("/api/v1/encounters/" + encounter.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, "auth_other_patient_999"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/encounters/{id}/start - Transitions OPEN to IN_PROGRESS")
    void startEncounter_Success() throws Exception {
        EncounterEntity encounter = new EncounterEntity(
                UUID.randomUUID(),
                "ENC-20261008-STRT",
                testPatient,
                testProvider,
                testHospital,
                testAppointment,
                EncounterType.INITIAL_CONSULTATION,
                EncounterStatus.OPEN,
                null,
                "Fever",
                "doctor"
        );
        encounter = encounterRepository.save(encounter);

        mockMvc.perform(post("/api/v1/encounters/" + encounter.getId() + "/start")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.data.startedAt", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/encounters/{id}/complete - Finalizes encounter and appointment to COMPLETED")
    void completeEncounter_Success() throws Exception {
        EncounterEntity encounter = new EncounterEntity(
                UUID.randomUUID(),
                "ENC-20261008-COMP",
                testPatient,
                testProvider,
                testHospital,
                testAppointment,
                EncounterType.INITIAL_CONSULTATION,
                EncounterStatus.IN_PROGRESS,
                null,
                "Fever",
                "doctor"
        );
        encounter = encounterRepository.save(encounter);

        mockMvc.perform(post("/api/v1/encounters/" + encounter.getId() + "/complete")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("COMPLETED")))
                .andExpect(jsonPath("$.data.endedAt", notNullValue()));

        // Verify linked appointment was updated to COMPLETED
        AppointmentEntity updatedAppt = appointmentRepository.findById(testAppointment.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(AppointmentStatus.COMPLETED, updatedAppt.getStatus());
    }

    @Test
    @DisplayName("POST /api/v1/encounters/{id}/complete - Fails with 400 when encounter is OPEN (not yet in progress)")
    void completeEncounter_WhenOpen_Returns400() throws Exception {
        EncounterEntity encounter = new EncounterEntity(
                UUID.randomUUID(),
                "ENC-20261008-INVAL",
                testPatient,
                testProvider,
                testHospital,
                testAppointment,
                EncounterType.INITIAL_CONSULTATION,
                EncounterStatus.OPEN,
                null,
                "Fever",
                "doctor"
        );
        encounter = encounterRepository.save(encounter);

        mockMvc.perform(post("/api/v1/encounters/" + encounter.getId() + "/complete")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/encounters/{id} - Doctor updates clinical examination notes")
    void updateEncounter_Success() throws Exception {
        EncounterEntity encounter = new EncounterEntity(
                UUID.randomUUID(),
                "ENC-20261008-NOTE",
                testPatient,
                testProvider,
                testHospital,
                testAppointment,
                EncounterType.INITIAL_CONSULTATION,
                EncounterStatus.IN_PROGRESS,
                null,
                "Fever",
                "doctor"
        );
        encounter = encounterRepository.save(encounter);

        UpdateEncounterRequestDto updateDto = new UpdateEncounterRequestDto(
                "High fever and chest pain",
                "Lungs clear. Mild pharyngeal congestion.",
                "Viral Pharyngitis",
                "Paracetamol 650mg TDS, warm saline gargles.",
                "Follow up in 3 days"
        );

        mockMvc.perform(patch("/api/v1/encounters/" + encounter.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.chiefComplaint", is("High fever and chest pain")))
                .andExpect(jsonPath("$.data.clinicalNotes", is("Lungs clear. Mild pharyngeal congestion.")))
                .andExpect(jsonPath("$.data.diagnosisSummary", is("Viral Pharyngitis")))
                .andExpect(jsonPath("$.data.treatmentPlan", is("Paracetamol 650mg TDS, warm saline gargles.")));
    }

    @Test
    @DisplayName("GET /api/v1/patients/me/encounters - Patient lists own encounters")
    void getPatientEncounters_Success() throws Exception {
        EncounterEntity encounter = new EncounterEntity(
                UUID.randomUUID(),
                "ENC-20261008-LIST",
                testPatient,
                testProvider,
                testHospital,
                testAppointment,
                EncounterType.INITIAL_CONSULTATION,
                EncounterStatus.OPEN,
                null,
                "Fever",
                "doctor"
        );
        encounterRepository.save(encounter);

        mockMvc.perform(get("/api/v1/patients/me/encounters")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, testPatient.getAuthUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].encounterReference", is("ENC-20261008-LIST")));
    }
}
