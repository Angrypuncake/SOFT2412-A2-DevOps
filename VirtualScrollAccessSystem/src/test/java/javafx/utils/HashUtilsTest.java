package javafx.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HashUtilsTest {

    @Test
    public void testHashPassword() {
        // Define the input and the expected result for testing
        String password = "mySecurePassword123";
        String expectedHash = "ca6ee54120465533d367b4cac5cd2f12ee75234225130dd89470de546ab9ca46";

        // Call the hashPassword method
        String actualHash = HashUtils.hashPassword(password);

        // Check if the result matches the expected hash
        assertEquals(expectedHash, actualHash, "The hashed password does not match the expected hash!");
    }

    @Test
    public void testHashPasswordNotNull() {
        String password = "anotherPassword";
        String hash = HashUtils.hashPassword(password);

        // Verify the hashed password is not null
        assertNotNull(hash, "Hashed password should not be null");
    }
}
