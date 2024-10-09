package javafx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserSessionTest {

    @BeforeEach
    public void clearSession() {
        // Ensure the session is cleared before each test
        UserSession.endSession();
    }

    @Test
    public void testStartSession() {
        // Start a new session
        UserSession.startSession("1", "testUser", "User");

        // Retrieve the current session and verify the values
        UserSession session = UserSession.getInstance();
        assertEquals("1", session.getUserId(), "User ID should be '1'");
        assertEquals("testUser", session.getUsername(), "Username should be 'testUser'");
        assertEquals("User", session.getRole(), "Role should be 'User'");
    }

    @Test
    public void testSingletonBehavior() {
        // Start a session
        UserSession.startSession("1", "testUser", "User");

        // Attempt to start another session with different values
        UserSession.startSession("2", "newUser", "Admin");

        // Ensure the initial session is still in place
        UserSession session = UserSession.getInstance();
        assertEquals("1", session.getUserId(), "User ID should remain '1'");
        assertEquals("testUser", session.getUsername(), "Username should remain 'testUser'");
        assertEquals("User", session.getRole(), "Role should remain 'User'");
    }

    @Test
    public void testEndSession() {
        // Start and then end the session
        UserSession.startSession("1", "testUser", "User");
        UserSession.endSession();

        // Verify that trying to get the session throws an exception
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            UserSession.getInstance();
        });
        assertEquals("No user is logged in.", exception.getMessage());
    }

    @Test
    public void testIsAdmin() {
        // Start an admin session and verify the isAdmin method
        UserSession.startSession("1", "adminUser", "Admin");
        assertTrue(UserSession.getInstance().isAdmin(), "User should be an admin");

        // Start a non-admin session and verify
        UserSession.endSession();
        UserSession.startSession("2", "testUser", "User");
        assertFalse(UserSession.getInstance().isAdmin(), "User should not be an admin");
    }

    @Test
    public void testIsGuest() {
        // Start a guest session and verify the isGuest method
        UserSession.startSession("3", "guestUser", "Guest");
        assertTrue(UserSession.getInstance().isGuest(), "User should be a guest");

        // Start a non-guest session and verify
        UserSession.endSession();
        UserSession.startSession("4", "testUser", "User");
        assertFalse(UserSession.getInstance().isGuest(), "User should not be a guest");
    }
}
