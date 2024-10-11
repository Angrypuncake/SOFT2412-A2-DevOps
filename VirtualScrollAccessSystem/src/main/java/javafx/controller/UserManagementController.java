package javafx.controller;
import javafx.model.User1;

import javafx.MainApp;
import database.Database;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.utils.HashUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.model.UserSession;

import java.sql.DriverManager;
import java.sql.ResultSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.UUID;
import javafx.utils.HashUtils;


import static javafx.utils.AppConstants.DEFAULT_WINDOW_HEIGHT;
import static javafx.utils.AppConstants.DEFAULT_WINDOW_WIDTH;

public class UserManagementController {
    @FXML private ListView<User1> userListView;
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

    private ObservableList<User1> userList;


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
                User1 user = new User1(id,name,email,phone);
                userList.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayUserDetails(User1 user) {

        nameTextField.setText(user.getName());
        emailTextField.setText(user.getEmail());
        userIDTextField.setText(user.getEmail());
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
        User1 selectedUser = userListView.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            deleteUser(selectedUser);
            userList.remove(selectedUser);
            clearUserDetails();
        }

    }

    private void deleteUser(User1 user) {
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
        String id = UUID.randomUUID().toString();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            errorMessage.setText("All fields are required!");
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
                    User1 newUser = new User1(id, username, email, phone);
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
        try {
            UserSession.endSession();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javafx/login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = MainApp.getPrimaryStage();
            Scene loginScene = new Scene(loginRoot, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);

            stage.setScene(loginScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void handleHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javafx/AdminHomePage.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = MainApp.getPrimaryStage();
            Scene adminpage = new Scene(loginRoot, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);

            stage.setScene(adminpage);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

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