package javafx.model;

import java.util.List;

public class AdminUser extends User {

    public AdminUser(String id, String username, String password, String email, String phoneNumber) {
        super(id, username, password, email, phoneNumber);
    }

    @Override
    public String getUserType() {
        return "Admin";
    }

    // Admin privileges: javafx.model.view users, delete users, etc.
    public void viewUsers(List<User> users) {
        // Logic to display users
    }

    public void deleteUser(User user) {
        // Logic to delete user
    }
}
