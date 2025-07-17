// src/model/Database.java
package model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "160892";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established");
            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found", e);
        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error code: " + e.getErrorCode());
            System.err.println("SQL state: " + e.getSQLState());
            throw e;
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // Check if tables exist, create if they don't
            DatabaseMetaData dbm = conn.getMetaData();
            
            // Check and create users table
            ResultSet usersTable = dbm.getTables(null, null, "users", null);
            if (!usersTable.next()) {
                String createUsersTable = "CREATE TABLE users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "role ENUM('admin', 'librarian', 'member') NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createUsersTable);
                }
                
                // Insert default admin user
                String insertAdmin = "INSERT INTO users (username, password, role, full_name, email) " +
                                    "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertAdmin)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, "admin123");
                    pstmt.setString(3, "admin");
                    pstmt.setString(4, "System Administrator");
                    pstmt.setString(5, "admin@library.com");
                    pstmt.executeUpdate();
                }
            }
            
            // Check and create books table
            ResultSet booksTable = dbm.getTables(null, null, "books", null);
            if (!booksTable.next()) {
                String createBooksTable = "CREATE TABLE books (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "author VARCHAR(100) NOT NULL, " +
                    "isbn VARCHAR(20) NOT NULL UNIQUE, " +
                    "genre VARCHAR(50) NOT NULL, " +
                    "publication_year INT, " +
                    "quantity INT NOT NULL DEFAULT 1, " +
                    "available_quantity INT NOT NULL DEFAULT 1, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createBooksTable);
                }
                
                // Insert some sample books
                String insertBooks = "INSERT INTO books (title, author, isbn, genre, publication_year, quantity) " +
                                    "VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertBooks)) {
                    // Sample book 1
                    pstmt.setString(1, "To Kill a Mockingbird");
                    pstmt.setString(2, "Harper Lee");
                    pstmt.setString(3, "9780061120084");
                    pstmt.setString(4, "Fiction");
                    pstmt.setInt(5, 1960);
                    pstmt.setInt(6, 5);
                    pstmt.addBatch();
                    
                    // Sample book 2
                    pstmt.setString(1, "1984");
                    pstmt.setString(2, "George Orwell");
                    pstmt.setString(3, "9780451524935");
                    pstmt.setString(4, "Dystopian");
                    pstmt.setInt(5, 1949);
                    pstmt.setInt(6, 3);
                    pstmt.addBatch();
                    
                    pstmt.executeBatch();
                }
            }
            
            // Check and create book_loans table
            ResultSet loansTable = dbm.getTables(null, null, "book_loans", null);
            if (!loansTable.next()) {
                String createLoansTable = "CREATE TABLE book_loans (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "book_id INT NOT NULL, " +
                    "user_id INT NOT NULL, " +
                    "loan_date DATE NOT NULL, " +
                    "due_date DATE NOT NULL, " +
                    "return_date DATE, " +
                    "status ENUM('borrowed', 'returned', 'overdue') NOT NULL, " +
                    "FOREIGN KEY (book_id) REFERENCES books(id), " +
                    "FOREIGN KEY (user_id) REFERENCES users(id)" +
                    ")";
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(createLoansTable);
                }
            }

            
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean loanBook(int bookId, int userId, int days) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Check availability
                String checkSql = "SELECT available_quantity FROM books WHERE id = ? FOR UPDATE";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setInt(1, bookId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next() && rs.getInt("available_quantity") > 0) {
                    // Create loan
                    String loanSql = "INSERT INTO book_loans (book_id, user_id, due_date, status) " +
                            "VALUES (?, ?, DATE_ADD(CURRENT_DATE(), INTERVAL ? DAY), 'borrowed')";
                    PreparedStatement loanStmt = conn.prepareStatement(loanSql);
                    loanStmt.setInt(1, bookId);
                    loanStmt.setInt(2, userId);
                    loanStmt.setInt(3, days);
                    loanStmt.executeUpdate();

                    // Update availability
                    String updateSql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setInt(1, bookId);
                    updateStmt.executeUpdate();

                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    // Add this method to your Database class
    public static boolean returnBook(int loanId) throws SQLException {
        try (Connection conn = getConnection()) {
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
                    return true;
                }
                return false;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
