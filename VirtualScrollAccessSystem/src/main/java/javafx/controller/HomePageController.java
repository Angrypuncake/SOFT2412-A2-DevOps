package javafx.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.model.UserSession;
import javafx.scene.control.Alert;

import static javafx.utils.SceneUtil.switchScene;

public class HomePageController {

    // Shared methods across different views

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        UserSession.endSession();
        switchScene("login.fxml");
    }

    // User-specific methods
    @FXML
    public void handleProfile(ActionEvent actionEvent) {
        switchScene("updateProfile.fxml");
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
        // See if the user is a user or admin
        UserSession userSession = UserSession.getInstance();
        if(userSession.isGuest()){
            switchScene("GuestRegisterPrompt.fxml");
        }
        else{
            switchScene("MockScrollManagement.fxml");
        }
    }

    public void handleScrollFinder(ActionEvent actionEvent) {
        switchScene("MockScrollFinder.fxml");
    }


    public void handleGuestRegister(ActionEvent actionEvent) {
        switchScene("register.fxml");
    }

    public void handleGuestGoBack(ActionEvent actionEvent) {
        switchScene("GuestHomePage.fxml");
    }
}
