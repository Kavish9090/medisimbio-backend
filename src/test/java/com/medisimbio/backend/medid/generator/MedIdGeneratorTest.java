package com.medisimbio.backend.medid.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class MedIdGeneratorTest {

    private MedIdGenerator generator;
    private static final Pattern MED_ID_REGEX = Pattern.compile("^MD-[A-Z0-9]{4}-[A-Z0-9]{4}$");

    @BeforeEach
    void setUp() {
        generator = new MedIdGenerator();
    }

    @Test
    @DisplayName("Format: Generates valid Med-ID adhering to MD-XXXX-XXXX format")
    void generateMedIdNumber_shouldMatchPattern() {
        String medId = generator.generateMedIdNumber();
        assertThat(medId).isNotNull();
        assertThat(medId).matches(MED_ID_REGEX);
    }

    @Test
    @DisplayName("Randomness: Generates unique Med-IDs across 1000 consecutive invocations")
    void generateMedIdNumber_shouldProduceDistinctValues() {
        Set<String> generated = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String medId = generator.generateMedIdNumber();
            assertThat(generated.add(medId)).as("Duplicate Med-ID generated: " + medId).isTrue();
        }
    }

    @Test
    @DisplayName("QR Token: Generates structured QR verification token")
    void generateQrToken_shouldFormatCorrectly() {
        UUID patientId = UUID.randomUUID();
        String medId = "MD-S3AK-9Q8P";

        String qrToken = generator.generateQrToken(medId, patientId);

        assertThat(qrToken).isNotNull();
        assertThat(qrToken).startsWith("MEDID:MD-S3AK-9Q8P:" + patientId + ":");
    }
}
