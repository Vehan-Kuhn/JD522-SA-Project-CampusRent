package za.ac.ctu.campusrent.gui;
import za.ac.ctu.campusrent.model.Campus;
import za.ac.ctu.campusrent.model.Equipment;
import za.ac.ctu.campusrent.service.CampusService;
import za.ac.ctu.campusrent.service.EquipmentService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class AddEquipmentDialog extends JDialog {

    private final CampusService campusService;
    private final EquipmentService equipmentService;
    private final Runnable onSaved;

    private JComboBox<Campus> campusCombo;
    private JTextField serialNumberField;
    private JTextField equipmentNameField;
    private JTextArea descriptionArea;
    private JComboBox<String> categoryCombo;
    private JTextField dailyRateField;
    private JComboBox<String> conditionCombo;
    private JComboBox<String> availabilityCombo;
    private JButton saveButton;

    public AddEquipmentDialog(Window owner, Runnable onSaved) {
        super(owner, "Add Equipment", ModalityType.APPLICATION_MODAL);

        this.onSaved = onSaved;
        this.campusService = new CampusService();
        this.equipmentService = new EquipmentService();

        initialiseDialog();
        loadCampuses();
    }

    private void initialiseDialog() {

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 620);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        JLabel heading = new JLabel("Register New Equipment");
        heading.setFont(new Font("Arial", Font.BOLD, 22));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        campusCombo = new JComboBox<>();
        serialNumberField = new JTextField(20);
        equipmentNameField = new JTextField(20);

        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        categoryCombo = new JComboBox<>(new String[]{
                "LAPTOP", "PROJECTOR", "CAMERA",
                "TABLET", "NETWORK_KIT", "OTHER"
        });

        dailyRateField = new JTextField(20);

        conditionCombo = new JComboBox<>(new String[]{
                "NEW", "GOOD", "FAIR", "DAMAGED"
        });

        availabilityCombo = new JComboBox<>(new String[]{
                "AVAILABLE", "MAINTENANCE", "RETIRED"
        });

        int row = 0;

        addFormRow(formPanel, gbc, row++, "Campus:", campusCombo);
        addFormRow(formPanel, gbc, row++, "Serial Number:", serialNumberField);
        addFormRow(formPanel, gbc, row++, "Equipment Name:", equipmentNameField);
        addFormRow(formPanel, gbc, row++, "Description:",
                new JScrollPane(descriptionArea));
        addFormRow(formPanel, gbc, row++, "Category:", categoryCombo);
        addFormRow(formPanel, gbc, row++, "Daily Rate (R):", dailyRateField);
        addFormRow(formPanel, gbc, row++, "Condition:", conditionCombo);
        addFormRow(formPanel, gbc, row++, "Availability:", availabilityCombo);

        saveButton = new JButton("Save Equipment");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveEquipment());
        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        mainPanel.add(heading, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            Component input) {

        gbc.gridy = row;

        gbc.gridx = 0;
        gbc.weightx = 0.35;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(input, gbc);
    }

    private void loadCampuses() {

        campusCombo.setEnabled(false);
        saveButton.setEnabled(false);

        SwingWorker<List<Campus>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<Campus> doInBackground() throws SQLException {
                return campusService.getAllCampuses();
            }

            @Override
            protected void done() {

                try {
                    List<Campus> campuses = get();

                    campusCombo.removeAllItems();

                    for (Campus campus : campuses) {
                        campusCombo.addItem(campus);
                    }

                    campusCombo.setEnabled(!campuses.isEmpty());
                    saveButton.setEnabled(!campuses.isEmpty());

                    if (campuses.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                AddEquipmentDialog.this,
                                "No campuses were found. Add a campus before registering equipment.",
                                "No Campuses Available",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            AddEquipmentDialog.this,
                            "Could not load campuses from the database.\n"
                                    + getErrorMessage(ex),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );

                    dispose();
                }
            }
        };

        worker.execute();
    }

    private void saveEquipment() {

        Campus selectedCampus =
                (Campus) campusCombo.getSelectedItem();

        String serialNumber = serialNumberField.getText().trim();
        String equipmentName = equipmentNameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String dailyRateText = dailyRateField.getText().trim();

        if (selectedCampus == null) {
            showValidationError("Please select a campus.");
            return;
        }

        if (serialNumber.isEmpty()) {
            showValidationError("Please enter the serial number.");
            return;
        }

        if (equipmentName.isEmpty()) {
            showValidationError("Please enter the equipment name.");
            return;
        }

        if (dailyRateText.isEmpty()) {
            showValidationError("Please enter the daily rental rate.");
            return;
        }

        final double dailyRate;

        try {
            dailyRate = Double.parseDouble(dailyRateText);

            if (!Double.isFinite(dailyRate) || dailyRate < 0) {
                showValidationError(
                        "The daily rate must be a valid number greater than or equal to zero."
                );
                return;
            }

        } catch (NumberFormatException ex) {
            showValidationError(
                    "Please enter a valid daily rate, for example: 150.00"
            );
            return;
        }

        Equipment equipment = new Equipment();

        equipment.setCampusId(selectedCampus.getCampusId());
        equipment.setSerialNumber(serialNumber);
        equipment.setEquipmentName(equipmentName);
        equipment.setDescription(description);
        equipment.setCategory((String) categoryCombo.getSelectedItem());
        equipment.setDailyRate(dailyRate);
        equipment.setConditionStatus((String) conditionCombo.getSelectedItem());
        equipment.setAvailabilityStatus((String) availabilityCombo.getSelectedItem());

        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {

            @Override
            protected Integer doInBackground() throws SQLException {
                return equipmentService.addEquipment(equipment);
            }

            @Override
            protected void done() {

                saveButton.setEnabled(true);
                saveButton.setText("Save Equipment");

                try {
                    int equipmentId = get();

                    JOptionPane.showMessageDialog(
                            AddEquipmentDialog.this,
                            "Equipment registered successfully.\n"
                                    + "Equipment ID: " + equipmentId,
                            "Equipment Saved",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    if (onSaved != null) {
                        onSaved.run();
                    }

                    dispose();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            AddEquipmentDialog.this,
                            getErrorMessage(ex),
                            "Unable to Save Equipment",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };

        worker.execute();
    }

    private void showValidationError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private String getErrorMessage(Exception ex) {

        Throwable cause = ex.getCause();

        while (cause != null) {
            if (cause instanceof IllegalArgumentException
                    || cause instanceof SQLException) {
                return cause.getMessage();
            }

            cause = cause.getCause();
        }

        return ex.getMessage() != null
                ? ex.getMessage()
                : "An unexpected error occurred.";
    }
}

