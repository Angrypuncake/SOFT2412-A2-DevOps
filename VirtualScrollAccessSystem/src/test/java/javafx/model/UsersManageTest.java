package javafx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class User1Test {

    private UsersManage user;
    private final String id = "001";
    private final String name = "John Doe";
    private final String email = "john.doe@example.com";
    private final String phone = "1234567890";
    private final String fullname = "John Doe";

    @BeforeEach
    void setUp() {
        user = new UsersManage(id, name, fullname, email, phone);
    }

    @Test
    void testConstructor() {
        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
        assertEquals(email, user.getEmail());
        assertEquals(phone, user.getPhone());
        assertEquals(fullname, user.getFullName());
    }

    @Test
    void testGetId() {
        assertEquals(id, user.getId());
    }

    @Test
    void testGetName() {
        assertEquals(name, user.getName());
    }

    @Test
    void testGetEmail() {
        assertEquals(email, user.getEmail());
    }

    @Test
    void testGetPhone() {
        assertEquals(phone, user.getPhone());
    }

    @Test
    void testToString() {
        assertEquals(name, user.toString());
    }
}
