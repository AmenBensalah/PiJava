package edu.ProjetPI.controllers;

import edu.ProjetPI.services.BrevoEmailService;
import edu.ProjetPI.services.PasswordResetService;
import edu.ProjetPI.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ForgotPasswordController {

    private final PasswordResetService passwordResetService =
            new PasswordResetService(new UserService(), new BrevoEmailService());

    @FXML
    private TextField emailField;

    @FXML
    private TextField codeField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.installResetOnInput(messageLabel, emailField, codeField, newPasswordField, confirmPasswordField);
    }

    @FXML
    public void handleSendCode() {
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.clearInvalid(emailField);
        try {
            passwordResetService.sendCode(emailField.getText());
            FormFeedback.showSuccess(messageLabel, "Reset code sent by email. Check your inbox.");
        } catch (IllegalArgumentException e) {
            FormFeedback.markInvalid(emailField);
            FormFeedback.showError(messageLabel, e.getMessage());
        } catch (Exception e) {
            FormFeedback.showError(messageLabel, e.getMessage());
        }
    }

    @FXML
    public void handleResetPassword() {
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.clearInvalid(emailField);
        FormFeedback.clearInvalid(codeField);
        FormFeedback.clearInvalid(newPasswordField);
        FormFeedback.clearInvalid(confirmPasswordField);
        try {
            passwordResetService.resetPassword(
                    emailField.getText(),
                    codeField.getText(),
                    newPasswordField.getText(),
                    confirmPasswordField.getText()
            );
            FormFeedback.showSuccess(messageLabel, "Password updated successfully. Redirecting to login...");
            SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "Login");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (message.contains("email")) {
                FormFeedback.markInvalid(emailField);
            } else if (message.contains("code")) {
                FormFeedback.markInvalid(codeField);
            } else if (message.contains("password")) {
                FormFeedback.markInvalid(newPasswordField);
                FormFeedback.markInvalid(confirmPasswordField);
            }
            FormFeedback.showError(messageLabel, e.getMessage());
        } catch (Exception e) {
            FormFeedback.showError(messageLabel, e.getMessage());
        }
    }

    @FXML
    public void goToLogin() {
        SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "Login");
    }
}
