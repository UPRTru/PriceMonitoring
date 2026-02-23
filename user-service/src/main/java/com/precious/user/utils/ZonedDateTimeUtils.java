package com.precious.user.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

public final class ZonedDateTimeUtils {

    private ZonedDateTimeUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String getUserTime(Long utcTime, String zone) {
        Objects.requireNonNull(utcTime, "utcTime cannot be null");
        Objects.requireNonNull(zone, "zone cannot be null");
        if (zone.isBlank()) {
            throw new IllegalArgumentException("zone cannot be empty");
        }
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(utcTime).atZone(ZoneId.of(zone));
        return zonedDateTime.toString();
    }

    public static String getUserTimeFormatted(Long utcTime, String zone, String format) {
        Objects.requireNonNull(utcTime, "utcTime cannot be null");
        Objects.requireNonNull(zone, "zone cannot be null");
        Objects.requireNonNull(format, "format cannot be null");
        if (zone.isBlank()) {
            throw new IllegalArgumentException("zone cannot be empty");
        }
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(utcTime).atZone(ZoneId.of(zone));
        return zonedDateTime.format(java.time.format.DateTimeFormatter.ofPattern(format));
    }
}