package javafx.controller;

import database.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.model.UsersManage;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.utils.HashUtils;

import java.sql.*;
import java.util.UUID;

import static javafx.utils.SceneUtil.switchScene;

public class UserManagementController {
    @FXML private ListView<UsersManage> userListView;
    @FXML private Label nameTextField;
    @FXML private Label emailTextField;
    @FXML private Label phoneTextField;
    @FXML private Label userIDTextField;
    @FXML private ImageView wizardImage;
    @FXML private Button deleteButton;
    @FXML private Label previewID;
    @FXML private Label previewName;
    @FXML private Label previewEmail;
    @FXML private Label previewPhone;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label errorMessage;

    private ObservableList<UsersManage> userList;


    @FXML
    public void initialize() {
        // Load users into the ListView
        userList = FXCollections.observableArrayList();
        loadUsersFromDatabase();
        userListView.setItems(userList);

        // Set an event listener on the ListView to display user credentials when a user is selected
        userListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                displayUserDetails(newValue);
            }
        });
    }

    private void loadUsersFromDatabase() {

        String query = "SELECT id, username, email, phone FROM users";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                String name = rs.getString("username");
                String email = rs.getString("email");
                String phone = rs.getString("phone");
                UsersManage user = new UsersManage(id,name,email,phone);
                userList.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayUserDetails(UsersManage user) {

        nameTextField.setText(user.getName());
        emailTextField.setText(user.getEmail());
        userIDTextField.setText(user.getId());
        phoneTextField.setText(user.getPhone());

        previewEmail.setVisible(true);
        previewID.setVisible(true);
        previewName.setVisible(true);
        previewPhone.setVisible(true);
        deleteButton.setVisible(true);
        wizardImage.setVisible(true);
    }

    @FXML
    private void handleDeleteUser() {
        UsersManage selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            deleteUser(selectedUser);
            userList.remove(selectedUser);
            clearUserDetails();
        }

    }

    private void deleteUser(UsersManage user) {
        String query = "DELETE FROM users WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getId());
            stmt.executeUpdate();
            System.out.println("User deleted: " + user.getName());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddUser() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        int startId = 800000001;
        try (Connection connection = Database.getConnection()) {
            String query = "SELECT MAX(CAST(id AS INTEGER)) FROM users WHERE CAST(id as INTEGER) >= 800000001";
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {
                if (resultSet.next() && resultSet.getInt(1) >= 800000001) {
                    startId = resultSet.getInt(1) + 1;
                }
            }
        } catch (SQLException e) {
            errorMessage.setText("Database Error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        String id = String.valueOf(startId);

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            errorMessage.setText("All fields are required!");
            return;
        }

        // Validate username format
        if (!username.matches("^[A-Za-z0-9_]+$")) {
            errorMessage.setText("Username can only contain letters, numbers and underscores.");
            return;
        }

        // Validate password requirements
        if (password.length() < 8 || !password.chars().anyMatch(Character::isUpperCase)) {
            errorMessage.setText("Password must be at least 8 characters long and contain at least an uppercase");
            return;
        }

        // Validate phone
        if (!phone.matches("\\d+")) {
            errorMessage.setText("Phone number must contain only digits!");
            return;
        }

        // Validate email format
        if (!email.contains("@") || !email.contains(".")) {
            errorMessage.setText("Please enter a valid email address!");
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
                        errorMessage.setText("Email is already used, please choose another one!");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            errorMessage.setText("Database error: " + e.getMessage());
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
                        errorMessage.setText("Phone number is already used, please choose another one!");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            errorMessage.setText("Database error: " + e.getMessage());
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
                        errorMessage.setText("Username is already taken, please choose another one!");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            errorMessage.setText("Database error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        String hashedPassword = HashUtils.hashPassword(password);

        try (Connection connection = Database.getConnection()) {
            String insertUser = "INSERT INTO users (id, username, password, email, phone, userType) VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement statement = connection.prepareStatement(insertUser)) {
                statement.setString(1, id);
                statement.setString(2, username);
                statement.setString(3, hashedPassword);
                statement.setString(4, email);
                statement.setString(5, phone);
                statement.setString(6, "Normal");

                int rowsAffected = statement.executeUpdate();
                if (rowsAffected > 0) {
                    errorMessage.setText("User successfully registered!");
                    UsersManage newUser = new UsersManage(id, username, email, phone);
                    userList.add(newUser);
                    clearFields();
                } else {
                    errorMessage.setText("Registration failed!");
                }
            }
        } catch (SQLException e) {
            errorMessage.setText("Database error: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @FXML
    private void handleLogout() {
        switchScene("login.fxml");
    }

    @FXML
    private void handleHome() {
        switchScene("AdminHomePage.fxml");

    }


    private void clearUserDetails() {
        // Clear the text fields and hide the delete button
        userIDTextField.setText("");
        nameTextField.setText("");
        emailTextField.setText("");
        phoneTextField.setText("");
        deleteButton.setVisible(false);
        wizardImage.setVisible(false);
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        emailField.clear();
        phoneField.clear();
        errorMessage.setText("");
    }

}