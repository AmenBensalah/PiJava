package edu.ProjetPI.controllers;

import edu.ProjetPI.entities.User;
import edu.ProjetPI.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    private final UserService userService = new UserService();

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    public void handleLogin() {
        try {
            Optional<User> user = userService.authenticate(emailField.getText(), passwordField.getText());
            if (user.isEmpty()) {
                messageLabel.setText("Invalid email or password.");
                return;
            }

            DashboardSession.setCurrentUser(user.get());
            String role = user.get().getRole();
            if ("ROLE_ADMIN".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
                SceneManager.switchScene("/edu/ProjetPI/views/admin-dashboard.fxml", "Admin Dashboard");
            } else {
                SceneManager.switchScene("/edu/ProjetPI/views/user-dashboard.fxml", "Front Office");
            }
        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void goToRegister() {
        SceneManager.switchScene("/edu/ProjetPI/views/register.fxml", "Create Account");
    }
}
