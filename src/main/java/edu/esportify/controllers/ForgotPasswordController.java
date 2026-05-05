package edu.esportify.controllers;

import edu.ProjetPI.services.BrevoEmailService;
import edu.ProjetPI.services.PasswordResetService;
import edu.ProjetPI.services.UserService;
import edu.esportify.navigation.AppNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ForgotPasswordController {
    private final PasswordResetService passwordResetService =
            new PasswordResetService(new UserService(), new BrevoEmailService());

    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label infoLabel;

    @FXML
    private void onSendCode() {
        try {
            passwordResetService.sendCode(emailField.getText());
            infoLabel.setText("Code envoye par email.");
        } catch (Exception e) {
            infoLabel.setText(messageOf(e));
        }
    }

    @FXML
    private void onResetPassword() {
        try {
            passwordResetService.resetPassword(
                    emailField.getText(),
                    codeField.getText(),
                    newPasswordField.getText(),
                    confirmPasswordField.getText()
            );
            infoLabel.setText("Mot de passe modifie. Retour au login...");
            AppNavigator.goToLogin();
        } catch (Exception e) {
            infoLabel.setText(messageOf(e));
        }
    }

    @FXML
    private void onBackToLogin() {
        AppNavigator.goToLogin();
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
