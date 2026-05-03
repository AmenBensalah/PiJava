package edu.connexion3a77.services;

import edu.connexion3a77.entities.DemandeParticipation;
import edu.connexion3a77.interfaces.IService;
import edu.connexion3a77.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DemandeParticipationService implements IService<DemandeParticipation> {
    private static final String TABLE_NAME = "participation_request";
    private final Map<String, String> columns = new HashMap<>();

    public DemandeParticipationService() {
        createTableIfNotExists();
        resolveColumns();
    }

    private void createTableIfNotExists() {
        String requete = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "tournoi_id INT NOT NULL, " +
                "description VARCHAR(255) NOT NULL, " +
                "niveau VARCHAR(100) NOT NULL, " +
                "statut VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE'" +
                ")";

        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            st.executeUpdate(requete);
            // If table already existed with unique key, drop it to allow multiple demandes per tournoi.
            try {
                st.executeUpdate("ALTER TABLE " + TABLE_NAME + " DROP INDEX uq_demande_tournoi");
            } catch (SQLException ignored) {
                // Index may not exist, ignore.
            }
            try {
                st.executeUpdate("ALTER TABLE " + TABLE_NAME + " ADD COLUMN statut VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE'");
            } catch (SQLException ignored) {
                // Column may already exist, ignore.
            }
        } catch (SQLException e) {
            System.out.println("Erreur creation table participation_request : " + e.getMessage());
        }
    }

    @Override
    public void addEntity(DemandeParticipation demandeParticipation) {
        if (!isDemandeValide(demandeParticipation)) {
            return;
        }

        if (existsSameDemande(demandeParticipation, null)) {
            System.out.println("Controle saisie: cette meme demande existe deja dans ce tournoi.");
            return;
        }

        boolean includeUserId = columns.containsKey("userId");
        boolean includeRulesAccepted = columns.containsKey("rulesAccepted");
        boolean includeApplicantName = columns.containsKey("applicantName");
        boolean includeApplicantEmail = columns.containsKey("applicantEmail");
        boolean includeCreatedAt = columns.containsKey("createdAt");
        boolean includeLegacyStatut = columns.containsKey("legacyStatut");

        StringBuilder requete = new StringBuilder("INSERT INTO " + TABLE_NAME + " (")
                .append(col("tournoiId")).append(", ")
                .append(col("description")).append(", ")
                .append(col("niveau")).append(", ")
                .append(col("statut"));

        if (includeLegacyStatut) requete.append(", ").append(col("legacyStatut"));
        if (includeUserId) requete.append(", ").append(col("userId"));
        if (includeRulesAccepted) requete.append(", ").append(col("rulesAccepted"));
        if (includeApplicantName) requete.append(", ").append(col("applicantName"));
        if (includeApplicantEmail) requete.append(", ").append(col("applicantEmail"));
        if (includeCreatedAt) requete.append(", ").append(col("createdAt"));

        requete.append(") VALUES (?, ?, ?, ?");
        if (includeLegacyStatut) requete.append(", ?");
        if (includeUserId) requete.append(", ?");
        if (includeRulesAccepted) requete.append(", ?");
        if (includeApplicantName) requete.append(", ?");
        if (includeApplicantEmail) requete.append(", ?");
        if (includeCreatedAt) requete.append(", ?");
        requete.append(")");

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete.toString());
            pst.setInt(1, demandeParticipation.getTournoiId());
            pst.setString(2, demandeParticipation.getDescription());
            pst.setString(3, demandeParticipation.getNiveau());
            pst.setString(4, normalizedStatut(demandeParticipation.getStatut()));

            int idx = 5;
            if (includeLegacyStatut) {
                pst.setString(idx++, normalizedStatut(demandeParticipation.getStatut()));
            }
            if (includeUserId) {
                Integer userId = resolveDefaultUserId();
                if (userId == null) {
                    pst.setNull(idx++, Types.INTEGER);
                } else {
                    pst.setInt(idx++, userId);
                }
            }
            if (includeRulesAccepted) {
                pst.setBoolean(idx++, true);
            }
            if (includeApplicantName) {
                pst.setString(idx++, "Utilisateur App");
            }
            if (includeApplicantEmail) {
                pst.setString(idx++, "user@app.local");
            }
            if (includeCreatedAt) {
                pst.setTimestamp(idx++, new Timestamp(System.currentTimeMillis()));
            }

            pst.executeUpdate();
            System.out.println("Demande de participation ajoutee avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void ajouter(DemandeParticipation demandeParticipation) {
        addEntity(demandeParticipation);
    }

    @Override
    public void deleteEntity(DemandeParticipation demandeParticipation) {
        String requete = "DELETE FROM " + TABLE_NAME + " WHERE " + col("id") + " = ?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, demandeParticipation.getId());
            pst.executeUpdate();
            System.out.println("Demande de participation supprimee avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void supprimer(int id) {
        DemandeParticipation demandeParticipation = new DemandeParticipation();
        demandeParticipation.setId(id);
        deleteEntity(demandeParticipation);
    }

    @Override
    public void updateEntity(int id, DemandeParticipation demandeParticipation) {
        if (!isDemandeValide(demandeParticipation)) {
            return;
        }

        if (existsSameDemande(demandeParticipation, id)) {
            System.out.println("Controle saisie: cette meme demande existe deja dans ce tournoi.");
            return;
        }

        String requete = "UPDATE " + TABLE_NAME + " SET " + col("tournoiId") + " = ?, " + col("description") + " = ?, " + col("niveau") + " = ?, " + col("statut") + " = ? WHERE " + col("id") + " = ?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, demandeParticipation.getTournoiId());
            pst.setString(2, demandeParticipation.getDescription());
            pst.setString(3, demandeParticipation.getNiveau());
            pst.setString(4, normalizedStatut(demandeParticipation.getStatut()));
            pst.setInt(5, id);
            pst.executeUpdate();
            System.out.println("Demande de participation modifiee avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void modifier(DemandeParticipation demandeParticipation) {
        updateEntity(demandeParticipation.getId(), demandeParticipation);
    }

    @Override
    public List<DemandeParticipation> getData() {
        List<DemandeParticipation> data = new ArrayList<>();
        String requete = "SELECT * FROM " + TABLE_NAME;

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                DemandeParticipation demandeParticipation = new DemandeParticipation();
                demandeParticipation.setId(rs.getInt(col("id")));
                demandeParticipation.setTournoiId(rs.getInt(col("tournoiId")));
                demandeParticipation.setDescription(rs.getString(col("description")));
                demandeParticipation.setNiveau(rs.getString(col("niveau")));
                demandeParticipation.setStatut(normalizedStatut(rs.getString(col("statut"))));
                data.add(demandeParticipation);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }

    public List<DemandeParticipation> afficher() {
        return getData();
    }

    public List<DemandeParticipation> afficherParStatut(String statut) {
        String statutNormalise = normalizedStatut(statut);
        if (statutNormalise == null) {
            return getData();
        }

        List<DemandeParticipation> data = new ArrayList<>();
        String requete = "SELECT * FROM " + TABLE_NAME + " WHERE " + col("statut") + " = ?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, statutNormalise);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                DemandeParticipation demandeParticipation = new DemandeParticipation();
                demandeParticipation.setId(rs.getInt(col("id")));
                demandeParticipation.setTournoiId(rs.getInt(col("tournoiId")));
                demandeParticipation.setDescription(rs.getString(col("description")));
                demandeParticipation.setNiveau(rs.getString(col("niveau")));
                demandeParticipation.setStatut(normalizedStatut(rs.getString(col("statut"))));
                data.add(demandeParticipation);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return data;
    }

    public void updateStatut(int id, String statut) {
        String statutNormalise = normalizedStatut(statut);
        if (statutNormalise == null) {
            System.out.println("Controle saisie: statut invalide.");
            return;
        }

        String requete = "UPDATE " + TABLE_NAME + " SET " + col("statut") + " = ? WHERE " + col("id") + " = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, statutNormalise);
            pst.setInt(2, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean isDemandeValide(DemandeParticipation demandeParticipation) {
        if (demandeParticipation == null) {
            System.out.println("Controle saisie: tous les champs doivent etre remplis.");
            return false;
        }

        if (demandeParticipation.getTournoiId() <= 0) {
            System.out.println("Controle saisie: tournoi_id est obligatoire.");
            return false;
        }

        if (demandeParticipation.getDescription() == null || demandeParticipation.getDescription().trim().isEmpty()) {
            System.out.println("Controle saisie: la description ne doit pas etre vide.");
            return false;
        }

        if (demandeParticipation.getNiveau() == null || demandeParticipation.getNiveau().trim().isEmpty()) {
            System.out.println("Controle saisie: le niveau ne doit pas etre vide.");
            return false;
        }

        if (normalizedStatut(demandeParticipation.getStatut()) == null) {
            System.out.println("Controle saisie: statut invalide.");
            return false;
        }

        return true;
    }

    private boolean existsSameDemande(DemandeParticipation demandeParticipation, Integer excludeId) {
        String requete = "SELECT " + col("id") + " FROM " + TABLE_NAME +
                " WHERE " + col("tournoiId") + " = ? AND " + col("description") + " = ? AND " + col("niveau") + " = ?" +
                (excludeId != null ? " AND " + col("id") + " <> ?" : "") +
                " LIMIT 1";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, demandeParticipation.getTournoiId());
            pst.setString(2, demandeParticipation.getDescription().trim());
            pst.setString(3, demandeParticipation.getNiveau().trim());
            if (excludeId != null) {
                pst.setInt(4, excludeId);
            }
            ResultSet rs = pst.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private String normalizedStatut(String statut) {
        if (statut == null || statut.trim().isEmpty()) {
            return DemandeParticipation.STATUT_EN_ATTENTE;
        }
        String normalized = statut.trim().toUpperCase();
        if (DemandeParticipation.STATUT_EN_ATTENTE.equals(normalized)
                || DemandeParticipation.STATUT_ACCEPTEE.equals(normalized)
                || DemandeParticipation.STATUT_REFUSEE.equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private void resolveColumns() {
        columns.put("id", findColumnName("id", "id_participation", "request_id"));
        columns.put("tournoiId", findColumnName("tournoi_id", "tournament_id", "tournoiId"));
        columns.put("description", findColumnName("description", "message", "details", "contenu", "commentaire", "body"));
        columns.put("niveau", findColumnName("niveau", "level", "niveau_joueur", "rank", "skill", "player_level"));
        // Important: prioritize "status" over legacy "statut" because DB requires it.
        columns.put("statut", findColumnName("status", "statut", "etat", "state"));
        String legacyStatutCol = findOptionalColumnName("statut");
        if (legacyStatutCol != null && !legacyStatutCol.isBlank() && !legacyStatutCol.equalsIgnoreCase(columns.get("statut"))) {
            columns.put("legacyStatut", legacyStatutCol);
        }

        String userIdCol = findOptionalColumnName("user_id", "id_user", "userid");
        if (userIdCol != null && !userIdCol.isBlank()) {
            columns.put("userId", userIdCol);
        }
        String rulesAcceptedCol = findOptionalColumnName("rules_accepted", "accepted_rules", "rulesaccepted");
        if (rulesAcceptedCol != null && !rulesAcceptedCol.isBlank()) {
            columns.put("rulesAccepted", rulesAcceptedCol);
        }
        String applicantNameCol = findOptionalColumnName("applicant_name", "nom_applicant", "player_name", "fullname");
        if (applicantNameCol != null && !applicantNameCol.isBlank()) {
            columns.put("applicantName", applicantNameCol);
        }
        String applicantEmailCol = findOptionalColumnName("applicant_email", "email", "mail");
        if (applicantEmailCol != null && !applicantEmailCol.isBlank()) {
            columns.put("applicantEmail", applicantEmailCol);
        }
        String createdAtCol = findOptionalColumnName("created_at", "createdat", "date_creation", "created_on");
        if (createdAtCol != null && !createdAtCol.isBlank()) {
            columns.put("createdAt", createdAtCol);
        }
    }

    private String col(String key) {
        String value = columns.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Colonne introuvable pour: " + key);
        }
        return value;
    }

    private String findColumnName(String... candidates) {
        String optional = findOptionalColumnName(candidates);
        if (optional != null) {
            return optional;
        }
        return candidates[0];
    }

    private String findOptionalColumnName(String... candidates) {
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx()
                    .prepareStatement("SELECT * FROM " + TABLE_NAME + " LIMIT 1");
            ResultSet rs = pst.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            Map<String, String> existing = new HashMap<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String label = meta.getColumnLabel(i);
                existing.put(label.toLowerCase(Locale.ROOT), label);
            }
            for (String candidate : candidates) {
                String hit = existing.get(candidate.toLowerCase(Locale.ROOT));
                if (hit != null) {
                    return hit;
                }
            }

            // Fallback: relaxed matching ignoring underscores/case.
            for (String candidate : candidates) {
                String normalizedCandidate = normalize(candidate);
                for (Map.Entry<String, String> entry : existing.entrySet()) {
                    if (normalize(entry.getKey()).equals(normalizedCandidate)) {
                        return entry.getValue();
                    }
                }
            }

            if (candidates.length > 0) {
                String first = normalize(candidates[0]);

                if (first.contains("niveau") || first.contains("level")) {
                    for (Map.Entry<String, String> entry : existing.entrySet()) {
                        String n = normalize(entry.getKey());
                        if (n.contains("niveau") || n.contains("level") || n.contains("rank") || n.contains("skill")) {
                            return entry.getValue();
                        }
                    }
                }

                if (first.contains("description") || first.contains("message")) {
                    for (Map.Entry<String, String> entry : existing.entrySet()) {
                        String n = normalize(entry.getKey());
                        if (n.contains("description") || n.contains("message") || n.contains("detail") || n.contains("comment")) {
                            return entry.getValue();
                        }
                    }
                }

                if (first.contains("statut") || first.contains("status")) {
                    for (Map.Entry<String, String> entry : existing.entrySet()) {
                        String n = normalize(entry.getKey());
                        if (n.contains("statut") || n.contains("status") || n.contains("etat") || n.contains("state")) {
                            return entry.getValue();
                        }
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Detection colonnes participation_request: " + e.getMessage());
        }
        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").trim();
    }

    private Integer resolveDefaultUserId() {
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery("SELECT id FROM user ORDER BY id ASC LIMIT 1");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {
        }
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery("SELECT id_user FROM user ORDER BY id_user ASC LIMIT 1");
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException ignored) {
        }
        return 1;
    }

}
