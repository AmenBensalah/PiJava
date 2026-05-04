package edu.connexion3a77.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class TournoiAdminApp extends Application {

    @Override
    public void start(Stage stage) {
        SceneNavigator.init(stage);
        SceneNavigator.showAdminView();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
