
package za.ac.ctu.campusrent.service;

import za.ac.ctu.campusrent.model.Booking;
import za.ac.ctu.campusrent.model.Equipment;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StreamReportService {

    public record EquipmentRental(
            int equipmentId,
            String equipmentName,
            long rentalCount) {
    }

    public record SyntheticBooking(
            int equipmentId,
            double totalCost) {
    }

    public record StreamReport(
            List<EquipmentRental> topFive,
            Map<Integer, Long> bookingsPerCampus,
            Map<Integer, Double> revenuePerCampus,
            Map<String, Double> revenuePerCategory,
            Map<Boolean, List<Booking>> onTimeAndOverdue,
            double totalRevenue,
            double averageDailyRate,
            Optional<Booking> firstActiveBooking) {
    }

    /**
     * Builds all required reports using the Java Stream API.
     */
    public StreamReport generateReport(
            List<Booking> bookings,
            List<Equipment> equipment) {

        Objects.requireNonNull(bookings);
        Objects.requireNonNull(equipment);

        Map<Integer, Equipment> equipmentMap = equipment.stream()
                .collect(Collectors.toMap(
                        Equipment::getEquipmentId,
                        Function.identity(),
                        (first, second) -> first
                ));

        // FILTER, MAP, SORTED and LIMIT:
        // Find the five most frequently rented equipment items.
        Map<Integer, Long> rentalCounts = bookings.stream()
                .filter(this::isNotCancelled)
                .collect(Collectors.groupingBy(
                        Booking::getEquipmentId,
                        Collectors.counting()
                ));

        List<EquipmentRental> topFive = rentalCounts.entrySet()
                .stream()
                .filter(entry -> equipmentMap.containsKey(entry.getKey()))
                .map(entry -> new EquipmentRental(
                        entry.getKey(),
                        equipmentMap.get(entry.getKey()).getEquipmentName(),
                        entry.getValue()
                ))
                .sorted(Comparator.comparingLong(
                        EquipmentRental::rentalCount).reversed())
                .limit(5)
                .toList();

        // GROUPING BY AND COUNTING:
        // Count bookings per campus.
        Map<Integer, Long> bookingsPerCampus = bookings.stream()
                .filter(this::isNotCancelled)
                .flatMap(booking ->
                        Optional.ofNullable(
                                equipmentMap.get(booking.getEquipmentId())
                        ).stream()
                )
                .collect(Collectors.groupingBy(
                        Equipment::getCampusId,
                        Collectors.counting()
                ));

        // GROUPING BY AND SUMMING DOUBLE:
        // Calculate revenue per campus.
        Map<Integer, Double> revenuePerCampus = bookings.stream()
                .filter(this::isNotCancelled)
                .filter(b -> equipmentMap.containsKey(b.getEquipmentId()))
                .collect(Collectors.groupingBy(
                        b -> equipmentMap.get(
                                b.getEquipmentId()).getCampusId(),
                        Collectors.summingDouble(Booking::getTotalCost)
                ));

        // Calculate revenue per equipment category.
        Map<String, Double> revenuePerCategory = bookings.stream()
                .filter(this::isNotCancelled)
                .filter(b -> equipmentMap.containsKey(b.getEquipmentId()))
                .collect(Collectors.groupingBy(
                        b -> equipmentMap.get(
                                b.getEquipmentId()).getCategory(),
                        Collectors.summingDouble(Booking::getTotalCost)
                ));

        // PARTITIONING BY:
        // Separate overdue and on-time bookings.
        Map<Boolean, List<Booking>> onTimeAndOverdue =
                bookings.stream()
                        .filter(this::isNotCancelled)
                        .collect(Collectors.partitioningBy(
                                this::isOverdue
                        ));

        // REDUCE:
        // Calculate total booking revenue.
        double totalRevenue = bookings.stream()
                .filter(this::isNotCancelled)
                .map(Booking::getTotalCost)
                .reduce(0.0, Double::sum);

        // PRIMITIVE STREAM:
        // Calculate the average equipment daily rental rate.
        double averageDailyRate = equipment.stream()
                .mapToDouble(Equipment::getDailyRate)
                .average()
                .orElse(0.0);

        // OPTIONAL:
        // Safely find the first active booking.
        Optional<Booking> firstActiveBooking = bookings.stream()
                .filter(b -> "ACTIVE".equalsIgnoreCase(
                        b.getBookingStatus()))
                .findFirst();

        return new StreamReport(
                topFive,
                bookingsPerCampus,
                revenuePerCampus,
                revenuePerCategory,
                onTimeAndOverdue,
                totalRevenue,
                averageDailyRate,
                firstActiveBooking
        );
    }

    private boolean isNotCancelled(Booking booking) {
        return booking.getBookingStatus() != null
                && !"CANCELLED".equalsIgnoreCase(
                booking.getBookingStatus());
    }

    private boolean isOverdue(Booking booking) {

        if ("OVERDUE".equalsIgnoreCase(
                booking.getBookingStatus())) {
            return true;
        }

        return booking.getDueDate() != null
                && booking.getDueDate().isBefore(LocalDate.now())
                && "ACTIVE".equalsIgnoreCase(
                booking.getBookingStatus());
    }

    /**
     * Demonstrates Optional with findFirst() and orElseThrow().
     */
    public Booking findFirstActiveBooking(List<Booking> bookings) {

        return bookings.stream()
                .filter(b -> "ACTIVE".equalsIgnoreCase(
                        b.getBookingStatus()))
                .findFirst()
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "No active bookings were found."));
    }

    /**
     * Generates 100,000 synthetic bookings and compares
     * sequential and parallel stream performance.
     */
    public void benchmarkStreams() {

        System.out.println("\n========== STREAM PERFORMANCE TEST ==========");

        List<SyntheticBooking> syntheticBookings =
                IntStream.range(0, 100_000)
                        .mapToObj(i -> new SyntheticBooking(
                                (i % 20) + 1,
                                100.0 + (i % 500)
                        ))
                        .toList();

        // Sequential stream.
        long sequentialStart = System.nanoTime();

        double sequentialRevenue = syntheticBookings.stream()
                .mapToDouble(SyntheticBooking::totalCost)
                .sum();

        long sequentialTime =
                System.nanoTime() - sequentialStart;

        // Parallel stream: same report, same dataset.
        long parallelStart = System.nanoTime();

        double parallelRevenue = syntheticBookings.parallelStream()
                .mapToDouble(SyntheticBooking::totalCost)
                .sum();

        long parallelTime =
                System.nanoTime() - parallelStart;

        System.out.println(
                "Synthetic bookings: " + syntheticBookings.size());

        System.out.println(
                "Sequential revenue: R" + sequentialRevenue);

        System.out.println(
                "Parallel revenue: R" + parallelRevenue);

        System.out.println(
                "Sequential time: " +
                        sequentialTime / 1_000_000.0 + " ms");

        System.out.println(
                "Parallel time: " +
                        parallelTime / 1_000_000.0 + " ms");

        System.out.println(
                "Results match: " +
                        (Math.abs(sequentialRevenue - parallelRevenue)
                                < 0.01));

        System.out.println(
                "==============================================");
    }

    /**
     * Demonstrates Optional.orElseThrow() safely.
     */
    public String getFirstActiveBookingDescription(
            List<Booking> bookings) {

        return bookings.stream()
                .filter(b -> "ACTIVE".equalsIgnoreCase(
                        b.getBookingStatus()))
                .findFirst()
                .map(b -> "Active booking ID: " + b.getBookingId())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "No active bookings available."));
    }
}
