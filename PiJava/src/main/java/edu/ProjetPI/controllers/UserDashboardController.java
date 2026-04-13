package edu.ProjetPI.controllers;

import edu.ProjetPI.entities.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class UserDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label detailsLabel;

    @FXML
    public void initialize() {
        User currentUser = DashboardSession.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
            detailsLabel.setText("You are connected in the front office with email: " + currentUser.getEmail());
        }
    }

    @FXML
    public void handleViewProfile() {
        SceneManager.switchScene("/edu/ProjetPI/views/profile.fxml", "Mon Profil");
    }

    @FXML
    public void handleLogout() {
        DashboardSession.clear();
        SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "Login");
    }
}
