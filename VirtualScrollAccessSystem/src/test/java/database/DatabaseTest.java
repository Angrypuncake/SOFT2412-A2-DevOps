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

}
