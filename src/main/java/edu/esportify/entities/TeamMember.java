package edu.esportify.entities;

public class TeamMember {
    private final String username;
    private final String role;
    private final boolean active;

    public TeamMember(String username, String role, boolean active) {
        this.username = username;
        this.role = role;
        this.active = active;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}
