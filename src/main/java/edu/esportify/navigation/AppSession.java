package edu.esportify.navigation;

import edu.esportify.entities.Equipe;

public final class AppSession {
    private static final AppSession INSTANCE = new AppSession();

    private String username;
    private String role;
    private Equipe selectedEquipe;

    private AppSession() {
    }

    public static AppSession getInstance() {
        return INSTANCE;
    }

    public void login(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public void logout() {
        username = null;
        role = null;
        selectedEquipe = null;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public Equipe getSelectedEquipe() {
        return selectedEquipe;
    }

    public void setSelectedEquipe(Equipe selectedEquipe) {
        this.selectedEquipe = selectedEquipe;
    }
}
