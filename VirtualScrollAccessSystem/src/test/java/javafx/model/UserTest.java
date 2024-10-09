package javafx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private User user;

    @BeforeEach
    public void setup() {
        user = new User("1", "testUser", "testPassword", "test@example.com", "123456789");
    }

    @Test
    public void testGetId() {
        assertEquals("1", user.getId(), "User ID should be '1'");
    }

    @Test
    public void testGetUsername() {
        assertEquals("testUser", user.getUsername(), "Username should be 'testUser'");
    }

    @Test
    public void testSetUsername() {
        user.setUsername("newUsername");
        assertEquals("newUsername", user.getUsername(), "Username should be updated to 'newUsername'");
    }

    @Test
    public void testSetPassword() {
        user.setPassword("newPassword");
        assertTrue(user.authenticate("testUser", "newPassword"), "Password should be updated and match for authentication");
    }

    @Test
    public void testGetEmail() {
        assertEquals("test@example.com", user.getEmail(), "Email should be 'test@example.com'");
    }

    @Test
    public void testSetEmail() {
        user.setEmail("new@example.com");
        assertEquals("new@example.com", user.getEmail(), "Email should be updated to 'new@example.com'");
    }

    @Test
    public void testGetPhoneNumber() {
        assertEquals("123456789", user.getPhoneNumber(), "Phone number should be '123456789'");
    }

    @Test
    public void testSetPhoneNumber() {
        user.setPhoneNumber("987654321");
        assertEquals("987654321", user.getPhoneNumber(), "Phone number should be updated to '987654321'");
    }

    @Test
    public void testAuthenticate() {
        assertTrue(user.authenticate("testUser", "testPassword"), "Authentication should succeed with correct username and password");
        assertFalse(user.authenticate("testUser", "wrongPassword"), "Authentication should fail with incorrect password");
        assertFalse(user.authenticate("wrongUser", "testPassword"), "Authentication should fail with incorrect username");
    }

    @Test
    public void testGetUserType() {
        assertEquals("Normal", user.getUserType(), "Default user type should be 'Normal'");
    }
}
