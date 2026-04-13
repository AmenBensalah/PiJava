package edu.PROJETPI.tools;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneNavigator {
    private SceneNavigator() {
    }

    public static void switchScene(Node node, String resource, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(resource));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(SceneNavigator.class.getResource("/styles/esportify-theme.css").toExternalForm());

            Stage stage = (Stage) node.getScene().getWindow();
            boolean maximized = stage.isMaximized();
            boolean fullScreen = stage.isFullScreen();
            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMaximized(maximized || !fullScreen);
            stage.setFullScreen(fullScreen);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de charger l'ecran " + resource, e);
        }
    }
}
