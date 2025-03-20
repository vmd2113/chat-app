package com.duongw.chatapp.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtils {

    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

    public static String formatInstant(Instant instant, String pattern) {
        return DateTimeFormatter.ofPattern(pattern)
                .withZone(DEFAULT_ZONE_ID)
                .format(instant);
    }

    public static LocalDate instantToLocalDate(Instant instant) {
        return instant != null ? instant.atZone(DEFAULT_ZONE_ID).toLocalDate() : null;
    }

    public static LocalDateTime instantToLocalDateTime(Instant instant) {
        return instant != null ? LocalDateTime.ofInstant(instant, DEFAULT_ZONE_ID) : null;
    }

    public static Instant localDateTimeToInstant(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atZone(DEFAULT_ZONE_ID).toInstant() : null;
    }

    public static Instant localDateToInstant(LocalDate localDate) {
        return localDate != null ? localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant() : null;
    }
}