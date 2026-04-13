package edu.esportify.navigation;

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
        primaryStage.setTitle("E-sportify Desktop");
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(760);
    }

    public static void goToLogin() {
        setScene("/views/login-view.fxml");
    }

    public static void goToManager() {
        setScene("/views/manager-layout-view.fxml");
    }

    public static void goToUserHome() {
        setScene("/views/user-layout-view.fxml");
    }

    public static void goToAdmin() {
        setScene("/views/admin-layout-view.fxml");
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
