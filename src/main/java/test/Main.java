package test;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // On charge la boutique par défaut (Front-office)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajoutProduit.fxml"));
        Parent root = loader.load();
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("E-SPORTIFY : Boutique Management System");
        
        // On force le mode plein écran pour une meilleure immersion
        stage.setFullScreen(true);
        stage.show();
    }

    public static void main(String[] args) {
        // On utilise launch directement ici
        launch(args);
    }
}
