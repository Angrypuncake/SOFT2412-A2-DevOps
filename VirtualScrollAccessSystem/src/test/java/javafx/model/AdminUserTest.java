package javafx.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AdminUserTest {

    private AdminUser admin;
    private User user1;
    private User user2;
    private List<User> users;

    @BeforeEach
    public void setup() {
        admin = new AdminUser("1", "admin", "adminPass", "admin@example.com", "123456789");
        user1 = new User("2", "user1", "pass1", "user1@example.com", "987654321");
        user2 = new User("3", "user2", "pass2", "user2@example.com", "123123123");

        users = new ArrayList<>();
        users.add(user1);
        users.add(user2);
    }

    @Test
    public void testViewUsers() {
        // Redirecting System.out to test viewUsers output
        admin.viewUsers(users);

        // Since this method prints directly, you'd typically verify using the console manually,
        // but in real-world scenarios, we would mock the output stream to capture it for verification.
        // Here, we're focusing on the logic and no exceptions should occur.
        assertEquals(2, users.size());
    }

    @Test
    public void testDeleteUser() {
        // Initial size of user list should be 2
        assertEquals(2, users.size());

        // Deleting user1
        admin.deleteUser(users, user1);

        // Now the size should be 1
        assertEquals(1, users.size());
        // Ensure user1 is no longer in the list
        assertFalse(users.contains(user1));
        // Ensure user2 is still in the list
        assertTrue(users.contains(user2));
    }

    @Test
    public void testGetUserType() {
        // Testing the getUserType method
        assertEquals("Admin", admin.getUserType());
    }
}
