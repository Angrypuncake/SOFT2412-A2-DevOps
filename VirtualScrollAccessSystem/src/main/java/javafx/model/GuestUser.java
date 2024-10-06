package javafx.model;

public class GuestUser extends User {

    public GuestUser() {
        super("guest", "guest", "", "", "");
    }

    @Override
    public String getUserType() {
        return "Guest";
    }
}
