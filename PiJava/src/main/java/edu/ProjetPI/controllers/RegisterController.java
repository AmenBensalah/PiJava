package edu.ProjetPI.controllers;

import edu.ProjetPI.entities.User;
import edu.ProjetPI.services.UserService;
import edu.ProjetPI.tools.UserValidationRules;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {

    private final UserService userService = new UserService();

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
    public void handleRegister() {
        try {
            UserValidationRules.validateFullName(fullNameField.getText());
            UserValidationRules.validatePseudo(pseudoField.getText());
            UserValidationRules.validateEmail(emailField.getText());
            UserValidationRules.validatePasswordForCreate(passwordField.getText());

            userService.add(new User(
                    fullNameField.getText().trim(),
                    pseudoField.getText().trim(),
                    UserValidationRules.normalizeEmail(emailField.getText()),
                    passwordField.getText(),
                    "ROLE_JOUEUR"
            ));
            messageLabel.setText("Compte cree avec succes. Vous pouvez vous connecter.");
            clearFields();
        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void goToLogin() {
        SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "Login");
    }

    private void clearFields() {
        fullNameField.clear();
        pseudoField.clear();
        emailField.clear();
        passwordField.clear();
    }
}
