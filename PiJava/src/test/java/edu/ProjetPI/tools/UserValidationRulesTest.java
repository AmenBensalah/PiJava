package edu.ProjetPI.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserValidationRulesTest {

    @Test
    void shouldAcceptValidData() {
        assertDoesNotThrow(() -> UserValidationRules.validateFullName("John Doe"));
        assertDoesNotThrow(() -> UserValidationRules.validatePseudo("jdoe"));
        assertDoesNotThrow(() -> UserValidationRules.validateEmail("john.doe@mail.com"));
        assertDoesNotThrow(() -> UserValidationRules.validatePasswordForCreate("secret12"));
    }

    @Test
    void shouldRejectInvalidEmailFormat() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> UserValidationRules.validateEmail("invalid-email")
        );
        assertEquals("Le format de l'email est invalide.", ex.getMessage());
    }

    @Test
    void shouldRejectTooShortPasswordOnCreate() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> UserValidationRules.validatePasswordForCreate("123")
        );
        assertEquals("Le mot de passe doit contenir au moins 6 caracteres.", ex.getMessage());
    }

    @Test
    void shouldNormalizeEmail() {
        assertEquals("admin@mail.com", UserValidationRules.normalizeEmail("  ADMIN@mail.com "));
    }
}
