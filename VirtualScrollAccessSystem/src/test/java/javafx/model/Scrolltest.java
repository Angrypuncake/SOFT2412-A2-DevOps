package javafx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.LocalDateTime;

class ScrollTest {

    private Scroll scroll;
    private String id = "123";
    private String name = "Sample Scroll";
    private String uploaderId = "uploader123";
    private String uploaderUsername = "uploaderUser";
    private LocalDateTime uploadDate = LocalDateTime.now();
    private long fileSize = 2048;
    private File binaryFile = new File("path/to/file");

    @BeforeEach
    void setUp() {
        scroll = new Scroll(id, name, uploaderId, uploaderUsername, uploadDate, fileSize, binaryFile);
    }

    @Test
    void testConstructor() {
        assertEquals(id, scroll.getId());
        assertEquals(name, scroll.getName());
        assertEquals(uploaderId, scroll.getUploaderId());
        assertEquals(uploaderUsername, scroll.getUploaderUsername());
        assertEquals(uploadDate, scroll.getUploadDate());
        assertEquals(fileSize, scroll.getFileSize());
        assertEquals(binaryFile, scroll.getBinaryFile());
    }

    @Test
    void testSetId() {
        String newId = "456";
        scroll.setId(newId);
        assertEquals(newId, scroll.getId());
    }

    @Test
    void testSetName() {
        String newName = "New Scroll Name";
        scroll.setName(newName);
        assertEquals(newName, scroll.getName());
    }

    @Test
    void testSetUploaderId() {
        String newUploaderId = "uploader456";
        scroll.setUploaderId(newUploaderId);
        assertEquals(newUploaderId, scroll.getUploaderId());
    }

    @Test
    void testSetUploaderUsername() {
        String newUsername = "newUploaderUser";
        scroll.setUploaderUsername(newUsername);
        assertEquals(newUsername, scroll.getUploaderUsername());
    }

    @Test
    void testSetUploadDate() {
        LocalDateTime newDate = LocalDateTime.now().minusDays(1);
        scroll.setUploadDate(newDate);
        assertEquals(newDate, scroll.getUploadDate());
    }

    @Test
    void testSetFileSize() {
        long newSize = 4096;
        scroll.setFileSize(newSize);
        assertEquals(newSize, scroll.getFileSize());
    }

    @Test
    void testSetBinaryFile() {
        File newFile = new File("path/to/newfile");
        scroll.setBinaryFile(newFile);
        assertEquals(newFile, scroll.getBinaryFile());
    }
}
