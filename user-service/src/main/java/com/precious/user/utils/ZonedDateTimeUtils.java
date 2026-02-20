package com.precious.user.utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class ZonedDateTimeUtils {

    private ZonedDateTimeUtils() {
    }

    public static String getUserTime(Long utcTime, String zone) {
        if (utcTime == null || zone == null || zone.isBlank()) {
            throw new IllegalArgumentException("utcTime and zone cannot be null");
        }
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(utcTime).atZone(ZoneId.of(zone));
        return zonedDateTime.toString();
    }
}
