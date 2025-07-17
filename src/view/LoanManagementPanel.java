package view;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import model.Database;
import model.User;

public class LoanManagementPanel extends JPanel {
    private JTable loanTable;
    private DefaultTableModel tableModel;
    private User currentUser;

    public LoanManagementPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());

        // Create top panel with buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add Return Book button (only for staff)
        if (!currentUser.getRole().equals("member")) {
            JButton returnButton = new JButton("Return Book");
            styleButton(returnButton, "success");
            returnButton.addActionListener(e -> returnBook());
            topPanel.add(returnButton);
        }

        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton, "secondary");
        refreshButton.addActionListener(e -> loadLoans());

        topPanel.add(refreshButton);

        add(topPanel, BorderLayout.NORTH);

        // Create loan table
        String[] columnNames = {"Loan ID", "Book Title", "Borrower", "Loan Date", "Due Date", "Status", "Return Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // Column 0: ID (Integer), Columns 3-6: Dates (Date), others String
                if (columnIndex == 0) return Integer.class;
                if (columnIndex >= 3 && columnIndex <= 6) return java.sql.Date.class;
                return String.class;
            }
        };

        loanTable = new JTable(tableModel);
        loanTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        loanTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        loanTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // Loan ID
        loanTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Book Title
        loanTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Borrower
        loanTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Loan Date
        loanTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Due Date
        loanTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Status
        loanTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Return Date

        // Add color coding for status
        loanTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);

                String status = (String) table.getModel().getValueAt(row, 5);
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                    c.setForeground(table.getSelectionForeground());
                } else if ("Overdue".equals(status)) {
                    c.setBackground(new Color(255, 200, 200)); // Light red
                    c.setForeground(Color.BLACK);
                } else if ("Returned".equals(status)) {
                    c.setBackground(new Color(200, 255, 200)); // Light green
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(loanTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Load loans
        loadLoans();
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

    private void loadLoans() {
        tableModel.setRowCount(0); // Clear table

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT bl.id, b.title, u.username, bl.loan_date, bl.due_date, " +
                    "CASE WHEN bl.status = 'returned' THEN 'Returned' " +
                    "WHEN bl.due_date < CURRENT_DATE() THEN 'Overdue' " +
                    "ELSE 'Borrowed' END AS status, " +
                    "bl.return_date " +
                    "FROM book_loans bl " +
                    "JOIN books b ON bl.book_id = b.id " +
                    "JOIN users u ON bl.user_id = u.id " +
                    "ORDER BY bl.loan_date DESC";

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("username"),
                        rs.getDate("loan_date"),
                        rs.getDate("due_date"),
                        rs.getString("status"),
                        rs.getDate("return_date")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading loans: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void returnBook() {
        int selectedRow = loanTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a loan to return",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int loanId = (int) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 5);

        if ("Returned".equals(status)) {
            JOptionPane.showMessageDialog(this, "This book is already returned",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to mark this book as returned?",
                "Confirm Return", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            processReturn(loanId);
        }
    }

    private void processReturn(int loanId) {
        try (Connection conn = Database.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);

            try {
                // 1. Get book ID from loan
                String getLoanSql = "SELECT book_id FROM book_loans WHERE id = ? FOR UPDATE";
                PreparedStatement getLoanStmt = conn.prepareStatement(getLoanSql);
                getLoanStmt.setInt(1, loanId);
                ResultSet rs = getLoanStmt.executeQuery();

                if (rs.next()) {
                    int bookId = rs.getInt("book_id");

                    // 2. Update loan status
                    String updateLoanSql = "UPDATE book_loans SET return_date = CURRENT_DATE(), status = 'returned' WHERE id = ?";
                    PreparedStatement updateLoanStmt = conn.prepareStatement(updateLoanSql);
                    updateLoanStmt.setInt(1, loanId);
                    updateLoanStmt.executeUpdate();

                    // 3. Update book availability
                    String updateBookSql = "UPDATE books SET available_quantity = available_quantity + 1 WHERE id = ?";
                    PreparedStatement updateBookStmt = conn.prepareStatement(updateBookSql);
                    updateBookStmt.setInt(1, bookId);
                    updateBookStmt.executeUpdate();

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Book returned successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadLoans(); // Refresh loan list
                }
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error returning book: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}