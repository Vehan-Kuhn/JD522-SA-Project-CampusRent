
package za.ac.ctu.campusrent.gui;

import za.ac.ctu.campusrent.model.Booking;
import za.ac.ctu.campusrent.model.User;
import za.ac.ctu.campusrent.service.BookingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class ReturnsPanel extends JPanel {

    private final User loggedInUser;
    private final BookingService bookingService = new BookingService();

    private final ReturnTableModel tableModel = new ReturnTableModel();
    private final JTable returnsTable = new JTable(tableModel);

    private final JLabel statusLabel = new JLabel();
    private final JButton completeButton = new JButton();
    private final JButton refreshButton = new JButton();

    private final NumberFormat currency =
            NumberFormat.getCurrencyInstance(
                    Locale.forLanguageTag("en-ZA"));

    public ReturnsPanel(User loggedInUser) {

        if (loggedInUser == null) {
            throw new IllegalArgumentException(
                    "A logged-in user is required.");
        }

        this.loggedInUser = loggedInUser;

        setLayout(new BorderLayout(0, 14));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(22, 24, 22, 24));

        add(createHeader(), BorderLayout.NORTH);
        add(createTable(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadReturns();
    }

    private JPanel createHeader() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel heading = new JLabel(
                LanguageManager.text("returns.heading"));

        heading.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel subtitle = new JLabel(
                LanguageManager.text("returns.subtitle"));

        subtitle.setForeground(Color.GRAY);

        JPanel text = new JPanel(
                new GridLayout(0, 1, 0, 5));

        text.setOpaque(false);
        text.add(heading);
        text.add(subtitle);

        panel.add(text, BorderLayout.WEST);

        return panel;
    }

    private JPanel createTable() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 236)),
                        new EmptyBorder(12, 12, 12, 12)));

        returnsTable.setRowHeight(34);

        returnsTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        returnsTable.setAutoCreateRowSorter(true);

        returnsTable.getTableHeader().setReorderingAllowed(false);

        panel.add(
                new JScrollPane(returnsTable),
                BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooter() {

        JPanel panel = new JPanel(
                new BorderLayout(10, 8));

        panel.setOpaque(false);

        completeButton.setText(
                LanguageManager.text("returns.complete"));

        refreshButton.setText(
                LanguageManager.text("common.refresh"));

        completeButton.addActionListener(
                e -> completeSelectedReturn());

        refreshButton.addActionListener(
                e -> loadReturns());

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0));

        actions.setOpaque(false);
        actions.add(completeButton);
        actions.add(refreshButton);

        panel.add(actions, BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadReturns() {

        setBusy(true);

        statusLabel.setText(
                LanguageManager.text("returns.loading"));

        SwingWorker<List<Booking>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<Booking> doInBackground()
                            throws Exception {

                        bookingService.updateOverdueBookings();

                        return bookingService.getAllBookings()
                                .stream()
                                .filter(booking ->
                                        "ACTIVE".equalsIgnoreCase(
                                                booking.getBookingStatus())
                                                || "OVERDUE".equalsIgnoreCase(
                                                booking.getBookingStatus()))
                                .toList();
                    }

                    @Override
                    protected void done() {

                        try {
                            List<Booking> bookings = get();

                            tableModel.setBookings(bookings);

                            statusLabel.setText(
                                    LanguageManager.text("returns.count")
                                            .replace(
                                                    "{count}",
                                                    String.valueOf(bookings.size())));

                        } catch (Exception ex) {

                            statusLabel.setText(
                                    LanguageManager.text("returns.loadError"));

                            showError(rootMessage(ex));

                        } finally {
                            setBusy(false);
                        }
                    }
                };

        worker.execute();
    }

    private void completeSelectedReturn() {

        int viewRow = returnsTable.getSelectedRow();

        if (viewRow < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    LanguageManager.text("returns.selectFirst"),
                    LanguageManager.text("returns.heading"),
                    JOptionPane.INFORMATION_MESSAGE);

            return;
        }

        int modelRow =
                returnsTable.convertRowIndexToModel(viewRow);

        Booking booking = tableModel.getBookingAt(modelRow);

        if (booking == null) {
            return;
        }

        double lateFee =
                tableModel.getEstimatedLateFee(booking);

        String message =
                LanguageManager.text("returns.confirmBooking")
                        + booking.getBookingId()
                        + "?\n"
                        + LanguageManager.text("returns.returnDate")
                        + " " + LocalDate.now()
                        + "\n"
                        + LanguageManager.text("returns.estimatedFee")
                        + " " + currency.format(lateFee)
                        + "\n\n"
                        + LanguageManager.text("returns.confirmNote");

        int choice = JOptionPane.showConfirmDialog(
                this,
                message,
                LanguageManager.text("returns.confirmTitle"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true);

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Boolean doInBackground()
                            throws Exception {

                        return bookingService.completeBooking(
                                booking.getBookingId(),
                                LocalDate.now());
                    }

                    @Override
                    protected void done() {

                        try {

                            if (get()) {

                                JOptionPane.showMessageDialog(
                                        ReturnsPanel.this,
                                        LanguageManager.text(
                                                        "returns.completed")
                                                .replace(
                                                        "{id}",
                                                        String.valueOf(
                                                                booking.getBookingId())),
                                        LanguageManager.text(
                                                "returns.completedTitle"),
                                        JOptionPane.INFORMATION_MESSAGE);

                                loadReturns();

                            } else {

                                showError(
                                        LanguageManager.text(
                                                "returns.failed"));

                                setBusy(false);
                            }

                        } catch (Exception ex) {

                            showError(rootMessage(ex));
                            setBusy(false);
                        }
                    }
                };

        worker.execute();
    }

    private void setBusy(boolean busy) {

        completeButton.setEnabled(!busy);
        refreshButton.setEnabled(!busy);
        returnsTable.setEnabled(!busy);

        setCursor(
                busy
                        ? Cursor.getPredefinedCursor(
                        Cursor.WAIT_CURSOR)
                        : Cursor.getDefaultCursor());
    }

    private String rootMessage(Throwable throwable) {

        Throwable current = throwable;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() == null
                ? LanguageManager.text("common.unexpectedError")
                : current.getMessage();
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                LanguageManager.text("returns.heading"),
                JOptionPane.WARNING_MESSAGE);
    }
}
