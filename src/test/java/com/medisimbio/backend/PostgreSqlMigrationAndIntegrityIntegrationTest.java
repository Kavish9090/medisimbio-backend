package com.medisimbio.backend;

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
import com.medisimbio.backend.prescription.entity.PrescriptionEntity;
import com.medisimbio.backend.prescription.entity.PrescriptionItemEntity;
import com.medisimbio.backend.prescription.entity.PrescriptionStatus;
import com.medisimbio.backend.prescription.repository.PrescriptionRepository;
import com.medisimbio.backend.provider.entity.ProviderEntity;
import com.medisimbio.backend.provider.repository.ProviderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
@EnabledIf("isPostgresAvailable")
class PostgreSqlMigrationAndIntegrityIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private EncounterRepository encounterRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    private PatientEntity patient;
    private HospitalEntity hospital;
    private ProviderEntity provider;
    private AppointmentEntity appointment;
    private EncounterEntity encounter;

    static boolean isPostgresAvailable() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("localhost", 5433), 1500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        cleanDb();

        patient = patientRepository.save(new PatientEntity(
                UUID.randomUUID(), "auth_patient_pg", "Postgres Patient",
                "patient.pg@test.com", "+919876543111", LocalDate.of(1990, 1, 1), Gender.MALE
        ));

        hospital = hospitalRepository.save(new HospitalEntity(
                UUID.randomUUID(), "PG General Hospital", HospitalType.GOVERNMENT,
                "+911122338800", "1st PG Street", "Mumbai", "Maharashtra", "400001"
        ));

        provider = providerRepository.save(new ProviderEntity(
                UUID.randomUUID(), "auth_doc_pg", "Dr. Postgres Doctor",
                "MCI-PG-01", "MBBS, MD", 10, "doc.pg@test.com", "+919811001122"
        ));

        appointment = appointmentRepository.save(new AppointmentEntity(
                UUID.randomUUID(), patient, provider, hospital, null,
                LocalDate.now().plusDays(1), LocalTime.of(11, 0), LocalTime.of(11, 30),
                AppointmentStatus.CONFIRMED, "MSB-PG-001", "PostgreSQL Consultation"
        ));

        encounter = encounterRepository.save(new EncounterEntity(
                UUID.randomUUID(), "ENC-PG-001", patient, provider, hospital, appointment,
                EncounterType.INITIAL_CONSULTATION, EncounterStatus.IN_PROGRESS, null,
                "Chest congestion", "auth_doc_pg"
        ));
    }

    @AfterEach
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
    @DisplayName("Verify PostgreSQL 16 Flyway migrations through V9 executed successfully")
    void flywayMigrations_AllAppliedSuccessfully() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank"
        );

        assertFalse(rows.isEmpty(), "flyway_schema_history should not be empty");
        boolean v9Found = false;
        for (Map<String, Object> row : rows) {
            String version = (String) row.get("version");
            Boolean success = (Boolean) row.get("success");
            assertTrue(Boolean.TRUE.equals(success), "Migration V" + version + " must be successful");
            if ("9".equals(version)) {
                v9Found = true;
            }
        }
        assertTrue(v9Found, "Migration V9 must be applied to PostgreSQL 16");
    }

    @Test
    @DisplayName("Verify PostgreSQL 16 ON DELETE RESTRICT on prescription items")
    void prescriptionItems_DeleteRestrictEnforcedOnPostgres() {
        PrescriptionEntity prescription = new PrescriptionEntity(
                UUID.randomUUID(), "RX-PG-DEL-01", patient, encounter, provider, hospital,
                PrescriptionStatus.DRAFT, Instant.now(), LocalDate.now().plusDays(30),
                "Instructions", 1, null, "auth_doc_pg"
        );
        PrescriptionItemEntity item = new PrescriptionItemEntity(
                UUID.randomUUID(), prescription, "Amoxicillin", "Amoxicillin Trihydrate",
                "500mg", "CAPSULE", "500mg", "ORAL", "TID",
                5, "DAYS", 15, "CAPSULES", 0, "After meals", 1
        );
        prescription.addItem(item);
        prescription.setStatus(PrescriptionStatus.FINALIZED);
        prescription.setFinalizedAt(Instant.now());
        PrescriptionEntity saved = prescriptionRepository.saveAndFlush(prescription);

        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update("DELETE FROM prescriptions WHERE id = ?", saved.getId());
        });
    }

    @Test
    @DisplayName("Verify PostgreSQL 16 partial unique index prevents duplicate active dispensing orders")
    void dispensingOrders_PartialUniqueIndexEnforcedOnPostgres() {
        PrescriptionEntity prescription = new PrescriptionEntity(
                UUID.randomUUID(), "RX-PG-ORD-01", patient, encounter, provider, hospital,
                PrescriptionStatus.DRAFT, Instant.now(), LocalDate.now().plusDays(30),
                "Instructions", 1, null, "auth_doc_pg"
        );
        prescription.setStatus(PrescriptionStatus.FINALIZED);
        prescription.setFinalizedAt(Instant.now());
        PrescriptionEntity saved = prescriptionRepository.saveAndFlush(prescription);

        UUID order1Id = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO dispensing_orders (id, dispensing_reference, prescription_id, patient_id, hospital_id, status, ordered_at, created_by)
            VALUES (?, 'ORD-PG-001', ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP, 'auth_patient_pg')
        """, order1Id, saved.getId(), patient.getId(), hospital.getId());

        // Second PENDING order for the same prescription must fail due to partial unique index
        UUID order2Id = UUID.randomUUID();
        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update("""
                INSERT INTO dispensing_orders (id, dispensing_reference, prescription_id, patient_id, hospital_id, status, ordered_at, created_by)
                VALUES (?, 'ORD-PG-002', ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP, 'auth_patient_pg')
            """, order2Id, saved.getId(), patient.getId(), hospital.getId());
        });

        // Cancel order 1 -> now a new active order can be placed
        jdbcTemplate.update("UPDATE dispensing_orders SET status = 'CANCELLED' WHERE id = ?", order1Id);

        int inserted = jdbcTemplate.update("""
            INSERT INTO dispensing_orders (id, dispensing_reference, prescription_id, patient_id, hospital_id, status, ordered_at, created_by)
            VALUES (?, 'ORD-PG-003', ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP, 'auth_patient_pg')
        """, order2Id, saved.getId(), patient.getId(), hospital.getId());

        assertEquals(1, inserted);
    }

    @Test
    @DisplayName("Verify PostgreSQL 16 check constraint on dispensed_quantity bounds")
    void dispensingOrderItems_DispensedQuantityBoundsEnforcedOnPostgres() {
        PrescriptionEntity prescription = new PrescriptionEntity(
                UUID.randomUUID(), "RX-PG-QTY-01", patient, encounter, provider, hospital,
                PrescriptionStatus.DRAFT, Instant.now(), LocalDate.now().plusDays(30),
                "Instructions", 1, null, "auth_doc_pg"
        );
        PrescriptionItemEntity presItem = new PrescriptionItemEntity(
                UUID.randomUUID(), prescription, "Amoxicillin", "Amoxicillin Trihydrate",
                "500mg", "CAPSULE", "500mg", "ORAL", "TID",
                5, "DAYS", 15, "CAPSULES", 0, "After meals", 1
        );
        prescription.addItem(presItem);
        prescription.setStatus(PrescriptionStatus.FINALIZED);
        prescription.setFinalizedAt(Instant.now());
        PrescriptionEntity saved = prescriptionRepository.saveAndFlush(prescription);
        UUID presItemId = saved.getItems().get(0).getId();

        UUID orderId = UUID.randomUUID();
        jdbcTemplate.update("""
            INSERT INTO dispensing_orders (id, dispensing_reference, prescription_id, patient_id, hospital_id, status, ordered_at, created_by)
            VALUES (?, 'ORD-PG-QTY-01', ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP, 'auth_patient_pg')
        """, orderId, saved.getId(), patient.getId(), hospital.getId());

        // Valid dispensed_quantity (0 <= 5 <= 10)
        UUID item1Id = UUID.randomUUID();
        int valid = jdbcTemplate.update("""
            INSERT INTO dispensing_order_items (id, dispensing_order_id, prescription_item_id, medicine_name, requested_quantity, dispensed_quantity)
            VALUES (?, ?, ?, 'Amoxicillin', 10, 5)
        """, item1Id, orderId, presItemId);
        assertEquals(1, valid);

        // Negative dispensed_quantity (< 0) -> check violation
        UUID item2Id = UUID.randomUUID();
        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update("""
                INSERT INTO dispensing_order_items (id, dispensing_order_id, prescription_item_id, medicine_name, requested_quantity, dispensed_quantity)
                VALUES (?, ?, ?, 'Amoxicillin', 10, -1)
            """, item2Id, orderId, presItemId);
        });

        // Exceeded dispensed_quantity (> requested_quantity) -> check violation
        UUID item3Id = UUID.randomUUID();
        assertThrows(DataIntegrityViolationException.class, () -> {
            jdbcTemplate.update("""
                INSERT INTO dispensing_order_items (id, dispensing_order_id, prescription_item_id, medicine_name, requested_quantity, dispensed_quantity)
                VALUES (?, ?, ?, 'Amoxicillin', 10, 15)
            """, item3Id, orderId, presItemId);
        });
    }
}
