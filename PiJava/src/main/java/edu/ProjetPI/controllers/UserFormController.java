package edu.ProjetPI.controllers;

import edu.ProjetPI.entities.User;
import edu.ProjetPI.services.UserService;
import edu.ProjetPI.tools.UserValidationRules;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class UserFormController {

    private final UserService userService = new UserService();

    @FXML
    private Label formTitleLabel;

    @FXML
    private Label formSubtitleLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField pseudoField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleBox;

    @FXML
    private Label passwordHintLabel;

    @FXML
    private Button saveButton;

    @FXML
    public void initialize() {
        roleBox.setItems(FXCollections.observableArrayList("ROLE_ADMIN", "ROLE_JOUEUR", "ROLE_MANAGER"));
        roleBox.setValue("ROLE_JOUEUR");

        if (UserFormSession.isEditMode()) {
            User user = UserFormSession.getEditingUser();
            formTitleLabel.setText("Modifier compte");
            formSubtitleLabel.setText("Mettre a jour les informations utilisateur");
            saveButton.setText("Enregistrer les modifications");
            passwordField.setPromptText("Laisser vide pour conserver");
            passwordHintLabel.setText("Mot de passe optionnel en mode modification");

            fullNameField.setText(user.getFullName());
            pseudoField.setText(user.getPseudo());
            emailField.setText(user.getEmail());
            roleBox.setValue(user.getRole());
        }
    }

    @FXML
    public void handleSave() {
        try {
            boolean editMode = UserFormSession.isEditMode();
            UserValidationRules.validateFullName(fullNameField.getText());
            UserValidationRules.validatePseudo(pseudoField.getText());
            UserValidationRules.validateEmail(emailField.getText());
            if (!editMode) {
                UserValidationRules.validatePasswordForCreate(passwordField.getText());
            } else {
                UserValidationRules.validatePasswordForUpdate(passwordField.getText());
            }
            if (roleBox.getValue() == null) {
                messageLabel.setText("Le role est obligatoire.");
                return;
            }

            User user = new User(
                    editMode ? UserFormSession.getEditingUser().getId() : 0,
                    fullNameField.getText().trim(),
                    pseudoField.getText().trim(),
                    UserValidationRules.normalizeEmail(emailField.getText()),
                    passwordField.getText(),
                    roleBox.getValue()
            );

            if (editMode) {
                userService.update(user);
            } else {
                userService.add(user);
            }

            UserFormSession.clear();
            SceneManager.switchScene("/edu/ProjetPI/views/admin-dashboard.fxml", "Admin Dashboard");
        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        UserFormSession.clear();
        SceneManager.switchScene("/edu/ProjetPI/views/admin-dashboard.fxml", "Admin Dashboard");
    }
}
