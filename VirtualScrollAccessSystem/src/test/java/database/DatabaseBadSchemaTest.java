package database;

import javafx.model.ScrollStats;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseBadSchemaTest {
    private static final String TEST_DB_URL = "jdbc:sqlite:src/test/resources/db/test_database2.db";
    private static final String TEST_SCROLL_DIR = "src/test/resources/scrolls/";

    @Test
    void setupWithLockedDB() throws SQLException, IOException {
        // Create the directories if they don't exist
        Files.createDirectories(Paths.get("src/test/resources/db"));
        Files.createDirectories(Paths.get(TEST_SCROLL_DIR));

        // Initialize the database setup for the tests
        Database.setTestDbUrl(TEST_DB_URL);

        // Step 1: Lock the database file
        Path dbPath = Paths.get("src/test/resources/db/test_database2.db");
        try (RandomAccessFile dbFile = new RandomAccessFile(dbPath.toFile(), "rw");
             FileChannel dbChannel = dbFile.getChannel()) {

            // Lock the database file
            FileLock lock = dbChannel.lock();

            // Step 2: Attempt the setup while the file is locked
            try {
                Database.setup(); // This should fail because the file is locked
            } catch (SQLException e) {
                System.out.println("Database setup failed due to locked file: " + e.getMessage());
            }


            // Step 3: Unlock the database
            lock.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void closeConnectionTest() throws SQLException {


    }



    @Nested
    public class MaintestBlock{
        @BeforeAll
        static void setupAll() throws SQLException, IOException {
            Files.createDirectories(Paths.get("src/test/resources/db"));
            Files.createDirectories(Paths.get(TEST_SCROLL_DIR));
            // Initialize the database setup for the tests
            Database.setTestDbUrl(TEST_DB_URL);
        }

        @Test
        void checkAdminUser() throws SQLException {
            Database.ensureAdminUserExists();
            Connection connection = Database.getConnection();
            try (var statement = connection.prepareStatement("SELECT * FROM users WHERE username = ?")) {
                statement.setString(1, "admin");
            }
            catch (SQLException e) {
                System.out.println("Successfully caught sql exception");
            }
        }

        @Test
        void getScrollByUploaderIdTest() {
            try {
                Database.getScrollsByUploaderId("1");
            } catch (SQLException e) {
                System.out.println("Successfully caught sql exception");
            }

        }

        @Test
        void deleteScroll() throws IOException {
            try{
                Database.deleteScroll("1");
            } catch (SQLException e) {
                System.out.println("Successfully caught sql exception");
            }

        }

        @Test
        void loadScrollStatsTest() throws IOException {
            try{
                Database.loadScrollStats();
            } catch (SQLException e) {
                System.out.println("Successfully caught sql exception");
            }
        }

        @Test
        void getScrollIdByNameTest(){
            try{
                Database.getScrollIdByName("blah");
            } catch(SQLException e){
                System.out.println("Successfully caught sql exception");
            }
        }

        @Test
        void getUploaderIdByScrollNameTest(){
            try{
                Database.getUploaderIdByScrollName("whofhwefoa");
            } catch(SQLException e){
                System.out.println("Successfully caught sql exception");
            }
        }

        @Test
        void deleteScrollStatsTest(){
            try{
                Database.deleteScrollStats("wifhobnae");
            } catch(SQLException e){
                System.out.println("Successfully caught sql exception");
            }
        }

        @Test
        void toggleOrphanedInDatabaseTest(){
            ScrollStats scrollStats = new ScrollStats("131", "21", 3, 1,2,false);;
            try{
                Database.toggleOrphanedInDatabase(scrollStats);
            } catch(SQLException e){
                System.out.println("Successfully caught sql exception");
            }
        }



    }


}



