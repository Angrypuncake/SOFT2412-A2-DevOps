package javafx.controller;


import javafx.fxml.FXML;
import javafx.model.UserSession;
import javafx.scene.control.Alert;
import javafx.scene.control.*;

import static javafx.utils.SceneUtil.switchScene;

public class AdminController {

    public Label userLabel;
    @FXML private Button shadowAdmin;
    @FXML private Button shadowNormal;
    @FXML private Button shadowGuest;
    @FXML private CheckBox shadowCheckbox;

    private UserSession userSession;


    public void initialize() {
        this.userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();
        if (!shadow.equals("Empty")) {
            shadowAdmin.setVisible(true);
            shadowNormal.setVisible(true);
            shadowGuest.setVisible(true);
        }
        shadowCheckbox.setSelected(!shadow.equals("Empty"));

        userLabel.setText("Admin User " + "#" + userSession.getUserId() + " " + userSession.getUsername());
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

    public void handleShadowMode(){
        //UserSession userSession = UserSession.getInstance();
        //shadowAdmin.setVisible(true);
        //shadowNormal.setVisible(true);
        //shadowGuest.setVisible(true);

        if (shadowCheckbox.isSelected()) {
            shadowAdmin.setVisible(true);
            shadowNormal.setVisible(true);
            shadowGuest.setVisible(true);
            userSession.setShadow("Admin");  // Or other role depending on your logic
            System.out.println("Shadow mode enabled");
        } else {
            shadowAdmin.setVisible(false);
            shadowNormal.setVisible(false);
            shadowGuest.setVisible(false);
            userSession.setShadow("Empty");
            System.out.println("Shadow mode disabled");
        }
    }

    public void handleAdminChange(){
        // error message
    }

    public void handleNormalChange(){
        UserSession userSession = UserSession.getInstance();
        userSession.setShadow("Normal");
        if(userSession.isAdmin()){
            switchScene("UserHomePage.fxml");
        }

    }

    public void handleGuestChange(){
        UserSession userSession = UserSession.getInstance();
        userSession.setShadow("Guest");
        if(userSession.isAdmin()){
            switchScene("GuestHomePage.fxml");
        }
    }

}
