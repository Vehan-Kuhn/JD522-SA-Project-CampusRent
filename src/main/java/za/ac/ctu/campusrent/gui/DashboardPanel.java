package za.ac.ctu.campusrent.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Main CampusRent dashboard. **/

public class DashboardPanel extends JPanel {

    public DashboardPanel() {

        setLayout(new BorderLayout(15, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel heading = new JLabel("Dashboard Overview");
        heading.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel statistics = new JPanel(new GridLayout(1, 4, 15, 15));
        statistics.setOpaque(false);
        statistics.add(createCard("Total Equipment", "20", "Registered items"));
        statistics.add(createCard("Active Bookings", "0", "Current rentals"));
        statistics.add(createCard("Overdue Items", "0", "Require attention"));
        statistics.add(createCard("Registered Users", "10", "Students and staff"));

        JPanel center = new JPanel(new BorderLayout(10, 20));
        center.setOpaque(false);
        JLabel activityTitle = new JLabel("Recent Activity");
        activityTitle.setFont(new Font("Arial", Font.BOLD, 18));
        JTextArea activity = new JTextArea(
                "Welcome to CampusRent!\n\n" +
                        "Your equipment rental management system is ready.\n\n" +
                        "Recent activities will appear here once the backend is connected."
        );

        activity.setFont(new Font("Arial", Font.PLAIN, 14));
        activity.setEditable(false);
        activity.setBackground(Color.WHITE);
        activity.setBorder(new EmptyBorder(20, 20, 20, 20));

        center.add(activityTitle, BorderLayout.NORTH);
        center.add(activity, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(0, 20));
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);
        top.add(statistics, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }

    private JPanel createCard(String title, String value, String description) {

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 235)),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(new Color(41, 128, 185));
        valueLabel.setFont(new Font("Arial", Font.BOLD, 30));

        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setForeground(Color.GRAY);
        descriptionLabel.setFont(new Font("Arial", Font.PLAIN, 11));

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(descriptionLabel);

        return card;
    }
}
