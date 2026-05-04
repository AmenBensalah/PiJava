package edu.PROJETPI.tools;

import javafx.scene.control.Alert;

public final class AlertUtils {
    private AlertUtils() {
    }

    public static void showError(String message) {
        show(Alert.AlertType.ERROR, "Erreur", message);
    }

    public static void showSuccess(String message) {
        show(Alert.AlertType.INFORMATION, "Succes", message);
    }

    public static void showInfo(String message) {
        show(Alert.AlertType.INFORMATION, "Information", message);
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
