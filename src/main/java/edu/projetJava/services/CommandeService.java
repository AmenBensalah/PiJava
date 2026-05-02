package edu.projetJava.services;

import edu.projetJava.models.Commande;
import edu.projetJava.tools.MyDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CommandeService {
    private Connection connection;

    public CommandeService() {
        connection = MyDatabase.getInstance().getConnection();
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS commande_boutique (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "produit_id INT," +
                "nom_client VARCHAR(100)," +
                "email_client VARCHAR(100)," +
                "methode_paiement VARCHAR(50)," +
                "quantite INT," +
                "prix_total DOUBLE," +
                "date_commande VARCHAR(50)" +
                ")";
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Erreur lors de la création de la table commande_boutique : " + e.getMessage());
        }
    }

    public void ajouter(Commande commande) throws SQLException {
        String req = "INSERT INTO commande_boutique (produit_id, nom_client, email_client, methode_paiement, quantite, prix_total, date_commande) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setInt(1, commande.getProduitId());
            ps.setString(2, commande.getNomClient());
            ps.setString(3, commande.getEmailClient());
            ps.setString(4, commande.getMethodePaiement());
            ps.setInt(5, commande.getQuantite());
            ps.setDouble(6, commande.getPrixTotal());
            ps.setString(7, commande.getDateCommande());
            ps.executeUpdate();
            System.out.println("Commande ajoutée avec succès !");
        }
    }

    public boolean aDejaCommande(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String req = "SELECT COUNT(*) FROM commande_boutique WHERE email_client = ?";
        try (PreparedStatement ps = connection.prepareStatement(req)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
