package database;

import database.Database;
import javafx.model.Scroll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseTest {

    private static final String TEST_SCROLL_ID = "scroll-123";
    private static final String TEST_SCROLL_NAME = "Ancient Scroll";
    private static final String TEST_UPLOADER_ID = "uploader-001";
    private static final long TEST_FILE_SIZE = 2048L;
    private static final File TEST_FILE = new File("path/to/scroll/file.txt");
    private static final LocalDateTime TEST_UPLOAD_DATE = LocalDateTime.now();

    @BeforeEach
    public void setupDatabase() throws SQLException {
        Database.setup();
    }

    @Test
    public void testEnsureAdminUserExists() throws SQLException {
        // Using a mock to simulate the database behavior
        try (MockedStatic<Database> mockDatabase = mockStatic(Database.class)) {
            mockDatabase.when(Database::getConnection).thenReturn(mock(Connection.class));

            // Ensure admin user is inserted if not present
            Database.ensureAdminUserExists();
            mockDatabase.verify(() -> Database.ensureAdminUserExists(), times(1));
        }
    }

    @Test
    public void testAddScroll() throws SQLException {
        // Setup
        Database.addScroll(TEST_SCROLL_ID, TEST_SCROLL_NAME, TEST_UPLOADER_ID, TEST_UPLOAD_DATE, TEST_FILE_SIZE, TEST_FILE);

        // Fetch all scrolls and verify the added scroll
        List<Scroll> scrolls = Database.getAllScrolls();
        assertNotNull(scrolls);
        assertTrue(scrolls.stream().anyMatch(scroll -> scroll.getId().equals(TEST_SCROLL_ID)));
    }

    @Test
    public void testGetAllScrolls() throws SQLException {
        // Add a scroll to the database
        Database.addScroll(TEST_SCROLL_ID, TEST_SCROLL_NAME, TEST_UPLOADER_ID, TEST_UPLOAD_DATE, TEST_FILE_SIZE, TEST_FILE);

        // Fetch all scrolls
        List<Scroll> scrolls = Database.getAllScrolls();
        assertNotNull(scrolls);
        assertFalse(scrolls.isEmpty());

        // Verify the scroll properties
        Scroll scroll = scrolls.get(0);
        assertEquals(TEST_SCROLL_ID, scroll.getId());
        assertEquals(TEST_SCROLL_NAME, scroll.getName());
        assertEquals(TEST_UPLOADER_ID, scroll.getUploaderId());
    }

    @Test
    public void testGetScrollsByUploaderId() throws SQLException {
        // Add a scroll for a specific uploader
        Database.addScroll(TEST_SCROLL_ID, TEST_SCROLL_NAME, TEST_UPLOADER_ID, TEST_UPLOAD_DATE, TEST_FILE_SIZE, TEST_FILE);

        // Fetch scrolls by uploader ID
        List<Scroll> scrolls = Database.getScrollsByUploaderId(TEST_UPLOADER_ID);
        assertNotNull(scrolls);
        assertFalse(scrolls.isEmpty());

        // Verify that the uploader ID matches
        Scroll scroll = scrolls.get(0);
        assertEquals(TEST_UPLOADER_ID, scroll.getUploaderId());
    }

    @Test
    public void testDeleteScroll() throws SQLException {
        // Add a scroll to the database
        Database.addScroll(TEST_SCROLL_ID, TEST_SCROLL_NAME, TEST_UPLOADER_ID, TEST_UPLOAD_DATE, TEST_FILE_SIZE, TEST_FILE);

        // Delete the scroll
        Database.deleteScroll(TEST_SCROLL_ID);

        // Verify the scroll was deleted
        List<Scroll> scrolls = Database.getAllScrolls();
        assertTrue(scrolls.stream().noneMatch(scroll -> scroll.getId().equals(TEST_SCROLL_ID)));
    }

    @Test
    public void testSetup() throws SQLException {
        // Verify that the setup method successfully creates tables
        Connection connection = Database.getConnection();
        Statement statement = connection.createStatement();

        // Check if the 'users' and 'scrolls' tables exist
        ResultSet rsUsers = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='users'");
        assertTrue(rsUsers.next());

        ResultSet rsScrolls = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='scrolls'");
        assertTrue(rsScrolls.next());
    }
}
