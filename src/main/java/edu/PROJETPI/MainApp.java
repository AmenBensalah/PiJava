package edu.PROJETPI;

import edu.ProjetPI.controllers.SceneManager;
import edu.PROJETPI.tools.MyConexion;
import javafx.application.Platform;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        try {
            MyConexion.initDatabase();
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de demarrage");
            alert.setHeaderText("Connexion MySQL impossible");
            alert.setContentText("Verifiez que MySQL est lance sur localhost:3306 avec l'utilisateur root.");
            alert.showAndWait();
            Platform.exit();
            return;
        }

        SceneManager.setStage(stage);

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/edu/ProjetPI/views/login.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setTitle("E-SPORTIFY : Connexion");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setFullScreen(false);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
