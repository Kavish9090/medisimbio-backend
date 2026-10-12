package com.medisimbio.backend.medid.generator;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * Authoritative Generator for MediSimbio Health IDs and QR tokens.
 * Generates unique Med-IDs conforming to pattern: MD-XXXX-XXXX
 */
@Component
public class MedIdGenerator {

    private static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // Base32 unambiguous charset
    private static final int SEGMENT_LENGTH = 4;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a unique Med-ID string in the format MD-XXXX-XXXX.
     *
     * @return Generated Med-ID string
     */
    public String generateMedIdNumber() {
        StringBuilder sb = new StringBuilder("MD-");
        appendRandomSegment(sb, SEGMENT_LENGTH);
        sb.append("-");
        appendRandomSegment(sb, SEGMENT_LENGTH);
        return sb.toString();
    }

    /**
     * Generates a tamper-resistant QR code token for a patient and Med-ID.
     *
     * @param medIdNumber The Med-ID
     * @param patientId   The internal patient UUID
     * @return Formatted QR token payload
     */
    public String generateQrToken(String medIdNumber, UUID patientId) {
        long epochSecond = Instant.now().getEpochSecond();
        return String.format("MEDID:%s:%s:%d", medIdNumber, patientId, epochSecond);
    }

    private void appendRandomSegment(StringBuilder sb, int length) {
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }
    }
}
