package za.ac.ctu.campusrent.model;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum BookingStatus {
    PENDING, APPROVED, ACTIVE, RETURNED, OVERDUE, CANCELLED, COMPLETED;

    private static final Map<BookingStatus, Set<BookingStatus>> TRANSITIONS =
            Map.of(
                    PENDING, EnumSet.of(APPROVED, CANCELLED),
                    APPROVED, EnumSet.of(ACTIVE, CANCELLED),
                    ACTIVE, EnumSet.of(OVERDUE, RETURNED, COMPLETED),
                    OVERDUE, EnumSet.of(RETURNED, COMPLETED),
                    RETURNED, EnumSet.noneOf(BookingStatus.class),
                    COMPLETED, EnumSet.noneOf(BookingStatus.class),
                    CANCELLED, EnumSet.noneOf(BookingStatus.class)
            );

    public boolean canTransitionTo(BookingStatus next) {
        return next != null
                && next != this
                && TRANSITIONS.get(this).contains(next);
    }

    public static BookingStatus fromDatabase(String value) {
        if (value == null || value.isBlank()) {
            return PENDING;
        }

        if ("RETURNED".equalsIgnoreCase(value)) {
            return RETURNED;
        }

        return valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
    }
}