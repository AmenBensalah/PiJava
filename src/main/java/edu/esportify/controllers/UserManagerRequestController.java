package edu.esportify.controllers;

import edu.esportify.entities.ManagerRequest;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.ManagerRequestService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class UserManagerRequestController implements UserContentController {
    private static final String INVALID_STYLE_CLASS = "validation-error";
    private static final int LONG_TEXT_MIN = 10;

    private static final String[] LEVELS = {
            "Amateur",
            "Medium",
            "Pro",
            "Legendaire",
            "Heroique"
    };

    private final ManagerRequestService managerRequestService = new ManagerRequestService();

    private UserLayoutController parentController;

    @FXML private VBox formShell;
    @FXML private TextField usernameField;
    @FXML private ComboBox<String> levelBox;
    @FXML private TextArea motivationArea;
    @FXML private Label infoLabel;
    @FXML private Button backButton;
    @FXML private Button submitButton;

    @FXML
    private void initialize() {
        ensureStyleClass(formShell, "coord-card", "form-shell", "admin-premium-form-shell");
        ensureStyleClass(usernameField, "field", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(levelBox, "dark-combo", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(motivationArea, "text-area", "admin-editor-area");
        ensureStyleClass(backButton, "manager-outline-button");
        ensureStyleClass(submitButton, "manager-action-button");

        levelBox.getItems().setAll(LEVELS);
        levelBox.setValue("Amateur");
    }

    @Override
    public void init(UserLayoutController parentController) {
        this.parentController = parentController;
        usernameField.setText(value(AppSession.getInstance().getUsername()));
    }

    @FXML
    private void onBack() {
        parentController.showTeams();
    }

    @FXML
    private void onSubmit() {
        clearValidationState();
        String username = value(usernameField.getText()).trim();
        String niveau = value(levelBox.getValue()).trim();
        String motivation = value(motivationArea.getText()).trim();

        if (username.isBlank()) {
            showValidationError("Le nom du joueur est obligatoire.", usernameField);
            return;
        }
        if (niveau.isBlank()) {
            showValidationError("Le niveau est obligatoire.", levelBox);
            return;
        }
        if (motivation.isBlank()) {
            showValidationError("La motivation est obligatoire.", motivationArea);
            return;
        }
        if (motivation.length() <= LONG_TEXT_MIN) {
            showValidationError("La motivation doit contenir plus de 10 caracteres.", motivationArea);
            return;
        }
        if (managerRequestService.hasActiveRequestForUser(username, null)) {
            showValidationError("Une demande manager active existe deja pour cet utilisateur.", usernameField);
            return;
        }

        ManagerRequest request = new ManagerRequest();
        request.setUsername(username);
        request.setEmail(username.toLowerCase().replace(" ", ".") + "@esportify.local");
        request.setNiveau(niveau);
        request.setMotivation(motivation);
        request.setStatus("En attente");
        try {
            managerRequestService.addEntity(request);
        } catch (RuntimeException e) {
            infoLabel.setText("Echec envoi demande: " + e.getMessage());
            return;
        }

        infoLabel.setText("Demande envoyee avec succes.");
        usernameField.setText(value(AppSession.getInstance().getUsername()));
        levelBox.setValue("Amateur");
        motivationArea.clear();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private void clearValidationState() {
        clearInvalidStyle(usernameField, levelBox, motivationArea);
        infoLabel.setText("");
    }

    private void showValidationError(String message, Control control) {
        if (control != null && !control.getStyleClass().contains(INVALID_STYLE_CLASS)) {
            control.getStyleClass().add(INVALID_STYLE_CLASS);
        }
        infoLabel.setText(message);
    }

    private void clearInvalidStyle(Control... controls) {
        for (Control control : controls) {
            if (control != null) {
                control.getStyleClass().remove(INVALID_STYLE_CLASS);
            }
        }
    }

    private void ensureStyleClass(javafx.scene.Node node, String... styleClasses) {
        if (node == null) {
            return;
        }
        for (String styleClass : styleClasses) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        }
    }
}
