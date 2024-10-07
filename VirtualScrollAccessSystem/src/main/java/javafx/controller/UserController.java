package javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.model.User;
import java.util.UUID;

//Database
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import database.Database;

public class UserController {

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

        // Proceed with registration (this is where you'd add database logic)
        try (Connection connection = Database.getConnection()) {
            String insertUser = "INSERT INTO users (id, username, password, email, phone, userType) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(insertUser)) {
                statement.setString(1, id);
                statement.setString(2, username);
                statement.setString(3, password); // We should hash the password as storing raw password isnt safe
                statement.setString(4, email);
                statement.setString(5, phone);
                statement.setString(6, "Normal"); //defaulting to normal, we can change this later if needed

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    errorMessageLabel.setText("User successfully registered!");
                    System.out.println("User " + username + " successfully registered!");
                } else {
                    errorMessageLabel.setText("Registration failed!");
                }
            }
        } catch (SQLException e) {
            errorMessageLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }


        // Example of adding a new user (could be extended with actual DB operations)
//        User newUser = new User(id, username, password, email, phone);
//        System.out.println("Registered user: " + newUser.getUsername());
//        errorMessageLabel.setText("Registration successful!");
    }
}
