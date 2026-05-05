package edu.esportify.navigation;

import edu.esportify.entities.Equipe;
import edu.esportify.entities.User;

public final class AppSession {
    public enum UserHomeSection {
        FEED,
        STORE,
        TEAMS,
        TOURNAMENTS,
        ORDERS,
        ACCOUNT
    }

    public enum AdminSection {
        OVERVIEW,
        TEAMS,
        REQUESTS,
        STORE
    }

    private static final AppSession INSTANCE = new AppSession();

    private String username;
    private String role;
    private User currentUser;
    private Equipe selectedEquipe;
    private UserHomeSection pendingUserHomeSection = UserHomeSection.STORE;
    private AdminSection pendingAdminSection = AdminSection.OVERVIEW;

    private AppSession() {
    }

    public static AppSession getInstance() {
        return INSTANCE;
    }

    public void login(User user) {
        this.currentUser = user;
        this.username = user == null ? null : user.getUsername();
        this.role = user == null || user.getRole() == null ? null : user.getRole().getDisplayName();
    }

    public void logout() {
        username = null;
        role = null;
        currentUser = null;
        selectedEquipe = null;
        pendingUserHomeSection = UserHomeSection.STORE;
        pendingAdminSection = AdminSection.OVERVIEW;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Equipe getSelectedEquipe() {
        return selectedEquipe;
    }

    public void setSelectedEquipe(Equipe selectedEquipe) {
        this.selectedEquipe = selectedEquipe;
    }

    public UserHomeSection getPendingUserHomeSection() {
        return pendingUserHomeSection;
    }

    public void setPendingUserHomeSection(UserHomeSection pendingUserHomeSection) {
        this.pendingUserHomeSection = pendingUserHomeSection == null
                ? UserHomeSection.STORE
                : pendingUserHomeSection;
    }

    public AdminSection getPendingAdminSection() {
        return pendingAdminSection;
    }

    public void setPendingAdminSection(AdminSection pendingAdminSection) {
        this.pendingAdminSection = pendingAdminSection == null
                ? AdminSection.OVERVIEW
                : pendingAdminSection;
    }
}
