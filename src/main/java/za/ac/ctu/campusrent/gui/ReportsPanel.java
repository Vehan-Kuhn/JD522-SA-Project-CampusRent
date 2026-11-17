package za.ac.ctu.campusrent.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Reports and analytics screen. **/
public class ReportsPanel extends JPanel {

    public ReportsPanel() {

        setLayout(new BorderLayout(15, 20));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel heading = new JLabel("Reports Dashboard");
        heading.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel reports = new JPanel(new GridLayout(2, 2, 15, 15));
        reports.setOpaque(false);

        reports.add(createReportCard(
                "Equipment Usage",
                "Most frequently rented equipment"
        ));
        reports.add(createReportCard("Campus Revenue", "Rental revenue per campus"
        ));
        reports.add(createReportCard("Category Revenue", "Revenue by equipment category"
        ));
        reports.add(createReportCard("Booking Status", "On-time and overdue bookings"
        ));
        add(heading, BorderLayout.NORTH);
        add(reports, BorderLayout.CENTER);
    }

    private JPanel createReportCard(String title, String description) {

        JPanel card = new JPanel(new BorderLayout(10, 15));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 230, 235)
                ),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 17));
        JLabel descriptionLabel = new JLabel(description);
        descriptionLabel.setForeground(Color.GRAY);
        JButton viewButton = new JButton("View Report");
        viewButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Report data will be connected after backend development."
                )
        );
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descriptionLabel, BorderLayout.CENTER);
        card.add(viewButton, BorderLayout.SOUTH);
        return card;
    }
}
