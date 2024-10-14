package javafx.controller;


import javafx.fxml.FXML;
import javafx.model.UserSession;
import javafx.scene.control.Alert;

import static javafx.utils.SceneUtil.switchScene;

public class AdminController {

    @FXML
    public void handleUserManagement() {
        switchScene("UserManagement.fxml");
    }

    public void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleScrollManagement(){
        switchScene("ScrollManagement.fxml");
    }
    public void handleProfile(){
        switchScene("updateProfile.fxml");
    }

    public void handleScrollFinder(){
        switchScene("ScrollFinder.fxml");
    }

    public void handleScrollStats(){
        switchScene("ScrollStats.fxml");
    }

    public void handleLogout(){
        // End session then logout
        UserSession.endSession();
        switchScene("login.fxml");
    }




}
