package com.medisimbio.backend.encounter.generator;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generator for human-readable clinical encounter references.
 * Format: ENC-YYYYMMDD-XXXX (e.g. ENC-20261008-7K2M)
 */
@Component
public class EncounterReferenceGenerator {

    private static final String ALPHANUMERIC = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final SecureRandom secureRandom = new SecureRandom();
    private final Clock clock;

    public EncounterReferenceGenerator() {
        this(Clock.systemDefaultZone());
    }

    public EncounterReferenceGenerator(Clock clock) {
        this.clock = clock;
    }

    public String generateReference() {
        return generateReference(LocalDate.now(clock));
    }

    public String generateReference(LocalDate date) {
        String datePart = (date != null ? date : LocalDate.now(clock)).format(DATE_FORMATTER);
        StringBuilder suffix = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            int index = secureRandom.nextInt(ALPHANUMERIC.length());
            suffix.append(ALPHANUMERIC.charAt(index));
        }
        return "ENC-" + datePart + "-" + suffix;
    }
}
