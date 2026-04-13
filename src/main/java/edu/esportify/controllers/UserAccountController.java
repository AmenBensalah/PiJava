package edu.esportify.controllers;

import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UserAccountController implements UserContentController {
    @FXML private Label usernameValueLabel;
    @FXML private Label roleValueLabel;
    @FXML private Label statusValueLabel;

    private UserLayoutController parentController;

    @Override
    public void init(UserLayoutController parentController) {
        this.parentController = parentController;
        usernameValueLabel.setText(AppSession.getInstance().getUsername());
        roleValueLabel.setText(AppSession.getInstance().getRole());
        statusValueLabel.setText("Offline");
    }

    @FXML
    private void onTeams() {
        parentController.showTeams();
    }

    @FXML
    private void onCandidatures() {
        parentController.showCandidatures();
    }

    @FXML
    private void onLogout() {
        AppSession.getInstance().logout();
        AppNavigator.goToLogin();
    }
}
