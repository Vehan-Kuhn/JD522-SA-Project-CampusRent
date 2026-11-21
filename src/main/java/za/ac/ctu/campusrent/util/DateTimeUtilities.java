package za.ac.ctu.campusrent.util;

import java.text.NumberFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public final class DateTimeUtilities {

    public static final ZoneId SOUTH_AFRICAN_ZONE =
            ZoneId.of("Africa/Johannesburg");

    private DateTimeUtilities() {
    }

    public static long rentalDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("Invalid rental dates.");
        }

        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    public static Period calendarPeriod(
            LocalDate start, LocalDate end) {
        return Period.between(start, end);
    }

    public static Duration elapsedTime(
            LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end);
    }

    public static ZonedDateTime auditTimestamp() {
        return ZonedDateTime.now(SOUTH_AFRICAN_ZONE);
    }

    public static String formatDate(
            LocalDate date, Locale locale) {

        Locale selectedLocale = locale == null
                ? Locale.forLanguageTag("en-ZA")
                : locale;

        return date.format(
                DateTimeFormatter
                        .ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(selectedLocale));
    }

    public static String formatDateCustom(LocalDate date) {
        return date.format(
                DateTimeFormatter.ofPattern(
                        "dd MMMM uuuu",
                        Locale.forLanguageTag("en-ZA")));
    }

    public static String formatRand(double amount, Locale locale) {
        Locale selectedLocale = locale == null
                ? Locale.forLanguageTag("en-ZA")
                : locale;

        NumberFormat currency =
                NumberFormat.getCurrencyInstance(selectedLocale);

        currency.setCurrency(java.util.Currency.getInstance("ZAR"));

        return currency.format(amount);
    }
}