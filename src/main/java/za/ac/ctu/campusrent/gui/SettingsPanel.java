
package za.ac.ctu.campusrent.gui;

import za.ac.ctu.campusrent.model.AppPreferences;
import za.ac.ctu.campusrent.service.BackupService;
import za.ac.ctu.campusrent.service.CsvService;
import za.ac.ctu.campusrent.service.PreferencesService;
import za.ac.ctu.campusrent.service.FileManagementService;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsPanel extends JPanel {

    private final PreferencesService preferencesService;
    private AppPreferences preferences;
    private final FileManagementService fileManagementService =
            new FileManagementService();
    private JTextField exportDirectoryField;
    private JTextField backupDirectoryField;

    private JComboBox<String> languageCombo;
    private JComboBox<String> themeCombo;

    private JCheckBox automaticBackupCheck;

    public SettingsPanel() {

        preferencesService = new PreferencesService();

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        loadPreferences();
        buildInterface();
    }

    private void loadPreferences() {

        try {
            preferences = preferencesService.load();

        } catch (Exception ex) {

            preferences = new AppPreferences();

            JOptionPane.showMessageDialog(
                    this,
                    "Could not load preferences. Default settings will be used.\n"
                            + ex.getMessage(),
                    "Preferences Warning",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    private void buildInterface() {

        JLabel heading = new JLabel("Application Settings");
        heading.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel subtitle = new JLabel(
                "Configure CampusRent file storage and application preferences."
        );
        subtitle.setForeground(Color.GRAY);

        JPanel titlePanel = new JPanel(new BorderLayout(0, 6));
        titlePanel.setOpaque(false);
        titlePanel.add(heading, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.CENTER);

        exportDirectoryField =
                new JTextField(preferences.getExportDirectory(), 25);

        backupDirectoryField =
                new JTextField(preferences.getBackupDirectory(), 25);

        languageCombo = new JComboBox<>(
                new String[]{"English", "Afrikaans", "isiZulu"}
        );

        themeCombo = new JComboBox<>(
                new String[]{"LIGHT", "DARK"}
        );

        automaticBackupCheck = new JCheckBox(
                "Enable automatic backup preference",
                preferences.isAutomaticBackup()
        );

        languageCombo.setSelectedItem(
                switch (preferences.getLanguage()) {
                    case "af" -> "Afrikaans";
                    case "zu" -> "isiZulu";
                    default -> "English";
                }
        );

        themeCombo.setSelectedItem(preferences.getTheme());
        themeCombo.addActionListener(e -> {
            String selectedTheme =
                    (String) themeCombo.getSelectedItem();

            ThemeManager.setTheme(selectedTheme);
            ThemeManager.applyToAllWindows();
        });

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        addRow(form, gbc, row++, "Export directory:",
                createDirectoryField(exportDirectoryField));

        addRow(form, gbc, row++, "Backup directory:",
                createDirectoryField(backupDirectoryField));

        addRow(form, gbc, row++, "Language:", languageCombo);
        addRow(form, gbc, row++, "Theme:", themeCombo);

        gbc.gridx = 1;
        gbc.gridy = row;
        form.add(automaticBackupCheck, gbc);

        JButton saveButton = new JButton("Save Preferences");
        JButton backupButton = new JButton("Create Backup Now");
        JButton viewBackupsButton = new JButton("View Backup Files");
        styleButton(viewBackupsButton, new Color(142, 68, 173));
        viewBackupsButton.addActionListener(e -> viewBackupFiles());
        styleButton(saveButton, new Color(41, 128, 185));
        styleButton(backupButton, new Color(39, 174, 96));

        saveButton.addActionListener(e -> savePreferences());
        backupButton.addActionListener(e -> createBackup());

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 10)
        );
        buttons.setOpaque(false);
        buttons.add(saveButton);
        buttons.add(backupButton);
        buttons.add(viewBackupsButton);

        JPanel content = new JPanel(new BorderLayout(0, 15));
        content.setOpaque(false);
        content.add(form, BorderLayout.NORTH);
        content.add(buttons, BorderLayout.CENTER);

        add(titlePanel, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
    }

    private JPanel createDirectoryField(JTextField field) {

        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);

        JButton browseButton = new JButton("Browse");

        browseButton.addActionListener(e -> chooseDirectory(field));

        panel.add(field, BorderLayout.CENTER);
        panel.add(browseButton, BorderLayout.EAST);

        return panel;
    }

    private void addRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            Component component) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;

        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;

        panel.add(component, gbc);
    }

    private void chooseDirectory(JTextField field) {

        JFileChooser chooser = new JFileChooser();

        chooser.setFileSelectionMode(
                JFileChooser.DIRECTORIES_ONLY
        );

        chooser.setDialogTitle("Select Directory");

        if (chooser.showOpenDialog(this)
                == JFileChooser.APPROVE_OPTION) {

            field.setText(
                    chooser.getSelectedFile().getAbsolutePath()
            );
        }
    }

    private void savePreferences() {

        try {

            String exportDirectory =
                    exportDirectoryField.getText().trim();

            String backupDirectory =
                    backupDirectoryField.getText().trim();

            if (exportDirectory.isBlank()
                    || backupDirectory.isBlank()) {

                throw new IllegalArgumentException(
                        "Both directory paths are required."
                );
            }

            Files.createDirectories(Path.of(exportDirectory));
            Files.createDirectories(Path.of(backupDirectory));

            preferences.setExportDirectory(exportDirectory);
            preferences.setBackupDirectory(backupDirectory);

            preferences.setLanguage(
                    switch ((String) languageCombo.getSelectedItem()) {
                        case "Afrikaans" -> "af";
                        case "isiZulu" -> "zu";
                        default -> "en";
                    }
            );

            preferences.setTheme(
                    (String) themeCombo.getSelectedItem()
            );

            preferences.setAutomaticBackup(
                    automaticBackupCheck.isSelected()
            );

            preferencesService.save(preferences);
            ThemeManager.setTheme(preferences.getTheme());
            ThemeManager.applyToAllWindows();

            JOptionPane.showMessageDialog(
                    this,
                    "Preferences saved successfully.",
                    "Settings Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {

            showError("Could not save preferences:\n" + ex.getMessage());
        }
    }

    private void createBackup() {

        try {

            String backupPath =
                    backupDirectoryField.getText().trim();

            if (backupPath.isBlank()) {
                throw new IllegalArgumentException(
                        "Please select a backup directory."
                );
            }

            Path destination = new BackupService(
                    new CsvService(),
                    Path.of("campusrent-data"),
                    Path.of(backupPath)
            ).createBackup();

            JOptionPane.showMessageDialog(
                    this,
                    "Backup created successfully:\n"
                            + destination.toAbsolutePath(),
                    "Backup Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {

            showError("Backup failed:\n" + ex.getMessage());
        }
    }

    private void styleButton(JButton button, Color colour) {

        button.setBackground(colour);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(10, 15, 10, 15));
    }

    private void viewBackupFiles() {

        try {

            Path backupDirectory = Path.of(
                    backupDirectoryField.getText().trim()
            );

            var files = fileManagementService.listFiles(
                    backupDirectory
            );

            if (files.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "No backup files were found.",
                        "Backup Files",
                        JOptionPane.INFORMATION_MESSAGE
                );

                return;
            }

            StringBuilder report = new StringBuilder();

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");

            for (var file : files) {

                String modified = Instant.ofEpochMilli(
                                file.getLastModified()
                        ).atZone(ZoneId.systemDefault())
                        .format(formatter);

                report.append("File: ")
                        .append(file.getPath().getFileName())
                        .append("\nSize: ")
                        .append(file.getSize())
                        .append(" bytes\nModified: ")
                        .append(modified)
                        .append("\n\n");
            }

            JTextArea textArea = new JTextArea(report.toString());
            textArea.setEditable(false);
            textArea.setCaretPosition(0);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 350));

            JOptionPane.showMessageDialog(
                    this,
                    scrollPane,
                    "CampusRent Backup Files",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {

            showError(
                    "Could not retrieve backup files:\n" + ex.getMessage()
            );
        }
    }


    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Settings Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
