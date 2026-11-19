
package za.ac.ctu.campusrent.gui;

import za.ac.ctu.campusrent.model.AuditLog;
import za.ac.ctu.campusrent.model.Booking;
import za.ac.ctu.campusrent.model.User;
import za.ac.ctu.campusrent.service.AuditLogService;
import za.ac.ctu.campusrent.service.BookingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

/**
 * Booking management screen with role-aware, status-sensitive actions.
 * Students can view and cancel their own pending/approved bookings.
 * Staff and administrators can approve, activate, cancel and complete bookings.
 */
public class BookingPanel extends JPanel {

    private final User loggedInUser;
    private final String role;
    private final boolean student;
    private final boolean staffOrAdmin;

    private final BookingService bookingService = new BookingService();
    private final AuditLogService auditLogService = new AuditLogService();

    private final BookingTableModel tableModel = new BookingTableModel();
    private final JTable bookingTable = new JTable(tableModel);
    private final JTextField searchField = new JTextField(22);
    private final JLabel statusLabel = new JLabel("Loading bookings...");

    private final JButton createButton = new JButton("+ Create Booking");
    private final JButton approveButton = new JButton("Approve");
    private final JButton activateButton = new JButton("Activate");
    private final JButton cancelButton = new JButton("Cancel Booking");
    private final JButton completeButton = new JButton("Complete Return");
    private final JButton refreshButton = new JButton("Refresh");

    public BookingPanel(User loggedInUser) {
        if (loggedInUser == null) {
            throw new IllegalArgumentException("A logged-in user is required.");
        }

        this.loggedInUser = loggedInUser;
        this.role = loggedInUser.getUserRole().trim().toUpperCase(Locale.ROOT);
        this.student = "STUDENT".equals(role);
        this.staffOrAdmin = "STAFF".equals(role) || "ADMIN".equals(role);

        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(22, 24, 22, 24));

        add(createHeader(), BorderLayout.NORTH);
        add(createTableSection(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        configureTable();
        configureSelectionHandling();
        updateActionAvailability();
        loadBookings();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setOpaque(false);

        JLabel heading = new JLabel("Booking Management");
        heading.setFont(new Font("Arial", Font.BOLD, 24));
        panel.add(heading, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.add(new JLabel("Search:"));
        toolbar.add(searchField);
        toolbar.add(createButton);

        createButton.addActionListener(e -> openCreateDialog());
        panel.add(toolbar, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 236)),
                new EmptyBorder(12, 12, 12, 12)));

        bookingTable.setRowHeight(32);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.setAutoCreateRowSorter(true);
        bookingTable.getTableHeader().setReorderingAllowed(false);
        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new BorderLayout(10, 8));
        panel.setOpaque(false);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);

        if (staffOrAdmin) {
            actions.add(approveButton);
            actions.add(activateButton);
            actions.add(completeButton);
        }

        actions.add(cancelButton);
        actions.add(refreshButton);

        approveButton.addActionListener(e -> performAction("approve"));
        activateButton.addActionListener(e -> performAction("activate"));
        cancelButton.addActionListener(e -> performAction("cancel"));
        completeButton.addActionListener(e -> performAction("complete"));
        refreshButton.addActionListener(e -> loadBookings());

        panel.add(actions, BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.SOUTH);
        return panel;
    }

    private void configureTable() {
        TableRowSorter<BookingTableModel> sorter =
                new TableRowSorter<>(tableModel);

        bookingTable.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {

                    private void update() {
                        String text = searchField.getText().trim();

                        sorter.setRowFilter(text.isEmpty() ? null
                                : RowFilter.regexFilter("(?i)"
                                + java.util.regex.Pattern.quote(text)));
                    }

                    @Override
                    public void insertUpdate(javax.swing.event.DocumentEvent e) {
                        update();
                    }

                    @Override
                    public void removeUpdate(javax.swing.event.DocumentEvent e) {
                        update();
                    }

                    @Override
                    public void changedUpdate(javax.swing.event.DocumentEvent e) {
                        update();
                    }
                });
    }

    private void configureSelectionHandling() {
        bookingTable.getSelectionModel().addListSelectionListener(
                (ListSelectionEvent event) -> {
                    if (!event.getValueIsAdjusting()) {
                        updateActionAvailability();
                    }
                });
    }

    private void openCreateDialog() {
        CreateBookingDialog dialog = new CreateBookingDialog(
                SwingUtilities.getWindowAncestor(this),
                loggedInUser,
                staffOrAdmin,
                this::loadBookings);

        dialog.setVisible(true);
    }

    private void loadBookings() {
        statusLabel.setText("Loading bookings...");
        setBusy(true);

        SwingWorker<List<Booking>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<Booking> doInBackground() throws Exception {
                return student
                        ? bookingService.getBookingsByUserId(
                        loggedInUser.getUserId())
                        : bookingService.getAllBookings();
            }

            @Override
            protected void done() {
                try {
                    List<Booking> bookings = get();
                    tableModel.setBookings(bookings);

                    statusLabel.setText(
                            bookings.size()
                                    + " booking(s) found. Select a booking to see available actions.");

                } catch (Exception ex) {
                    statusLabel.setText("Could not load bookings.");
                    showError(rootMessage(ex));

                } finally {
                    setBusy(false);
                    updateActionAvailability();
                }
            }
        };

        worker.execute();
    }

    private void updateActionAvailability() {
        Booking selected = getSelectedBooking();

        boolean hasSelection = selected != null;

        String status = hasSelection
                && selected.getBookingStatus() != null
                ? selected.getBookingStatus()
                .trim()
                .toUpperCase(Locale.ROOT)
                : "";

        boolean pending = "PENDING".equals(status);
        boolean approved = "APPROVED".equals(status);
        boolean active = "ACTIVE".equals(status);
        boolean overdue = "OVERDUE".equals(status);

        approveButton.setEnabled(staffOrAdmin && pending);

        activateButton.setEnabled(staffOrAdmin && approved);

        completeButton.setEnabled(
                staffOrAdmin && (active || overdue));

        // Students can only see their own bookings.
        // Only pending and approved bookings can be cancelled.
        cancelButton.setEnabled(
                hasSelection && (pending || approved));

        createButton.setEnabled(true);
        refreshButton.setEnabled(true);
    }

    private void performAction(String action) {

        Booking selected = getSelectedBooking();

        if (selected == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a booking first.",
                    "Booking Management",
                    JOptionPane.INFORMATION_MESSAGE);

            return;
        }

        String reason = null;

        String confirmation = switch (action) {

            case "approve" ->
                    "Approve booking #" + selected.getBookingId() + "?";

            case "activate" ->
                    "Activate booking #" + selected.getBookingId()
                            + "?\nThis confirms the equipment is being handed to the borrower.";

            case "cancel" ->
                    "Cancel booking #" + selected.getBookingId() + "?";

            case "complete" ->
                    "Complete the return for booking #"
                            + selected.getBookingId()
                            + " using today's date (" + LocalDate.now() + ")?";

            default -> "Continue with this booking action?";
        };

        // Cancellation requires a reason.
        if ("cancel".equals(action)) {

            JTextArea reasonArea = new JTextArea(4, 28);
            reasonArea.setLineWrap(true);
            reasonArea.setWrapStyleWord(true);

            JScrollPane scroll = new JScrollPane(reasonArea);

            JPanel input = new JPanel(new BorderLayout(5, 5));

            input.add(
                    new JLabel("Cancellation reason (required):"),
                    BorderLayout.NORTH);

            input.add(scroll, BorderLayout.CENTER);

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    input,
                    "Cancel Booking #" + selected.getBookingId(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (choice != JOptionPane.OK_OPTION) {
                return;
            }

            reason = reasonArea.getText().trim();

            if (reason.isBlank()) {
                showError(
                        "Please enter a reason for cancelling this booking.");
                return;
            }

            confirmation += "\n\nReason: " + reason;

        } else {

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    confirmation,
                    "Confirm Booking Action",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        final String cancellationReason = reason;

        setBusy(true);

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() throws Exception {

                return switch (action) {

                    case "approve" ->
                            bookingService.approveBooking(
                                    selected.getBookingId());

                    case "activate" ->
                            bookingService.activateBooking(
                                    selected.getBookingId());

                    case "cancel" ->
                            bookingService.cancelBooking(
                                    selected.getBookingId());

                    case "complete" ->
                            bookingService.completeBooking(
                                    selected.getBookingId(),
                                    LocalDate.now());

                    default -> false;
                };
            }

            @Override
            protected void done() {

                try {

                    if (!get()) {
                        showError(
                                "No booking was changed. Refresh and try again.");

                        setBusy(false);
                        updateActionAvailability();
                        return;
                    }

                    // Booking transaction has committed.
                    // Record the action in the audit log.
                    try {

                        writeAuditLog(
                                action,
                                selected,
                                cancellationReason);

                    } catch (Exception auditException) {

                        JOptionPane.showMessageDialog(
                                BookingPanel.this,
                                "The booking was updated, but its audit entry could not be saved:\n"
                                        + rootMessage(auditException),
                                "Audit Log Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }

                    JOptionPane.showMessageDialog(
                            BookingPanel.this,
                            successMessage(
                                    action,
                                    selected.getBookingId()),
                            "CampusRent",
                            JOptionPane.INFORMATION_MESSAGE);

                    loadBookings();

                } catch (Exception ex) {

                    showError(rootMessage(ex));

                    setBusy(false);
                    updateActionAvailability();
                }
            }
        };

        worker.execute();
    }

    private void writeAuditLog(
            String action,
            Booking booking,
            String cancellationReason) throws Exception {

        AuditLog log = new AuditLog();

        log.setUserId(loggedInUser.getUserId());
        log.setAction(
                action.toUpperCase(Locale.ROOT) + "_BOOKING");

        log.setEntityType("BOOKING");
        log.setEntityId(booking.getBookingId());

        String details = switch (action) {

            case "approve" ->
                    "Booking approved.";

            case "activate" ->
                    "Booking activated; equipment handed over.";

            case "cancel" ->
                    "Booking cancelled. Reason: " + cancellationReason;

            case "complete" ->
                    "Booking return completed on " + LocalDate.now() + ".";

            default ->
                    "Booking action performed.";
        };

        log.setDetails(details);

        auditLogService.addAuditLog(log);
    }

    private String successMessage(String action, int bookingId) {

        return switch (action) {

            case "approve" ->
                    "Booking #" + bookingId + " has been approved.";

            case "activate" ->
                    "Booking #" + bookingId + " is now active.";

            case "cancel" ->
                    "Booking #" + bookingId + " has been cancelled.";

            case "complete" ->
                    "Return for booking #" + bookingId + " has been completed.";

            default ->
                    "Booking action completed successfully.";
        };
    }

    private Booking getSelectedBooking() {

        int viewRow = bookingTable.getSelectedRow();

        if (viewRow < 0) {
            return null;
        }

        int modelRow = bookingTable.convertRowIndexToModel(viewRow);

        return tableModel.getBookingAt(modelRow);
    }

    private void setBusy(boolean busy) {

        createButton.setEnabled(!busy);
        refreshButton.setEnabled(!busy);

        approveButton.setEnabled(!busy);
        activateButton.setEnabled(!busy);
        cancelButton.setEnabled(!busy);
        completeButton.setEnabled(!busy);

        bookingTable.setEnabled(!busy);

        setCursor(
                busy
                        ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                        : Cursor.getDefaultCursor());
    }

    private String rootMessage(Throwable throwable) {

        Throwable current = throwable;

        while (current.getCause() != null) {
            current = current.getCause();
        }

        return current.getMessage() == null
                ? "An unexpected error occurred."
                : current.getMessage();
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Booking Error",
                JOptionPane.WARNING_MESSAGE);
    }
}
