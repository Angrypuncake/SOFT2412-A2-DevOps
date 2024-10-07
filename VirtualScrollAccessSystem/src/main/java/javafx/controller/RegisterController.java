package javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.model.User;
import database.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

import javafx.stage.Stage;
import javafx.utils.HashUtils;

import static javafx.utils.AppConstants.DEFAULT_WINDOW_HEIGHT;
import static javafx.utils.AppConstants.DEFAULT_WINDOW_WIDTH;

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
        try {
            Parent registerRoot = FXMLLoader.load(getClass().getResource("/javafx/UserHomePage.fxml"));

            // Get the current stage (the window) and set the scene to the register page
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(registerRoot, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT));
            stage.setTitle("UserHomePage");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
