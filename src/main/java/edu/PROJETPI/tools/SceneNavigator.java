package edu.PROJETPI.tools;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public final class SceneNavigator {
    private SceneNavigator() {
    }

    public static void switchScene(Node node, String resource, String title) {
        try {
            Stage stage = (Stage) node.getScene().getWindow();
            boolean maximized = stage.isMaximized();
            double x = stage.getX();
            double y = stage.getY();
            double width = stage.getWidth();
            double height = stage.getHeight();

            FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(resource));
            Parent root = loader.load();
            Scene scene = new Scene(root, node.getScene().getWidth(), node.getScene().getHeight());
            scene.setFill(Color.BLACK);
            scene.getStylesheets().add(SceneNavigator.class.getResource("/styles/esportify-theme.css").toExternalForm());

            stage.setTitle(title);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setFullScreen(false);
            if (!maximized) {
                stage.setX(x);
                stage.setY(y);
                stage.setWidth(width);
                stage.setHeight(height);
            }
            stage.setMaximized(true);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de charger l'ecran " + resource, e);
        }
    }
}
