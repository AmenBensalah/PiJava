package edu.esportify.controllers;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class MainController {
    private TextField usernameField;
    private PasswordField passwordField;
    private Label infoLabel;

    public void setFields(TextField usernameField, PasswordField passwordField, Label infoLabel) {
        this.usernameField = usernameField;
        this.passwordField = passwordField;
        this.infoLabel = infoLabel;
    }

    public void onLogin() {
        if (usernameField == null || passwordField == null || infoLabel == null) {
            return;
        }

        String user = usernameField.getText() == null ? "" : usernameField.getText().trim();
        String pass = passwordField.getText() == null ? "" : passwordField.getText().trim();

        if ("manager".equalsIgnoreCase(user) && "manager123".equals(pass)) {
            infoLabel.setText("Connexion manager reussie.");
            return;
        }
        if ("user".equalsIgnoreCase(user) && "user123".equals(pass)) {
            infoLabel.setText("Connexion user reussie.");
            return;
        }
        infoLabel.setText("Identifiants invalides.");
    }
}
