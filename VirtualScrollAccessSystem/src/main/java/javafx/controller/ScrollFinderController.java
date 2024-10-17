package javafx.controller;

import database.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.model.Scroll;
import javafx.model.UserSession;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static javafx.utils.SceneUtil.switchScene;

public class ScrollFinderController {

    public TextField nameSearchField;
    public TextField uploaderSearchField;
    public DatePicker fromDatePicker;
    public DatePicker toDatePicker;
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

    @FXML private Button adminShadow;
    @FXML private Button normalShadow;
    @FXML private Button guestShadow;

    private ObservableList<Scroll> allScrolls = FXCollections.observableArrayList();  // List to hold all scrolls


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

        nameSearchField.setOnKeyReleased(this::filterScrolls);
        uploaderSearchField.setOnKeyReleased(this::filterScrolls);

        UserSession userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();
        if(!shadow.equals("Empty")) {
            System.out.println(shadow);
            adminShadow.setVisible(true);
            normalShadow.setVisible(true);
            guestShadow.setVisible(true);
        } else {
            normalShadow.setVisible(false);
            guestShadow.setVisible(false);
            adminShadow.setVisible(false);
        }

    }

    private void filterScrolls(KeyEvent keyEvent) {
        String nameSearch = nameSearchField.getText().toLowerCase();
        String uploaderSearch = uploaderSearchField.getText().toLowerCase();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        ObservableList<Scroll> filteredScrolls = FXCollections.observableArrayList();

        for (Scroll scroll : allScrolls) {
            boolean matchesName = scroll.getName().toLowerCase().startsWith(nameSearch);
            boolean matchesUploader = scroll.getUploaderUsername().toLowerCase().contains(uploaderSearch);
            boolean matchesDate = (fromDate == null || !scroll.getUploadDate().isBefore(fromDate.atStartOfDay())) &&
                    (toDate == null || !scroll.getUploadDate().isAfter(toDate.atTime(23, 59, 59)));

            if (matchesName && matchesUploader && matchesDate) {
                filteredScrolls.add(scroll);
            }
        }

        scrollTable.setItems(filteredScrolls);
    }

    // Method to load all known scrolls from the database
    public void loadAllScrolls() {
        try {
            List<Scroll> scrolls = Database.getAllScrolls();  // Fetch all scrolls from the database
            allScrolls.setAll(scrolls);  // Store all scrolls in the list
            scrollTable.setItems(allScrolls);  // Display all scrolls in the TableView
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
        UserSession userSession = UserSession.getInstance();
        String currentRole = userSession.getRole();
        String shadow = userSession.getShadow();

        if (currentRole.equals("Admin")) {
            if (shadow.equals("Empty") || shadow.equals("Admin")) { // default
                switchScene("AdminHomePage.fxml");
            }
            else if (shadow.equals("Normal")) {
                switchScene("UserHomePage.fxml");
            }
            else if (shadow.equals("Normal")) {
                switchScene("GuestHomePage.fxml");
            }
        }
        else if (currentRole.equals("Normal") ) {
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
            switchScene("ScrollManagement.fxml");
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
                // Limit the content to 2000 characters
                String content = fileContent.toString();
                if (content.length() > 2000) {
                    content = content.substring(0, 2000) + "...";  // Truncate and append ellipsis
                }

                // Display the file content in the TextArea
                filePreview.setText(content);
            }
        } else {
            filePreview.setText("Unable to load the file preview.");
        }
    }
    // These set of methods will return them to the home page of the given account type
    // they select.

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


    // These methods will change the type of user but not return them home
    /*
    public void handleAdminShadow(){
        UserSession userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();
        if (shadow.equals("Normal")) {
            userSession.setShadow("Admin");
        } else if (shadow.equals("Guest")) {
            userSession.setShadow("Admin");
        } else{
            // error message saying already in admin type
        }
    }

    public void handleNormalShadow(){
        UserSession userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();
        if (shadow.equals("Admin")) {
            userSession.setShadow("Normal");
        } else if (shadow.equals("Guest")) {
            userSession.setShadow("Normal");
        } else{
            // error message saying already in normal type
        }

    }

    public void handleGuestShadow(){
        UserSession userSession = UserSession.getInstance();
        String shadow = userSession.getShadow();
        if (shadow.equals("Admin")) {
            userSession.setShadow("Guest");
        } else if (shadow.equals("Normal")) {
            userSession.setShadow("Guest");
        } else{
            // error message saying already in guest type
        }
    }
     */


}
