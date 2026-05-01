package edu.ProjetPI.controllers;

import edu.ProjetPI.entities.User;
import edu.ProjetPI.services.DiscordOAuthService;
import edu.ProjetPI.services.FaceIdAuthService;
import edu.ProjetPI.services.GoogleOAuthService;
import edu.ProjetPI.services.UserService;
import edu.ProjetPI.tools.UserValidationRules;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LoginController {

    private final UserService userService = new UserService();
    private final FaceIdAuthService faceIdAuthService = new FaceIdAuthService(userService);
    private final GoogleOAuthService googleOAuthService = new GoogleOAuthService();
    private final DiscordOAuthService discordOAuthService = new DiscordOAuthService();

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

            userService.checkAndClearBanStatus(user.get());

            DashboardSession.setCurrentUser(user.get());
            userService.updateLastLogin(user.get().getId());
            String role = user.get().getRole();
            String email = user.get().getEmail();

            // Redirect based on email (as requested) or role
            if ("admin@admin.com".equalsIgnoreCase(email) || "ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
                SceneManager.switchScene("/backListProduit.fxml", "Boutique Admin Dashboard");
            } else {
                SceneManager.switchScene("/ajoutProduit.fxml", "E-SPORTIFY : Boutique");
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
    public void handleGoogleLogin() {
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.showSuccess(messageLabel, "Opening Google login in browser...");

        CompletableFuture.supplyAsync(() -> {
            GoogleOAuthService.GoogleProfile profile = googleOAuthService.authenticate();
            User user = userService.findOrCreateGoogleUser(profile.email(), profile.name());
            userService.checkAndClearBanStatus(user);
            return user;
        }).whenComplete((user, error) -> Platform.runLater(() -> {
            if (error != null) {
                FormFeedback.showError(messageLabel, unwrapMessage(error));
                return;
            }
            DashboardSession.setCurrentUser(user);
            userService.updateLastLogin(user.getId());
            String role = user.getRole();
            String email = user.getEmail();

            if ("admin@admin.com".equalsIgnoreCase(email) || "ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
                SceneManager.switchScene("/backListProduit.fxml", "Boutique Admin Dashboard");
            } else {
                SceneManager.switchScene("/ajoutProduit.fxml", "E-SPORTIFY : Boutique");
            }
        }));
    }

    @FXML
    public void handleDiscordLogin() {
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.showSuccess(messageLabel, "Opening Discord login in browser...");

        CompletableFuture.supplyAsync(() -> {
            DiscordOAuthService.DiscordProfile profile = discordOAuthService.authenticate();
            User user = userService.findOrCreateDiscordUser(profile.email(), profile.displayName());
            userService.checkAndClearBanStatus(user);
            return user;
        }).whenComplete((user, error) -> Platform.runLater(() -> {
            if (error != null) {
                FormFeedback.showError(messageLabel, unwrapMessage(error));
                return;
            }
            DashboardSession.setCurrentUser(user);
            userService.updateLastLogin(user.getId());
            String role = user.getRole();
            String email = user.getEmail();

            if ("admin@admin.com".equalsIgnoreCase(email) || "ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
                SceneManager.switchScene("/backListProduit.fxml", "Boutique Admin Dashboard");
            } else {
                SceneManager.switchScene("/ajoutProduit.fxml", "E-SPORTIFY : Boutique");
            }
        }));
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
                userService.checkAndClearBanStatus(user);
                DashboardSession.setCurrentUser(user);
                userService.updateLastLogin(user.getId());
                String role = user.getRole();
                String email = user.getEmail();

                if ("admin@admin.com".equalsIgnoreCase(email) || "ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
                    SceneManager.switchScene("/backListProduit.fxml", "Boutique Admin Dashboard");
                } else {
                    SceneManager.switchScene("/ajoutProduit.fxml", "E-SPORTIFY : Boutique");
                }
            } else {
                FormFeedback.showError(messageLabel, "Face capture canceled.");
            }
        } catch (Exception e) {
            FormFeedback.showError(messageLabel, "Face capture failed: " + e.getMessage());
        }
    }

    private static String unwrapMessage(Throwable error) {
        Throwable cursor = error;
        while (cursor.getCause() != null) {
            cursor = cursor.getCause();
        }
        String msg = cursor.getMessage();
        return msg == null || msg.isBlank() ? "Social login failed." : msg;
    }
}
