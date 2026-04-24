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

public class LoginController {

    private final UserService userService = new UserService();
    private final FaceIdAuthService faceIdAuthService = new FaceIdAuthService(userService);

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.installResetOnInput(messageLabel, emailField, passwordField);
    }

    @FXML
    public void handleLogin() {
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.clearInvalid(emailField);
        FormFeedback.clearInvalid(passwordField);

        try {
            UserValidationRules.validateEmail(emailField.getText());
        } catch (IllegalArgumentException e) {
            FormFeedback.markInvalid(emailField);
            FormFeedback.showError(messageLabel, e.getMessage());
            return;
        }

        if (passwordField.getText() == null || passwordField.getText().isBlank()) {
            FormFeedback.markInvalid(passwordField);
            FormFeedback.showError(messageLabel, "Le mot de passe est obligatoire.");
            return;
        }

        try {
            Optional<User> user = userService.authenticate(emailField.getText(), passwordField.getText());
            if (user.isEmpty()) {
                FormFeedback.markInvalid(emailField);
                FormFeedback.markInvalid(passwordField);
                FormFeedback.showError(messageLabel, "Invalid email or password.");
                return;
            }

            DashboardSession.setCurrentUser(user.get());
            String role = user.get().getRole();
            if ("ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
                SceneManager.switchScene("/edu/ProjetPI/views/admin-dashboard.fxml", "Admin Dashboard");
            } else {
                SceneManager.switchScene("/edu/ProjetPI/views/user-dashboard.fxml", "Front Office");
            }
        } catch (Exception e) {
            FormFeedback.showError(messageLabel, e.getMessage());
        }
    }

    @FXML
    public void goToRegister() {
        SceneManager.switchScene("/edu/ProjetPI/views/register.fxml", "Create Account");
    }

    @FXML
    public void goToForgotPassword() {
        SceneManager.switchScene("/edu/ProjetPI/views/forgot-password.fxml", "Reset Password");
    }

    @FXML
    public void handleFaceLogin() {
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.clearInvalid(emailField);
        FormFeedback.clearInvalid(passwordField);
        Window owner = messageLabel.getScene() == null ? null : messageLabel.getScene().getWindow();
        try {
            var descriptor = FaceApiCaptureDialog.captureDescriptor(owner);
            if (descriptor.isPresent()) {
                Optional<FaceIdAuthService.FaceMatchResult> match = faceIdAuthService.authenticateByDescriptor(descriptor.get());
                if (match.isEmpty()) {
                    FormFeedback.markInvalid(emailField);
                    FormFeedback.markInvalid(passwordField);
                    FormFeedback.showError(messageLabel, "Face captured, but not recognized.");
                    return;
                }
                User user = match.get().user();
                DashboardSession.setCurrentUser(user);
                String role = user.getRole();
                if ("ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
                    SceneManager.switchScene("/edu/ProjetPI/views/admin-dashboard.fxml", "Admin Dashboard");
                } else {
                    SceneManager.switchScene("/edu/ProjetPI/views/user-dashboard.fxml", "Front Office");
                }
            } else {
                FormFeedback.showError(messageLabel, "Face capture canceled.");
            }
        } catch (Exception e) {
            FormFeedback.showError(messageLabel, "Face capture failed: " + e.getMessage());
        }
    }
}
