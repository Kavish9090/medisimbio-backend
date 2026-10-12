package com.medisimbio.backend.vitals.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
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
import com.medisimbio.backend.vitals.dto.CreateVitalsRequestDto;
import com.medisimbio.backend.vitals.entity.VitalsEntity;
import com.medisimbio.backend.vitals.repository.VitalsRepository;
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

import java.math.BigDecimal;
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
class VitalsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VitalsRepository vitalsRepository;

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

    private static final String AUTH_DOCTOR = "auth_doctor_vit";

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
                "auth_patient_vit",
                "Karan Gupta",
                "karan.vit@test.com",
                "+919876543202",
                LocalDate.of(1988, 3, 20),
                Gender.MALE
        );
        testPatient = patientRepository.save(testPatient);

        testHospital = new HospitalEntity(
                UUID.randomUUID(),
                "Apex Clinic",
                HospitalType.PRIVATE,
                "+911144332211",
                "10 Health Park",
                "Bengaluru",
                "Karnataka",
                "560001"
        );
        testHospital = hospitalRepository.save(testHospital);

        testProvider = new ProviderEntity(
                UUID.randomUUID(),
                AUTH_DOCTOR,
                "Dr. Meera Rao",
                "MCI-VIT-01",
                "MBBS, MD",
                8,
                "meera.vit@test.com",
                "+919822334455"
        );
        testProvider = providerRepository.save(testProvider);

        testAppointment = new AppointmentEntity(
                UUID.randomUUID(),
                testPatient,
                testProvider,
                testHospital,
                null,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 30),
                LocalTime.of(10, 0),
                AppointmentStatus.CONFIRMED,
                "MSB-VIT-APP-01",
                "Annual health checkup"
        );
        testAppointment = appointmentRepository.save(testAppointment);

        testEncounter = new EncounterEntity(
                UUID.randomUUID(),
                "ENC-20261008-VIT1",
                testPatient,
                testProvider,
                testHospital,
                testAppointment,
                EncounterType.ROUTINE_CHECKUP,
                EncounterStatus.IN_PROGRESS,
                null,
                "Routine checkup",
                "doctor"
        );
        testEncounter = encounterRepository.save(testEncounter);
    }

    @Test
    @DisplayName("POST /api/v1/encounters/{id}/vitals - Records vitals and calculates BMI automatically")
    void createVitals_CalculatesBmi_Success() throws Exception {
        CreateVitalsRequestDto request = new CreateVitalsRequestDto(
                null,
                new BigDecimal("98.4"),
                72,
                16,
                120,
                80,
                new BigDecimal("99.0"),
                new BigDecimal("70.00"), // 70 kg
                new BigDecimal("175.00"), // 175 cm -> BMI = 70 / (1.75)^2 = 22.857 -> 22.9
                null, // BMI calculated by service
                new BigDecimal("92.0"),
                "Resting vitals recorded"
        );

        mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/vitals")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", notNullValue()))
                .andExpect(jsonPath("$.data.temperature", is(98.4)))
                .andExpect(jsonPath("$.data.heartRate", is(72)))
                .andExpect(jsonPath("$.data.bloodPressureFormatted", is("120/80 mmHg")))
                .andExpect(jsonPath("$.data.bmi", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/encounters/{id}/vitals - Negative temperature fails bean validation (400)")
    void createVitals_NegativeTemperature_ValidationFails() throws Exception {
        CreateVitalsRequestDto request = new CreateVitalsRequestDto(
                null,
                new BigDecimal("-5.0"), // Invalid negative temp
                72,
                16,
                120,
                80,
                new BigDecimal("98.0"),
                new BigDecimal("70.00"),
                new BigDecimal("175.00"),
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/vitals")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("GET /api/v1/encounters/{id}/vitals - Patient retrieves encounter vitals")
    void getVitalsByEncounter_Success() throws Exception {
        VitalsEntity vitals = new VitalsEntity(
                UUID.randomUUID(),
                testEncounter,
                testPatient,
                null,
                new BigDecimal("98.6"),
                76,
                16,
                118,
                78,
                new BigDecimal("98.5"),
                new BigDecimal("68.00"),
                new BigDecimal("170.00"),
                new BigDecimal("23.5"),
                new BigDecimal("95.0"),
                "Normal baseline",
                "doctor"
        );
        vitalsRepository.save(vitals);

        mockMvc.perform(get("/api/v1/encounters/" + testEncounter.getId() + "/vitals")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, testPatient.getAuthUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].heartRate", is(76)))
                .andExpect(jsonPath("$.data[0].bloodPressureFormatted", is("118/78 mmHg")));
    }
}
