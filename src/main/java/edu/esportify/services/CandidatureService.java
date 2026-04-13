package edu.esportify.services;

import edu.esportify.entities.Candidature;
import edu.esportify.interfaces.IService;
import edu.esportify.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class CandidatureService implements IService<Candidature> {
    private static final List<Candidature> LOCAL_DATA = new ArrayList<>();
    private static int nextId = 1;

    @Override
    public void addEntity(Candidature candidature) {
        if (!hasConnection()) {
            if (candidature.getId() <= 0) {
                candidature.setId(nextId++);
            } else {
                nextId = Math.max(nextId, candidature.getId() + 1);
            }
            LOCAL_DATA.removeIf(item -> item.getId() == candidature.getId());
            LOCAL_DATA.add(candidature);
            return;
        }
        try {
            Map<String, Object> values = buildInsertValues(candidature);
            String requete = buildInsertQuery(values);
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            bindValues(pst, values);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout candidature: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEntity(Candidature candidature) {
        if (!hasConnection()) {
            LOCAL_DATA.removeIf(item -> item.getId() == candidature.getId());
            return;
        }
        String requete = "DELETE FROM candidature WHERE id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, candidature.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression candidature: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateEntity(int id, Candidature candidature) {
        if (!hasConnection()) {
            candidature.setId(id);
            for (int i = 0; i < LOCAL_DATA.size(); i++) {
                if (LOCAL_DATA.get(i).getId() == id) {
                    LOCAL_DATA.set(i, candidature);
                    return;
                }
            }
            LOCAL_DATA.add(candidature);
            nextId = Math.max(nextId, id + 1);
            return;
        }
        try {
            Map<String, Object> values = buildUpdateValues(candidature);
            String requete = buildUpdateQuery(values);
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            int index = bindValues(pst, values);
            pst.setInt(index, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur modification candidature: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Candidature> getData() {
        if (!hasConnection()) {
            return LOCAL_DATA.stream()
                    .sorted(Comparator.comparingInt(Candidature::getId).reversed())
                    .toList();
        }
        List<Candidature> data = new ArrayList<>();
        String requete = """
                SELECT c.*, e.nom_equipe
                FROM candidature c
                JOIN equipe e ON c.equipe_id = e.id
                ORDER BY c.id DESC
                """;
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                data.add(mapCandidature(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture candidatures: " + e.getMessage(), e);
        }
        return data;
    }

    public List<Candidature> getByEquipe(int equipeId) {
        if (!hasConnection()) {
            return LOCAL_DATA.stream()
                    .filter(candidature -> candidature.getEquipeId() == equipeId)
                    .sorted(Comparator.comparingInt(Candidature::getId).reversed())
                    .toList();
        }
        List<Candidature> data = new ArrayList<>();
        String requete = """
                SELECT c.*, e.nom_equipe
                FROM candidature c
                JOIN equipe e ON c.equipe_id = e.id
                WHERE c.equipe_id = ?
                ORDER BY c.id DESC
                """;
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, equipeId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                data.add(mapCandidature(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture candidatures equipe: " + e.getMessage(), e);
        }
        return data;
    }

    public List<Candidature> getByAccountUsername(String username) {
        if (username == null || username.isBlank()) {
            return List.of();
        }
        if (!hasConnection()) {
            return LOCAL_DATA.stream()
                    .filter(candidature -> username.equalsIgnoreCase(valueOrEmpty(candidature.getAccountUsername())))
                    .sorted(Comparator.comparingInt(Candidature::getId).reversed())
                    .toList();
        }
        List<Candidature> data = new ArrayList<>();
        for (Candidature candidature : getData()) {
            if (username.equalsIgnoreCase(valueOrEmpty(candidature.getAccountUsername()))) {
                data.add(candidature);
            }
        }
        return data;
    }

    public Candidature getAcceptedForUser(String username) {
        return getByAccountUsername(username).stream()
                .filter(candidature -> "Acceptee".equalsIgnoreCase(valueOrEmpty(candidature.getStatut())))
                .findFirst()
                .orElse(null);
    }

    public List<Candidature> getAcceptedMembersByEquipe(int equipeId) {
        return getByEquipe(equipeId).stream()
                .filter(candidature -> "Acceptee".equalsIgnoreCase(valueOrEmpty(candidature.getStatut())))
                .toList();
    }

    public void acceptMembership(Candidature candidature) {
        if (candidature == null) {
            return;
        }
        String username = candidature.getAccountUsername();
        if (username != null && !username.isBlank()) {
            for (Candidature other : getByAccountUsername(username)) {
                if (other.getId() == candidature.getId()) {
                    continue;
                }
                if ("Acceptee".equalsIgnoreCase(valueOrEmpty(other.getStatut()))) {
                    other.setStatut("Quittee");
                    updateEntity(other.getId(), other);
                } else if ("En attente".equalsIgnoreCase(valueOrEmpty(other.getStatut()))) {
                    other.setStatut("Refusee");
                    updateEntity(other.getId(), other);
                }
            }
        }
        candidature.setStatut("Acceptee");
        updateEntity(candidature.getId(), candidature);
    }

    public void excludeMember(Candidature candidature) {
        if (candidature == null) {
            return;
        }
        candidature.setStatut("Exclu");
        updateEntity(candidature.getId(), candidature);
    }

    public void deleteByEquipe(int equipeId) {
        if (!hasConnection()) {
            LOCAL_DATA.removeIf(candidature -> candidature.getEquipeId() == equipeId);
            return;
        }
        String requete = "DELETE FROM candidature WHERE equipe_id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, equipeId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression candidatures equipe: " + e.getMessage(), e);
        }
    }

    public boolean existsForUserAndEquipe(String username, int equipeId, Integer excludeId) {
        String normalized = valueOrEmpty(username).trim();
        if (normalized.isBlank() || equipeId <= 0) {
            return false;
        }
        return getData().stream()
                .anyMatch(candidature -> normalized.equalsIgnoreCase(valueOrEmpty(candidature.getAccountUsername()).trim())
                        && candidature.getEquipeId() == equipeId
                        && (excludeId == null || candidature.getId() != excludeId));
    }

    private Candidature mapCandidature(ResultSet rs) throws SQLException {
        Candidature candidature = new Candidature();
        candidature.setId(rs.getInt("id"));
        candidature.setPseudoJoueur(rs.getString("pseudo_joueur"));
        candidature.setNiveau(rs.getString("niveau"));
        candidature.setRolePrefere(rs.getString("role_prefere"));
        candidature.setRegion(rs.getString("region"));
        candidature.setDisponibilite(rs.getString("disponibilite"));
        String motivation = rs.getString("motivation");
        if (motivation == null || motivation.isBlank()) {
            motivation = rs.getString("reason");
        }
        candidature.setMotivation(motivation);
        candidature.setStatut(rs.getString("statut"));
        candidature.setDateCandidature(rs.getTimestamp("date_candidature").toLocalDateTime());
        candidature.setEquipeId(rs.getInt("equipe_id"));
        candidature.setEquipeNom(rs.getString("nom_equipe"));
        String accountUsername = null;
        try {
            accountUsername = rs.getString("account_username");
        } catch (SQLException ignored) {
        }
        if (accountUsername == null) {
            try {
                accountUsername = rs.getString("user_username");
            } catch (SQLException ignored) {
            }
        }
        if (accountUsername == null) {
            try {
                accountUsername = rs.getString("username");
            } catch (SQLException ignored) {
            }
        }
        candidature.setAccountUsername(accountUsername);
        return candidature;
    }

    private boolean hasConnection() {
        return MyConnection.getInstance().getCnx() != null;
    }

    private Map<String, Object> buildInsertValues(Candidature candidature) throws SQLException {
        Set<String> columns = getCandidatureColumns();
        Map<String, Object> values = new LinkedHashMap<>();
        addIfPresent(values, columns, "pseudo_joueur", candidature.getPseudoJoueur());
        addIfPresent(values, columns, "niveau", candidature.getNiveau());
        addIfPresent(values, columns, "role_prefere", candidature.getRolePrefere());
        addIfPresent(values, columns, "play_style", candidature.getRolePrefere());
        addIfPresent(values, columns, "region", candidature.getRegion());
        addIfPresent(values, columns, "disponibilite", candidature.getDisponibilite());
        addIfPresent(values, columns, "motivation", candidature.getMotivation());
        addIfPresent(values, columns, "reason", candidature.getMotivation());
        addIfPresent(values, columns, "statut", candidature.getStatut());
        addIfPresent(values, columns, "status", candidature.getStatut());
        addIfPresent(values, columns, "date_candidature", Timestamp.valueOf(candidature.getDateCandidature()));
        addIfPresent(values, columns, "equipe_id", candidature.getEquipeId());
        addIfPresent(values, columns, "account_username", candidature.getAccountUsername());
        addIfPresent(values, columns, "user_username", candidature.getAccountUsername());
        addIfPresent(values, columns, "username", candidature.getAccountUsername());
        return values;
    }

    private Map<String, Object> buildUpdateValues(Candidature candidature) throws SQLException {
        Set<String> columns = getCandidatureColumns();
        Map<String, Object> values = new LinkedHashMap<>();
        addIfPresent(values, columns, "pseudo_joueur", candidature.getPseudoJoueur());
        addIfPresent(values, columns, "niveau", candidature.getNiveau());
        addIfPresent(values, columns, "role_prefere", candidature.getRolePrefere());
        addIfPresent(values, columns, "play_style", candidature.getRolePrefere());
        addIfPresent(values, columns, "region", candidature.getRegion());
        addIfPresent(values, columns, "disponibilite", candidature.getDisponibilite());
        addIfPresent(values, columns, "motivation", candidature.getMotivation());
        addIfPresent(values, columns, "reason", candidature.getMotivation());
        addIfPresent(values, columns, "statut", candidature.getStatut());
        addIfPresent(values, columns, "status", candidature.getStatut());
        addIfPresent(values, columns, "equipe_id", candidature.getEquipeId());
        addIfPresent(values, columns, "account_username", candidature.getAccountUsername());
        addIfPresent(values, columns, "user_username", candidature.getAccountUsername());
        addIfPresent(values, columns, "username", candidature.getAccountUsername());
        return values;
    }

    private Set<String> getCandidatureColumns() throws SQLException {
        Set<String> columns = new HashSet<>();
        ResultSet rs = MyConnection.getInstance().getCnx().getMetaData()
                .getColumns(null, null, "candidature", null);
        while (rs.next()) {
            columns.add(rs.getString("COLUMN_NAME").toLowerCase());
        }
        return columns;
    }

    private void addIfPresent(Map<String, Object> values, Set<String> columns, String column, Object value) {
        if (columns.contains(column.toLowerCase())) {
            values.put(column, value);
        }
    }

    private String buildInsertQuery(Map<String, Object> values) {
        String columns = String.join(", ", values.keySet());
        String placeholders = String.join(", ", values.keySet().stream().map(key -> "?").toList());
        return "INSERT INTO candidature (" + columns + ") VALUES (" + placeholders + ")";
    }

    private String buildUpdateQuery(Map<String, Object> values) {
        String setClause = values.keySet().stream()
                .map(key -> key + " = ?")
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        return "UPDATE candidature SET " + setClause + " WHERE id = ?";
    }

    private int bindValues(PreparedStatement pst, Map<String, Object> values) throws SQLException {
        int index = 1;
        for (Object value : values.values()) {
            pst.setObject(index++, value);
        }
        return index;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
