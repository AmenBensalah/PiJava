package edu.PROJETPI;

import com.stripe.exception.StripeException;
import edu.PROJETPI.entites.CartItem;
import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.services.CheckoutService;
import edu.PROJETPI.services.InvoicePdfService;
import edu.PROJETPI.services.OrderSession;
import edu.PROJETPI.services.StripeCheckoutService;
import edu.PROJETPI.tools.AlertUtils;
import edu.PROJETPI.tools.SceneNavigator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
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
    @FXML
    private Label productNameLabel;
    @FXML
    private Button confirmPaymentButton;
    @FXML
    private Button confirmPaymentSummaryButton;

    private final CheckoutService checkoutService = new CheckoutService();
    private final InvoicePdfService invoicePdfService = new InvoicePdfService();
    private final StripeCheckoutService stripeCheckoutService = new StripeCheckoutService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadDraftSummary();
        savePendingCommandeOnScreenLoad();
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

        setPaymentInProgress(true);
        try {
            StripeCheckoutService.StripeCheckoutSession session = stripeCheckoutService.createCheckoutSession(
                    draft,
                    OrderSession.getInstance().getCartItems()
            );
            stripeCheckoutService.openCheckoutPage(session.url());
            AlertUtils.showInfo("Commande #" + draft.getId() + " deja enregistree. Finalisez le paiement dans Stripe.");
            startStripePaymentVerification(session.sessionId());
        } catch (IllegalStateException | IOException | StripeException e) {
            setPaymentInProgress(false);
            AlertUtils.showError("Impossible de lancer Stripe : " + e.getMessage());
        }
    }

    @FXML
    private void printInvoice() {
        Commande draft = OrderSession.getInstance().getDraftCommande();
        if (draft == null) {
            AlertUtils.showError("Les informations client sont manquantes.");
            return;
        }
        if (OrderSession.getInstance().isCartEmpty()) {
            AlertUtils.showError("Aucun article a imprimer dans la facture.");
            return;
        }

        try {
            var pdfPath = invoicePdfService.generateInvoice(
                    draft,
                    OrderSession.getInstance().getCartItems(),
                    OrderSession.getInstance().getCartTotal()
            );
            invoicePdfService.openInvoice(pdfPath);
        } catch (IOException e) {
            AlertUtils.showError("Impossible de generer la facture PDF : " + e.getMessage());
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
            productNameLabel.setText("-");
            return;
        }

        List<CartItem> cartItems = OrderSession.getInstance().getCartItems();
        nomLabel.setText(emptyIfNull(draft.getNom()));
        prenomLabel.setText(emptyIfNull(draft.getPrenom()));
        telephoneLabel.setText(emptyIfNull(draft.getTelephone()));
        adresseLabel.setText(emptyIfNull(draft.getAdresse()));
        productNameLabel.setText(formatProductSummary(cartItems));
        totalLabel.setText(String.format("%.2f TND", OrderSession.getInstance().getCartTotal()));
        articleCountLabel.setText(String.valueOf(OrderSession.getInstance().getTotalItems()));
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

    private void savePendingCommandeOnScreenLoad() {
        Commande draft = OrderSession.getInstance().getDraftCommande();
        if (draft == null || OrderSession.getInstance().isCartEmpty()) {
            return;
        }

        try {
            int commandeId = checkoutService.savePendingPaymentCommande(
                    OrderSession.getInstance(),
                    java.sql.Date.valueOf(java.time.LocalDate.now())
            );
            draft.setId(commandeId);
            draft.setStatut("EN_ATTENTE");
        } catch (SQLException e) {
            setPaymentInProgress(false);
            if (confirmPaymentButton != null) {
                confirmPaymentButton.setDisable(true);
            }
            AlertUtils.showError("Impossible d'enregistrer la commande en attente : " + e.getMessage());
        }
    }

    private String emptyIfNull(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void startStripePaymentVerification(String sessionId) {
        Thread verificationThread = new Thread(() -> {
            int attempts = 0;
            int maxAttempts = 120;

            while (attempts < maxAttempts) {
                try {
                    if (stripeCheckoutService.isPaymentCompleted(sessionId)) {
                        finalizeOrderAfterStripe();
                        return;
                    }
                    if (stripeCheckoutService.isExpiredOrCanceled(sessionId)) {
                        Platform.runLater(() -> {
                            setPaymentInProgress(false);
                            AlertUtils.showError("Le paiement Stripe a ete annule ou la session a expire.");
                        });
                        return;
                    }
                    Thread.sleep(3000);
                    attempts++;
                } catch (StripeException e) {
                    Platform.runLater(() -> {
                        setPaymentInProgress(false);
                        AlertUtils.showError("Erreur Stripe pendant la verification : " + e.getMessage());
                    });
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Platform.runLater(() -> setPaymentInProgress(false));
                    return;
                }
            }

            Platform.runLater(() -> {
                setPaymentInProgress(false);
                AlertUtils.showError("Verification Stripe expiree. Reessayez si le paiement n'a pas ete confirme.");
            });
        });
        verificationThread.setDaemon(true);
        verificationThread.start();
    }

    private void finalizeOrderAfterStripe() {
        try {
            int commandeId = checkoutService.checkout(
                    OrderSession.getInstance(),
                    java.sql.Date.valueOf(java.time.LocalDate.now())
            );
            Platform.runLater(() -> {
                setPaymentInProgress(false);
                AlertUtils.showSuccess("Paiement Stripe confirme. Commande enregistree avec succes. ID commande : " + commandeId);
                SceneNavigator.switchScene(nomLabel, "/main-view.fxml", "Catalogue produits");
            });
        } catch (SQLException e) {
            Platform.runLater(() -> {
                setPaymentInProgress(false);
                AlertUtils.showError("Paiement valide sur Stripe, mais l'enregistrement local a echoue : " + e.getMessage());
            });
        }
    }

    private void setPaymentInProgress(boolean inProgress) {
        if (confirmPaymentButton != null) {
            confirmPaymentButton.setDisable(inProgress);
            confirmPaymentButton.setText(inProgress ? "En attente de Stripe..." : "Payer en ligne Stripe");
        }
        if (confirmPaymentSummaryButton != null) {
            confirmPaymentSummaryButton.setDisable(inProgress);
            confirmPaymentSummaryButton.setText(inProgress ? "En attente de Stripe..." : "Payer maintenant");
        }
    }
}
