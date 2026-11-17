package za.ac.ctu.campusrent.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


 /** Main application window for CampusRent. **/

public class MainFrame extends JFrame {

    private final Color SIDEBAR_COLOR = new Color(25, 35, 55);
    private final Color ACCENT_COLOR = new Color(41, 128, 185);
    private final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    private final Color TEXT_COLOR = new Color(45, 55, 72);

    private final CardLayout cardLayout;
    private final JPanel contentPanel;

    private JLabel dateTimeLabel;
    private JLabel userLabel;

    public MainFrame() {

        setTitle("CampusRent | Equipment Rental System");
        setSize(1250, 780);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BACKGROUND_COLOR);

        setLayout(new BorderLayout());

        // Build application components
        add(createSidebar(), BorderLayout.WEST);
        add(createMainContent(), BorderLayout.CENTER);
        add(createStatusBar(), BorderLayout.SOUTH);

        // Start on dashboard
        cardLayout.show(contentPanel, "DASHBOARD");

        // Update date and time every second
        Timer timer = new Timer(1000, e -> updateDateTime());
        timer.start();
    }

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(235, 0));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setLayout(new BorderLayout());

        // Branding
        JPanel branding = new JPanel();
        branding.setBackground(SIDEBAR_COLOR);
        branding.setBorder(new EmptyBorder(25, 18, 25, 10));
        branding.setLayout(new BorderLayout());

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

        navigation.add(createNavButton("Dashboard", "DASHBOARD"));
        navigation.add(createNavButton("Equipment Catalogue", "EQUIPMENT"));
        navigation.add(createNavButton("Bookings", "BOOKINGS"));
        navigation.add(createNavButton("Returns", "RETURNS"));
        navigation.add(createNavButton("Users", "USERS"));
        navigation.add(createNavButton("Reports", "REPORTS"));
        navigation.add(createNavButton("Settings", "SETTINGS"));

        // User section
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(32, 45, 68));
        userPanel.setBorder(new EmptyBorder(18, 15, 18, 15));
        JLabel userIcon = new JLabel("●");
        userIcon.setForeground(new Color(46, 204, 113));
        userIcon.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel userName = new JLabel("Administrator");
        userName.setForeground(Color.WHITE);
        userName.setFont(new Font("Arial", Font.BOLD, 13));
        userPanel.add(userIcon, BorderLayout.WEST);
        userPanel.add(userName, BorderLayout.CENTER);
        sidebar.add(branding, BorderLayout.NORTH);
        sidebar.add(navigation, BorderLayout.CENTER);
        sidebar.add(userPanel, BorderLayout.SOUTH);

        return sidebar;
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

        button.addActionListener(e -> showScreen(screen)); button.addMouseListener(new java.awt.event.MouseAdapter() {

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

    private JPanel createMainContent() {

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BACKGROUND_COLOR);

        main.add(createHeader(), BorderLayout.NORTH);

        // Add application screens
        contentPanel.add(new DashboardPanel(), "DASHBOARD");
        contentPanel.add(new EquipmentPanel(), "EQUIPMENT");
        contentPanel.add(new BookingPanel(), "BOOKINGS");
        contentPanel.add(new PlaceholderPanel("Returns Management"), "RETURNS");
        contentPanel.add(new PlaceholderPanel("User Management"), "USERS");
        contentPanel.add(new ReportsPanel(), "REPORTS");
        contentPanel.add(new PlaceholderPanel("Application Settings"), "SETTINGS");

        main.add(contentPanel, BorderLayout.CENTER);

        return main;
    }

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(18, 25, 18, 25));

        JLabel heading = new JLabel("CampusRent Management");
        heading.setFont(new Font("Arial", Font.BOLD, 21));
        heading.setForeground(TEXT_COLOR);
        userLabel = new JLabel("Welcome, Administrator");
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

        JLabel status = new JLabel("CampusRent | System Ready");
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
                    DateTimeFormatter.ofPattern("dd MMM yyyy | HH:mm:ss")
            );

            dateTimeLabel.setText(currentTime);
        }
    }

    private void showScreen(String screen) {
        cardLayout.show(contentPanel, screen);
    }
}

