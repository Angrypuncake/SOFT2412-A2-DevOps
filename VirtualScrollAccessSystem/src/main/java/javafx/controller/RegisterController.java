package javafx.controller;

import database.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.model.UserSession;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.utils.HashUtils;

import java.sql.*;
import java.util.UUID;

import static javafx.model.UserSession.startSession;
import static javafx.utils.SceneUtil.switchScene;

public class RegisterController {

    @FXML private TextField idField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label errorMessageLabel;
    @FXML private TextField fullNameField;

    public void initialize() {
        setDefaultId();
    }

    private void setDefaultId() {
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT MAX(CAST(id AS INTEGER)) FROM users WHERE CAST(id as INTEGER) >= 2";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                int startId = 2;
                if (resultSet.next() && resultSet.getInt(1) >= 2) {
                    startId = resultSet.getInt(1) + 1;
                }
                idField.setText(String.valueOf(startId));
            }
        } catch (SQLException e) {
            errorMessageLabel.setText("Database Error getting ID: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method called when Register button is clicked
    @FXML
    public void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String id = idField.getText();

        //Validate ID
        if (!id.matches("^[a-z0-9]+$") && !id.matches("^[0-9]+$")) {
            errorMessageLabel.setText("ID must only contain lowercases and numbers.");
            return;
        }

        // Validate input fields
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            errorMessageLabel.setText("All fields are required!");
            return;
        }

        if (fullName.isEmpty()) {
            errorMessageLabel.setText("Full name is required!");
            return;
        }

        // Validate username format
        if (!username.matches("^[A-Za-z0-9_]+$")) {
            errorMessageLabel.setText("Username can only contain letters, numbers and underscores.");
            return;
        }

        // Validate password requirements
        if (password.length() < 8 || !password.chars().anyMatch(Character::isUpperCase)) {
            errorMessageLabel.setText("Password must be at least 8 characters long and contain at least an uppercase");
            return;
        }

        // Validate phone
        if (!phone.matches("\\d+")) {
            errorMessageLabel.setText("Phone number must contain only digits!");
            return;
        }

        // Validate email format
        if (!email.contains("@") || !email.contains(".")) {
            errorMessageLabel.setText("Please enter a valid email address!");
            return;
        }

        // Check if id is already taken
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT COUNT(*) FROM users WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        errorMessageLabel.setText("ID is already taken, please choose another one!");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            errorMessageLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Check if email is already used
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT COUNT(*) FROM users WHERE email = ? AND id != ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, email);
                statement.setString(2, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        errorMessageLabel.setText("Email is already used, please choose another one!");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            errorMessageLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Check if phone number is unique
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT COUNT(*) FROM users WHERE phone = ? AND id != ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, phone);
                statement.setString(2, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        errorMessageLabel.setText("Phone number is already used, please choose another one!");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            errorMessageLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Check if username taken
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        errorMessageLabel.setText("Username is already taken, please choose another one!");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            errorMessageLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Hash the password before storing it
        String hashedPassword = HashUtils.hashPassword(password);

        // Proceed with registration and store the user in the database
        try (Connection connection = Database.getConnection()) {
            String insertUser = "INSERT INTO users (id, username, password, full_name, email, phone, userType) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(insertUser)) {
                statement.setString(1, id);
                statement.setString(2, username);
                statement.setString(3, hashedPassword);  // Store the hashed password
                statement.setString(4, fullName);
                statement.setString(5, email);
                statement.setString(6, phone);
                statement.setString(7, "Normal");  // Default user type is "Normal"

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    errorMessageLabel.setText("User successfully registered!");
                    clearFields();  // Clear fields after successful registration
                    // Go to main homepage
                    // Make sure to start session
                    UserSession.startSession(id, username, "Normal");
                    goToUserHomePage();

                } else {
                    errorMessageLabel.setText("Registration failed!");
                }
            }
        } catch (SQLException e) {
            errorMessageLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }

    }
    // Clear input fields after successful registration
    private void clearFields() {
        idField.clear();
        usernameField.clear();
        passwordField.clear();
        emailField.clear();
        phoneField.clear();
    }

    private void goToUserHomePage(){
        switchScene("UserHomePage.fxml");
    }


    public void handleBack(ActionEvent actionEvent) {
        switchScene("login.fxml");
    }
}
