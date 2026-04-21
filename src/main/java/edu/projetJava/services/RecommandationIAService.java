package edu.projetJava.services;

import edu.projetJava.models.Produit;
import edu.projetJava.tools.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RecommandationIAService {
    
    private ProduitService produitService = new ProduitService();
    private Connection connection;

    public RecommandationIAService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    /**
     * Retourne une map associant un produit à son nombre réel de ventes.
     */
    public Map<Produit, Integer> getRecommandationsReelles() throws SQLException {
        List<Produit> tousLesProduits = produitService.recuperer();
        Map<Produit, Integer> ventesParProduit = new HashMap<>();

        // 1. Initialiser tous les produits à 0 ventes
        for (Produit p : tousLesProduits) {
            ventesParProduit.put(p, 0);
        }

        // 2. Récupérer les ventes réelles depuis la base de données
        String sql = "SELECT produit_id, SUM(quantite) as total_vendu FROM commande_boutique GROUP BY produit_id";
        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                int pid = rs.getInt("produit_id");
                int totalVendu = rs.getInt("total_vendu");
                
                // Trouver le produit correspondant
                for (Produit p : tousLesProduits) {
                    if (p.getId() == pid) {
                        ventesParProduit.put(p, totalVendu);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // Si la table n'existe pas encore ou est vide, on ignore
        }

        return ventesParProduit;
    }
}
