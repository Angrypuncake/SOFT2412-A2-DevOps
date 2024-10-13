package database;

import javafx.model.Scroll;
import javafx.utils.HashUtils;


import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Handles all database operations and initialisation

public class Database {
    private static Connection connection;

    // Hardcoded admin credentials
    private static final String ADMIN_ID = "1";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin";
    private static final String ADMIN_EMAIL = "admin@admin.com";
    private static final String ADMIN_ROLE = "Admin";

    private Database() {}

    private static final String URL = "jdbc:sqlite:src/main/resources/db/app_database.db";

    // Method to establish a connection to the SQLite database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void setup() throws SQLException {
        // Step 1: Establish a connection
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            // Step 2: Create the users table if it doesn't exist
            String createUserTableSQL = """
            CREATE TABLE IF NOT EXISTS users (
                id TEXT PRIMARY KEY,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                phone TEXT,
                userType TEXT NOT NULL
            );
        """;

            // Step 3: Create the scrolls table if it doesn't exist
            String createScrollsTableSQL = """
    CREATE TABLE IF NOT EXISTS scrolls (
        id TEXT PRIMARY KEY NOT NULL,
        name TEXT NOT NULL,
        uploader_id TEXT NOT NULL,
        upload_date TIMESTAMP NOT NULL,
        file_size BIGINT NOT NULL,
        file_path TEXT NOT NULL,
        FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE
    );
""";

            // Step 4: Execute the SQL statements to create the tables
            statement.execute(createUserTableSQL);
            statement.execute(createScrollsTableSQL);

            System.out.println("Database setup complete. Tables created if they didn't exist.");

            // Ensure there is at least one admin user
            Database.ensureAdminUserExists();

        } catch (SQLException e) {
            throw new SQLException("Error during database setup: " + e.getMessage(), e);
        }
    }


    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Failed to close the database connection: " + e.getMessage());
        }
    }


    // Method to check if the admin exists in the database
    public static void ensureAdminUserExists() {

        try (Connection connection = getConnection()) {
            // Check if admin user exists
            String checkQuery = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setString(1, ADMIN_USERNAME);
                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        // Admin doesn't exist, create the admin user
                        insertAdminUser(connection);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to insert the hardcoded admin user
    private static void insertAdminUser(Connection connection) throws SQLException {
        String insertQuery = "INSERT INTO users (id, username, password, email, userType) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
            insertStatement.setString(1, ADMIN_ID);
            insertStatement.setString(2, ADMIN_USERNAME);
            insertStatement.setString(3, HashUtils.hashPassword(ADMIN_PASSWORD));  // Make sure to hash the password
            insertStatement.setString(4, ADMIN_EMAIL);
            insertStatement.setString(5, ADMIN_ROLE);
            insertStatement.executeUpdate();
            System.out.println("Master admin user created.");
        }
    }


    public static List<Scroll> getAllScrolls() throws SQLException {
        String query = """
        SELECT s.id, s.name, s.uploader_id, u.username, s.upload_date, s.file_size, s.file_path 
        FROM scrolls s 
        JOIN users u ON s.uploader_id = u.id
    """;

        List<Scroll> scrolls = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String id = resultSet.getString("id");
                String name = resultSet.getString("name");
                String uploaderId = resultSet.getString("uploader_id");  // Get uploader ID
                String uploaderUsername = resultSet.getString("username");  // Get uploader username
                LocalDateTime uploadDate = resultSet.getTimestamp("upload_date").toLocalDateTime();
                long fileSize = resultSet.getLong("file_size");
                String filePath = resultSet.getString("file_path");

                // Create a Scroll object with both the uploader's ID and username
                Scroll scroll = new Scroll(id, name, uploaderId, uploaderUsername, uploadDate, fileSize, new File(filePath));
                scrolls.add(scroll);
            }
        }

        return scrolls; // Return the list of scrolls
    }



    public static List<Scroll> getScrollsByUploaderId(String userId) throws SQLException {
        String query = """
        SELECT s.id, s.name, s.uploader_id, u.username, s.upload_date, s.file_size, s.file_path 
        FROM scrolls s 
        JOIN users u ON s.uploader_id = u.id
        WHERE s.uploader_id = ?
    """;

        List<Scroll> scrolls = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the userId in the query
            preparedStatement.setString(1, userId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String id = resultSet.getString("id");
                    String name = resultSet.getString("name");
                    String uploaderId = resultSet.getString("uploader_id");
                    String uploaderUsername = resultSet.getString("username");
                    LocalDateTime uploadDate = resultSet.getTimestamp("upload_date").toLocalDateTime();
                    long fileSize = resultSet.getLong("file_size");
                    String filePath = resultSet.getString("file_path");

                    // Create a Scroll object with both the uploader's ID and username
                    Scroll scroll = new Scroll(id, name, uploaderId, uploaderUsername, uploadDate, fileSize, new File(filePath));
                    scrolls.add(scroll);
                }
            }

        } catch (SQLException e) {
            throw new SQLException("Error while retrieving scrolls: " + e.getMessage(), e);
        }

        return scrolls; // Return the list of scrolls for the given userId
    }



    public static void addScroll(String id, String name, String uploaderId, LocalDateTime uploadDate, long fileSize, File binaryFile) throws SQLException {
        String insertSQL = "INSERT INTO scrolls (id, name, uploader_id, upload_date, file_size, file_path) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {

            preparedStatement.setString(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, uploaderId);
            preparedStatement.setTimestamp(4, java.sql.Timestamp.valueOf(uploadDate)); // Convert LocalDateTime to SQL Timestamp
            preparedStatement.setLong(5, fileSize);
            preparedStatement.setString(6, binaryFile.getAbsolutePath()); // Store file path

            preparedStatement.executeUpdate();
        }
    }
    // Method to delete a scroll from the database
    public static void deleteScroll(String scrollId) throws SQLException {
        String deleteSQL = "DELETE FROM scrolls WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {

            preparedStatement.setString(1, scrollId);  // Set the scroll ID in the prepared statement

            int affectedRows = preparedStatement.executeUpdate();  // Execute the delete statement
            if (affectedRows > 0) {
                System.out.println("Scroll deleted successfully.");
            } else {
                System.out.println("No scroll found with the given ID.");
            }
        } catch (SQLException e) {
            throw new SQLException("Error while deleting scroll: " + e.getMessage(), e);
        }
    }
}
