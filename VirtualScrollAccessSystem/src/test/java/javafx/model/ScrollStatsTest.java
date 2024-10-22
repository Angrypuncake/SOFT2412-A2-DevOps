package javafx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ScrollStatsTest {

    private String name = "scroll";
    private String uploaderName = "John Doe";
    private int uploadCount = 1;
    private int downloadCount = 1;
    private boolean orphaned = false;
    private int uploaderId = 1;
    private ScrollStats stats;

    @BeforeEach
    public void setup() {
        stats = new ScrollStats(name, uploaderName, uploaderId, uploadCount, downloadCount, orphaned);

    }

    @Test
    public void testGetName() {
        assertEquals(name, stats.getName());
    }

    @Test
    public void testGetUploader() {
        assertEquals(uploaderName, stats.getUploaderName());
    }

    @Test
    public void testGetUploadCount() {
        assertEquals(uploadCount, stats.getUploadCount());
    }

    @Test
    public void testgetDownloadCount() {
        assertEquals(downloadCount, stats.getDownloadCount());
    }

    @Test
    public void testGetUploaderId() {
        assertEquals(uploaderId, stats.getUploaderId());
    }

    @Test
    public void testGetOrphaned() {
        assertEquals(orphaned, stats.isOrphaned());
    }

    @Test
    public void testSetOrphaned() {
        stats.setOrphaned(true);
        assertEquals(true, stats.isOrphaned());
    }
}
