package com.precious.user.utils;

import com.precious.user.model.User;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateTimeUtils {

    public static String getUserTime(Long utcTime, String zone) {
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(utcTime).atZone(ZoneId.of(zone));
        return zonedDateTime.toString();
    }
}
