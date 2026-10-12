package com.medisimbio.backend.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.appointment.dto.BookAppointmentRequestDto;
import com.medisimbio.backend.appointment.dto.CancelAppointmentRequestDto;
import com.medisimbio.backend.appointment.dto.RescheduleAppointmentRequestDto;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private com.medisimbio.backend.provider.repository.ProviderHospitalRepository providerHospitalRepository;

    @Autowired
    private com.medisimbio.backend.hospital.repository.HospitalStaffRepository hospitalStaffRepository;

    private ProviderEntity testProvider;
    private HospitalEntity testHospital;
    private PatientEntity testPatient1;
    private PatientEntity testPatient2;

    private static final String AUTH_PATIENT_1 = "auth_patient_001";
    private static final String AUTH_PATIENT_2 = "auth_patient_002";
    private static final String AUTH_PROVIDER = "auth_doctor_vikram_001";
    private static final String AUTH_HOSPITAL_STAFF = "auth_hospital_admin_001";

    @BeforeEach
    void setUp() {
        appointmentRepository.deleteAll();
        availabilityRepository.deleteAll();
        providerHospitalRepository.deleteAll();
        hospitalStaffRepository.deleteAll();
        patientRepository.deleteAll();
        providerRepository.deleteAll();
        hospitalRepository.deleteAll();

        // Seed hospital
        testHospital = new HospitalEntity(
                UUID.randomUUID(),
                "Fortis Healthcare",
                HospitalType.PRIVATE,
                "+91-11-22334455",
                "789 Medical Square",
                "Delhi",
                "Delhi",
                "110001"
        );
        testHospital = hospitalRepository.save(testHospital);

        // Seed hospital staff
        hospitalStaffRepository.save(new com.medisimbio.backend.hospital.entity.HospitalStaffEntity(
                UUID.randomUUID(),
                testHospital,
                AUTH_HOSPITAL_STAFF,
                "Admin Officer",
                "ADMIN",
                true
        ));

        // Seed provider
        testProvider = new ProviderEntity(
                UUID.randomUUID(),
                AUTH_PROVIDER,
                "Dr. Vikram Seth",
                "DMC-TEST-12345",
                "MBBS, MD (General Medicine)",
                15,
                "dr.vikram@example.com",
                "+91-9123456789"
        );
        testProvider = providerRepository.save(testProvider);

        // Affiliate provider with hospital
        com.medisimbio.backend.provider.entity.ProviderHospitalEntity affiliation =
                new com.medisimbio.backend.provider.entity.ProviderHospitalEntity(
                        UUID.randomUUID(),
                        testProvider,
                        testHospital,
                        "General Medicine",
                        new java.math.BigDecimal("700.00"),
                        true
                );
        providerHospitalRepository.save(affiliation);

        // Seed patients
        testPatient1 = new PatientEntity(
                UUID.randomUUID(),
                AUTH_PATIENT_1,
                "Aarav Patel",
                "aarav.patel@example.com",
                "+91-9123456780",
                LocalDate.of(1992, 6, 20),
                Gender.MALE
        );
        testPatient1 = patientRepository.save(testPatient1);

        testPatient2 = new PatientEntity(
                UUID.randomUUID(),
                AUTH_PATIENT_2,
                "Priya Sharma",
                "priya.sharma@example.com",
                "+91-9123456781",
                LocalDate.of(1995, 8, 10),
                Gender.FEMALE
        );
        testPatient2 = patientRepository.save(testPatient2);

        // Seed availability for Friday 09:00 - 13:00 (30 min slots)
        DoctorAvailabilityEntity availabilityFriday = new DoctorAvailabilityEntity(
                testProvider,
                testHospital,
                DayOfWeek.FRIDAY,
                LocalTime.of(9, 0),
                LocalTime.of(13, 0),
                30
        );
        availabilityRepository.save(availabilityFriday);

        // Seed availability for Monday 10:00 - 14:00 (30 min slots)
        DoctorAvailabilityEntity availabilityMonday = new DoctorAvailabilityEntity(
                testProvider,
                testHospital,
                DayOfWeek.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(14, 0),
                30
        );
        availabilityRepository.save(availabilityMonday);
    }

    @Test
    @DisplayName("POST /api/v1/appointments - Successfully books a consultation slot")
    void shouldBookAppointmentSuccessfully() throws Exception {
        LocalDate nextFriday = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));

        BookAppointmentRequestDto bookDto = new BookAppointmentRequestDto(
                testProvider.getId(),
                testHospital.getId(),
                nextFriday,
                LocalTime.of(9, 30),
                "Routine health checkup and blood pressure review"
        );

        mockMvc.perform(post("/api/v1/appointments")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id").isNotEmpty())
                .andExpect(jsonPath("$.data.bookingReference", startsWith("MSB-")))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.data.appointmentDate").value(nextFriday.toString()))
                .andExpect(jsonPath("$.data.startTime").value("09:30:00"))
                .andExpect(jsonPath("$.data.endTime").value("10:00:00"))
                .andExpect(jsonPath("$.data.patientName").value("Aarav Patel"))
                .andExpect(jsonPath("$.data.providerName").value("Dr. Vikram Seth"))
                .andExpect(jsonPath("$.data.hospitalName").value("Fortis Healthcare"));
    }

    @Test
    @DisplayName("POST /api/v1/appointments - Double booking identical slot returns 409 Conflict")
    void shouldRejectDoubleBookingSameSlot() throws Exception {
        LocalDate nextFriday = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));

        BookAppointmentRequestDto bookDto = new BookAppointmentRequestDto(
                testProvider.getId(),
                testHospital.getId(),
                nextFriday,
                LocalTime.of(10, 0),
                "First booking"
        );

        // First booking succeeds
        mockMvc.perform(post("/api/v1/appointments")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isCreated());

        // Second booking by another patient fails with 409 Conflict
        BookAppointmentRequestDto secondBookingDto = new BookAppointmentRequestDto(
                testProvider.getId(),
                testHospital.getId(),
                nextFriday,
                LocalTime.of(10, 0),
                "Second concurrent booking attempt"
        );

        mockMvc.perform(post("/api/v1/appointments")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondBookingDto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("RESOURCE_ALREADY_EXISTS")));
    }

    @Test
    @DisplayName("POST /api/v1/appointments - Booking invalid slot outside availability returns 400")
    void shouldRejectBookingOutsideAvailability() throws Exception {
        LocalDate nextFriday = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));

        // Provider is available 09:00 - 13:00 on Friday. 15:00 is outside availability.
        BookAppointmentRequestDto bookDto = new BookAppointmentRequestDto(
                testProvider.getId(),
                testHospital.getId(),
                nextFriday,
                LocalTime.of(15, 0),
                "Attempting booking outside schedule"
        );

        mockMvc.perform(post("/api/v1/appointments")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")));
    }

    @Test
    @DisplayName("GET /api/v1/appointments/me - Retrieves patient's appointment list")
    void shouldGetMyAppointments() throws Exception {
        LocalDate nextFriday = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));

        BookAppointmentRequestDto bookDto = new BookAppointmentRequestDto(
                testProvider.getId(),
                testHospital.getId(),
                nextFriday,
                LocalTime.of(11, 0),
                "Consultation notes"
        );

        mockMvc.perform(post("/api/v1/appointments")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/appointments/me")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].patientName").value("Aarav Patel"))
                .andExpect(jsonPath("$.data.items[0].providerName").value("Dr. Vikram Seth"));
    }

    @Test
    @DisplayName("GET /api/v1/appointments/{id} - Isolation: Patient 2 cannot view Patient 1 appointment")
    void shouldEnforcePatientDataIsolation() throws Exception {
        LocalDate nextFriday = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));

        BookAppointmentRequestDto bookDto = new BookAppointmentRequestDto(
                testProvider.getId(),
                testHospital.getId(),
                nextFriday,
                LocalTime.of(11, 30),
                "Confidential consultation"
        );

        String createResponse = mockMvc.perform(post("/api/v1/appointments")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("data").get("id").asText();

        // Patient 1 can view
        mockMvc.perform(get("/api/v1/appointments/{id}", appointmentId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1))
                .andExpect(status().isOk());

        // Patient 2 is forbidden
        mockMvc.perform(get("/api/v1/appointments/{id}", appointmentId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_2))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("POST /api/v1/appointments/{id}/cancel - Cancels appointment and releases slot")
    void shouldCancelAppointmentSuccessfully() throws Exception {
        LocalDate nextFriday = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));

        BookAppointmentRequestDto bookDto = new BookAppointmentRequestDto(
                testProvider.getId(),
                testHospital.getId(),
                nextFriday,
                LocalTime.of(12, 0),
                "Initial booking"
        );

        String createResponse = mockMvc.perform(post("/api/v1/appointments")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("data").get("id").asText();

        CancelAppointmentRequestDto cancelDto = new CancelAppointmentRequestDto("Unable to attend due to work trip");

        mockMvc.perform(post("/api/v1/appointments/{id}/cancel", appointmentId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.cancellationReason").value("Unable to attend due to work trip"))
                .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());

        // Now Patient 2 can book that exact slot since it was released
        mockMvc.perform(post("/api/v1/appointments")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/appointments/{id}/reschedule - Atomically reschedules appointment")
    void shouldRescheduleAppointmentAtomically() throws Exception {
        LocalDate friday1 = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        LocalDate mondayNext = LocalDate.now().plusWeeks(2).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        BookAppointmentRequestDto bookDto = new BookAppointmentRequestDto(
                testProvider.getId(),
                testHospital.getId(),
                friday1,
                LocalTime.of(9, 0),
                "Original consultation"
        );

        String createResponse = mockMvc.perform(post("/api/v1/appointments")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String originalAppointmentId = objectMapper.readTree(createResponse).get("data").get("id").asText();

        RescheduleAppointmentRequestDto rescheduleDto = new RescheduleAppointmentRequestDto(
                mondayNext,
                LocalTime.of(10, 0)
        );

        mockMvc.perform(post("/api/v1/appointments/{id}/reschedule", originalAppointmentId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rescheduleDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.data.appointmentDate").value(mondayNext.toString()))
                .andExpect(jsonPath("$.data.startTime").value("10:00:00"))
                .andExpect(jsonPath("$.data.rescheduledFromId").value(originalAppointmentId));

        // Verify the original appointment is marked CANCELLED
        mockMvc.perform(get("/api/v1/appointments/{id}", originalAppointmentId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.data.cancellationReason", containsString("Rescheduled to appointment")));
    }

    @Test
    @DisplayName("GET /api/v1/providers/{providerId}/appointments - Unauthenticated access returns 401")
    void shouldRejectUnauthenticatedProviderAppointments() throws Exception {
        mockMvc.perform(get("/api/v1/providers/{providerId}/appointments", testProvider.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/providers/{providerId}/appointments - Unauthorized caller returns 403 Forbidden")
    void shouldRejectUnauthorizedProviderAppointments() throws Exception {
        mockMvc.perform(get("/api/v1/providers/{providerId}/appointments", testProvider.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("GET /api/v1/providers/{providerId}/appointments - Authorized provider receives appointment directory")
    void shouldAllowAuthorizedProviderAppointments() throws Exception {
        mockMvc.perform(get("/api/v1/providers/{providerId}/appointments", testProvider.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PROVIDER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("GET /api/v1/hospitals/{hospitalId}/appointments - Unauthenticated access returns 401")
    void shouldRejectUnauthenticatedHospitalAppointments() throws Exception {
        mockMvc.perform(get("/api/v1/hospitals/{hospitalId}/appointments", testHospital.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/hospitals/{hospitalId}/appointments - Unauthorized caller returns 403 Forbidden")
    void shouldRejectUnauthorizedHospitalAppointments() throws Exception {
        mockMvc.perform(get("/api/v1/hospitals/{hospitalId}/appointments", testHospital.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("GET /api/v1/hospitals/{hospitalId}/appointments - Authorized hospital facility receives appointment directory")
    void shouldAllowAuthorizedHospitalAppointments() throws Exception {
        mockMvc.perform(get("/api/v1/hospitals/{hospitalId}/appointments", testHospital.getId())
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_HOSPITAL_STAFF))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @DisplayName("POST /api/v1/appointments/{id}/cancel - Patient 2 cannot cancel Patient 1 appointment (403 Forbidden)")
    void shouldRejectCancelByUnauthorizedPatient() throws Exception {
        LocalDate nextFriday = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));

        BookAppointmentRequestDto bookDto = new BookAppointmentRequestDto(
                testProvider.getId(),
                testHospital.getId(),
                nextFriday,
                LocalTime.of(12, 30),
                "Privacy cancellation test"
        );

        String createResponse = mockMvc.perform(post("/api/v1/appointments")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("data").get("id").asText();

        CancelAppointmentRequestDto cancelDto = new CancelAppointmentRequestDto("Attacker trying to cancel");

        mockMvc.perform(post("/api/v1/appointments/{id}/cancel", appointmentId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("POST /api/v1/appointments/{id}/reschedule - Patient 2 cannot reschedule Patient 1 appointment (403 Forbidden)")
    void shouldRejectRescheduleByUnauthorizedPatient() throws Exception {
        LocalDate nextFriday = LocalDate.now().plusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        LocalDate mondayNext = LocalDate.now().plusWeeks(2).with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        BookAppointmentRequestDto bookDto = new BookAppointmentRequestDto(
                testProvider.getId(),
                testHospital.getId(),
                nextFriday,
                LocalTime.of(9, 30),
                "Privacy reschedule test"
        );

        String createResponse = mockMvc.perform(post("/api/v1/appointments")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String appointmentId = objectMapper.readTree(createResponse).get("data").get("id").asText();

        RescheduleAppointmentRequestDto rescheduleDto = new RescheduleAppointmentRequestDto(
                mondayNext,
                LocalTime.of(11, 0)
        );

        mockMvc.perform(post("/api/v1/appointments/{id}/reschedule", appointmentId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT_2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rescheduleDto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));
    }
}
