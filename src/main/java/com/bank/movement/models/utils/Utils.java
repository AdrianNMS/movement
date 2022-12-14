package com.bank.movement.models.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class Utils
{
    public static boolean BetweenDates(LocalDateTime date, LocalDateTime minDate, LocalDateTime maxDate)
    {
        return date.isAfter(minDate.minusDays(1))&& date.isBefore(maxDate.plusDays(1));
    }

    public static LocalDateTime parseLocalDate(String date)
    {
        DateTimeFormatter DATEFORMATTER1 = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        DateTimeFormatter DATEFORMATTER = new DateTimeFormatterBuilder().append(DATEFORMATTER1)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                .toFormatter();

        //DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDateTime.parse(date, DATEFORMATTER);
    }
}
