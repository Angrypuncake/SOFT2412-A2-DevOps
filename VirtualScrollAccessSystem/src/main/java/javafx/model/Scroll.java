package javafx.model;

import java.io.File;
import java.time.LocalDateTime;

public class Scroll {

    private String id;
    private String name;
    private String uploaderId;        // Uploader ID for foreign key relations
    private String uploaderUsername;  // Uploader username for display
    private LocalDateTime uploadDate;
    private long fileSize;
    private File binaryFile;

    // Constructor
    public Scroll(String id, String name, String uploaderId, String uploaderUsername, LocalDateTime uploadDate, long fileSize, File binaryFile) {
        this.id = id;
        this.name = name;
        this.uploaderId = uploaderId;               // Store uploader ID
        this.uploaderUsername = uploaderUsername;   // Store uploader username
        this.uploadDate = uploadDate;
        this.fileSize = fileSize;
        this.binaryFile = binaryFile;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getUploaderUsername() {
        return uploaderUsername;
    }

    public void setUploaderUsername(String uploaderUsername) {
        this.uploaderUsername = uploaderUsername;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public File getBinaryFile() {
        return binaryFile;
    }

    public void setBinaryFile(File binaryFile) {
        this.binaryFile = binaryFile;
    }
}
