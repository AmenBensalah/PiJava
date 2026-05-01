package edu.ProjetPI.tools;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    private static final int BCRYPT_COST = 13;

    private PasswordUtils() {
    }

    public static String hash(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        String bcrypt2a = BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
        return toSymfonyBcryptPrefix(bcrypt2a);
    }

    public static boolean matches(String rawPassword, String hashedPassword) {
        if (rawPassword == null || rawPassword.isBlank() || hashedPassword == null || hashedPassword.isBlank()) {
            return false;
        }

        if (isBcryptHash(hashedPassword)) {
            try {
                return BCrypt.checkpw(rawPassword, toJavaBcryptPrefix(hashedPassword));
            } catch (IllegalArgumentException ignored) {
                return false;
            }
        }

        return false;
    }

    private static boolean isBcryptHash(String hash) {
        return hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$");
    }

    private static String toJavaBcryptPrefix(String hash) {
        if (hash.startsWith("$2y$")) {
            return "$2a$" + hash.substring(4);
        }
        return hash;
    }

    private static String toSymfonyBcryptPrefix(String hash) {
        if (hash.startsWith("$2a$")) {
            return "$2y$" + hash.substring(4);
        }
        return hash;
    }
}
