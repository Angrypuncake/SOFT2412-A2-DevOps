package javafx.model;

public class UsersManage {
    private final String id;
    private final String name;
    private final String email;
    private final String phone;
    private final String fullName;

    public UsersManage(String id, String name, String fullName, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.fullName = fullName;
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

    public String getFullName() {return fullName;}

    @Override
    public String toString() {
        return name; // Display name in ListView
    }
}