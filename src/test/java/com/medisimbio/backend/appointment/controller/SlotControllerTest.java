package com.medisimbio.backend.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.entity.DoctorAvailabilityEntity;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.appointment.repository.DoctorAvailabilityRepository;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SlotControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    private PatientEntity testPatient;

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
                "Apollo Multi-Specialty Hospital",
                HospitalType.PRIVATE,
                "+91-80-87654321",
                "456 Health Boulevard",
                "Bengaluru",
                "Karnataka",
                "560001"
        );
        testHospital = hospitalRepository.save(testHospital);

        // Seed provider
        testProvider = new ProviderEntity(
                UUID.randomUUID(),
                "Dr. Ananya Roy",
                "KMC-TEST-55443",
                "MBBS, MS (Orthopedics)",
                10,
                "dr.ananya@example.com",
                "+91-8899776655"
        );
        testProvider = providerRepository.save(testProvider);

        // Affiliate provider with hospital
        com.medisimbio.backend.provider.entity.ProviderHospitalEntity affiliation =
                new com.medisimbio.backend.provider.entity.ProviderHospitalEntity(
                        UUID.randomUUID(),
                        testProvider,
                        testHospital,
                        "Orthopedics",
                        new java.math.BigDecimal("600.00"),
                        true
                );
        providerHospitalRepository.save(affiliation);

        // Seed patient
        testPatient = new PatientEntity(
                UUID.randomUUID(),
                "patient_auth_slot_user",
                "Rahul Verma",
                "rahul.verma@example.com",
                "+91-9876543210",
                LocalDate.of(1990, 1, 15),
                Gender.MALE
        );
        testPatient = patientRepository.save(testPatient);
    }

    @Test
    @DisplayName("GET /api/v1/providers/{providerId}/slots - Computes dynamic slots for a given date")
    void shouldComputeDynamicSlots() throws Exception {
        LocalDate targetDate = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));

        // Create availability on Wednesday 09:00 - 11:00, 30 min slots (4 slots total)
        DoctorAvailabilityEntity availability = new DoctorAvailabilityEntity(
                testProvider,
                testHospital,
                DayOfWeek.WEDNESDAY,
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                30
        );
        availabilityRepository.save(availability);

        mockMvc.perform(get("/api/v1/providers/{providerId}/slots", testProvider.getId())
                        .param("date", targetDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[0].startTime").value("09:00:00"))
                .andExpect(jsonPath("$.data[0].endTime").value("09:30:00"))
                .andExpect(jsonPath("$.data[0].available").value(true))
                .andExpect(jsonPath("$.data[1].startTime").value("09:30:00"))
                .andExpect(jsonPath("$.data[1].available").value(true))
                .andExpect(jsonPath("$.data[2].startTime").value("10:00:00"))
                .andExpect(jsonPath("$.data[2].available").value(true))
                .andExpect(jsonPath("$.data[3].startTime").value("10:30:00"))
                .andExpect(jsonPath("$.data[3].available").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/providers/{providerId}/slots - Booked slot shows as unavailable")
    void shouldShowBookedSlotAsUnavailable() throws Exception {
        LocalDate targetDate = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY));

        // Availability: Thursday 10:00 - 12:00, 30 min slots (4 slots)
        DoctorAvailabilityEntity availability = new DoctorAvailabilityEntity(
                testProvider,
                testHospital,
                DayOfWeek.THURSDAY,
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                30
        );
        availabilityRepository.save(availability);

        // Book slot 10:30 - 11:00
        AppointmentEntity appointment = new AppointmentEntity(
                "MSB-20261015-TEST",
                testPatient,
                testProvider,
                testHospital,
                targetDate,
                LocalTime.of(10, 30),
                LocalTime.of(11, 0),
                AppointmentStatus.SCHEDULED,
                "Knee pain consultation"
        );
        appointmentRepository.save(appointment);

        mockMvc.perform(get("/api/v1/providers/{providerId}/slots", testProvider.getId())
                        .param("date", targetDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[0].startTime").value("10:00:00"))
                .andExpect(jsonPath("$.data[0].available").value(true))
                .andExpect(jsonPath("$.data[1].startTime").value("10:30:00"))
                .andExpect(jsonPath("$.data[1].available").value(false))
                .andExpect(jsonPath("$.data[2].startTime").value("11:00:00"))
                .andExpect(jsonPath("$.data[2].available").value(true))
                .andExpect(jsonPath("$.data[3].startTime").value("11:30:00"))
                .andExpect(jsonPath("$.data[3].available").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/providers/{providerId}/slots - Rejects past date with 400 Bad Request")
    void shouldRejectPastDate() throws Exception {
        LocalDate pastDate = LocalDate.now().minusDays(2);

        mockMvc.perform(get("/api/v1/providers/{providerId}/slots", testProvider.getId())
                        .param("date", pastDate.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")));
    }
}
