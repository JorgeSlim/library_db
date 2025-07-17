package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import model.Book;
import model.Database;
import model.User;

public class BookManagementPanel extends JPanel {
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private User currentUser;
    private JPanel buttonPanel;

    public BookManagementPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());

        // Create top panel with search and buttons
        JPanel topPanel = new JPanel(new BorderLayout());

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(30);
        JButton searchButton = new JButton("Search");
        styleButton(searchButton, "primary");
        searchButton.addActionListener(e -> searchBooks());
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        topPanel.add(searchPanel, BorderLayout.WEST);

        // Button panel
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add Book");
        styleButton(addButton, "success");
        addButton.addActionListener(e -> showAddBookDialog());

        JButton editButton = new JButton("Edit Book");
        styleButton(editButton, "warning");
        editButton.addActionListener(e -> showEditBookDialog());

        JButton deleteButton = new JButton("Delete Book");
        styleButton(deleteButton, "danger");
        deleteButton.addActionListener(e -> deleteBook());

        // Only show loan button for staff roles
        if (!currentUser.getRole().equals("member")) {
            JButton loanButton = new JButton("Loan Book");
            styleButton(loanButton, "primary");
            loanButton.addActionListener(e -> showLoanBookDialog());
            buttonPanel.add(loanButton);
        }

        JButton refreshButton = new JButton("Refresh");
        styleButton(refreshButton, "secondary");
        refreshButton.addActionListener(e -> loadBooks());

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Create book table
        String[] columnNames = {"ID", "Title", "Author", "ISBN", "Genre", "Year", "Total", "Available"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 6) { // For quantity and available columns
                    return Integer.class;
                }
                return String.class;
            }
        };

        bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Title
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Author
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(120); // ISBN
        bookTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Genre
        bookTable.getColumnModel().getColumn(5).setPreferredWidth(60);  // Year
        bookTable.getColumnModel().getColumn(6).setPreferredWidth(50);  // Total
        bookTable.getColumnModel().getColumn(7).setPreferredWidth(70);  // Available

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Load books
        loadBooks();
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

    private void loadBooks() {
        tableModel.setRowCount(0); // Clear table

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, title, author, isbn, genre, publication_year, quantity, available_quantity " +
                    "FROM books ORDER BY title";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("genre"),
                        rs.getInt("publication_year"),
                        rs.getInt("quantity"),
                        rs.getInt("available_quantity")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading books: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchBooks() {
        String searchTerm = searchField.getText().trim();
        tableModel.setRowCount(0); // Clear table

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, title, author, isbn, genre, publication_year, quantity, available_quantity " +
                    "FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + searchTerm + "%");
            stmt.setString(2, "%" + searchTerm + "%");
            stmt.setString(3, "%" + searchTerm + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = {
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("genre"),
                        rs.getInt("publication_year"),
                        rs.getInt("quantity"),
                        rs.getInt("available_quantity")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error searching books: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Add New Book", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField isbnField = new JTextField();
        JTextField genreField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField quantityField = new JTextField("1");

        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Author:"));
        panel.add(authorField);
        panel.add(new JLabel("ISBN:"));
        panel.add(isbnField);
        panel.add(new JLabel("Genre:"));
        panel.add(genreField);
        panel.add(new JLabel("Publication Year:"));
        panel.add(yearField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);

        JButton saveButton = new JButton("Save");
        styleButton(saveButton, "success");
        saveButton.addActionListener(e -> {
            try {
                Book book = new Book(
                        titleField.getText(),
                        authorField.getText(),
                        isbnField.getText(),
                        genreField.getText(),
                        Integer.parseInt(yearField.getText()),
                        Integer.parseInt(quantityField.getText())
                );

                saveBook(book);
                dialog.dispose();
                loadBooks();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for year and quantity",
                        "Error", JOptionPane.ERROR_MESSAGE);
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

    private void saveBook(Book book) {
        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO books (title, author, isbn, genre, publication_year, quantity, available_quantity) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setString(4, book.getGenre());
            stmt.setInt(5, book.getPublicationYear());
            stmt.setInt(6, book.getQuantity());
            stmt.setInt(7, book.getQuantity()); // Available equals total quantity initially

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Book added successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving book: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEditBookDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to edit",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int bookId = (int) tableModel.getValueAt(selectedRow, 0);

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT * FROM books WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Book book = new Book(
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("isbn"),
                        rs.getString("genre"),
                        rs.getInt("publication_year"),
                        rs.getInt("quantity")
                );
                book.setId(bookId);

                JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Edit Book", true);
                dialog.setSize(400, 300);
                dialog.setLocationRelativeTo(this);

                JPanel panel = new JPanel(new GridLayout(7, 2, 5, 5));
                panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JTextField titleField = new JTextField(book.getTitle());
                JTextField authorField = new JTextField(book.getAuthor());
                JTextField isbnField = new JTextField(book.getIsbn());
                JTextField genreField = new JTextField(book.getGenre());
                JTextField yearField = new JTextField(String.valueOf(book.getPublicationYear()));
                JTextField quantityField = new JTextField(String.valueOf(book.getQuantity()));

                panel.add(new JLabel("Title:"));
                panel.add(titleField);
                panel.add(new JLabel("Author:"));
                panel.add(authorField);
                panel.add(new JLabel("ISBN:"));
                panel.add(isbnField);
                panel.add(new JLabel("Genre:"));
                panel.add(genreField);
                panel.add(new JLabel("Publication Year:"));
                panel.add(yearField);
                panel.add(new JLabel("Quantity:"));
                panel.add(quantityField);

                JButton saveButton = new JButton("Save");
                styleButton(saveButton, "primary");
                saveButton.addActionListener(e -> {
                    try {
                        book.setTitle(titleField.getText());
                        book.setAuthor(authorField.getText());
                        book.setIsbn(isbnField.getText());
                        book.setGenre(genreField.getText());
                        book.setPublicationYear(Integer.parseInt(yearField.getText()));
                        int newQuantity = Integer.parseInt(quantityField.getText());
                        book.setQuantity(newQuantity);

                        updateBook(book);
                        dialog.dispose();
                        loadBooks();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog, "Please enter valid numbers for year and quantity",
                                "Error", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Error loading book details: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBook(Book book) {
        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE books SET title = ?, author = ?, isbn = ?, genre = ?, " +
                    "publication_year = ?, quantity = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, book.getTitle());
            stmt.setString(2, book.getAuthor());
            stmt.setString(3, book.getIsbn());
            stmt.setString(4, book.getGenre());
            stmt.setInt(5, book.getPublicationYear());
            stmt.setInt(6, book.getQuantity());
            stmt.setInt(7, book.getId());

            stmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Book updated successfully",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating book: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int bookId = (int) tableModel.getValueAt(selectedRow, 0);
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + bookTitle + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Database.getConnection()) {
                String sql = "DELETE FROM books WHERE id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, bookId);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Book deleted successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadBooks();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error deleting book: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showLoanBookDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a book to loan",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int bookId = (int) tableModel.getValueAt(selectedRow, 0);
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 1);
        int available = (int) tableModel.getValueAt(selectedRow, 7);

        if (available <= 0) {
            JOptionPane.showMessageDialog(this, "No copies available for loan",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Loan Book", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Get list of members from database
        DefaultComboBoxModel<String> memberModel = new DefaultComboBoxModel<>();
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, username, full_name FROM users WHERE role = 'member'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                memberModel.addElement(rs.getInt("id") + " - " + rs.getString("full_name"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JComboBox<String> memberCombo = new JComboBox<>(memberModel);
        JTextField daysField = new JTextField("14"); // Default 14-day loan period

        panel.add(new JLabel("Book:"));
        panel.add(new JLabel(bookTitle));
        panel.add(new JLabel("Member:"));
        panel.add(memberCombo);
        panel.add(new JLabel("Loan Period (days):"));
        panel.add(daysField);

        JButton saveButton = new JButton("Loan");
        styleButton(saveButton, "success");
        saveButton.addActionListener(e -> {
            try {
                int days = Integer.parseInt(daysField.getText());
                if (days <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Please enter positive number of days",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String selectedMember = (String) memberCombo.getSelectedItem();
                int userId = Integer.parseInt(selectedMember.split(" - ")[0]);

                loanBook(bookId, userId, days);
                dialog.dispose();
                loadBooks(); // Refresh book list
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid number of days",
                        "Error", JOptionPane.ERROR_MESSAGE);
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

    private void loanBook(int bookId, int userId, int days) {
        try (Connection conn = Database.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);

            try {
                // 1. Check book availability
                String checkSql = "SELECT available_quantity FROM books WHERE id = ? FOR UPDATE";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setInt(1, bookId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt("available_quantity") > 0) {
                    // 2. Create loan record
                    String loanSql = "INSERT INTO book_loans (book_id, user_id, loan_date, due_date, status) " +
                            "VALUES (?, ?, CURRENT_DATE(), DATE_ADD(CURRENT_DATE(), INTERVAL ? DAY), 'borrowed')";
                    PreparedStatement loanStmt = conn.prepareStatement(loanSql);
                    loanStmt.setInt(1, bookId);
                    loanStmt.setInt(2, userId);
                    loanStmt.setInt(3, days);
                    loanStmt.executeUpdate();

                    // 3. Update book availability
                    String updateSql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setInt(1, bookId);
                    updateStmt.executeUpdate();

                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Book loaned successfully",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    conn.rollback();
                    JOptionPane.showMessageDialog(this, "Book is no longer available",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loaning book: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}