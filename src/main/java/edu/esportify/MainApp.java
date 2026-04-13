package edu.esportify;

import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) {
        AppSession.getInstance().logout();
        AppNavigator.init(stage);
        AppNavigator.goToLogin();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
