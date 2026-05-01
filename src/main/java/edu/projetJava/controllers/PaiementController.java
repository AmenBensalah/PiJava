package edu.projetJava.controllers;

import edu.projetJava.models.Produit;
import edu.projetJava.models.Commande;
import edu.projetJava.services.CommandeService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PaiementController {

    @FXML private ImageView produitImage;
    @FXML private Label produitNom;
    @FXML private Label produitPrix;
    @FXML private TextField inputNom;
    @FXML private TextField inputEmail;
    @FXML private TextField inputQuantite;
    @FXML private ComboBox<String> comboPaiement;
    @FXML private Label lblTotal;

    @FXML private Label lblPromo;

    private Produit produitConcerne;
    private CommandeService commandeService = new CommandeService();
    private double currentTotal = 0.0;
    private boolean isDiscountApplied = false;

    @FXML
    public void initialize() {
        comboPaiement.getItems().addAll("Carte Bancaire", "PayPal", "Crypto (Bitcoin)", "À la livraison");
        comboPaiement.getSelectionModel().selectFirst();
    }

    public void initData(Produit produit) {
        this.produitConcerne = produit;
        produitNom.setText(produit.getNom());
        produitPrix.setText(produit.getPrix() + " €");
        updateTotal();

        if (produit.getImage() != null && !produit.getImage().isEmpty()) {
            try {
                String path = produit.getImage();
                if (!path.startsWith("http") && !path.startsWith("file:")) {
                    path = new File(path).toURI().toString();
                }
                produitImage.setImage(new Image(path));
            } catch (Exception e) {
                // Ignore image error
            }
        }
    }

    public void setGlobalDiscount(boolean discountApplied, String email) {
        if (discountApplied) {
            this.isDiscountApplied = true;
            if (email != null) {
                this.inputEmail.setText(email);
            }
            this.lblPromo.setVisible(true);
            this.lblPromo.setManaged(true);
            updateTotal();
        }
    }

    @FXML
    void updateTotal() {
        try {
            int quantite = Integer.parseInt(inputQuantite.getText());
            if (quantite <= 0) quantite = 1;
            if (quantite > produitConcerne.getStock()) {
                inputQuantite.setText(String.valueOf(produitConcerne.getStock()));
                quantite = produitConcerne.getStock();
            }
            
            double baseTotal = quantite * produitConcerne.getPrix();
            
            if (isDiscountApplied) {
                currentTotal = baseTotal * 0.9; // 10% de réduction
            } else {
                currentTotal = baseTotal;
            }
            
            lblTotal.setText(String.format("%.2f €", currentTotal));
            
        } catch (NumberFormatException e) {
            lblTotal.setText("0 €");
        }
    }

    @FXML
    void appliquerOffre(ActionEvent event) {
        String email = inputEmail.getText();
        if (email == null || !email.contains("@")) {
            showAlert(Alert.AlertType.WARNING, "Email invalide", "Veuillez entrer une adresse e-mail valide avant de vérifier l'offre.");
            return;
        }

        if (commandeService.aDejaCommande(email)) {
            // Client a déjà commandé, on applique 10%
            isDiscountApplied = true;
            lblPromo.setVisible(true);
            lblPromo.setManaged(true);
            lblPromo.getParent().layout();
            updateTotal();
            showAlert(Alert.AlertType.INFORMATION, "Offre Activée !", "Félicitations ! Vous avez déjà commandé chez nous.\nUne réduction de 10% a été appliquée sur votre panier.");
        } else {
            isDiscountApplied = false;
            lblPromo.setVisible(false);
            lblPromo.setManaged(false);
            lblPromo.getParent().layout();
            updateTotal();
            showAlert(Alert.AlertType.INFORMATION, "Aucune offre", "Aucune commande précédente trouvée pour cet e-mail.\nL'offre s'applique à partir de la 2ème commande.");
        }
    }

    @FXML
    void confirmerAchat(ActionEvent event) {
        String nom = inputNom.getText();
        String email = inputEmail.getText();
        String quantiteStr = inputQuantite.getText();
        String methode = comboPaiement.getValue();

        if (nom.isEmpty() || email.isEmpty() || quantiteStr.isEmpty() || methode == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        if (!email.contains("@")) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez entrer un email valide.");
            return;
        }

        try {
            int quantite = Integer.parseInt(quantiteStr);
            if (quantite <= 0) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "La quantité doit être supérieure à 0.");
                return;
            }
            if (quantite > produitConcerne.getStock()) {
                showAlert(Alert.AlertType.WARNING, "Erreur", "Stock insuffisant (Max: " + produitConcerne.getStock() + ").");
                return;
            }

            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            Commande c = new Commande(produitConcerne.getId(), nom, email, methode, quantite, currentTotal, date);
            commandeService.ajouter(c);

            String successMsg = "Votre commande pour " + produitConcerne.getNom() + " a bien été enregistrée !";
            if (isDiscountApplied) {
                successMsg += "\n\n🎉 Merci pour votre fidélité ! Vous avez bénéficié de -10% sur cette commande.";
            }

            showAlert(Alert.AlertType.INFORMATION, "Paiement réussi", successMsg);
            fermer();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Serveur", "Impossible d'enregistrer la commande : " + e.getMessage());
        }
    }

    @FXML
    void fermer() {
        Stage stage = (Stage) inputNom.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        if (inputNom != null && inputNom.getScene() != null) {
            alert.initOwner(inputNom.getScene().getWindow());
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
