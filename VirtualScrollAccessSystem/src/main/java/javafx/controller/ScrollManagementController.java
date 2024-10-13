package javafx.controller;

import database.Database;
import javafx.MainApp;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.model.Scroll;
import javafx.model.UserSession;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static javafx.utils.SceneUtil.switchScene;


public class ScrollManagementController {


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
    private TextField scrollNameField;  // TextField for editing scroll name

    private ObservableList<Scroll> allScrolls = FXCollections.observableArrayList();  // List to hold all scrolls

    @FXML
    private TextField searchField;  // Search bar for filtering scroll names



    private File selectedFile;

    // Method to open file chooser and let user select a file, followed by automatic upload
    public void chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Scroll File to Upload");

        // Add filters to limit file selection
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = MainApp.getPrimaryStage();
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            System.out.println("File selected: " + selectedFile.getName());
            try {
                uploadScroll(); // Automatically upload the file after selection
            } catch (SQLException | IOException e) {
                showAlert(Alert.AlertType.ERROR, "Upload Error", "Error uploading file: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "File Selection Error", "No file selected.");
        }
    }

    // Method to upload scroll and save metadata to the database
    public void uploadScroll() throws SQLException, IOException {
        String id = UUID.randomUUID().toString();  // Automatically generate a unique scroll ID
        String name = selectedFile.getName();  // Use the file name as the scroll name

        UserSession userSession = UserSession.getInstance();
        String uploaderId = userSession.getUserId();  // Retrieve uploader ID from session

        // Generate upload metadata
        LocalDateTime uploadDate = LocalDateTime.now();  // Current date and time
        long fileSize = selectedFile.length();  // File size in bytes

        // Define where to save the file
        File saveDirectory = new File("scrolls/");
        if (!saveDirectory.exists()) {
            saveDirectory.mkdir();  // Create directory if it doesn't exist
        }

        // Save file to the "scrolls" directory
        File savedFile = new File(saveDirectory, selectedFile.getName());
        Files.copy(selectedFile.toPath(), savedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Add scroll metadata to the database
        Database.addScroll(id, name, uploaderId, uploadDate, fileSize, savedFile);

        // Refresh the scroll list after upload
        loadScrollsForCurrentUser();

        selectedFile = null;  // Clear the selected file
        showAlert(Alert.AlertType.INFORMATION, "Success", "Scroll uploaded successfully.");
    }

    // Method to display only the scrolls uploaded by the current user
    @FXML
    public void loadScrollsForCurrentUser() {
        try {
            UserSession userSession = UserSession.getInstance();
            String userId = userSession.getUserId();  // Retrieve the user ID from the session

            List<Scroll> scrolls = Database.getScrollsByUploaderId(userId);  // Get scrolls for current user
            scrollTable.getItems().setAll(scrolls);  // Display the scrolls in the TableView
            allScrolls.setAll(scrolls);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error loading your scrolls: " + e.getMessage());
        }
    }

    // Initialize the scroll management table view
    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        uploaderColumn.setCellValueFactory(new PropertyValueFactory<>("uploaderUsername"));
        uploadDate.setCellValueFactory(new PropertyValueFactory<>("uploadDate"));

        // Populate the form when a scroll is selected
        scrollTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                scrollNameField.setText(newValue.getName());
            }
        });

        // Load only the current user's scrolls
        loadScrollsForCurrentUser();

        // Add a listener to filter scrolls as the user types in the search field
        searchField.setOnKeyReleased(this::filterScrolls);
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
        // Go to the respective homepage based on usersession
        Stage stage = MainApp.getPrimaryStage();

        String currentRole = UserSession.getInstance().getRole();


        if (currentRole.equals("Admin")) {
            switchScene("AdminHomePage.fxml");
        }
        else if (currentRole.equals("Normal")) {
            switchScene("UserHomePage.fxml");
        }
    }

    @FXML
    public void handleDeleteScroll() {
        Scroll selectedScroll = scrollTable.getSelectionModel().getSelectedItem();
        if (selectedScroll == null) {
            showAlert(Alert.AlertType.ERROR, "No Selection", "Please select a scroll to delete.");
            return;
        }

        // Confirm deletion
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText("Are you sure you want to delete this scroll?");
        confirmationAlert.setContentText("This action cannot be undone.");

        if (confirmationAlert.showAndWait().get() == ButtonType.OK) {
            try {
                Database.deleteScroll(selectedScroll.getId());  // Delete the scroll from the database
                loadScrollsForCurrentUser();  // Refresh the TableView after deletion
                showAlert(Alert.AlertType.INFORMATION, "Success", "Scroll deleted successfully.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error deleting scroll: " + e.getMessage());
            }
        }
    }


    @FXML
    public void handleUpdateScroll() {
        Scroll selectedScroll = scrollTable.getSelectionModel().getSelectedItem();
        if (selectedScroll == null) {
            showAlert(Alert.AlertType.ERROR, "No Selection", "Please select a scroll to update.");
            return;
        }

        // Get the updated name from the TextField
        String updatedName = scrollNameField.getText();
        if (updatedName == null || updatedName.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "Please enter a valid scroll name.");
            return;
        }

        // Confirm update
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Update");
        confirmationAlert.setHeaderText("Are you sure you want to update this scroll?");
        confirmationAlert.setContentText("The name will be updated to: " + updatedName);

        if (confirmationAlert.showAndWait().get() == ButtonType.OK) {
            try {
                // Update the scroll name in the database
                Database.updateScrollName(selectedScroll.getId(), updatedName);
                loadScrollsForCurrentUser();  // Refresh the TableView after the update
                showAlert(Alert.AlertType.INFORMATION, "Success", "Scroll updated successfully.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating scroll: " + e.getMessage());
            }
        }
    }

    // Method to filter scrolls by name based on user input
    private void filterScrolls(KeyEvent event) {
        String searchTerm = searchField.getText().toLowerCase();  // Convert search term to lowercase
        // If the search term is empty, display all scrolls
        if (searchTerm.isEmpty()) {
            scrollTable.setItems(allScrolls);  // Display all scrolls
            return;
        }

        // Filter scrolls whose names start with the search term (case-insensitive)
        ObservableList<Scroll> filteredScrolls = FXCollections.observableArrayList();
        for (Scroll scroll : allScrolls) {
            if (scroll.getName().toLowerCase().startsWith(searchTerm)) {
                filteredScrolls.add(scroll);
            }
        }
        // Update the TableView with the filtered results
        scrollTable.setItems(filteredScrolls);
    }




}
