package edu.esportify.entities;

public class UserProfile {
    private final int id;
    private final String displayName;
    private final String email;
    private final String role;
    private final String avatarLabel;

    public UserProfile(int id, String displayName, String email, String role, String avatarLabel) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
        this.avatarLabel = avatarLabel;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getAvatarLabel() {
        return avatarLabel;
    }
}
