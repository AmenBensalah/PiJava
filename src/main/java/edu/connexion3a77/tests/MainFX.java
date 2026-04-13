package edu.connexion3a77.tests;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainFX extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FilActualiteView.fxml"));
        Parent content = loader.load();

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double targetW = Math.min(1600, bounds.getWidth() * 0.92);
        double targetH = Math.min(900, bounds.getHeight() * 0.92);

        Scene scene = new Scene(content, targetW, targetH);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("E-SPORTIFY JavaFX");
        stage.setWidth(targetW);
        stage.setHeight(targetH);
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.setMaximized(false);
        stage.setFullScreen(false);
        stage.setResizable(true);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
