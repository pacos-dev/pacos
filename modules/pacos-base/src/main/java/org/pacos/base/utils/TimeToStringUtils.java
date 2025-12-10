package org.pacos.base.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Helper class that allows to format time to string in one specific format for the whole system
 */
public class TimeToStringUtils {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
    private static final DateTimeFormatter formatterFileName = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private TimeToStringUtils() {
    }

    public static String format(LocalDateTime time) {
        return formatter.format(time);
    }

    public static String formatToFileName(LocalDateTime time) {
        return formatterFileName.format(time);
    }

}
