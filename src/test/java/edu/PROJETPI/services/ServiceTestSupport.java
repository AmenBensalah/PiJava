package edu.PROJETPI.services;

import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.tools.MyConexion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

abstract class ServiceTestSupport {

    protected Connection connection;

    @BeforeAll
    static void initializeDatabase() {
        System.setProperty("projetpi.db", "projetpi_test");
        MyConexion.initDatabase();
    }

    @BeforeEach
    void resetDatabase() throws SQLException {
        connection = MyConexion.getInstance().getConnection();
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM payment");
            statement.executeUpdate("DELETE FROM lignecommande");
            statement.executeUpdate("DELETE FROM commande");
            statement.executeUpdate("ALTER TABLE payment AUTO_INCREMENT = 1");
            statement.executeUpdate("ALTER TABLE lignecommande AUTO_INCREMENT = 1");
            statement.executeUpdate("ALTER TABLE commande AUTO_INCREMENT = 1");
        }
    }

    protected int insertCommande(Commande commande) throws SQLException {
        String query = "INSERT INTO commande (dateCommande, total, clientId, statut, nom, prenom, numtel, adresse) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setDate(1, new java.sql.Date(commande.getDateCommande().getTime()));
            pst.setDouble(2, commande.getTotal());
            pst.setInt(3, commande.getClientId());
            pst.setString(4, commande.getStatut());
            pst.setString(5, commande.getNom());
            pst.setString(6, commande.getPrenom());
            pst.setString(7, commande.getTelephone());
            pst.setString(8, commande.getAdresse());
            pst.executeUpdate();
            try (var generatedKeys = pst.getGeneratedKeys()) {
                generatedKeys.next();
                return generatedKeys.getInt(1);
            }
        }
    }

    protected Commande sampleCommande() {
        Commande commande = new Commande(java.sql.Date.valueOf("2026-04-11"), 199.90, 7);
        commande.setStatut("EN_ATTENTE");
        commande.setNom("Test");
        commande.setPrenom("Client");
        commande.setTelephone("12345678");
        commande.setAdresse("Adresse test");
        return commande;
    }
}
