package database;

import javafx.model.Scroll;
import javafx.model.ScrollStats;
import javafx.utils.HashUtils;


import java.io.File;
import java.io.IOException;
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

    private static String URL = "jdbc:sqlite:src/main/resources/db/app_database.db";

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
            name VARCHAR(255) UNIQUE NOT NULL,
            uploader_id INT NOT NULL,
            upload_date TIMESTAMP NOT NULL,
            file_size BIGINT NOT NULL,
            file_path TEXT NOT NULL,
            FOREIGN KEY (uploader_id) REFERENCES users(id) ON DELETE CASCADE
        );
        """;

            String createScrollStats = """
            CREATE TABLE IF NOT EXISTS scrollStats (
                id TEXT PRIMARY KEY NOT NULL,  -- Foreign key from scrolls
                name VARCHAR(255) UNIQUE NOT NULL,  -- Unique name of the scroll
                uploader_id INT NOT NULL,
                upload_count INT DEFAULT 0,
                download_count INT DEFAULT 0,
                orphaned BOOLEAN DEFAULT FALSE,  -- Indicates if the associated scroll has been deleted
                FOREIGN KEY (id) REFERENCES scrolls(id) ON UPDATE CASCADE  -- Use id as the foreign key
            );
        """;


            // Step 4: Execute the SQL statements to create the tables
            statement.execute(createUserTableSQL);
            statement.execute(createScrollsTableSQL);
            statement.execute(createScrollStats);

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

    public static boolean CheckScrollExistsByName(String name) throws SQLException {
        String checkSQL = "SELECT name FROM scrolls WHERE name = ?";

        try (Connection connection = getConnection();
             PreparedStatement checkStatement = connection.prepareStatement(checkSQL)) {

            // Set the name parameter in the SQL query
            checkStatement.setString(1, name);

            // Execute the query and get the result set
            ResultSet resultSet = checkStatement.executeQuery();

            // Check if a result was returned (i.e., the scroll exists)
            return resultSet.next();
        }
    }




    public static void addScroll(String id, String name, String uploaderId, LocalDateTime uploadDate, long fileSize, String relativeFilePath) throws SQLException {
        String checkScrollSQL = "SELECT * FROM scrolls WHERE id = ?";
        String insertScrollSQL = "INSERT INTO scrolls (id, name, uploader_id, upload_date, file_size, file_path) VALUES (?, ?, ?, ?, ?, ?)";
        String updateScrollSQL = "UPDATE scrolls SET name = ?, uploader_id = ?, upload_date = ?, file_size = ?, file_path = ? WHERE id = ?";

        String checkStatsSQL = "SELECT * FROM scrollStats WHERE id = ?";
        String insertStatsSQL = "INSERT INTO scrollStats (id, name, uploader_id, upload_count, download_count, orphaned) VALUES (?, ?, ?, ?, ?, ?)";
        String updateStatsSQL = "UPDATE scrollStats SET upload_count = upload_count + 1 WHERE id = ?";
        String unmarkOrphanedSQL = "UPDATE scrollStats SET orphaned = FALSE WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement checkScrollStatement = connection.prepareStatement(checkScrollSQL);
             PreparedStatement insertScrollStatement = connection.prepareStatement(insertScrollSQL);
             PreparedStatement updateScrollStatement = connection.prepareStatement(updateScrollSQL);
             PreparedStatement checkStatsStatement = connection.prepareStatement(checkStatsSQL);
             PreparedStatement insertStatsStatement = connection.prepareStatement(insertStatsSQL);
             PreparedStatement updateStatsStatement = connection.prepareStatement(updateStatsSQL);
             PreparedStatement unmarkOrphanedStatement = connection.prepareStatement(unmarkOrphanedSQL)) {

            // Check if the scroll already exists
            checkScrollStatement.setString(1, id);
            ResultSet scrollResultSet = checkScrollStatement.executeQuery();

            if (scrollResultSet.next()) {
                // Scroll exists, update it
                updateScrollStatement.setString(1, name);
                updateScrollStatement.setString(2, uploaderId);
                updateScrollStatement.setTimestamp(3, java.sql.Timestamp.valueOf(uploadDate));
                updateScrollStatement.setLong(4, fileSize);
                updateScrollStatement.setString(5, relativeFilePath);
                updateScrollStatement.setString(6, id);
                updateScrollStatement.executeUpdate();
            } else {
                // Scroll does not exist, insert it
                insertScrollStatement.setString(1, id);
                insertScrollStatement.setString(2, name);
                insertScrollStatement.setString(3, uploaderId);
                insertScrollStatement.setTimestamp(4, java.sql.Timestamp.valueOf(uploadDate));
                insertScrollStatement.setLong(5, fileSize);
                insertScrollStatement.setString(6, relativeFilePath);
                insertScrollStatement.executeUpdate();
            }

            // Check if the scrollStats entry already exists
            checkStatsStatement.setString(1, id);
            ResultSet statsResultSet = checkStatsStatement.executeQuery();

            if (statsResultSet.next()) {
                // scrollStats entry exists, increment the upload_count and unmark orphaned if necessary
                updateStatsStatement.setString(1, id);
                updateStatsStatement.executeUpdate();

                if (statsResultSet.getBoolean("orphaned")) {
                    unmarkOrphanedStatement.setString(1, id);
                    unmarkOrphanedStatement.executeUpdate();
                }
            } else {
                // scrollStats entry does not exist, insert a new entry with upload_count set to 1 and orphaned set to false
                insertStatsStatement.setString(1, id);
                insertStatsStatement.setString(2, name);
                insertStatsStatement.setString(3, uploaderId);
                insertStatsStatement.setInt(4, 1); // Initial upload_count is 1
                insertStatsStatement.setInt(5, 0); // Initial download_count is 0
                insertStatsStatement.setBoolean(6, false); // Not orphaned
                insertStatsStatement.executeUpdate();
            }
        }
    }



    // Method to delete a scroll from the database and remove the corresponding file
    public static void deleteScroll(String scrollId) throws SQLException, IOException {
        String selectSQL = "SELECT file_path FROM scrolls WHERE id = ?";
        String deleteSQL = "DELETE FROM scrolls WHERE id = ?";
        String orphanStatsSQL = "UPDATE scrollStats SET orphaned = TRUE WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
             PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL);
             PreparedStatement orphanStatsStatement = connection.prepareStatement(orphanStatsSQL)) {

            // Retrieve the file path associated with the scroll ID
            selectStatement.setString(1, scrollId);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                String filePath = resultSet.getString("file_path");
                File file = new File(filePath);

                // Delete the scroll from the database
                deleteStatement.setString(1, scrollId);
                int affectedRows = deleteStatement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Scroll deleted successfully.");

                    // Mark scrollStats as orphaned
                    orphanStatsStatement.setString(1, scrollId);
                    orphanStatsStatement.executeUpdate();

                    // Check if the file exists before attempting to delete
                    if (file.exists()) {
                        boolean deleted = file.delete();
                        if (deleted) {
                            System.out.println("File deleted successfully: " + filePath);
                        } else {
                            System.out.println("Failed to delete the file: " + filePath);
                        }
                    } else {
                        System.out.println("File not found: " + filePath);
                    }
                } else {
                    System.out.println("No scroll found with the given ID.");
                }
            } else {
                System.out.println("No scroll found with the given ID.");
            }

        } catch (SQLException e) {
            throw new SQLException("Error while deleting scroll: " + e.getMessage(), e);
        }
    }



    public static void updateScrollName(String scrollId, String newName) throws SQLException, IOException {
        String selectSQL = "SELECT file_path FROM scrolls WHERE id = ?";
        String updateScrollSQL = "UPDATE scrolls SET name = ?, file_path = ? WHERE id = ?";
        String updateScrollStatsSQL = "UPDATE scrollStats SET name = ? WHERE id = ?";

        try (Connection connection = getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
             PreparedStatement updateScrollStatement = connection.prepareStatement(updateScrollSQL);
             PreparedStatement updateScrollStatsStatement = connection.prepareStatement(updateScrollStatsSQL)) {

            // Retrieve file path associated with scroll ID
            selectStatement.setString(1, scrollId);
            ResultSet resultSet = selectStatement.executeQuery();

            if (!resultSet.next()) {
                System.err.println("Error: No scroll found with the given ID: " + scrollId);
                return;
            }

            String filePath = resultSet.getString("file_path");
            File oldFile = new File(filePath);
            File newFile = new File(oldFile.getParent(), newName);

            // File existence, permission, and conflict check
            if (!oldFile.exists() || !oldFile.canWrite() || newFile.exists()) {
                System.err.println("Error: File issue (not found, not writable, or name conflict).");
                return;
            }

            // Rename the file
            if (oldFile.renameTo(newFile)) {
                // Update scroll name and file path in scrolls table
                updateScrollStatement.setString(1, newName);
                updateScrollStatement.setString(2, newFile.getPath());
                updateScrollStatement.setString(3, scrollId);
                updateScrollStatement.executeUpdate();

                // Update scroll name in scrollStats table
                updateScrollStatsStatement.setString(1, newName);
                updateScrollStatsStatement.setString(2, scrollId);
                updateScrollStatsStatement.executeUpdate();

                System.out.println("Scroll and file updated successfully.");
            } else {
                System.err.println("Error: Failed to rename the file.");
            }
        }
    }



    public static void setTestDbUrl(String testDbUrl) {
        URL = testDbUrl;
    }


    // Increment the download count for a specific scroll
    public static void incrementDownloadCount(String scrollId) throws SQLException {
        String updateSQL = "UPDATE scrollStats SET download_count = download_count + 1 WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
            preparedStatement.setString(1, scrollId);
            preparedStatement.executeUpdate();
        }
    }

    public static List<ScrollStats> loadScrollStats() throws SQLException {
        String query = """
        SELECT ss.id, u.username, ss.upload_count, ss.download_count, ss.name, ss.orphaned
        FROM scrollStats ss
        LEFT JOIN users u ON ss.uploader_id = u.id
        LEFT JOIN scrolls s ON ss.id = s.id
    """;

        List<ScrollStats> scrollStatsList = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            while (resultSet.next()) {
                String name = resultSet.getString("name");  // Get scroll name from scrolls table
                String uploaderName = resultSet.getString("username");  // Get uploader name from users table
                int uploadCount = resultSet.getInt("upload_count");
                int downloadCount = resultSet.getInt("download_count");
                boolean orphaned = resultSet.getBoolean("orphaned");  // Check if orphaned

                // Create a ScrollStats object and add it to the list
                ScrollStats scrollStat = new ScrollStats(name, uploaderName, uploadCount, downloadCount, orphaned);
                scrollStatsList.add(scrollStat);
            }
        } catch (SQLException e) {
            throw new SQLException("Error while retrieving scroll stats: " + e.getMessage(), e);
        }

        return scrollStatsList;  // Return the list of ScrollStats objects
    }





    public static String getScrollIdByName(String name) throws SQLException {
        String query = "SELECT id FROM scrolls WHERE name = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the name parameter in the query
            preparedStatement.setString(1, name);

            // Execute the query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If a result is found, return the scroll ID
                if (resultSet.next()) {
                    return resultSet.getString("id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving scroll ID by name", e);
        }

        // If no scroll with that name was found, return null
        return null;
    }


    public static String getUploaderIdByScrollName(String name) {
        String query = "SELECT uploader_id FROM scrolls WHERE name = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            // Set the name parameter in the query
            preparedStatement.setString(1, name);

            // Execute the query
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If a result is found, return the scroll ID
                if (resultSet.next()) {
                    return resultSet.getString("uploader_id");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving uploader_id by name", e);
        }

        // If no scroll with that name was found, return null
        return null;
    }

    public static void updateScrollStats(String name, int uploadCount, int downloadCount) throws SQLException {
        System.out.println("Updating scroll stats: " + name);
        String query = "UPDATE scrollStats SET upload_count = ?, download_count = ? WHERE name = ?";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, uploadCount);
            preparedStatement.setInt(2, downloadCount);
            preparedStatement.setString(3, name);
            preparedStatement.executeUpdate();
        }
    }


    public static void deleteScrollStats(String name) throws SQLException {
        String deleteSQL = "DELETE FROM scrollStats WHERE name = ?";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL)) {
            // Set the scroll name parameter
            preparedStatement.setString(1, name);

            // Execute the DELETE statement
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Scroll stats deleted successfully.");
            } else {
                System.out.println("No scroll stats found with the given name.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while deleting scroll stats: " + e.getMessage(), e);
        }
    }

}
