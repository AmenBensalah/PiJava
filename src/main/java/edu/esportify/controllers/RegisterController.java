package edu.esportify.controllers;

import edu.ProjetPI.controllers.FaceApiCaptureDialog;
import edu.ProjetPI.services.FaceIdAuthService;
import edu.ProjetPI.tools.UserValidationRules;
import edu.esportify.entities.UserRole;
import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;

public class RegisterController {
    private final edu.ProjetPI.services.UserService legacyUserService = new edu.ProjetPI.services.UserService();
    private final FaceIdAuthService faceIdAuthService = new FaceIdAuthService(legacyUserService);
    private String pendingFaceDescriptorJson;

    @FXML private TextField fullNameField;
    @FXML private TextField pseudoField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label infoLabel;

    @FXML
    private void initialize() {
        infoLabel.setText("Creez votre compte user et ajoutez Face ID si vous voulez.");
    }

    @FXML
    private void onRegister() {
        try {
            UserValidationRules.validateFullName(fullNameField.getText());
            UserValidationRules.validatePseudo(pseudoField.getText());
            UserValidationRules.validateEmail(emailField.getText());
            UserValidationRules.validatePasswordForCreate(passwordField.getText());

            String normalizedEmail = UserValidationRules.normalizeEmail(emailField.getText());
            String rawPassword = passwordField.getText();

            legacyUserService.add(new edu.ProjetPI.entities.User(
                    value(fullNameField.getText()),
                    value(pseudoField.getText()),
                    normalizedEmail,
                    rawPassword,
                    "ROLE_JOUEUR",
                    pendingFaceDescriptorJson
            ));

            Optional<edu.ProjetPI.entities.User> created = legacyUserService.authenticate(normalizedEmail, rawPassword);
            if (created.isEmpty()) {
                infoLabel.setText("Compte cree. Connectez-vous maintenant.");
                clearFields();
                return;
            }

            AppSession.getInstance().login(mapLegacyUser(created.get()));
            AppNavigator.goToUserHome();
        } catch (Exception e) {
            infoLabel.setText(messageOf(e));
        }
    }

    @FXML
    private void onFaceId() {
        Optional<String> availabilityIssue = FaceApiCaptureDialog.validateFaceIdAvailability();
        if (availabilityIssue.isPresent()) {
            infoLabel.setText(availabilityIssue.get());
            return;
        }
        Window owner = infoLabel.getScene() == null ? null : infoLabel.getScene().getWindow();
        try {
            Optional<List<Double>> descriptor = FaceApiCaptureDialog.captureDescriptor(owner);
            if (descriptor.isEmpty()) {
                infoLabel.setText("Capture Face ID annulee.");
                return;
            }
            double[] normalized = faceIdAuthService.normalizeAndValidate(descriptor.get());
            pendingFaceDescriptorJson = faceIdAuthService.toJson(normalized);
            infoLabel.setText("Face ID capture et ajoute au compte.");
        } catch (Exception e) {
            infoLabel.setText("Face ID indisponible: " + messageOf(e));
        }
    }

    @FXML
    private void onBackToLogin() {
        AppNavigator.goToLogin();
    }

    private edu.esportify.entities.User mapLegacyUser(edu.ProjetPI.entities.User legacyUser) {
        edu.esportify.entities.User user = new edu.esportify.entities.User();
        user.setId(legacyUser.getId());
        user.setUsername(firstNonBlank(legacyUser.getPseudo(), extractNameFromEmail(legacyUser.getEmail()), "user"));
        user.setFirstName(firstNonBlank(legacyUser.getFullName(), legacyUser.getPseudo(), "User"));
        user.setEmail(legacyUser.getEmail());
        user.setRole(UserRole.fromValue(legacyUser.getRole()));
        user.setActive(true);
        return user;
    }

    private void clearFields() {
        fullNameField.clear();
        pseudoField.clear();
        emailField.clear();
        passwordField.clear();
        pendingFaceDescriptorJson = null;
    }

    private String value(String text) {
        return text == null ? "" : text.trim();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private String extractNameFromEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return "";
        }
        return email.substring(0, email.indexOf('@'));
    }

    private String messageOf(Throwable error) {
        Throwable cursor = error;
        while (cursor.getCause() != null) {
            cursor = cursor.getCause();
        }
        String message = cursor.getMessage();
        return message == null || message.isBlank() ? "Erreur inconnue." : message;
    }
}
