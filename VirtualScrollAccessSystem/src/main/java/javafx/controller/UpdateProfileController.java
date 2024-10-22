package javafx.controller;

import database.Database;
import java.util.*;
import javafx.fxml.FXML;
import javafx.model.UserSession;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.utils.HashUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static javafx.utils.SceneUtil.switchScene;

public class UpdateProfileController {

    public TextField fullNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField idField;

    @FXML private Button adminShadow;
    @FXML private Button normalShadow;
    @FXML private Button guestShadow;

    @FXML
    public void initialize() {
        loadUserData();

        UserSession userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();
        if(!shadow.equals("Empty")) {
            adminShadow.setVisible(true);
            normalShadow.setVisible(true);
            guestShadow.setVisible(true);
        } else {
            normalShadow.setVisible(false);
            guestShadow.setVisible(false);
            adminShadow.setVisible(false);
        }

    }

    private void loadUserData() {
        UserSession session = UserSession.getInstance();
        String userId = session.getUserId();

        String query = "SELECT username, email, full_name, phone FROM users WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String username = resultSet.getString("username");
                    String email = resultSet.getString("email");
                    String phone = resultSet.getString("phone");
                    String full_name = resultSet.getString("full_name");

                    usernameField.setText(username);
                    emailField.setText(email);
                    phoneField.setText(phone);
                    idField.setText(userId);
                    fullNameField.setText(full_name);

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
        String newId = idField.getText();
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String phone = phoneField.getText();
        String fullname = fullNameField.getText();

        // Get the current user's ID from the session
        UserSession session = UserSession.getInstance();
        String userId = session.getUserId();

        // Validate ID (you can remove this if you don't allow ID changes)
        if (!newId.matches("^[a-z0-9]+$") && !newId.matches("^[0-9]+$")) {
            showAlert("Error", "ID must only contain lowercase letters and numbers.");
            return;
        }

        // Validate username format
        if (!username.matches("^[A-Za-z0-9_]+$")) {
            showAlert("Error", "Username can only contain letters, numbers, and underscores.");
            return;
        }

        if (!password.isEmpty()) {
            // Validate password requirements only if the field is not empty
            if (password.length() < 8 || !password.chars().anyMatch(Character::isUpperCase)) {
                showAlert("Error", "Password must be at least 8 characters long and contain at least one uppercase letter.");
                return;
            }
        }


        // Validate phone number
        if (!phone.matches("\\d+")) {
            showAlert("Error", "Phone number must contain only digits!");
            return;
        }

        // Validate email format
        if (!email.contains("@") || !email.contains(".")) {
            showAlert("Error", "Please enter a valid email address!");
            return;
        }

        // Check if the new ID is already taken
        if (!newId.equals(userId)) {
            try (Connection connection = Database.getConnection()) {
                String query = "SELECT COUNT(*) FROM users WHERE id = ?";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, newId);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next() && resultSet.getInt(1) > 0) {
                            showAlert("Error", "ID is already taken, please choose another one!");
                            return;
                        }
                    }
                }
            } catch (SQLException e) {
                showAlert("Error", "Database error: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        // Check if email is already used
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT COUNT(*) FROM users WHERE email = ? AND id != ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, email);
                statement.setString(2, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        showAlert("Error", "Email is already used, please choose another one!");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
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
                        showAlert("Error", "Phone number is already used, please choose another one!");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Check if username is taken
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT COUNT(*) FROM users WHERE username = ? AND id != ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, userId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        showAlert("Error", "Username is already taken, please choose another one!");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Prepare update statements for users table
        StringBuilder updateSQL = new StringBuilder("UPDATE users SET ");
        List<String> updateFields = new ArrayList<>();
        List<Object> updateValues = new ArrayList<>();

        if (!newId.equals(userId)) {
            updateFields.add("id = ?");
            updateValues.add(newId);
        }
        if (!username.isEmpty()) {
            updateFields.add("username = ?");
            updateValues.add(username);
        }
        if (!email.isEmpty()) {
            updateFields.add("email = ?");
            updateValues.add(email);
        }
        if (!password.isEmpty()) {
            updateFields.add("password = ?");
            updateValues.add(HashUtils.hashPassword(password));
        }
        if (!phone.isEmpty()) {
            updateFields.add("phone = ?");
            updateValues.add(phone);
        }

        if(!fullname.isEmpty()) {
            updateFields.add("full_name = ?");
            updateValues.add(fullname);
        }

        if (updateFields.isEmpty()) {
            showAlert("Information", "No changes made. Profile remains the same.");
            return;
        }

        updateSQL.append(String.join(", ", updateFields));
        updateSQL.append(" WHERE id = ?");

        try (Connection connection = Database.getConnection()) {
            String query = updateSQL.toString();
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (int i = 0; i < updateValues.size(); i++) {
                    statement.setObject(i + 1, updateValues.get(i));
                }
                statement.setString(updateValues.size() + 1, userId);

                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    showAlert("Success", "Profile updated successfully");

                    // Update uploader_id in scrolls and scrollStats
                    if (!newId.equals(userId)) {
                        updateUploaderIdInScrollsAndStats(connection, userId, newId);
                        session.setUserId(newId);
                    }

                } else {
                    showAlert("Error", "Profile update failed");
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper function to update uploader_id in scrolls and scrollStats tables
    private void updateUploaderIdInScrollsAndStats(Connection connection, String oldUserId, String newUserId) throws SQLException {
        String updateScrollsSQL = "UPDATE scrolls SET uploader_id = ? WHERE uploader_id = ?";
        String updateScrollStatsSQL = "UPDATE scrollStats SET uploader_id = ? WHERE uploader_id = ?";

        try (PreparedStatement scrollsStmt = connection.prepareStatement(updateScrollsSQL);
             PreparedStatement scrollStatsStmt = connection.prepareStatement(updateScrollStatsSQL)) {

            // Update in scrolls table
            scrollsStmt.setString(1, newUserId);
            scrollsStmt.setString(2, oldUserId);
            scrollsStmt.executeUpdate();

            // Update in scrollStats table
            scrollStatsStmt.setString(1, newUserId);
            scrollStatsStmt.setString(2, oldUserId);
            scrollStatsStmt.executeUpdate();

            System.out.println("uploader_id updated in scrolls and scrollStats tables.");
        }
    }




    // Handle the cancel button click
    @FXML
    private void handleCancel() throws IOException {
        // Go to the respective homepage based on usersession
        Stage stage = (Stage) usernameField.getScene().getWindow();
        UserSession userSession = UserSession.getInstance();
        String currentRole = userSession.getRole();
        String shadow = userSession.getShadow();

        if (currentRole.equals("Admin")) {
            if (shadow.equals("Empty") || shadow.equals("Admin")) {
                switchScene("AdminHomePage.fxml");
            } else if (shadow.equals("Normal")) {
                switchScene("UserHomePage.fxml");
            }
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

    public void handleAdminShadow(){
        UserSession userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();
        if (shadow.equals("Normal")) {
            userSession.setShadow("Admin");
            switchScene("AdminHomePage.fxml");
        } else if (shadow.equals("Guest")) {
            userSession.setShadow("Admin");
            switchScene("AdminHomePage.fxml");
        } else{
            // error message saying already in admin type
        }
    }

    public void handleNormalShadow(){
        UserSession userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();
        if (shadow.equals("Admin")) {
            userSession.setShadow("Normal");
            switchScene("UserHomePage.fxml");
        } else if (shadow.equals("Guest")) {
            userSession.setShadow("Normal");
            switchScene("UserHomePage.fxml");
        } else{
            // error message saying already in normal type
        }

    }

    public void handleGuestShadow(){
        UserSession userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();
        if (shadow.equals("Admin")) {
            userSession.setShadow("Guest");
            switchScene("UserHomePage.fxml");
        } else if (shadow.equals("Normal")) {
            userSession.setShadow("Guest");
            switchScene("GuestHomePage.fxml");
        } else{
            // error message saying already in guest type
        }
    }
}
