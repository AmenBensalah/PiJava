package edu.ProjetPI.controllers;

import edu.ProjetPI.entities.User;
import edu.ProjetPI.services.DiscordOAuthService;
import edu.ProjetPI.services.FaceIdAuthService;
import edu.ProjetPI.services.GoogleOAuthService;
import edu.ProjetPI.services.UserService;
import edu.ProjetPI.tools.UserValidationRules;
import edu.PROJETPI.services.OrderSession;
import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
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

            completeLogin(user.get());
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
            completeLogin(user);
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
            completeLogin(user);
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
                completeLogin(user);
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

    private void completeLogin(User user) {
        DashboardSession.setCurrentUser(user);
        OrderSession.getInstance().reloadCartForCurrentUser();
        userService.updateLastLogin(user.getId());
        redirectAfterLogin(user);
    }

    private static void redirectAfterLogin(User user) {
        AppSession.getInstance().login(mapLegacyUser(user));

        if (isAdmin(user)) {
            if (AppNavigator.isReady()) {
                AppNavigator.goToAdmin();
            } else {
                SceneManager.switchScene("/views/admin-layout-view.fxml", "E-SPORTIFY : Admin");
            }
            return;
        }

        if (isManager(user)) {
            if (AppNavigator.isReady()) {
                AppNavigator.goToManager();
            } else {
                SceneManager.switchScene("/views/manager-layout-view.fxml", "E-SPORTIFY : Manager");
            }
            return;
        }

        AppSession.getInstance().setPendingUserHomeSection(AppSession.UserHomeSection.STORE);
        if (AppNavigator.isReady()) {
            AppNavigator.goToUserHome(AppSession.UserHomeSection.STORE);
        } else {
            SceneManager.switchScene("/views/user-layout-view.fxml", "E-SPORTIFY : Boutique");
        }
    }

    private static boolean isAdmin(User user) {
        String email = user.getEmail();
        String role = user.getRole();
        return "admin@admin.com".equalsIgnoreCase(email)
                || "ROLE_ADMIN".equalsIgnoreCase(role)
                || "ADMIN".equalsIgnoreCase(role);
    }

    private static boolean isManager(User user) {
        String role = user.getRole();
        return "ROLE_MANAGER".equalsIgnoreCase(role)
                || "ROLE_ORGANISATEUR".equalsIgnoreCase(role)
                || "MANAGER".equalsIgnoreCase(role);
    }

    private static edu.esportify.entities.User mapLegacyUser(User legacyUser) {
        edu.esportify.entities.User user = new edu.esportify.entities.User();
        user.setId(legacyUser.getId());
        user.setUsername(firstNonBlank(legacyUser.getPseudo(), extractNameFromEmail(legacyUser.getEmail()), "user"));
        user.setFirstName(firstNonBlank(legacyUser.getFullName(), legacyUser.getPseudo(), "User"));
        user.setEmail(legacyUser.getEmail());
        user.setRole(edu.esportify.entities.UserRole.fromValue(legacyUser.getRole()));
        user.setActive(true);
        return user;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return "";
    }

    private static String extractNameFromEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return "";
        }
        return email.substring(0, email.indexOf('@'));
    }
}
