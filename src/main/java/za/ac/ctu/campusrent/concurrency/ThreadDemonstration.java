
package za.ac.ctu.campusrent.concurrency;

import java.util.concurrent.CountDownLatch;

/**
 * Demonstrates Java thread creation, race conditions,
 * synchronization and thread lifecycle management.
 */
public class ThreadDemonstration {

    /**
     * Shared counter used to demonstrate thread safety.
     */
    private static class SharedCounter {

        private int counter = 0;

        public void incrementUnsafe() {
            counter++;
        }

        public synchronized void incrementSafe() {
            counter++;
        }

        public int getValue() {
            return counter;
        }

        public void reset() {
            counter = 0;
        }
    }

    /**
     * Demonstrates creating a thread by extending Thread.
     */
    private static class AutoSaveThread extends Thread {

        public AutoSaveThread() {
            super("CampusRent-AutoSave");
        }

        @Override
        public void run() {

            System.out.println(
                    getName() + " started.");

            try {

                for (int i = 1; i <= 3; i++) {

                    System.out.println(
                            "Auto-save demonstration: save " + i);

                    Thread.sleep(1000);
                }

            } catch (InterruptedException ex) {

                Thread.currentThread().interrupt();

                System.out.println(
                        "Auto-save thread interrupted.");
            }

            System.out.println(
                    getName() + " finished.");
        }
    }

    /**
     * Demonstrates creating a thread using Runnable.
     */
    private static class ClockTask implements Runnable {

        @Override
        public void run() {

            try {

                for (int i = 1; i <= 3; i++) {

                    System.out.println(
                            "Clock update: "
                                    + java.time.LocalDateTime.now());

                    Thread.sleep(1000);
                }

            } catch (InterruptedException ex) {

                Thread.currentThread().interrupt();

                System.out.println(
                        "Clock task interrupted.");
            }
        }
    }

    /**
     * Demonstrates an interruptible background task.
     */
    private static class InterruptibleTask implements Runnable {

        @Override
        public void run() {

            try {

                while (!Thread.currentThread().isInterrupted()) {

                    System.out.println(
                            "Background task is running...");

                    Thread.sleep(1000);
                }

            } catch (InterruptedException ex) {

                Thread.currentThread().interrupt();

                System.out.println(
                        "Background task received interrupt.");
            }

            System.out.println(
                    "Background task stopped.");
        }
    }

    /**
     * Runs the unsynchronized and synchronized counter tests.
     */
    private static void demonstrateRaceCondition()
            throws InterruptedException {

        final int increments = 200_000;

        SharedCounter counter = new SharedCounter();

        CountDownLatch startSignal = new CountDownLatch(1);

        Thread first = new Thread(() -> {

            try {
                startSignal.await();

                for (int i = 0; i < increments; i++) {
                    counter.incrementUnsafe();
                }

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }, "Unsafe-Thread-1");

        Thread second = new Thread(() -> {

            try {
                startSignal.await();

                for (int i = 0; i < increments; i++) {
                    counter.incrementUnsafe();
                }

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }, "Unsafe-Thread-2");

        System.out.println("\n--- RACE CONDITION TEST ---");

        first.start();
        second.start();

        startSignal.countDown();

        // Wait for both threads to finish.
        first.join();
        second.join();

        int expected = increments * 2;

        System.out.println(
                "Expected counter: " + expected);

        System.out.println(
                "Actual counter without synchronization: "
                        + counter.getValue());

        System.out.println(
                "Race condition detected: "
                        + (counter.getValue() != expected));

        // Reset and repeat using synchronized increments.
        counter.reset();

        Thread safeFirst = new Thread(() -> {

            for (int i = 0; i < increments; i++) {
                counter.incrementSafe();
            }

        }, "Safe-Thread-1");

        Thread safeSecond = new Thread(() -> {

            for (int i = 0; i < increments; i++) {
                counter.incrementSafe();
            }

        }, "Safe-Thread-2");

        safeFirst.start();
        safeSecond.start();

        safeFirst.join();
        safeSecond.join();

        System.out.println(
                "Actual counter with synchronization: "
                        + counter.getValue());

        System.out.println(
                "Synchronization successful: "
                        + (counter.getValue() == expected));
    }

    /**
     * Demonstrates thread lifecycle states.
     */
    private static void demonstrateLifecycle()
            throws InterruptedException {

        System.out.println("\n--- THREAD LIFECYCLE ---");

        Thread worker = new Thread(() -> {

            try {
                Thread.sleep(2000);

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }

        }, "Lifecycle-Demonstration");

        System.out.println(
                "Before start: " + worker.getState());

        worker.start();

        System.out.println(
                "After start: " + worker.getState());

        Thread.sleep(200);

        System.out.println(
                "During sleep: " + worker.getState());

        worker.join();

        System.out.println(
                "After join: " + worker.getState());
    }

    /**
     * Demonstrates interruption of a running thread.
     */
    private static void demonstrateInterrupt()
            throws InterruptedException {

        System.out.println("\n--- INTERRUPT TEST ---");

        Thread worker = new Thread(
                new InterruptibleTask());

        worker.start();

        Thread.sleep(1500);

        System.out.println(
                "Sending interrupt signal...");

        worker.interrupt();

        worker.join();

        System.out.println(
                "Interrupt demonstration completed.");
    }

    /**
     * Entry point for the thread demonstrations.
     */
    public static void main(String[] args) {

        try {

            System.out.println(
                    "========== THREAD DEMONSTRATIONS ==========");

            // Thread subclass.
            AutoSaveThread autoSave = new AutoSaveThread();
            autoSave.start();

            // Runnable implementation.
            Thread clock = new Thread(
                    new ClockTask(),
                    "CampusRent-Clock");

            clock.start();

            // Wait for both demonstrations.
            autoSave.join();
            clock.join();

            demonstrateRaceCondition();

            demonstrateLifecycle();

            demonstrateInterrupt();

            System.out.println(
                    "\nAll thread demonstrations completed.");

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            System.err.println(
                    "Thread demonstration interrupted.");
        }
    }
}
