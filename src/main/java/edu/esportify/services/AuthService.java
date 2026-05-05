package edu.esportify.services;

import edu.esportify.entities.User;
import edu.esportify.entities.UserRole;

public class AuthService {
    private final UserService userService = new UserService();

    public User authenticate(String login, String password) {
        User user = userService.findByUsernameOrEmail(login);
        if (user == null || !user.isActive()) {
            return null;
        }
        return user.getPassword() != null && user.getPassword().equals(password) ? user : null;
    }

    public User register(String firstName, String email, String phoneNumber, String password) {
        validateRegistration(firstName, email, phoneNumber, password);
        User user = new User();
        user.setUsername(userService.generateUniqueUsername(extractUsernameSeed(firstName, email)));
        user.setFirstName(firstName.trim());
        user.setEmail(email.trim());
        user.setPhoneNumber(phoneNumber.trim());
        user.setPassword(password);
        user.setRole(UserRole.USER);
        userService.addEntity(user);
        return user;
    }

    private void validateRegistration(String firstName, String email, String phoneNumber, String password) {
        if (firstName == null || firstName.isBlank() || firstName.trim().length() < 2) {
            throw new IllegalArgumentException("Le prenom doit contenir au moins 2 caracteres.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("L'email est invalide.");
        }
        if (phoneNumber == null || phoneNumber.isBlank() || !phoneNumber.matches("[0-9+ ]{8,20}")) {
            throw new IllegalArgumentException("Le numero de telephone est invalide.");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caracteres.");
        }
        if (userService.emailExists(email)) {
            throw new IllegalArgumentException("Cet email existe deja.");
        }
    }

    private String extractUsernameSeed(String firstName, String email) {
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf('@'));
        }
        return firstName == null ? "" : firstName;
    }
}
