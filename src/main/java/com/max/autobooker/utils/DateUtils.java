package com.max.autobooker.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

/**
 * @author Maxime Rocchia
 */
public class DateUtils {
    public static String getFormattedDate(LocalDate date) {
        int dayOfMonth = date.getDayOfMonth();
        return dayOfMonth
                + getDayOfMonthSuffix(dayOfMonth)
                + " "
                + DateTimeFormatter.ofPattern("MMM yyyy").format(date);
    }

    private static String getDayOfMonthSuffix(final int n) {
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }


    public static LocalDate getMondayOfSameWeek(LocalDate date) {
        return date.with(WeekFields.of(Locale.FRANCE).dayOfWeek(), 1L);
    }
}
