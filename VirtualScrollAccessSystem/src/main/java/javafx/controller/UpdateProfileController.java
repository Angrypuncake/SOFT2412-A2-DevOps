package javafx.controller;

import database.Database;
import javafx.fxml.FXML;
import javafx.model.UserSession;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.utils.HashUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static javafx.utils.SceneUtil.switchScene;

public class UpdateProfileController {

    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField phoneField;

    @FXML
    public void initialize() {
        loadUserData();
    }

    private void loadUserData() {
        UserSession session = UserSession.getInstance();
        String userId = session.getUserId();

        String query = "SELECT username, email, phone FROM users WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String username = resultSet.getString("username");
                    String email = resultSet.getString("email");
                    String phone = resultSet.getString("phone");

                    usernameField.setText(username);
                    emailField.setText(email);
                    phoneField.setText(phone);

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred when loading your data");
        }
    }

    // Handle the update button click
    @FXML
    private void handleUpdateProfile() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String phone = phoneField.getText();

        // Get the current user's ID from the session
        UserSession session = UserSession.getInstance();
        String userId = session.getUserId();

        // Validate username format
        if (!username.matches("^[A-Za-z0-9_]+$")) {
            showAlert("Error","Username can only contain letters, numbers and underscores.");
            return;
        }

        // Validate password requirements
        if (password.length() < 8 || !password.chars().anyMatch(Character::isUpperCase)) {
            showAlert("Error", "Password must be at least 8 characters long and contain at least an uppercase");
            return;
        }

        // Validate phone
        if (!phone.matches("\\d+")) {
            showAlert("Error","Phone number must contain only digits!");
            return;
        }

        // Validate email format
        if (!email.contains("@") || !email.contains(".")) {
            showAlert("Error","Please enter a valid email address!");
            return;
        }

        // Check if email is already used
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT COUNT(*) FROM users WHERE email = ? AND id != ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, email);
                statement.setString(2, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        showAlert("Error","Email is already used, please choose another one!");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Error","Database error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Check if phone number is unique
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT COUNT(*) FROM users WHERE phone = ? AND id != ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, phone);
                statement.setString(2, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        showAlert("Error","Phone number is already used, please choose another one!");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Error","Database error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Check if username taken
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT COUNT(*) FROM users WHERE username = ? AND id != ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, userId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        showAlert("Error","Username is already taken, please choose another one!");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Error","Database error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        StringBuilder updateSQL = new StringBuilder("UPDATE users SET ");
        boolean hasUpdates = false;

        if (!username.isEmpty()) {
            updateSQL.append("username = ?, ");
            hasUpdates = true;
        }
        if (!email.isEmpty()) {
            updateSQL.append("email = ?, ");
            hasUpdates = true;
        }
        if (!password.isEmpty()) {
            updateSQL.append("password = ?, ");
            hasUpdates = true;
        }
        if (!phone.isEmpty()) {
            updateSQL.append("phone = ?, ");
            hasUpdates = true;
        }

        if (!hasUpdates) {
            showAlert("Error", "No fields to update!");
            return;
        }

        updateSQL.setLength(updateSQL.length() - 2);  // Remove trailing comma
        updateSQL.append(" WHERE id = ?");  // Update using userId

        try (Connection connection = Database.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(updateSQL.toString())) {
                int parameterIndex = 1;

                if (!username.isEmpty()) {
                    statement.setString(parameterIndex++, username);
                }
                if (!email.isEmpty()) {
                    statement.setString(parameterIndex++, email);
                }
                if (!password.isEmpty()) {
                    statement.setString(parameterIndex++, HashUtils.hashPassword(password));
                }
                if (!phone.isEmpty()) {
                    statement.setString(parameterIndex++, phone);
                }

                // Bind the userId to the last parameter
                statement.setString(parameterIndex, userId);

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    showAlert("Success", "Profile updated successfully!");
                } else {
                    showAlert("Error", "Profile update failed.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while updating the profile.");
        }
    }



    // Handle the cancel button click
    @FXML
    private void handleCancel() throws IOException {
        // Go to the respective homepage based on usersession
        Stage stage = (Stage) usernameField.getScene().getWindow();

        String currentRole = UserSession.getInstance().getRole();

        if (currentRole.equals("Admin")) {
            switchScene("AdminHomePage.fxml");
        }
        else if (currentRole.equals("Normal")) {
            switchScene("UserHomePage.fxml");
        }
    }

    // Helper method to show alert dialogs
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
