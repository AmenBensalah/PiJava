package edu.ProjetPI.controllers;

import edu.ProjetPI.entities.User;
import edu.ProjetPI.services.UserService;
import edu.ProjetPI.tools.UserValidationRules;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ProfileController {

    private final UserService userService = new UserService();

    @FXML
    private Label titleLabel;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField pseudoField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label roleLabel;

    @FXML
    private Label messageLabel;

    @FXML
    public void initialize() {
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.installResetOnInput(messageLabel, fullNameField, pseudoField, emailField, passwordField);

        User currentUser = DashboardSession.getCurrentUser();
        if (currentUser != null) {
            titleLabel.setText("Mon Profil - " + currentUser.getFullName());
            fullNameField.setText(currentUser.getFullName());
            pseudoField.setText(currentUser.getPseudo());
            emailField.setText(currentUser.getEmail());
            roleLabel.setText("Role: " + currentUser.getRole());
        }
    }

    @FXML
    public void handleUpdateProfile() {
        User currentUser = DashboardSession.getCurrentUser();
        if (currentUser == null) {
            FormFeedback.showError(messageLabel, "No connected user found.");
            return;
        }

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
            UserValidationRules.validatePasswordForUpdate(passwordField.getText());
        } catch (IllegalArgumentException e) {
            FormFeedback.markInvalid(passwordField);
            FormFeedback.showError(messageLabel, e.getMessage());
            return;
        }

        try {
            User updatedUser = new User(
                    currentUser.getId(),
                    fullNameField.getText(),
                    pseudoField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    currentUser.getRole()
            );
            userService.update(updatedUser);

            currentUser.setFullName(updatedUser.getFullName());
            currentUser.setPseudo(updatedUser.getPseudo());
            currentUser.setEmail(updatedUser.getEmail().trim().toLowerCase());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
                currentUser.setPassword(updatedUser.getPassword());
            }
            DashboardSession.setCurrentUser(currentUser);

            titleLabel.setText("Mon Profil - " + currentUser.getFullName());
            roleLabel.setText("Role: " + currentUser.getRole());
            passwordField.clear();
            FormFeedback.showSuccess(messageLabel, "Profile updated successfully.");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage() == null ? "" : e.getMessage();
            if (message.toLowerCase().contains("email")) {
                FormFeedback.markInvalid(emailField);
            }
            FormFeedback.showError(messageLabel, message);
        } catch (Exception e) {
            FormFeedback.showError(messageLabel, e.getMessage());
        }
    }

    @FXML
    public void handleDeleteAccount() {
        User currentUser = DashboardSession.getCurrentUser();
        if (currentUser == null) {
            FormFeedback.showError(messageLabel, "No connected user found.");
            return;
        }

        try {
            userService.delete(currentUser.getId());
            DashboardSession.clear();
            SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "Login");
        } catch (Exception e) {
            FormFeedback.showError(messageLabel, e.getMessage());
        }
    }

    @FXML
    public void handleBack() {
        User currentUser = DashboardSession.getCurrentUser();
        if (currentUser != null && "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            SceneManager.switchScene("/edu/ProjetPI/views/admin-dashboard.fxml", "Admin Dashboard");
        } else {
            SceneManager.switchScene("/edu/ProjetPI/views/user-dashboard.fxml", "Front Office");
        }
    }
}
