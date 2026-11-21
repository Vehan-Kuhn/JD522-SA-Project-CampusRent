
package za.ac.ctu.campusrent.gui;

import za.ac.ctu.campusrent.model.AuditLog;
import za.ac.ctu.campusrent.model.User;
import za.ac.ctu.campusrent.service.AuditLogService;
import za.ac.ctu.campusrent.service.UserService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Administrator-only user management screen.
 */
public class UserManagementPanel extends JPanel {

    private final User loggedInUser;
    private final UserService userService;
    private final AuditLogService auditLogService;

    private final DefaultTableModel tableModel;
    private final JTable userTable;
    private final JTextField searchField;

    private List<User> users = new ArrayList<>();

    private final Color BACKGROUND =
            new Color(245, 247, 250);

    private final Color BLUE =
            new Color(41, 128, 185);

    private static String t(String key) {
        return LanguageManager.text(key);
    }

    public UserManagementPanel(User loggedInUser) {

        if (loggedInUser == null
                || !"ADMIN".equalsIgnoreCase(
                loggedInUser.getUserRole())) {

            throw new IllegalArgumentException(
                    "Only administrators can access User Management.");
        }

        this.loggedInUser = loggedInUser;
        this.userService = new UserService();
        this.auditLogService = new AuditLogService();

        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(22, 22, 22, 22));

        JLabel heading = new JLabel(
                t("users.heading"));

        heading.setFont(
                new Font("Arial", Font.BOLD, 24));

        JLabel subtitle = new JLabel(
                t("users.subtitle"));

        subtitle.setForeground(Color.GRAY);

        JPanel headingPanel = new JPanel(
                new BorderLayout(0, 6));

        headingPanel.setOpaque(false);
        headingPanel.add(heading, BorderLayout.NORTH);
        headingPanel.add(subtitle, BorderLayout.CENTER);

        searchField = new JTextField(22);

        searchField.setFont(
                new Font("Arial", Font.PLAIN, 14));

        JButton refreshButton = createButton(
                t("common.refresh"), BLUE);

        JButton addButton = createButton(
                t("users.add"),
                new Color(39, 174, 96));

        JButton editButton = createButton(
                t("users.edit"),
                new Color(52, 152, 219));

        JButton statusButton = createButton(
                t("users.toggleStatus"),
                new Color(230, 126, 34));

        JPanel searchPanel = new JPanel(
                new FlowLayout(FlowLayout.LEFT));

        searchPanel.setOpaque(false);

        searchPanel.add(new JLabel(
                t("common.search")));

        searchPanel.add(searchField);
        searchPanel.add(refreshButton);
        searchPanel.add(addButton);
        searchPanel.add(editButton);
        searchPanel.add(statusButton);

        JPanel topPanel = new JPanel(
                new BorderLayout(0, 15));

        topPanel.setOpaque(false);

        topPanel.add(
                headingPanel,
                BorderLayout.NORTH);

        topPanel.add(
                searchPanel,
                BorderLayout.CENTER);

        tableModel = new DefaultTableModel(
                new String[]{
                        t("users.column.id"),
                        t("users.column.firstName"),
                        t("users.column.lastName"),
                        t("users.column.email"),
                        t("users.column.phone"),
                        t("users.column.role"),
                        t("users.column.status"),
                        t("users.column.campusId")
                },
                0) {

            @Override
            public boolean isCellEditable(
                    int row,
                    int column) {

                return false;
            }
        };

        userTable = new JTable(tableModel);

        userTable.setRowHeight(30);

        userTable.setFont(
                new Font("Arial", Font.PLAIN, 13));

        userTable.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13));

        userTable.getTableHeader().setReorderingAllowed(false);

        userTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<>(tableModel);

        userTable.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {

                    private void filter() {

                        String value =
                                searchField.getText().trim();

                        sorter.setRowFilter(
                                value.isEmpty()
                                        ? null
                                        : RowFilter.regexFilter(
                                        "(?i)"
                                                + java.util.regex.Pattern.quote(
                                                value)));
                    }

                    @Override
                    public void insertUpdate(
                            javax.swing.event.DocumentEvent e) {
                        filter();
                    }

                    @Override
                    public void removeUpdate(
                            javax.swing.event.DocumentEvent e) {
                        filter();
                    }

                    @Override
                    public void changedUpdate(
                            javax.swing.event.DocumentEvent e) {
                        filter();
                    }
                });

        JScrollPane scrollPane =
                new JScrollPane(userTable);

        scrollPane.setBorder(
                BorderFactory.createLineBorder(
                        new Color(220, 225, 230)));

        JLabel footer = new JLabel(
                t("users.selectPrompt"));

        footer.setForeground(Color.GRAY);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        refreshButton.addActionListener(
                e -> loadUsers());

        addButton.addActionListener(
                e -> showAddUserDialog());

        editButton.addActionListener(
                e -> editSelectedUser());

        statusButton.addActionListener(
                e -> toggleSelectedUserStatus());

        userTable.addMouseListener(
                new java.awt.event.MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            java.awt.event.MouseEvent e) {

                        if (e.getClickCount() == 2) {
                            editSelectedUser();
                        }
                    }
                });

        loadUsers();
    }

    private JButton createButton(
            String text,
            Color color) {

        JButton button = new JButton(text);

        button.setFont(
                new Font("Arial", Font.BOLD, 12));

        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR));

        button.setBorder(
                new EmptyBorder(9, 13, 9, 13));

        return button;
    }

    // ---------------------------------------------------------
    // LOAD USERS
    // ---------------------------------------------------------

    private void loadUsers() {

        setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.WAIT_CURSOR));

        new SwingWorker<List<User>, Void>() {

            @Override
            protected List<User> doInBackground()
                    throws Exception {

                return userService.getAllUsers();
            }

            @Override
            protected void done() {

                try {

                    users = get();
                    refreshTable();

                } catch (Exception ex) {

                    showError(
                            t("users.loadError")
                                    + " "
                                    + getMessage(ex));

                } finally {

                    setCursor(
                            Cursor.getDefaultCursor());
                }
            }

        }.execute();
    }

    private void refreshTable() {

        tableModel.setRowCount(0);

        for (User user : users) {

            tableModel.addRow(new Object[]{
                    user.getUserId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getPhone(),
                    translateRole(user.getUserRole()),
                    translateStatus(user.getAccountStatus()),
                    user.getCampusId()
            });
        }
    }

    private String translateRole(String role) {

        if (role == null) {
            return "";
        }

        return t("users.role." + role);
    }

    private String translateStatus(String status) {

        if (status == null) {
            return "";
        }

        return t("users.status." + status);
    }

    private User getSelectedUser() {

        int selectedRow = userTable.getSelectedRow();

        if (selectedRow < 0) {

            JOptionPane.showMessageDialog(
                    this,
                    t("users.selectFirst"),
                    t("users.noSelectionTitle"),
                    JOptionPane.WARNING_MESSAGE);

            return null;
        }

        int modelRow =
                userTable.convertRowIndexToModel(
                        selectedRow);

        int userId = (int) tableModel.getValueAt(
                modelRow, 0);

        return users.stream()
                .filter(user ->
                        user.getUserId() == userId)
                .findFirst()
                .orElse(null);
    }

    // ---------------------------------------------------------
    // ROLE COMBO BOX
    // ---------------------------------------------------------

    private JComboBox<String> createRoleCombo() {

        JComboBox<String> combo =
                new JComboBox<>(new String[]{
                        "STUDENT",
                        "STAFF",
                        "ADMIN"
                });

        // Display translated roles but keep original
        // database codes as the selected values.
        combo.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                JLabel label = (JLabel)
                        super.getListCellRendererComponent(
                                list,
                                value,
                                index,
                                isSelected,
                                cellHasFocus);

                if (value != null) {
                    label.setText(
                            translateRole(value.toString()));
                }

                return label;
            }
        });

        return combo;
    }

    // ---------------------------------------------------------
    // ADD USER
    // ---------------------------------------------------------

    private void showAddUserDialog() {

        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();

        JPasswordField passwordField =
                new JPasswordField();

        JComboBox<String> roleCombo =
                createRoleCombo();

        JPanel form = createForm(
                firstNameField,
                lastNameField,
                emailField,
                phoneField,
                passwordField,
                roleCombo);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                t("users.addTitle"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            User user = new User();

            user.setCampusId(
                    loggedInUser.getCampusId());

            user.setFirstName(
                    firstNameField.getText().trim());

            user.setLastName(
                    lastNameField.getText().trim());

            user.setEmail(
                    emailField.getText().trim());

            user.setPhone(
                    phoneField.getText().trim());

            user.setUserRole(
                    (String) roleCombo.getSelectedItem());

            user.setAccountStatus("ACTIVE");

            String password =
                    new String(passwordField.getPassword());

            runDatabaseAction(
                    t("users.adding"),
                    () -> {

                        int newUserId =
                                userService.registerUser(
                                        user,
                                        password);

                        writeAuditLog(
                                "USER_CREATED",
                                "USER",
                                newUserId,
                                t("users.auditCreated")
                                        + " "
                                        + user.getEmail());
                    });

        } catch (Exception ex) {

            showError(getMessage(ex));
        }
    }

    // ---------------------------------------------------------
    // EDIT USER
    // ---------------------------------------------------------

    private void editSelectedUser() {

        User selectedUser = getSelectedUser();

        if (selectedUser == null) {
            return;
        }

        JTextField firstNameField =
                new JTextField(
                        selectedUser.getFirstName());

        JTextField lastNameField =
                new JTextField(
                        selectedUser.getLastName());

        JTextField emailField =
                new JTextField(
                        selectedUser.getEmail());

        JTextField phoneField =
                new JTextField(
                        selectedUser.getPhone());

        JComboBox<String> roleCombo =
                createRoleCombo();

        roleCombo.setSelectedItem(
                selectedUser.getUserRole());

        JPanel form = createForm(
                firstNameField,
                lastNameField,
                emailField,
                phoneField,
                null,
                roleCombo);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                t("users.editTitle")
                        + " #"
                        + selectedUser.getUserId(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String newRole =
                (String) roleCombo.getSelectedItem();

        // Prevent an administrator from removing
        // their own administrator privileges.
        if (selectedUser.getUserId()
                == loggedInUser.getUserId()
                && !"ADMIN".equals(newRole)) {

            showError(
                    t("users.cannotDemoteSelf"));

            return;
        }

        selectedUser.setFirstName(
                firstNameField.getText().trim());

        selectedUser.setLastName(
                lastNameField.getText().trim());

        selectedUser.setEmail(
                emailField.getText().trim());

        selectedUser.setPhone(
                phoneField.getText().trim());

        selectedUser.setUserRole(newRole);

        runDatabaseAction(
                t("users.updating"),
                () -> {

                    if (!userService.updateUser(
                            selectedUser)) {

                        throw new SQLException(
                                t("users.updateFailed"));
                    }

                    writeAuditLog(
                            "USER_UPDATED",
                            "USER",
                            selectedUser.getUserId(),
                            t("users.auditUpdated")
                                    + " "
                                    + selectedUser.getEmail());
                });
    }

    // ---------------------------------------------------------
    // ACTIVATE / DEACTIVATE
    // ---------------------------------------------------------

    private void toggleSelectedUserStatus() {

        User selectedUser = getSelectedUser();

        if (selectedUser == null) {
            return;
        }

        if (selectedUser.getUserId()
                == loggedInUser.getUserId()) {

            showError(
                    t("users.cannotDeactivateSelf"));

            return;
        }

        String newStatus =
                "ACTIVE".equalsIgnoreCase(
                        selectedUser.getAccountStatus())
                        ? "INACTIVE"
                        : "ACTIVE";

        int confirm = JOptionPane.showConfirmDialog(
                this,
                t("users.confirmStatus")
                        + " "
                        + translateStatus(newStatus)
                        + "?",
                t("users.confirmStatusTitle"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        selectedUser.setAccountStatus(newStatus);

        runDatabaseAction(
                t("users.updatingStatus"),
                () -> {

                    if (!userService.updateUser(
                            selectedUser)) {

                        throw new SQLException(
                                t("users.statusUpdateFailed"));
                    }

                    writeAuditLog(
                            "USER_STATUS_CHANGED",
                            "USER",
                            selectedUser.getUserId(),
                            t("users.auditStatusChanged")
                                    + " "
                                    + newStatus);
                });
    }

    // ---------------------------------------------------------
    // ADD / EDIT FORM
    // ---------------------------------------------------------

    private JPanel createForm(
            JTextField firstName,
            JTextField lastName,
            JTextField email,
            JTextField phone,
            JPasswordField password,
            JComboBox<String> role) {

        JPanel panel = new JPanel(
                new GridLayout(0, 2, 10, 10));

        panel.add(new JLabel(
                t("users.field.firstName")));
        panel.add(firstName);

        panel.add(new JLabel(
                t("users.field.lastName")));
        panel.add(lastName);

        panel.add(new JLabel(
                t("users.field.email")));
        panel.add(email);

        panel.add(new JLabel(
                t("users.field.phone")));
        panel.add(phone);

        if (password != null) {

            panel.add(new JLabel(
                    t("users.field.password")));

            panel.add(password);
        }

        panel.add(new JLabel(
                t("users.field.role")));

        panel.add(role);

        return panel;
    }

    // ---------------------------------------------------------
    // DATABASE ACTIONS
    // ---------------------------------------------------------

    private void runDatabaseAction(
            String message,
            DatabaseAction action) {

        setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.WAIT_CURSOR));

        new SwingWorker<Void, Void>() {

            @Override
            protected Void doInBackground()
                    throws Exception {

                action.execute();
                return null;
            }

            @Override
            protected void done() {

                try {

                    get();

                    JOptionPane.showMessageDialog(
                            UserManagementPanel.this,
                            t("users.operationSuccess"),
                            t("users.successTitle"),
                            JOptionPane.INFORMATION_MESSAGE);

                    loadUsers();

                } catch (Exception ex) {

                    showError(
                            message
                                    + "\n"
                                    + getMessage(ex));

                } finally {

                    setCursor(
                            Cursor.getDefaultCursor());
                }
            }

        }.execute();
    }

    // ---------------------------------------------------------
    // AUDIT LOG
    // ---------------------------------------------------------

    private void writeAuditLog(
            String action,
            String entityType,
            int entityId,
            String details) throws SQLException {

        AuditLog log = new AuditLog();

        log.setUserId(loggedInUser.getUserId());
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);

        auditLogService.addAuditLog(log);
    }

    // ---------------------------------------------------------
    // ERROR HANDLING
    // ---------------------------------------------------------

    private String getMessage(Throwable ex) {

        Throwable cause = ex;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        return cause.getMessage() == null
                ? cause.getClass().getSimpleName()
                : cause.getMessage();
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                t("users.errorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }

    @FunctionalInterface
    private interface DatabaseAction {

        void execute() throws Exception;
    }
}
