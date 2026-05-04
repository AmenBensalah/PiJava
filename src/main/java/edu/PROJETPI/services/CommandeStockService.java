package edu.PROJETPI.services;

import edu.PROJETPI.entites.CartItem;
import edu.PROJETPI.entites.LigneCommande;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CommandeStockService {

    private CommandeStockService() {
    }

    static boolean impactsStock(String statut) {
        if (statut == null) {
            return false;
        }

        String normalized = statut.trim().toUpperCase();
        return "PAYEE".equals(normalized)
                || "PAID".equals(normalized)
                || "EN_LIVRAISON".equals(normalized);
    }

    static void decrementForCart(Connection cnx, List<CartItem> items) throws SQLException {
        Map<Integer, Integer> quantitiesByProduct = new LinkedHashMap<>();
        for (CartItem item : items) {
            quantitiesByProduct.merge(item.getProduitId(), item.getQuantite(), Integer::sum);
        }
        decrement(cnx, quantitiesByProduct);
    }

    static void decrementForCommande(Connection cnx, int commandeId) throws SQLException {
        decrement(cnx, readQuantitiesByCommande(cnx, commandeId));
    }

    static void restoreForCommande(Connection cnx, int commandeId) throws SQLException {
        restore(cnx, readQuantitiesByCommande(cnx, commandeId));
    }

    static void restoreForLignes(Connection cnx, List<LigneCommande> lignes) throws SQLException {
        Map<Integer, Integer> quantitiesByProduct = new LinkedHashMap<>();
        for (LigneCommande ligne : lignes) {
            quantitiesByProduct.merge(ligne.getProduitId(), ligne.getQuantite(), Integer::sum);
        }
        restore(cnx, quantitiesByProduct);
    }

    private static Map<Integer, Integer> readQuantitiesByCommande(Connection cnx, int commandeId) throws SQLException {
        String query = """
                SELECT produitId, SUM(quantite) AS total_quantite
                FROM lignecommande
                WHERE commandeId = ?
                GROUP BY produitId
                """;
        Map<Integer, Integer> quantitiesByProduct = new LinkedHashMap<>();
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, commandeId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    quantitiesByProduct.put(rs.getInt("produitId"), rs.getInt("total_quantite"));
                }
            }
        }
        return quantitiesByProduct;
    }

    private static void decrement(Connection cnx, Map<Integer, Integer> quantitiesByProduct) throws SQLException {
        String selectStock = "SELECT nom, stock FROM produit WHERE id = ? FOR UPDATE";
        String updateStock = """
                UPDATE produit
                SET stock = stock - ?,
                    statut = CASE WHEN stock - ? <= 0 THEN 'RUPTURE' ELSE statut END
                WHERE id = ?
                """;

        for (Map.Entry<Integer, Integer> entry : quantitiesByProduct.entrySet()) {
            int produitId = entry.getKey();
            int quantite = entry.getValue();
            if (quantite <= 0) {
                continue;
            }

            try (PreparedStatement pstSelect = cnx.prepareStatement(selectStock)) {
                pstSelect.setInt(1, produitId);
                try (ResultSet rs = pstSelect.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Produit introuvable pour la commande : ID " + produitId);
                    }

                    int stock = rs.getInt("stock");
                    if (stock < quantite) {
                        String nom = rs.getString("nom");
                        throw new SQLException("Stock insuffisant pour " + nom + " : stock " + stock + ", demande " + quantite + ".");
                    }
                }
            }

            try (PreparedStatement pstUpdate = cnx.prepareStatement(updateStock)) {
                pstUpdate.setInt(1, quantite);
                pstUpdate.setInt(2, quantite);
                pstUpdate.setInt(3, produitId);
                pstUpdate.executeUpdate();
            }
        }
    }

    private static void restore(Connection cnx, Map<Integer, Integer> quantitiesByProduct) throws SQLException {
        String updateStock = """
                UPDATE produit
                SET stock = stock + ?,
                    statut = CASE WHEN stock + ? > 0 AND UPPER(COALESCE(statut, '')) = 'RUPTURE' THEN 'DISPONIBLE' ELSE statut END
                WHERE id = ?
                """;

        for (Map.Entry<Integer, Integer> entry : quantitiesByProduct.entrySet()) {
            int quantite = entry.getValue();
            if (quantite <= 0) {
                continue;
            }

            try (PreparedStatement pstUpdate = cnx.prepareStatement(updateStock)) {
                pstUpdate.setInt(1, quantite);
                pstUpdate.setInt(2, quantite);
                pstUpdate.setInt(3, entry.getKey());
                pstUpdate.executeUpdate();
            }
        }
    }
}
