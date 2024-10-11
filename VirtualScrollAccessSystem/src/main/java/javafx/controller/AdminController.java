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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.model.UserSession;

import static javafx.utils.AppConstants.DEFAULT_WINDOW_HEIGHT;
import static javafx.utils.AppConstants.DEFAULT_WINDOW_WIDTH;

public class AdminController {

    @FXML
    public void handleUserManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javafx/UserManagement.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = MainApp.getPrimaryStage();
            Scene loginScene = new Scene(loginRoot, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);

            stage.setScene(loginScene);
            stage.show();

        } catch (IOException e) {
            showErrorAlert("Error loading the User Management page.");
            e.printStackTrace();
        }
    }

    public void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleScrollManagement(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javafx/ScrollManager.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = MainApp.getPrimaryStage();
            Scene scrollpage = new Scene(loginRoot, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);

            stage.setScene(scrollpage);
            stage.show();

        } catch (IOException e) {
            showErrorAlert("Error loading the Scroll Management page.");
            e.printStackTrace();
        }
    }
    public void handleProfile(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javafx/updateProfile.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = MainApp.getPrimaryStage();
            Scene profilePage = new Scene(loginRoot, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);

            stage.setScene(profilePage);
            stage.show();

        } catch (IOException e) {
            showErrorAlert("Error loading the Profile page.");
            e.printStackTrace();
        }
    }

    public void handleScrollFinder(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javafx/ScrollFinder.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = MainApp.getPrimaryStage();
            Scene scrollfinderPage = new Scene(loginRoot, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);

            stage.setScene(scrollfinderPage);
            stage.show();

        } catch (IOException e) {
            showErrorAlert("Error loading the Scroll Finder page.");
            e.printStackTrace();
        }
    }

    public void handleScrollStats(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/javafx/ScrollStats.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = MainApp.getPrimaryStage();
            Scene statspage = new Scene(loginRoot, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);

            stage.setScene(statspage);
            stage.show();

        } catch (IOException e) {
            showErrorAlert("Error loading the Statistics page.");
            e.printStackTrace();
        }
    }

    public void handleLogout(){
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




}
