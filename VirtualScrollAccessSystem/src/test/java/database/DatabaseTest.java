package database;

import org.junit.jupiter.api.*;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {

    @BeforeAll
    static void setupDatabase() {
        try {
            Database.setup();
        } catch (SQLException e) {
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @AfterAll
    static void cleanup() {
        Database.closeConnection();
    }

    @Test
    void testConnection() {
        try (Connection connection = Database.getConnection()) {
            assertNotNull(connection, "Connection should be established.");
        } catch (SQLException e) {
            fail("Failed to establish connection: " + e.getMessage());
        }
    }

    @Test
    void testAdminUserExists() {
        // Check if the admin user was created
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            statement.setString(1, "admin");
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next(), "Admin user should exist in the database.");
            }
        } catch (SQLException e) {
            fail("Failed to query admin user: " + e.getMessage());
        }
    }

    @Test
    void testAdminUserInsertion() {
        // Simulate removal of the admin user and re-creation
        try (Connection connection = Database.getConnection();
             PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM users WHERE username = ?")) {
            deleteStatement.setString(1, "admin");
            deleteStatement.executeUpdate();

            // Re-run the ensureAdminUserExists method to insert admin again
            Database.ensureAdminUserExists();

            // Verify the admin user is created again
            try (PreparedStatement checkStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?")) {
                checkStatement.setString(1, "admin");
                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    assertTrue(resultSet.next(), "Admin user should be re-created after deletion.");
                }
            }
        } catch (SQLException e) {
            fail("Error during admin user insertion test: " + e.getMessage());
        }
    }

}
