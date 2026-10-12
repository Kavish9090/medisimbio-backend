package com.medisimbio.backend.dispensing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.dispensing.dto.CreateDispensingOrderRequestDto;
import com.medisimbio.backend.dispensing.repository.DispensingOrderRepository;
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
import com.medisimbio.backend.prescription.dto.CreatePrescriptionRequestDto;
import com.medisimbio.backend.prescription.dto.PrescriptionItemRequestDto;
import com.medisimbio.backend.prescription.dto.PrescriptionResponseDto;
import com.medisimbio.backend.prescription.service.PrescriptionService;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DispensingOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private com.medisimbio.backend.prescription.repository.PrescriptionRepository prescriptionRepository;

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
    private HospitalStaffRepository hospitalStaffRepository;

    private PatientEntity testPatient;
    private PatientEntity otherPatient;
    private ProviderEntity testProvider;
    private HospitalEntity testHospital;
    private AppointmentEntity testAppointment;
    private EncounterEntity testEncounter;
    private com.medisimbio.backend.prescription.entity.PrescriptionEntity finalizedPrescription;
    private com.medisimbio.backend.prescription.entity.PrescriptionEntity draftPrescription;

    private static final String AUTH_DOCTOR = "auth_doc_dsp_1";
    private static final String AUTH_PATIENT = "auth_pat_dsp_1";
    private static final String AUTH_OTHER_PATIENT = "auth_pat_dsp_2";
    private static final String AUTH_STAFF = "auth_staff_dsp_1";

    @BeforeEach
    void setUp() {
        cleanDb();

        testPatient = patientRepository.save(new PatientEntity(
                UUID.randomUUID(), AUTH_PATIENT, "Vikram Malhotra", "vikram.dsp@test.com",
                "+919876543999", LocalDate.of(1989, 5, 14), Gender.MALE
        ));

        otherPatient = patientRepository.save(new PatientEntity(
                UUID.randomUUID(), AUTH_OTHER_PATIENT, "Sneha Joshi", "sneha.dsp@test.com",
                "+919876543888", LocalDate.of(1993, 11, 2), Gender.FEMALE
        ));

        testHospital = hospitalRepository.save(new HospitalEntity(
                UUID.randomUUID(), "Fortis Dispensing Hub", HospitalType.PRIVATE,
                "+911144556677", "12 Care Road", "Bengaluru", "Karnataka", "560002"
        ));

        hospitalStaffRepository.save(new HospitalStaffEntity(
                UUID.randomUUID(), testHospital, AUTH_STAFF, "Pharmacist John", "PHARMACIST", true
        ));

        testProvider = providerRepository.save(new ProviderEntity(
                UUID.randomUUID(), AUTH_DOCTOR, "Dr. Siddharth Sen", "MCI-DSP-01",
                "MBBS, MD Internal Medicine", 15, "siddharth.dsp@test.com", "+919811445566"
        ));

        testAppointment = appointmentRepository.save(new AppointmentEntity(
                UUID.randomUUID(), testPatient, testProvider, testHospital, null,
                LocalDate.now().plusDays(1), LocalTime.of(14, 0), LocalTime.of(14, 30),
                AppointmentStatus.CONFIRMED, "MSB-DSP-APP-01", "Consultation"
        ));

        testEncounter = encounterRepository.save(new EncounterEntity(
                UUID.randomUUID(), "ENC-DSP-001", testPatient, testProvider, testHospital, testAppointment,
                EncounterType.INITIAL_CONSULTATION, EncounterStatus.IN_PROGRESS, null,
                "Acute acidity and stomach discomfort", AUTH_DOCTOR
        ));

        finalizedPrescription = new com.medisimbio.backend.prescription.entity.PrescriptionEntity(
                UUID.randomUUID(), "RX-DSP-001", testPatient, testEncounter, testProvider, testHospital,
                com.medisimbio.backend.prescription.entity.PrescriptionStatus.FINALIZED,
                java.time.Instant.now(), LocalDate.now().plusMonths(1), "Gastritis treatment", 1, null, AUTH_DOCTOR
        );
        finalizedPrescription.setFinalizedAt(java.time.Instant.now());
        com.medisimbio.backend.prescription.entity.PrescriptionItemEntity item1 =
                new com.medisimbio.backend.prescription.entity.PrescriptionItemEntity(
                        UUID.randomUUID(), finalizedPrescription, "Pantoprazole 40mg", "Pantoprazole", "40 mg",
                        "TABLET", "1 tablet", "ORAL", "ONCE_DAILY", 14, "DAYS", 14, "TABLETS", 0, "Empty stomach", 1
                );
        com.medisimbio.backend.prescription.entity.PrescriptionItemEntity item2 =
                new com.medisimbio.backend.prescription.entity.PrescriptionItemEntity(
                        UUID.randomUUID(), finalizedPrescription, "Domperidone 10mg", "Domperidone", "10 mg",
                        "TABLET", "1 tablet", "ORAL", "TWICE_DAILY", 7, "DAYS", 14, "TABLETS", 0, "Before meals", 2
                );
        finalizedPrescription.getItems().add(item1);
        finalizedPrescription.getItems().add(item2);
        finalizedPrescription = prescriptionRepository.save(finalizedPrescription);

        draftPrescription = new com.medisimbio.backend.prescription.entity.PrescriptionEntity(
                UUID.randomUUID(), "RX-DSP-002", testPatient, testEncounter, testProvider, testHospital,
                com.medisimbio.backend.prescription.entity.PrescriptionStatus.DRAFT,
                java.time.Instant.now(), LocalDate.now().plusMonths(1), "Draft notes", 1, null, AUTH_DOCTOR
        );
        com.medisimbio.backend.prescription.entity.PrescriptionItemEntity itemDraft =
                new com.medisimbio.backend.prescription.entity.PrescriptionItemEntity(
                        UUID.randomUUID(), draftPrescription, "Pantoprazole 40mg", "Pantoprazole", "40 mg",
                        "TABLET", "1 tablet", "ORAL", "ONCE_DAILY", 14, "DAYS", 14, "TABLETS", 0, "Empty stomach", 1
                );
        draftPrescription.getItems().add(itemDraft);
        draftPrescription = prescriptionRepository.save(draftPrescription);
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

    @Test
    @DisplayName("POST /prescriptions/{id}/dispensing-orders - Patient creates dispensing order for finalized prescription")
    void createDispensingOrder_Success() throws Exception {
        CreateDispensingOrderRequestDto request = new CreateDispensingOrderRequestDto(
                "Home delivery preferred", null
        );

        mockMvc.perform(post("/api/v1/prescriptions/" + finalizedPrescription.getId() + "/dispensing-orders")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("PENDING")))
                .andExpect(jsonPath("$.data.dispensingReference", startsWith("DSP-")))
                .andExpect(jsonPath("$.data.items", hasSize(2)))
                .andExpect(jsonPath("$.data.items[0].dispensedQuantity", is(0)))
                .andExpect(jsonPath("$.data.items[1].dispensedQuantity", is(0)));
    }

    @Test
    @DisplayName("POST /prescriptions/{id}/dispensing-orders - Fails for DRAFT prescription (400)")
    void createDispensingOrder_DraftPrescription_Fails() throws Exception {
        CreateDispensingOrderRequestDto request = new CreateDispensingOrderRequestDto("Deliver soon", null);

        mockMvc.perform(post("/api/v1/prescriptions/" + draftPrescription.getId() + "/dispensing-orders")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")));
    }

    @Test
    @DisplayName("POST /prescriptions/{id}/dispensing-orders - Duplicate active order rejected with 409 Conflict")
    void createDispensingOrder_DuplicateOrder_Conflict() throws Exception {
        CreateDispensingOrderRequestDto request = new CreateDispensingOrderRequestDto("First order", null);

        mockMvc.perform(post("/api/v1/prescriptions/" + finalizedPrescription.getId() + "/dispensing-orders")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Repeated request on same prescription while order is active
        mockMvc.perform(post("/api/v1/prescriptions/" + finalizedPrescription.getId() + "/dispensing-orders")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")));
    }

    @Test
    @DisplayName("POST /prescriptions/{id}/dispensing-orders - IDOR: Other patient cannot create order (403)")
    void createDispensingOrder_OtherPatient_Forbidden() throws Exception {
        CreateDispensingOrderRequestDto request = new CreateDispensingOrderRequestDto(null, null);

        mockMvc.perform(post("/api/v1/prescriptions/" + finalizedPrescription.getId() + "/dispensing-orders")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_OTHER_PATIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("GET /dispensing-orders/{id} - Patient retrieves own order, other patient forbidden (403)")
    void getDispensingOrder_AccessControl() throws Exception {
        CreateDispensingOrderRequestDto request = new CreateDispensingOrderRequestDto("Pickup at hospital pharmacy", null);

        String responseStr = mockMvc.perform(post("/api/v1/prescriptions/" + finalizedPrescription.getId() + "/dispensing-orders")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        // Own patient can view
        mockMvc.perform(get("/api/v1/dispensing-orders/" + orderId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("PENDING")));

        // Other patient cannot view
        mockMvc.perform(get("/api/v1/dispensing-orders/" + orderId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_OTHER_PATIENT))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /dispensing-orders/{id}/cancel - Cancels pending dispensing order")
    void cancelDispensingOrder_Success() throws Exception {
        CreateDispensingOrderRequestDto request = new CreateDispensingOrderRequestDto("Pickup", null);

        String responseStr = mockMvc.perform(post("/api/v1/prescriptions/" + finalizedPrescription.getId() + "/dispensing-orders")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        mockMvc.perform(post("/api/v1/dispensing-orders/" + orderId + "/cancel")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT)
                        .param("reason", "Purchased from nearby retail store"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("POST /prescriptions/{id}/dispensing-orders - Doctor cannot create dispensing order (403)")
    void createDispensingOrder_DoctorAttempt_Forbidden() throws Exception {
        CreateDispensingOrderRequestDto request = new CreateDispensingOrderRequestDto(null, null);

        mockMvc.perform(post("/api/v1/prescriptions/" + finalizedPrescription.getId() + "/dispensing-orders")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("POST /prescriptions/{id}/dispensing-orders - Hospital staff cannot create dispensing order (403)")
    void createDispensingOrder_HospitalStaffAttempt_Forbidden() throws Exception {
        CreateDispensingOrderRequestDto request = new CreateDispensingOrderRequestDto(null, null);

        mockMvc.perform(post("/api/v1/prescriptions/" + finalizedPrescription.getId() + "/dispensing-orders")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_STAFF)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("POST /dispensing-orders/{id}/cancel - Non-owning patient cannot cancel order (403)")
    void cancelDispensingOrder_NonOwningPatient_Forbidden() throws Exception {
        CreateDispensingOrderRequestDto request = new CreateDispensingOrderRequestDto("Pickup", null);

        String responseStr = mockMvc.perform(post("/api/v1/prescriptions/" + finalizedPrescription.getId() + "/dispensing-orders")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        mockMvc.perform(post("/api/v1/dispensing-orders/" + orderId + "/cancel")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_OTHER_PATIENT)
                        .param("reason", "Malicious cancellation attempt"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("POST /dispensing-orders/{id}/cancel - Cancelling already cancelled order fails with 409 Conflict")
    void cancelDispensingOrder_AlreadyCancelled_ThrowsConflict() throws Exception {
        CreateDispensingOrderRequestDto request = new CreateDispensingOrderRequestDto("Pickup", null);

        String responseStr = mockMvc.perform(post("/api/v1/prescriptions/" + finalizedPrescription.getId() + "/dispensing-orders")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String orderId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        // Cancel once (succeeds)
        mockMvc.perform(post("/api/v1/dispensing-orders/" + orderId + "/cancel")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT)
                        .param("reason", "First cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("CANCELLED")));

        // Cancel second time fails with 409 Conflict
        mockMvc.perform(post("/api/v1/dispensing-orders/" + orderId + "/cancel")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT)
                        .param("reason", "Second cancel"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")));
    }
}
