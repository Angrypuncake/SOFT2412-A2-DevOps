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

public class AdminController {

    @FXML
    public void handleUserManagement() {
        try {
            // Step 2: Load the login page FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javafx/UserManagement.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = MainApp.getPrimaryStage();
            Scene loginScene = new Scene(loginRoot, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);

            // Step 4: Set the login scene and show it
            stage.setScene(loginScene);
            stage.show();

        } catch (IOException e) {
            showErrorAlert("Error loading the User Management page.");
            e.printStackTrace();
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleScrollManagement(){
        //
    }
    public void handleProfile(){
        //
    }

    public void handleScrollFinder(){
        //
    }

    public void handleScrollStats(){
        //
    }

    public void handleLogout(){
        //
    }




}
