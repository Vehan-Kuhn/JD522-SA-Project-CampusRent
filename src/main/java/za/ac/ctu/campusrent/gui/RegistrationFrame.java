
package za.ac.ctu.campusrent.gui;

import za.ac.ctu.campusrent.model.Campus;
import za.ac.ctu.campusrent.model.User;
import za.ac.ctu.campusrent.service.CampusService;
import za.ac.ctu.campusrent.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class RegistrationFrame extends JFrame {

    private final UserService userService;
    private final CampusService campusService;

    private JTextField firstNameField;
    private JTextField lastNameField;
    private JTextField emailField;
    private JTextField phoneField;

    private JComboBox<Campus> campusComboBox;

    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private JButton registerButton;

    private final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private final Color SIDEBAR_COLOR = new Color(25, 35, 55);
    private final Color ACCENT_COLOR = new Color(41, 128, 185);

    public RegistrationFrame() {

        LanguageManager.loadSavedLanguage();
        ThemeManager.loadSavedTheme();

        userService = new UserService();
        campusService = new CampusService();

        setTitle(LanguageManager.text("registration.title"));
        setSize(500, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        createRegistrationInterface();
        loadCampuses();
    }

    private void createRegistrationInterface() {

        JPanel mainPanel = new JPanel(
                new BorderLayout(0, 15)
        );

        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(
                new EmptyBorder(25, 40, 25, 40)
        );

        // Heading
        JLabel headingLabel = new JLabel(
                LanguageManager.text("registration.heading"),
                SwingConstants.CENTER
        );

        headingLabel.setFont(
                new Font("Arial", Font.BOLD, 25)
        );

        headingLabel.setForeground(SIDEBAR_COLOR);

        JLabel subtitleLabel = new JLabel(
                LanguageManager.text("registration.subtitle"),
                SwingConstants.CENTER
        );

        subtitleLabel.setForeground(Color.GRAY);

        JPanel headingPanel = new JPanel(
                new GridLayout(2, 1, 0, 8)
        );

        headingPanel.setBackground(BACKGROUND_COLOR);
        headingPanel.add(headingLabel);
        headingPanel.add(subtitleLabel);

        // Registration form
        JPanel formPanel = new JPanel(
                new GridBagLayout()
        );

        formPanel.setBackground(BACKGROUND_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        emailField = new JTextField(20);
        phoneField = new JTextField(20);

        campusComboBox = new JComboBox<>();
        campusComboBox.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        passwordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);

        addFormRow(formPanel, gbc, row++,
                LanguageManager.text("registration.firstName"),
                firstNameField);

        addFormRow(formPanel, gbc, row++,
                LanguageManager.text("registration.lastName"),
                lastNameField);

        addFormRow(formPanel, gbc, row++,
                LanguageManager.text("registration.email"),
                emailField);

        addFormRow(formPanel, gbc, row++,
                LanguageManager.text("registration.phone"),
                phoneField);

        addFormRow(formPanel, gbc, row++,
                LanguageManager.text("registration.campus"),
                campusComboBox);

        addFormRow(formPanel, gbc, row++,
                LanguageManager.text("registration.password"),
                passwordField);

        addFormRow(formPanel, gbc, row++,
                LanguageManager.text("registration.confirmPassword"),
                confirmPasswordField);

        // Buttons
        registerButton = new JButton(
                LanguageManager.text("registration.create")
        );

        registerButton.setFont(
                new Font("Arial", Font.BOLD, 14)
        );

        registerButton.setForeground(Color.WHITE);
        registerButton.setBackground(ACCENT_COLOR);
        registerButton.setFocusPainted(false);
        registerButton.setBorderPainted(false);
        registerButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        registerButton.setEnabled(false);
        registerButton.addActionListener(e -> registerUser());

        JButton cancelButton = new JButton(
                LanguageManager.text("registration.cancel")
        );

        cancelButton.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        cancelButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel(
                new GridLayout(1, 2, 12, 0)
        );

        buttonPanel.setBackground(BACKGROUND_COLOR);
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        JLabel roleLabel = new JLabel(
                LanguageManager.text("registration.roleNotice")
        );

        roleLabel.setFont(
                new Font("Arial", Font.PLAIN, 11)
        );

        roleLabel.setForeground(Color.GRAY);

        JPanel bottomPanel = new JPanel(
                new BorderLayout(0, 12)
        );

        bottomPanel.setBackground(BACKGROUND_COLOR);
        bottomPanel.add(roleLabel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);

        mainPanel.add(headingPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        ThemeManager.apply(this);
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String labelText,
            JComponent field) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;

        JLabel label = new JLabel(labelText);
        label.setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;

        field.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        panel.add(field, gbc);
    }

    private void loadCampuses() {

        registerButton.setText(
                LanguageManager.text("registration.loading")
        );

        SwingWorker<List<Campus>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<Campus> doInBackground() throws SQLException {
                return campusService.getAllCampuses();
            }

            @Override
            protected void done() {

                try {

                    List<Campus> campuses = get();

                    campusComboBox.removeAllItems();

                    for (Campus campus : campuses) {
                        campusComboBox.addItem(campus);
                    }

                    if (campuses.isEmpty()) {

                        JOptionPane.showMessageDialog(
                                RegistrationFrame.this,
                                LanguageManager.text("registration.noCampuses"),
                                LanguageManager.text("registration.noCampusesTitle"),
                                JOptionPane.WARNING_MESSAGE
                        );

                        registerButton.setText(
                                LanguageManager.text("registration.create")
                        );

                        return;
                    }

                    registerButton.setText(
                            LanguageManager.text("registration.create")
                    );

                    registerButton.setEnabled(true);

                } catch (Exception ex) {

                    registerButton.setText(
                            LanguageManager.text("registration.create")
                    );

                    JOptionPane.showMessageDialog(
                            RegistrationFrame.this,
                            LanguageManager.text("registration.loadError"),
                            LanguageManager.text("registration.databaseError"),
                            JOptionPane.ERROR_MESSAGE
                    );

                    ex.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    private void registerUser() {

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        String password = new String(
                passwordField.getPassword()
        );

        String confirmPassword = new String(
                confirmPasswordField.getPassword()
        );

        Campus selectedCampus =
                (Campus) campusComboBox.getSelectedItem();

        if (firstName.isEmpty()
                || lastName.isEmpty()
                || email.isEmpty()
                || phone.isEmpty()
                || password.isEmpty()
                || confirmPassword.isEmpty()) {

            showValidationMessage(
                    "registration.required"
            );

            return;
        }

        if (selectedCampus == null) {
            showValidationMessage("registration.selectCampus");
            return;
        }

        if (!phone.matches("^[0-9+()\\- ]{7,20}$")) {
            showValidationMessage("registration.invalidPhone");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            showValidationMessage("registration.invalidEmail");
            return;
        }

        if (password.length() < 8) {
            showValidationMessage("registration.shortPassword");
            return;
        }

        if (password.length() > 256) {
            showValidationMessage("registration.longPassword");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showValidationMessage("registration.passwordMismatch");
            return;
        }

        User user = new User();

        user.setCampusId(selectedCampus.getCampusId());
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);

        user.setUserRole("STUDENT");
        user.setAccountStatus("ACTIVE");

        registerButton.setEnabled(false);

        registerButton.setText(
                LanguageManager.text("registration.registering")
        );

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {

            @Override
            protected Integer doInBackground() throws SQLException {
                return userService.registerUser(user, password);
            }

            @Override
            protected void done() {

                registerButton.setEnabled(true);

                registerButton.setText(
                        LanguageManager.text("registration.create")
                );

                try {

                    int userId = get();

                    if (userId > 0) {

                        JOptionPane.showMessageDialog(
                                RegistrationFrame.this,
                                LanguageManager.text("registration.success"),
                                LanguageManager.text("registration.successTitle"),
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        dispose();

                    } else {

                        showValidationMessage(
                                "registration.failed"
                        );
                    }

                } catch (Exception ex) {

                    Throwable cause = ex.getCause();

                    if (cause instanceof IllegalArgumentException) {

                        JOptionPane.showMessageDialog(
                                RegistrationFrame.this,
                                cause.getMessage(),
                                LanguageManager.text("registration.validationTitle"),
                                JOptionPane.WARNING_MESSAGE
                        );

                    } else {

                        JOptionPane.showMessageDialog(
                                RegistrationFrame.this,
                                LanguageManager.text("registration.error"),
                                LanguageManager.text("registration.errorTitle"),
                                JOptionPane.ERROR_MESSAGE
                        );

                        ex.printStackTrace();
                    }
                }
            }
        };

        worker.execute();
    }

    private void showValidationMessage(String key) {

        JOptionPane.showMessageDialog(
                this,
                LanguageManager.text(key),
                LanguageManager.text("registration.validationTitle"),
                JOptionPane.WARNING_MESSAGE
        );
    }
}
