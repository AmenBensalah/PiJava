package edu.ProjetPI.controllers;

import edu.ProjetPI.entities.User;

public class DashboardSession {

    private static User currentUser;

    private DashboardSession() {
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}
