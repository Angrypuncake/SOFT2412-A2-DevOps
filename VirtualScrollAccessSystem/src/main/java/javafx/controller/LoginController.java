package javafx.controller;

import database.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.model.UserSession;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.utils.HashUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static javafx.utils.SceneUtil.switchScene;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorMessageLabel;

    // Method called when Login button is clicked
    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Validate input fields
        if (username.isEmpty() || password.isEmpty()) {
            errorMessageLabel.setText("Username and Password are required!");
            return;
        }

        // Query the database for user
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        // Retrieve the stored hashed password
                        String storedHashedPassword = resultSet.getString("password");
                        // Hash the password

                        // Verify the password (hash the input password and compare)
                        if (verifyPassword(password, storedHashedPassword)) {
                            errorMessageLabel.setText("Login successful!");

                            // Retrieve user details from the database for the session
                            String userId = resultSet.getString("id");
                            String role = resultSet.getString("userType");  // Could be "User", "Admin", etc.

                            // Start the user session with the retrieved data
                            UserSession.startSession(userId, username, role);

                            System.out.println("User session started for: " + username + " with role: " + role + "with id" + userId);

                            // Proceed to load the next screen (e.g., UserHomePage or AdminHomePage)
                            loadHomePageBasedOnRole(role);
                        } else {
                            errorMessageLabel.setText("Incorrect password!");
                        }
                    } else {
                        errorMessageLabel.setText("User not found!");
                    }
                }
            }
        } catch (SQLException e) {
            errorMessageLabel.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Simulate password hashing and verification
    private boolean verifyPassword(String inputPassword, String storedHashedPassword) {
        // Hash the input password and compare it with the stored hashed password
        String hashedInputPassword = HashUtils.hashPassword(inputPassword);
        System.out.println("comparing password: " + hashedInputPassword);
        System.out.println("Stored hashed password: " + storedHashedPassword);
        return hashedInputPassword.equals(storedHashedPassword);
    }

    public void loadHomePageBasedOnRole(String role){
        if(role.equals("Admin")){
            handleAdminLogin();
        }
        if(role.equals("Normal")){
            handleNormalLogin();
        }
    }

    @FXML
    public void switchToRegister() {
        switchScene("register.fxml");
    }

    public void handleGuestLogin(ActionEvent actionEvent) {
        UserSession.startSession("Guest", "Guest", "Guest");
        switchScene("GuestHomePage.fxml");
    }

    private void handleNormalLogin(){
        switchScene("UserHomePage.fxml");
    }

    private void handleAdminLogin(){
        switchScene("AdminHomePage.fxml");
    }

}
