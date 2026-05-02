package edu.projetJava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import edu.projetJava.entities.Categorie;
import edu.projetJava.models.Produit;
import edu.projetJava.services.CategorieService;
import edu.projetJava.services.ProduitService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class BackAjoutProduitController implements Initializable {

    @FXML private TextField tfId;
    @FXML private TextField tfNom;
    @FXML private TextField tfPrix;
    @FXML private TextField tfStock;
    @FXML private TextField tfDescription;
    @FXML private TextField tfStatut;
    @FXML private TextField tfImage;
    @FXML private CheckBox cbActive;
    @FXML private ComboBox<Categorie> cbCategorie;
    @FXML private Button btnAjouter;
    @FXML private Button btnEnregistrer;
    @FXML private Button btnSupprimer;

    private ProduitService produitService = new ProduitService();
    private CategorieService categorieService = new CategorieService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            List<Categorie> categories = categorieService.recuperer();
            cbCategorie.getItems().addAll(categories);
            
            // Mode AJOUT par défaut (on cache les autres)
            setModeEdition(false);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void initData(Produit p) {
        setModeEdition(true); // Active le mode édition
        tfId.setText(String.valueOf(p.getId()));
        tfNom.setText(p.getNom());
        tfPrix.setText(String.valueOf(p.getPrix()));
        tfStock.setText(String.valueOf(p.getStock()));
        tfDescription.setText(p.getDescription());
        tfStatut.setText(p.getStatut());
        tfImage.setText(p.getImage());
        cbActive.setSelected(p.getActive());

        for (Categorie c : cbCategorie.getItems()) {
            if (c.getId() == p.getCategorieId()) {
                cbCategorie.getSelectionModel().select(c);
                break;
            }
        }
    }

    @FXML
    void uploadImage(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        java.io.File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            tfImage.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    void ajouterProduit(ActionEvent event) {
        if (validerSaisie()) {
            try {
                // VERIFICATION DES DOUBLONS PAR NOM
                if (produitService.existeDeja(tfNom.getText().trim())) {
                    showAlert(Alert.AlertType.WARNING, "PRODUIT EXISTANT", "Un produit portant le nom '" + tfNom.getText().trim() + "' existe déjà dans le catalogue.");
                    return;
                }

                Produit p = new Produit();
                remplirProduitDepuisFormulaire(p);
                produitService.ajouter(p);
                System.out.println("Produit ajouté avec succès !");
                goBackToList(event);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur Base de Données", "Impossible d'enregistrer le produit.");
            }
        }
    }

    @FXML
    void modifierProduit(ActionEvent event) {
        if (tfId.getText().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Sélection Requise", "Veuillez d'abord choisir un produit à modifier.");
            return;
        }

        if (validerSaisie()) {
            try {
                Produit p = new Produit();
                p.setId(Integer.parseInt(tfId.getText()));
                remplirProduitDepuisFormulaire(p);
                produitService.modifier(p);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Produit mis à jour avec succès !");
                goBackToList(event);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de la modification.");
            }
        }
    }

    private boolean validerSaisie() {
        StringBuilder errors = new StringBuilder();

        if (tfNom.getText().trim().isEmpty()) errors.append("- Le nom du produit est obligatoire.\n");
        
        try {
            int prix = Integer.parseInt(tfPrix.getText());
            if (prix <= 0) errors.append("- Le prix doit être supérieur à 0 €.\n");
        } catch (NumberFormatException e) {
            errors.append("- Le prix doit être un nombre entier valide.\n");
        }

        try {
            int stock = Integer.parseInt(tfStock.getText());
            if (stock < 0) errors.append("- Le stock ne peut pas être négatif.\n");
        } catch (NumberFormatException e) {
            errors.append("- Le stock doit être un chiffre entier.\n");
        }

        if (tfDescription.getText().trim().isEmpty()) errors.append("- La description est obligatoire pour le client.\n");
        
        if (cbCategorie.getValue() == null) errors.append("- Veuillez sélectionner une catégorie.\n");

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Contrôle de saisie", "Veuillez corriger les erreurs suivantes :\n" + errors.toString());
            return false;
        }
        return true;
    }

    private void remplirProduitDepuisFormulaire(Produit p) {
        p.setNom(tfNom.getText().trim());
        p.setPrix(Integer.parseInt(tfPrix.getText()));
        p.setStock(Integer.parseInt(tfStock.getText()));
        p.setDescription(tfDescription.getText().trim());
        p.setStatut(tfStatut.getText());
        p.setImage(tfImage.getText());
        p.setActive(cbActive.isSelected());
        if (cbCategorie.getValue() != null) {
            p.setCategorieId(cbCategorie.getValue().getId());
        }
    }

    @FXML
    void supprimerProduit(ActionEvent event) {
        try {
            if (tfId.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Pour supprimer, vous devez uniquement fournir l'ID du produit.");
                return;
            }
            int id = Integer.parseInt(tfId.getText());
            produitService.supprimer(id);
            showAlert(Alert.AlertType.INFORMATION, "Succès!", "Le produit avec l'ID " + id + " a été supprimé.");
            clearFields();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Erreur base de données: " + e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Format", "L'ID doit être un chiffre.");
        }
    }

    private void clearFields() {
        tfId.clear();
        tfNom.clear();
        tfPrix.clear();
        tfStock.clear();
        tfDescription.clear();
        tfStatut.clear();
        tfImage.clear();
        cbCategorie.getSelectionModel().clearSelection();
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
    void goToAdminCategorie(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/backCategorie.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        if(!stage.isFullScreen()) stage.setFullScreen(true);
    }

    @FXML
    void goBackToList(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/backListProduit.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            if(!stage.isFullScreen()) stage.setFullScreen(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setModeEdition(boolean editMode) {
        if (btnAjouter != null) {
            btnAjouter.setVisible(!editMode);
            btnAjouter.setManaged(!editMode);
        }
        if (btnEnregistrer != null) {
            btnEnregistrer.setVisible(editMode);
            btnEnregistrer.setManaged(editMode);
        }
        if (btnSupprimer != null) {
            btnSupprimer.setVisible(editMode);
            btnSupprimer.setManaged(editMode);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        showModernAlert(title, content);
    }

    private void showModernAlert(String title, String message) {
        try {
            Stage alertStage = new Stage();
            alertStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            alertStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);

            VBox root = new VBox(15);
            root.getStyleClass().add("modern-alert");
            // On charge le CSS de l'admin pour avoir les styles du popup
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
            // Fallback si souci de chargement
            System.err.println("Erreur alerte moderne : " + e.getMessage());
        }
    }
}
