package za.ac.ctu.campusrent.gui;
import za.ac.ctu.campusrent.model.Booking;
import za.ac.ctu.campusrent.model.Equipment;
import za.ac.ctu.campusrent.model.User;
import za.ac.ctu.campusrent.service.BookingService;
import za.ac.ctu.campusrent.service.EquipmentService;
import za.ac.ctu.campusrent.service.UserService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/** Dialog used to create a real booking in the database. */
public class CreateBookingDialog extends JDialog {

    private final User loggedInUser;
    private final boolean staffOrAdmin;
    private final Runnable onCreated;
    private final JComboBox<User> userCombo = new JComboBox<>();
    private final JComboBox<Equipment> equipmentCombo = new JComboBox<>();
    private final JTextField startDateField = new JTextField(LocalDate.now().plusDays(1).toString(), 14);
    private final JTextField dueDateField = new JTextField(LocalDate.now().plusDays(3).toString(), 14);
    private final JTextArea notesArea = new JTextArea(3, 22);
    private final JLabel rateLabel = new JLabel("Select equipment to view its daily rate.");
    private final UserService userService = new UserService();
    private final EquipmentService equipmentService = new EquipmentService();
    private final BookingService bookingService = new BookingService();
    private final NumberFormat currency =
            NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-ZA"));

    public CreateBookingDialog(Window owner, User loggedInUser,
                               boolean staffOrAdmin, Runnable onCreated) {
        super(owner, "Create Equipment Booking", ModalityType.APPLICATION_MODAL);
        this.loggedInUser = loggedInUser;
        this.staffOrAdmin = staffOrAdmin;
        this.onCreated = onCreated;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(560, 510);
        setMinimumSize(new Dimension(520, 470));
        setLocationRelativeTo(owner);
        buildForm();
        loadChoices();
    }

    private void buildForm() {
        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBorder(new EmptyBorder(20, 24, 20, 24));
        content.setBackground(Color.WHITE);
        JLabel heading = new JLabel("Create Equipment Booking");
        heading.setFont(new Font("Arial", Font.BOLD, 22));
        content.add(heading, BorderLayout.NORTH);
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        addField(form, gbc, row++, "Borrower:", userCombo);
        addField(form, gbc, row++, "Equipment:", equipmentCombo);
        addField(form, gbc, row++, "Start date (yyyy-MM-dd):", startDateField);
        addField(form, gbc, row++, "Due date (yyyy-MM-dd):", dueDateField);
        addField(form, gbc, row++, "Daily rate:", rateLabel);

        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        addField(form, gbc, row, "Notes:", notesScroll);
        userCombo.setEnabled(staffOrAdmin);
        equipmentCombo.addActionListener(e -> updateRateLabel());
        content.add(form, BorderLayout.CENTER);
        JButton createButton = new JButton("Create Booking");
        JButton cancelButton = new JButton("Cancel");
        createButton.addActionListener(e -> createBooking(createButton, cancelButton));
        cancelButton.addActionListener(e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(cancelButton);
        actions.add(createButton);
        content.add(actions, BorderLayout.SOUTH);

        setContentPane(content);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, int row,
                          String label, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(component, gbc);
    }

    private void loadChoices() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private List<User> users;
            private List<Equipment> equipment;

            @Override
            protected Void doInBackground() throws Exception {
                users = staffOrAdmin
                        ? userService.getAllUsers().stream()
                        .filter(u -> "ACTIVE".equalsIgnoreCase(u.getAccountStatus()))
                        .toList()
                        : List.of(loggedInUser);
                equipment = equipmentService.getAvailableEquipment();
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    for (User user : users) {
                        userCombo.addItem(user);
                    }
                    for (Equipment item : equipment) {
                        equipmentCombo.addItem(item);
                    }
                    userCombo.setRenderer(new UserRenderer());
                    equipmentCombo.setRenderer(new EquipmentRenderer());

                    if (users.isEmpty()) {
                        JOptionPane.showMessageDialog(CreateBookingDialog.this,
                                "There are no active users available.",
                                "Booking", JOptionPane.WARNING_MESSAGE);
                        dispose();
                    } else if (equipment.isEmpty()) {
                        JOptionPane.showMessageDialog(CreateBookingDialog.this,
                                "There is currently no available equipment.",
                                "Booking", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    }
                    updateRateLabel();
                } catch (Exception ex) {
                    showError("Could not load booking options: " + rootMessage(ex));
                    dispose();
                }
            }
        };
        worker.execute();
    }

    private void updateRateLabel() {
        Equipment selected = (Equipment) equipmentCombo.getSelectedItem();
        rateLabel.setText(selected == null
                ? "Select equipment to view its daily rate."
                : currency.format(selected.getDailyRate()) + " per day");
    }

    private void createBooking(JButton createButton, JButton cancelButton) {
        try {
            User selectedUser = (User) userCombo.getSelectedItem();
            Equipment selectedEquipment = (Equipment) equipmentCombo.getSelectedItem();

            if (selectedUser == null || selectedEquipment == null) {
                throw new IllegalArgumentException("Select a borrower and equipment item.");
            }

            LocalDate startDate = LocalDate.parse(startDateField.getText().trim());
            LocalDate dueDate = LocalDate.parse(dueDateField.getText().trim());

            if (dueDate.isBefore(startDate)) {
                throw new IllegalArgumentException("The due date cannot be before the start date.");
            }
            if (startDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("The start date cannot be in the past.");
            }

            Booking booking = new Booking();
            booking.setUserId(selectedUser.getUserId());
            booking.setEquipmentId(selectedEquipment.getEquipmentId());
            booking.setStartDate(startDate);
            booking.setDueDate(dueDate);
            booking.setNotes(notesArea.getText().trim());

            createButton.setEnabled(false);
            cancelButton.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            SwingWorker<Integer, Void> worker = new SwingWorker<>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return bookingService.addBooking(booking);
                }

                @Override
                protected void done() {
                    setCursor(Cursor.getDefaultCursor());
                    createButton.setEnabled(true);
                    cancelButton.setEnabled(true);
                    try {
                        int id = get();
                        JOptionPane.showMessageDialog(CreateBookingDialog.this,
                                "Booking #" + id + " created successfully.\nStatus: PENDING\nTotal: "
                                        + currency.format(booking.getTotalCost()),
                                "Booking Created", JOptionPane.INFORMATION_MESSAGE);
                        if (onCreated != null) {
                            onCreated.run();
                        }
                        dispose();
                    } catch (Exception ex) {
                        showError(rootMessage(ex));
                    }
                }
            };
            worker.execute();

        } catch (DateTimeParseException ex) {
            showError("Enter dates using yyyy-MM-dd, for example 2026-11-20.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? "An unexpected error occurred." : current.getMessage();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Booking Error", JOptionPane.WARNING_MESSAGE);
    }

    private static class UserRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof User user) {
                setText(user.getFirstName() + " " + user.getLastName()
                        + " (" + user.getUserRole() + ")");
            }
            return this;
        }
    }

    private static class EquipmentRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Equipment item) {
                setText(item.getEquipmentName() + " | " + item.getSerialNumber()
                        + " | " + currencyText(item.getDailyRate()) + "/day");
            }
            return this;
        }

        private String currencyText(double amount) {
            return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-ZA")).format(amount);
        }
    }
}
