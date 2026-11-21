
package za.ac.ctu.campusrent.gui;

import za.ac.ctu.campusrent.model.AuditLog;
import za.ac.ctu.campusrent.model.Booking;
import za.ac.ctu.campusrent.service.AuditLogService;
import za.ac.ctu.campusrent.service.BookingService;
import za.ac.ctu.campusrent.service.EquipmentService;
import za.ac.ctu.campusrent.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class DashboardPanel extends JPanel {

    private final AuditLogService auditLogService;
    private final BookingService bookingService;
    private final EquipmentService equipmentService;
    private final UserService userService;

    private final JLabel equipmentValue;
    private final JLabel activeBookingsValue;
    private final JLabel overdueValue;
    private final JLabel usersValue;

    private final JTextArea activityArea;
    private final JButton refreshButton;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy | HH:mm:ss");

    public DashboardPanel() {

        auditLogService = new AuditLogService();
        bookingService = new BookingService();
        equipmentService = new EquipmentService();
        userService = new UserService();

        setLayout(new BorderLayout(15, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel heading = new JLabel(
                LanguageManager.text("dashboard.heading")
        );
        heading.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel statistics = new JPanel(
                new GridLayout(1, 4, 15, 15)
        );
        statistics.setOpaque(false);

        JPanel equipmentCard = createCard(
                "dashboard.totalEquipment",
                "dashboard.registeredItems"
        );
        equipmentValue = getCardValueLabel(equipmentCard);

        JPanel activeCard = createCard(
                "dashboard.activeBookings",
                "dashboard.currentRentals"
        );
        activeBookingsValue = getCardValueLabel(activeCard);

        JPanel overdueCard = createCard(
                "dashboard.overdueItems",
                "dashboard.requireAttention"
        );
        overdueValue = getCardValueLabel(overdueCard);

        JPanel usersCard = createCard(
                "dashboard.registeredUsers",
                "dashboard.studentsStaff"
        );
        usersValue = getCardValueLabel(usersCard);

        statistics.add(equipmentCard);
        statistics.add(activeCard);
        statistics.add(overdueCard);
        statistics.add(usersCard);

        JPanel center = new JPanel(new BorderLayout(10, 15));
        center.setOpaque(false);

        JLabel activityTitle = new JLabel(
                LanguageManager.text("dashboard.recentActivity")
        );
        activityTitle.setFont(new Font("Arial", Font.BOLD, 18));

        activityArea = new JTextArea(
                LanguageManager.text("dashboard.loadingActivity")
        );

        activityArea.setFont(new Font("Arial", Font.PLAIN, 14));
        activityArea.setEditable(false);
        activityArea.setLineWrap(true);
        activityArea.setWrapStyleWord(true);
        activityArea.setBackground(Color.WHITE);
        activityArea.setBorder(
                new EmptyBorder(20, 20, 20, 20)
        );

        JScrollPane activityScroll = new JScrollPane(activityArea);
        activityScroll.setBorder(BorderFactory.createEmptyBorder());

        refreshButton = new JButton(
                LanguageManager.text("dashboard.refresh")
        );

        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(e -> loadDashboardData());

        JPanel activityHeader = new JPanel(new BorderLayout());
        activityHeader.setOpaque(false);

        activityHeader.add(activityTitle, BorderLayout.WEST);
        activityHeader.add(refreshButton, BorderLayout.EAST);

        center.add(activityHeader, BorderLayout.NORTH);
        center.add(activityScroll, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(0, 20));
        top.setOpaque(false);

        top.add(heading, BorderLayout.NORTH);
        top.add(statistics, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);

        loadDashboardData();
    }

    /**
     * Loads statistics and audit records from MySQL.
     */
    private void loadDashboardData() {

        refreshButton.setEnabled(false);

        SwingWorker<DashboardData, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected DashboardData doInBackground()
                            throws Exception {

                        List<Booking> bookings =
                                bookingService.getAllBookings();

                        int activeCount = 0;
                        int overdueCount = 0;

                        for (Booking booking : bookings) {

                            if ("ACTIVE".equalsIgnoreCase(
                                    booking.getBookingStatus())) {
                                activeCount++;
                            }

                            if ("OVERDUE".equalsIgnoreCase(
                                    booking.getBookingStatus())) {
                                overdueCount++;
                            }
                        }

                        int equipmentCount =
                                equipmentService.getAllEquipment().size();

                        int userCount =
                                userService.getAllUsers().size();

                        List<AuditLog> logs =
                                auditLogService.getAllAuditLogs();

                        return new DashboardData(
                                equipmentCount,
                                activeCount,
                                overdueCount,
                                userCount,
                                logs
                        );
                    }

                    @Override
                    protected void done() {

                        refreshButton.setEnabled(true);

                        try {

                            DashboardData data = get();

                            equipmentValue.setText(
                                    String.valueOf(data.equipmentCount())
                            );

                            activeBookingsValue.setText(
                                    String.valueOf(data.activeCount())
                            );

                            overdueValue.setText(
                                    String.valueOf(data.overdueCount())
                            );

                            usersValue.setText(
                                    String.valueOf(data.userCount())
                            );

                            displayRecentActivity(data.logs());

                        } catch (Exception ex) {

                            activityArea.setText(
                                    LanguageManager.text(
                                            "dashboard.loadError"
                                    ) + "\n\n" + getErrorMessage(ex)
                            );

                            equipmentValue.setText("-");
                            activeBookingsValue.setText("-");
                            overdueValue.setText("-");
                            usersValue.setText("-");
                        }
                    }
                };

        worker.execute();
    }

    /**
     * Displays the latest 15 audit records.
     */
    private void displayRecentActivity(List<AuditLog> logs) {

        if (logs == null || logs.isEmpty()) {

            activityArea.setText(
                    LanguageManager.text("dashboard.welcome")
                            + "\n\n"
                            + LanguageManager.text("dashboard.ready")
                            + "\n\n"
                            + LanguageManager.text("dashboard.noActivity")
            );

            return;
        }

        logs.sort(
                Comparator.comparing(
                        AuditLog::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        StringBuilder output = new StringBuilder();

        output.append(
                LanguageManager.text("dashboard.latestActivity")
        );

        output.append("\n========================================\n\n");

        int displayed = Math.min(15, logs.size());

        for (int i = 0; i < displayed; i++) {

            AuditLog log = logs.get(i);

            output.append(
                    LanguageManager.text("dashboard.activity")
            );

            output.append(" ")
                    .append(i + 1)
                    .append("\n");

            output.append(
                    LanguageManager.text("dashboard.action")
            );

            output.append(": ")
                    .append(safeText(log.getAction()))
                    .append("\n");

            output.append(
                    LanguageManager.text("dashboard.entity")
            );

            output.append(": ")
                    .append(safeText(log.getEntityType()));

            if (log.getEntityId() != null) {
                output.append(" #")
                        .append(log.getEntityId());
            }

            output.append("\n");

            output.append(
                    LanguageManager.text("dashboard.details")
            );

            output.append(": ")
                    .append(safeText(log.getDetails()))
                    .append("\n");

            output.append(
                    LanguageManager.text("dashboard.userId")
            );

            output.append(": ")
                    .append(log.getUserId() == null
                            ? LanguageManager.text("dashboard.system")
                            : log.getUserId())
                    .append("\n");

            output.append(
                    LanguageManager.text("dashboard.date")
            );

            output.append(": ")
                    .append(log.getCreatedAt() == null
                            ? LanguageManager.text("dashboard.unknown")
                            : log.getCreatedAt().format(DATE_FORMAT))
                    .append("\n");

            output.append("----------------------------------------\n\n");
        }

        output.append(
                LanguageManager.text("dashboard.showing")
        );

        output.append(" ")
                .append(displayed)
                .append(" ")
                .append(LanguageManager.text("dashboard.of"))
                .append(" ")
                .append(logs.size())
                .append(" ")
                .append(LanguageManager.text("dashboard.activities"));

        activityArea.setText(output.toString());
        activityArea.setCaretPosition(0);
    }

    private String safeText(String value) {

        return value == null || value.isBlank()
                ? LanguageManager.text("dashboard.noDetails")
                : value;
    }

    private String getErrorMessage(Exception ex) {

        Throwable cause = ex.getCause();

        if (cause != null && cause.getMessage() != null) {
            return cause.getMessage();
        }

        return ex.getMessage() == null
                ? LanguageManager.text("dashboard.unknownError")
                : ex.getMessage();
    }

    private JPanel createCard(
            String titleKey,
            String descriptionKey) {

        JPanel card = new JPanel();

        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 230, 235)
                ),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel titleLabel = new JLabel(
                LanguageManager.text(titleKey)
        );

        titleLabel.setForeground(Color.GRAY);
        titleLabel.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        JLabel valueLabel = new JLabel(
                LanguageManager.text("dashboard.loading")
        );

        valueLabel.setName("dashboardCardValue");
        valueLabel.setForeground(new Color(41, 128, 185));
        valueLabel.setFont(
                new Font("Arial", Font.BOLD, 30)
        );

        JLabel descriptionLabel = new JLabel(
                LanguageManager.text(descriptionKey)
        );

        descriptionLabel.setForeground(Color.GRAY);
        descriptionLabel.setFont(
                new Font("Arial", Font.PLAIN, 11)
        );

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(descriptionLabel);

        return card;
    }

    private JLabel getCardValueLabel(JPanel card) {

        for (Component component : card.getComponents()) {

            if (component instanceof JLabel label
                    && "dashboardCardValue".equals(label.getName())) {
                return label;
            }
        }

        throw new IllegalStateException(
                "Dashboard statistic label could not be found."
        );
    }

    private record DashboardData(
            int equipmentCount,
            int activeCount,
            int overdueCount,
            int userCount,
            List<AuditLog> logs) {
    }
}
