package za.ac.ctu.campusrent.exception;

import java.io.IOException;
import java.io.StringReader;

public final class ExceptionDemonstration {

    private ExceptionDemonstration() {
    }

    public static double validateRate(double rate) {
        if (!Double.isFinite(rate) || rate < 0) {
            throw new InvalidRateException(
                    "Daily rate must be zero or greater.");
        }

        return rate;
    }

    public static void validateBookingDates(
            java.time.LocalDate start,
            java.time.LocalDate due)
            throws InvalidBookingException {

        if (start == null || due == null || due.isBefore(start)) {
            throw new InvalidBookingException(
                    "Booking dates are invalid.");
        }
    }

    public static void demonstrate() {

        // Run with -ea to enable this assertion.
        double sampleRate = 150.0;
        assert sampleRate >= 0 : "Rental rate invariant failed";

        // Try-with-resources, multi-catch and finally.
        try (StringReader reader = new StringReader("CampusRent")) {
            if (reader.read() < 0) {
                throw new IOException("No input.");
            }
        } catch (IOException | IllegalArgumentException ex) {
            System.err.println("Input problem: " + ex.getMessage());
        } finally {
            System.out.println("Exception demonstration finished.");
        }

        // Suppressed exceptions from resources closing.
        class FailingResource implements AutoCloseable {
            private final String name;

            FailingResource(String name) {
                this.name = name;
            }

            @Override
            public void close() throws IOException {
                throw new IOException("Close failed: " + name);
            }
        }

        try (FailingResource first = new FailingResource("first");
             FailingResource second = new FailingResource("second")) {

            throw new IOException("Primary operation failure.");

        } catch (IOException ex) {
            System.out.println("Primary: " + ex.getMessage());

            for (Throwable suppressed : ex.getSuppressed()) {
                System.out.println(
                        "Suppressed: " + suppressed.getMessage());
            }
        }

        // Exception chaining.
        try {
            try {
                throw new IOException("Database operation failed.");
            } catch (IOException cause) {
                throw new InvalidBookingException(
                        "Could not create booking.", cause);
            }
        } catch (InvalidBookingException ex) {
            System.out.println(
                    "Chained cause: " + ex.getCause());
        }
    }
}