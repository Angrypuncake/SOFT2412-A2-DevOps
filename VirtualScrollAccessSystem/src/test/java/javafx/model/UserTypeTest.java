package javafx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
//

class UserTypeTest {

    @Test
    void testGuestDisplayName() {
        assertEquals("Guest", UserType.GUEST.getDisplayName());
    }

    @Test
    void testNormalDisplayName() {
        assertEquals("Normal", UserType.NORMAL.getDisplayName());
    }

    @Test
    void testAdminDisplayName() {
        assertEquals("Admin", UserType.ADMIN.getDisplayName());
    }

    @Test
    void testValues() {
        UserType[] expectedValues = {UserType.GUEST, UserType.NORMAL, UserType.ADMIN};
        assertArrayEquals(expectedValues, UserType.values());
    }

    @Test
    void testValueOf() {
        assertEquals(UserType.GUEST, UserType.valueOf("GUEST"));
        assertEquals(UserType.NORMAL, UserType.valueOf("NORMAL"));
        assertEquals(UserType.ADMIN, UserType.valueOf("ADMIN"));
    }
}
