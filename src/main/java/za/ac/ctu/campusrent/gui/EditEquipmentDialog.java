
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

        super(
                owner,
                LanguageManager.text("equipment.editTitle"),
                ModalityType.APPLICATION_MODAL
        );

        if (equipment == null) {
            throw new IllegalArgumentException(
                    "Equipment is required."
            );
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

        JPanel mainPanel = new JPanel(
                new BorderLayout(10, 15)
        );

        mainPanel.setBorder(
                new EmptyBorder(20, 25, 20, 25)
        );

        mainPanel.setBackground(Color.WHITE);

        JLabel heading = new JLabel(
                LanguageManager.text("equipment.editHeading")
        );

        heading.setFont(
                new Font("Arial", Font.BOLD, 22)
        );

        JPanel formPanel = new JPanel(
                new GridBagLayout()
        );

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
                "LAPTOP",
                "PROJECTOR",
                "CAMERA",
                "TABLET",
                "NETWORK_KIT",
                "OTHER"
        });

        dailyRateField = new JTextField(20);

        conditionCombo = new JComboBox<>(new String[]{
                "NEW",
                "GOOD",
                "FAIR",
                "DAMAGED"
        });

        int row = 0;

        addFormRow(
                formPanel, gbc, row++,
                LanguageManager.text("equipment.formCampus"),
                campusCombo
        );

        addFormRow(
                formPanel, gbc, row++,
                LanguageManager.text("equipment.formSerial"),
                serialNumberField
        );

        addFormRow(
                formPanel, gbc, row++,
                LanguageManager.text("equipment.formName"),
                equipmentNameField
        );

        addFormRow(
                formPanel, gbc, row++,
                LanguageManager.text("equipment.formDescription"),
                new JScrollPane(descriptionArea)
        );

        addFormRow(
                formPanel, gbc, row++,
                LanguageManager.text("equipment.formCategory"),
                categoryCombo
        );

        addFormRow(
                formPanel, gbc, row++,
                LanguageManager.text("equipment.formRate"),
                dailyRateField
        );

        addFormRow(
                formPanel, gbc, row++,
                LanguageManager.text("equipment.formCondition"),
                conditionCombo
        );

        JLabel availabilityLabel = new JLabel(
                LanguageManager.text("equipment.availability")
                        + ": "
                        + equipment.getAvailabilityStatus()
                        + " "
                        + LanguageManager.text(
                        "equipment.managedSeparately"
                )
        );

        availabilityLabel.setForeground(Color.GRAY);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;

        formPanel.add(availabilityLabel, gbc);

        gbc.gridwidth = 1;

        saveButton = new JButton(
                LanguageManager.text("equipment.saveChanges")
        );

        JButton cancelButton = new JButton(
                LanguageManager.text("common.cancel")
        );

        saveButton.addActionListener(
                e -> saveChanges()
        );

        cancelButton.addActionListener(
                e -> dispose()
        );

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT)
        );

        buttonPanel.setOpaque(false);
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        mainPanel.add(heading, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void populateFields() {

        serialNumberField.setText(
                equipment.getSerialNumber()
        );

        equipmentNameField.setText(
                equipment.getEquipmentName()
        );

        descriptionArea.setText(
                equipment.getDescription() == null
                        ? ""
                        : equipment.getDescription()
        );

        categoryCombo.setSelectedItem(
                equipment.getCategory()
        );

        dailyRateField.setText(
                String.valueOf(equipment.getDailyRate())
        );

        conditionCombo.setSelectedItem(
                equipment.getConditionStatus()
        );
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

        SwingWorker<List<Campus>, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected List<Campus> doInBackground()
                            throws SQLException {

                        return campusService.getAllCampuses();
                    }

                    @Override
                    protected void done() {

                        try {

                            List<Campus> campuses = get();

                            campusCombo.removeAllItems();

                            for (Campus campus : campuses) {

                                campusCombo.addItem(campus);

                                if (campus.getCampusId()
                                        == equipment.getCampusId()) {

                                    campusCombo.setSelectedItem(campus);
                                }
                            }

                            campusCombo.setEnabled(!campuses.isEmpty());
                            saveButton.setEnabled(!campuses.isEmpty());

                            if (campuses.isEmpty()) {

                                JOptionPane.showMessageDialog(
                                        EditEquipmentDialog.this,
                                        LanguageManager.text(
                                                "equipment.noCampuses"
                                        ),
                                        LanguageManager.text(
                                                "equipment.noCampusesTitle"
                                        ),
                                        JOptionPane.WARNING_MESSAGE
                                );
                            }

                        } catch (Exception ex) {

                            JOptionPane.showMessageDialog(
                                    EditEquipmentDialog.this,
                                    LanguageManager.text(
                                            "equipment.loadCampusesError"
                                    )
                                            + "\n"
                                            + getErrorMessage(ex),
                                    LanguageManager.text(
                                            "equipment.databaseErrorTitle"
                                    ),
                                    JOptionPane.ERROR_MESSAGE
                            );

                            dispose();
                        }
                    }
                };

        worker.execute();
    }

    private void saveChanges() {

        Campus selectedCampus =
                (Campus) campusCombo.getSelectedItem();

        String serialNumber =
                serialNumberField.getText().trim();

        String name =
                equipmentNameField.getText().trim();

        String rateText =
                dailyRateField.getText().trim();

        if (selectedCampus == null) {

            showValidationError(
                    LanguageManager.text("equipment.validationCampus")
            );

            return;
        }

        if (serialNumber.isEmpty()) {

            showValidationError(
                    LanguageManager.text("equipment.validationSerial")
            );

            return;
        }

        if (name.isEmpty()) {

            showValidationError(
                    LanguageManager.text("equipment.validationName")
            );

            return;
        }

        if (rateText.isEmpty()) {

            showValidationError(
                    LanguageManager.text("equipment.validationRate")
            );

            return;
        }

        final double rate;

        try {

            rate = Double.parseDouble(rateText);

            if (!Double.isFinite(rate) || rate < 0) {

                showValidationError(
                        LanguageManager.text(
                                "equipment.validationRatePositive"
                        )
                );

                return;
            }

        } catch (NumberFormatException ex) {

            showValidationError(
                    LanguageManager.text(
                            "equipment.validationRateFormat"
                    )
            );

            return;
        }

        equipment.setCampusId(
                selectedCampus.getCampusId()
        );

        equipment.setSerialNumber(serialNumber);
        equipment.setEquipmentName(name);

        equipment.setDescription(
                descriptionArea.getText().trim()
        );

        equipment.setCategory(
                (String) categoryCombo.getSelectedItem()
        );

        equipment.setDailyRate(rate);

        equipment.setConditionStatus(
                (String) conditionCombo.getSelectedItem()
        );

        // Availability is intentionally not edited here.
        // Use the service's maintenance, restore and retire
        // operations to preserve booking safeguards.

        saveButton.setEnabled(false);

        saveButton.setText(
                LanguageManager.text("equipment.saving")
        );

        SwingWorker<Boolean, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Boolean doInBackground()
                            throws SQLException {

                        return equipmentService.updateEquipment(equipment);
                    }

                    @Override
                    protected void done() {

                        saveButton.setEnabled(true);

                        saveButton.setText(
                                LanguageManager.text(
                                        "equipment.saveChanges"
                                )
                        );

                        try {

                            if (get()) {

                                JOptionPane.showMessageDialog(
                                        EditEquipmentDialog.this,
                                        LanguageManager.text(
                                                "equipment.updated"
                                        ),
                                        LanguageManager.text(
                                                "equipment.changesSaved"
                                        ),
                                        JOptionPane.INFORMATION_MESSAGE
                                );

                                if (onSaved != null) {
                                    onSaved.run();
                                }

                                dispose();

                            } else {

                                JOptionPane.showMessageDialog(
                                        EditEquipmentDialog.this,
                                        LanguageManager.text(
                                                "equipment.updateNotCompleted"
                                        ),
                                        LanguageManager.text(
                                                "equipment.updateNotCompletedTitle"
                                        ),
                                        JOptionPane.WARNING_MESSAGE
                                );
                            }

                        } catch (Exception ex) {

                            JOptionPane.showMessageDialog(
                                    EditEquipmentDialog.this,
                                    getErrorMessage(ex),
                                    LanguageManager.text(
                                            "equipment.updateErrorTitle"
                                    ),
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
                LanguageManager.text(
                        "equipment.validationTitle"
                ),
                JOptionPane.WARNING_MESSAGE
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
                ? LanguageManager.text("common.unknownError")
                : ex.getMessage();
    }
}
