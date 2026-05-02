package edu.PROJETPI.services;

import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.entites.Produit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CheckoutServiceTest extends ServiceTestSupport {

    private final CheckoutService checkoutService = new CheckoutService();

    @BeforeEach
    void clearSessionCart() {
        OrderSession.getInstance().clearCart();
    }

    @Test
    void checkoutShouldPersistPaidCommandeLinesAndPayment() throws SQLException {
        prepareDraftCommande();

        int commandeId = checkoutService.checkout(
                OrderSession.getInstance(),
                java.sql.Date.valueOf("2026-04-19")
        );

        assertCommandeStatut(commandeId, "PAYEE");
        assertDeliveryColumns(commandeId, null, null, null, null, null);
        assertTableCount("lignecommande", 1);
        assertTableCount("payment", 1);
        assertProductStock(1, 9);
    }

    @Test
    void savePendingPaymentCommandeShouldBeVisibleBeforeStripeConfirmation() throws SQLException {
        prepareDraftCommande();

        int commandeId = checkoutService.savePendingPaymentCommande(
                OrderSession.getInstance(),
                java.sql.Date.valueOf("2026-04-19")
        );

        assertCommandeStatut(commandeId, "EN_ATTENTE");
        assertTableCount("commande", 1);
        assertTableCount("lignecommande", 1);
        assertTableCount("payment", 0);
        assertProductStock(1, 10);
        assertTrue(new ServiceCommande().readAll().stream().anyMatch(commande ->
                commande.getId() == commandeId
                        && "EN_ATTENTE".equals(commande.getStatut())
                        && "Test".equals(commande.getNom())
        ));
    }

    @Test
    void checkoutAfterPendingPaymentShouldUpdateSameCommandeAndRegisterPayment() throws SQLException {
        prepareDraftCommande();

        int pendingCommandeId = checkoutService.savePendingPaymentCommande(
                OrderSession.getInstance(),
                java.sql.Date.valueOf("2026-04-19")
        );
        int paidCommandeId = checkoutService.checkout(
                OrderSession.getInstance(),
                java.sql.Date.valueOf("2026-04-19")
        );

        assertEquals(pendingCommandeId, paidCommandeId);
        assertCommandeStatut(paidCommandeId, "PAYEE");
        assertTableCount("commande", 1);
        assertTableCount("lignecommande", 1);
        assertTableCount("payment", 1);
        assertProductStock(1, 9);
    }

    @Test
    void checkoutCashOnDeliveryShouldPersistDeliveryCommandeWithoutPayment() throws SQLException {
        prepareDraftCommande();
        Commande draft = OrderSession.getInstance().getDraftCommande();
        draft.setPaysLivraison("Tunisie");
        draft.setGouvernoratLivraison("Gouvernorat Ariana");
        draft.setCodePostalLivraison("2022");
        draft.setAdresseLivraison("Kalaat el-Andalous");
        draft.setDescriptionLivraison("Position GPS: 37.090240, 10.107422");

        int commandeId = checkoutService.checkoutCashOnDelivery(
                OrderSession.getInstance(),
                java.sql.Date.valueOf("2026-04-19")
        );

        assertCommandeStatut(commandeId, "EN_LIVRAISON");
        assertDeliveryColumns(
                commandeId,
                "Tunisie",
                "Gouvernorat Ariana",
                "2022",
                "Kalaat el-Andalous",
                "Position GPS: 37.090240, 10.107422"
        );
        assertTableCount("lignecommande", 1);
        assertTableCount("payment", 0);
        assertProductStock(1, 9);
    }

    @Test
    void checkoutCashOnDeliveryShouldBeVisibleInAdminCommandeList() throws SQLException {
        prepareDraftCommande();
        Commande draft = OrderSession.getInstance().getDraftCommande();
        draft.setPaysLivraison("Tunisie");
        draft.setGouvernoratLivraison("Gouvernorat Ariana");
        draft.setCodePostalLivraison("2022");
        draft.setAdresseLivraison("Kalaat el-Andalous");
        draft.setDescriptionLivraison("Position GPS: 37.090240, 10.107422");

        int commandeId = checkoutService.checkoutCashOnDelivery(
                OrderSession.getInstance(),
                java.sql.Date.valueOf("2026-04-19")
        );

        assertTrue(new ServiceCommande().readAll().stream().anyMatch(commande ->
                commande.getId() == commandeId
                        && "EN_LIVRAISON".equals(commande.getStatut())
                        && "Test".equals(commande.getNom())
                        && "Client".equals(commande.getPrenom())
                        && "Tunisie".equals(commande.getPaysLivraison())
        ));
    }

    @Test
    void checkoutCashOnDeliveryShouldMoveLegacyDeliveryTextToDedicatedColumns() throws SQLException {
        prepareDraftCommande();
        Commande draft = OrderSession.getInstance().getDraftCommande();
        draft.setAdresse("""
                Adresse client

                Livraison
                Pays: Tunisie
                Gouvernorat: Gouvernorat Ariana
                Code postal: 2022
                Adresse: Kalaat el-Andalous
                Description: Position GPS: 37.090240, 10.107422""");

        int commandeId = checkoutService.checkoutCashOnDelivery(
                OrderSession.getInstance(),
                java.sql.Date.valueOf("2026-04-19")
        );

        assertCommandeAdresse(commandeId, "Adresse client");
        assertDeliveryColumns(
                commandeId,
                "Tunisie",
                "Gouvernorat Ariana",
                "2022",
                "Kalaat el-Andalous",
                "Position GPS: 37.090240, 10.107422"
        );
    }

    private void prepareDraftCommande() {
        OrderSession session = OrderSession.getInstance();
        session.clearCart();
        session.addProduct(new Produit(1, "Casque Gaming", 249.0, 10, "Test"), 1);

        Commande commande = sampleCommande();
        commande.setDateCommande(java.sql.Date.valueOf("2026-04-19"));
        commande.setTotal(session.getCartTotal());
        commande.setStatut("EN_ATTENTE_PAIEMENT");
        session.setDraftCommande(commande);
    }

    private void assertCommandeStatut(int commandeId, String expectedStatut) throws SQLException {
        try (var rs = connection.createStatement().executeQuery(
                "SELECT statut FROM commande WHERE id = " + commandeId
        )) {
            rs.next();
            assertEquals(expectedStatut, rs.getString("statut"));
        }
    }

    private void assertTableCount(String tableName, int expectedCount) throws SQLException {
        try (var rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            rs.next();
            assertEquals(expectedCount, rs.getInt(1));
        }
    }

    private void assertProductStock(int produitId, int expectedStock) throws SQLException {
        try (var rs = connection.createStatement().executeQuery(
                "SELECT stock FROM produit WHERE id = " + produitId
        )) {
            rs.next();
            assertEquals(expectedStock, rs.getInt("stock"));
        }
    }

    private void assertDeliveryColumns(
            int commandeId,
            String pays,
            String gouvernorat,
            String codePostal,
            String adresse,
            String description
    ) throws SQLException {
        try (var rs = connection.createStatement().executeQuery(
                "SELECT pays, gouvernerat, code_postal, adresse_detail FROM commande WHERE id = " + commandeId
        )) {
            rs.next();
            assertEquals(pays, rs.getString("pays"));
            assertEquals(gouvernorat, rs.getString("gouvernerat"));
            assertEquals(codePostal, rs.getString("code_postal"));
            Commande commande = new Commande();
            edu.PROJETPI.services.CommandeDatabaseMapper.populateDeliveryFields(commande, rs.getString("adresse_detail"));
            assertEquals(adresse, commande.getAdresseLivraison());
            assertEquals(description, commande.getDescriptionLivraison());
        }
    }

    private void assertCommandeAdresse(int commandeId, String expectedAdresse) throws SQLException {
        try (var rs = connection.createStatement().executeQuery(
                "SELECT adresse FROM commande WHERE id = " + commandeId
        )) {
            rs.next();
            assertEquals(expectedAdresse, rs.getString("adresse"));
        }
    }
}
