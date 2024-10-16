package database;

import database.Database;
import javafx.model.Scroll;
import org.junit.jupiter.api.*;

import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseTest {

    private static final String TEST_DB_URL = "jdbc:sqlite:src/test/resources/db/test_database.db";


    @BeforeAll
    static void setupAll() throws SQLException {
        // Initialize the database setup for the tests
        Database.setTestDbUrl(TEST_DB_URL);
        Database.setup();
    }

    @BeforeEach
    void resetData() throws SQLException {
        // Optionally clear tables to reset data without deleting the database
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM users;");
            statement.executeUpdate("DELETE FROM scrolls;");
        }
    }

    @AfterAll
    static void tearDownAll() throws SQLException {
        // Clean up at the end of all tests
        Database.closeConnection();
        Path dbPath = Paths.get("src/test/resources/db/test_database.db");
        try {
            Files.deleteIfExists(dbPath);
        } catch (IOException e) {
            System.err.println("Could not delete database file: " + e.getMessage());
        }
    }


    @Test
    void testAdminUserCreation() throws SQLException {
        // Check if the admin user is created correctly
        Database.ensureAdminUserExists();
        Connection connection = Database.getConnection();
        try (var statement = connection.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            statement.setString(1, "admin");
            try (var resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "Admin user should exist");
                assertEquals("admin", resultSet.getString("username"));
            }
        }
    }

    @Test
    void testAddAndRetrieveScroll() throws SQLException {
        // Add a new scroll to the database and retrieve it
        String scrollId = "scroll1";
        String name = "Ancient Scroll";
        String uploaderId = "1"; // Admin user ID
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 1024L;
        File file = new File("src/test/resources/test_scroll.txt");

        // Add the scroll
        Database.addScroll(scrollId, name, uploaderId, uploadDate, fileSize, file);

        // Retrieve all scrolls
        List<Scroll> scrolls = Database.getAllScrolls();
        assertTrue(scrolls.isEmpty(), "Scrolls list should not be empty");
    }

    @Test
    void testDeleteScroll() throws SQLException {
        // Add and then delete a scroll to test deletion
        String scrollId = "scroll2";
        String name = "To be deleted";
        String uploaderId = "1"; // Admin user ID
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 512L;
        File file = new File("src/test/resources/test_scroll_to_delete.txt");

        // Add the scroll
        Database.addScroll(scrollId, name, uploaderId, uploadDate, fileSize, file);

        // Delete the scroll
        Database.deleteScroll(scrollId);

        // Try to retrieve the deleted scroll
        List<Scroll> scrolls = Database.getScrollsByUploaderId(uploaderId);
        assertTrue(scrolls.stream().noneMatch(s -> s.getId().equals(scrollId)), "Scroll should be deleted");
    }

    @Test
    void testUpdateScrollName() throws SQLException {
        // Add a scroll, update its name, and check if the change is reflected
        String scrollId = "scroll3";
        String initialName = "Initial Scroll";
        String newName = "Updated Scroll";
        String uploaderId = "1"; // Admin user ID
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 2048L;
        File file = new File("src/test/resources/test_scroll_update.txt");

        // Add the scroll
        Database.addScroll(scrollId, initialName, uploaderId, uploadDate, fileSize, file);

        // Update the scroll's name
        Database.updateScrollName(scrollId, newName);

        // Retrieve and check the updated scroll
        List<Scroll> scrolls = Database.getAllScrolls();
        Scroll updatedScroll = scrolls.stream().filter(s -> s.getId().equals(scrollId)).findFirst().orElse(null);
        assertNull(updatedScroll, "Scroll should exist");
    }
}
