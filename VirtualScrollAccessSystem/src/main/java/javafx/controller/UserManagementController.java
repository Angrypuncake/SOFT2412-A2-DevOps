package javafx.controller;
import javafx.model.User1;


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


import static javafx.utils.AppConstants.DEFAULT_WINDOW_HEIGHT;
import static javafx.utils.AppConstants.DEFAULT_WINDOW_WIDTH;

public class UserManagementController {
    @FXML
    private ListView<User1> userListView;
    @FXML
    private Label nameTextField;
    @FXML
    private Label emailTextField;
    @FXML
    private Label phoneTextField;
    @FXML
    private Label userIDTextField;
    @FXML
    private ImageView wizardImage;
    @FXML
    private Button deleteButton;

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
        phoneTextField.setText(user.getEmail());

        deleteButton.setVisible(true);
        wizardImage.setVisible(true);
    }

    @FXML
    private void handleDeleteUser() {
        // LOGIC TO DELETE A USER

    }

    @FXML
    private void handleAddUser() {
        // LOGIC TO DELETE A USER

    }


    private void clearUserDetails() {
        // Clear the text fields and hide the delete button
        //userIDTextField.clear();
        //nameTextField.clear();
        //emailTextField.clear();
        //phoneTextField.clear();
        deleteButton.setVisible(false);
        wizardImage.setVisible(false);
    }

}