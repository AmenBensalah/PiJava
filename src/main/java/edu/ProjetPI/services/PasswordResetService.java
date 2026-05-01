package edu.ProjetPI.services;

import edu.ProjetPI.tools.UserValidationRules;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PasswordResetService {

    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private final UserService userService;
    private final BrevoEmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, ResetToken> tokenByEmail = new ConcurrentHashMap<>();

    public PasswordResetService(UserService userService, BrevoEmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    public void sendCode(String email) {
        UserValidationRules.validateEmail(email);
        String normalizedEmail = UserValidationRules.normalizeEmail(email);
        if (userService.findByEmail(normalizedEmail).isEmpty()) {
            throw new IllegalArgumentException("No account found with this email.");
        }

        String code = generateSixDigitCode();
        tokenByEmail.put(normalizedEmail, new ResetToken(code, Instant.now().plus(CODE_TTL), 0));
        emailService.sendPasswordResetCode(normalizedEmail, code);
    }

    public void resetPassword(String email, String code, String newPassword, String confirmPassword) {
        UserValidationRules.validateEmail(email);
        UserValidationRules.validatePasswordForCreate(newPassword);
        if (confirmPassword == null || !confirmPassword.equals(newPassword)) {
            throw new IllegalArgumentException("Password confirmation does not match.");
        }

        String normalizedEmail = UserValidationRules.normalizeEmail(email);
        ResetToken token = tokenByEmail.get(normalizedEmail);
        if (token == null) {
            throw new IllegalArgumentException("No reset code found for this email. Request a new code.");
        }
        if (Instant.now().isAfter(token.expiresAt())) {
            tokenByEmail.remove(normalizedEmail);
            throw new IllegalArgumentException("Reset code expired. Request a new code.");
        }
        if (!token.code().equals(code == null ? "" : code.trim())) {
            int attempts = token.attempts() + 1;
            if (attempts >= MAX_VERIFY_ATTEMPTS) {
                tokenByEmail.remove(normalizedEmail);
                throw new IllegalArgumentException("Too many invalid attempts. Request a new code.");
            }
            tokenByEmail.put(normalizedEmail, new ResetToken(token.code(), token.expiresAt(), attempts));
            throw new IllegalArgumentException("Invalid reset code.");
        }

        userService.updatePasswordByEmail(normalizedEmail, newPassword);
        tokenByEmail.remove(normalizedEmail);
    }

    private String generateSixDigitCode() {
        int value = 100_000 + secureRandom.nextInt(900_000);
        return Integer.toString(value);
    }

    private record ResetToken(String code, Instant expiresAt, int attempts) {
    }
}
