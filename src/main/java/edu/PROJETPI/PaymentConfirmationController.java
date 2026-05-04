package edu.PROJETPI;

import edu.PROJETPI.entites.CartItem;
import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.services.InvoicePdfService;
import edu.PROJETPI.services.OrderSession;
import edu.PROJETPI.tools.AlertUtils;
import edu.PROJETPI.tools.SceneNavigator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PaymentConfirmationController implements Initializable {

    @FXML
    private Label confirmationMessageLabel;
    @FXML
    private Label commandeIdLabel;
    @FXML
    private Label nomLabel;
    @FXML
    private Label prenomLabel;
    @FXML
    private Label telephoneLabel;
    @FXML
    private Label adresseLabel;
    @FXML
    private Label productNameLabel;
    @FXML
    private Label articleCountLabel;
    @FXML
    private Label totalLabel;

    private final InvoicePdfService invoicePdfService = new InvoicePdfService();
    private Commande confirmedCommande;
    private List<CartItem> confirmedItems;
    private double confirmedTotal;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadConfirmationSummary();
    }

    @FXML
    private void printInvoice() {
        if (confirmedCommande == null) {
            AlertUtils.showError("Les informations client sont manquantes.");
            return;
        }
        if (confirmedItems == null || confirmedItems.isEmpty()) {
            AlertUtils.showError("Aucun article a imprimer dans la facture.");
            return;
        }

        try {
            var pdfPath = invoicePdfService.generateInvoice(confirmedCommande, confirmedItems, confirmedTotal);
            invoicePdfService.openInvoice(pdfPath);
        } catch (IOException e) {
            AlertUtils.showError("Impossible de generer la facture PDF : " + e.getMessage());
        }
    }

    @FXML
    private void goToCatalogue() {
        SceneNavigator.switchScene(confirmationMessageLabel, "/ajoutProduit.fxml", "E-SPORTIFY : Boutique");
    }

    private void loadConfirmationSummary() {
        OrderSession session = OrderSession.getInstance();
        confirmedCommande = session.getConfirmedCommande();
        confirmedItems = session.getConfirmedCartItems();
        confirmedTotal = session.getConfirmedCartTotal();

        if (confirmedCommande == null) {
            confirmedCommande = session.getDraftCommande();
            confirmedItems = session.getCartItems();
            confirmedTotal = session.getCartTotal();
        }

        if (confirmedCommande == null) {
            confirmationMessageLabel.setText("Paiement confirme.");
            commandeIdLabel.setText("-");
            nomLabel.setText("-");
            prenomLabel.setText("-");
            telephoneLabel.setText("-");
            adresseLabel.setText("-");
            productNameLabel.setText("-");
            articleCountLabel.setText("0");
            totalLabel.setText("0.00 TND");
            return;
        }

        String clientName = (emptyIfNull(confirmedCommande.getPrenom()) + " " + emptyIfNull(confirmedCommande.getNom())).trim();
        confirmationMessageLabel.setText("Paiement confirme avec succes. Merci " + clientName + ", votre commande est enregistree.");
        commandeIdLabel.setText(session.getConfirmedCommandeId() > 0 ? "#" + session.getConfirmedCommandeId() : "-");
        nomLabel.setText(emptyIfNull(confirmedCommande.getNom()));
        prenomLabel.setText(emptyIfNull(confirmedCommande.getPrenom()));
        telephoneLabel.setText(emptyIfNull(confirmedCommande.getTelephone()));
        adresseLabel.setText(emptyIfNull(confirmedCommande.getAdresse()));
        productNameLabel.setText(formatProductSummary(confirmedItems));
        articleCountLabel.setText(String.valueOf(totalItems(confirmedItems)));
        totalLabel.setText(String.format("%.2f TND", confirmedTotal));
    }

    private int totalItems(List<CartItem> cartItems) {
        if (cartItems == null) {
            return 0;
        }
        return cartItems.stream().mapToInt(CartItem::getQuantite).sum();
    }

    private String formatProductSummary(List<CartItem> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return "-";
        }

        CartItem firstItem = cartItems.get(0);
        if (cartItems.size() == 1) {
            return emptyIfNull(firstItem.getNomProduit());
        }

        return emptyIfNull(firstItem.getNomProduit()) + " +" + (cartItems.size() - 1) + " autres";
    }

    private String emptyIfNull(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
