
package za.ac.ctu.campusrent.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Equipment catalogue screen. **/
public class EquipmentPanel extends JPanel {

    private JTable equipmentTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;

    public EquipmentPanel() {

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel heading = new JLabel("Equipment Catalogue");
        heading.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel topPanel = new JPanel(new BorderLayout(0, 20));
        topPanel.setOpaque(false);
        topPanel.add(heading, BorderLayout.NORTH);
        topPanel.add(createToolbar(), BorderLayout.CENTER);

        String[] columns = {
                "Serial Number", "Equipment Name",
                "Category", "Campus", "Daily Rate",
                "Availability"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        equipmentTable = new JTable(tableModel);
        equipmentTable.setRowHeight(32);
        equipmentTable.setFont(new Font("Arial", Font.PLAIN, 13));
        equipmentTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        equipmentTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        populateSampleData();

        JScrollPane scrollPane = new JScrollPane(equipmentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(
                new Color(225, 230, 235)
        ));

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<>(tableModel);

        equipmentTable.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {

                    private void filter() {
                        String text = searchField.getText();

                        if (text.isBlank()) {
                            sorter.setRowFilter(null);
                        } else {
                            sorter.setRowFilter(
                                    RowFilter.regexFilter(
                                            "(?i)" + java.util.regex.Pattern.quote(text)
                                    )
                            );
                        }
                    }
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e) {
                        filter();
                    }
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e) {
                        filter();
                    }
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e) {
                        filter();
                    }
                }
        );

        categoryFilter.addActionListener(e -> {

            String selected =
                    (String) categoryFilter.getSelectedItem();

            if (selected == null || selected.equals("All Categories")) {
                sorter.setRowFilter(null);
            } else {
                sorter.setRowFilter(
                        RowFilter.regexFilter(
                                "^" + selected + "$", 2
                        )
                );
            }
        });
    }

    private JPanel createToolbar() {

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.setOpaque(false);
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 34));
        categoryFilter = new JComboBox<>(new String[]{
                "All Categories",
                "LAPTOP",
                "PROJECTOR",
                "CAMERA",
                "TABLET",
                "NETWORK_KIT"
        });

        JButton addButton = new JButton("Add Equipment");
        JButton editButton = new JButton("Edit");
        JButton deleteButton = new JButton("Delete");

        addButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Equipment registration will be implemented next.")
        );
        editButton.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Equipment editing will be implemented next.")
        );
        deleteButton.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "Equipment deletion will be implemented next.")
        );

        toolbar.add(new JLabel("Search:"));
        toolbar.add(searchField);
        toolbar.add(new JLabel("Category:"));
        toolbar.add(categoryFilter);
        toolbar.add(addButton);
        toolbar.add(editButton);
        toolbar.add(deleteButton);

        return toolbar;
    }

    private void populateSampleData() {

        tableModel.addRow(new Object[]{
                "LT-0001", "Dell Latitude 5440",
                "LAPTOP", "Pretoria", "R85.00", "Available"
        });

        tableModel.addRow(new Object[]{
                "PJ-0002", "Epson EB-X49",
                "PROJECTOR", "Johannesburg", "R120.00", "Available"
        });

        tableModel.addRow(new Object[]{
                "CM-0003", "Canon EOS 250D",
                "CAMERA", "Durban", "R150.00", "Available"
        });

        tableModel.addRow(new Object[]{
                "TB-0004", "Samsung Galaxy Tab S9",
                "TABLET", "Cape Town", "R70.00", "Available"
        });

        tableModel.addRow(new Object[]{
                "NK-0005", "Cisco Lab Kit",
                "NETWORK_KIT", "Polokwane", "R95.00", "Available"
        });
    }
}
