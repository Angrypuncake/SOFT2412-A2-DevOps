package javafx.model;

public class UserSession {

    private static UserSession instance;

    private final String userId;
    private final String username;
    private final String role;  // e.g., "Guest", "User", "Admin"
    private String shadow; // e.g "Guest", "User", "Admin"

    // Private constructor to enforce Singleton pattern
    private UserSession(String userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = (role == null || role.isEmpty()) ? "Guest" : role;  // Default to "Guest" if no role provided
        this.shadow = "Empty";
    }

    // Static method to start a session
    public static void startSession(String userId, String username, String role) {
        instance = new UserSession(userId, username, role);
    }

    // Static method to get the current session
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession(null, "Guest", "Guest");  // Default to Guest session
        }
        return instance;
    }

    // Static method to reset the session to "Guest" (end the user session)
    public static void endSession() {
        instance = new UserSession(null, "Guest", "Guest");  // Reset to guest session
    }

    // Getters for user information
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    // Example: Check if the user is an admin
    public boolean isAdmin() {
        return "Admin".equals(role);
    }

    // Example: Check if the user is logged in as a guest
    public boolean isGuest() {
        return "Guest".equals(role);
    }

    // Example: Check if the user is a regular user
    public boolean isUser() {
        return "User".equals(role);
    }

    //Check if a user is in a shadow mode
    public void setShadow(String state) {this.shadow = state;
    }
    //Check if a user is in a shadow mode
    public String getShadow() {
        return this.shadow;
    }
}
