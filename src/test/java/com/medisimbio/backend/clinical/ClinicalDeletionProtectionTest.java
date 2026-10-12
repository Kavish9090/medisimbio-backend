package com.medisimbio.backend.clinical;

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
import com.medisimbio.backend.vitals.entity.VitalsEntity;
import com.medisimbio.backend.vitals.repository.VitalsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies ON DELETE RESTRICT data-integrity guarantees for clinical history.
 * Ensures diagnoses, vitals, and encounters cannot be accidentally deleted via cascade.
 */
@SpringBootTest
@ActiveProfiles("test")
public class ClinicalDeletionProtectionTest {

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    private VitalsRepository vitalsRepository;

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

    private PatientEntity patient;
    private ProviderEntity provider;
    private HospitalEntity hospital;
    private AppointmentEntity appointment;
    private EncounterEntity encounter;

    @BeforeEach
    void setUp() {
        cleanDb();

        patient = patientRepository.save(new PatientEntity(
                UUID.randomUUID(), "auth_patient_protect", "Meera Desai",
                "meera.protect@test.com", "+919876543999", LocalDate.of(1985, 8, 12), Gender.FEMALE
        ));

        hospital = hospitalRepository.save(new HospitalEntity(
                UUID.randomUUID(), "Integrity Hospital", HospitalType.GOVERNMENT,
                "+911122339900", "1st Health Street", "Delhi", "Delhi", "110001"
        ));

        provider = providerRepository.save(new ProviderEntity(
                UUID.randomUUID(), "auth_doc_protect", "Dr. Sanjeev Kapoor",
                "MCI-PROT-01", "MBBS, MD", 14, "sanjeev.prot@test.com", "+919811002233"
        ));

        appointment = appointmentRepository.save(new AppointmentEntity(
                UUID.randomUUID(), patient, provider, hospital, null,
                LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(10, 30),
                AppointmentStatus.CONFIRMED, "MSB-PROT-001", "Routine Consultation"
        ));

        encounter = encounterRepository.save(new EncounterEntity(
                UUID.randomUUID(), "ENC-PROT-001", patient, provider, hospital, appointment,
                EncounterType.INITIAL_CONSULTATION, EncounterStatus.IN_PROGRESS, null,
                "Chest congestion", "auth_doc_protect"
        ));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        cleanDb();
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
    @DisplayName("Deleting encounter with attached diagnoses throws DataIntegrityViolationException (RESTRICT)")
    void encounterWithDiagnoses_CannotBeDeleted() {
        DiagnosisEntity diagnosis = new DiagnosisEntity(
                UUID.randomUUID(), encounter, patient, "J20.9", "Acute Bronchitis",
                "Bronchial inflammation", DiagnosisType.PRIMARY, DiagnosisStatus.ACTIVE,
                null, "Initial diagnosis", "auth_doc_protect"
        );
        diagnosisRepository.save(diagnosis);

        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update("DELETE FROM encounters WHERE id = ?", encounter.getId());
        });
    }

    @Test
    @DisplayName("Deleting encounter with attached vitals throws DataIntegrityViolationException (RESTRICT)")
    void encounterWithVitals_CannotBeDeleted() {
        VitalsEntity vitals = new VitalsEntity(
                UUID.randomUUID(), encounter, patient, Instant.now(),
                new BigDecimal("98.6"), 72, 16, 120, 80,
                new BigDecimal("99.0"), new BigDecimal("70.0"), new BigDecimal("175.0"),
                new BigDecimal("22.9"), new BigDecimal("95.0"), "Baseline", "auth_doc_protect"
        );
        vitalsRepository.save(vitals);

        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update("DELETE FROM encounters WHERE id = ?", encounter.getId());
        });
    }

    @Test
    @DisplayName("Deleting patient with clinical history throws DataIntegrityViolationException (RESTRICT)")
    void patientWithClinicalRecords_CannotBeDeleted() {
        DiagnosisEntity diagnosis = new DiagnosisEntity(
                UUID.randomUUID(), encounter, patient, "I10", "Essential Hypertension",
                "High BP", DiagnosisType.PRIMARY, DiagnosisStatus.ACTIVE,
                null, "Hypertension diagnosis", "auth_doc_protect"
        );
        diagnosisRepository.save(diagnosis);

        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update("DELETE FROM patients WHERE id = ?", patient.getId());
        });
    }

    @Test
    @DisplayName("Deleting prescription with attached prescription items throws DataIntegrityViolationException (RESTRICT)")
    void prescriptionWithItems_CannotBeDeleted() {
        UUID prescriptionId = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO prescriptions (id, prescription_reference, patient_id, encounter_id, provider_id, hospital_id, status, prescribed_at, version_number, created_by)
            VALUES (?, 'RX-DEL-TEST', ?, ?, ?, ?, 'FINALIZED', CURRENT_TIMESTAMP, 1, 'auth_doc_protect')
        """, prescriptionId, patient.getId(), encounter.getId(), provider.getId(), hospital.getId());

        jdbcTemplate.update("""
            INSERT INTO prescription_items (id, prescription_id, medicine_name, dose, route, frequency, duration_value, quantity, sequence_number)
            VALUES (?, ?, 'Amoxicillin', '500mg', 'ORAL', 'TID', 5, 15, 1)
        """, UUID.randomUUID(), prescriptionId);

        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update("DELETE FROM prescriptions WHERE id = ?", prescriptionId);
        });
    }
}
