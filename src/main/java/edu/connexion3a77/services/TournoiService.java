package edu.connexion3a77.services;

import edu.connexion3a77.entities.Tournoi;
import edu.connexion3a77.interfaces.IService;
import edu.connexion3a77.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TournoiService implements IService<Tournoi> {
    private static final String TABLE_NAME = "tournoi";
    private final Map<String, String> columns = new HashMap<>();

    public TournoiService() {
        createTableIfNotExists();
        resolveColumns();
    }

    private void createTableIfNotExists() {
        String requete = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "nom_tournoi VARCHAR(255) NOT NULL, " +
                "type_tournoi VARCHAR(255) NOT NULL, " +
                "nom_jeu VARCHAR(255) NOT NULL, " +
                "date_debut DATE NOT NULL, " +
                "date_fin DATE NOT NULL, " +
                "nombre_participants INT NOT NULL, " +
                "cash_prize DOUBLE NOT NULL" +
                ")";

        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            st.executeUpdate(requete);
        } catch (SQLException e) {
            System.out.println("Erreur creation table tournoi : " + e.getMessage());
        }
    }

    @Override
    public void addEntity(Tournoi tournoi) {
        if (!isTournoiValide(tournoi)) {
            return;
        }

        String creatorColumn = columns.get("creatorId");
        boolean includeCreator = creatorColumn != null && !creatorColumn.isBlank();

        String typeGameColumn = columns.get("typeGame");
        boolean includeTypeGame = typeGameColumn != null && !typeGameColumn.isBlank();
        String statusColumn = columns.get("status");
        boolean includeStatus = statusColumn != null && !statusColumn.isBlank();

        StringBuilder requete = new StringBuilder("INSERT INTO " + TABLE_NAME + " (")
                .append(col("nom")).append(", ")
                .append(col("type")).append(", ")
                .append(col("jeu")).append(", ")
                .append(col("dateDebut")).append(", ")
                .append(col("dateFin")).append(", ")
                .append(col("participants")).append(", ")
                .append(col("cashPrize"));

        if (includeTypeGame) {
            requete.append(", ").append(typeGameColumn);
        }
        if (includeStatus) {
            requete.append(", ").append(statusColumn);
        }
        if (includeCreator) {
            requete.append(", ").append(creatorColumn);
        }
        requete.append(") VALUES (?, ?, ?, ?, ?, ?, ?");
        if (includeTypeGame) {
            requete.append(", ?");
        }
        if (includeStatus) {
            requete.append(", ?");
        }
        if (includeCreator) {
            requete.append(", ?");
        }
        requete.append(")");

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete.toString());
            pst.setString(1, tournoi.getNomTournoi());
            pst.setString(2, tournoi.getTypeTournoi());
            pst.setString(3, tournoi.getNomJeu());
            pst.setDate(4, tournoi.getDateDebut());
            pst.setDate(5, tournoi.getDateFin());
            pst.setInt(6, tournoi.getNombreParticipants());
            pst.setDouble(7, tournoi.getCashPrize());
            int idx = 8;
            if (includeTypeGame) {
                pst.setString(idx++, inferTypeJeu(tournoi.getNomJeu()));
            }
            if (includeStatus) {
                pst.setString(idx++, "OPEN");
            }
            if (includeCreator) {
                Integer creatorId = resolveDefaultCreatorId();
                if (creatorId == null) {
                    pst.setNull(idx, Types.INTEGER);
                } else {
                    pst.setInt(idx, creatorId);
                }
            }
            pst.executeUpdate();
            System.out.println("Tournoi ajoute avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void ajouter(Tournoi tournoi) {
        addEntityIfNotExists(tournoi);
    }

    public void addEntityIfNotExists(Tournoi tournoi) {
        String requete = "SELECT " + col("id") + " FROM " + TABLE_NAME + " WHERE " + col("nom") + " = ? AND " + col("type") + " = ? AND " + col("jeu") + " = ? AND " + col("dateDebut") + " = ? AND " + col("dateFin") + " = ? LIMIT 1";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, tournoi.getNomTournoi());
            pst.setString(2, tournoi.getTypeTournoi());
            pst.setString(3, tournoi.getNomJeu());
            pst.setDate(4, tournoi.getDateDebut());
            pst.setDate(5, tournoi.getDateFin());

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                System.out.println("Tournoi deja existant, ajout ignore.");
                return;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return;
        }

        addEntity(tournoi);
    }

    @Override
    public void deleteEntity(Tournoi tournoi) {
        String requete = "DELETE FROM " + TABLE_NAME + " WHERE " + col("id") + " = ?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, tournoi.getId());
            pst.executeUpdate();
            System.out.println("Tournoi supprime avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void supprimer(int id) {
        Tournoi tournoi = new Tournoi();
        tournoi.setId(id);
        deleteEntity(tournoi);
    }

    @Override
    public void updateEntity(int id, Tournoi tournoi) {
        if (!isTournoiValide(tournoi)) {
            return;
        }

        StringBuilder requete = new StringBuilder("UPDATE " + TABLE_NAME + " SET ")
                .append(col("nom")).append(" = ?, ")
                .append(col("type")).append(" = ?, ")
                .append(col("jeu")).append(" = ?, ")
                .append(col("dateDebut")).append(" = ?, ")
                .append(col("dateFin")).append(" = ?, ")
                .append(col("participants")).append(" = ?, ")
                .append(col("cashPrize")).append(" = ?");

        String typeGameColumn = columns.get("typeGame");
        boolean includeTypeGame = typeGameColumn != null && !typeGameColumn.isBlank();
        String statusColumn = columns.get("status");
        boolean includeStatus = statusColumn != null && !statusColumn.isBlank();
        if (includeTypeGame) {
            requete.append(", ").append(typeGameColumn).append(" = ?");
        }
        if (includeStatus) {
            requete.append(", ").append(statusColumn).append(" = ?");
        }
        requete.append(" WHERE ").append(col("id")).append(" = ?");

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete.toString());
            pst.setString(1, tournoi.getNomTournoi());
            pst.setString(2, tournoi.getTypeTournoi());
            pst.setString(3, tournoi.getNomJeu());
            pst.setDate(4, tournoi.getDateDebut());
            pst.setDate(5, tournoi.getDateFin());
            pst.setInt(6, tournoi.getNombreParticipants());
            pst.setDouble(7, tournoi.getCashPrize());
            int idx = 8;
            if (includeTypeGame) {
                pst.setString(idx++, inferTypeJeu(tournoi.getNomJeu()));
            }
            if (includeStatus) {
                pst.setString(idx++, "OPEN");
            }
            pst.setInt(idx, id);
            pst.executeUpdate();
            System.out.println("Tournoi modifie avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void modifier(Tournoi tournoi) {
        updateEntity(tournoi.getId(), tournoi);
    }

    private boolean isTournoiValide(Tournoi tournoi) {
        if (tournoi == null) {
            System.out.println("Controle saisie: tous les champs doivent etre remplis.");
            return false;
        }

        if (tournoi.getNomTournoi() == null || tournoi.getNomTournoi().trim().isEmpty()
                || tournoi.getTypeTournoi() == null || tournoi.getTypeTournoi().trim().isEmpty()
                || tournoi.getNomJeu() == null || tournoi.getNomJeu().trim().isEmpty()) {
            System.out.println("Controle saisie: tous les champs texte doivent etre remplis.");
            return false;
        }

        if (tournoi.getNombreParticipants() < 4) {
            System.out.println("Controle saisie: le nombre de participants doit etre au moins 4.");
            return false;
        }

        if (tournoi.getCashPrize() < 0) {
            System.out.println("Controle saisie: le cash prize ne doit pas etre negatif.");
            return false;
        }

        if (tournoi.getDateDebut() == null || tournoi.getDateFin() == null) {
            System.out.println("Controle saisie: les dates debut et fin sont obligatoires.");
            return false;
        }

        LocalDate dateActuelle = LocalDate.now();
        LocalDate dateDebut = tournoi.getDateDebut().toLocalDate();
        LocalDate dateFin = tournoi.getDateFin().toLocalDate();

        if (dateDebut.isBefore(dateActuelle)) {
            System.out.println("Controle saisie: la date de debut ne doit pas etre avant la date actuelle.");
            return false;
        }

        if (dateFin.isBefore(dateDebut)) {
            System.out.println("Controle saisie: la date de fin ne doit pas etre avant la date de debut.");
            return false;
        }

        return true;
    }

    @Override
    public List<Tournoi> getData() {
        List<Tournoi> data = new ArrayList<>();
        String requete = "SELECT * FROM " + TABLE_NAME;

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Tournoi tournoi = new Tournoi();
                tournoi.setId(rs.getInt(col("id")));
                tournoi.setNomTournoi(rs.getString(col("nom")));
                tournoi.setTypeTournoi(rs.getString(col("type")));
                tournoi.setNomJeu(rs.getString(col("jeu")));
                tournoi.setDateDebut(rs.getDate(col("dateDebut")));
                tournoi.setDateFin(rs.getDate(col("dateFin")));
                tournoi.setNombreParticipants(rs.getInt(col("participants")));
                tournoi.setCashPrize(rs.getDouble(col("cashPrize")));
                data.add(tournoi);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }

    public List<Tournoi> afficher() {
        return getData();
    }

    private void resolveColumns() {
        columns.put("id", findColumnName("id", "id_tournoi"));
        columns.put("nom", findColumnName("nom_tournoi", "nom", "name"));
        columns.put("type", findColumnName("type_tournoi", "type"));
        columns.put("jeu", findColumnName("nom_jeu", "jeu", "game", "game_name"));
        columns.put("dateDebut", findColumnName("date_debut", "dateDebut", "start_date"));
        columns.put("dateFin", findColumnName("date_fin", "dateFin", "end_date"));
        columns.put("participants", findColumnName("nombre_participants", "participants", "max_participants", "nbr_participants", "places", "slots"));
        columns.put("cashPrize", findColumnName("cash_prize", "cashPrize", "prize", "dotation", "prix", "prize_pool", "reward"));
        String typeGameCol = findOptionalColumnName("type_game", "game_type", "typejeu");
        if (typeGameCol != null && !typeGameCol.isBlank()) {
            columns.put("typeGame", typeGameCol);
        }
        String statusCol = findOptionalColumnName("status", "statut", "etat", "state");
        if (statusCol != null && !statusCol.isBlank()) {
            columns.put("status", statusCol);
        }
        String creatorIdColumn = findOptionalColumnName("creator_id", "created_by", "user_id", "owner_id");
        if (creatorIdColumn != null && !creatorIdColumn.isBlank()) {
            columns.put("creatorId", creatorIdColumn);
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
        String optionalMatch = findOptionalColumnName(candidates);
        if (optionalMatch != null) {
            return optionalMatch;
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

            // Final fallback for participants-style columns with uncommon naming.
            if (candidates.length > 0 && normalize(candidates[0]).contains("participants")) {
                for (Map.Entry<String, String> entry : existing.entrySet()) {
                    String n = normalize(entry.getKey());
                    if (n.contains("participant")
                            || n.contains("participants")
                            || n.contains("joueur")
                            || n.contains("players")
                            || n.contains("places")
                            || n.contains("slots")) {
                        return entry.getValue();
                    }
                }
            }

            // Final fallback for money/prize columns with uncommon naming.
            if (candidates.length > 0 && (normalize(candidates[0]).contains("cash") || normalize(candidates[0]).contains("prize"))) {
                for (Map.Entry<String, String> entry : existing.entrySet()) {
                    String n = normalize(entry.getKey());
                    if (n.contains("prix")
                            || n.contains("prize")
                            || n.contains("cash")
                            || n.contains("dotation")
                            || n.contains("reward")
                            || n.contains("pool")
                            || n.contains("montant")) {
                        return entry.getValue();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Detection colonnes tournoi: " + e.getMessage());
        }
        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").trim();
    }

    private Integer resolveDefaultCreatorId() {
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

    private String inferTypeJeu(String nomJeu) {
        if (nomJeu == null) return "MIND";
        String normalized = nomJeu.toLowerCase(Locale.ROOT);
        if (normalized.contains("fifa") || normalized.contains("nba") || normalized.contains("pes")) return "SPORTS";
        if (normalized.contains("valorant") || normalized.contains("cs") || normalized.contains("call of duty")) return "FPS";
        if (normalized.contains("fortnite") || normalized.contains("pubg") || normalized.contains("apex")) return "BATTLE ROYALE";
        return "MIND";
    }
}
