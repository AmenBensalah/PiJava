package edu.PROJETPI;

import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.services.CheckoutService;
import edu.PROJETPI.services.OrderSession;
import edu.PROJETPI.tools.AlertUtils;
import edu.PROJETPI.tools.SceneNavigator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class PaymentController implements Initializable {

    @FXML
    private Label nomLabel;
    @FXML
    private Label prenomLabel;
    @FXML
    private Label telephoneLabel;
    @FXML
    private Label adresseLabel;
    @FXML
    private Label totalLabel;
    @FXML
    private Label articleCountLabel;

    private final CheckoutService checkoutService = new CheckoutService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDraftSummary();
    }

    @FXML
    private void goBackToCommande() {
        SceneNavigator.switchScene(nomLabel, "/commande-view.fxml", "Informations commande");
    }

    @FXML
    private void confirmPayment() {
        Commande draft = OrderSession.getInstance().getDraftCommande();
        if (draft == null) {
            AlertUtils.showError("Les informations de commande sont manquantes.");
            return;
        }
        if (OrderSession.getInstance().isCartEmpty()) {
            AlertUtils.showError("Le panier est vide.");
            return;
        }

        try {
            int commandeId = checkoutService.checkout(
                    OrderSession.getInstance(),
                    java.sql.Date.valueOf(java.time.LocalDate.now())
            );
            AlertUtils.showSuccess("Commande payee avec succes. ID commande : " + commandeId);
            SceneNavigator.switchScene(nomLabel, "/main-view.fxml", "Catalogue produits");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur pendant la validation du paiement : " + e.getMessage());
        }
    }

    private void loadDraftSummary() {
        Commande draft = OrderSession.getInstance().getDraftCommande();
        if (draft == null) {
            nomLabel.setText("-");
            prenomLabel.setText("-");
            telephoneLabel.setText("-");
            adresseLabel.setText("-");
            totalLabel.setText("0.00 TND");
            articleCountLabel.setText("0");
            return;
        }

        nomLabel.setText(emptyIfNull(draft.getNom()));
        prenomLabel.setText(emptyIfNull(draft.getPrenom()));
        telephoneLabel.setText(emptyIfNull(draft.getTelephone()));
        adresseLabel.setText(emptyIfNull(draft.getAdresse()));
        totalLabel.setText(String.format("%.2f TND", OrderSession.getInstance().getCartTotal()));
        articleCountLabel.setText(String.valueOf(OrderSession.getInstance().getTotalItems()));
    }

    private String emptyIfNull(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
