package javafx.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.model.UserSession;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;

import static javafx.utils.SceneUtil.switchScene;

public class HomePageController {
    @FXML
    private Button adminShadowGuest;
    @FXML
    private Button normalShadowGuest;
    @FXML
    private Button guestShadowGuest;

    // Shared methods across different views

    @FXML
    public void initialize() {
        UserSession userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();

        if (!shadow.equals("Empty")) {
            adminShadowGuest.setVisible(true);
            normalShadowGuest.setVisible(true);
            guestShadowGuest.setVisible(true);
        } else {
            adminShadowGuest.setVisible(false);
            normalShadowGuest.setVisible(false);
            guestShadowGuest.setVisible(false);
        }

    }

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
        if (userSession.isGuest()) {
            switchScene("GuestRegisterPrompt.fxml");
        } else {
            switchScene("ScrollManagement.fxml");
        }
    }

    public void handleScrollFinder(ActionEvent actionEvent) {
        switchScene("ScrollFinder.fxml");
    }


    public void handleGuestRegister(ActionEvent actionEvent) {
        switchScene("register.fxml");
    }

    public void handleGuestGoBack(ActionEvent actionEvent) {
        switchScene("GuestHomePage.fxml");

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
        } else {
            // error message saying already in guest type

        }
    }
}
