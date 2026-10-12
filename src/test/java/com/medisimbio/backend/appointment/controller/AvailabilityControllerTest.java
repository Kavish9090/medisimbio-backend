package com.medisimbio.backend.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.appointment.dto.CreateAvailabilityRequestDto;
import com.medisimbio.backend.appointment.dto.UpdateAvailabilityRequestDto;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.appointment.repository.DoctorAvailabilityRepository;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.entity.HospitalType;
import com.medisimbio.backend.hospital.repository.HospitalRepository;
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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

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

    private static final String TEST_AUTH_USER = "auth_admin_001";

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        providerHospitalRepository.deleteAll();
        patientRepository.deleteAll();
        providerRepository.deleteAll();
        hospitalRepository.deleteAll();

        // Seed hospital
        testHospital = new HospitalEntity(
                UUID.randomUUID(),
                "City General Hospital",
                HospitalType.GOVERNMENT,
                "+91-22-12345678",
                "123 Healthcare Ave",
                "Mumbai",
                "Maharashtra",
                "400001"
        );
        testHospital = hospitalRepository.save(testHospital);

        // Seed provider
        testProvider = new ProviderEntity(
                UUID.randomUUID(),
                "Dr. Rajesh Sharma",
                "MCI-TEST-99887",
                "MBBS, MD (Cardiology)",
                12,
                "dr.rajesh@example.com",
                "+91-9876543210"
        );
        testProvider = providerRepository.save(testProvider);

        // Affiliate provider with hospital
        com.medisimbio.backend.provider.entity.ProviderHospitalEntity affiliation =
                new com.medisimbio.backend.provider.entity.ProviderHospitalEntity(
                        UUID.randomUUID(),
                        testProvider,
                        testHospital,
                        "Cardiology",
                        new java.math.BigDecimal("500.00"),
                        true
                );
        providerHospitalRepository.save(affiliation);
    }

    @Test
    @DisplayName("GET /api/v1/providers/{providerId}/availability - Publicly retrieves doctor availability")
    void shouldGetAvailabilityPublicly() throws Exception {
        // Create availability via authenticated POST
        CreateAvailabilityRequestDto createDto = new CreateAvailabilityRequestDto(
                testHospital.getId(),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(13, 0),
                30
        );

        mockMvc.perform(post("/api/v1/providers/{providerId}/availability", testProvider.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());

        // Retrieve publicly without auth header
        mockMvc.perform(get("/api/v1/providers/{providerId}/availability", testProvider.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].providerId").value(testProvider.getId().toString()))
                .andExpect(jsonPath("$.data[0].hospitalId").value(testHospital.getId().toString()))
                .andExpect(jsonPath("$.data[0].dayOfWeek").value("MONDAY"))
                .andExpect(jsonPath("$.data[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$.data[0].endTime").value("13:00:00"))
                .andExpect(jsonPath("$.data[0].slotDurationMinutes").value(30))
                .andExpect(jsonPath("$.data[0].active").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/providers/{providerId}/availability - Unauthenticated request returns 401")
    void shouldRejectUnauthenticatedCreateAvailability() throws Exception {
        CreateAvailabilityRequestDto createDto = new CreateAvailabilityRequestDto(
                testHospital.getId(),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(13, 0),
                30
        );

        mockMvc.perform(post("/api/v1/providers/{providerId}/availability", testProvider.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/v1/providers/{providerId}/availability - Rejects overlapping availability schedule")
    void shouldRejectOverlappingSchedule() throws Exception {
        CreateAvailabilityRequestDto createDto1 = new CreateAvailabilityRequestDto(
                testHospital.getId(),
                DayOfWeek.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(13, 0),
                30
        );

        mockMvc.perform(post("/api/v1/providers/{providerId}/availability", testProvider.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto1)))
                .andExpect(status().isCreated());

        // Overlapping schedule: 11:00 to 15:00 on MONDAY
        CreateAvailabilityRequestDto overlapDto = new CreateAvailabilityRequestDto(
                testHospital.getId(),
                DayOfWeek.MONDAY,
                LocalTime.of(11, 0),
                LocalTime.of(15, 0),
                30
        );

        mockMvc.perform(post("/api/v1/providers/{providerId}/availability", testProvider.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(overlapDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("RESOURCE_ALREADY_EXISTS")));
    }

    @Test
    @DisplayName("POST /api/v1/providers/{providerId}/availability - Rejects invalid time range (start >= end)")
    void shouldRejectInvalidTimeRange() throws Exception {
        CreateAvailabilityRequestDto invalidDto = new CreateAvailabilityRequestDto(
                testHospital.getId(),
                DayOfWeek.MONDAY,
                LocalTime.of(14, 0),
                LocalTime.of(10, 0),
                30
        );

        mockMvc.perform(post("/api/v1/providers/{providerId}/availability", testProvider.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")));
    }

    @Test
    @DisplayName("PUT /api/v1/providers/{providerId}/availability/{id} - Updates existing availability")
    void shouldUpdateAvailability() throws Exception {
        CreateAvailabilityRequestDto createDto = new CreateAvailabilityRequestDto(
                testHospital.getId(),
                DayOfWeek.TUESDAY,
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                30
        );

        String createResponse = mockMvc.perform(post("/api/v1/providers/{providerId}/availability", testProvider.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String availabilityId = objectMapper.readTree(createResponse).get("data").get("id").asText();

        UpdateAvailabilityRequestDto updateDto = new UpdateAvailabilityRequestDto();
        updateDto.setStartTime(LocalTime.of(10, 0));
        updateDto.setEndTime(LocalTime.of(14, 0));
        updateDto.setSlotDurationMinutes(20);

        mockMvc.perform(put("/api/v1/providers/{providerId}/availability/{id}", testProvider.getId(), availabilityId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.startTime").value("10:00:00"))
                .andExpect(jsonPath("$.data.endTime").value("14:00:00"))
                .andExpect(jsonPath("$.data.slotDurationMinutes").value(20));
    }

    @Test
    @DisplayName("DELETE /api/v1/providers/{providerId}/availability/{id} - Deletes availability")
    void shouldDeleteAvailability() throws Exception {
        CreateAvailabilityRequestDto createDto = new CreateAvailabilityRequestDto(
                testHospital.getId(),
                DayOfWeek.FRIDAY,
                LocalTime.of(14, 0),
                LocalTime.of(18, 0),
                15
        );

        String createResponse = mockMvc.perform(post("/api/v1/providers/{providerId}/availability", testProvider.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String availabilityId = objectMapper.readTree(createResponse).get("data").get("id").asText();

        mockMvc.perform(delete("/api/v1/providers/{providerId}/availability/{id}", testProvider.getId(), availabilityId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, TEST_AUTH_USER))
                .andExpect(status().isOk());

        // Verify it is gone
        mockMvc.perform(get("/api/v1/providers/{providerId}/availability", testProvider.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(0)));
    }
}
