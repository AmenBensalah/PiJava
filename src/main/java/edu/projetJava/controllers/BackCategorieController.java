package edu.projetJava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import edu.projetJava.entities.Categorie;
import edu.projetJava.services.CategorieService;

import java.io.IOException;
import java.sql.SQLException;

public class BackCategorieController {

    @FXML private TextField tfId;
    @FXML private TextField tfNom;

    private CategorieService categorieService = new CategorieService();

    public void initData(Categorie c) {
        tfId.setText(String.valueOf(c.getId()));
        tfNom.setText(c.getNom());
    }

    @FXML
    void ajouterCategorie(ActionEvent event) {
        try {
            if (tfNom.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez remplir le Nom de la catégorie.");
                return;
            }

            Categorie c = new Categorie();
            c.setNom(tfNom.getText());
            categorieService.ajouter(c);
            
            showAlert(Alert.AlertType.INFORMATION, "Succès!", "La catégorie '"+c.getNom()+"' a été ajoutée !");
            tfNom.clear();
            tfId.clear();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Impossible d'insérer: " + e.getMessage());
        }
    }

    @FXML
    void modifierCategorie(ActionEvent event) {
        try {
            if (tfId.getText().isEmpty() || tfNom.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez fournir l'ID et le nouveau Nom.");
                return;
            }

            Categorie c = new Categorie();
            c.setId(Integer.parseInt(tfId.getText()));
            c.setNom(tfNom.getText());

            categorieService.modifier(c);
            showAlert(Alert.AlertType.INFORMATION, "Succès!", "La catégorie modifiée avec succès.");
            tfNom.clear();
            tfId.clear();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Format", "L'ID doit être un nombre.");
        }
    }

    @FXML
    void supprimerCategorie(ActionEvent event) {
        try {
            if (tfId.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez fournir l'ID de la catégorie à supprimer.");
                return;
            }
            int id = Integer.parseInt(tfId.getText());
            categorieService.supprimer(id);
            showAlert(Alert.AlertType.INFORMATION, "Succès!", "Catégorie supprimée.");
            tfNom.clear();
            tfId.clear();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Format", "L'ID doit être un nombre.");
        }
    }

    // --- NAVIGATION ---
    @FXML
    void goToFrontOffice(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ajoutProduit.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        if(!stage.isFullScreen()) stage.setFullScreen(true);
    }

    @FXML
    void goToAdminProduit(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/backAjoutProduit.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        if(!stage.isFullScreen()) stage.setFullScreen(true);
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
