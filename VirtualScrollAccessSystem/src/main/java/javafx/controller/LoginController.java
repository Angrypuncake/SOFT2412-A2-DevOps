package javafx.controller;

import javafx.event.ActionEvent;
import javafx.utils.HashUtils;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import database.Database;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.model.UserSession;
import javafx.stage.Stage;
import java.io.IOException;

import static javafx.utils.AppConstants.DEFAULT_WINDOW_HEIGHT;
import static javafx.utils.AppConstants.DEFAULT_WINDOW_WIDTH;

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

                            System.out.println("User session started for: " + username + " with role: " + role);

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
        try {
            // Load the Register.fxml file
            Parent registerRoot = FXMLLoader.load(getClass().getResource("/javafx/register.fxml"));

            // Get the current stage (the window) and set the scene to the register page
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(registerRoot, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT));
            stage.setTitle("Register");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleGuestLogin(ActionEvent actionEvent) {
        UserSession.startSession("Guest", "Guest", "Guest");

        try {
            Parent registerRoot = FXMLLoader.load(getClass().getResource("/javafx/GuestHomePage.fxml"));

            // Get the current stage (the window) and set the scene to the register page
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(registerRoot, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT));
            stage.setTitle("GuestHomePage");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleNormalLogin(){
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

    private void handleAdminLogin(){
        try {
            Parent registerRoot = FXMLLoader.load(getClass().getResource("/javafx/AdminHomePage.fxml"));

            // Get the current stage (the window) and set the scene to the register page
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(registerRoot, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT));
            stage.setTitle("AdminHomePage");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
