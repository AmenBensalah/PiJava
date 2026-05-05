package edu.esportify.navigation;

import edu.ProjetPI.controllers.SceneManager;
import java.io.IOException;
import java.net.URL;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class AppNavigator {
    private static Stage primaryStage;

    private AppNavigator() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
        SceneManager.setStage(stage);
        primaryStage.setTitle("E-sportify Desktop");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(760);
    }

    public static boolean isReady() {
        return primaryStage != null;
    }

    public static void goToLogin() {
        setScene("/edu/ProjetPI/views/login.fxml");
    }

    public static void goToRegister() {
        setScene("/views/register-view.fxml");
    }

    public static void goToForgotPassword() {
        setScene("/views/forgot-password-view.fxml");
    }

    public static void goToManager() {
        setScene("/views/manager-layout-view.fxml");
    }

    public static void goToUserHome() {
        AppSession.getInstance().setPendingUserHomeSection(AppSession.UserHomeSection.STORE);
        setScene("/views/user-layout-view.fxml");
    }

    public static void goToUserHome(AppSession.UserHomeSection section) {
        AppSession.getInstance().setPendingUserHomeSection(section);
        setScene("/views/user-layout-view.fxml");
    }

    public static void goToAdmin() {
        goToAdmin(AppSession.AdminSection.OVERVIEW);
    }

    public static void goToAdmin(AppSession.AdminSection section) {
        AppSession.getInstance().setPendingAdminSection(section);
        if (section == AppSession.AdminSection.TEAMS || section == AppSession.AdminSection.REQUESTS) {
            setScene("/backTeamsDashboard.fxml");
            return;
        }
        if (section == AppSession.AdminSection.STORE) {
            setScene("/backListProduit.fxml");
            return;
        }
        setScene("/fxml/rawg-games-view.fxml");
    }

    public static void goToProfile() {
        setScene("/edu/ProjetPI/views/profile.fxml");
    }

    public static FXMLLoader createLoader(String resourcePath) {
        URL resource = AppNavigator.class.getResource(resourcePath);
        if (resource == null) {
            throw new IllegalArgumentException("Vue introuvable: " + resourcePath);
        }
        return new FXMLLoader(resource);
    }

    public static Parent loadNode(String resourcePath) {
        try {
            return createLoader(resourcePath).load();
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger " + resourcePath, e);
        }
    }

    private static void setScene(String resourcePath) {
        try {
            FXMLLoader loader = createLoader(resourcePath);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            URL stylesheet = AppNavigator.class.getResource("/styles/app.css");
            if (stylesheet != null) {
                scene.getStylesheets().add(stylesheet.toExternalForm());
            }
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'ouvrir " + resourcePath, e);
        }
    }
}
