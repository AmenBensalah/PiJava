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
     * Appelle l'API Python pour obtenir les recommandations avancées et statistiques.
     */
    public Map<String, Object> getRecommandationsAvancees() {
        Map<String, Object> result = new HashMap<>();
        try {
            java.net.URL url = new java.net.URL("http://localhost:5000/api/recommendations");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            
            if (conn.getResponseCode() == 200) {
                java.util.Scanner scanner = new java.util.Scanner(conn.getInputStream(), "UTF-8");
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();
                
                // Parsing manuel basique (sans librairie externe)
                double ca = parseDoubleFromJson(response, "\"chiffre_affaire_total\":");
                int ventes = (int) parseDoubleFromJson(response, "\"total_ventes\":");
                String dernierAchat = extractStringFromJson(response, "\"dernier_achat\":");
                String statut = extractStringFromJson(response, "\"statut_modele\":");
                
                result.put("ca_total", ca);
                result.put("ventes_totales", ventes);
                result.put("dernier_achat", dernierAchat);
                result.put("statut", statut);
                
                // Parser les produits recommandés
                Map<Produit, Map<String, String>> recommendations = new HashMap<>();
                List<Produit> tousLesProduits = produitService.recuperer();
                
                String[] items = response.split("\\{\"ca_produit\"");
                for (int i = 1; i < items.length; i++) {
                    int id = (int) parseDoubleFromJson(items[i], "\"id\":");
                    int ventesProd = (int) parseDoubleFromJson(items[i], "\"ventes_totales\":");
                    String tendance = extractStringFromJson(items[i], "\"tendance_ia\":");
                    int prediction = (int) parseDoubleFromJson(items[i], "\"prediction_ventes_mois_prochain\":");
                    
                    for (Produit p : tousLesProduits) {
                        if (p.getId() == id) {
                            Map<String, String> stats = new HashMap<>();
                            stats.put("ventes", String.valueOf(ventesProd));
                            stats.put("tendance", tendance);
                            stats.put("prediction", String.valueOf(prediction));
                            recommendations.put(p, stats);
                            break;
                        }
                    }
                }
                result.put("produits", recommendations);
                return result;
            }
        } catch (Exception e) {
            System.out.println("API Python indisponible, fallback manuel... " + e.getMessage());
        }
        
        // --- FALLBACK SI L'API PYTHON EST ÉTEINTE ---
        result.put("ca_total", 0.0);
        result.put("ventes_totales", 0);
        result.put("dernier_achat", "N/A");
        result.put("statut", "FALLBACK SQL");
        
        Map<Produit, Map<String, String>> fallbackMap = new HashMap<>();
        try {
            List<Produit> tousLesProduits = produitService.recuperer();
            // Initialisation avec 0 pour tous les produits
            for (Produit p : tousLesProduits) {
                Map<String, String> stats = new HashMap<>();
                stats.put("ventes", "0");
                stats.put("tendance", "Stable →");
                stats.put("prediction", "0");
                fallbackMap.put(p, stats);
            }
            
            String sql = "SELECT produit_id, SUM(quantite) as total_vendu, SUM(prix_total) as ca FROM commande_boutique GROUP BY produit_id";
            try (PreparedStatement ps = connection.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                 
                double caGlobal = 0;
                int ventesGlobales = 0;
                
                while (rs.next()) {
                    int pid = rs.getInt("produit_id");
                    int totalVendu = rs.getInt("total_vendu");
                    caGlobal += rs.getDouble("ca");
                    ventesGlobales += totalVendu;
                    
                    for (Produit p : tousLesProduits) {
                        if (p.getId() == pid) {
                            Map<String, String> stats = fallbackMap.get(p);
                            stats.put("ventes", String.valueOf(totalVendu));
                            stats.put("tendance", "Forte demande 🚀");
                            stats.put("prediction", String.valueOf((int)(totalVendu * 1.2)));
                            break;
                        }
                    }
                }
                result.put("ca_total", caGlobal);
                result.put("ventes_totales", ventesGlobales);
            }
        } catch (Exception e) {}
        
        result.put("produits", fallbackMap);
        return result;
    }
    
    private double parseDoubleFromJson(String json, String key) {
        try {
            int idx = json.indexOf(key);
            if (idx == -1) return 0;
            idx += key.length();
            int end = json.indexOf(",", idx);
            if (end == -1) end = json.indexOf("}", idx);
            String val = json.substring(idx, end).trim();
            return Double.parseDouble(val);
        } catch (Exception e) { return 0; }
    }
    
    private String extractStringFromJson(String json, String key) {
        try {
            int idx = json.indexOf(key);
            if (idx == -1) return "";
            idx += key.length();
            int start = json.indexOf("\"", idx) + 1;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) { return ""; }
    }
}
