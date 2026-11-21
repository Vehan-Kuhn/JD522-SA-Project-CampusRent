
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

    private final JLabel statusLabel = new JLabel(
            LanguageManager.text("booking.loading"));

    private final JButton createButton = new JButton();
    private final JButton approveButton = new JButton();
    private final JButton activateButton = new JButton();
    private final JButton cancelButton = new JButton();
    private final JButton completeButton = new JButton();
    private final JButton refreshButton = new JButton();

    public BookingPanel(User loggedInUser) {

        if (loggedInUser == null) {
            throw new IllegalArgumentException(
                    "A logged-in user is required.");
        }

        this.loggedInUser = loggedInUser;

        this.role = loggedInUser.getUserRole()
                .trim()
                .toUpperCase(Locale.ROOT);

        this.student = "STUDENT".equals(role);

        this.staffOrAdmin =
                "STAFF".equals(role) || "ADMIN".equals(role);

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

    // ---------------------------------------------------------
    // HEADER
    // ---------------------------------------------------------

    private JPanel createHeader() {

        JPanel panel = new JPanel(new BorderLayout(10, 12));
        panel.setOpaque(false);

        JLabel heading = new JLabel(
                LanguageManager.text("booking.heading"));

        heading.setFont(new Font("Arial", Font.BOLD, 24));

        panel.add(heading, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0));

        toolbar.setOpaque(false);

        toolbar.add(new JLabel(
                LanguageManager.text("common.search")));

        toolbar.add(searchField);

        createButton.setText(
                LanguageManager.text("booking.create"));

        createButton.addActionListener(e -> openCreateDialog());

        toolbar.add(createButton);

        panel.add(toolbar, BorderLayout.CENTER);

        return panel;
    }

    // ---------------------------------------------------------
    // TABLE
    // ---------------------------------------------------------

    private JPanel createTableSection() {

        JPanel panel = new JPanel(new BorderLayout());

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 236)),
                        new EmptyBorder(12, 12, 12, 12)));

        bookingTable.setRowHeight(32);

        bookingTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        bookingTable.setAutoCreateRowSorter(true);

        bookingTable.getTableHeader().setReorderingAllowed(false);

        panel.add(
                new JScrollPane(bookingTable),
                BorderLayout.CENTER);

        return panel;
    }

    // ---------------------------------------------------------
    // FOOTER AND ACTION BUTTONS
    // ---------------------------------------------------------

    private JPanel createFooter() {

        JPanel panel = new JPanel(new BorderLayout(10, 8));
        panel.setOpaque(false);

        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 0));

        actions.setOpaque(false);

        // Translate button text.
        approveButton.setText(
                LanguageManager.text("booking.approve"));

        activateButton.setText(
                LanguageManager.text("booking.activate"));

        completeButton.setText(
                LanguageManager.text("booking.completeReturn"));

        cancelButton.setText(
                LanguageManager.text("booking.cancel"));

        refreshButton.setText(
                LanguageManager.text("common.refresh"));

        if (staffOrAdmin) {
            actions.add(approveButton);
            actions.add(activateButton);
            actions.add(completeButton);
        }

        actions.add(cancelButton);
        actions.add(refreshButton);

        // Button actions.
        approveButton.addActionListener(
                e -> performAction("approve"));

        activateButton.addActionListener(
                e -> performAction("activate"));

        cancelButton.addActionListener(
                e -> performAction("cancel"));

        completeButton.addActionListener(
                e -> performAction("complete"));

        refreshButton.addActionListener(
                e -> loadBookings());

        panel.add(actions, BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    // ---------------------------------------------------------
    // TABLE SEARCH
    // ---------------------------------------------------------

    private void configureTable() {

        TableRowSorter<BookingTableModel> sorter =
                new TableRowSorter<>(tableModel);

        bookingTable.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {

                    private void update() {

                        String text = searchField.getText().trim();

                        sorter.setRowFilter(
                                text.isEmpty()
                                        ? null
                                        : RowFilter.regexFilter(
                                        "(?i)"
                                                + java.util.regex.Pattern.quote(text)));
                    }

                    @Override
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e) {
                        update();
                    }

                    @Override
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e) {
                        update();
                    }

                    @Override
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e) {
                        update();
                    }
                });
    }

    // ---------------------------------------------------------
    // TABLE SELECTION
    // ---------------------------------------------------------

    private void configureSelectionHandling() {

        bookingTable.getSelectionModel().addListSelectionListener(
                (ListSelectionEvent event) -> {

                    if (!event.getValueIsAdjusting()) {
                        updateActionAvailability();
                    }
                });
    }

    // ---------------------------------------------------------
    // CREATE BOOKING
    // ---------------------------------------------------------

    private void openCreateDialog() {

        CreateBookingDialog dialog = new CreateBookingDialog(
                SwingUtilities.getWindowAncestor(this),
                loggedInUser,
                staffOrAdmin,
                this::loadBookings);

        dialog.setVisible(true);
    }

    // ---------------------------------------------------------
    // LOAD BOOKINGS
    // ---------------------------------------------------------

    private void loadBookings() {

        statusLabel.setText(
                LanguageManager.text("booking.loading"));

        setBusy(true);

        SwingWorker<List<Booking>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<Booking> doInBackground()
                            throws Exception {

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
                                    bookings.size() + " "
                                            + LanguageManager.text("booking.found")
                                            + " "
                                            + LanguageManager.text("booking.selectPrompt"));

                        } catch (Exception ex) {

                            statusLabel.setText(
                                    LanguageManager.text("booking.loadError"));

                            showError(rootMessage(ex));

                        } finally {

                            setBusy(false);
                            updateActionAvailability();
                        }
                    }
                };

        worker.execute();
    }

    // ---------------------------------------------------------
    // ACTION BUTTON AVAILABILITY
    // ---------------------------------------------------------

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

        approveButton.setEnabled(
                staffOrAdmin && pending);

        activateButton.setEnabled(
                staffOrAdmin && approved);

        completeButton.setEnabled(
                staffOrAdmin && (active || overdue));

        // Only pending and approved bookings can be cancelled.
        cancelButton.setEnabled(
                hasSelection && (pending || approved));

        createButton.setEnabled(true);
        refreshButton.setEnabled(true);
    }

    // ---------------------------------------------------------
    // PERFORM BOOKING ACTION
    // ---------------------------------------------------------

    private void performAction(String action) {

        Booking selected = getSelectedBooking();

        if (selected == null) {

            JOptionPane.showMessageDialog(
                    this,
                    LanguageManager.text("booking.selectFirst"),
                    LanguageManager.text("booking.heading"),
                    JOptionPane.INFORMATION_MESSAGE);

            return;
        }

        String reason = null;

        String confirmation = switch (action) {

            case "approve" ->
                    LanguageManager.text("booking.confirmApprove")
                            + selected.getBookingId() + "?";

            case "activate" ->
                    LanguageManager.text("booking.confirmActivate")
                            + selected.getBookingId()
                            + "?\n"
                            + LanguageManager.text("booking.handoverWarning");

            case "cancel" ->
                    LanguageManager.text("booking.confirmCancel")
                            + selected.getBookingId() + "?";

            case "complete" ->
                    LanguageManager.text("booking.confirmComplete")
                            + selected.getBookingId()
                            + " ("
                            + LocalDate.now()
                            + ")?";

            default ->
                    LanguageManager.text("booking.confirmDefault");
        };

        // Cancellation requires a reason.
        if ("cancel".equals(action)) {

            JTextArea reasonArea = new JTextArea(4, 28);

            reasonArea.setLineWrap(true);
            reasonArea.setWrapStyleWord(true);

            JScrollPane scroll = new JScrollPane(reasonArea);

            JPanel input = new JPanel(
                    new BorderLayout(5, 5));

            input.add(
                    new JLabel(
                            LanguageManager.text("booking.cancelReason")),
                    BorderLayout.NORTH);

            input.add(scroll, BorderLayout.CENTER);

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    input,
                    LanguageManager.text("booking.cancelTitle")
                            + selected.getBookingId(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (choice != JOptionPane.OK_OPTION) {
                return;
            }

            reason = reasonArea.getText().trim();

            if (reason.isBlank()) {

                showError(
                        LanguageManager.text("booking.reasonRequired"));

                return;
            }

            confirmation += "\n\n"
                    + LanguageManager.text("booking.cancelReason")
                    + " "
                    + reason;

        } else {

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    confirmation,
                    LanguageManager.text("booking.confirmTitle"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        final String cancellationReason = reason;

        setBusy(true);

        // Run the database operation in the background.
        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Boolean doInBackground()
                            throws Exception {

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
                                        LanguageManager.text("booking.noChange"));

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
                                        LanguageManager.text("booking.auditWarning")
                                                + "\n"
                                                + rootMessage(auditException),
                                        LanguageManager.text(
                                                "booking.auditWarningTitle"),
                                        JOptionPane.WARNING_MESSAGE);
                            }

                            JOptionPane.showMessageDialog(
                                    BookingPanel.this,
                                    successMessage(
                                            action,
                                            selected.getBookingId()),
                                    LanguageManager.text("app.title"),
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

    // ---------------------------------------------------------
    // AUDIT LOG
    // ---------------------------------------------------------

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
                    LanguageManager.text("booking.auditApproved");

            case "activate" ->
                    LanguageManager.text("booking.auditActivated");

            case "cancel" ->
                    LanguageManager.text("booking.auditCancelled")
                            + " "
                            + cancellationReason;

            case "complete" ->
                    LanguageManager.text("booking.auditCompleted")
                            + " "
                            + LocalDate.now() + ".";

            default ->
                    LanguageManager.text("booking.auditDefault");
        };

        log.setDetails(details);

        auditLogService.addAuditLog(log);
    }

    // ---------------------------------------------------------
    // SUCCESS MESSAGES
    // ---------------------------------------------------------

    private String successMessage(String action, int bookingId) {

        String key = switch (action) {

            case "approve" -> "booking.approved";

            case "activate" -> "booking.activated";

            case "cancel" -> "booking.cancelled";

            case "complete" -> "booking.returned";

            default -> "booking.actionCompleted";
        };

        return LanguageManager.text(key)
                .replace("{id}", String.valueOf(bookingId));
    }

    // ---------------------------------------------------------
    // SELECTED BOOKING
    // ---------------------------------------------------------

    private Booking getSelectedBooking() {

        int viewRow = bookingTable.getSelectedRow();

        if (viewRow < 0) {
            return null;
        }

        int modelRow = bookingTable.convertRowIndexToModel(viewRow);

        return tableModel.getBookingAt(modelRow);
    }

    // ---------------------------------------------------------
    // BUSY STATE
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // ERROR HANDLING
    // ---------------------------------------------------------

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
                LanguageManager.text("booking.errorTitle"),
                JOptionPane.WARNING_MESSAGE);
    }
}
