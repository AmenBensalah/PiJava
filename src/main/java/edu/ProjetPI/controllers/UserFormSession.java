package edu.ProjetPI.controllers;

import edu.ProjetPI.entities.User;

public final class UserFormSession {

    private static User editingUser;

    private UserFormSession() {
    }

    public static void prepareCreate() {
        editingUser = null;
    }

    public static void prepareEdit(User user) {
        if (user == null) {
            editingUser = null;
            return;
        }
        editingUser = new User(
                user.getId(),
                user.getFullName(),
                user.getPseudo(),
                user.getEmail(),
                "",
                user.getRole()
        );
    }

    public static boolean isEditMode() {
        return editingUser != null;
    }

    public static User getEditingUser() {
        return editingUser;
    }

    public static void clear() {
        editingUser = null;
    }
}
