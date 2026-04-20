package edu.projetJava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import edu.projetJava.services.MailService;

import java.io.IOException;

public class BackMailingController {

    @FXML private TextField tfEmailClient;
    @FXML private TextField tfSujet;
    @FXML private TextArea taMessage;

    @FXML
    void envoyerEmail(ActionEvent event) {
        String email = tfEmailClient.getText().trim();
        String sujet = tfSujet.getText().trim();
        String message = taMessage.getText().trim();

        if (email.isEmpty() || sujet.isEmpty() || message.isEmpty()) {
            showModernAlert("Champs manquants", "Veuillez remplir l'email cible, le sujet et rediger le message complet.");
            return;
        }

        try {
            // Style de l'e-mail envoyé
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ccc; border-radius: 10px;'>"
                               + "<h2 style='color: #8b5cf6;'>Notification E-SPORTIFY</h2>"
                               + "<p style='font-size: 14px;'>" + message.replace("\n", "<br>") + "</p>"
                               + "<br/><hr style='border-top:1px solid #eee;'/><br/>"
                               + "<small style='color: #888;'>Ceci est un message d'information automatique. Merci de ne pas y répondre.</small>"
                               + "</div>";
            
            // Appel au MailService
            MailService.sendEmail(email, sujet, htmlContent);
            
            showModernAlert("Succès!", "L'email vient d'être expédié au client avec succès.");
            
            // Vider les champs
            tfEmailClient.clear();
            tfSujet.clear();
            taMessage.clear();
            
        } catch (Exception e) {
            e.printStackTrace();
            showModernAlert("Erreur d'envoi", "Détail de l'erreur : " + e.toString() + "\n\nAssurez-vous que l'email / mot de passe d'application sont corrects dans MailService.java");
        }
    }

    // --- NAVIGATION COPIEE DE L'ADMIN ---
    @FXML
    void goToBackListProduit(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/backListProduit.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
    }
    
    @FXML
    void goToAdminCategorie(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/backCategorie.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
    }
    
    @FXML
    void goToFrontOffice(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ajoutProduit.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
    }

    private void showModernAlert(String title, String message) {
        try {
            Stage alertStage = new Stage();
            alertStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            alertStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            VBox root = new VBox(15);
            root.getStyleClass().add("modern-alert");
            String cssPath = getClass().getResource("/ajoutProduit.css").toExternalForm();
            root.getStylesheets().add(cssPath);
            root.setPadding(new javafx.geometry.Insets(25));
            root.setAlignment(javafx.geometry.Pos.CENTER);
            root.setMinWidth(400);

            Label lblTitle = new Label("🚨 " + title.toUpperCase());
            lblTitle.getStyleClass().add("modern-alert-title");

            Label lblMessage = new Label(message);
            lblMessage.getStyleClass().add("modern-alert-content");
            lblMessage.setWrapText(true);
            lblMessage.setAlignment(javafx.geometry.Pos.CENTER);

            Button btnOk = new Button("COMPRIS");
            btnOk.getStyleClass().add("modern-alert-btn");
            btnOk.setPrefWidth(120);
            btnOk.setPrefHeight(40);
            btnOk.setOnAction(e -> alertStage.close());

            root.getChildren().addAll(lblTitle, lblMessage, btnOk);
            
            Scene scene = new Scene(root);
            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
            alertStage.setScene(scene);
            alertStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
