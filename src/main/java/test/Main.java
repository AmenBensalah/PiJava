package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Initialiser le gestionnaire de scènes
        edu.ProjetPI.controllers.SceneManager.setStage(stage);

        // Définir la page de Login comme point d'entrée
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/ProjetPI/views/login.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("E-SPORTIFY : Connexion");
        
        // Mode plein écran pour l'immersion Cyberpunk
        stage.setFullScreen(false);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        // On utilise launch directement ici
        launch(args);
    }
}
