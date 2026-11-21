
package za.ac.ctu.campusrent.gui;

import za.ac.ctu.campusrent.model.Booking;
import za.ac.ctu.campusrent.model.Equipment;
import za.ac.ctu.campusrent.service.BookingService;
import za.ac.ctu.campusrent.service.EquipmentService;
import za.ac.ctu.campusrent.service.StreamReportService;

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

/**
 * Reports dashboard with database-backed statistics,
 * Stream API demonstrations and CSV export.
 */
public class ReportsPanel extends JPanel {

    private final BookingService bookingService =
            new BookingService();

    private final EquipmentService equipmentService =
            new EquipmentService();

    private final StreamReportService streamReportService =
            new StreamReportService();

    private final JLabel equipmentCountLabel = new JLabel("0");
    private final JLabel availableCountLabel = new JLabel("0");
    private final JLabel bookingCountLabel = new JLabel("0");
    private final JLabel revenueLabel = new JLabel("R0.00");

    private final JLabel lastUpdatedLabel = new JLabel(
            t("reports.notLoaded"));

    private final DefaultTableModel statusModel = createModel(
            "reports.column.bookingStatus",
            "reports.column.bookingCount");

    private final DefaultTableModel availabilityModel = createModel(
            "reports.column.equipmentStatus",
            "reports.column.itemCount");

    private final DefaultTableModel topEquipmentModel = createModel(
            "reports.column.equipment",
            "reports.column.bookings");

    private final DefaultTableModel categoryRevenueModel = createModel(
            "reports.column.category",
            "reports.column.bookingValue");

    private final DefaultTableModel campusRevenueModel = createModel(
            "reports.column.campusId",
            "reports.column.bookingValue");

    private final DefaultTableModel overdueModel = createModel(
            "reports.column.rentalTiming",
            "reports.column.bookingCount");

    private final JButton refreshButton =
            new JButton(t("reports.refresh"));

    private final JButton exportButton =
            new JButton(t("reports.export"));

    private final NumberFormat currencyFormat =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("en-ZA"));

    private static String t(String key) {
        return LanguageManager.text(key);
    }

    public ReportsPanel() {

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(22, 22, 22, 22));

        add(createHeader(), BorderLayout.NORTH);
        add(createReportContent(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        refreshButton.addActionListener(e -> loadReports());
        exportButton.addActionListener(e -> exportReports());

        loadReports();

        // Demonstrate sequential and parallel streams
        // without blocking the Swing event thread.
        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() {
                benchmarkStreams();
                return null;
            }

        }.execute();
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(
                new BorderLayout(10, 12));

        header.setOpaque(false);

        JPanel titlePanel = new JPanel(
                new BorderLayout(0, 5));

        titlePanel.setOpaque(false);

        JLabel heading = new JLabel(
                t("reports.heading"));

        heading.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel subtitle = new JLabel(
                t("reports.subtitle"));

        subtitle.setForeground(Color.GRAY);

        titlePanel.add(heading, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.CENTER);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT));

        buttons.setOpaque(false);

        styleButton(
                refreshButton,
                new Color(41, 128, 185));

        styleButton(
                exportButton,
                new Color(39, 174, 96));

        buttons.add(refreshButton);
        buttons.add(exportButton);

        header.add(titlePanel, BorderLayout.CENTER);
        header.add(buttons, BorderLayout.EAST);

        return header;
    }

    private JPanel createReportContent() {

        JPanel content = new JPanel(
                new BorderLayout(15, 15));

        content.setOpaque(false);

        JPanel summary = new JPanel(
                new GridLayout(1, 4, 12, 12));

        summary.setOpaque(false);

        summary.add(createSummaryCard(
                t("reports.totalEquipment"),
                equipmentCountLabel,
                new Color(52, 152, 219)));

        summary.add(createSummaryCard(
                t("reports.availableEquipment"),
                availableCountLabel,
                new Color(39, 174, 96)));

        summary.add(createSummaryCard(
                t("reports.totalBookings"),
                bookingCountLabel,
                new Color(142, 68, 173)));

        summary.add(createSummaryCard(
                t("reports.bookingValue"),
                revenueLabel,
                new Color(230, 126, 34)));

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab(
                t("reports.tab.bookingStatus"),
                createTablePanel(statusModel));

        tabs.addTab(
                t("reports.tab.availability"),
                createTablePanel(availabilityModel));

        tabs.addTab(
                t("reports.tab.topEquipment"),
                createTablePanel(topEquipmentModel));

        tabs.addTab(
                t("reports.tab.categoryRevenue"),
                createTablePanel(categoryRevenueModel));

        tabs.addTab(
                t("reports.tab.campusRevenue"),
                createTablePanel(campusRevenueModel));

        tabs.addTab(
                t("reports.tab.overdue"),
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
                t("reports.footerNote"));

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

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 235)),
                        new EmptyBorder(16, 16, 16, 16)));

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

    private JPanel createTablePanel(
            DefaultTableModel model) {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(Color.WHITE);
        panel.setBorder(
                new EmptyBorder(12, 12, 12, 12));

        JTable table = new JTable(model);

        table.setRowHeight(29);
        table.setFont(
                new Font("Arial", Font.PLAIN, 13));

        table.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13));

        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER);

        return panel;
    }

    private DefaultTableModel createModel(
            String... columnKeys) {

        String[] columns = new String[columnKeys.length];

        for (int i = 0; i < columnKeys.length; i++) {
            columns[i] = t(columnKeys[i]);
        }

        return new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };
    }

    private void styleButton(
            JButton button,
            Color background) {

        button.setFont(
                new Font("Arial", Font.BOLD, 12));

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

    // ---------------------------------------------------------
    // LOAD REPORTS
    // ---------------------------------------------------------

    private void loadReports() {

        refreshButton.setEnabled(false);
        exportButton.setEnabled(false);

        refreshButton.setText(
                t("reports.loading"));

        new SwingWorker<ReportData, Void>() {

            @Override
            protected ReportData doInBackground()
                    throws Exception {

                List<Booking> bookings =
                        bookingService.getAllBookings();

                List<Equipment> equipment =
                        equipmentService.getAllEquipment();

                return new ReportData(
                        bookings,
                        equipment);
            }

            @Override
            protected void done() {

                try {

                    ReportData data = get();

                    updateDashboard(data);

                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern(
                                    "dd MMM yyyy HH:mm:ss",
                                    LanguageManager.getLocale());

                    lastUpdatedLabel.setText(
                            t("reports.lastUpdated")
                                    + " "
                                    + LocalDateTime.now()
                                    .format(formatter));

                } catch (InterruptedException ex) {

                    Thread.currentThread().interrupt();

                    showError(
                            t("reports.interrupted"));

                } catch (ExecutionException ex) {

                    showError(
                            t("reports.loadError")
                                    + " "
                                    + getErrorMessage(ex));

                } finally {

                    refreshButton.setEnabled(true);
                    exportButton.setEnabled(true);

                    refreshButton.setText(
                            t("reports.refresh"));
                }
            }

        }.execute();
    }

    private void updateDashboard(ReportData data) {

        List<Booking> bookings = data.bookings;
        List<Equipment> equipment = data.equipment;

        StreamReportService.StreamReport streamReport =
                streamReportService.generateReport(
                        bookings,
                        equipment);

        // Preserve the Stream API console demonstrations.
        streamReport.firstActiveBooking().ifPresentOrElse(
                booking -> System.out.println(
                        "First active booking: "
                                + booking.getBookingId()),
                () -> System.out.println(
                        "No active bookings found."));

        System.out.println(
                "\n========== STREAM REPORTS ==========");

        System.out.println("Top five rented equipment:");

        streamReport.topFive().forEach(item ->
                System.out.println(
                        item.equipmentName()
                                + " - "
                                + item.rentalCount()
                                + " rentals"));

        System.out.println(
                "Bookings per campus: "
                        + streamReport.bookingsPerCampus());

        System.out.println(
                "Revenue per campus: "
                        + streamReport.revenuePerCampus());

        System.out.println(
                "Revenue per category: "
                        + streamReport.revenuePerCategory());

        System.out.println(
                "On-time bookings: "
                        + streamReport.onTimeAndOverdue()
                        .get(false).size());

        System.out.println(
                "Overdue bookings: "
                        + streamReport.onTimeAndOverdue()
                        .get(true).size());

        System.out.println(
                "Total revenue: R"
                        + streamReport.totalRevenue());

        System.out.println(
                "Average equipment daily rate: R"
                        + streamReport.averageDailyRate());

        System.out.println(
                "====================================");

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

    // ---------------------------------------------------------
    // BOOKING STATUS REPORT
    // ---------------------------------------------------------

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
                        Collectors.counting()));

        counts.forEach((status, count) ->
                statusModel.addRow(new Object[]{
                        translateBookingStatus(status),
                        count
                }));
    }

    private String translateBookingStatus(String status) {

        if ("UNKNOWN".equals(status)) {
            return t("reports.unknown");
        }

        return t("booking.status." + status);
    }

    // ---------------------------------------------------------
    // EQUIPMENT AVAILABILITY REPORT
    // ---------------------------------------------------------

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
                        Collectors.counting()));

        counts.forEach((status, count) ->
                availabilityModel.addRow(new Object[]{
                        "UNKNOWN".equals(status)
                                ? t("reports.unknown")
                                : t("reports.availability." + status),
                        count
                }));
    }

    // ---------------------------------------------------------
    // TOP RENTED EQUIPMENT
    // ---------------------------------------------------------

    private void updateTopEquipmentFromStreams(
            StreamReportService.StreamReport report) {

        topEquipmentModel.setRowCount(0);

        report.topFive().forEach(item ->
                topEquipmentModel.addRow(new Object[]{
                        item.equipmentName(),
                        item.rentalCount()
                }));
    }

    // ---------------------------------------------------------
    // CATEGORY REVENUE
    // ---------------------------------------------------------

    private void updateCategoryRevenue(
            List<Booking> bookings,
            List<Equipment> equipment) {

        categoryRevenueModel.setRowCount(0);

        Map<Integer, Equipment> equipmentMap =
                equipment.stream()
                        .collect(Collectors.toMap(
                                Equipment::getEquipmentId,
                                e -> e,
                                (first, second) -> first));

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
                                        Booking::getTotalCost)));

        revenue.forEach((category, amount) ->
                categoryRevenueModel.addRow(new Object[]{
                        "UNKNOWN".equals(category)
                                ? t("reports.unknown")
                                : t("reports.category."
                                + category.toUpperCase(
                                Locale.ROOT)),
                        currencyFormat.format(amount)
                }));
    }

    // ---------------------------------------------------------
    // CAMPUS REVENUE
    // ---------------------------------------------------------

    private void updateCampusRevenue(
            List<Booking> bookings,
            List<Equipment> equipment) {

        campusRevenueModel.setRowCount(0);

        Map<Integer, Equipment> equipmentMap =
                equipment.stream()
                        .collect(Collectors.toMap(
                                Equipment::getEquipmentId,
                                e -> e,
                                (first, second) -> first));

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
                                        Booking::getTotalCost)));

        revenue.forEach((campusId, amount) ->
                campusRevenueModel.addRow(new Object[]{
                        campusId,
                        currencyFormat.format(amount)
                }));
    }

    // ---------------------------------------------------------
    // ON-TIME / OVERDUE REPORT
    // ---------------------------------------------------------

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
                                Collectors.counting()));

        overdueModel.addRow(new Object[]{
                t("reports.overdue"),
                timing.get(true)
        });

        overdueModel.addRow(new Object[]{
                t("reports.notOverdue"),
                timing.get(false)
        });
    }

    // ---------------------------------------------------------
    // ADVANCED STREAM API DEMONSTRATIONS
    // ---------------------------------------------------------

    private void demonstrateAdvancedStreams(
            List<Booking> bookings) {

        Optional<Booking> firstBooking =
                bookings.stream().findFirst();

        firstBooking.ifPresentOrElse(
                booking -> System.out.println(
                        "First booking ID: "
                                + booking.getBookingId()),
                () -> System.out.println(
                        "No bookings found."));

        // REDUCE
        double totalValue = bookings.stream()
                .filter(b -> !"CANCELLED".equalsIgnoreCase(
                        b.getBookingStatus()))
                .map(Booking::getTotalCost)
                .reduce(0.0, Double::sum);

        System.out.println(
                "Total value using reduce: "
                        + currencyFormat.format(totalValue));

        // PRIMITIVE STREAM
        double averageValue = bookings.stream()
                .filter(b -> !"CANCELLED".equalsIgnoreCase(
                        b.getBookingStatus()))
                .mapToDouble(Booking::getTotalCost)
                .average()
                .orElse(0.0);

        System.out.println(
                "Average booking value: "
                        + currencyFormat.format(averageValue));

        // FLATMAP
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
                "Total rental-day entries: "
                        + rentalDays.size());
    }

    // ---------------------------------------------------------
    // SEQUENTIAL / PARALLEL PERFORMANCE TEST
    // ---------------------------------------------------------

    private void benchmarkStreams() {

        List<SyntheticBooking> syntheticBookings =
                IntStream.range(0, 100_000)
                        .mapToObj(i -> new SyntheticBooking(
                                i % 20,
                                100.0 + (i % 10),
                                i % 5 == 0))
                        .toList();

        long sequentialStart = System.nanoTime();

        Map<Integer, Double> sequentialResult =
                syntheticBookings.stream()
                        .collect(Collectors.groupingBy(
                                SyntheticBooking::equipmentId,
                                Collectors.summingDouble(
                                        SyntheticBooking::amount)));

        long sequentialTime =
                System.nanoTime() - sequentialStart;

        long parallelStart = System.nanoTime();

        Map<Integer, Double> parallelResult =
                syntheticBookings.parallelStream()
                        .collect(Collectors.groupingBy(
                                SyntheticBooking::equipmentId,
                                Collectors.summingDouble(
                                        SyntheticBooking::amount)));

        long parallelTime =
                System.nanoTime() - parallelStart;

        System.out.println(
                "\n--- STREAM PERFORMANCE TEST ---");

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

    // ---------------------------------------------------------
    // CSV EXPORT
    // ---------------------------------------------------------

    private void exportReports() {

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle(
                t("reports.exportTitle"));

        chooser.setSelectedFile(
                new java.io.File("CampusRent_Reports.csv"));

        if (chooser.showSaveDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();

        if (!path.toString()
                .toLowerCase(Locale.ROOT)
                .endsWith(".csv")) {

            path = Path.of(path.toString() + ".csv");
        }

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             path,
                             StandardCharsets.UTF_8)) {

            writeTable(
                    writer,
                    t("reports.tab.bookingStatus"),
                    statusModel);

            writeTable(
                    writer,
                    t("reports.tab.availability"),
                    availabilityModel);

            writeTable(
                    writer,
                    t("reports.tab.topEquipment"),
                    topEquipmentModel);

            writeTable(
                    writer,
                    t("reports.tab.categoryRevenue"),
                    categoryRevenueModel);

            writeTable(
                    writer,
                    t("reports.tab.campusRevenue"),
                    campusRevenueModel);

            writeTable(
                    writer,
                    t("reports.tab.overdue"),
                    overdueModel);

            JOptionPane.showMessageDialog(
                    this,
                    t("reports.exportSuccess")
                            + "\n"
                            + path.toAbsolutePath(),
                    t("reports.exportComplete"),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {

            showError(
                    t("reports.exportError")
                            + " " + ex.getMessage());
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

                Object value =
                        model.getValueAt(row, column);

                writer.write(
                        escapeCsv(
                                value == null
                                        ? ""
                                        : value.toString()));
            }

            writer.newLine();
        }
    }

    private String escapeCsv(String value) {

        return "\""
                + value.replace("\"", "\"\"")
                + "\"";
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
                t("reports.errorTitle"),
                JOptionPane.ERROR_MESSAGE);
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
