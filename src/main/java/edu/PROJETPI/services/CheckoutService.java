package edu.PROJETPI.services;

import edu.PROJETPI.entites.CartItem;
import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.tools.MyConexion;
import edu.ProjetPI.controllers.DashboardSession;
import edu.ProjetPI.entities.User;

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

        String insertCommande = "INSERT INTO commande (nom, prenom, adresse, quantite, numtel, statut, pays, gouvernerat, code_postal, adresse_detail, user_id, identity_key) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String updateCommande = "UPDATE commande SET nom = ?, prenom = ?, adresse = ?, quantite = ?, numtel = ?, statut = ?, pays = ?, gouvernerat = ?, code_postal = ?, adresse_detail = ?, user_id = ?, identity_key = ? WHERE id = ?";
        String insertLigne = "INSERT INTO lignecommande (commandeId, produitId, quantite, prixUnitaire) VALUES (?, ?, ?, ?)";
        String insertPayment = "INSERT INTO payment (amount, created_at, status, commande_id) VALUES (?, ?, ?, ?)";
        String deleteExistingLignes = "DELETE FROM lignecommande WHERE commandeId = ?";

        try {
            cnx.setAutoCommit(false);

            int commandeId;
            String previousStatut = null;
            if (draft.getId() > 0) {
                commandeId = draft.getId();
                previousStatut = readCommandeStatus(commandeId);
                try (PreparedStatement pstCommande = cnx.prepareStatement(updateCommande)) {
                    fillCommandeStatement(pstCommande, draft, session.getCartTotal(), session.getTotalItems(), commandeStatut);
                    pstCommande.setInt(13, commandeId);
                    pstCommande.executeUpdate();
                }
            } else {
                try (PreparedStatement pstCommande = cnx.prepareStatement(insertCommande, Statement.RETURN_GENERATED_KEYS)) {
                    fillCommandeStatement(pstCommande, draft, session.getCartTotal(), session.getTotalItems(), commandeStatut);
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

            if (CommandeStockService.impactsStock(previousStatut)) {
                CommandeStockService.restoreForCommande(cnx, commandeId);
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

            if (CommandeStockService.impactsStock(commandeStatut)) {
                CommandeStockService.decrementForCart(cnx, session.getCartItems());
            }

            if (registerPayment) {
                try (PreparedStatement pstPayment = cnx.prepareStatement(insertPayment)) {
                    pstPayment.setDouble(1, session.getCartTotal());
                    pstPayment.setTimestamp(2, new java.sql.Timestamp(paymentDate.getTime()));
                    pstPayment.setString(3, "PAYEE");
                    pstPayment.setInt(4, commandeId);
                    pstPayment.executeUpdate();
                }
            }

            cnx.commit();
            draft.setTotal(session.getCartTotal());
            draft.setStatut(commandeStatut);
            if (resetAfterSave) {
                session.rememberConfirmedOrder(draft, session.getCartItems(), session.getCartTotal(), commandeId);
            }
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

    private void fillCommandeStatement(
            PreparedStatement pstCommande,
            Commande draft,
            double total,
            int quantite,
            String statut
    ) throws SQLException {
        pstCommande.setString(1, draft.getNom());
        pstCommande.setString(2, draft.getPrenom());
        pstCommande.setString(3, draft.getAdresse());
        pstCommande.setInt(4, CommandeDatabaseMapper.resolveCommandeQuantite(draft, quantite));
        pstCommande.setObject(5, CommandeDatabaseMapper.toDatabaseTelephone(draft.getTelephone()));
        pstCommande.setString(6, statut);
        pstCommande.setString(7, draft.getPaysLivraison());
        pstCommande.setString(8, draft.getGouvernoratLivraison());
        pstCommande.setString(9, draft.getCodePostalLivraison());
        pstCommande.setString(10, CommandeDatabaseMapper.buildAdresseDetail(draft));
        pstCommande.setObject(11, CommandeDatabaseMapper.normalizeUserId(resolveClientId(draft)));
        pstCommande.setString(12, CommandeDatabaseMapper.buildIdentityKey(draft, total));
    }

    private int resolveClientId(Commande draft) {
        if (draft.getClientId() > 0) {
            return draft.getClientId();
        }

        User currentUser = DashboardSession.getCurrentUser();
        int currentUserId = currentUser == null ? 0 : currentUser.getId();
        if (currentUserId > 0) {
            draft.setClientId(currentUserId);
        }
        return currentUserId;
    }

    private String readCommandeStatus(int commandeId) throws SQLException {
        String query = "SELECT statut FROM commande WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, commandeId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getString("statut") : null;
            }
        }
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
