package edu.esportify.controllers;

import edu.esportify.navigation.AppNavigator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class AdminGridDashboardController {
    
    @FXML private VBox rootPane;
    
    private AdminLayoutController parentController;
    
    public void init(AdminLayoutController parentController) {
        this.parentController = parentController;
    }
    
    @FXML
    private void onFeedSection() {
        if (parentController != null) {
            parentController.showFeedSection();
        }
    }
    
    @FXML
    private void onTeamsSection() {
        if (parentController != null) {
            parentController.showTeamsSection();
        }
    }
    
    @FXML
    private void onTournamentsSection() {
        if (parentController != null) {
            parentController.showTournamentsSection();
        }
    }
    
    @FXML
    private void onStoreSection() {
        if (parentController != null) {
            parentController.showStoreSection();
        }
    }
    
    @FXML
    private void onAccountsSection() {
        if (parentController != null) {
            parentController.showAccountsSection();
        }
    }
    
    @FXML
    private void onBackoffice() {
        if (parentController != null) {
            parentController.showBackoffice();
        }
    }
    
    @FXML
    private void onPaymentsSection() {
        if (parentController != null) {
            parentController.showPaymentsSection();
        }
    }
    
    @FXML
    private void onForecastSection() {
        if (parentController != null) {
            parentController.showForecastSection();
        }
    }
    
    @FXML
    private void onSettingsSection() {
        if (parentController != null) {
            parentController.showSettingsSection();
        }
    }
}
