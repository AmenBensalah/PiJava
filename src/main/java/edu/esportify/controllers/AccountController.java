package edu.esportify.controllers;

import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AccountController implements ManagerContentController {
    private ManagerLayoutController parentController;

    @FXML
    private Label usernameValueLabel;

    @FXML
    private Label roleValueLabel;

    @FXML
    private Label statusValueLabel;

    @Override
    public void init(ManagerLayoutController parentController) {
        this.parentController = parentController;
        usernameValueLabel.setText(AppSession.getInstance().getUsername());
        roleValueLabel.setText(AppSession.getInstance().getRole());
        statusValueLabel.setText("Offline");
    }

    @FXML
    private void onMyTeam() {
        if (parentController != null) {
            parentController.showTeamDashboard();
        }
    }

    @FXML
    private void onApplications() {
        if (parentController != null) {
            parentController.showCandidates();
        }
    }

    @FXML
    private void onLogout() {
        AppSession.getInstance().logout();
        AppNavigator.goToLogin();
    }
}
