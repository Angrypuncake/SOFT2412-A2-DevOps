package javafx.controller;


import javafx.fxml.FXML;
import javafx.model.UserSession;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import static javafx.utils.SceneUtil.switchScene;

public class KnightController {

    public Label userLabel;
    @FXML
    private Button adminShadow;
    @FXML
    private Button normalShadow;
    @FXML
    private Button guestShadow;

    private UserSession userSession;


    public void initialize() {
        this.userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();

        if (!shadow.equals("Empty")) {
            adminShadow.setVisible(true);
            normalShadow.setVisible(true);
            guestShadow.setVisible(true);
        } else {
            adminShadow.setVisible(false);
            normalShadow.setVisible(false);
            guestShadow.setVisible(false);
        }
        userLabel.setText("Normal User " + "#" + userSession.getUserId() + " " + userSession.getUsername());
    }

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

    public void handleScrollManagement() {
        switchScene("ScrollManagement.fxml");
    }

    public void handleProfile() {
        switchScene("updateProfile.fxml");
    }

    public void handleScrollFinder() {
        switchScene("ScrollFinder.fxml");
    }


    public void handleLogout() {
        // End session then logout
        UserSession.endSession();
        switchScene("login.fxml");
    }


    public void handleAdminShadow() {
        UserSession userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();
        if (shadow.equals("Normal")) {
            userSession.setShadow("Admin");
            switchScene("AdminHomePage.fxml");
        } else if (shadow.equals("Guest")) {
            userSession.setShadow("Admin");
            switchScene("AdminHomePage.fxml");
        } else {
            // error message saying already in admin type
        }
    }

    public void handleNormalShadow() {
        UserSession userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();
        if (shadow.equals("Admin")) {
            userSession.setShadow("Normal");
            switchScene("UserHomePage.fxml");
        } else if (shadow.equals("Guest")) {
            userSession.setShadow("Normal");
            switchScene("UserHomePage.fxml");
        } else {
            // error message saying already in normal type
        }

    }

    public void handleGuestShadow() {
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
