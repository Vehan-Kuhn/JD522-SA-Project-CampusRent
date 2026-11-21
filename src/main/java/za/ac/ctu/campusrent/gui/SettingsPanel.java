
package za.ac.ctu.campusrent.gui;

import za.ac.ctu.campusrent.model.AppPreferences;
import za.ac.ctu.campusrent.service.BackupService;
import za.ac.ctu.campusrent.service.CsvService;
import za.ac.ctu.campusrent.service.PreferencesService;
import za.ac.ctu.campusrent.service.FileManagementService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SettingsPanel extends JPanel {

    private final PreferencesService preferencesService;
    private final FileManagementService fileManagementService =
            new FileManagementService();

    private final Runnable languageChangedCallback;

    private AppPreferences preferences;

    private JTextField exportDirectoryField;
    private JTextField backupDirectoryField;

    private JComboBox<String> languageCombo;
    private JComboBox<String> themeCombo;

    private JCheckBox automaticBackupCheck;

    /**
     * Default constructor retained for compatibility.
     */
    public SettingsPanel() {
        this(() -> {
        });
    }

    /**
     * Constructor that accepts a callback to refresh MainFrame
     * after the application language has changed.
     */
    public SettingsPanel(Runnable languageChangedCallback) {

        this.languageChangedCallback = languageChangedCallback != null
                ? languageChangedCallback
                : () -> {
        };

        preferencesService = new PreferencesService();

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        loadPreferences();
        buildInterface();
    }

    /**
     * Loads saved application preferences.
     */
    private void loadPreferences() {

        try {

            preferences = preferencesService.load();

            if (preferences == null) {
                preferences = new AppPreferences();
            }

        } catch (Exception ex) {

            preferences = new AppPreferences();

            JOptionPane.showMessageDialog(
                    this,
                    LanguageManager.text("settings.loadError")
                            + "\n" + ex.getMessage(),
                    LanguageManager.text("settings.warning"),
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    /**
     * Creates the settings interface.
     */
    private void buildInterface() {

        JLabel heading = new JLabel(
                LanguageManager.text("settings.heading")
        );

        heading.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel subtitle = new JLabel(
                LanguageManager.text("settings.subtitle")
        );

        subtitle.setForeground(Color.GRAY);

        JPanel titlePanel = new JPanel(new BorderLayout(0, 6));
        titlePanel.setOpaque(false);

        titlePanel.add(heading, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.CENTER);

        exportDirectoryField = new JTextField(
                preferences.getExportDirectory(), 25
        );

        backupDirectoryField = new JTextField(
                preferences.getBackupDirectory(), 25
        );

        languageCombo = new JComboBox<>(
                new String[]{
                        "English",
                        "Afrikaans",
                        "isiZulu"
                }
        );

        themeCombo = new JComboBox<>(
                new String[]{
                        "LIGHT",
                        "DARK"
                }
        );

        automaticBackupCheck = new JCheckBox(
                LanguageManager.text("settings.autoBackup"),
                preferences.isAutomaticBackup()
        );

        automaticBackupCheck.setOpaque(false);

        // Select the saved language.
        languageCombo.setSelectedItem(
                switch (preferences.getLanguage() == null
                        ? "en"
                        : preferences.getLanguage()) {

                    case "af" -> "Afrikaans";
                    case "zu" -> "isiZulu";
                    default -> "English";
                }
        );

        // Select the saved theme.
        themeCombo.setSelectedItem(preferences.getTheme());

        // Apply theme immediately when changed.
        themeCombo.addActionListener(e -> {

            String selectedTheme =
                    (String) themeCombo.getSelectedItem();

            if (selectedTheme != null) {

                ThemeManager.setTheme(selectedTheme);
                ThemeManager.applyToAllWindows();
            }
        });

        // Settings form.
        JPanel form = new JPanel(new GridBagLayout());

        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(10, 8, 10, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        addRow(
                form,
                gbc,
                row++,
                LanguageManager.text("settings.export"),
                createDirectoryField(exportDirectoryField)
        );

        addRow(
                form,
                gbc,
                row++,
                LanguageManager.text("settings.backup"),
                createDirectoryField(backupDirectoryField)
        );

        addRow(
                form,
                gbc,
                row++,
                LanguageManager.text("settings.language"),
                languageCombo
        );

        addRow(
                form,
                gbc,
                row++,
                LanguageManager.text("settings.theme"),
                themeCombo
        );

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.weightx = 1;

        form.add(automaticBackupCheck, gbc);

        // Buttons.
        JButton saveButton = new JButton(
                LanguageManager.text("settings.save")
        );

        JButton backupButton = new JButton(
                LanguageManager.text("settings.createBackup")
        );

        JButton viewBackupsButton = new JButton(
                LanguageManager.text("settings.viewBackups")
        );

        styleButton(saveButton, new Color(41, 128, 185));
        styleButton(backupButton, new Color(39, 174, 96));
        styleButton(viewBackupsButton, new Color(142, 68, 173));

        saveButton.addActionListener(e -> savePreferences());
        backupButton.addActionListener(e -> createBackup());
        viewBackupsButton.addActionListener(e -> viewBackupFiles());

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

    /**
     * Creates a directory text field with a Browse button.
     */
    private JPanel createDirectoryField(JTextField field) {

        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);

        JButton browseButton = new JButton(
                LanguageManager.text("settings.browse")
        );

        browseButton.addActionListener(
                e -> chooseDirectory(field)
        );

        panel.add(field, BorderLayout.CENTER);
        panel.add(browseButton, BorderLayout.EAST);

        return panel;
    }

    /**
     * Adds a labelled row to the settings form.
     */
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

    /**
     * Opens a directory selection window.
     */
    private void chooseDirectory(JTextField field) {

        JFileChooser chooser = new JFileChooser();

        chooser.setFileSelectionMode(
                JFileChooser.DIRECTORIES_ONLY
        );

        chooser.setDialogTitle(
                LanguageManager.text("settings.selectDirectory")
        );

        if (chooser.showOpenDialog(this)
                == JFileChooser.APPROVE_OPTION) {

            field.setText(
                    chooser.getSelectedFile().getAbsolutePath()
            );
        }
    }

    /**
     * Saves preferences and applies the selected language and theme.
     */
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

            // Create directories if they do not exist.
            Files.createDirectories(Path.of(exportDirectory));
            Files.createDirectories(Path.of(backupDirectory));

            // Remember the previous language.
            String previousLanguage = preferences.getLanguage();

            if (previousLanguage == null) {
                previousLanguage = "en";
            }

            // Convert selected language into its language code.
            String selectedLanguage =
                    (String) languageCombo.getSelectedItem();

            String languageCode = switch (
                    selectedLanguage == null
                            ? "English"
                            : selectedLanguage
                    ) {
                case "Afrikaans" -> "af";
                case "isiZulu" -> "zu";
                default -> "en";
            };

            // Update preferences.
            preferences.setExportDirectory(exportDirectory);
            preferences.setBackupDirectory(backupDirectory);
            preferences.setLanguage(languageCode);

            preferences.setTheme(
                    (String) themeCombo.getSelectedItem()
            );

            preferences.setAutomaticBackup(
                    automaticBackupCheck.isSelected()
            );

            // Save preferences before applying changes.
            preferencesService.save(preferences);

            // Apply the new language.
            LanguageManager.setLanguage(languageCode);

            // Apply the selected theme.
            ThemeManager.setTheme(preferences.getTheme());
            ThemeManager.applyToAllWindows();

            // Show confirmation in the newly selected language.
            JOptionPane.showMessageDialog(
                    this,
                    LanguageManager.text("settings.saved"),
                    LanguageManager.text("settings.savedTitle"),
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Refresh MainFrame only if the language changed.
            if (!previousLanguage.equals(languageCode)) {

                languageChangedCallback.run();
            }

        } catch (Exception ex) {

            showError(
                    LanguageManager.text("settings.error")
                            + "\n" + ex.getMessage()
            );
        }
    }

    /**
     * Creates a backup of CampusRent data.
     */
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

            showError(
                    "Backup failed:\n" + ex.getMessage()
            );
        }
    }

    /**
     * Applies button styling.
     */
    private void styleButton(JButton button, Color colour) {

        button.setBackground(colour);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBorder(
                new EmptyBorder(10, 15, 10, 15)
        );
    }

    /**
     * Displays the files stored in the backup directory.
     */
    private void viewBackupFiles() {

        try {

            String backupPath =
                    backupDirectoryField.getText().trim();

            if (backupPath.isBlank()) {

                throw new IllegalArgumentException(
                        "Please select a backup directory."
                );
            }

            Path backupDirectory = Path.of(backupPath);

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
                    DateTimeFormatter.ofPattern(
                            "dd MMM yyyy HH:mm:ss"
                    );

            for (var file : files) {

                String modified = Instant.ofEpochMilli(
                                file.getLastModified()
                        )
                        .atZone(ZoneId.systemDefault())
                        .format(formatter);

                report.append("File: ")
                        .append(file.getPath().getFileName())
                        .append("\nSize: ")
                        .append(file.getSize())
                        .append(" bytes\nModified: ")
                        .append(modified)
                        .append("\n\n");
            }

            JTextArea textArea = new JTextArea(
                    report.toString()
            );

            textArea.setEditable(false);
            textArea.setCaretPosition(0);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(
                    new Dimension(500, 350)
            );

            JOptionPane.showMessageDialog(
                    this,
                    scrollPane,
                    "CampusRent Backup Files",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {

            showError(
                    "Could not retrieve backup files:\n"
                            + ex.getMessage()
            );
        }
    }

    /**
     * Displays an error message.
     */
    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Settings Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
