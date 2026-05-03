package edu.ProjetPI.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {

    private static Stage primaryStage;

    private SceneManager() {
    }

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene currentScene = primaryStage.getScene();
            Scene scene = currentScene == null
                    ? new Scene(root)
                    : new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            scene.setFill(Color.BLACK);
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.setFullScreen(false);
            primaryStage.setMaximized(true);
            primaryStage.show();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load scene: " + fxmlPath, e);
        }
    }
}
