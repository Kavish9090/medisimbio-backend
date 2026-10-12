package com.medisimbio.backend.common.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Utility methods for ISO-8601 and UTC date-time manipulation.
 */
public final class DateTimeUtil {

    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");
    public static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private DateTimeUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Formats an Instant into standard ISO-8601 UTC string.
     */
    public static String formatIsoUtc(Instant instant) {
        if (instant == null) {
            return null;
        }
        return ISO_FORMATTER.format(instant);
    }

    /**
     * Parses an ISO-8601 string into an Instant.
     */
    public static Instant parseIsoUtc(String isoString) {
        if (isoString == null || isoString.isBlank()) {
            return null;
        }
        return Instant.parse(isoString);
    }
}
