
package za.ac.ctu.campusrent.gui;

import za.ac.ctu.campusrent.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Main application window for CampusRent.
 * Navigation is controlled by the logged-in user's role.
 */
public class MainFrame extends JFrame {

    private final Color SIDEBAR_COLOR = new Color(25, 35, 55);
    private final Color ACCENT_COLOR = new Color(41, 128, 185);
    private final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private final Color TEXT_COLOR = new Color(45, 55, 72);

    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    private final User loggedInUser;
    private final String userRole;

    private JLabel dateTimeLabel;
    private Timer clockTimer;

    public MainFrame(User loggedInUser) {

        if (loggedInUser == null) {
            throw new IllegalArgumentException(
                    "A logged-in user is required."
            );
        }

        this.loggedInUser = loggedInUser;

        this.userRole = loggedInUser.getUserRole()
                .trim()
                .toUpperCase(Locale.ROOT);

        // Load saved preferences BEFORE creating the interface.
        LanguageManager.loadSavedLanguage();
        ThemeManager.loadSavedTheme();

        setTitle(LanguageManager.text("app.title"));
        setSize(1250, 780);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BACKGROUND_COLOR);

        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        cardLayout.show(contentPanel, "DASHBOARD");

        clockTimer = new Timer(1000, e -> updateDateTime());
        clockTimer.start();

        SwingUtilities.invokeLater(() -> ThemeManager.apply(this));
    }

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBackground(SIDEBAR_COLOR);

        // Branding
        JPanel branding = new JPanel(new BorderLayout());
        branding.setBackground(SIDEBAR_COLOR);
        branding.setBorder(new EmptyBorder(25, 18, 25, 10));

        JLabel logo = new JLabel("CR");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD, 28));

        JLabel title = new JLabel("CampusRent");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 21));

        branding.add(logo, BorderLayout.WEST);
        branding.add(title, BorderLayout.CENTER);

        // Navigation
        JPanel navigation = new JPanel();
        navigation.setBackground(SIDEBAR_COLOR);
        navigation.setLayout(new GridLayout(0, 1, 0, 8));
        navigation.setBorder(new EmptyBorder(15, 12, 15, 12));

        addNavigationIfAllowed(
                navigation, LanguageManager.text("nav.dashboard"), "DASHBOARD"
        );

        addNavigationIfAllowed(
                navigation, LanguageManager.text("nav.equipment"), "EQUIPMENT"
        );

        addNavigationIfAllowed(
                navigation, LanguageManager.text("nav.bookings"), "BOOKINGS"
        );

        addNavigationIfAllowed(
                navigation, LanguageManager.text("nav.returns"), "RETURNS"
        );

        addNavigationIfAllowed(
                navigation, LanguageManager.text("nav.users"), "USERS"
        );

        addNavigationIfAllowed(
                navigation, LanguageManager.text("nav.reports"), "REPORTS"
        );

        addNavigationIfAllowed(
                navigation, LanguageManager.text("nav.settings"), "SETTINGS"
        );

        // Logged-in user section
        JPanel userPanel = new JPanel(new BorderLayout(0, 12));
        userPanel.setBackground(new Color(32, 45, 68));
        userPanel.setBorder(new EmptyBorder(18, 15, 18, 15));

        JPanel userDetails = new JPanel(new BorderLayout(10, 0));
        userDetails.setBackground(new Color(32, 45, 68));

        JLabel userIcon = new JLabel("●");
        userIcon.setForeground(new Color(46, 204, 113));
        userIcon.setFont(new Font("Arial", Font.BOLD, 18));

        String fullName = loggedInUser.getFirstName()
                + " " + loggedInUser.getLastName();

        JLabel userName = new JLabel(
                "<html>" + fullName + "<br><small>"
                        + userRole + "</small></html>"
        );

        userName.setForeground(Color.WHITE);
        userName.setFont(new Font("Arial", Font.BOLD, 13));

        userDetails.add(userIcon, BorderLayout.WEST);
        userDetails.add(userName, BorderLayout.CENTER);

        // Logout button
        JButton logoutButton = new JButton(
                LanguageManager.text("nav.logout")
        );

        logoutButton.setFont(new Font("Arial", Font.BOLD, 13));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setBackground(new Color(192, 57, 43));
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setOpaque(true);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setBorder(new EmptyBorder(10, 15, 10, 15));

        logoutButton.addActionListener(e -> logout());

        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                logoutButton.setBackground(new Color(231, 76, 60));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                logoutButton.setBackground(new Color(192, 57, 43));
            }
        });

        userPanel.add(userDetails, BorderLayout.CENTER);
        userPanel.add(logoutButton, BorderLayout.SOUTH);

        sidebar.add(branding, BorderLayout.NORTH);
        sidebar.add(navigation, BorderLayout.CENTER);
        sidebar.add(userPanel, BorderLayout.SOUTH);

        return sidebar;
    }

    private void addNavigationIfAllowed(
            JPanel navigation,
            String text,
            String screen) {

        if (hasAccess(screen)) {
            navigation.add(createNavButton(text, screen));
        }
    }

    private JButton createNavButton(String text, String screen) {

        JButton button = new JButton(text);

        button.setFont(new Font("Arial", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(SIDEBAR_COLOR);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setBorder(new EmptyBorder(13, 15, 13, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> showScreen(screen));

        button.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(SIDEBAR_COLOR);
            }
        });

        return button;
    }

    /**
     * Checks whether the logged-in user's role can access a screen.
     */
    private boolean hasAccess(String screen) {

        switch (userRole) {

            case "STUDENT":
                return screen.equals("DASHBOARD")
                        || screen.equals("EQUIPMENT")
                        || screen.equals("BOOKINGS");

            case "STAFF":
                return screen.equals("DASHBOARD")
                        || screen.equals("EQUIPMENT")
                        || screen.equals("BOOKINGS")
                        || screen.equals("RETURNS")
                        || screen.equals("REPORTS");

            case "ADMIN":
                return screen.equals("DASHBOARD")
                        || screen.equals("EQUIPMENT")
                        || screen.equals("BOOKINGS")
                        || screen.equals("RETURNS")
                        || screen.equals("USERS")
                        || screen.equals("REPORTS")
                        || screen.equals("SETTINGS");

            default:
                return false;
        }
    }

    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BACKGROUND_COLOR);

        // Application header
        main.add(createHeader(), BorderLayout.NORTH);

        // Dashboard
        contentPanel.add(new DashboardPanel(), "DASHBOARD");

        // Equipment and bookings
        contentPanel.add(
                new EquipmentPanel(loggedInUser),
                "EQUIPMENT"
        );

        contentPanel.add(
                new BookingPanel(loggedInUser),
                "BOOKINGS"
        );

        // Role-specific screens
        if (hasAccess("RETURNS")) {
            contentPanel.add(
                    new ReturnsPanel(loggedInUser),
                    "RETURNS"
            );
        }

        if (hasAccess("USERS")) {
            contentPanel.add(
                    new UserManagementPanel(loggedInUser),
                    "USERS"
            );
        }

        if (hasAccess("REPORTS")) {
            contentPanel.add(
                    new ReportsPanel(),
                    "REPORTS"
            );
        }

        if (hasAccess("SETTINGS")) {
            contentPanel.add(
                    new SettingsPanel(this::refreshForLanguageChange),
                    "SETTINGS"
            );
        }

        // Attach CardLayout panel to the main window.
        main.add(contentPanel, BorderLayout.CENTER);

        return main;
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(18, 25, 18, 25));

        JLabel heading = new JLabel(
                LanguageManager.text("app.heading")
        );

        heading.setFont(new Font("Arial", Font.BOLD, 21));
        heading.setForeground(TEXT_COLOR);

        String fullName = loggedInUser.getFirstName()
                + " " + loggedInUser.getLastName();

        JLabel userLabel = new JLabel(
                LanguageManager.text("app.welcome")
                        + ", " + fullName + " (" + userRole + ")"
        );

        userLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        userLabel.setForeground(Color.GRAY);

        header.add(heading, BorderLayout.WEST);
        header.add(userLabel, BorderLayout.EAST);

        return header;
    }

    private JPanel createStatusBar() {

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(Color.WHITE);
        statusBar.setBorder(new EmptyBorder(10, 18, 10, 18));

        JLabel status = new JLabel(
                "CampusRent | " + LanguageManager.text("app.ready")
        );

        dateTimeLabel = new JLabel();
        dateTimeLabel.setForeground(Color.GRAY);

        statusBar.add(status, BorderLayout.WEST);
        statusBar.add(dateTimeLabel, BorderLayout.EAST);

        updateDateTime();

        return statusBar;
    }

    private void updateDateTime() {

        if (dateTimeLabel != null) {

            String currentTime = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern(
                            "dd MMM yyyy | HH:mm:ss",
                            LanguageManager.getLocale()
                    )
            );

            dateTimeLabel.setText(currentTime);
        }
    }

    private void showScreen(String screen) {

        if (!hasAccess(screen)) {

            JOptionPane.showMessageDialog(
                    this,
                    LanguageManager.text("dialog.accessDenied"),
                    LanguageManager.text("dialog.accessTitle"),
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        cardLayout.show(contentPanel, screen);
    }

    /**
     * Recreates the main window after the language has been changed.
     * The selected language is loaded from saved preferences.
     */
    private void refreshForLanguageChange() {

        SwingUtilities.invokeLater(() -> {

            if (clockTimer != null) {
                clockTimer.stop();
            }

            dispose();

            MainFrame refreshedFrame = new MainFrame(loggedInUser);
            refreshedFrame.setLocationRelativeTo(null);
            refreshedFrame.setVisible(true);
        });
    }

    /**
     * Confirms logout, stops the clock timer,
     * and returns to the login screen.
     */
    private void logout() {

        int choice = JOptionPane.showConfirmDialog(
                this,
                LanguageManager.text("dialog.logoutConfirm"),
                LanguageManager.text("dialog.logoutTitle"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {

            if (clockTimer != null) {
                clockTimer.stop();
            }

            dispose();

            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setLocationRelativeTo(null);
            loginFrame.setVisible(true);
        }
    }
}
