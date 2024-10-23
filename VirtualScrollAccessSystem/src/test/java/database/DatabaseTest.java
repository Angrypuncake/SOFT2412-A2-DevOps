package database;

import database.Database;
import javafx.model.Scroll;
import javafx.model.ScrollStats;
import org.junit.jupiter.api.*;

import org.mockito.MockedStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseTest {

    private static final String TEST_DB_URL = "jdbc:sqlite:src/test/resources/db/test_database.db";
    private static final String TEST_SCROLL_DIR = "src/test/resources/scrolls/";

    @BeforeAll
    static void setupAll() throws SQLException, IOException {
        Files.createDirectories(Paths.get("src/test/resources/db"));
        Files.createDirectories(Paths.get(TEST_SCROLL_DIR));
        // Initialize the database setup for the tests
        Database.setTestDbUrl(TEST_DB_URL);
        Database.setup();
    }

    @Test
    void setupFreshDatabase() throws SQLException {
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {

            // Drop tables if they exist
            statement.executeUpdate("DROP TABLE IF EXISTS scrollStats;");
            statement.executeUpdate("DROP TABLE IF EXISTS scrolls;");
            statement.executeUpdate("DROP TABLE IF EXISTS users;");

            // Recreate the tables (reuse schema creation code)
            String createUserTableSQL = """
            CREATE TABLE IF NOT EXISTS users (
                id TEXT PRIMARY KEY,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                full_name TEXT,
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

            String createScrollStats =
                    """
            CREATE TABLE IF NOT EXISTS
                    scrollStats (
                id TEXT PRIMARY KEY NOT NULL,
                    -- Foreign key from scrolls
                name VARCHAR(255) UNIQUE NOT NULL,
                    -- Unique name of the scr
                               uploader_name
                                  uploader_id INT,
                upload_count INT DEFAULT 0,
                download_count INT DEFAULT 0,
                orphaned BOOLEAN DEFAULT FALSE,
                    -- Indicates if the associated scroll has been deleted
                FOREIGN KEY (id) REFERENCES scrolls(id) ON UPDATE
                    CASCADE  -- Use id as the foreign key
            );
        """;
            // Execute table creation statements
            statement.execute(createUserTableSQL);
            statement.execute(createScrollsTableSQL);
            statement.execute(createScrollStats);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


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

    @Nested
    public class MainTestBlock {

    @BeforeEach
    void resetDatabase() throws SQLException {
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {

            // Drop tables if they exist
            statement.executeUpdate("DROP TABLE IF EXISTS scrollStats;");
            statement.executeUpdate("DROP TABLE IF EXISTS scrolls;");
            statement.executeUpdate("DROP TABLE IF EXISTS users;");

            // Recreate the tables (reuse schema creation code)
            String createUserTableSQL = """
            CREATE TABLE IF NOT EXISTS users (
                id TEXT PRIMARY KEY,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                full_name TEXT,
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
                uploader_name TEXT,
                uploader_id INT,
                upload_count INT DEFAULT 0,
                download_count INT DEFAULT 0,
                orphaned BOOLEAN DEFAULT FALSE,  -- Indicates if the associated scroll has been deleted
                FOREIGN KEY (id) REFERENCES scrolls(id) ON UPDATE CASCADE  -- Use id as the foreign key
            );
        """;
            // Execute table creation statements
            statement.execute(createUserTableSQL);
            statement.execute(createScrollsTableSQL);
            statement.execute(createScrollStats);

            Database.insertAdminUser(connection);
        }
    }


    @AfterAll
    static void tearDownAll() throws SQLException {
        // Clean up at the end of all tests
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
    void testConnection() {
        assertDoesNotThrow(() -> {
            Connection connection = Database.getConnection();
            assertNotNull(connection);
            assertFalse(connection.isClosed());
            connection.close();
        });
    }

    @Test
    void testAddAndRetrieveScroll() throws SQLException {
        // Arrange: Create test scroll details
        String scrollId = "1234";
        String scrollName = "TestScroll";
        String uploaderId = "1";
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 1024;

        // Use File.separator for platform-independent file paths
        String filePath = TEST_SCROLL_DIR + File.separator + "TestScroll.txt";

        // Act: Add the scroll to the database
        Database.addScroll(scrollId, scrollName, uploaderId, uploadDate, fileSize, filePath);

        // Assert: Retrieve the scroll and check the details
        List<Scroll> scrolls = Database.getAllScrolls();
        assertEquals(1, scrolls.size(), "There should be one scroll in the database");

        Scroll retrievedScroll = scrolls.get(0);
        assertEquals(scrollId, retrievedScroll.getId());
        assertEquals(scrollName, retrievedScroll.getName());
        assertEquals(uploaderId, retrievedScroll.getUploaderId());
        assertEquals(fileSize, retrievedScroll.getFileSize());

        // Use retrievedScroll.getBinaryFile().getPath() and normalize for comparison
        assertEquals(new File(filePath).getPath(), retrievedScroll.getBinaryFile().getPath());
    }


    @Test
    void testDeleteScroll() throws SQLException, IOException {
        // Arrange: Add a scroll to delete
        String scrollId = "3";
        String scrollName = "ScrollToDelete";
        String uploaderId = "1";
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 4096;
        String filePath = TEST_SCROLL_DIR + "ScrollToDelete.txt";

        // Add a scroll
        Database.addScroll(scrollId, scrollName, uploaderId, uploadDate, fileSize, filePath);

        File scrollFile = new File(filePath);
        if (!scrollFile.exists()) {
            File parentDir = scrollFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs(); // Create directories if they don't exist
            }
            scrollFile.createNewFile(); // Create the scroll file
            System.out.println("Scroll file created: " + scrollFile.getAbsolutePath());
        } else {
            System.out.println("Scroll file already exists.");
        }
        // Ensure the scroll exists
        assertTrue(Database.CheckScrollExistsByName(scrollName));

        // Act: Delete the scroll
        Database.deleteScroll(scrollId);

        // Assert: Ensure the scroll was deleted
        assertFalse(Database.CheckScrollExistsByName(scrollName), "Scroll should be deleted");
        wipeDirectory(new File(TEST_SCROLL_DIR));
    }

    @Test
    void testUpdateScrollName() throws SQLException, IOException {
        // Arrange: Add a scroll to update
        String scrollId = "4";
        String oldName = "OldScrollName";
        String newName = "NewScrollName";
        String uploaderId = "1";
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 5120;
        String filePath = TEST_SCROLL_DIR + oldName + ".txt";

        // Manually create a scroll file at src/test/resources/scrolls/OldScrollName.txt
        File scrollFile = new File(filePath);
        if (!scrollFile.exists()) {
            File parentDir = scrollFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs(); // Create directories if they don't exist
            }
            scrollFile.createNewFile(); // Create the scroll file
            System.out.println("Scroll file created: " + scrollFile.getAbsolutePath());
        } else {
            System.out.println("Scroll file already exists.");
        }

        // Add the scroll
        Database.addScroll(scrollId, oldName, uploaderId, uploadDate, fileSize, filePath);
        // Act: Update the scroll name
        Database.updateScrollName(scrollId, newName);

        // Assert: Ensure the scroll name was updated
        assertNull(Database.getScrollIdByName(oldName), "Old scroll name should not exist");
        assertNotNull(Database.getScrollIdByName(newName), "New scroll name should exist");
        wipeDirectory(new File(TEST_SCROLL_DIR));
    }

    private void wipeDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) { // Check if it's not null (not a file or doesn't exist)
                for (File file : files) {
                    if (file.isDirectory()) {
                        wipeDirectory(file); // Recursively delete subdirectories
                    }
                    file.delete(); // Delete file or directory
                }
            }
        }
    }

    @Test
    void testIncrementDownloadCount() throws SQLException, IOException {
        // Arrange: Add a scroll to increment its download count
        String scrollId = "5";
        String scrollName = "DownloadableScroll";
        String uploaderId = "1";
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 1024;
        String filePath = TEST_SCROLL_DIR + "DownloadableScroll.txt";

        // Add the scroll
        Database.addScroll(scrollId, scrollName, uploaderId, uploadDate, fileSize, filePath);

        // Act: Increment the download count
        Database.incrementDownloadCount(scrollId);

        // Assert: Ensure the download count was incremented
        List<ScrollStats> scrollStats = Database.loadScrollStats();
        ScrollStats stats = scrollStats.stream().filter(s -> s.getName().equals(scrollName)).findFirst().orElse(null);
        assertNotNull(stats, "Scroll stats should exist");
        assertEquals(1, stats.getDownloadCount(), "Download count should be incremented");
    }


    @Test
    void testUpdateNonExistingScroll() throws SQLException, IOException {
        Database.updateScrollName("4", "newName");
        // Assert: Ensure the scroll name was updated
        assertNull(Database.getScrollIdByName("newName"), "Scroll should not exist");
    }

    @Test
    void testGetUploaderIdByScrollName() throws SQLException {
        // Arrange: Add a scroll to retrieve the uploader ID
        String scrollId = "12345";
        String scrollName = "UploaderScroll";
        String uploaderId = "1";
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 1024;
        String filePath = TEST_SCROLL_DIR + File.separator + scrollName + ".txt";

        // Add the scroll
        Database.addScroll(scrollId, scrollName, uploaderId, uploadDate, fileSize, filePath);

        // Act: Get the uploader ID
        String retrievedUploaderId = Database.getUploaderIdByScrollName(scrollName);

        // Assert: Ensure the correct uploader ID is retrieved
        assertEquals(uploaderId, retrievedUploaderId, "The uploader ID should match the one added");
    }

    @Test
    void testGetScrollIdByName() throws SQLException {
        // Arrange: Add a scroll to retrieve by name
        String scrollId = "123456";
        String scrollName = "ScrollToRetrieve";
        String uploaderId = "1";
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 3072;
        String filePath = TEST_SCROLL_DIR + File.separator + scrollName + ".txt";

        // Add the scroll
        Database.addScroll(scrollId, scrollName, uploaderId, uploadDate, fileSize, filePath);

        // Act: Get the scroll ID by name
        String retrievedScrollId = Database.getScrollIdByName(scrollName);

        // Assert: Ensure the correct scroll ID is retrieved
        assertEquals(scrollId, retrievedScrollId, "The scroll ID should match the one added");
    }

    @Test
    void testGetScrollsByUploaderId() throws SQLException {
        // Arrange: Add some scrolls for user "1"
        String userId = "1";
        String username = "admin";
        LocalDateTime uploadDate = LocalDateTime.now();

        // Add two scrolls for the same user
        Database.addScroll("scroll1", "Scroll1", userId, uploadDate, 2048, TEST_SCROLL_DIR + File.separator + "Scroll1.txt");
        Database.addScroll("scroll2", "Scroll2", userId, uploadDate, 1024, TEST_SCROLL_DIR + File.separator + "Scroll2.txt");

        // Act: Retrieve the scrolls for the user
        List<Scroll> scrolls = Database.getScrollsByUploaderId(userId);

        // Assert: Ensure that both scrolls are retrieved
        assertEquals(2, scrolls.size(), "There should be two scrolls for the user");

        // Check the details of the first scroll
        Scroll scroll1 = scrolls.get(0);
        assertEquals("scroll1", scroll1.getId());
        assertEquals("Scroll1", scroll1.getName());
        assertEquals(userId, scroll1.getUploaderId());
        assertEquals(username, scroll1.getUploaderUsername());
        assertEquals(2048, scroll1.getFileSize());

        // Check the details of the second scroll
        Scroll scroll2 = scrolls.get(1);
        assertEquals("scroll2", scroll2.getId());
        assertEquals("Scroll2", scroll2.getName());
        assertEquals(userId, scroll2.getUploaderId());
        assertEquals(username, scroll2.getUploaderUsername());
        assertEquals(1024, scroll2.getFileSize());
    }


    @Test
    void testUpdateScrollStats() throws SQLException {
        // Arrange: Add a scroll and its stats to the database first
        String scrollName = "TestScroll";
        int initialUploadCount = 5;
        int initialDownloadCount = 10;
        Database.addScroll("1234", scrollName, "1", LocalDateTime.now(), 2048, TEST_SCROLL_DIR + File.separator + scrollName + ".txt");

        // Assume scrollStats are initialized somewhere (you can add a method to insert stats into the scrollStats table)
        // Act: Update the scroll stats
        int newUploadCount = 7;
        int newDownloadCount = 15;
        Database.updateScrollStats(scrollName, newUploadCount, newDownloadCount);

        // Assert: Retrieve the updated scroll stats and verify the values
        ScrollStats stats = getScrollStatsByName(scrollName);
        assertEquals(newUploadCount, stats.getUploadCount(), "Upload count should be updated");
        assertEquals(newDownloadCount, stats.getDownloadCount(), "Download count should be updated");
    }


    @Test
    void testDeleteScrollStats() throws SQLException {
        // Arrange: Add a scroll with stats to the database
        String scrollName = "ScrollToDelete";
        int uploadCount = 10;
        int downloadCount = 20;
        Database.addScroll("1234", scrollName, "1", LocalDateTime.now(), 2048, TEST_SCROLL_DIR + File.separator + scrollName + ".txt");

        // Ensure the scroll stats were added before the delete
        ScrollStats statsBeforeDelete = getScrollStatsByName(scrollName);
        assertNotNull(statsBeforeDelete, "Scroll stats should exist before deletion");

        // Act: Delete the scroll stats
        Database.deleteScrollStats(scrollName);

        // Assert: Ensure the scroll stats are deleted
        ScrollStats statsAfterDelete = getScrollStatsByName(scrollName);
        assertNull(statsAfterDelete, "Scroll stats should be deleted");
    }


    public static ScrollStats getScrollStatsByName(String name) throws SQLException {
        String query = "SELECT upload_count, uploader_name, uploader_id, download_count, orphaned FROM scrollStats WHERE name = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int uploadCount = resultSet.getInt("upload_count");
                    int downloadCount = resultSet.getInt("download_count");
                    boolean orphaned = resultSet.getBoolean("orphaned");
                    String uploader_name = resultSet.getString("uploader_name");
                    int uploader_id = resultSet.getInt("uploader_id");
                    return new ScrollStats(name, uploader_name, uploader_id, uploadCount, downloadCount, orphaned);
                }
            }
        }
        return null; // Return null if no stats found for the scroll
    }

    @Test
    void testToggleOrphanedInDatabase() throws SQLException {
        // Arrange: Add a scroll with stats to the database
        String scrollName = "ScrollToToggleOrphaned";
        int uploadCount = 15;
        int downloadCount = 25;
        Database.addScroll("5678", scrollName, "1", LocalDateTime.now(), 4096, TEST_SCROLL_DIR + File.separator + scrollName + ".txt");
        ScrollStats scrollStats = getScrollStatsByName(scrollName);
        assertFalse(scrollStats.isOrphaned(), "Initially, the scroll should not be orphaned");

        // Act: Toggle the orphaned status to true
        scrollStats.setOrphaned(true);
        Database.toggleOrphanedInDatabase(scrollStats);

        // Assert: Ensure the orphaned status is updated to true in the database
        ScrollStats updatedStats = getScrollStatsByName(scrollName);
        assertTrue(updatedStats.isOrphaned(), "Scroll stats should be marked as orphaned");

        // Act: Toggle the orphaned status back to false
        scrollStats.setOrphaned(false);
        Database.toggleOrphanedInDatabase(scrollStats);

        // Assert: Ensure the orphaned status is updated back to false
        updatedStats = getScrollStatsByName(scrollName);
        assertFalse(updatedStats.isOrphaned(), "Scroll stats should no longer be marked as orphaned");
    }

    @Test
    void testDeleteScroll_ScrollNotFound() throws SQLException, IOException {
        // Arrange: Use a non-existent scrollId
        String scrollId = "nonExistentScrollId";

        // Act: Try to delete the non-existent scroll
        Database.deleteScroll(scrollId);

        // Assert: Check that the appropriate message is logged
        // Capture console output or verify no changes were made in the database
    }

    @Test
    void testDeleteScroll_ScrollFoundButFileNotFound() throws SQLException, IOException {
        // Arrange: Add a scroll with a non-existent file path
        String scrollId = "1234";
        String scrollName = "TestScroll";
        Database.addScroll(scrollId, scrollName, "1", LocalDateTime.now(), 2048, "nonExistentFilePath.txt");

        // Act: Try to delete the scroll
        Database.deleteScroll(scrollId);

        // Assert: Check that the correct log message "File not found" is printed
        ScrollStats stats = getScrollStatsByName(scrollName);
        assertTrue(stats.isOrphaned(), "Scroll should be marked as orphaned");
    }

    @Test
    void testDeleteScroll_FileDeletionFails() throws SQLException, IOException {
        // Arrange: Add a scroll with a file that cannot be deleted (e.g., locked or permission issues)
        String scrollId = "5678";
        String scrollName = "UndeletableFileScroll";

        // Assume you manually lock the file or simulate permission issues
        String undeletableFilePath = "lockedFile.txt"; // simulate file system restriction
        Database.addScroll(scrollId, scrollName, "1", LocalDateTime.now(), 2048, undeletableFilePath);

        // Act: Try to delete the scroll
        Database.deleteScroll(scrollId);

        // Assert: Ensure that the appropriate log "Failed to delete the file" is printed
    }

    @Test
    void testDeleteScroll_Success() throws SQLException, IOException {
        // Arrange: Add a scroll and an associated file
        String scrollId = "4321";
        String scrollName = "ValidScroll";
        String filePath = TEST_SCROLL_DIR + File.separator + "ValidScroll.txt";
        File file = new File(filePath);
        file.createNewFile(); // Create the file so that it exists

        Database.addScroll(scrollId, scrollName, "1", LocalDateTime.now(), 2048, filePath);

        // Act: Delete the scroll
        Database.deleteScroll(scrollId);

        // Assert: Ensure that the file was deleted and the scroll stats were updated
        assertFalse(file.exists(), "The file should be deleted");
        ScrollStats stats = getScrollStatsByName(scrollName);
        assertTrue(stats.isOrphaned(), "Scroll should be marked as orphaned");
    }

    @Test
    void testAddScroll_ScrollExists_Update() throws SQLException {
        // Arrange: Add a scroll that already exists
        String scrollId = "1234";
        String oldName = "OldScroll";
        String newName = "UpdatedScroll";
        String uploaderId = "1";
        LocalDateTime oldUploadDate = LocalDateTime.now().minusDays(1);
        LocalDateTime newUploadDate = LocalDateTime.now();
        long oldFileSize = 2048;
        long newFileSize = 4096;
        String oldFilePath = TEST_SCROLL_DIR + File.separator + "OldScroll.txt";
        String newFilePath = TEST_SCROLL_DIR + File.separator + "UpdatedScroll.txt";

        // Insert the scroll first
        Database.addScroll(scrollId, oldName, uploaderId, oldUploadDate, oldFileSize, oldFilePath);

        // Act: Update the scroll with new details
        Database.addScroll(scrollId, newName, uploaderId, newUploadDate, newFileSize, newFilePath);
    }

    @Test
    void testAddScroll_UploaderNotFound() {
        // Arrange: Create a scroll with a non-existent uploader ID
        String scrollId = "1234";
        String scrollName = "TestScroll";
        String nonExistentUploaderId = "9999"; // Assume this uploader ID doesn't exist
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 1024;
        String filePath = TEST_SCROLL_DIR + File.separator + "TestScroll.txt";

        // Act & Assert: Expect an exception when adding the scroll
        SQLException exception = assertThrows(SQLException.class, () -> {
            Database.addScroll(scrollId, scrollName, nonExistentUploaderId, uploadDate, fileSize, filePath);
        });
        assertTrue(exception.getMessage().contains("Uploader username not found for uploader ID"));
    }

    @Test
    void testAddScroll_ScrollStatsExists_UnmarkOrphaned() throws SQLException {
        // Arrange: Add a scroll and set its stats as orphaned
        String scrollId = "5678";
        String scrollName = "OrphanedScroll";
        String uploaderId = "1";
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 1024;
        String filePath = TEST_SCROLL_DIR + File.separator + "OrphanedScroll.txt";

        // Add the scroll and mark it as orphaned
        Database.addScroll(scrollId, scrollName, uploaderId, uploadDate, fileSize, filePath);
        ScrollStats stats = new ScrollStats(scrollName, "admin", 1,1, 0, true);
        Database.toggleOrphanedInDatabase(stats);

        // Assert the scroll is orphaned
        ScrollStats orphanedStats = getScrollStatsByName(scrollName);
        assertTrue(orphanedStats.isOrphaned(), "Scroll should be marked as orphaned");

        // Act: Add the scroll again to unmark it as orphaned
        Database.addScroll(scrollId, scrollName, uploaderId, uploadDate, fileSize, filePath);

        // Assert: Ensure the orphaned flag is now false
        ScrollStats updatedStats = getScrollStatsByName(scrollName);
        assertFalse(updatedStats.isOrphaned(), "Scroll should no longer be marked as orphaned");
    }


    @Test
    void testUpdateScroll_ActualFileMissing() throws SQLException, IOException {
        // Arrange: Add a scroll to update
        String scrollId = "4";
        String oldName = "OldScrollName";
        String newName = "NewScrollName";
        String uploaderId = "1";
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 5120;
        String filePath = TEST_SCROLL_DIR + oldName + ".txt";
        // Add the scroll
        Database.addScroll(scrollId, oldName, uploaderId, uploadDate, fileSize, filePath);
        // Act: Update the scroll name
        Database.updateScrollName(scrollId, newName);

        // Assert: Ensure the scroll name was updated
        assertNull(Database.getScrollIdByName(newName), "New scroll name should not exist");
        wipeDirectory(new File(TEST_SCROLL_DIR));
    }

    @Test
    void testUpdateScroll_NewFileExists() throws SQLException, IOException {
        // Arrange: Add a scroll to update
        String scrollId = "4";
        String oldName = "OldScrollName";
        String newName = "NewScrollName";
        String uploaderId = "1";
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 5120;
        String filePath = TEST_SCROLL_DIR + oldName + ".txt";
        String newFilePath = TEST_SCROLL_DIR + "NewScrollName";
        // Add the scroll
        Database.addScroll(scrollId, oldName, uploaderId, uploadDate, fileSize, filePath);

        // Create a file with the new name already
        File scrollFile = new File(newFilePath);
        if (!scrollFile.exists()) {
            File parentDir = scrollFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs(); // Create directories if they don't exist
            }
            scrollFile.createNewFile(); // Create the scroll file
            System.out.println("Scroll file created: " + scrollFile.getAbsolutePath());
        } else {
            System.out.println("Scroll file already exists.");
        }
        // Act: Update the scroll name
        Database.updateScrollName(scrollId, newName);

        // Assert: Ensure the scroll name was updated
        assertNotNull(Database.getScrollIdByName(oldName), "Old scroll name should still exist");
        wipeDirectory(new File(TEST_SCROLL_DIR));
    }



    @Test
    void testUpdateScrollName_FileIssue() throws SQLException, IOException {
        // Arrange: Simulate a scroll with an old file path that does not exist
        String scrollId = "1234";
        String scrollName = "TestScroll";
        String uploaderId = "1";
        LocalDateTime uploadDate = LocalDateTime.now();
        long fileSize = 1024;
        String nonExistentFilePath = TEST_SCROLL_DIR + File.separator + "NonExistentFile.txt";

        // Add scroll to the database with a non-existent file
        Database.addScroll(scrollId, scrollName, uploaderId, uploadDate, fileSize, nonExistentFilePath);

        // Act & Assert: Attempt to update the scroll name and expect a file issue
        Database.updateScrollName(scrollId, "NewScrollName");

        // The log should indicate that there was a file issue
    }


    @Test
    void testDeleteScrollStatsNotFound() throws SQLException {
        // Arrange: Add a scroll with stats to the database
        String scrollName = "ScrollToDelete";
        int uploadCount = 10;
        int downloadCount = 20;
        Database.addScroll("1234", scrollName, "1", LocalDateTime.now(), 2048, TEST_SCROLL_DIR + File.separator + scrollName + ".txt");

        // Ensure the scroll stats were added before the delete
        ScrollStats statsBeforeDelete = getScrollStatsByName(scrollName);
        assertNotNull(statsBeforeDelete, "Scroll stats should exist before deletion");

        // Act: Delete the scroll stats
        Database.deleteScrollStats("junk");

        // Assert: Ensure the scroll stats are deleted
        ScrollStats statsAfterDelete = getScrollStatsByName(scrollName);
        assertNotNull(statsAfterDelete, "Scroll stats shouldn't be deleted");
    }

    @Test
    void testGetUploaderIdbyNonExistingScrollName() throws SQLException {
        assertNull(Database.getUploaderIdByScrollName("junk"));
    }


    }
























}
