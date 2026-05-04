package edu.PROJETPI.services;

import edu.PROJETPI.entites.Produit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderSessionDatabaseCartTest extends ServiceTestSupport {

    private static final int TEST_USER_ID = 61;

    @BeforeEach
    void prepareSession() {
        clearDashboardSession();
        OrderSession.getInstance().resetAfterCheckout();
        setDashboardUser(TEST_USER_ID);
    }

    @Test
    void addProductShouldPersistCartLineInDatabaseImmediately() throws SQLException {
        OrderSession session = OrderSession.getInstance();

        session.addProduct(new Produit(1, "Casque Gaming", 249.0, 10, "Test"), 2);

        int commandeId = assertOneDraftCommandeForUser();
        assertCartLine(commandeId, 1, 2, 249.0);
    }

    @Test
    void loginReloadShouldRestoreCartFromDatabaseForSameUser() throws SQLException {
        OrderSession session = OrderSession.getInstance();
        session.addProduct(new Produit(1, "Casque Gaming", 249.0, 10, "Test"), 2);
        int commandeId = assertOneDraftCommandeForUser();

        clearDashboardSession();
        session.resetAfterCheckout();
        setDashboardUser(TEST_USER_ID);
        session.reloadCartForCurrentUser();

        assertEquals(1, session.getCartItems().size());
        assertEquals(1, session.getCartItems().get(0).getProduitId());
        assertEquals(2, session.getCartItems().get(0).getQuantite());
        assertEquals("Casque Gaming", session.getCartItems().get(0).getNomProduit());
        assertEquals(commandeId, session.getDraftCommande().getId());
    }

    private int assertOneDraftCommandeForUser() throws SQLException {
        try (var pst = connection.prepareStatement("""
                SELECT id, quantite, statut
                FROM commande
                WHERE user_id = ?
                """)) {
            pst.setInt(1, TEST_USER_ID);
            try (var rs = pst.executeQuery()) {
                rs.next();
                int commandeId = rs.getInt("id");
                assertEquals(2, rs.getInt("quantite"));
                assertEquals("DRAFT", rs.getString("statut"));
                return commandeId;
            }
        }
    }

    private void assertCartLine(int commandeId, int produitId, int quantite, double prixUnitaire) throws SQLException {
        try (var pst = connection.prepareStatement("""
                SELECT produitId, quantite, prixUnitaire
                FROM lignecommande
                WHERE commandeId = ?
                """)) {
            pst.setInt(1, commandeId);
            try (var rs = pst.executeQuery()) {
                rs.next();
                assertEquals(produitId, rs.getInt("produitId"));
                assertEquals(quantite, rs.getInt("quantite"));
                assertEquals(prixUnitaire, rs.getDouble("prixUnitaire"));
            }
        }
    }

    private static void setDashboardUser(int userId) {
        try {
            Class<?> userClass = Class.forName("edu.ProjetPI.entities.User");
            Object user = userClass
                    .getConstructor(int.class, String.class, String.class, String.class, String.class, String.class)
                    .newInstance(userId, "Aysser Test", "aysser", "aysser@test.tn", "secret", "ROLE_USER");
            Class<?> sessionClass = Class.forName("edu.ProjetPI.controllers.DashboardSession");
            sessionClass.getMethod("setCurrentUser", userClass).invoke(null, user);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException
                 | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Impossible de preparer la session utilisateur de test.", e);
        }
    }

    private static void clearDashboardSession() {
        try {
            Class.forName("edu.ProjetPI.controllers.DashboardSession")
                    .getMethod("clear")
                    .invoke(null);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Impossible de nettoyer la session utilisateur de test.", e);
        }
    }
}
