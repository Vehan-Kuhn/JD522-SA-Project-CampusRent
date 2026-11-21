
package za.ac.ctu.campusrent.gui;

import za.ac.ctu.campusrent.model.User;
import za.ac.ctu.campusrent.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class LoginFrame extends JFrame {

    private final UserService userService;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;

    private final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private final Color SIDEBAR_COLOR = new Color(25, 35, 55);
    private final Color ACCENT_COLOR = new Color(41, 128, 185);

    public LoginFrame() {

        LanguageManager.loadSavedLanguage();
        ThemeManager.loadSavedTheme();

        userService = new UserService();

        setTitle(LanguageManager.text("login.title"));
        setSize(450, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        createLoginInterface();
    }

    private void createLoginInterface() {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(35, 40, 35, 40));

        // Branding
        JPanel brandingPanel = new JPanel();
        brandingPanel.setBackground(BACKGROUND_COLOR);
        brandingPanel.setLayout(
                new BoxLayout(brandingPanel, BoxLayout.Y_AXIS)
        );

        JLabel logoLabel = new JLabel("CampusRent");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 30));
        logoLabel.setForeground(SIDEBAR_COLOR);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                LanguageManager.text("login.subtitle")
        );

        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandingPanel.add(logoLabel);
        brandingPanel.add(Box.createVerticalStrut(8));
        brandingPanel.add(subtitleLabel);

        // Login form
        JPanel formPanel = new JPanel(
                new GridLayout(0, 1, 0, 10)
        );

        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(new EmptyBorder(35, 0, 20, 0));

        JLabel welcomeLabel = new JLabel(
                LanguageManager.text("login.welcome")
        );

        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 21));
        welcomeLabel.setForeground(SIDEBAR_COLOR);

        JLabel instructionLabel = new JLabel(
                LanguageManager.text("login.instruction")
        );

        instructionLabel.setForeground(Color.GRAY);

        JLabel emailLabel = new JLabel(
                LanguageManager.text("login.email")
        );

        emailField = new JTextField();
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel passwordLabel = new JLabel(
                LanguageManager.text("login.password")
        );

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));

        loginButton = new JButton(
                LanguageManager.text("login.signIn")
        );

        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(ACCENT_COLOR);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());

        // Registration button
        JButton registerButton = new JButton(
                LanguageManager.text("login.createAccount")
        );

        registerButton.setFont(new Font("Arial", Font.PLAIN, 13));
        registerButton.setForeground(ACCENT_COLOR);
        registerButton.setBackground(BACKGROUND_COLOR);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        registerButton.addActionListener(e -> {

            RegistrationFrame registrationFrame =
                    new RegistrationFrame();

            registrationFrame.setLocationRelativeTo(this);
            registrationFrame.setVisible(true);
        });

        formPanel.add(welcomeLabel);
        formPanel.add(instructionLabel);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(loginButton);
        formPanel.add(registerButton);

        // Footer
        JLabel footerLabel = new JLabel(
                LanguageManager.text("login.footer"),
                SwingConstants.CENTER
        );

        footerLabel.setForeground(Color.GRAY);
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        mainPanel.add(brandingPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(footerLabel, BorderLayout.SOUTH);

        add(mainPanel);

        ThemeManager.apply(this);
    }

    private void performLogin() {

        String email = emailField.getText().trim();

        String password = new String(
                passwordField.getPassword()
        );

        if (email.isEmpty() || password.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    LanguageManager.text("login.required"),
                    LanguageManager.text("login.missingTitle"),
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText(
                LanguageManager.text("login.signingIn")
        );

        SwingWorker<User, Void> worker = new SwingWorker<>() {

            @Override
            protected User doInBackground() throws SQLException {
                return userService.authenticate(email, password);
            }

            @Override
            protected void done() {

                loginButton.setEnabled(true);
                loginButton.setText(
                        LanguageManager.text("login.signIn")
                );

                passwordField.setText("");

                try {

                    User authenticatedUser = get();

                    if (authenticatedUser == null) {

                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                LanguageManager.text("login.invalid"),
                                LanguageManager.text("login.failedTitle"),
                                JOptionPane.ERROR_MESSAGE
                        );

                        return;
                    }

                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            LanguageManager.text("login.success")
                                    + " "
                                    + authenticatedUser.getFirstName()
                                    + "!"
                    );

                    MainFrame mainFrame =
                            new MainFrame(authenticatedUser);

                    mainFrame.setVisible(true);
                    dispose();

                } catch (Exception ex) {

                    Throwable cause = ex;

                    while (cause.getCause() != null) {
                        cause = cause.getCause();
                    }

                    String errorMessage =
                            cause.getClass().getSimpleName()
                                    + "\n\n"
                                    + (cause.getMessage() == null
                                    ? LanguageManager.text("login.noErrorDetails")
                                    : cause.getMessage());

                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            errorMessage,
                            LanguageManager.text("login.errorTitle"),
                            JOptionPane.ERROR_MESSAGE
                    );

                    System.err.println("CAMPUSRENT LOGIN ERROR:");
                    cause.printStackTrace();
                }
            }
        };

        worker.execute();
    }
}
