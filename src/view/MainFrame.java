// src/view/MainFrame.java
package view;

import javax.swing.*;
import java.awt.*;
import model.User;

public class MainFrame extends JFrame {
    private User currentUser;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("Library Management System - Welcome, " + user.getFullName());
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Books menu
        JMenu booksMenu = new JMenu("Books");
        JMenuItem manageBooksItem = new JMenuItem("Manage Books");
        manageBooksItem.addActionListener(e -> showBookManagementPanel());
        booksMenu.add(manageBooksItem);
        menuBar.add(booksMenu);

        // Loans menu
        JMenu loansMenu = new JMenu("Loans");
        JMenuItem manageLoansItem = new JMenuItem("Manage Loans");
        manageLoansItem.addActionListener(e -> showLoanManagementPanel());
        loansMenu.add(manageLoansItem);
        menuBar.add(loansMenu);

        // Users menu (only for admin)
        if ("admin".equals(currentUser.getRole())) {
            JMenu usersMenu = new JMenu("Users");
            JMenuItem manageUsersItem = new JMenuItem("Manage Users");
            manageUsersItem.addActionListener(e -> showUserManagementPanel());
            usersMenu.add(manageUsersItem);
            menuBar.add(usersMenu);
        }

        setJMenuBar(menuBar);

        // Set up main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome to Library Management System", 
            SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        mainPanel.add(welcomeLabel, BorderLayout.CENTER);
        
        add(mainPanel);
    }

    private void showBookManagementPanel() {
        getContentPane().removeAll();
        add(new BookManagementPanel(currentUser));
        revalidate();
        repaint();
    }

    private void showUserManagementPanel() {
        getContentPane().removeAll();
        add(new UserManagementPanel(currentUser));
        revalidate();
        repaint();
    }

    private void showLoanManagementPanel() {
        getContentPane().removeAll();
        add(new LoanManagementPanel(currentUser));  // Pass the currentUser
        revalidate();
        repaint();
    }

}
