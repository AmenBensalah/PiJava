package edu.esportify.controllers;

import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label infoLabel;

    @FXML
    private void initialize() {
        usernameField.setText("manager");
        passwordField.setText("manager123");
        infoLabel.setText("Connexion manager demo");
    }

    @FXML
    private void onLogin() {
        String user = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String pass = passwordField.getText() == null ? "" : passwordField.getText().trim();

        try {
            if ("manager".equalsIgnoreCase(user) && "manager123".equals(pass)) {
                AppSession.getInstance().login("manager", "Manager");
                AppNavigator.goToManager();
                return;
            }
            if ("user".equalsIgnoreCase(user) && "user123".equals(pass)) {
                AppSession.getInstance().login("user", "User");
                AppNavigator.goToUserHome();
                return;
            }
            if ("admin".equalsIgnoreCase(user) && "admin123".equals(pass)) {
                AppSession.getInstance().login("admin", "Admin");
                AppNavigator.goToAdmin();
                return;
            }
        } catch (RuntimeException e) {
            infoLabel.setText("Erreur ouverture interface admin.");
            System.out.println("Erreur navigation login: " + e.getMessage());
            return;
        }
        infoLabel.setText("Identifiants invalides.");
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
        infoLabel.setText("Creation de compte indisponible en mode demo.");
    }

    @FXML
    private void onForgotPassword() {
        infoLabel.setText("Recuperation du mot de passe indisponible.");
    }

    @FXML
    private void onGoogle() {
        infoLabel.setText("Connexion Google indisponible.");
    }

    @FXML
    private void onDiscord() {
        infoLabel.setText("Connexion Discord indisponible.");
    }
}
