// src/Main.java
import view.LoginForm;

public class Main {
    public static void main(String[] args) {
        // Initialize database (create tables if they don't exist)
        try {
            model.Database.initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Show login form
        LoginForm loginForm = new LoginForm();
        loginForm.setVisible(true);
    }
}
