package javafx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class ScrollTest {

    private Scroll scroll;

    @BeforeEach
    public void setup() {
        scroll = new Scroll("1", "Ancient Scroll", "uploader123", LocalDate.of(2023, 10, 9), "/path/to/binaryData");
    }

    @Test
    public void testGetId() {
        assertEquals("1", scroll.getId(), "The Scroll ID should be 1");
    }

    @Test
    public void testGetName() {
        assertEquals("Ancient Scroll", scroll.getName(), "The Scroll name should be 'Ancient Scroll'");
    }

    @Test
    public void testGetUploadDate() {
        assertEquals(LocalDate.of(2023, 10, 9), scroll.getUploadDate(), "The upload date should be 2023-10-09");
    }

    @Test
    public void testSetters() {
        scroll = new Scroll("2", "Mystic Scroll", "uploader456", LocalDate.of(2024, 1, 15), "/new/path/to/data");
        assertEquals("2", scroll.getId(), "The Scroll ID should now be 2");
        assertEquals("Mystic Scroll", scroll.getName(), "The Scroll name should now be 'Mystic Scroll'");
        assertEquals(LocalDate.of(2024, 1, 15), scroll.getUploadDate(), "The upload date should now be 2024-01-15");
    }
}
