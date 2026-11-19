package za.ac.ctu.campusrent.gui;
import za.ac.ctu.campusrent.model.User;
import za.ac.ctu.campusrent.service.UserService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import za.ac.ctu.campusrent.gui.MainFrame;


public class LoginFrame extends JFrame {
    private final UserService userService;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private final Color SIDEBAR_COLOR = new Color(25, 35, 55);
    private final Color ACCENT_COLOR = new Color(41, 128, 185);

    public LoginFrame() {
        ThemeManager.loadSavedTheme();
        userService = new UserService();

        setTitle("CampusRent | Login");
        setSize(450, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        createLoginInterface();
    }

    private void createLoginInterface() {
        ThemeManager.apply(this);
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(35, 40, 35, 40));
        // Branding
        JPanel brandingPanel = new JPanel();
        brandingPanel.setBackground(BACKGROUND_COLOR);
        brandingPanel.setLayout(new BoxLayout(
                brandingPanel, BoxLayout.Y_AXIS));
        JLabel logoLabel = new JLabel("CampusRent");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 30));
        logoLabel.setForeground(SIDEBAR_COLOR);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(
                "Equipment Rental Management System");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandingPanel.add(logoLabel);
        brandingPanel.add(Box.createVerticalStrut(8));
        brandingPanel.add(subtitleLabel);

        // Login form
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        formPanel.setBackground(BACKGROUND_COLOR);
        formPanel.setBorder(new EmptyBorder(35, 0, 20, 0));
        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 21));
        welcomeLabel.setForeground(SIDEBAR_COLOR);
        JLabel instructionLabel = new JLabel(
                "Please sign in to continue.");
        instructionLabel.setForeground(Color.GRAY);
        JLabel emailLabel = new JLabel("Email Address");
        emailField = new JTextField();
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel passwordLabel = new JLabel("Password");
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        loginButton = new JButton("Sign In");
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(ACCENT_COLOR);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> performLogin());
        passwordField.addActionListener(e -> performLogin());

        // Registration button
        JButton registerButton = new JButton("Create an Account");
        registerButton.setFont(new Font("Arial", Font.PLAIN, 13));
        registerButton.setForeground(ACCENT_COLOR);
        registerButton.setBackground(BACKGROUND_COLOR);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> {
            RegistrationFrame registrationFrame =
                    new RegistrationFrame();

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
                "CampusRent | Secure User Login",
                SwingConstants.CENTER);
        footerLabel.setForeground(Color.GRAY);
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        mainPanel.add(brandingPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(footerLabel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void performLogin() {

        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your email address and password.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        SwingWorker<User, Void> worker = new SwingWorker<>() {

            @Override
            protected User doInBackground() throws SQLException {
                return userService.authenticate(email, password);
            }

            @Override
            protected void done() {

                loginButton.setEnabled(true);
                loginButton.setText("Sign In");
                passwordField.setText("");

                try {
                    User authenticatedUser = get();

                    if (authenticatedUser == null) {
                        JOptionPane.showMessageDialog(
                                LoginFrame.this,
                                "Invalid email or password, or your account is inactive.",
                                "Login Failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            "Welcome, " + authenticatedUser.getFirstName() + "!"
                    );

                    MainFrame mainFrame = new MainFrame(authenticatedUser);
                    mainFrame.setVisible(true);

                    dispose();

                } catch (Exception ex) {

                    Throwable cause = ex;

                    // Unwrap SwingWorker exceptions.
                    while (cause.getCause() != null) {
                        cause = cause.getCause();
                    }

                    String errorMessage = cause.getClass().getSimpleName()
                            + "\n\n"
                            + (cause.getMessage() == null
                            ? "No additional error information."
                            : cause.getMessage());

                    JOptionPane.showMessageDialog(
                            LoginFrame.this,
                            errorMessage,
                            "Detailed Login Error",
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
