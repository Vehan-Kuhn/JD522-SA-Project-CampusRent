
package za.ac.ctu.campusrent.gui;

import za.ac.ctu.campusrent.model.Campus;
import za.ac.ctu.campusrent.model.Equipment;
import za.ac.ctu.campusrent.model.User;
import za.ac.ctu.campusrent.service.CampusService;
import za.ac.ctu.campusrent.service.CsvService;
import za.ac.ctu.campusrent.service.EquipmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EquipmentPanel extends JPanel {

    private final User loggedInUser;
    private final boolean canManageEquipment;

    private final EquipmentService equipmentService;
    private final CampusService campusService;
    private final CsvService csvService;

    private JTable equipmentTable;
    private EquipmentTableModel tableModel;

    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private JLabel statusLabel;

    private TableRowSorter<EquipmentTableModel> sorter;

    private List<Equipment> equipmentList = new ArrayList<>();
    private Map<Integer, String> campusNames = new HashMap<>();

    public EquipmentPanel(User loggedInUser) {

        if (loggedInUser == null) {
            throw new IllegalArgumentException(
                    "A logged-in user is required."
            );
        }

        this.loggedInUser = loggedInUser;
        this.csvService = new CsvService();

        String role = loggedInUser.getUserRole()
                .trim()
                .toUpperCase(Locale.ROOT);

        this.canManageEquipment =
                role.equals("STAFF") || role.equals("ADMIN");

        equipmentService = new EquipmentService();
        campusService = new CampusService();

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel heading = new JLabel(
                LanguageManager.text("equipment.heading")
        );

        heading.setFont(new Font("Arial", Font.BOLD, 24));

        JPanel topPanel = new JPanel(
                new BorderLayout(0, 20)
        );

        topPanel.setOpaque(false);
        topPanel.add(heading, BorderLayout.NORTH);
        topPanel.add(createToolbar(), BorderLayout.CENTER);

        tableModel = new EquipmentTableModel();

        equipmentTable = new JTable(tableModel);
        equipmentTable.setRowHeight(32);
        equipmentTable.setFont(
                new Font("Arial", Font.PLAIN, 13)
        );

        equipmentTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        equipmentTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        equipmentTable.getTableHeader().setResizingAllowed(false);
        equipmentTable.getTableHeader().setReorderingAllowed(false);
        equipmentTable.setDefaultEditor(Object.class, null);
        equipmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        equipmentTable.getColumnModel().getColumn(0)
                .setPreferredWidth(150);

        equipmentTable.getColumnModel().getColumn(1)
                .setPreferredWidth(190);

        equipmentTable.getColumnModel().getColumn(2)
                .setPreferredWidth(130);

        equipmentTable.getColumnModel().getColumn(3)
                .setPreferredWidth(270);

        equipmentTable.getColumnModel().getColumn(4)
                .setPreferredWidth(120);

        equipmentTable.getColumnModel().getColumn(5)
                .setPreferredWidth(140);

        equipmentTable.setFillsViewportHeight(true);

        sorter = new TableRowSorter<>(tableModel);
        equipmentTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(equipmentTable);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(225, 230, 235)
                )
        );

        statusLabel = new JLabel(
                LanguageManager.text("equipment.loading")
        );

        statusLabel.setForeground(Color.GRAY);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        configureSearch();
        configureCategoryFilter();

        loadEquipment();
    }

    private JPanel createToolbar() {

        JPanel toolbar = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 10, 5)
        );

        toolbar.setOpaque(false);

        searchField = new JTextField(20);
        searchField.setPreferredSize(
                new Dimension(200, 34)
        );

        categoryFilter = new JComboBox<>(new String[]{
                LanguageManager.text("equipment.allCategories"),
                "LAPTOP",
                "PROJECTOR",
                "CAMERA",
                "TABLET",
                "NETWORK_KIT",
                "OTHER"
        });

        JButton refreshButton = new JButton(
                LanguageManager.text("common.refresh")
        );

        refreshButton.addActionListener(
                e -> loadEquipment()
        );

        toolbar.add(new JLabel(
                LanguageManager.text("common.search")
        ));

        toolbar.add(searchField);

        toolbar.add(new JLabel(
                LanguageManager.text("equipment.category")
        ));

        toolbar.add(categoryFilter);
        toolbar.add(refreshButton);

        if (canManageEquipment) {

            JButton addButton = new JButton(
                    LanguageManager.text("equipment.add")
            );

            addButton.addActionListener(e -> {

                AddEquipmentDialog dialog = new AddEquipmentDialog(
                        SwingUtilities.getWindowAncestor(this),
                        this::loadEquipment
                );

                dialog.setVisible(true);
            });

            JButton importButton = new JButton(
                    LanguageManager.text("equipment.importCsv")
            );

            importButton.addActionListener(
                    e -> importEquipmentCsv()
            );

            JButton exportEquipmentButton = new JButton(
                    LanguageManager.text("equipment.exportEquipment")
            );

            exportEquipmentButton.addActionListener(
                    e -> exportEquipmentCsv()
            );

            JButton exportBookingsButton = new JButton(
                    LanguageManager.text("equipment.exportBookings")
            );

            exportBookingsButton.addActionListener(
                    e -> exportBookingsCsv()
            );

            JButton editButton = new JButton(
                    LanguageManager.text("equipment.edit")
            );

            editButton.addActionListener(
                    e -> editSelectedEquipment()
            );

            JButton deleteButton = new JButton(
                    LanguageManager.text("equipment.delete")
            );

            deleteButton.addActionListener(
                    e -> deleteSelectedEquipment()
            );

            toolbar.add(importButton);
            toolbar.add(exportEquipmentButton);
            toolbar.add(exportBookingsButton);
            toolbar.add(addButton);
            toolbar.add(editButton);
            toolbar.add(deleteButton);
        }

        return toolbar;
    }

    // ---------------------------------------------------------
    // CSV IMPORT
    // ---------------------------------------------------------

    private void importEquipmentCsv() {

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle(
                LanguageManager.text("equipment.selectCsv")
        );

        if (chooser.showOpenDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path file = chooser.getSelectedFile().toPath();

        int confirm = JOptionPane.showConfirmDialog(
                this,
                LanguageManager.text("equipment.confirmImport")
                        + "\n" + file.getFileName() + "?",
                LanguageManager.text("equipment.confirmImportTitle"),
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        setCursor(
                Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        );

        statusLabel.setText(
                LanguageManager.text("equipment.importing")
        );

        new SwingWorker<CsvService.ImportResult, Void>() {

            @Override
            protected CsvService.ImportResult doInBackground()
                    throws Exception {

                return csvService.importEquipment(file);
            }

            @Override
            protected void done() {

                setCursor(Cursor.getDefaultCursor());

                try {

                    CsvService.ImportResult result = get();

                    loadEquipment();

                    String message =
                            LanguageManager.text("equipment.importCompleted")
                                    + "\n\n"
                                    + LanguageManager.text("equipment.imported")
                                    + ": " + result.getImported()
                                    + "\n"
                                    + LanguageManager.text("equipment.failed")
                                    + ": " + result.getFailed();

                    if (!result.getErrors().isEmpty()) {

                        message += "\n\n"
                                + LanguageManager.text("equipment.firstErrors")
                                + "\n";

                        message += String.join(
                                "\n",
                                result.getErrors()
                                        .stream()
                                        .limit(10)
                                        .toList()
                        );

                        if (result.getErrors().size() > 10) {

                            message += "\n"
                                    + LanguageManager.text("equipment.moreErrors")
                                    + " "
                                    + (result.getErrors().size() - 10);
                        }
                    }

                    JOptionPane.showMessageDialog(
                            EquipmentPanel.this,
                            message,
                            LanguageManager.text("equipment.importResults"),
                            result.getFailed() == 0
                                    ? JOptionPane.INFORMATION_MESSAGE
                                    : JOptionPane.WARNING_MESSAGE
                    );

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            EquipmentPanel.this,
                            getErrorMessage(ex),
                            LanguageManager.text("equipment.importFailedTitle"),
                            JOptionPane.ERROR_MESSAGE
                    );

                    statusLabel.setText(
                            LanguageManager.text("equipment.importFailed")
                    );
                }
            }

        }.execute();
    }

    // ---------------------------------------------------------
    // CSV EXPORT - EQUIPMENT
    // ---------------------------------------------------------

    private void exportEquipmentCsv() {

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle(
                LanguageManager.text("equipment.exportEquipment")
        );

        chooser.setSelectedFile(
                new java.io.File("CampusRent_Equipment.csv")
        );

        if (chooser.showSaveDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path file = chooser.getSelectedFile().toPath();

        if (!file.toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = Path.of(file.toString() + ".csv");
        }

        Path finalFile = file;

        setCursor(
                Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        );

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {

                csvService.exportEquipment(finalFile);
                return null;
            }

            @Override
            protected void done() {

                setCursor(Cursor.getDefaultCursor());

                try {

                    get();

                    JOptionPane.showMessageDialog(
                            EquipmentPanel.this,
                            LanguageManager.text("equipment.equipmentExported")
                                    + "\n"
                                    + finalFile.toAbsolutePath(),
                            LanguageManager.text("equipment.exportComplete"),
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            EquipmentPanel.this,
                            getErrorMessage(ex),
                            LanguageManager.text("equipment.exportFailed"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        }.execute();
    }

    // ---------------------------------------------------------
    // CSV EXPORT - BOOKINGS
    // ---------------------------------------------------------

    private void exportBookingsCsv() {

        JFileChooser chooser = new JFileChooser();

        chooser.setDialogTitle(
                LanguageManager.text("equipment.exportBookings")
        );

        chooser.setSelectedFile(
                new java.io.File("CampusRent_Bookings.csv")
        );

        if (chooser.showSaveDialog(this)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path file = chooser.getSelectedFile().toPath();

        if (!file.toString().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            file = Path.of(file.toString() + ".csv");
        }

        Path finalFile = file;

        setCursor(
                Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        );

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground() throws Exception {

                csvService.exportBookings(finalFile);
                return null;
            }

            @Override
            protected void done() {

                setCursor(Cursor.getDefaultCursor());

                try {

                    get();

                    JOptionPane.showMessageDialog(
                            EquipmentPanel.this,
                            LanguageManager.text("equipment.bookingsExported")
                                    + finalFile.toAbsolutePath(),
                            LanguageManager.text("equipment.exportComplete"),
                            JOptionPane.INFORMATION_MESSAGE
                    );

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            EquipmentPanel.this,
                            getErrorMessage(ex),
                            LanguageManager.text("equipment.exportFailed"),
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }

        }.execute();
    }

    // ---------------------------------------------------------
    // EDIT EQUIPMENT
    // ---------------------------------------------------------

    private void editSelectedEquipment() {

        Equipment selected = getSelectedEquipment();

        if (selected == null) {
            showNoSelectionMessage("edit");
            return;
        }

        EditEquipmentDialog dialog = new EditEquipmentDialog(
                SwingUtilities.getWindowAncestor(this),
                selected,
                this::loadEquipment
        );

        dialog.setVisible(true);
    }

    // ---------------------------------------------------------
    // DELETE EQUIPMENT
    // ---------------------------------------------------------

    private void deleteSelectedEquipment() {

        Equipment selected = getSelectedEquipment();

        if (selected == null) {
            showNoSelectionMessage("delete");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                LanguageManager.text("equipment.confirmDelete")
                        + "\n\n"
                        + selected.getEquipmentName()
                        + "\n"
                        + LanguageManager.text("equipment.serialNumber")
                        + ": " + selected.getSerialNumber()
                        + "\n\n"
                        + LanguageManager.text("equipment.deleteWarning"),
                LanguageManager.text("equipment.confirmDeleteTitle"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        setCursor(
                Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        );

        statusLabel.setText(
                LanguageManager.text("equipment.deleting")
        );

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {

            @Override
            protected Boolean doInBackground() throws SQLException {

                return equipmentService.deleteEquipment(
                        selected.getEquipmentId()
                );
            }

            @Override
            protected void done() {

                setCursor(Cursor.getDefaultCursor());

                try {

                    boolean deleted = get();

                    if (deleted) {

                        JOptionPane.showMessageDialog(
                                EquipmentPanel.this,
                                LanguageManager.text("equipment.deleted"),
                                LanguageManager.text("equipment.deletedTitle"),
                                JOptionPane.INFORMATION_MESSAGE
                        );

                        loadEquipment();

                    } else {

                        JOptionPane.showMessageDialog(
                                EquipmentPanel.this,
                                LanguageManager.text("equipment.deleteNotCompleted"),
                                LanguageManager.text("equipment.deleteNotCompletedTitle"),
                                JOptionPane.WARNING_MESSAGE
                        );

                        loadEquipment();
                    }

                } catch (Exception ex) {

                    JOptionPane.showMessageDialog(
                            EquipmentPanel.this,
                            getErrorMessage(ex),
                            LanguageManager.text("equipment.deleteErrorTitle"),
                            JOptionPane.ERROR_MESSAGE
                    );

                    statusLabel.setText(
                            LanguageManager.text("equipment.deleteFailed")
                    );
                }
            }

        };

        worker.execute();
    }

    // ---------------------------------------------------------
    // SELECTED EQUIPMENT
    // ---------------------------------------------------------

    private Equipment getSelectedEquipment() {

        int selectedViewRow = equipmentTable.getSelectedRow();

        if (selectedViewRow < 0) {
            return null;
        }

        int modelRow = equipmentTable.convertRowIndexToModel(
                selectedViewRow
        );

        return tableModel.getEquipmentAt(modelRow);
    }

    private void showNoSelectionMessage(String action) {

        String message = action.equalsIgnoreCase("edit")
                ? LanguageManager.text("equipment.selectToEdit")
                : LanguageManager.text("equipment.selectToDelete");

        JOptionPane.showMessageDialog(
                this,
                message,
                LanguageManager.text("equipment.noSelectionTitle"),
                JOptionPane.WARNING_MESSAGE
        );
    }

    // ---------------------------------------------------------
    // SEARCH
    // ---------------------------------------------------------

    private void configureSearch() {

        searchField.getDocument().addDocumentListener(
                new DocumentListener() {

                    private void filter() {
                        applyFilters();
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        filter();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        filter();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        filter();
                    }
                }
        );
    }

    private void configureCategoryFilter() {

        categoryFilter.addActionListener(
                e -> applyFilters()
        );
    }

    private void applyFilters() {

        List<RowFilter<EquipmentTableModel, Integer>> filters =
                new ArrayList<>();

        String searchText = searchField.getText().trim();

        if (!searchText.isEmpty()) {

            filters.add(
                    RowFilter.regexFilter(
                            "(?i)" + java.util.regex.Pattern.quote(searchText)
                    )
            );
        }

        String selectedCategory =
                (String) categoryFilter.getSelectedItem();

        if (selectedCategory != null
                && !selectedCategory.equals(
                LanguageManager.text("equipment.allCategories"))) {

            filters.add(
                    RowFilter.regexFilter(
                            "^" + java.util.regex.Pattern.quote(selectedCategory) + "$",
                            2
                    )
            );
        }

        if (filters.isEmpty()) {

            sorter.setRowFilter(null);

        } else {

            sorter.setRowFilter(
                    RowFilter.andFilter(filters)
            );
        }

        updateStatusLabel();
    }

    // ---------------------------------------------------------
    // LOAD EQUIPMENT
    // ---------------------------------------------------------

    private void loadEquipment() {

        statusLabel.setText(
                LanguageManager.text("equipment.loading")
        );

        setCursor(
                Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
        );

        SwingWorker<Void, Void> worker = new SwingWorker<>() {

            private List<Equipment> loadedEquipment;
            private Map<Integer, String> loadedCampuses;

            @Override
            protected Void doInBackground() throws SQLException {

                loadedEquipment = equipmentService.getAllEquipment();
                loadedCampuses = new HashMap<>();

                List<Campus> campuses =
                        campusService.getAllCampuses();

                for (Campus campus : campuses) {

                    loadedCampuses.put(
                            campus.getCampusId(),
                            campus.getCampusName()
                                    + " - "
                                    + campus.getCity()
                    );
                }

                return null;
            }

            @Override
            protected void done() {

                setCursor(Cursor.getDefaultCursor());

                try {

                    get();

                    equipmentList = loadedEquipment;
                    campusNames = loadedCampuses;

                    tableModel.setEquipment(
                            equipmentList,
                            campusNames
                    );

                    applyFilters();
                    updateStatusLabel();

                } catch (Exception ex) {

                    statusLabel.setText(
                            LanguageManager.text("equipment.loadFailed")
                    );

                    JOptionPane.showMessageDialog(
                            EquipmentPanel.this,
                            LanguageManager.text("equipment.databaseError")
                                    + "\n\n"
                                    + getErrorMessage(ex),
                            LanguageManager.text("equipment.databaseErrorTitle"),
                            JOptionPane.ERROR_MESSAGE
                    );

                    ex.printStackTrace();
                }
            }
        };

        worker.execute();
    }

    // ---------------------------------------------------------
    // ERROR HANDLING
    // ---------------------------------------------------------

    private String getErrorMessage(Exception ex) {

        Throwable cause = ex;

        while (cause != null) {

            if (cause.getMessage() != null) {
                return cause.getMessage();
            }

            cause = cause.getCause();
        }

        return LanguageManager.text("common.unknownError");
    }

    private void updateStatusLabel() {

        if (statusLabel == null || equipmentTable == null) {
            return;
        }

        statusLabel.setText(
                LanguageManager.text("equipment.showing")
                        + " "
                        + equipmentTable.getRowCount()
                        + " "
                        + LanguageManager.text("equipment.of")
                        + " "
                        + equipmentList.size()
                        + " "
                        + LanguageManager.text("equipment.records")
        );
    }

    // ---------------------------------------------------------
    // TABLE MODEL
    // ---------------------------------------------------------

    private static class EquipmentTableModel
            extends AbstractTableModel {

        private final String[] columns = {
                LanguageManager.text("equipment.serialNumber"),
                LanguageManager.text("equipment.equipmentName"),
                LanguageManager.text("equipment.category"),
                LanguageManager.text("equipment.campus"),
                LanguageManager.text("equipment.dailyRate"),
                LanguageManager.text("equipment.availability")
        };

        private List<Equipment> equipment = new ArrayList<>();

        private Map<Integer, String> campusNames =
                new HashMap<>();

        public void setEquipment(
                List<Equipment> equipment,
                Map<Integer, String> campusNames) {

            this.equipment = new ArrayList<>(equipment);
            this.campusNames = new HashMap<>(campusNames);

            fireTableDataChanged();
        }

        public Equipment getEquipmentAt(int row) {

            if (row < 0 || row >= equipment.size()) {
                return null;
            }

            return equipment.get(row);
        }

        @Override
        public int getRowCount() {
            return equipment.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {

            Equipment item = equipment.get(rowIndex);

            switch (columnIndex) {

                case 0:
                    return item.getSerialNumber();

                case 1:
                    return item.getEquipmentName();

                case 2:
                    return item.getCategory();

                case 3:
                    return campusNames.getOrDefault(
                            item.getCampusId(),
                            LanguageManager.text("equipment.campusId")
                                    + ": "
                                    + item.getCampusId()
                    );

                case 4:

                    NumberFormat currency =
                            NumberFormat.getCurrencyInstance(
                                    Locale.forLanguageTag("en-ZA")
                            );

                    return currency.format(item.getDailyRate());

                case 5:
                    return item.getAvailabilityStatus();

                default:
                    return "";
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
