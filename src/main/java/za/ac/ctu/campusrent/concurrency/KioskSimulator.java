
package za.ac.ctu.campusrent.concurrency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simulates five campus kiosks competing to book limited equipment.
 * Demonstrates ExecutorService, Callable, Future, ReentrantLock
 * and thread-safe collections.
 */
public class KioskSimulator {

    private static final int NUMBER_OF_KIOSKS = 5;
    private static final int ATTEMPTS_PER_KIOSK = 10;
    private static final int NUMBER_OF_EQUIPMENT_ITEMS = 5;

    private final Map<Integer, ReentrantLock> equipmentLocks =
            new ConcurrentHashMap<>();

    private final Map<Integer, String> bookedEquipment =
            new ConcurrentHashMap<>();

    private final List<BookingResult> results =
            new CopyOnWriteArrayList<>();

    private final AtomicInteger successfulBookings =
            new AtomicInteger(0);

    private final AtomicInteger rejectedBookings =
            new AtomicInteger(0);

    public KioskSimulator() {

        for (int equipmentId = 1;
             equipmentId <= NUMBER_OF_EQUIPMENT_ITEMS;
             equipmentId++) {

            equipmentLocks.put(
                    equipmentId,
                    new ReentrantLock()
            );
        }
    }

    /**
     * Executes 50 concurrent booking attempts using five kiosk workers.
     */
    public List<BookingResult> simulate()
            throws InterruptedException, ExecutionException {

        ExecutorService executor =
                Executors.newFixedThreadPool(NUMBER_OF_KIOSKS);

        List<Future<BookingResult>> futures =
                new ArrayList<>();

        try {

            for (int kiosk = 1;
                 kiosk <= NUMBER_OF_KIOSKS;
                 kiosk++) {

                final int kioskId = kiosk;

                for (int attempt = 1;
                     attempt <= ATTEMPTS_PER_KIOSK;
                     attempt++) {

                    final int attemptNumber = attempt;

                    // Each kiosk makes ten attempts.
                    // The five equipment items are shared by all kiosks.
                    int equipmentId =
                            ((attemptNumber - 1)
                                    % NUMBER_OF_EQUIPMENT_ITEMS) + 1;

                    Callable<BookingResult> task = () ->
                            attemptBooking(kioskId, equipmentId);

                    futures.add(executor.submit(task));
                }
            }

            // Retrieve each Callable result through its Future.
            for (Future<BookingResult> future : futures) {

                BookingResult result = future.get();

                results.add(result);

                if (result.success()) {
                    successfulBookings.incrementAndGet();
                } else {
                    rejectedBookings.incrementAndGet();
                }
            }

        } finally {

            executor.shutdown();

            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }

        printResults();

        generateAsyncReport();

        return List.copyOf(results);
    }

    /**
     * Attempts to reserve one equipment item safely.
     */
    private BookingResult attemptBooking(
            int kioskId,
            int equipmentId) {

        ReentrantLock lock =
                equipmentLocks.get(equipmentId);

        boolean acquired = false;

        try {

            // Wait at most two seconds for the equipment lock.
            acquired = lock.tryLock(2, TimeUnit.SECONDS);

            if (!acquired) {

                return new BookingResult(
                        kioskId,
                        equipmentId,
                        false,
                        "Lock timeout"
                );
            }

            // putIfAbsent ensures an equipment item is reserved once.
            String existingOwner = bookedEquipment.putIfAbsent(
                    equipmentId,
                    "Kiosk-" + kioskId
            );

            if (existingOwner == null) {

                return new BookingResult(
                        kioskId,
                        equipmentId,
                        true,
                        "Booking successful"
                );
            }

            return new BookingResult(
                    kioskId,
                    equipmentId,
                    false,
                    "Already booked by " + existingOwner
            );

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            return new BookingResult(
                    kioskId,
                    equipmentId,
                    false,
                    "Booking interrupted"
            );

        } finally {

            if (acquired) {
                lock.unlock();
            }
        }
    }

    /**
     * Prints the outcome of all 50 attempts.
     */
    private void printResults() {

        System.out.println("\n========== KIOSK SIMULATION ==========");

        results.forEach(result ->
                System.out.println(
                        "Kiosk " + result.kioskId()
                                + " | Equipment " + result.equipmentId()
                                + " | " + result.message()
                ));

        System.out.println("--------------------------------------");

        System.out.println(
                "Total attempts: " + results.size());

        System.out.println(
                "Successful bookings: "
                        + successfulBookings.get());

        System.out.println(
                "Rejected attempts: "
                        + rejectedBookings.get());

        System.out.println(
                "Unique equipment booked: "
                        + bookedEquipment.size());

        System.out.println(
                "Double-booking prevented: "
                        + (bookedEquipment.size()
                        == successfulBookings.get()));

        System.out.println("======================================");
    }

    /**
     * Builds a background report using CompletableFuture.
     */
    private void generateAsyncReport() {

        CompletableFuture
                .supplyAsync(() -> results.size())

                .thenApply(total ->
                        "Total attempts processed: " + total
                                + "\nSuccessful bookings: "
                                + successfulBookings.get()
                                + "\nRejected attempts: "
                                + rejectedBookings.get()
                                + "\nUnique equipment reserved: "
                                + bookedEquipment.size()
                )

                .thenAccept(report -> {

                    System.out.println(
                            "\n--- ASYNCHRONOUS BOOKING REPORT ---");

                    System.out.println(report);
                })

                .join();
    }

    /**
     * Result returned by each kiosk booking attempt.
     */
    public record BookingResult(
            int kioskId,
            int equipmentId,
            boolean success,
            String message) {
    }

    /**
     * Runs the concurrency demonstration independently.
     */
    public static void main(String[] args) {

        try {

            KioskSimulator simulator = new KioskSimulator();

            simulator.simulate();

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            System.err.println(
                    "Simulation interrupted.");

        } catch (ExecutionException ex) {

            System.err.println(
                    "Simulation failed: " + ex.getMessage());
        }
    }
}
