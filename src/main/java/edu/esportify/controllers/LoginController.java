package edu.esportify.controllers;

import edu.ProjetPI.controllers.FaceApiCaptureDialog;
import edu.ProjetPI.controllers.SceneManager;
import edu.ProjetPI.controllers.DashboardSession;
import edu.ProjetPI.services.DiscordOAuthService;
import edu.ProjetPI.services.FaceIdAuthService;
import edu.ProjetPI.services.GoogleOAuthService;
import edu.esportify.entities.User;
import edu.esportify.entities.UserRole;
import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.AuthService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Window;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LoginController {
    private final AuthService authService = new AuthService();
    private final edu.ProjetPI.services.UserService legacyUserService = new edu.ProjetPI.services.UserService();
    private final FaceIdAuthService faceIdAuthService = new FaceIdAuthService(legacyUserService);
    private final GoogleOAuthService googleOAuthService = new GoogleOAuthService();
    private final DiscordOAuthService discordOAuthService = new DiscordOAuthService();

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label infoLabel;

    @FXML
    private void initialize() {
        usernameField.setText("user");
        passwordField.setText("user123");
        infoLabel.setText("Connectez-vous ou utilisez Face ID.");
    }

    @FXML
    private void onLogin() {
        String login = value(usernameField.getText());
        String password = value(passwordField.getText());
        if (login.isBlank() || password.isBlank()) {
            infoLabel.setText("Entrez votre email/username et votre mot de passe.");
            return;
        }

        try {
            User user = authService.authenticate(login, password);
            if (user == null) {
                Optional<edu.ProjetPI.entities.User> legacyUser = legacyUserService.authenticate(login, password);
                if (legacyUser.isPresent()) {
                    completeLogin(mapLegacyUser(legacyUser.get()));
                    return;
                }
            }
            if (user == null) {
                infoLabel.setText("Identifiants invalides.");
                return;
            }
            completeLogin(user);
        } catch (Exception e) {
            infoLabel.setText("Connexion impossible: " + cleanMessage(e));
        }
    }

    @FXML
    private void onFaceId() {
        Optional<String> availabilityIssue = FaceApiCaptureDialog.validateFaceIdAvailability();
        if (availabilityIssue.isPresent()) {
            infoLabel.setText(availabilityIssue.get());
            return;
        }
        infoLabel.setText("Capture Face ID en cours...");
        Window owner = infoLabel.getScene() == null ? null : infoLabel.getScene().getWindow();
        try {
            Optional<List<Double>> descriptor = FaceApiCaptureDialog.captureDescriptor(owner);
            if (descriptor.isEmpty()) {
                infoLabel.setText("Capture Face ID annulee.");
                return;
            }

            Optional<FaceIdAuthService.FaceMatchResult> match = faceIdAuthService.authenticateByDescriptor(descriptor.get());
            if (match.isEmpty()) {
                infoLabel.setText("Visage non reconnu.");
                return;
            }

            completeLogin(mapLegacyUser(match.get().user()));
        } catch (Exception e) {
            infoLabel.setText("Face ID indisponible: " + cleanMessage(e));
        }
    }

    @FXML
    private void useManagerDemo() {
        usernameField.setText("manager");
        passwordField.setText("manager123");
        infoLabel.setText("Connexion manager demo");
    }

    @FXML
    private void useUserDemo() {
        usernameField.setText("user");
        passwordField.setText("user123");
        infoLabel.setText("Connexion user demo");
    }

    @FXML
    private void useAdminDemo() {
        usernameField.setText("admin");
        passwordField.setText("admin123");
        infoLabel.setText("Connexion admin demo");
    }

    @FXML
    private void onCreateAccount() {
        AppNavigator.goToRegister();
    }

    @FXML
    private void onForgotPassword() {
        AppNavigator.goToForgotPassword();
    }

    @FXML
    private void onGoogle() {
        infoLabel.setText("Ouverture de Google...");
        CompletableFuture
                .supplyAsync(() -> {
                    GoogleOAuthService.GoogleProfile profile = googleOAuthService.authenticate();
                    edu.ProjetPI.entities.User legacyUser =
                            legacyUserService.findOrCreateGoogleUser(profile.email(), profile.name());
                    return mapLegacyUser(legacyUser);
                })
                .whenComplete((user, error) -> Platform.runLater(() -> {
                    if (error != null) {
                        infoLabel.setText("Google indisponible: " + cleanMessage(error));
                        return;
                    }
                    completeLogin(user);
                }));
    }

    @FXML
    private void onDiscord() {
        infoLabel.setText("Ouverture de Discord...");
        CompletableFuture
                .supplyAsync(() -> {
                    DiscordOAuthService.DiscordProfile profile = discordOAuthService.authenticate();
                    edu.ProjetPI.entities.User legacyUser =
                            legacyUserService.findOrCreateDiscordUser(profile.email(), profile.displayName());
                    return mapLegacyUser(legacyUser);
                })
                .whenComplete((user, error) -> Platform.runLater(() -> {
                    if (error != null) {
                        infoLabel.setText("Discord indisponible: " + cleanMessage(error));
                        return;
                    }
                    completeLogin(user);
                }));
    }

    private void completeLogin(User user) {
        AppSession.getInstance().login(user);
        DashboardSession.setCurrentUser(toLegacyDashboardUser(user));
        if (user.getRole() == UserRole.ADMIN) {
            AppNavigator.goToAdmin();
            return;
        }
        if (user.getRole() == UserRole.MANAGER) {
            AppNavigator.goToManager();
            return;
        }
        AppNavigator.goToUserHome();
    }

    private User mapLegacyUser(edu.ProjetPI.entities.User legacyUser) {
        User user = new User();
        user.setId(legacyUser.getId());
        user.setUsername(firstNonBlank(legacyUser.getPseudo(), extractNameFromEmail(legacyUser.getEmail()), "user"));
        user.setFirstName(firstNonBlank(legacyUser.getFullName(), legacyUser.getPseudo(), "User"));
        user.setEmail(legacyUser.getEmail());
        user.setRole(UserRole.fromValue(legacyUser.getRole()));
        user.setActive(true);
        return user;
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

    private String cleanMessage(Throwable error) {
        Throwable cursor = error;
        while (cursor.getCause() != null) {
            cursor = cursor.getCause();
        }
        String message = cursor.getMessage();
        return message == null || message.isBlank() ? "Erreur inconnue." : message;
    }

    private edu.ProjetPI.entities.User toLegacyDashboardUser(User user) {
        edu.ProjetPI.entities.User legacyUser = new edu.ProjetPI.entities.User();
        legacyUser.setId(user.getId());
        legacyUser.setFullName(firstNonBlank(user.getFirstName(), user.getUsername(), "User"));
        legacyUser.setPseudo(firstNonBlank(user.getUsername(), "user"));
        legacyUser.setEmail(user.getEmail());
        legacyUser.setRole(toLegacyRole(user.getRole()));
        return legacyUser;
    }

    private String toLegacyRole(UserRole role) {
        if (role == null) {
            return "ROLE_JOUEUR";
        }
        return switch (role) {
            case ADMIN -> "ROLE_ADMIN";
            case MANAGER -> "ROLE_MANAGER";
            case USER -> "ROLE_JOUEUR";
        };
    }
}
