package javafx.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.model.User;
import javafx.model.GuestUser;

import java.util.UUID;

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
        // Example of adding a new user (could be extended with actual DB operations)
        User newUser = new User(id, username, password, email, phone);
        System.out.println("Registered user: " + newUser.getUsername());
        errorMessageLabel.setText("Registration successful!");
    }
}
