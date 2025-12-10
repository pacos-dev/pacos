package org.pacos.base.utils;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TimeToStringUtilsTest {

    @Test
    void whenValidLocalDateTimeThenFormattedCorrectly() {
        LocalDateTime time = LocalDateTime.of(2024, 11, 12, 14, 30, 45);
        String formattedTime = TimeToStringUtils.format(time);
        assertEquals("14:30:45 12-11-2024", formattedTime);
    }

    @Test
    void whenMinimumLocalDateTimeThenFormattedCorrectly() {
        LocalDateTime time = LocalDateTime.of(0, 1, 1, 0, 0, 0);
        String formattedTime = TimeToStringUtils.format(time);
        assertEquals("00:00:00 01-01-0001", formattedTime);
    }

    @Test
    void whenMaximumLocalDateTimeThenFormattedCorrectly() {
        LocalDateTime time = LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        String formattedTime = TimeToStringUtils.format(time);
        assertEquals("23:59:59 31-12-9999", formattedTime);
    }

    @Test
    void whenFormatToFileNameThenNoSpace() {
        LocalDateTime time = LocalDateTime.of(0, 1, 1, 0, 0, 0);
        String formattedTime = TimeToStringUtils.formatToFileName(time);
        assertEquals("0001-01-01_00-00-00", formattedTime);
    }
}
