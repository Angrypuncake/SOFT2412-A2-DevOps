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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static javafx.utils.HashUtils.hashScroll;
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

    @FXML private Button adminShadow;
    @FXML private Button normalShadow;
    @FXML private Button guestShadow;


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
        String name = selectedFile.getName();  // Use the file name as the scroll name

        UserSession userSession = UserSession.getInstance();
        String uploaderId = userSession.getUserId();  // Retrieve uploader ID from session
        System.out.println("Current user id is: " + userSession.getUserId());

        // Check if a scroll with the same name already exists in the system
        if (Database.CheckScrollExistsByName(name)) {
            // Retrieve the scroll's uploader ID from the database
            String existingUploaderId = Database.getUploaderIdByScrollName(name);
            System.out.println("Uploader id is: " + existingUploaderId);

            // Check if the current uploader is the same as the existing uploader
            if (existingUploaderId.equals(uploaderId)) {
                // Prompt the user: Overwrite or Rename
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Scroll Exists");
                alert.setHeaderText("You already uploaded a scroll with this name.");
                alert.setContentText("Would you like to overwrite the existing scroll or rename this new scroll?");

                ButtonType overwriteButton = new ButtonType("Overwrite");
                ButtonType renameButton = new ButtonType("Rename");
                ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(overwriteButton, renameButton, cancelButton);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == overwriteButton) {
                    // Overwrite the scroll - proceed with the upload
                    String id = Database.getScrollIdByName(name);  // Retrieve the scroll ID for the existing scroll
                    processScrollUpload(id, name, uploaderId);  // Update the existing scroll

                } else if (result.isPresent() && result.get() == renameButton) {
                    // Rename the scroll and proceed with the upload
                    String newName = promptForNewScrollName();  // Ask the user to input a new scroll name
                    String newId = hashScroll(uploaderId, newName);
                    processScrollUpload(newId, newName, uploaderId);  // Insert the new scroll
                } else {
                    // User cancelled the upload
                    showAlert(Alert.AlertType.INFORMATION, "Cancelled", "Scroll upload cancelled.");
                    return;
                }
            } else {
                // Different uploader, enforce renaming
                showAlert(Alert.AlertType.WARNING, "Scroll Exists", "A scroll with this name already exists. You must rename it.");
                String newName = promptForNewScrollName();  // Ask the user to input a new scroll name
                String newId = hashScroll(uploaderId, newName);
                processScrollUpload(newId, newName, uploaderId);  // Insert the new scroll
            }
        } else {
            // Scroll with this name doesn't exist, proceed with the upload
            String id = hashScroll(uploaderId, name);
            processScrollUpload(id, name, uploaderId);  // Insert the new scroll
        }

        // Refresh the scroll list after upload
        loadScrollsForCurrentUser();

        selectedFile = null;  // Clear the selected file
        showAlert(Alert.AlertType.INFORMATION, "Success", "Scroll uploaded successfully.");
    }

    // Helper method to handle the actual scroll upload
    private void processScrollUpload(String id, String name, String uploaderId) throws SQLException, IOException {
        LocalDateTime uploadDate = LocalDateTime.now();  // Current date and time
        long fileSize = selectedFile.length();  // File size in bytes

        // Define where to save the file - using a relative path
        Path saveDirectory = Paths.get("src", "main", "resources", "scrolls");
        Files.createDirectories(saveDirectory);  // Ensure directory exists

        // Save file to the "scrolls" directory with a relative path
        Path savedFilePath = saveDirectory.resolve(selectedFile.getName());
        Files.copy(selectedFile.toPath(), savedFilePath, StandardCopyOption.REPLACE_EXISTING);

        // Store the relative path as a string
        String relativePath = "src/main/resources/scrolls/" + selectedFile.getName();
        System.out.println("File saved at: " + savedFilePath.toAbsolutePath().toString());

        // Add or update scroll metadata to the database
        Database.addScroll(id, name, uploaderId, uploadDate, fileSize, relativePath);
    }

    // Helper method to prompt user for a new scroll name
    private String promptForNewScrollName() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Rename Scroll");
        dialog.setHeaderText("Enter a new name for your scroll:");
        dialog.setContentText("Scroll Name:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse("Unnamed Scroll");
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

        UserSession userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();
        if(!shadow.equals("Empty")) {
            adminShadow.setVisible(true);
            normalShadow.setVisible(true);
            guestShadow.setVisible(true);
        } else {
            normalShadow.setVisible(false);
            guestShadow.setVisible(false);
            adminShadow.setVisible(false);
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
        // Go to the respective homepage based on usersession
        Stage stage = MainApp.getPrimaryStage();
        UserSession userSession = UserSession.getInstance();
        String currentRole = userSession.getRole();
        String shadow = userSession.getShadow();


        if (currentRole.equals("Admin")) {
            if (shadow.equals("Empty") || shadow.equals("Admin")) {
                switchScene("AdminHomePage.fxml");
            } else if (shadow.equals("Normal")) {
                switchScene("UserHomePage.fxml");
            }
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
            } catch (IOException e) {
                throw new RuntimeException(e);
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

        try {
            // Check if the new name already exists under any uploader
            if (Database.CheckScrollExistsByName(updatedName)) {
                showAlert(Alert.AlertType.ERROR, "Name Conflict", "A scroll with this name already exists. Please choose a different name.");
                return;
            }

            // Confirm update
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Update");
            confirmationAlert.setHeaderText("Are you sure you want to update this scroll?");
            confirmationAlert.setContentText("The name will be updated to: " + updatedName);

            if (confirmationAlert.showAndWait().get() == ButtonType.OK) {
                // Update the scroll name in the database
                Database.updateScrollName(selectedScroll.getId(), updatedName);
                loadScrollsForCurrentUser();  // Refresh the TableView after the update
                showAlert(Alert.AlertType.INFORMATION, "Success", "Scroll updated successfully.");
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating scroll: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
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
        } else{
            // error message saying already in guest type
        }
    }





}
