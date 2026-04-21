package edu.PROJETPI;

import edu.PROJETPI.entites.CartItem;
import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.services.OrderSession;
import edu.PROJETPI.tools.AlertUtils;
import edu.PROJETPI.tools.SceneNavigator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class CommandeController implements Initializable {

    @FXML
    private TextField nomField;
    @FXML
    private TextField prenomField;
    @FXML
    private TextField telephoneField;
    @FXML
    private Label clientIdAutoLabel;
    @FXML
    private Label dateCommandeAutoLabel;
    @FXML
    private TextArea adresseArea;
    @FXML
    private Label totalField;
    @FXML
    private Label articlesField;
    @FXML
    private Label cartCountTopLabel;
    @FXML
    private Label panierProductNameLabel;
    @FXML
    private Label panierUnitPriceLabel;
    @FXML
    private Label panierQuantityLabel;
    @FXML
    private Label panierLineTotalLabel;
    @FXML
    private Label subtotalLabel;
    @FXML
    private Label totalSummaryLabel;

    private static final int AUTO_CLIENT_ID_PLACEHOLDER = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadSessionData();
    }

    @FXML
    private void goBackToCart() {
        SceneNavigator.switchScene(nomField, "/lignecommande-view.fxml", "Panier");
    }

    @FXML
    private void continueToPayment() {
        Commande commande = readForm();
        if (commande == null) {
            return;
        }

        commande.setStatut("EN_ATTENTE");
        OrderSession.getInstance().setDraftCommande(commande);
        OrderSession.getInstance().setCheckoutMode(OrderSession.CheckoutMode.STRIPE);
        SceneNavigator.switchScene(nomField, "/payment-view.fxml", "Paiement en ligne Stripe");
    }

    @FXML
    private void increaseQuantity() {
        updateFirstItemQuantity(1);
    }

    @FXML
    private void decreaseQuantity() {
        updateFirstItemQuantity(-1);
    }

    @FXML
    private void continueShopping() {
        SceneNavigator.switchScene(nomField, "/main-view.fxml", "Catalogue produits");
    }

    @FXML
    private void deleteCommande() {
        OrderSession.getInstance().clearCart();
        SceneNavigator.switchScene(nomField, "/main-view.fxml", "Catalogue produits");
    }

    @FXML
    private void goToDeliveryPayment() {
        Commande commande = readForm();
        if (commande == null) {
            return;
        }

        commande.setStatut("EN_LIVRAISON");
        OrderSession.getInstance().setDraftCommande(commande);
        OrderSession.getInstance().setCheckoutMode(OrderSession.CheckoutMode.CASH_ON_DELIVERY);
        SceneNavigator.switchScene(nomField, "/delivery-view.fxml", "Adresse de livraison");
    }

    private void loadSessionData() {
        Commande draft = OrderSession.getInstance().getDraftCommande();
        java.time.LocalDate today = java.time.LocalDate.now();
        if (clientIdAutoLabel != null) {
            clientIdAutoLabel.setText("Genere apres integration");
        }
        dateCommandeAutoLabel.setText(today.toString());
        nomField.setText(draft != null ? emptyIfNull(draft.getNom()) : "");
        prenomField.setText(draft != null ? emptyIfNull(draft.getPrenom()) : "");
        telephoneField.setText(draft != null ? emptyIfNull(draft.getTelephone()) : "");
        adresseArea.setText(draft != null ? extractClientAddress(draft.getAdresse()) : "");
        totalField.setText(String.format("%.2f TND", OrderSession.getInstance().getCartTotal()));
        articlesField.setText(String.valueOf(OrderSession.getInstance().getTotalItems()));
        refreshCartPreview();
    }

    private Commande readForm() {
        if (OrderSession.getInstance().isCartEmpty()) {
            AlertUtils.showError("Le panier est vide.");
            return null;
        }

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String adresse = adresseArea.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || telephone.isEmpty() || adresse.isEmpty()) {
            AlertUtils.showError("Remplissez toutes les informations client avant le paiement.");
            return null;
        }

        Commande draft = OrderSession.getInstance().getDraftCommande();
        Commande commande = new Commande(
                java.sql.Date.valueOf(java.time.LocalDate.now()),
                OrderSession.getInstance().getCartTotal(),
                AUTO_CLIENT_ID_PLACEHOLDER
        );
        if (draft != null) {
            commande.setId(draft.getId());
            commande.setPaysLivraison(draft.getPaysLivraison());
            commande.setGouvernoratLivraison(draft.getGouvernoratLivraison());
            commande.setCodePostalLivraison(draft.getCodePostalLivraison());
            commande.setAdresseLivraison(draft.getAdresseLivraison());
            commande.setDescriptionLivraison(draft.getDescriptionLivraison());
        }
        commande.setNom(nom);
        commande.setPrenom(prenom);
        commande.setTelephone(telephone);
        commande.setAdresse(adresse);
        commande.setStatut("EN_ATTENTE");
        return commande;
    }

    private void refreshCartPreview() {
        OrderSession session = OrderSession.getInstance();
        cartCountTopLabel.setText(session.getTotalItems() + " article(s)");
        subtotalLabel.setText(String.format("%.2f TND", session.getCartTotal()));
        totalSummaryLabel.setText(String.format("%.2f TND", session.getCartTotal()));

        if (session.getCartItems().isEmpty()) {
            panierProductNameLabel.setText("Panier vide");
            panierUnitPriceLabel.setText("-");
            panierQuantityLabel.setText("0");
            panierLineTotalLabel.setText("0.00 TND");
            totalField.setText("0.00 TND");
            articlesField.setText("0");
            return;
        }

        CartItem firstItem = session.getCartItems().get(0);
        panierProductNameLabel.setText(firstItem.getNomProduit());
        panierUnitPriceLabel.setText(String.format("Prix unitaire: %.2f TND", firstItem.getPrixUnitaire()));
        panierQuantityLabel.setText(String.valueOf(firstItem.getQuantite()));
        panierLineTotalLabel.setText(String.format("%.2f TND", firstItem.getSousTotal()));
        totalField.setText(String.format("%.2f TND", session.getCartTotal()));
        articlesField.setText(String.valueOf(session.getTotalItems()));
    }

    private void updateFirstItemQuantity(int delta) {
        if (OrderSession.getInstance().isCartEmpty()) {
            AlertUtils.showError("Le panier est vide.");
            return;
        }

        CartItem firstItem = OrderSession.getInstance().getCartItems().get(0);
        int newQuantity = firstItem.getQuantite() + delta;
        if (newQuantity <= 0) {
            AlertUtils.showError("La quantite doit rester superieure a zero.");
            return;
        }

        OrderSession.getInstance().updateQuantity(firstItem.getProduitId(), newQuantity);
        loadSessionData();
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private String extractClientAddress(String adresse) {
        if (adresse == null || adresse.isBlank()) {
            return "";
        }

        int deliveryIndex = adresse.indexOf("\n\nLivraison\n");
        return deliveryIndex >= 0 ? adresse.substring(0, deliveryIndex) : adresse;
    }

}
