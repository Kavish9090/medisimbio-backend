package com.medisimbio.backend.appointment.generator;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates unique human-readable booking references for appointments.
 * Format: MSB-YYYYMMDD-XXXX (e.g. MSB-20261015-8H2K)
 */
@Component
public class BookingReferenceGenerator {

    private static final String ALPHANUMERIC_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // excludes ambiguous 0,1,I,O
    private static final int RANDOM_PART_LENGTH = 4;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateReference(LocalDate appointmentDate) {
        LocalDate date = appointmentDate != null ? appointmentDate : LocalDate.now();
        String datePart = date.format(DATE_FORMATTER);

        StringBuilder randomPart = new StringBuilder(RANDOM_PART_LENGTH);
        for (int i = 0; i < RANDOM_PART_LENGTH; i++) {
            int index = RANDOM.nextInt(ALPHANUMERIC_CHARS.length());
            randomPart.append(ALPHANUMERIC_CHARS.charAt(index));
        }

        return "MSB-" + datePart + "-" + randomPart;
    }
}
