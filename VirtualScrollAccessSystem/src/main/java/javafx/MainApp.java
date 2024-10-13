package javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import database.Database;

import static javafx.utils.SceneUtil.DEFAULT_WINDOW_HEIGHT;
import static javafx.utils.SceneUtil.DEFAULT_WINDOW_WIDTH;

public class MainApp extends Application {

    private static Stage primaryStage;  // Static reference to the stage

    @Override
    public void start(Stage stage) throws Exception {
        Database.getConnection();
        Database.setup();

        // Store the stage reference
        primaryStage = stage;

        // Load the login screen
        Parent root = FXMLLoader.load(getClass().getResource("/javafx/login.fxml"));
        primaryStage.setTitle("Login Screen");
        primaryStage.setScene(new Scene(root, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT));
        primaryStage.show();
    }

    // Static method to get the stage from anywhere
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
