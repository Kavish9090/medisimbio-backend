package com.medisimbio.backend.prescription.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisimbio.backend.appointment.entity.AppointmentEntity;
import com.medisimbio.backend.appointment.entity.AppointmentStatus;
import com.medisimbio.backend.appointment.repository.AppointmentRepository;
import com.medisimbio.backend.encounter.entity.EncounterEntity;
import com.medisimbio.backend.encounter.entity.EncounterStatus;
import com.medisimbio.backend.encounter.entity.EncounterType;
import com.medisimbio.backend.encounter.repository.EncounterRepository;
import com.medisimbio.backend.common.exception.ConflictException;
import com.medisimbio.backend.dispensing.entity.DispensingOrderEntity;
import com.medisimbio.backend.dispensing.entity.DispensingOrderStatus;
import com.medisimbio.backend.dispensing.repository.DispensingOrderRepository;
import com.medisimbio.backend.hospital.entity.HospitalEntity;
import com.medisimbio.backend.hospital.entity.HospitalStaffEntity;
import com.medisimbio.backend.hospital.entity.HospitalType;
import com.medisimbio.backend.hospital.repository.HospitalRepository;
import com.medisimbio.backend.hospital.repository.HospitalStaffRepository;
import com.medisimbio.backend.patient.entity.Gender;
import com.medisimbio.backend.patient.entity.PatientEntity;
import com.medisimbio.backend.patient.repository.PatientRepository;
import com.medisimbio.backend.prescription.dto.*;
import com.medisimbio.backend.prescription.entity.PrescriptionEntity;
import com.medisimbio.backend.prescription.entity.PrescriptionItemEntity;
import com.medisimbio.backend.prescription.entity.PrescriptionStatus;
import com.medisimbio.backend.prescription.repository.PrescriptionRepository;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private DispensingOrderRepository dispensingOrderRepository;

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
    private ProviderEntity otherProvider;
    private HospitalEntity testHospital;
    private AppointmentEntity testAppointment;
    private EncounterEntity testEncounter;
    private EncounterEntity cancelledEncounter;

    private static final String AUTH_DOCTOR = "auth_doc_rx_1";
    private static final String AUTH_OTHER_DOC = "auth_doc_rx_2";
    private static final String AUTH_PATIENT = "auth_pat_rx_1";
    private static final String AUTH_OTHER_PATIENT = "auth_pat_rx_2";
    private static final String AUTH_STAFF = "auth_staff_rx_1";

    @BeforeEach
    void setUp() {
        cleanDb();

        testPatient = patientRepository.save(new PatientEntity(
                UUID.randomUUID(), AUTH_PATIENT, "Rohan Verma", "rohan.rx@test.com",
                "+919876543111", LocalDate.of(1995, 2, 10), Gender.MALE
        ));

        otherPatient = patientRepository.save(new PatientEntity(
                UUID.randomUUID(), AUTH_OTHER_PATIENT, "Ananya Sen", "ananya.rx@test.com",
                "+919876543222", LocalDate.of(1998, 7, 22), Gender.FEMALE
        ));

        testHospital = hospitalRepository.save(new HospitalEntity(
                UUID.randomUUID(), "Apollo Rx Center", HospitalType.PRIVATE,
                "+911122334455", "5 Medical Way", "New Delhi", "Delhi", "110001"
        ));

        hospitalStaffRepository.save(new HospitalStaffEntity(
                UUID.randomUUID(), testHospital, AUTH_STAFF, "Nurse Joy", "NURSE", true
        ));

        testProvider = providerRepository.save(new ProviderEntity(
                UUID.randomUUID(), AUTH_DOCTOR, "Dr. Rajesh Koothrappali", "MCI-RX-001",
                "MBBS, MD Pharmacology", 12, "rajesh.rx@test.com", "+919811223344"
        ));

        otherProvider = providerRepository.save(new ProviderEntity(
                UUID.randomUUID(), AUTH_OTHER_DOC, "Dr. Leonard Hofstadter", "MCI-RX-002",
                "MBBS, MS", 10, "leonard.rx@test.com", "+919811223355"
        ));

        testAppointment = appointmentRepository.save(new AppointmentEntity(
                UUID.randomUUID(), testPatient, testProvider, testHospital, null,
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentStatus.CONFIRMED, "MSB-RX-APP-01", "General consultation"
        ));

        testEncounter = encounterRepository.save(new EncounterEntity(
                UUID.randomUUID(), "ENC-RX-001", testPatient, testProvider, testHospital, testAppointment,
                EncounterType.INITIAL_CONSULTATION, EncounterStatus.IN_PROGRESS, null,
                "Persistent cough and fever", AUTH_DOCTOR
        ));

        AppointmentEntity cancelledAppointment = appointmentRepository.save(new AppointmentEntity(
                UUID.randomUUID(), testPatient, testProvider, testHospital, null,
                LocalDate.now().plusDays(2), LocalTime.of(11, 0), LocalTime.of(11, 30),
                AppointmentStatus.CANCELLED, "MSB-RX-APP-02", "Cancelled check"
        ));

        cancelledEncounter = encounterRepository.save(new EncounterEntity(
                UUID.randomUUID(), "ENC-RX-CANCELLED", testPatient, testProvider, testHospital, cancelledAppointment,
                EncounterType.INITIAL_CONSULTATION, EncounterStatus.CANCELLED, null,
                "Cancelled visit", AUTH_DOCTOR
        ));
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

    private PrescriptionItemRequestDto createSampleItem(String name, int qty) {
        return new PrescriptionItemRequestDto(
                name, "Amoxicillin", "500 mg", "CAPSULE", "1 capsule",
                "ORAL", "THRICE_DAILY", 5, "DAYS", qty, "CAPSULES", 0,
                "Take after meals", 1
        );
    }

    @Test
    @DisplayName("POST /encounters/{id}/prescriptions - Creates DRAFT prescription successfully")
    void createDraftPrescription_Success() throws Exception {
        CreatePrescriptionRequestDto request = new CreatePrescriptionRequestDto(
                LocalDate.now().plusMonths(1),
                "Complete full antibiotics course",
                List.of(createSampleItem("Amoxil 500mg", 15)),
                false
        );

        mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("DRAFT")))
                .andExpect(jsonPath("$.data.prescriptionReference", startsWith("RX-")))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].medicineName", is("Amoxil 500mg")))
                .andExpect(jsonPath("$.data.finalizedAt").doesNotExist());
    }

    @Test
    @DisplayName("POST /encounters/{id}/prescriptions - Creates FINALIZED prescription directly")
    void createFinalizedPrescription_Success() throws Exception {
        CreatePrescriptionRequestDto request = new CreatePrescriptionRequestDto(
                LocalDate.now().plusMonths(1),
                "Follow up if fever persists",
                List.of(createSampleItem("Augmentin 625mg", 10)),
                true
        );

        mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("FINALIZED")))
                .andExpect(jsonPath("$.data.finalizedAt", notNullValue()));
    }

    @Test
    @DisplayName("POST /encounters/{id}/prescriptions - Cannot create prescription on CANCELLED encounter (400)")
    void createPrescription_CancelledEncounter_Fails() throws Exception {
        CreatePrescriptionRequestDto request = new CreatePrescriptionRequestDto(
                LocalDate.now().plusDays(10), null, List.of(createSampleItem("Paracetamol 650", 10)), false
        );

        mockMvc.perform(post("/api/v1/encounters/" + cancelledEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("BAD_REQUEST")));
    }

    @Test
    @DisplayName("POST /encounters/{id}/prescriptions - Patient cannot author prescription (403)")
    void createPrescription_PatientAttempt_Forbidden() throws Exception {
        CreatePrescriptionRequestDto request = new CreatePrescriptionRequestDto(
                LocalDate.now().plusDays(10), null, List.of(createSampleItem("Paracetamol 650", 10)), false
        );

        mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("POST /encounters/{id}/prescriptions - Unassigned provider cannot author prescription (403)")
    void createPrescription_UnassignedProvider_Forbidden() throws Exception {
        CreatePrescriptionRequestDto request = new CreatePrescriptionRequestDto(
                LocalDate.now().plusDays(10), null, List.of(createSampleItem("Paracetamol 650", 10)), false
        );

        mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_OTHER_DOC)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("POST /encounters/{id}/prescriptions - Empty items fails validation (400)")
    void createPrescription_EmptyItems_Fails() throws Exception {
        CreatePrescriptionRequestDto request = new CreatePrescriptionRequestDto(
                LocalDate.now().plusDays(10), null, List.of(), false
        );

        mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /prescriptions/{id} - Patient cannot view DRAFT prescription (403)")
    void getPrescription_DraftByPatient_Forbidden() throws Exception {
        CreatePrescriptionRequestDto request = new CreatePrescriptionRequestDto(
                null, null, List.of(createSampleItem("Amoxil", 10)), false
        );

        String responseStr = mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String prescriptionId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/prescriptions/" + prescriptionId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("FORBIDDEN")));
    }

    @Test
    @DisplayName("GET /prescriptions/{id} - Patient can view FINALIZED prescription (200)")
    void getPrescription_FinalizedByPatient_Success() throws Exception {
        CreatePrescriptionRequestDto request = new CreatePrescriptionRequestDto(
                null, "Take with water", List.of(createSampleItem("Azithromycin", 6)), true
        );

        String responseStr = mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String prescriptionId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/prescriptions/" + prescriptionId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("FINALIZED")))
                .andExpect(jsonPath("$.data.items", hasSize(1)));
    }

    @Test
    @DisplayName("GET /prescriptions/{id} - IDOR Protection: Patient B cannot view Patient A prescription (403)")
    void getPrescription_IdorCrossPatient_Forbidden() throws Exception {
        CreatePrescriptionRequestDto request = new CreatePrescriptionRequestDto(
                null, null, List.of(createSampleItem("Azithromycin", 6)), true
        );

        String responseStr = mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String prescriptionId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        mockMvc.perform(get("/api/v1/prescriptions/" + prescriptionId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_OTHER_PATIENT))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /prescriptions/{id} - Updates draft prescription successfully")
    void updateDraftPrescription_Success() throws Exception {
        CreatePrescriptionRequestDto createReq = new CreatePrescriptionRequestDto(
                null, "Initial draft notes", List.of(createSampleItem("Amoxil", 10)), false
        );

        String responseStr = mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String prescriptionId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        UpdatePrescriptionRequestDto updateReq = new UpdatePrescriptionRequestDto(
                LocalDate.now().plusWeeks(2),
                "Updated instructions: Take after breakfast and dinner",
                List.of(createSampleItem("Amoxil 500mg Forte", 14))
        );

        mockMvc.perform(patch("/api/v1/prescriptions/" + prescriptionId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.clinicalInstructions", containsString("Updated instructions")))
                .andExpect(jsonPath("$.data.items[0].medicineName", is("Amoxil 500mg Forte")));
    }

    @Test
    @DisplayName("PATCH /prescriptions/{id} - Modifying FINALIZED prescription throws 409 Conflict")
    void updateFinalizedPrescription_ThrowsConflict() throws Exception {
        CreatePrescriptionRequestDto createReq = new CreatePrescriptionRequestDto(
                null, null, List.of(createSampleItem("Amoxil", 10)), true
        );

        String responseStr = mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String prescriptionId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        UpdatePrescriptionRequestDto updateReq = new UpdatePrescriptionRequestDto(
                null, "Attempted illegal modification", null
        );

        mockMvc.perform(patch("/api/v1/prescriptions/" + prescriptionId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")));
    }

    @Test
    @DisplayName("POST /prescriptions/{id}/finalize - Finalizes DRAFT prescription")
    void finalizePrescription_Success() throws Exception {
        CreatePrescriptionRequestDto createReq = new CreatePrescriptionRequestDto(
                null, "Draft to finalize", List.of(createSampleItem("Paracetamol", 20)), false
        );

        String responseStr = mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String prescriptionId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        mockMvc.perform(post("/api/v1/prescriptions/" + prescriptionId + "/finalize")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("FINALIZED")))
                .andExpect(jsonPath("$.data.finalizedAt", notNullValue()));
    }

    @Test
    @DisplayName("POST /prescriptions/{id}/cancel - Cancels prescription with mandatory reason")
    void cancelPrescription_Success() throws Exception {
        CreatePrescriptionRequestDto createReq = new CreatePrescriptionRequestDto(
                null, null, List.of(createSampleItem("Cough Syrup", 1)), true
        );

        String responseStr = mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String prescriptionId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        CancelPrescriptionRequestDto cancelReq = new CancelPrescriptionRequestDto("Patient reported drug allergy");

        mockMvc.perform(post("/api/v1/prescriptions/" + prescriptionId + "/cancel")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("CANCELLED")))
                .andExpect(jsonPath("$.data.cancellationReason", is("Patient reported drug allergy")));
    }

    @Test
    @DisplayName("POST /prescriptions/{id}/replacements - Atomically supersedes original and creates version 2")
    void replacePrescription_Success() throws Exception {
        CreatePrescriptionRequestDto createReq = new CreatePrescriptionRequestDto(
                null, "Initial course", List.of(createSampleItem("Medication A", 10)), true
        );

        String responseStr = mockMvc.perform(post("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String originalId = objectMapper.readTree(responseStr).path("data").path("id").asText();

        ReplacePrescriptionRequestDto replaceReq = new ReplacePrescriptionRequestDto(
                "Upgraded dosage due to ongoing symptoms",
                LocalDate.now().plusMonths(1),
                "Take higher strength medication",
                List.of(createSampleItem("Medication A Forte", 15)),
                true
        );

        mockMvc.perform(post("/api/v1/prescriptions/" + originalId + "/replacements")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replaceReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status", is("FINALIZED")))
                .andExpect(jsonPath("$.data.versionNumber", is(2)))
                .andExpect(jsonPath("$.data.replacesPrescriptionId", is(originalId)));

        // Original prescription is now SUPERSEDED
        mockMvc.perform(get("/api/v1/prescriptions/" + originalId)
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status", is("SUPERSEDED")));

        // Attempting to replace the same prescription again fails with 409 Conflict
        mockMvc.perform(post("/api/v1/prescriptions/" + originalId + "/replacements")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replaceReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")));
    }

    @Test
    @DisplayName("GET /encounters/{id}/prescriptions - Patient sees only finalized/superseded/cancelled without null entries")
    void getPrescriptionsByEncounter_PatientSeesOnlyFinalizedWithoutNullEntries() throws Exception {
        // 1. Create a DRAFT prescription
        PrescriptionEntity draftRx = new PrescriptionEntity(
                UUID.randomUUID(), "RX-TEST-DRAFT", testPatient, testEncounter, testProvider, testHospital,
                PrescriptionStatus.DRAFT, Instant.now(), LocalDate.now().plusMonths(1), "Draft note", 1, null, AUTH_DOCTOR
        );
        draftRx.addItem(new PrescriptionItemEntity(
                UUID.randomUUID(), draftRx, "Draft Med", "Active", "10mg", "TAB", "1", "ORAL", "OD", 5, "DAYS", 5, "TABS", 0, "Note", 1
        ));
        prescriptionRepository.save(draftRx);

        // 2. Create a FINALIZED prescription
        PrescriptionEntity finalRx = new PrescriptionEntity(
                UUID.randomUUID(), "RX-TEST-FINAL", testPatient, testEncounter, testProvider, testHospital,
                PrescriptionStatus.DRAFT, Instant.now(), LocalDate.now().plusMonths(1), "Final note", 1, null, AUTH_DOCTOR
        );
        finalRx.addItem(new PrescriptionItemEntity(
                UUID.randomUUID(), finalRx, "Final Med", "Active", "20mg", "TAB", "1", "ORAL", "OD", 10, "DAYS", 10, "TABS", 0, "Note", 1
        ));
        finalRx.setStatus(PrescriptionStatus.FINALIZED);
        finalRx.setFinalizedAt(Instant.now());
        prescriptionRepository.save(finalRx);

        // Patient view: exactly 1 element returned, totalElements is 1, content does not contain null
        mockMvc.perform(get("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_PATIENT)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(1)))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].prescriptionReference", is("RX-TEST-FINAL")))
                .andExpect(jsonPath("$.data.content[0].status", is("FINALIZED")));

        // Doctor view: both 2 prescriptions returned
        mockMvc.perform(get("/api/v1/encounters/" + testEncounter.getId() + "/prescriptions")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements", is(2)))
                .andExpect(jsonPath("$.data.content", hasSize(2)));
    }

    @Test
    @DisplayName("POST /prescriptions/{id}/cancel - Blocked with 409 Conflict when active dispensing order exists")
    void cancelPrescription_BlockedWhenActiveDispensingOrderExists() throws Exception {
        // 1. Create finalized prescription
        PrescriptionEntity finalRx = new PrescriptionEntity(
                UUID.randomUUID(), "RX-TEST-CANCEL-BLOCK", testPatient, testEncounter, testProvider, testHospital,
                PrescriptionStatus.DRAFT, Instant.now(), LocalDate.now().plusMonths(1), "Final note", 1, null, AUTH_DOCTOR
        );
        PrescriptionItemEntity item = new PrescriptionItemEntity(
                UUID.randomUUID(), finalRx, "Final Med", "Active", "20mg", "TAB", "1", "ORAL", "OD", 10, "DAYS", 10, "TABS", 0, "Note", 1
        );
        finalRx.addItem(item);
        finalRx.setStatus(PrescriptionStatus.FINALIZED);
        finalRx.setFinalizedAt(Instant.now());
        finalRx = prescriptionRepository.save(finalRx);

        // 2. Attach an active (PENDING) dispensing order
        DispensingOrderEntity order = new DispensingOrderEntity(
                UUID.randomUUID(), "DSP-TEST-ACTIVE-1", finalRx, testPatient, testHospital,
                DispensingOrderStatus.PENDING, Instant.now(), "Pending pickup", AUTH_PATIENT
        );
        dispensingOrderRepository.save(order);

        // 3. Attempt to cancel prescription fails with 409 Conflict
        CancelPrescriptionRequestDto cancelReq = new CancelPrescriptionRequestDto("Doctor changed mind");
        mockMvc.perform(post("/api/v1/prescriptions/" + finalRx.getId() + "/cancel")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")))
                .andExpect(jsonPath("$.message", containsString("active dispensing order")));
    }

    @Test
    @DisplayName("POST /prescriptions/{id}/replacements - Blocked with 409 Conflict when active dispensing order exists")
    void replacePrescription_BlockedWhenActiveDispensingOrderExists() throws Exception {
        // 1. Create finalized prescription
        PrescriptionEntity finalRx = new PrescriptionEntity(
                UUID.randomUUID(), "RX-TEST-REPLACE-BLOCK", testPatient, testEncounter, testProvider, testHospital,
                PrescriptionStatus.DRAFT, Instant.now(), LocalDate.now().plusMonths(1), "Final note", 1, null, AUTH_DOCTOR
        );
        PrescriptionItemEntity item = new PrescriptionItemEntity(
                UUID.randomUUID(), finalRx, "Final Med", "Active", "20mg", "TAB", "1", "ORAL", "OD", 10, "DAYS", 10, "TABS", 0, "Note", 1
        );
        finalRx.addItem(item);
        finalRx.setStatus(PrescriptionStatus.FINALIZED);
        finalRx.setFinalizedAt(Instant.now());
        finalRx = prescriptionRepository.save(finalRx);

        // 2. Attach an active (PENDING) dispensing order
        DispensingOrderEntity order = new DispensingOrderEntity(
                UUID.randomUUID(), "DSP-TEST-ACTIVE-2", finalRx, testPatient, testHospital,
                DispensingOrderStatus.PENDING, Instant.now(), "Pending pickup", AUTH_PATIENT
        );
        dispensingOrderRepository.save(order);

        // 3. Attempt to replace prescription fails with 409 Conflict
        ReplacePrescriptionRequestDto replaceReq = new ReplacePrescriptionRequestDto(
                "Upgraded dose", LocalDate.now().plusMonths(1), "New instructions",
                List.of(createSampleItem("Higher Dose Med", 20)), true
        );
        mockMvc.perform(post("/api/v1/prescriptions/" + finalRx.getId() + "/replacements")
                        .header(DevAuthenticationFilter.HEADER_USER_ID, AUTH_DOCTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replaceReq)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")))
                .andExpect(jsonPath("$.message", containsString("active dispensing order")));
    }

    @Test
    @DisplayName("Entity Immutability: Prescription items cannot be added, removed, or cleared on non-DRAFT prescription")
    void prescriptionEntity_ItemMutationGuardedWhenNotDraft() {
        PrescriptionEntity finalRx = new PrescriptionEntity(
                UUID.randomUUID(), "RX-TEST-IMMUTABLE", testPatient, testEncounter, testProvider, testHospital,
                PrescriptionStatus.DRAFT, Instant.now(), LocalDate.now().plusMonths(1), "Note", 1, null, AUTH_DOCTOR
        );
        PrescriptionItemEntity item1 = new PrescriptionItemEntity(
                UUID.randomUUID(), finalRx, "Med 1", "Act 1", "10mg", "TAB", "1", "ORAL", "OD", 5, "DAYS", 5, "TABS", 0, "Note", 1
        );
        finalRx.addItem(item1);
        finalRx.setStatus(PrescriptionStatus.FINALIZED);

        PrescriptionItemEntity item2 = new PrescriptionItemEntity(
                UUID.randomUUID(), finalRx, "Med 2", "Act 2", "20mg", "TAB", "1", "ORAL", "OD", 5, "DAYS", 5, "TABS", 0, "Note", 2
        );

        assertThrows(ConflictException.class, () -> finalRx.addItem(item2));
        assertThrows(ConflictException.class, () -> finalRx.removeItem(item1));
        assertThrows(ConflictException.class, finalRx::clearItems);
    }
}
