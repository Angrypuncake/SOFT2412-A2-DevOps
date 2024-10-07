package javafx.controller;

import javafx.MainApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.model.UserSession;

import java.io.IOException;

import static javafx.utils.AppConstants.DEFAULT_WINDOW_HEIGHT;
import static javafx.utils.AppConstants.DEFAULT_WINDOW_WIDTH;

public class HomePageController {

    // Shared methods across different views

    @FXML
    private void handleLogout(ActionEvent actionEvent) {
        try {
            // Step 1: End the current user session
            UserSession.endSession();

            // Step 2: Load the login page FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javafx/login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = MainApp.getPrimaryStage();
            Scene loginScene = new Scene(loginRoot, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);

            // Step 4: Set the login scene and show it
            stage.setScene(loginScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Guest-specific methods
    @FXML
    public void handleBrowseContent() {
        System.out.println("Browsing content as guest...");
        // Logic for browsing as a guest
    }

    @FXML
    public void handleLogin() {
        System.out.println("Switching to login/register...");
        // Logic for switching to the login page
    }

    // User-specific methods
    @FXML
    public void handleProfile() {
        System.out.println("Viewing profile...");
        // Logic for viewing profile
    }

    @FXML
    public void handleUserFeatures() {
        System.out.println("Accessing user features...");
        // Logic for user-specific features
    }



    // Helper method to show a simple alert
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleScrollManagement(ActionEvent actionEvent) {
    }

    public void handleScrollFinder(ActionEvent actionEvent) {
    }

    // Admin-specific methods
    @FXML
    public void handleManageUsers() {
        System.out.println("Managing users as admin...");
        // Logic for managing users
    }

    @FXML
    public void handleSystemSettings() {
        System.out.println("Accessing system settings...");
        // Logic for accessing system settings
    }
}
