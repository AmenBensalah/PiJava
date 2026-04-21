package edu.PROJETPI.services;

import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.interfaces.IServiceCommande;
import edu.PROJETPI.tools.MyConexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ServiceCommande implements IServiceCommande {
    private final Connection cnx = MyConexion.getInstance().getConnection();

    @Override
    public void add(Commande commande) throws SQLException {
        String query = "INSERT INTO commande (dateCommande, total, clientId, statut, nom, prenom, telephone, adresse, paysLivraison, gouvernoratLivraison, codePostalLivraison, adresseLivraison, descriptionLivraison) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            fillCommandeStatement(pst, commande);
            pst.executeUpdate();
        }
    }

    public int addAndReturnId(Commande commande) throws SQLException {
        String query = "INSERT INTO commande (dateCommande, total, clientId, statut, nom, prenom, telephone, adresse, paysLivraison, gouvernoratLivraison, codePostalLivraison, adresseLivraison, descriptionLivraison) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            fillCommandeStatement(pst, commande);
            pst.executeUpdate();

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Impossible de recuperer l'ID de la commande.");
                }
                return generatedKeys.getInt(1);
            }
        }
    }

    @Override
    public void update(Commande commande) throws SQLException {
        String query = "UPDATE commande SET dateCommande = ?, total = ?, clientId = ?, statut = ?, nom = ?, prenom = ?, telephone = ?, adresse = ?, paysLivraison = ?, gouvernoratLivraison = ?, codePostalLivraison = ?, adresseLivraison = ?, descriptionLivraison = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            fillCommandeStatement(pst, commande);
            pst.setInt(14, commande.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String deleteLignes = "DELETE FROM lignecommande WHERE commandeId = ?";
        String deletePayments = "DELETE FROM payment WHERE commandeId = ?";
        String deleteCommande = "DELETE FROM commande WHERE id = ?";

        try {
            cnx.setAutoCommit(false);

            try (PreparedStatement pstLignes = cnx.prepareStatement(deleteLignes);
                 PreparedStatement pstPayments = cnx.prepareStatement(deletePayments);
                 PreparedStatement pstCommande = cnx.prepareStatement(deleteCommande)) {
                pstLignes.setInt(1, id);
                pstLignes.executeUpdate();

                pstPayments.setInt(1, id);
                pstPayments.executeUpdate();

                pstCommande.setInt(1, id);
                pstCommande.executeUpdate();
            }

            cnx.commit();
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(true);
        }
    }

    @Override
    public List<Commande> readAll() throws SQLException {
        List<Commande> commandes = new ArrayList<>();
        String query = "SELECT * FROM commande ORDER BY id DESC";

        try (PreparedStatement pst = cnx.prepareStatement(query);
            ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Commande commande = new Commande(
                        rs.getInt("id"),
                        rs.getDate("dateCommande"),
                        rs.getDouble("total"),
                        rs.getInt("clientId"),
                        rs.getString("statut"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("telephone"),
                        rs.getString("adresse")
                );
                commande.setPaysLivraison(rs.getString("paysLivraison"));
                commande.setGouvernoratLivraison(rs.getString("gouvernoratLivraison"));
                commande.setCodePostalLivraison(rs.getString("codePostalLivraison"));
                commande.setAdresseLivraison(rs.getString("adresseLivraison"));
                commande.setDescriptionLivraison(rs.getString("descriptionLivraison"));
                commandes.add(commande);
            }
        }

        return commandes;
    }

    private void fillCommandeStatement(PreparedStatement pst, Commande commande) throws SQLException {
        pst.setDate(1, new Date(commande.getDateCommande().getTime()));
        pst.setDouble(2, commande.getTotal());
        pst.setInt(3, commande.getClientId());
        pst.setString(4, commande.getStatut());
        pst.setString(5, commande.getNom());
        pst.setString(6, commande.getPrenom());
        pst.setString(7, commande.getTelephone());
        pst.setString(8, commande.getAdresse());
        pst.setString(9, commande.getPaysLivraison());
        pst.setString(10, commande.getGouvernoratLivraison());
        pst.setString(11, commande.getCodePostalLivraison());
        pst.setString(12, commande.getAdresseLivraison());
        pst.setString(13, commande.getDescriptionLivraison());
    }
}
