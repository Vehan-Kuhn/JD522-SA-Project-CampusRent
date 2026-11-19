package za.ac.ctu.campusrent.gui;
import za.ac.ctu.campusrent.model.Booking;
import za.ac.ctu.campusrent.model.User;
import za.ac.ctu.campusrent.service.BookingService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/** Staff/Admin screen for processing equipment returns. */
public class ReturnsPanel extends JPanel {

    private final User loggedInUser;
    private final BookingService bookingService = new BookingService();
    private final ReturnTableModel tableModel = new ReturnTableModel();
    private final JTable returnsTable = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel("Loading returns...");
    private final JButton completeButton = new JButton("Complete Selected Return");
    private final JButton refreshButton = new JButton("Refresh");

    public ReturnsPanel(User loggedInUser) {
        if (loggedInUser == null) {
            throw new IllegalArgumentException("A logged-in user is required.");
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

        JLabel heading = new JLabel("Returns Management");
        heading.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel subtitle = new JLabel(
                "View equipment currently on loan, identify overdue items and process returns.");
        subtitle.setForeground(Color.GRAY);

        JPanel text = new JPanel(new GridLayout(0, 1, 0, 5));
        text.setOpaque(false);
        text.add(heading);
        text.add(subtitle);
        panel.add(text, BorderLayout.WEST);
        return panel;
    }

    private JPanel createTable() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 236)),
                new EmptyBorder(12, 12, 12, 12)));

        returnsTable.setRowHeight(34);
        returnsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        returnsTable.setAutoCreateRowSorter(true);
        returnsTable.getTableHeader().setReorderingAllowed(false);

        panel.add(new JScrollPane(returnsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new BorderLayout(10, 8));
        panel.setOpaque(false);

        completeButton.addActionListener(e -> completeSelectedReturn());
        refreshButton.addActionListener(e -> loadReturns());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.add(completeButton);
        actions.add(refreshButton);

        panel.add(actions, BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadReturns() {
        setBusy(true);
        statusLabel.setText("Checking overdue bookings and loading returns...");

        SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                bookingService.updateOverdueBookings();
                return bookingService.getAllBookings().stream()
                        .filter(booking ->
                                "ACTIVE".equalsIgnoreCase(booking.getBookingStatus())
                                        || "OVERDUE".equalsIgnoreCase(booking.getBookingStatus()))
                        .toList();
            }

            @Override
            protected void done() {
                try {
                    List<Booking> bookings = get();
                    tableModel.setBookings(bookings);
                    statusLabel.setText(bookings.size()
                            + " item(s) currently awaiting return. Late fee estimate: R50 per overdue day.");
                } catch (Exception ex) {
                    statusLabel.setText("Could not load returns.");
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
            JOptionPane.showMessageDialog(this, "Select a return record first.",
                    "Returns Management", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int modelRow = returnsTable.convertRowIndexToModel(viewRow);
        Booking booking = tableModel.getBookingAt(modelRow);
        if (booking == null) {
            return;
        }

        double lateFee = tableModel.getEstimatedLateFee(booking);
        String message = "Complete return for booking #" + booking.getBookingId()
                + "?\nReturn date: " + LocalDate.now()
                + "\nEstimated overdue fee: R" + String.format(java.util.Locale.ROOT, "%.2f", lateFee)
                + "\n\nThe booking will be marked COMPLETED and equipment availability refreshed.";

        int choice = JOptionPane.showConfirmDialog(this, message,
                "Confirm Equipment Return", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        setBusy(true);
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return bookingService.completeBooking(booking.getBookingId(), LocalDate.now());
            }

            @Override
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(ReturnsPanel.this,
                                "Return completed successfully.\nBooking #" + booking.getBookingId()
                                        + " is now COMPLETED.",
                                "Return Completed", JOptionPane.INFORMATION_MESSAGE);
                        loadReturns();
                    } else {
                        showError("The return could not be completed. Refresh and try again.");
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
        setCursor(busy ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor());
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null
                ? "An unexpected error occurred." : current.getMessage();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Returns Management", JOptionPane.WARNING_MESSAGE);
    }
}

