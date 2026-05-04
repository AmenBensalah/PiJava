package edu.PROJETPI.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import edu.PROJETPI.entites.CartItem;
import edu.PROJETPI.entites.Commande;

import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

public class StripeCheckoutService {

    public StripeCheckoutSession createCheckoutSession(Commande commande, List<CartItem> articles) throws StripeException {
        ensureConfigured();
        Stripe.apiKey = StripeConfig.getSecretKey();
        String currency = StripeConfig.getCurrency().toLowerCase();

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(StripeConfig.getSuccessUrl())
                .setCancelUrl(StripeConfig.getCancelUrl())
                .putMetadata("client_nom", safe(commande.getNom()))
                .putMetadata("client_prenom", safe(commande.getPrenom()))
                .putMetadata("telephone", safe(commande.getTelephone()))
                .putMetadata("adresse", safe(commande.getAdresse()));

        for (CartItem article : articles) {
            paramsBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long) article.getQuantite())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency(currency)
                                            .setUnitAmountDecimal(
                                                    BigDecimal.valueOf(article.getPrixUnitaire()).movePointRight(2)
                                            )
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(safe(article.getNomProduit()))
                                                            .build()
                                            )
                                            .build()
                            )
                            .build()
            );
        }

        Session session = Session.create(paramsBuilder.build());
        return new StripeCheckoutSession(session.getId(), session.getUrl());
    }

    public boolean isPaymentCompleted(String sessionId) throws StripeException {
        ensureConfigured();
        Stripe.apiKey = StripeConfig.getSecretKey();
        Session session = Session.retrieve(sessionId);
        return "paid".equalsIgnoreCase(session.getPaymentStatus())
                || "complete".equalsIgnoreCase(session.getStatus());
    }

    public boolean isExpiredOrCanceled(String sessionId) throws StripeException {
        ensureConfigured();
        Stripe.apiKey = StripeConfig.getSecretKey();
        Session session = Session.retrieve(sessionId);
        return "expired".equalsIgnoreCase(session.getStatus());
    }

    public void openCheckoutPage(String url) throws IOException {
        if (!Desktop.isDesktopSupported()) {
            throw new IOException("Ouverture automatique de Stripe non supportee sur cette machine.");
        }
        Desktop.getDesktop().browse(URI.create(url));
    }

    private void ensureConfigured() {
        if (!StripeConfig.isConfigured()) {
            throw new IllegalStateException("Stripe n'est pas configure. Renseignez STRIPE_SECRET_KEY ou stripe.properties.");
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    public record StripeCheckoutSession(String sessionId, String url) {
    }
}
