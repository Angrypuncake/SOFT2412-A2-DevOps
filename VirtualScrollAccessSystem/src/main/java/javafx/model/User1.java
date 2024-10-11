package javafx.model;

public class User1 {
    private String id;
    private String name;
    private String email;
    private String phone;

    public User1(String id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toString() {
        return name; // Display name in ListView
    }
}