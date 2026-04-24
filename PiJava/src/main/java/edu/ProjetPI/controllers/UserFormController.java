package edu.ProjetPI.controllers;

import edu.ProjetPI.entities.User;
import edu.ProjetPI.services.FaceIdAuthService;
import edu.ProjetPI.services.UserService;
import edu.ProjetPI.tools.UserValidationRules;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Window;

public class UserFormController {

    private final UserService userService = new UserService();
    private final FaceIdAuthService faceIdAuthService = new FaceIdAuthService(userService);
    private String pendingFaceDescriptorJson;

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
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.installResetOnInput(messageLabel, fullNameField, pseudoField, emailField, passwordField);
        FormFeedback.installResetOnSelection(messageLabel, roleBox);

        if (UserFormSession.isEditMode()) {
            User user = UserFormSession.getEditingUser();
            pendingFaceDescriptorJson = user.getFaceDescriptorJson();
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
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.clearInvalid(fullNameField);
        FormFeedback.clearInvalid(pseudoField);
        FormFeedback.clearInvalid(emailField);
        FormFeedback.clearInvalid(passwordField);
        FormFeedback.clearInvalid(roleBox);

        boolean editMode = UserFormSession.isEditMode();

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
            if (!editMode) {
                UserValidationRules.validatePasswordForCreate(passwordField.getText());
            } else {
                UserValidationRules.validatePasswordForUpdate(passwordField.getText());
            }
        } catch (IllegalArgumentException e) {
            FormFeedback.markInvalid(passwordField);
            FormFeedback.showError(messageLabel, e.getMessage());
            return;
        }

        if (roleBox.getValue() == null) {
            FormFeedback.markInvalid(roleBox);
            FormFeedback.showError(messageLabel, "Le role est obligatoire.");
            return;
        }

        try {
            User user = new User(
                    editMode ? UserFormSession.getEditingUser().getId() : 0,
                    fullNameField.getText().trim(),
                    pseudoField.getText().trim(),
                    UserValidationRules.normalizeEmail(emailField.getText()),
                    passwordField.getText(),
                    roleBox.getValue(),
                    pendingFaceDescriptorJson
            );

            if (editMode) {
                userService.update(user);
            } else {
                userService.add(user);
            }

            UserFormSession.clear();
            SceneManager.switchScene("/edu/ProjetPI/views/admin-dashboard.fxml", "Admin Dashboard");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage() == null ? "" : e.getMessage();
            if (message.toLowerCase().contains("email")) {
                FormFeedback.markInvalid(emailField);
            }
            if (message.toLowerCase().contains("role")) {
                FormFeedback.markInvalid(roleBox);
            }
            FormFeedback.showError(messageLabel, message);
        } catch (Exception e) {
            FormFeedback.showError(messageLabel, e.getMessage());
        }
    }

    @FXML
    public void handleCancel() {
        UserFormSession.clear();
        SceneManager.switchScene("/edu/ProjetPI/views/admin-dashboard.fxml", "Admin Dashboard");
    }

    @FXML
    public void handleCaptureFaceId() {
        FormFeedback.clearMessage(messageLabel);
        Window owner = messageLabel.getScene() == null ? null : messageLabel.getScene().getWindow();
        try {
            var descriptor = FaceApiCaptureDialog.captureDescriptor(owner);
            if (descriptor.isPresent()) {
                double[] normalized = faceIdAuthService.normalizeAndValidate(descriptor.get());
                pendingFaceDescriptorJson = faceIdAuthService.toJson(normalized);
                FormFeedback.showSuccess(messageLabel, "Face ID captured and will be saved with this account.");
            } else {
                FormFeedback.showError(messageLabel, "Face capture canceled.");
            }
        } catch (Exception e) {
            FormFeedback.showError(messageLabel, "Face capture failed: " + e.getMessage());
        }
    }
}
