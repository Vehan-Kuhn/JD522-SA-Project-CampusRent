package za.ac.ctu.campusrent.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public interface Rentable {

    boolean isAvailableForRental();

    default long calculateRentalDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("Invalid rental dates.");
        }

        return ChronoUnit.DAYS.between(start, end) + 1;
    }

    static double calculateBaseCost(double dailyRate, long days) {
        if (!Double.isFinite(dailyRate) || dailyRate < 0 || days < 1) {
            throw new IllegalArgumentException("Invalid rate or rental period.");
        }

        return dailyRate * days;
    }
}
