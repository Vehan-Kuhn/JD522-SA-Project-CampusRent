
package za.ac.ctu.campusrent.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Equipment booking screen. **/
public class BookingPanel extends JPanel {

    public BookingPanel() {

        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel heading = new JLabel("Create Equipment Booking");
        heading.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userField = new JTextField(20);
        JTextField equipmentField = new JTextField(20);

        JTextField startDate = new JTextField("2026-11-20");
        JTextField dueDate = new JTextField("2026-11-22");

        JComboBox<String> category = new JComboBox<>(
                new String[]{
                        "Student",
                        "Staff"
                }
        );

        addField(form, gbc, 0, "User ID:", userField);
        addField(form, gbc, 1, "Equipment Serial:", equipmentField);
        addField(form, gbc, 2, "Customer Type:", category);
        addField(form, gbc, 3, "Start Date:", startDate);
        addField(form, gbc, 4, "Due Date:", dueDate);

        JButton createButton = new JButton("Create Booking");

        createButton.setBackground(new Color(41, 128, 185));
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);

        gbc.gridx = 1;
        gbc.gridy = 5;

        form.add(createButton, gbc);

        createButton.addActionListener(e -> {
            try {

                if (userField.getText().isBlank()
                        || equipmentField.getText().isBlank()) {

                    throw new IllegalArgumentException(
                            "Please complete all required fields."
                    );
                }

                java.time.LocalDate start =
                        java.time.LocalDate.parse(startDate.getText());

                java.time.LocalDate due =
                        java.time.LocalDate.parse(dueDate.getText());

                if (due.isBefore(start)) {
                    throw new IllegalArgumentException(
                            "The due date cannot be before the start date."
                    );
                }

                JOptionPane.showMessageDialog(this, "Booking information validated successfully.", "CampusRent", JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Booking Validation",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(heading, BorderLayout.NORTH);

        add(top, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
    }
    private void addField(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String label,
            JComponent component) {

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(component, gbc);
    }
}
