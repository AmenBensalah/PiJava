package edu.ProjetPI.tests;

import edu.ProjetPI.controllers.SceneManager;
import edu.ProjetPI.tools.DatabaseInitializer;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        DatabaseInitializer.initialize();
        stage.initStyle(StageStyle.DECORATED);
        stage.setResizable(true);
        stage.setFullScreen(false);
        stage.setMaximized(false);
        stage.setMinWidth(980);
        stage.setMinHeight(720);
        stage.setWidth(1280);
        stage.setHeight(820);
        SceneManager.setStage(stage);
        SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "Login");
        stage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
