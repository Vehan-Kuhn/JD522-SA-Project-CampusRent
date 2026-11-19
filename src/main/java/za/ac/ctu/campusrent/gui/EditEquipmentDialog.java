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

public class EditEquipmentDialog extends JDialog {

    private final Equipment equipment;
    private final EquipmentService equipmentService;
    private final CampusService campusService;
    private final Runnable onSaved;

    private JComboBox<Campus> campusCombo;
    private JTextField serialNumberField;
    private JTextField equipmentNameField;
    private JTextArea descriptionArea;
    private JComboBox<String> categoryCombo;
    private JTextField dailyRateField;
    private JComboBox<String> conditionCombo;
    private JButton saveButton;

    public EditEquipmentDialog(
            Window owner,
            Equipment equipment,
            Runnable onSaved) {

        super(owner, "Edit Equipment", ModalityType.APPLICATION_MODAL);

        if (equipment == null) {
            throw new IllegalArgumentException("Equipment is required.");
        }

        this.equipment = equipment;
        this.onSaved = onSaved;
        this.equipmentService = new EquipmentService();
        this.campusService = new CampusService();

        initialiseDialog();
        populateFields();
        loadCampuses();
    }

    private void initialiseDialog() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 590);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 15));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        JLabel heading = new JLabel("Edit Equipment");
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

        int row = 0;
        addFormRow(formPanel, gbc, row++, "Campus:", campusCombo);
        addFormRow(formPanel, gbc, row++, "Serial Number:", serialNumberField);
        addFormRow(formPanel, gbc, row++, "Equipment Name:", equipmentNameField);
        addFormRow(formPanel, gbc, row++, "Description:",
                new JScrollPane(descriptionArea));
        addFormRow(formPanel, gbc, row++, "Category:", categoryCombo);
        addFormRow(formPanel, gbc, row++, "Daily Rate (R):", dailyRateField);
        addFormRow(formPanel, gbc, row++, "Condition:", conditionCombo);

        JLabel availabilityLabel = new JLabel(
                "Availability: " + equipment.getAvailabilityStatus()
                        + " (managed separately)"
        );
        availabilityLabel.setForeground(Color.GRAY);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        formPanel.add(availabilityLabel, gbc);
        gbc.gridwidth = 1;

        saveButton = new JButton("Save Changes");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveChanges());
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

    private void populateFields() {
        serialNumberField.setText(equipment.getSerialNumber());
        equipmentNameField.setText(equipment.getEquipmentName());
        descriptionArea.setText(
                equipment.getDescription() == null ? "" : equipment.getDescription()
        );
        categoryCombo.setSelectedItem(equipment.getCategory());
        dailyRateField.setText(String.valueOf(equipment.getDailyRate()));
        conditionCombo.setSelectedItem(equipment.getConditionStatus());
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
                        if (campus.getCampusId() == equipment.getCampusId()) {
                            campusCombo.setSelectedItem(campus);
                        }
                    }

                    campusCombo.setEnabled(!campuses.isEmpty());
                    saveButton.setEnabled(!campuses.isEmpty());

                    if (campuses.isEmpty()) {
                        JOptionPane.showMessageDialog(
                                EditEquipmentDialog.this,
                                "No campuses were found.",
                                "No Campuses Available",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            EditEquipmentDialog.this,
                            "Could not load campuses.\n" + getErrorMessage(ex),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    dispose();
                }
            }
        };
        worker.execute();
    }

    private void saveChanges() {
        Campus selectedCampus = (Campus) campusCombo.getSelectedItem();
        String serialNumber = serialNumberField.getText().trim();
        String name = equipmentNameField.getText().trim();
        String rateText = dailyRateField.getText().trim();

        if (selectedCampus == null) {
            showValidationError("Please select a campus.");
            return;
        }
        if (serialNumber.isEmpty()) {
            showValidationError("Please enter a serial number.");
            return;
        }
        if (name.isEmpty()) {
            showValidationError("Please enter an equipment name.");
            return;
        }
        if (rateText.isEmpty()) {
            showValidationError("Please enter a daily rate.");
            return;
        }

        final double rate;
        try {
            rate = Double.parseDouble(rateText);
            if (!Double.isFinite(rate) || rate < 0) {
                showValidationError("Daily rate must be a valid non-negative amount.");
                return;
            }
        } catch (NumberFormatException ex) {
            showValidationError("Enter a valid daily rate, for example 150.00.");
            return;
        }

        equipment.setCampusId(selectedCampus.getCampusId());
        equipment.setSerialNumber(serialNumber);
        equipment.setEquipmentName(name);
        equipment.setDescription(descriptionArea.getText().trim());
        equipment.setCategory((String) categoryCombo.getSelectedItem());
        equipment.setDailyRate(rate);
        equipment.setConditionStatus((String) conditionCombo.getSelectedItem());

        // Availability is intentionally not edited here. Its changes must go
        // through the service's maintenance/restore/retire safeguards.
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws SQLException {
                return equipmentService.updateEquipment(equipment);
            }

            @Override
            protected void done() {
                saveButton.setEnabled(true);
                saveButton.setText("Save Changes");

                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(
                                EditEquipmentDialog.this,
                                "Equipment updated successfully.",
                                "Changes Saved",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        if (onSaved != null) {
                            onSaved.run();
                        }
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(
                                EditEquipmentDialog.this,
                                "No equipment record was updated.",
                                "Update Not Completed",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(
                            EditEquipmentDialog.this,
                            getErrorMessage(ex),
                            "Unable to Update Equipment",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        };
        worker.execute();
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Validation Error", JOptionPane.WARNING_MESSAGE
        );
    }

    private String getErrorMessage(Exception ex) {
        Throwable cause = ex;
        while (cause != null) {
            if (cause.getMessage() != null
                    && (cause instanceof IllegalArgumentException
                    || cause instanceof SQLException)) {
                return cause.getMessage();
            }
            cause = cause.getCause();
        }
        return ex.getMessage() == null
                ? "An unexpected error occurred."
                : ex.getMessage();
    }
}
