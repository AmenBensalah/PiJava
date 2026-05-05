package edu.esportify.entities;

public enum UserRole {
    ADMIN("Admin"),
    MANAGER("Manager"),
    USER("User");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserRole fromValue(String value) {
        if (value == null || value.isBlank()) {
            return USER;
        }
        if ("ROLE_ADMIN".equalsIgnoreCase(value)) {
            return ADMIN;
        }
        if ("ROLE_MANAGER".equalsIgnoreCase(value) || "ROLE_ORGANISATEUR".equalsIgnoreCase(value)) {
            return MANAGER;
        }
        if ("ROLE_JOUEUR".equalsIgnoreCase(value)) {
            return USER;
        }
        for (UserRole role : values()) {
            if (role.name().equalsIgnoreCase(value) || role.displayName.equalsIgnoreCase(value)) {
                return role;
            }
        }
        return USER;
    }
}
