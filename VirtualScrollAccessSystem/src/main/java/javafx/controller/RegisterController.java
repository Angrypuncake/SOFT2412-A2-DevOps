package javafx.controller;

import database.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.utils.HashUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static javafx.utils.SceneUtil.switchScene;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label errorMessageLabel;

    // Method called when Register button is clicked
    @FXML
    public void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        String id = UUID.randomUUID().toString();

        // Validate input fields
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            errorMessageLabel.setText("All fields are required!");
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
            String insertUser = "INSERT INTO users (id, username, password, email, phone, userType) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(insertUser)) {
                statement.setString(1, id);
                statement.setString(2, username);
                statement.setString(3, hashedPassword);  // Store the hashed password
                statement.setString(4, email);
                statement.setString(5, phone);
                statement.setString(6, "Normal");  // Default user type is "Normal"

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    errorMessageLabel.setText("User successfully registered!");
                    clearFields();  // Clear fields after successful registration
                    // Go to main homepage
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
