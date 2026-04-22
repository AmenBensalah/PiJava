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
        return checkout(session, paymentDate, "PAYEE", true);
    }

    public int checkoutCashOnDelivery(OrderSession session, Date orderDate) throws SQLException {
        return checkout(session, orderDate, "EN_LIVRAISON", false);
    }

    public int savePendingPaymentCommande(OrderSession session, Date orderDate) throws SQLException {
        Commande draft = session.getDraftCommande();
        if (draft == null) {
            throw new SQLException("Commande brouillon introuvable.");
        }
        draft.setDateCommande(orderDate);
        return saveCommandeAndLines(session, "EN_ATTENTE", false, null, false);
    }

    private int checkout(OrderSession session, Date paymentDate, String commandeStatut, boolean registerPayment) throws SQLException {
        return saveCommandeAndLines(session, commandeStatut, registerPayment, paymentDate, true);
    }

    private int saveCommandeAndLines(
            OrderSession session,
            String commandeStatut,
            boolean registerPayment,
            Date paymentDate,
            boolean resetAfterSave
    ) throws SQLException {
        Commande draft = session.getDraftCommande();
        if (draft == null) {
            throw new SQLException("Commande brouillon introuvable.");
        }
        normalizeLegacyDeliveryFields(draft);

        String insertCommande = "INSERT INTO commande (dateCommande, total, clientId, statut, nom, prenom, numtel, adresse, pays, gouvernerat, code_postal, adresseLivraison, adresse_detail) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateCommande = "UPDATE commande SET dateCommande = ?, total = ?, clientId = ?, statut = ?, nom = ?, prenom = ?, numtel = ?, adresse = ?, pays = ?, gouvernerat = ?, code_postal = ?, adresseLivraison = ?, adresse_detail = ? WHERE id = ?";
        String insertLigne = "INSERT INTO lignecommande (commandeId, produitId, quantite, prixUnitaire) VALUES (?, ?, ?, ?)";
        String insertPayment = "INSERT INTO payment (commandeId, montant, datePayment) VALUES (?, ?, ?)";
        String deleteExistingLignes = "DELETE FROM lignecommande WHERE commandeId = ?";

        try {
            cnx.setAutoCommit(false);

            int commandeId;
            if (draft.getId() > 0) {
                commandeId = draft.getId();
                try (PreparedStatement pstCommande = cnx.prepareStatement(updateCommande)) {
                    fillCommandeStatement(pstCommande, draft, session.getCartTotal(), commandeStatut);
                    pstCommande.setInt(14, commandeId);
                    pstCommande.executeUpdate();
                }
            } else {
                try (PreparedStatement pstCommande = cnx.prepareStatement(insertCommande, Statement.RETURN_GENERATED_KEYS)) {
                    fillCommandeStatement(pstCommande, draft, session.getCartTotal(), commandeStatut);
                    pstCommande.executeUpdate();

                    try (ResultSet generatedKeys = pstCommande.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new SQLException("Impossible de recuperer l'ID de la commande.");
                        }
                        commandeId = generatedKeys.getInt(1);
                    }
                }
                draft.setId(commandeId);
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

            if (registerPayment) {
                try (PreparedStatement pstPayment = cnx.prepareStatement(insertPayment)) {
                    pstPayment.setInt(1, commandeId);
                    pstPayment.setDouble(2, session.getCartTotal());
                    pstPayment.setDate(3, paymentDate);
                    pstPayment.executeUpdate();
                }
            }

            cnx.commit();
            draft.setTotal(session.getCartTotal());
            draft.setStatut(commandeStatut);
            if (resetAfterSave) {
                session.resetAfterCheckout();
            }
            return commandeId;
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(true);
        }
    }

    private void fillCommandeStatement(PreparedStatement pstCommande, Commande draft, double total, String statut) throws SQLException {
        pstCommande.setDate(1, new Date(draft.getDateCommande().getTime()));
        pstCommande.setDouble(2, total);
        pstCommande.setInt(3, draft.getClientId());
        pstCommande.setString(4, statut);
        pstCommande.setString(5, draft.getNom());
        pstCommande.setString(6, draft.getPrenom());
        pstCommande.setString(7, draft.getTelephone());
        pstCommande.setString(8, draft.getAdresse());
        pstCommande.setString(9, draft.getPaysLivraison());
        pstCommande.setString(10, draft.getGouvernoratLivraison());
        pstCommande.setString(11, draft.getCodePostalLivraison());
        pstCommande.setString(12, draft.getAdresseLivraison());
        pstCommande.setString(13, draft.getDescriptionLivraison());
    }

    private void normalizeLegacyDeliveryFields(Commande draft) {
        String adresse = draft.getAdresse();
        if (adresse == null || !adresse.contains("Livraison")) {
            return;
        }

        if (isBlank(draft.getPaysLivraison())) {
            draft.setPaysLivraison(extractLegacyDeliveryValue(adresse, "Pays:"));
        }
        if (isBlank(draft.getGouvernoratLivraison())) {
            draft.setGouvernoratLivraison(extractLegacyDeliveryValue(adresse, "Gouvernorat:"));
        }
        if (isBlank(draft.getCodePostalLivraison())) {
            draft.setCodePostalLivraison(extractLegacyDeliveryValue(adresse, "Code postal:"));
        }
        if (isBlank(draft.getAdresseLivraison())) {
            draft.setAdresseLivraison(extractLegacyDeliveryValue(adresse, "Adresse:"));
        }
        if (isBlank(draft.getDescriptionLivraison())) {
            draft.setDescriptionLivraison(extractLegacyDeliveryValue(adresse, "Description:"));
        }

        int deliveryIndex = adresse.indexOf("\n\nLivraison\n");
        if (deliveryIndex >= 0) {
            draft.setAdresse(blankToNull(adresse.substring(0, deliveryIndex).trim()));
            return;
        }

        if (adresse.startsWith("Livraison")) {
            draft.setAdresse(null);
        }
    }

    private String extractLegacyDeliveryValue(String source, String label) {
        String[] lines = source.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.startsWith(label)) {
                continue;
            }

            String value = line.substring(label.length()).trim();
            if (!value.isBlank()) {
                return value;
            }

            if (i + 1 < lines.length) {
                String nextLine = lines[i + 1].trim();
                return nextLine.equalsIgnoreCase("NULL") ? null : blankToNull(nextLine);
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
