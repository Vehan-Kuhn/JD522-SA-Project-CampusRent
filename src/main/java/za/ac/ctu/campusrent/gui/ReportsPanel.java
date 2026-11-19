
package za.ac.ctu.campusrent.gui;

import za.ac.ctu.campusrent.model.Booking;
import za.ac.ctu.campusrent.model.Equipment;
import za.ac.ctu.campusrent.service.BookingService;
import za.ac.ctu.campusrent.service.EquipmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import za.ac.ctu.campusrent.service.StreamReportService;

public class ReportsPanel extends JPanel {

    private final BookingService bookingService;
    private final EquipmentService equipmentService;
    private final StreamReportService streamReportService =
            new StreamReportService();

    private final JLabel equipmentCountLabel = new JLabel("0");
    private final JLabel availableCountLabel = new JLabel("0");
    private final JLabel bookingCountLabel = new JLabel("0");
    private final JLabel revenueLabel = new JLabel("R0.00");
    private final JLabel lastUpdatedLabel = new JLabel("Not loaded yet");

    private final DefaultTableModel statusModel =
            createModel("Booking Status", "Number of Bookings");

    private final DefaultTableModel availabilityModel =
            createModel("Equipment Status", "Number of Items");

    private final DefaultTableModel topEquipmentModel =
            createModel("Equipment", "Bookings");

    private final DefaultTableModel categoryRevenueModel =
            createModel("Category", "Booking Value (R)");

    private final DefaultTableModel campusRevenueModel =
            createModel("Campus ID", "Booking Value (R)");

    private final DefaultTableModel overdueModel =
            createModel("Rental Timing", "Number of Bookings");

    private final JButton refreshButton =
            new JButton("Refresh Reports");

    private final JButton exportButton =
            new JButton("Export CSV");

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("en-ZA"));

    public ReportsPanel() {
        bookingService = new BookingService();
        equipmentService = new EquipmentService();

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(22, 22, 22, 22));

        add(createHeader(), BorderLayout.NORTH);
        add(createReportContent(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadReports());
        exportButton.addActionListener(e -> exportReports());

        loadReports();

        // Run the 100,000-record performance demonstration
        // without blocking the Swing interface.
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                benchmarkStreams();
                return null;
            }

        }.execute();
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout(10, 12));
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout(0, 5));
        titlePanel.setOpaque(false);

        JLabel heading = new JLabel("Reports Dashboard");
        heading.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel subtitle = new JLabel(
                "Live equipment, booking and rental performance.");

        subtitle.setForeground(Color.GRAY);

        titlePanel.add(heading, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.CENTER);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        buttons.setOpaque(false);

        styleButton(refreshButton, new Color(41, 128, 185));
        styleButton(exportButton, new Color(39, 174, 96));

        buttons.add(refreshButton);
        buttons.add(exportButton);

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(buttons, BorderLayout.EAST);

        return header;
    }

    private JPanel createReportContent() {

        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setOpaque(false);

        JPanel summary = new JPanel(
                new GridLayout(1, 4, 12, 12));

        summary.setOpaque(false);

        summary.add(createSummaryCard(
                "Total Equipment",
                equipmentCountLabel,
                new Color(52, 152, 219)));

        summary.add(createSummaryCard(
                "Available Equipment",
                availableCountLabel,
                new Color(39, 174, 96)));

        summary.add(createSummaryCard(
                "Total Bookings",
                bookingCountLabel,
                new Color(142, 68, 173)));

        summary.add(createSummaryCard(
                "Booking Value",
                revenueLabel,
                new Color(230, 126, 34)));

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Booking Status",
                createTablePanel(statusModel));

        tabs.addTab("Equipment Availability",
                createTablePanel(availabilityModel));

        tabs.addTab("Top Rented Equipment",
                createTablePanel(topEquipmentModel));

        tabs.addTab("Category Revenue",
                createTablePanel(categoryRevenueModel));

        tabs.addTab("Campus Revenue",
                createTablePanel(campusRevenueModel));

        tabs.addTab("On-Time / Overdue",
                createTablePanel(overdueModel));

        content.add(summary, BorderLayout.NORTH);
        content.add(tabs, BorderLayout.CENTER);

        return content;
    }

    private JPanel createFooter() {

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        lastUpdatedLabel.setForeground(Color.GRAY);
        lastUpdatedLabel.setFont(
                new Font("Arial", Font.PLAIN, 11));

        JLabel note = new JLabel(
                "Booking value excludes cancelled bookings.");

        note.setForeground(Color.GRAY);
        note.setFont(new Font("Arial", Font.PLAIN, 11));

        footer.add(note, BorderLayout.WEST);
        footer.add(lastUpdatedLabel, BorderLayout.EAST);

        return footer;
    }

    private JPanel createSummaryCard(
            String title,
            JLabel value,
            Color accent) {

        JPanel card = new JPanel(
                new BorderLayout(8, 12));

        card.setBackground(Color.WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 230, 235)),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setFont(
                new Font("Arial", Font.PLAIN, 12));

        value.setFont(
                new Font("Arial", Font.BOLD, 23));

        value.setForeground(accent);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTablePanel(DefaultTableModel model) {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JTable table = new JTable(model);

        table.setRowHeight(29);
        table.setFont(new Font("Arial", Font.PLAIN, 13));

        table.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13));

        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private DefaultTableModel createModel(String... columns) {

        return new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void styleButton(
            JButton button,
            Color background) {

        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        button.setBorder(
                new EmptyBorder(10, 14, 10, 14));
    }

    // Retrieves database data without blocking the GUI.
    private void loadReports() {

        refreshButton.setEnabled(false);
        exportButton.setEnabled(false);
        refreshButton.setText("Loading...");

        new SwingWorker<ReportData, Void>() {

            @Override
            protected ReportData doInBackground()
                    throws Exception {

                List<Booking> bookings =
                        bookingService.getAllBookings();

                List<Equipment> equipment =
                        equipmentService.getAllEquipment();

                return new ReportData(bookings, equipment);
            }

            @Override
            protected void done() {

                try {

                    ReportData data = get();

                    updateDashboard(data);

                    lastUpdatedLabel.setText(
                            "Last updated: " +
                                    LocalDateTime.now().format(
                                            DateTimeFormatter.ofPattern(
                                                    "dd MMM yyyy HH:mm:ss"))
                    );

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showError(
                            "Report loading was interrupted.");

                } catch (ExecutionException ex) {

                    showError(
                            "Could not load reports: "
                                    + getErrorMessage(ex));

                } finally {

                    refreshButton.setEnabled(true);
                    exportButton.setEnabled(true);
                    refreshButton.setText("Refresh Reports");
                }
            }

        }.execute();
    }

    private void updateDashboard(ReportData data) {

        List<Booking> bookings = data.bookings;
        List<Equipment> equipment = data.equipment;
        // Generate the required Stream API reports.
        StreamReportService.StreamReport streamReport =
                streamReportService.generateReport(bookings, equipment);
// Demonstrate Optional safely.
        streamReport.firstActiveBooking().ifPresentOrElse(
                booking -> System.out.println(
                        "First active booking: " + booking.getBookingId()),
                () -> System.out.println("No active bookings found.")
        );

// Display Stream API results in the console.
        System.out.println("\n========== STREAM REPORTS ==========");

        System.out.println("Top five rented equipment:");

        streamReport.topFive().forEach(item ->
                System.out.println(
                        item.equipmentName()
                                + " - " + item.rentalCount() + " rentals")
        );

        System.out.println("Bookings per campus: "
                + streamReport.bookingsPerCampus());

        System.out.println("Revenue per campus: "
                + streamReport.revenuePerCampus());

        System.out.println("Revenue per category: "
                + streamReport.revenuePerCategory());

        System.out.println("On-time bookings: "
                + streamReport.onTimeAndOverdue().get(false).size());

        System.out.println("Overdue bookings: "
                + streamReport.onTimeAndOverdue().get(true).size());

        System.out.println("Total revenue: R"
                + streamReport.totalRevenue());

        System.out.println("Average equipment daily rate: R"
                + streamReport.averageDailyRate());

        System.out.println("====================================");
        equipmentCountLabel.setText(
                String.valueOf(equipment.size()));

        long availableCount = equipment.stream()
                .filter(e -> "AVAILABLE".equalsIgnoreCase(
                        e.getAvailabilityStatus()))
                .count();

        availableCountLabel.setText(
                String.valueOf(availableCount));

        bookingCountLabel.setText(
                String.valueOf(bookings.size()));

        double totalBookingValue = bookings.stream()
                .filter(b -> !"CANCELLED".equalsIgnoreCase(
                        b.getBookingStatus()))
                .mapToDouble(Booking::getTotalCost)
                .sum();

        revenueLabel.setText(
                currencyFormat.format(totalBookingValue));

        updateBookingStatuses(bookings);
        updateEquipmentAvailability(equipment);
        updateTopEquipmentFromStreams(streamReport);
        updateCategoryRevenue(bookings, equipment);
        updateCampusRevenue(bookings, equipment);
        updateOverdueReport(bookings);

        demonstrateAdvancedStreams(bookings);
    }

    // GROUPING BY: Count bookings for each status.
    private void updateBookingStatuses(
            List<Booking> bookings) {

        statusModel.setRowCount(0);

        Map<String, Long> counts = bookings.stream()
                .collect(Collectors.groupingBy(
                        b -> b.getBookingStatus() == null
                                ? "UNKNOWN"
                                : b.getBookingStatus()
                                .toUpperCase(Locale.ROOT),
                        TreeMap::new,
                        Collectors.counting()
                ));

        counts.forEach((status, count) ->
                statusModel.addRow(
                        new Object[]{status, count}));
    }

    // GROUPING BY: Count equipment availability.
    private void updateEquipmentAvailability(
            List<Equipment> equipment) {

        availabilityModel.setRowCount(0);
        Map<String, Long> counts = equipment.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getAvailabilityStatus() == null
                                ? "UNKNOWN"
                                : e.getAvailabilityStatus()
                                .toUpperCase(Locale.ROOT),
                        TreeMap::new,
                        Collectors.counting()
                ));

        counts.forEach((status, count) ->
                availabilityModel.addRow(
                        new Object[]{status, count}));
    }

    // Populate the Top Rented Equipment table from StreamReportService.
    private void updateTopEquipmentFromStreams(
            StreamReportService.StreamReport report) {

        topEquipmentModel.setRowCount(0);

        report.topFive().forEach(item ->
                topEquipmentModel.addRow(new Object[]{
                        item.equipmentName(),
                        item.rentalCount()
                })
        );
    }

    // SORTING AND LIMIT: Display the five most rented items.
    private void updateTopEquipment(
            List<Booking> bookings,
            List<Equipment> equipment) {

        topEquipmentModel.setRowCount(0);

        Map<Integer, String> equipmentNames =
                equipment.stream()
                        .collect(Collectors.toMap(
                                Equipment::getEquipmentId,
                                Equipment::getEquipmentName,
                                (first, second) -> first
                        ));

        Map<Integer, Long> rentalCounts =
                bookings.stream()
                        .filter(b -> !"CANCELLED".equalsIgnoreCase(
                                b.getBookingStatus()))
                        .collect(Collectors.groupingBy(
                                Booking::getEquipmentId,
                                Collectors.counting()
                        ));

        rentalCounts.entrySet().stream()
                .sorted(
                        Map.Entry.<Integer, Long>
                                comparingByValue().reversed())
                .limit(5)
                .forEach(entry -> {

                    String name = equipmentNames.getOrDefault(
                            entry.getKey(),
                            "Equipment #" + entry.getKey());

                    topEquipmentModel.addRow(
                            new Object[]{
                                    name,
                                    entry.getValue()
                            });
                });
    }

    // GROUPING BY AND SUMMING: Revenue per equipment category.
    private void updateCategoryRevenue(
            List<Booking> bookings,
            List<Equipment> equipment) {

        categoryRevenueModel.setRowCount(0);

        Map<Integer, Equipment> equipmentMap =
                equipment.stream()
                        .collect(Collectors.toMap(
                                Equipment::getEquipmentId,
                                e -> e,
                                (first, second) -> first
                        ));

        Map<String, Double> revenue =
                bookings.stream()
                        .filter(b -> !"CANCELLED".equalsIgnoreCase(
                                b.getBookingStatus()))
                        .filter(b -> equipmentMap.containsKey(
                                b.getEquipmentId()))
                        .collect(Collectors.groupingBy(
                                b -> {
                                    String category = equipmentMap
                                            .get(b.getEquipmentId())
                                            .getCategory();

                                    return category == null
                                            ? "UNKNOWN"
                                            : category;
                                },
                                TreeMap::new,
                                Collectors.summingDouble(
                                        Booking::getTotalCost)
                        ));

        revenue.forEach((category, amount) ->
                categoryRevenueModel.addRow(
                        new Object[]{
                                category,
                                currencyFormat.format(amount)
                        }));
    }

    // GROUPING BY AND SUMMING: Revenue per campus.
    private void updateCampusRevenue(
            List<Booking> bookings,
            List<Equipment> equipment) {

        campusRevenueModel.setRowCount(0);

        Map<Integer, Equipment> equipmentMap =
                equipment.stream()
                        .collect(Collectors.toMap(
                                Equipment::getEquipmentId,
                                e -> e,
                                (first, second) -> first
                        ));

        Map<Integer, Double> revenue =
                bookings.stream()
                        .filter(b -> !"CANCELLED".equalsIgnoreCase(
                                b.getBookingStatus()))
                        .filter(b -> equipmentMap.containsKey(
                                b.getEquipmentId()))
                        .collect(Collectors.groupingBy(
                                b -> equipmentMap.get(
                                        b.getEquipmentId()).getCampusId(),
                                TreeMap::new,
                                Collectors.summingDouble(
                                        Booking::getTotalCost)
                        ));

        revenue.forEach((campusId, amount) ->
                campusRevenueModel.addRow(
                        new Object[]{
                                campusId,
                                currencyFormat.format(amount)
                        }));
    }

    // PARTITIONING BY: Separate overdue from non-overdue bookings.
    private void updateOverdueReport(
            List<Booking> bookings) {

        overdueModel.setRowCount(0);

        Map<Boolean, Long> timing =
                bookings.stream()
                        .filter(b -> !"CANCELLED".equalsIgnoreCase(
                                b.getBookingStatus()))
                        .collect(Collectors.partitioningBy(
                                b -> "OVERDUE".equalsIgnoreCase(
                                        b.getBookingStatus()),
                                Collectors.counting()
                        ));

        overdueModel.addRow(
                new Object[]{"Overdue", timing.get(true)});

        overdueModel.addRow(
                new Object[]{"Not Overdue", timing.get(false)});
    }

    // OPTIONAL, REDUCE, PRIMITIVE STREAM AND FLATMAP.
    private void demonstrateAdvancedStreams(
            List<Booking> bookings) {

        Optional<Booking> firstBooking =
                bookings.stream().findFirst();

        firstBooking.ifPresentOrElse(
                booking -> System.out.println(
                        "First booking ID: "
                                + booking.getBookingId()),
                () -> System.out.println("No bookings found.")
        );

        // REDUCE: Calculate total booking value.
        double totalValue = bookings.stream()
                .filter(b -> !"CANCELLED".equalsIgnoreCase(
                        b.getBookingStatus()))
                .map(Booking::getTotalCost)
                .reduce(0.0, Double::sum);

        System.out.println(
                "Total value using reduce: "
                        + currencyFormat.format(totalValue));

        // PRIMITIVE STREAM: Calculate average booking value.
        double averageValue = bookings.stream()
                .filter(b -> !"CANCELLED".equalsIgnoreCase(
                        b.getBookingStatus()))
                .mapToDouble(Booking::getTotalCost)
                .average()
                .orElse(0.0);

        System.out.println(
                "Average booking value: "
                        + currencyFormat.format(averageValue));

        // FLATMAP: Expand each booking into rental-day entries.
        List<Long> rentalDays = bookings.stream()
                .filter(b -> b.getStartDate() != null
                        && b.getDueDate() != null)
                .flatMap(b -> IntStream.rangeClosed(
                                0,
                                (int) ChronoUnit.DAYS.between(
                                        b.getStartDate(),
                                        b.getDueDate()))
                        .mapToObj(day -> (long) day + 1))
                .toList();

        System.out.println(
                "Total rental-day entries: " + rentalDays.size());
    }

    // PERFORMANCE TEST: Compare sequential and parallel streams.
    private void benchmarkStreams() {

        List<SyntheticBooking> syntheticBookings =
                IntStream.range(0, 100_000)
                        .mapToObj(i -> new SyntheticBooking(
                                i % 20,
                                100.0 + (i % 10),
                                i % 5 == 0
                        ))
                        .toList();

        long sequentialStart = System.nanoTime();

        Map<Integer, Double> sequentialResult =
                syntheticBookings.stream()
                        .collect(Collectors.groupingBy(
                                SyntheticBooking::equipmentId,
                                Collectors.summingDouble(
                                        SyntheticBooking::amount)
                        ));

        long sequentialTime =
                System.nanoTime() - sequentialStart;

        long parallelStart = System.nanoTime();

        Map<Integer, Double> parallelResult =
                syntheticBookings.parallelStream()
                        .collect(Collectors.groupingBy(
                                SyntheticBooking::equipmentId,
                                Collectors.summingDouble(
                                        SyntheticBooking::amount)
                        ));

        long parallelTime =
                System.nanoTime() - parallelStart;

        System.out.println("\n--- STREAM PERFORMANCE TEST ---");

        System.out.println(
                "Synthetic bookings: "
                        + syntheticBookings.size());

        System.out.println(
                "Sequential time: "
                        + sequentialTime / 1_000_000.0 + " ms");

        System.out.println(
                "Parallel time: "
                        + parallelTime / 1_000_000.0 + " ms");

        System.out.println(
                "Results match: "
                        + sequentialResult.equals(parallelResult));
    }

    private record SyntheticBooking(
            int equipmentId,
            double amount,
            boolean overdue) {
    }

    // Export all report tables into one CSV file.
    private void exportReports() {

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle("Export CampusRent Reports");

        chooser.setSelectedFile(
                new java.io.File("CampusRent_Reports.csv"));

        if (chooser.showSaveDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();

        if (!path.toString()
                .toLowerCase(Locale.ROOT).endsWith(".csv")) {

            path = Path.of(path.toString() + ".csv");
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                path, StandardCharsets.UTF_8)) {

            writeTable(writer, "BOOKING STATUS", statusModel);
            writeTable(writer, "EQUIPMENT AVAILABILITY", availabilityModel);
            writeTable(writer, "TOP RENTED EQUIPMENT", topEquipmentModel);
            writeTable(writer, "CATEGORY REVENUE", categoryRevenueModel);
            writeTable(writer, "CAMPUS REVENUE", campusRevenueModel);
            writeTable(writer, "ON-TIME AND OVERDUE", overdueModel);

            JOptionPane.showMessageDialog(
                    this,
                    "Reports exported successfully:\n"
                            + path.toAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException ex) {

            showError(
                    "Could not export reports: " + ex.getMessage());
        }
    }

    private void writeTable(
            BufferedWriter writer,
            String heading,
            DefaultTableModel model) throws IOException {

        writer.newLine();
        writer.write(escapeCsv(heading));
        writer.newLine();

        for (int column = 0;
             column < model.getColumnCount();
             column++) {

            if (column > 0) {
                writer.write(",");
            }

            writer.write(
                    escapeCsv(model.getColumnName(column)));
        }

        writer.newLine();

        for (int row = 0;
             row < model.getRowCount();
             row++) {

            for (int column = 0;
                 column < model.getColumnCount();
                 column++) {

                if (column > 0) {
                    writer.write(",");
                }

                Object value = model.getValueAt(row, column);

                writer.write(escapeCsv(
                        value == null ? "" : value.toString()));
            }

            writer.newLine();
        }
    }

    private String escapeCsv(String value) {

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String getErrorMessage(Throwable exception) {

        Throwable cause = exception;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        return cause.getMessage() == null
                ? cause.getClass().getSimpleName()
                : cause.getMessage();
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Reports Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static class ReportData {

        private final List<Booking> bookings;
        private final List<Equipment> equipment;

        private ReportData(
                List<Booking> bookings,
                List<Equipment> equipment) {

            this.bookings = bookings;
            this.equipment = equipment;
        }
    }
}
