package javafx.model;

public class UserSession {

    private static UserSession instance;

    private String userId;
    private String username;
    private String role;  // e.g., "Guest", "User", "Admin"

    // Private constructor to enforce Singleton pattern
    private UserSession(String userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    // Static method to initialize a session
    public static void startSession(String userId, String username, String role) {
        if (instance == null) {
            instance = new UserSession(userId, username, role);
        }
    }

    // Static method to get the current session
    public static UserSession getInstance() {
        if (instance == null) {
            throw new IllegalStateException("No user is logged in.");
        }
        return instance;
    }

    // Static method to clear the session (logout)
    public static void endSession() {
        instance = null;
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
}
