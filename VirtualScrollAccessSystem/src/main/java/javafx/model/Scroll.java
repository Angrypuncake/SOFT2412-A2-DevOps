package javafx.model;

import java.time.LocalDate;

public class Scroll {
    private String id;
    private String name;
    private String uploaderId;
    private LocalDate uploadDate;
    private String binaryDataPath; // Path to the binary file

    public Scroll(String id, String name, String uploaderId, LocalDate uploadDate, String binaryDataPath) {
        this.id = id;
        this.name = name;
        this.uploaderId = uploaderId;
        this.uploadDate = uploadDate;
        this.binaryDataPath = binaryDataPath;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public LocalDate getUploadDate() { return uploadDate; }
}
