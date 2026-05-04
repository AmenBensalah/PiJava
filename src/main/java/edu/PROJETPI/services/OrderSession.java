package edu.PROJETPI.services;

import edu.PROJETPI.entites.CartItem;
import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.entites.Produit;
import edu.PROJETPI.tools.MyConexion;
import edu.ProjetPI.controllers.DashboardSession;
import edu.ProjetPI.entities.User;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

public class OrderSession {
    public enum CheckoutMode {
        STRIPE,
        CASH_ON_DELIVERY
    }

    private static final OrderSession INSTANCE = new OrderSession();

    private final List<CartItem> cartItems = new ArrayList<>();
    private final Connection cnx = MyConexion.getInstance().getConnection();
    private Commande draftCommande;
    private Commande confirmedCommande;
    private List<CartItem> confirmedCartItems = new ArrayList<>();
    private double confirmedCartTotal;
    private int confirmedCommandeId;
    private CheckoutMode checkoutMode = CheckoutMode.STRIPE;

    private OrderSession() {
    }

    public static OrderSession getInstance() {
        return INSTANCE;
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public void addProduct(Produit produit, int quantite) {
        CartItem existing = findItem(produit.getId());
        int requestedQuantity = quantite + (existing == null ? 0 : existing.getQuantite());
        validateStockLimit(produit.getId(), requestedQuantity);
        if (existing != null) {
            existing.setQuantite(requestedQuantity);
            syncCartToDatabase();
            return;
        }
        cartItems.add(new CartItem(produit.getId(), produit.getNom(), quantite, produit.getPrix()));
        syncCartToDatabase();
    }

    public void updateQuantity(int produitId, int quantite) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantite doit etre positive.");
        }

        CartItem item = findItem(produitId);
        if (item != null) {
            validateStockLimit(produitId, quantite);
            item.setQuantite(quantite);
            syncCartToDatabase();
        }
    }

    public void removeProduct(int produitId) {
        cartItems.removeIf(item -> item.getProduitId() == produitId);
        syncCartToDatabase();
    }

    public void clearCart() {
        deleteActiveDatabaseCart();
        cartItems.clear();
        draftCommande = null;
        checkoutMode = CheckoutMode.STRIPE;
    }

    public boolean isCartEmpty() {
        return cartItems.isEmpty();
    }

    public int getTotalItems() {
        return cartItems.stream().mapToInt(CartItem::getQuantite).sum();
    }

    public double getCartTotal() {
        return cartItems.stream().mapToDouble(CartItem::getSousTotal).sum();
    }

    public Commande getDraftCommande() {
        return draftCommande;
    }

    public void setDraftCommande(Commande draftCommande) {
        this.draftCommande = draftCommande;
    }

    public CheckoutMode getCheckoutMode() {
        return checkoutMode;
    }

    public void setCheckoutMode(CheckoutMode checkoutMode) {
        this.checkoutMode = checkoutMode == null ? CheckoutMode.STRIPE : checkoutMode;
    }

    public void resetAfterCheckout() {
        cartItems.clear();
        draftCommande = null;
        checkoutMode = CheckoutMode.STRIPE;
    }

    public void reloadCartForCurrentUser() {
        cartItems.clear();
        draftCommande = null;
        checkoutMode = CheckoutMode.STRIPE;

        int userId = currentUserId();
        if (userId <= 0) {
            return;
        }

        String findCommande = """
                SELECT *
                FROM commande
                WHERE user_id = ?
                  AND UPPER(statut) IN ('DRAFT', 'EN_ATTENTE', 'EN_ATTENTE_PAIEMENT')
                ORDER BY id DESC
                LIMIT 1
                """;
        try (PreparedStatement pst = cnx.prepareStatement(findCommande)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) {
                    return;
                }

                Commande commande = new Commande();
                commande.setId(rs.getInt("id"));
                commande.setClientId(userId);
                commande.setNom(rs.getString("nom"));
                commande.setPrenom(rs.getString("prenom"));
                commande.setAdresse(rs.getString("adresse"));
                commande.setTelephone(CommandeDatabaseMapper.fromDatabaseTelephone(rs, "numtel"));
                commande.setStatut(rs.getString("statut"));
                commande.setDateCommande(Date.valueOf(java.time.LocalDate.now()));
                draftCommande = commande;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible de charger le panier depuis la base.", e);
        }

        loadCartLinesFromDatabase();
    }

    public void rememberConfirmedOrder(Commande commande, List<CartItem> items, double total, int commandeId) {
        confirmedCommande = commande;
        confirmedCartItems = items == null ? new ArrayList<>() : new ArrayList<>(items);
        confirmedCartTotal = total;
        confirmedCommandeId = commandeId;
    }

    public Commande getConfirmedCommande() {
        return confirmedCommande;
    }

    public List<CartItem> getConfirmedCartItems() {
        return new ArrayList<>(confirmedCartItems);
    }

    public double getConfirmedCartTotal() {
        return confirmedCartTotal;
    }

    public int getConfirmedCommandeId() {
        return confirmedCommandeId;
    }

    private CartItem findItem(int produitId) {
        return cartItems.stream()
                .filter(item -> item.getProduitId() == produitId)
                .findFirst()
                .orElse(null);
    }

    private void validateStockLimit(int produitId, int requestedQuantity) {
        String query = "SELECT nom, stock FROM produit WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, produitId);
            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) {
                    return;
                }

                int stock = rs.getInt("stock");
                if (requestedQuantity > stock) {
                    String nom = rs.getString("nom");
                    throw new IllegalArgumentException("Stock insuffisant pour " + nom + " : stock disponible " + stock + ".");
                }
            }
        } catch (SQLException e) {
            throw new IllegalArgumentException("Impossible de verifier le stock du produit : " + e.getMessage(), e);
        }
    }

    private void syncCartToDatabase() {
        int userId = currentUserId();
        if (userId <= 0) {
            return;
        }

        try {
            cnx.setAutoCommit(false);
            int commandeId = ensureActiveDatabaseCart(userId);

            try (PreparedStatement deleteLines = cnx.prepareStatement("DELETE FROM lignecommande WHERE commandeId = ?")) {
                deleteLines.setInt(1, commandeId);
                deleteLines.executeUpdate();
            }

            if (!cartItems.isEmpty()) {
                String insertLine = "INSERT INTO lignecommande (commandeId, produitId, quantite, prixUnitaire) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstLine = cnx.prepareStatement(insertLine)) {
                    for (CartItem item : cartItems) {
                        pstLine.setInt(1, commandeId);
                        pstLine.setInt(2, item.getProduitId());
                        pstLine.setInt(3, item.getQuantite());
                        pstLine.setDouble(4, item.getPrixUnitaire());
                        pstLine.addBatch();
                    }
                    pstLine.executeBatch();
                }
            }

            updateDraftQuantite(commandeId);
            cnx.commit();
        } catch (SQLException e) {
            rollbackQuietly();
            throw new IllegalArgumentException("Impossible d'enregistrer le panier dans la base : " + e.getMessage(), e);
        } finally {
            restoreAutoCommitQuietly();
        }
    }

    private int ensureActiveDatabaseCart(int userId) throws SQLException {
        if (draftCommande != null && draftCommande.getId() > 0 && isActiveCartStatus(draftCommande.getStatut())) {
            return draftCommande.getId();
        }

        String findCommande = """
                SELECT id, statut
                FROM commande
                WHERE user_id = ?
                  AND UPPER(statut) IN ('DRAFT', 'EN_ATTENTE', 'EN_ATTENTE_PAIEMENT')
                ORDER BY id DESC
                LIMIT 1
                """;
        try (PreparedStatement pstFind = cnx.prepareStatement(findCommande)) {
            pstFind.setInt(1, userId);
            try (ResultSet rs = pstFind.executeQuery()) {
                if (rs.next()) {
                    Commande commande = new Commande(Date.valueOf(java.time.LocalDate.now()), getCartTotal(), userId);
                    commande.setId(rs.getInt("id"));
                    commande.setStatut(rs.getString("statut"));
                    draftCommande = commande;
                    return commande.getId();
                }
            }
        }

        String insertCommande = """
                INSERT INTO commande (quantite, statut, user_id, identity_key)
                VALUES (?, 'DRAFT', ?, ?)
                """;
        try (PreparedStatement pstInsert = cnx.prepareStatement(insertCommande, Statement.RETURN_GENERATED_KEYS)) {
            pstInsert.setInt(1, getTotalItems());
            pstInsert.setInt(2, userId);
            pstInsert.setString(3, "PI_CMD|" + java.time.LocalDate.now() + "|" + String.format(java.util.Locale.US, "%.2f", getCartTotal()));
            pstInsert.executeUpdate();
            try (ResultSet keys = pstInsert.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Impossible de creer le panier en base.");
                }

                Commande commande = new Commande(Date.valueOf(java.time.LocalDate.now()), getCartTotal(), userId);
                commande.setId(keys.getInt(1));
                commande.setStatut("DRAFT");
                draftCommande = commande;
                return commande.getId();
            }
        }
    }

    private void loadCartLinesFromDatabase() {
        if (draftCommande == null || draftCommande.getId() <= 0) {
            return;
        }

        String query = """
                SELECT lc.produitId, lc.quantite, lc.prixUnitaire, COALESCE(p.nom, CONCAT('Produit ', lc.produitId)) AS nom
                FROM lignecommande lc
                LEFT JOIN produit p ON p.id = lc.produitId
                WHERE lc.commandeId = ?
                ORDER BY lc.id
                """;
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, draftCommande.getId());
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    cartItems.add(new CartItem(
                            rs.getInt("produitId"),
                            rs.getString("nom"),
                            rs.getInt("quantite"),
                            rs.getDouble("prixUnitaire")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible de charger les lignes du panier.", e);
        }
    }

    private void deleteActiveDatabaseCart() {
        if (draftCommande == null || draftCommande.getId() <= 0 || !isActiveCartStatus(draftCommande.getStatut())) {
            return;
        }

        try {
            cnx.setAutoCommit(false);
            try (PreparedStatement deleteLines = cnx.prepareStatement("DELETE FROM lignecommande WHERE commandeId = ?");
                 PreparedStatement deleteCommande = cnx.prepareStatement("DELETE FROM commande WHERE id = ?")) {
                deleteLines.setInt(1, draftCommande.getId());
                deleteLines.executeUpdate();
                deleteCommande.setInt(1, draftCommande.getId());
                deleteCommande.executeUpdate();
            }
            cnx.commit();
        } catch (SQLException e) {
            rollbackQuietly();
            throw new IllegalArgumentException("Impossible de vider le panier en base : " + e.getMessage(), e);
        } finally {
            restoreAutoCommitQuietly();
        }
    }

    private void updateDraftQuantite(int commandeId) throws SQLException {
        try (PreparedStatement pst = cnx.prepareStatement("UPDATE commande SET quantite = ? WHERE id = ?")) {
            pst.setInt(1, getTotalItems());
            pst.setInt(2, commandeId);
            pst.executeUpdate();
        }
    }

    private boolean isActiveCartStatus(String statut) {
        if (statut == null) {
            return false;
        }

        String normalized = statut.trim().toUpperCase();
        return "DRAFT".equals(normalized)
                || "EN_ATTENTE".equals(normalized)
                || "EN_ATTENTE_PAIEMENT".equals(normalized);
    }

    private int currentUserId() {
        User currentUser = DashboardSession.getCurrentUser();
        return currentUser == null ? 0 : currentUser.getId();
    }

    private void rollbackQuietly() {
        try {
            cnx.rollback();
        } catch (SQLException ignored) {
        }
    }

    private void restoreAutoCommitQuietly() {
        try {
            cnx.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }
}
