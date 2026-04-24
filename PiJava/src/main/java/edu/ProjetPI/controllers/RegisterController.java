package edu.ProjetPI.controllers;

import edu.ProjetPI.entities.User;
import edu.ProjetPI.services.FaceIdAuthService;
import edu.ProjetPI.services.UserService;
import edu.ProjetPI.tools.UserValidationRules;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import java.util.Optional;

public class RegisterController {

    private final UserService userService = new UserService();
    private final FaceIdAuthService faceIdAuthService = new FaceIdAuthService(userService);
    private String pendingFaceDescriptorJson;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField pseudoField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.installResetOnInput(messageLabel, fullNameField, pseudoField, emailField, passwordField);
    }

    @FXML
    public void handleRegister() {
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.clearInvalid(fullNameField);
        FormFeedback.clearInvalid(pseudoField);
        FormFeedback.clearInvalid(emailField);
        FormFeedback.clearInvalid(passwordField);

        try {
            UserValidationRules.validateFullName(fullNameField.getText());
        } catch (IllegalArgumentException e) {
            FormFeedback.markInvalid(fullNameField);
            FormFeedback.showError(messageLabel, e.getMessage());
            return;
        }

        try {
            UserValidationRules.validatePseudo(pseudoField.getText());
        } catch (IllegalArgumentException e) {
            FormFeedback.markInvalid(pseudoField);
            FormFeedback.showError(messageLabel, e.getMessage());
            return;
        }

        try {
            UserValidationRules.validateEmail(emailField.getText());
        } catch (IllegalArgumentException e) {
            FormFeedback.markInvalid(emailField);
            FormFeedback.showError(messageLabel, e.getMessage());
            return;
        }

        try {
            UserValidationRules.validatePasswordForCreate(passwordField.getText());
        } catch (IllegalArgumentException e) {
            FormFeedback.markInvalid(passwordField);
            FormFeedback.showError(messageLabel, e.getMessage());
            return;
        }

        try {
            String normalizedEmail = UserValidationRules.normalizeEmail(emailField.getText());
            String rawPassword = passwordField.getText();
            userService.add(new User(
                    fullNameField.getText().trim(),
                    pseudoField.getText().trim(),
                    normalizedEmail,
                    rawPassword,
                    "ROLE_JOUEUR",
                    pendingFaceDescriptorJson
            ));
            Optional<User> createdUser = userService.authenticate(normalizedEmail, rawPassword);
            if (createdUser.isPresent()) {
                DashboardSession.setCurrentUser(createdUser.get());
                clearFields();
                SceneManager.switchScene("/edu/ProjetPI/views/user-dashboard.fxml", "Front Office");
                return;
            }
            FormFeedback.showSuccess(messageLabel, "Compte cree avec succes. Connectez-vous pour continuer.");
            clearFields();
        } catch (Exception e) {
            FormFeedback.showError(messageLabel, e.getMessage());
        }
    }

    @FXML
    public void goToLogin() {
        SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "Login");
    }

    @FXML
    public void handleCaptureFaceId() {
        FormFeedback.clearMessage(messageLabel);
        Window owner = messageLabel.getScene() == null ? null : messageLabel.getScene().getWindow();
        try {
            var descriptor = FaceApiCaptureDialog.captureDescriptor(owner);
            if (descriptor.isPresent()) {
                double[] normalized = faceIdAuthService.normalizeAndValidate(descriptor.get());
                pendingFaceDescriptorJson = faceIdAuthService.toJson(normalized);
                FormFeedback.showSuccess(messageLabel, "Face ID captured and attached to this registration.");
            } else {
                FormFeedback.showError(messageLabel, "Face capture canceled.");
            }
        } catch (Exception e) {
            FormFeedback.showError(messageLabel, "Face capture failed: " + e.getMessage());
        }
    }

    private void clearFields() {
        fullNameField.clear();
        pseudoField.clear();
        emailField.clear();
        passwordField.clear();
        pendingFaceDescriptorJson = null;
    }
}
