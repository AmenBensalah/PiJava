package edu.PROJETPI.services;

import edu.PROJETPI.entites.CartItem;
import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.tools.MyConexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CheckoutService {
    private final Connection cnx = MyConexion.getInstance().getConnection();

    public int checkout(OrderSession session, Date paymentDate) throws SQLException {
        Commande draft = session.getDraftCommande();
        if (draft == null) {
            throw new SQLException("Commande brouillon introuvable.");
        }

        String insertCommande = "INSERT INTO commande (dateCommande, total, clientId, statut, nom, prenom, telephone, adresse) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String updateCommande = "UPDATE commande SET dateCommande = ?, total = ?, clientId = ?, statut = ?, nom = ?, prenom = ?, telephone = ?, adresse = ? WHERE id = ?";
        String insertLigne = "INSERT INTO lignecommande (commandeId, produitId, quantite, prixUnitaire) VALUES (?, ?, ?, ?)";
        String insertPayment = "INSERT INTO payment (commandeId, montant, datePayment) VALUES (?, ?, ?)";
        String deleteExistingLignes = "DELETE FROM lignecommande WHERE commandeId = ?";

        try {
            cnx.setAutoCommit(false);

            int commandeId;
            if (draft.getId() > 0) {
                commandeId = draft.getId();
                try (PreparedStatement pstCommande = cnx.prepareStatement(updateCommande)) {
                    pstCommande.setDate(1, new Date(draft.getDateCommande().getTime()));
                    pstCommande.setDouble(2, session.getCartTotal());
                    pstCommande.setInt(3, draft.getClientId());
                    pstCommande.setString(4, "PAYEE");
                    pstCommande.setString(5, draft.getNom());
                    pstCommande.setString(6, draft.getPrenom());
                    pstCommande.setString(7, draft.getTelephone());
                    pstCommande.setString(8, draft.getAdresse());
                    pstCommande.setInt(9, commandeId);
                    pstCommande.executeUpdate();
                }
            } else {
                try (PreparedStatement pstCommande = cnx.prepareStatement(insertCommande, Statement.RETURN_GENERATED_KEYS)) {
                    pstCommande.setDate(1, new Date(draft.getDateCommande().getTime()));
                    pstCommande.setDouble(2, session.getCartTotal());
                    pstCommande.setInt(3, draft.getClientId());
                    pstCommande.setString(4, "PAYEE");
                    pstCommande.setString(5, draft.getNom());
                    pstCommande.setString(6, draft.getPrenom());
                    pstCommande.setString(7, draft.getTelephone());
                    pstCommande.setString(8, draft.getAdresse());
                    pstCommande.executeUpdate();

                    try (ResultSet generatedKeys = pstCommande.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new SQLException("Impossible de recuperer l'ID de la commande.");
                        }
                        commandeId = generatedKeys.getInt(1);
                    }
                }
            }

            try (PreparedStatement pstDeleteLignes = cnx.prepareStatement(deleteExistingLignes)) {
                pstDeleteLignes.setInt(1, commandeId);
                pstDeleteLignes.executeUpdate();
            }

            try (PreparedStatement pstLigne = cnx.prepareStatement(insertLigne)) {
                for (CartItem item : session.getCartItems()) {
                    pstLigne.setInt(1, commandeId);
                    pstLigne.setInt(2, item.getProduitId());
                    pstLigne.setInt(3, item.getQuantite());
                    pstLigne.setDouble(4, item.getPrixUnitaire());
                    pstLigne.addBatch();
                }
                pstLigne.executeBatch();
            }

            try (PreparedStatement pstPayment = cnx.prepareStatement(insertPayment)) {
                pstPayment.setInt(1, commandeId);
                pstPayment.setDouble(2, session.getCartTotal());
                pstPayment.setDate(3, paymentDate);
                pstPayment.executeUpdate();
            }

            cnx.commit();
            session.resetAfterCheckout();
            return commandeId;
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(true);
        }
    }
}
