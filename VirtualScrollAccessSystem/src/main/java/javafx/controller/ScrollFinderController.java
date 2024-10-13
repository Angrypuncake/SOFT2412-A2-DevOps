package javafx.controller;

import database.Database;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.model.Scroll;
import javafx.model.UserSession;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;

import static javafx.utils.SceneUtil.switchScene;

public class ScrollFinderController {

    @FXML
    private TableView<Scroll> scrollTable;

    @FXML
    private TableColumn<Scroll, String> idColumn;

    @FXML
    private TableColumn<Scroll, String> nameColumn;

    @FXML
    private TableColumn<Scroll, String> uploaderColumn;

    @FXML
    public TableColumn<Scroll, String> uploadDate;

    @FXML
    private TextArea filePreview;  // TextArea for file preview

    // Initialize method to set up the table columns
    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        uploaderColumn.setCellValueFactory(new PropertyValueFactory<>("uploaderUsername"));
        uploadDate.setCellValueFactory(new PropertyValueFactory<>("uploadDate"));

        // Add listener for row selection in the TableView
        scrollTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println("Selected Scroll: " + newValue.getId());
                loadFilePreview(newValue);  // Load file preview when a new scroll is selected
            }
        });

        // Load all scrolls upon initialization
        loadAllScrolls();
    }

    // Method to load all known scrolls from the database
    public void loadAllScrolls() {
        try {
            List<Scroll> scrolls = Database.getAllScrolls();  // Fetch all scrolls from the database
            scrollTable.getItems().setAll(scrolls);  // Display all scrolls in the TableView
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading all scrolls: " + e.getMessage());
        }
    }

    // Utility method to show an alert dialog
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Handle the BackHome button click
    @FXML
    private void BackHome() throws IOException {
        String currentRole = UserSession.getInstance().getRole();

        if (currentRole.equals("Admin")) {
            switchScene("AdminHomePage.fxml");
        }
        else if (currentRole.equals("Normal")) {
            switchScene("UserHomePage.fxml");
        }
        else{
            switchScene("GuestHomePage.fxml");
        }
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

    @FXML
    public void handleDownload() {
        Scroll selectedScroll = scrollTable.getSelectionModel().getSelectedItem();
        if (selectedScroll == null) {
            showAlert(Alert.AlertType.ERROR, "No Selection", "Please select a scroll to download.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Scroll");
        fileChooser.setInitialFileName(selectedScroll.getName());  // Set default file name

        // Set default save location or let the user choose
        File fileToSave = fileChooser.showSaveDialog(scrollTable.getScene().getWindow());

        if (fileToSave != null) {
            try {
                // Copy the file from the system's stored location to the user's chosen location
                File sourceFile = new File(selectedScroll.getBinaryFile().getAbsolutePath());  // Path of the scroll in the system
                Files.copy(sourceFile.toPath(), fileToSave.toPath(), StandardCopyOption.REPLACE_EXISTING);

                showAlert(Alert.AlertType.INFORMATION, "Download Successful", "Scroll downloaded successfully.");
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Download Error", "Failed to download the scroll: " + e.getMessage());
            }
        }
    }

    // Load the file preview (assuming it's a text file)
    private void loadFilePreview(Scroll scroll) {
        File file = scroll.getBinaryFile();  // Get the file associated with the scroll

        if (file != null && file.exists() && file.isFile()) {
            StringBuilder fileContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.append(line).append("\n");
                }
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "File Error", "Error reading file content: " + e.getMessage());
                return;
            }

            // Check if the file is empty and display a message if so
            if (fileContent.length() == 0) {
                filePreview.setText("No content available.");
            } else {
                // Limit the content to 500 characters
                String content = fileContent.toString();
                if (content.length() > 500) {
                    content = content.substring(0, 500) + "...";  // Truncate and append ellipsis
                }

                // Display the file content in the TextArea
                filePreview.setText(content);
            }
        } else {
            filePreview.setText("Unable to load the file preview.");
        }
    }

}
