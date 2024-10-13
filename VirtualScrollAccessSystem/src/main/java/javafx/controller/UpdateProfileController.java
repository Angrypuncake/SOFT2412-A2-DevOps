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
