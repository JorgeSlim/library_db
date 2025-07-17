// src/view/UserManagementPanel.java
package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import model.User;
import model.Database;

public class UserManagementPanel extends JPanel {
    private JTable userTable;
    private DefaultTableModel tableModel;
    private User currentUser;

    public UserManagementPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());

        // Create top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addButton = new JButton("Add User");
        styleButton(addButton, "primary");
        addButton.addActionListener(e -> showAddUserDialog());

        JButton editButton = new JButton("Edit User");
        styleButton(editButton, "warning");
        editButton.addActionListener(e -> showEditUserDialog());

        JButton deleteButton = new JButton("Delete User");
        styleButton(deleteButton, "danger");
        deleteButton.addActionListener(e -> deleteUser());

        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton, "secondary");
        refreshButton.addActionListener(e -> loadUsers());

        topPanel.add(addButton);
        topPanel.add(editButton);
        topPanel.add(deleteButton);
        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // Create user table
        String[] columnNames = {"ID", "Username", "Role", "Full Name", "Email", "Created At"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        userTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Username
        userTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Role
        userTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Full Name
        userTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Email
        userTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Created At

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Load users
        loadUsers();
    }

    private void styleButton(JButton button, String type) {
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        switch (type) {
            case "primary":
                button.setBackground(new Color(13, 110, 253));
                button.setForeground(Color.WHITE);
                break;
            case "success":
                button.setBackground(new Color(25, 135, 84));
                button.setForeground(Color.WHITE);
                break;
            case "danger":
                button.setBackground(new Color(220, 53, 69));
                button.setForeground(Color.WHITE);
                break;
            case "warning":
                button.setBackground(new Color(255, 193, 7));
                button.setForeground(Color.BLACK);
                break;
            case "secondary":
                button.setBackground(new Color(108, 117, 125));
                button.setForeground(Color.WHITE);
                break;
            default:
                button.setBackground(new Color(248, 249, 250));
                button.setForeground(Color.BLACK);
        }
    }

    private void loadUsers() {
        tableModel.setRowCount(0); // Clear table

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM users";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getTimestamp("created_at").toString()
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading users: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddUserDialog() {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Add New User", true);
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"admin", "librarian", "member"});
        JTextField fullNameField = new JTextField();
        JTextField emailField = new JTextField();

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleComboBox);
        panel.add(new JLabel("Full Name:"));
        panel.add(fullNameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        JButton saveButton = new JButton("Save");
        styleButton(saveButton, "success");
        saveButton.addActionListener(e -> {
            if (validateUserInput(usernameField, passwordField, fullNameField, emailField)) {
                User newUser = new User(
                        usernameField.getText(),
                        new String(passwordField.getPassword()),
                        (String) roleComboBox.getSelectedItem(),
                        fullNameField.getText(),
                        emailField.getText()
                );

                saveUser(newUser);
                dialog.dispose();
                loadUsers();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        styleButton(cancelButton, "secondary");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private boolean validateUserInput(JTextField usernameField, JPasswordField passwordField,
                                      JTextField fullNameField, JTextField emailField) {
        if (usernameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username cannot be empty",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (passwordField.getPassword().length == 0) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (fullNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Full name cannot be empty",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (emailField.getText().trim().isEmpty() || !emailField.getText().contains("@")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void saveUser(User user) {
        try (Connection conn = Database.getConnection()) {
            // Check if username already exists
            String checkSql = "SELECT id FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, user.getUsername());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username already exists",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "INSERT INTO users (username, password, role, full_name, email) " +
                    "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getRole());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getEmail());

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "User added successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving user: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEditUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM users WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("full_name"),
                        rs.getString("email")
                );
                user.setId(userId);

                JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Edit User", true);
                dialog.setSize(450, 350);
                dialog.setLocationRelativeTo(this);

                JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
                panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                JTextField usernameField = new JTextField(user.getUsername());
                JPasswordField passwordField = new JPasswordField();
                passwordField.setText(user.getPassword()); // Show existing password (insecure for production)
                JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"admin", "librarian", "member"});
                roleComboBox.setSelectedItem(user.getRole());
                JTextField fullNameField = new JTextField(user.getFullName());
                JTextField emailField = new JTextField(user.getEmail());

                // Disable username editing (usernames should typically not be changed)
                usernameField.setEnabled(false);

                panel.add(new JLabel("Username:"));
                panel.add(usernameField);
                panel.add(new JLabel("Password:"));
                panel.add(passwordField);
                panel.add(new JLabel("Role:"));
                panel.add(roleComboBox);
                panel.add(new JLabel("Full Name:"));
                panel.add(fullNameField);
                panel.add(new JLabel("Email:"));
                panel.add(emailField);

                JButton saveButton = new JButton("Save");
                styleButton(saveButton, "primary");
                saveButton.addActionListener(e -> {
                    if (validateUserInput(usernameField, passwordField, fullNameField, emailField)) {
                        user.setPassword(new String(passwordField.getPassword()));
                        user.setRole((String) roleComboBox.getSelectedItem());
                        user.setFullName(fullNameField.getText());
                        user.setEmail(emailField.getText());

                        updateUser(user);
                        dialog.dispose();
                        loadUsers();
                    }
                });

                JButton cancelButton = new JButton("Cancel");
                styleButton(cancelButton, "secondary");
                cancelButton.addActionListener(e -> dialog.dispose());

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                buttonPanel.add(cancelButton);
                buttonPanel.add(saveButton);

                dialog.add(panel, BorderLayout.CENTER);
                dialog.add(buttonPanel, BorderLayout.SOUTH);
                dialog.setVisible(true);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading user details: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUser(User user) {
        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE users SET password = ?, role = ?, full_name = ?, email = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getRole());
            stmt.setString(3, user.getFullName());
            stmt.setString(4, user.getEmail());
            stmt.setInt(5, user.getId());

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "User updated successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating user: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);

        // Don't allow deletion of the current user
        if (userId == currentUser.getId()) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account while logged in",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user '" + username + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Database.getConnection()) {
                // First check if user has any active book loans
                String checkLoansSql = "SELECT COUNT(*) FROM book_loans WHERE user_id = ? AND status = 'borrowed'";
                PreparedStatement checkStmt = conn.prepareStatement(checkLoansSql);
                checkStmt.setInt(1, userId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Cannot delete user with active book loans. Please return all books first.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String sql = "DELETE FROM users WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, userId);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "User deleted successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadUsers();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting user: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}