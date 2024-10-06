package javafx.model;


public class User {
    protected String id;
    protected String username;
    protected String password;
    protected String email;
    protected String phoneNumber;
    protected UserType userType;

    // Constructor
    public User(String id, String username, String password, String email, String phoneNumber) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.userType = UserType.NORMAL;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserType(){
        return this.userType.getDisplayName();
    }
}