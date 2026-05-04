package edu.ProjetPI.tools;

import java.util.Locale;
import java.util.regex.Pattern;

public final class UserValidationRules {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private UserValidationRules() {
    }

    public static void validateFullName(String fullName) {
        if (isBlank(fullName)) {
            throw new IllegalArgumentException("Le nom complet est obligatoire.");
        }
        if (fullName.trim().length() < 3) {
            throw new IllegalArgumentException("Le nom complet doit contenir au moins 3 caracteres.");
        }
    }

    public static void validatePseudo(String pseudo) {
        if (isBlank(pseudo)) {
            throw new IllegalArgumentException("Le pseudo est obligatoire.");
        }
        if (pseudo.trim().length() < 3) {
            throw new IllegalArgumentException("Le pseudo doit contenir au moins 3 caracteres.");
        }
    }

    public static void validateEmail(String email) {
        if (isBlank(email)) {
            throw new IllegalArgumentException("L'email est obligatoire.");
        }
        String normalized = normalizeEmail(email);
        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Le format de l'email est invalide.");
        }
    }

    public static void validatePasswordForCreate(String password) {
        if (isBlank(password)) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire.");
        }
        if (password.trim().length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caracteres.");
        }
    }

    public static void validatePasswordForUpdate(String password) {
        if (isBlank(password)) {
            return;
        }
        if (password.trim().length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caracteres.");
        }
    }

    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
