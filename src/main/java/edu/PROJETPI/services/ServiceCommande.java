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
        String query = "INSERT INTO commande (nom, prenom, adresse, quantite, numtel, statut, pays, gouvernerat, code_postal, adresse_detail, user_id, identity_key) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            fillCommandeStatement(pst, commande, 0);
            pst.executeUpdate();
        }
    }

    public int addAndReturnId(Commande commande) throws SQLException {
        String query = "INSERT INTO commande (nom, prenom, adresse, quantite, numtel, statut, pays, gouvernerat, code_postal, adresse_detail, user_id, identity_key) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            fillCommandeStatement(pst, commande, 0);
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
        String query = "UPDATE commande SET nom = ?, prenom = ?, adresse = ?, quantite = ?, numtel = ?, statut = ?, pays = ?, gouvernerat = ?, code_postal = ?, adresse_detail = ?, user_id = ?, identity_key = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            fillCommandeStatement(pst, commande, 0);
            pst.setInt(13, commande.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String deleteLignes = "DELETE FROM lignecommande WHERE commandeId = ?";
        String deletePayments = "DELETE FROM payment WHERE commande_id = ?";
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
        String query = """
                SELECT c.*,
                       (SELECT COALESCE(SUM(lc.quantite * lc.prixUnitaire), 0)
                        FROM lignecommande lc
                        WHERE lc.commandeId = c.id) AS total_calc,
                       (SELECT MAX(p.created_at)
                        FROM payment p
                        WHERE p.commande_id = c.id) AS payment_date_calc
                FROM commande c
                ORDER BY c.id DESC
                """;

        try (PreparedStatement pst = cnx.prepareStatement(query);
            ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                Commande commande = new Commande(
                        rs.getInt("id"),
                        CommandeDatabaseMapper.extractCommandeDate(rs),
                        CommandeDatabaseMapper.extractCommandeTotal(rs),
                        CommandeDatabaseMapper.extractClientId(rs),
                        rs.getString("statut"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        CommandeDatabaseMapper.fromDatabaseTelephone(rs, "numtel"),
                        rs.getString("adresse")
                );
                commande.setPaysLivraison(rs.getString("pays"));
                commande.setGouvernoratLivraison(rs.getString("gouvernerat"));
                commande.setCodePostalLivraison(rs.getString("code_postal"));
                CommandeDatabaseMapper.populateDeliveryFields(commande, rs.getString("adresse_detail"));
                commandes.add(commande);
            }
        }

        return commandes;
    }

    private void fillCommandeStatement(PreparedStatement pst, Commande commande, int quantite) throws SQLException {
        pst.setString(1, commande.getNom());
        pst.setString(2, commande.getPrenom());
        pst.setString(3, commande.getAdresse());
        pst.setInt(4, CommandeDatabaseMapper.resolveCommandeQuantite(commande, quantite));
        pst.setObject(5, CommandeDatabaseMapper.toDatabaseTelephone(commande.getTelephone()));
        pst.setString(6, commande.getStatut());
        pst.setString(7, commande.getPaysLivraison());
        pst.setString(8, commande.getGouvernoratLivraison());
        pst.setString(9, commande.getCodePostalLivraison());
        pst.setString(10, CommandeDatabaseMapper.buildAdresseDetail(commande));
        pst.setObject(11, CommandeDatabaseMapper.normalizeUserId(commande.getClientId()));
        pst.setString(12, CommandeDatabaseMapper.buildIdentityKey(commande, commande.getTotal()));
    }
}
