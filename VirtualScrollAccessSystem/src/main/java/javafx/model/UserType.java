package javafx.model;

public enum UserType {
    GUEST("Guest"),
    NORMAL("Normal"),
    ADMIN("Admin");

    private String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
