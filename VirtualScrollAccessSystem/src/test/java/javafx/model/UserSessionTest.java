package javafx.model;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    @BeforeEach
    void resetSession() {
        UserSession.endSession();  // Reset session to ensure clean state before each test
    }

    @Test
    void testSingletonInstanceIsCreated() {
        // Ensure that a session starts as Guest if no session is active
        UserSession session = UserSession.getInstance();
        assertNotNull(session);
        assertEquals("Guest", session.getUsername());
        assertEquals("Guest", session.getRole());
    }

    @Test
    void testStartSessionWithValidUser() {
        // Start a session with specific user details
        UserSession.startSession("1", "testUser", "User");
        UserSession session = UserSession.getInstance();

        // Validate that the session details match
        assertEquals("1", session.getUserId());
        assertEquals("testUser", session.getUsername());
        assertEquals("User", session.getRole());
    }

    @Test
    void testEndSessionResetsToGuest() {
        // Start a session with a valid user
        UserSession.startSession("1", "testUser", "User");

        // End the session and check if it's reset to "Guest"
        UserSession.endSession();
        UserSession session = UserSession.getInstance();

        // Validate the session is now for a guest user
        assertNull(session.getUserId());
        assertEquals("Guest", session.getUsername());
        assertEquals("Guest", session.getRole());
        assertTrue(session.isGuest());
    }

    @Test
    void testIsAdminWhenRoleIsAdmin() {
        // Start a session with admin role
        UserSession.startSession("1", "adminUser", "Admin");
        UserSession session = UserSession.getInstance();

        // Validate the user is an admin
        assertTrue(session.isAdmin());
        assertFalse(session.isUser());
        assertFalse(session.isGuest());
    }

    @Test
    void testIsUserWhenRoleIsUser() {
        // Start a session with user role
        UserSession.startSession("1", "regularUser", "User");
        UserSession session = UserSession.getInstance();

        // Validate the user is a regular user
        assertTrue(session.isUser());
        assertFalse(session.isAdmin());
        assertFalse(session.isGuest());
    }

    @Test
    void testDefaultRoleIsGuestWhenRoleIsNullOrEmpty() {
        // Start a session without specifying the role (null)
        UserSession.startSession("1", "testUser", null);
        UserSession session = UserSession.getInstance();

        // Validate the session defaults to "Guest" role
        assertEquals("Guest", session.getRole());
        assertTrue(session.isGuest());

        // Start a session with an empty role
        UserSession.startSession("2", "emptyRoleUser", "");
        session = UserSession.getInstance();

        // Validate the session defaults to "Guest" role
        assertEquals("Guest", session.getRole());
        assertTrue(session.isGuest());
    }
}
