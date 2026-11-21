package za.ac.ctu.campusrent.concurrency;

import za.ac.ctu.campusrent.service.BookingService;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class OverdueBookingScheduler implements AutoCloseable {

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(
                        r, "CampusRent-Overdue-Checker");
                thread.setDaemon(true);
                return thread;
            });

    public void start(
            BookingService bookingService,
            Duration interval) {

        if (bookingService == null || interval == null
                || interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException(
                    "A service and positive interval are required.");
        }

        long seconds = Math.max(1, interval.toSeconds());

        scheduler.scheduleAtFixedRate(() -> {
            try {
                bookingService.updateOverdueBookings();
            } catch (Exception ex) {
                System.err.println(
                        "Overdue check failed: " + ex.getMessage());
            }
        }, 0, seconds, TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdown();
    }

    @Override
    public void close() {
        scheduler.shutdown();

        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException ex) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}