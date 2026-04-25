package edu.connexion3a77.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneNavigator {
    private static Stage primaryStage;

    private SceneNavigator() {
    }

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    public static void showAdminView() {
        show("/fxml/tournoi-admin-view.fxml", "/css/tournoi-admin.css", "E-Sportify Admin - Gestion des Tournois");
    }

    public static void showUserView() {
        show("/fxml/tournoi-user-view.fxml", "/css/tournoi-user.css", "E-Sportify User - Tournois");
    }

    public static void showRawgGamesView() {
        show("/fxml/rawg-games-view.fxml", "/css/rawg-games.css", "E-Sportify Admin - Consulter Jeux");
    }

    private static void show(String fxmlPath, String cssPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, 1400, 850);
            scene.getStylesheets().add(SceneNavigator.class.getResource(cssPath).toExternalForm());
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger la vue: " + fxmlPath, e);
        }
    }
}
