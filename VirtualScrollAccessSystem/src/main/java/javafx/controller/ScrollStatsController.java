package javafx.controller;

import database.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.model.ScrollStats;
import javafx.scene.control.*;

import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static javafx.utils.SceneUtil.switchScene;

public class ScrollStatsController {
    public ObservableList<ScrollStats> scrollStats;
    private FilteredList<ScrollStats> filteredScrollStats; // Filtered list based on toggle
    public Button deleteButton;
    public Button updateButton;
    @FXML
    private TableView<ScrollStats> scrollTableView;

    @FXML
    private TableColumn<ScrollStats, String> nameColumn;


    @FXML
    private TableColumn<ScrollStats, Integer> uploadCountColumn;

    @FXML
    private TableColumn<ScrollStats, Integer> downloadCountColumn;

    @FXML
    private ToggleButton toggleInactiveButton;

    @FXML
    private Label uploaderLabel;

    @FXML
    private Label orphanedLabel;

    @FXML
    public void initialize() throws SQLException {
        // Initialize the TableView with columns
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        uploadCountColumn.setCellValueFactory(new PropertyValueFactory<>("uploadCount"));
        downloadCountColumn.setCellValueFactory(new PropertyValueFactory<>("downloadCount"));

        // Load data from database
        scrollStats = FXCollections.observableArrayList(Database.loadScrollStats());

        // Set the data in the TableView
        // Create a filtered list that starts showing only active scrolls
        filteredScrollStats = new FilteredList<>(scrollStats, scroll -> !scroll.isOrphaned());
        scrollTableView.setItems(filteredScrollStats);

        // Set event listener for row selection
        scrollTableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Update the labels with the selected scroll's details
                uploaderLabel.setText("Uploader: " + newValue.getUploaderName());
                orphanedLabel.setText("Status: " + (newValue.isOrphaned() ? "Inactive" : "Active"));
            }
        });
    }

    @FXML
    private void handleLogout() {
        switchScene("login.fxml");
    }

    @FXML
    private void handleHome() {
        switchScene("AdminHomePage.fxml");

    }

    @FXML
    public void handleUpdateScrollStats() {
        ScrollStats selectedScrollStat = scrollTableView.getSelectionModel().getSelectedItem();
        if (selectedScrollStat == null) {
            showAlert(Alert.AlertType.ERROR, "No Selection", "Please select a scroll stat to update.");
            return;
        }

        // Open a dialog to update scroll stats (for simplicity, assume it's text input fields)
        TextInputDialog uploadCountDialog = new TextInputDialog(Integer.toString(selectedScrollStat.getUploadCount()));
        uploadCountDialog.setTitle("Update Upload Count");
        uploadCountDialog.setHeaderText("Update the upload count for the selected scroll stat:");
        uploadCountDialog.setContentText("Upload Count:");
        Optional<String> uploadCountResult = uploadCountDialog.showAndWait();

        TextInputDialog downloadCountDialog = new TextInputDialog(Integer.toString(selectedScrollStat.getDownloadCount()));
        downloadCountDialog.setTitle("Update Download Count");
        downloadCountDialog.setHeaderText("Update the download count for the selected scroll stat:");
        downloadCountDialog.setContentText("Download Count:");
        Optional<String> downloadCountResult = downloadCountDialog.showAndWait();

        if (uploadCountResult.isPresent() && downloadCountResult.isPresent()) {
            try {
                // Update the scroll stats in the database
                int newUploadCount = Integer.parseInt(uploadCountResult.get());
                int newDownloadCount = Integer.parseInt(downloadCountResult.get());
                Database.updateScrollStats(selectedScrollStat.getName(), newUploadCount, newDownloadCount);

                // Refresh the TableView
                scrollStats.setAll(Database.loadScrollStats());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Scroll stats updated successfully.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error updating scroll stats: " + e.getMessage());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter valid numbers.");
            }
        }
    }
    
    // Utility method to show an alert dialog
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    public void handleDeleteScrollStats() {
        ScrollStats selectedScrollStat = scrollTableView.getSelectionModel().getSelectedItem();
        if (selectedScrollStat == null) {
            showAlert(Alert.AlertType.ERROR, "No Selection", "Please select a scroll stat to delete.");
            return;
        }

        // Confirm the deletion
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText("Are you sure you want to delete the selected scroll stat?");
        confirmationAlert.setContentText("This action cannot be undone.");

        if (confirmationAlert.showAndWait().get() == ButtonType.OK) {
            try {
                // Delete the scroll stats from the database
                System.out.println("Selected name is " + selectedScrollStat.getName());
                Database.deleteScrollStats(selectedScrollStat.getName());

                // Refresh the TableView
                scrollStats.setAll(Database.loadScrollStats());
                showAlert(Alert.AlertType.INFORMATION, "Success", "Scroll stats deleted successfully.");
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error deleting scroll stats: " + e.getMessage());
            }
        }
    }


    // Toggle button action handler to show/hide inactive scrolls
    @FXML
    public void toggleInactiveScrolls() {
        if (toggleInactiveButton.isSelected()) {
            toggleInactiveButton.setText("Hide Inactive Scrolls");
            // Show all scrolls, including inactive (orphaned) ones
            filteredScrollStats.setPredicate(scroll -> true);
        } else {
            toggleInactiveButton.setText("Show Inactive Scrolls");
            // Only show active scrolls (not orphaned)
            filteredScrollStats.setPredicate(scroll -> !scroll.isOrphaned());
        }
    }
}
