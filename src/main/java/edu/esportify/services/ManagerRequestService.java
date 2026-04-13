package edu.esportify.services;

import edu.esportify.entities.ManagerRequest;
import edu.esportify.interfaces.IService;
import edu.esportify.tools.MyConnection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ManagerRequestService implements IService<ManagerRequest> {
    private static final List<ManagerRequest> LOCAL_DATA = new ArrayList<>();
    private static int nextId = 1;

    @Override
    public void addEntity(ManagerRequest request) {
        if (!hasConnection()) {
            saveLocal(request);
            return;
        }
        try {
            if (usesModernSchema()) {
                addModern(request);
            } else {
                addLegacy(request);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout demande manager: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEntity(ManagerRequest request) {
        if (!hasConnection()) {
            LOCAL_DATA.removeIf(item -> item.getId() == request.getId());
            return;
        }
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx()
                    .prepareStatement("DELETE FROM manager_request WHERE id = ?");
            pst.setInt(1, request.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression demande manager: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateEntity(int id, ManagerRequest request) {
        if (!hasConnection()) {
            request.setId(id);
            for (int i = 0; i < LOCAL_DATA.size(); i++) {
                if (LOCAL_DATA.get(i).getId() == id) {
                    LOCAL_DATA.set(i, request);
                    return;
                }
            }
            LOCAL_DATA.add(request);
            nextId = Math.max(nextId, id + 1);
            return;
        }
        try {
            if (usesModernSchema()) {
                updateModern(id, request);
            } else {
                updateLegacy(id, request);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur modification demande manager: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ManagerRequest> getData() {
        if (!hasConnection()) {
            return LOCAL_DATA.stream()
                    .sorted(Comparator.comparingInt(ManagerRequest::getId).reversed())
                    .toList();
        }
        List<ManagerRequest> data = new ArrayList<>();
        try {
            String query = usesModernSchema()
                    ? "SELECT * FROM manager_request ORDER BY id DESC"
                    : """
                        SELECT mr.*, u.email AS user_email
                        FROM manager_request mr
                        LEFT JOIN user u ON u.id = mr.user_id
                        ORDER BY mr.id DESC
                        """;
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                data.add(mapRequest(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture demandes manager: " + e.getMessage(), e);
        }
        return data;
    }

    public ManagerRequest getById(int id) {
        return getData().stream()
                .filter(request -> request.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public List<ManagerRequest> search(String keyword, String status) {
        String normalizedKeyword = valueOrEmpty(keyword).trim().toLowerCase();
        String normalizedStatus = valueOrEmpty(status).trim().toLowerCase();
        return getData().stream()
                .filter(request -> normalizedKeyword.isBlank()
                        || valueOrEmpty(request.getUsername()).toLowerCase().contains(normalizedKeyword)
                        || valueOrEmpty(request.getEmail()).toLowerCase().contains(normalizedKeyword)
                        || valueOrEmpty(request.getNiveau()).toLowerCase().contains(normalizedKeyword)
                        || valueOrEmpty(request.getMotivation()).toLowerCase().contains(normalizedKeyword))
                .filter(request -> normalizedStatus.isBlank()
                        || "tous".equals(normalizedStatus)
                        || valueOrEmpty(request.getStatus()).toLowerCase().equals(normalizedStatus)
                        || normalizeLegacyStatus(request.getStatus()).equals(normalizedStatus))
                .sorted(Comparator.comparing(ManagerRequest::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public boolean hasActiveRequestForUser(String username, Integer excludeId) {
        String normalized = valueOrEmpty(username).trim();
        if (normalized.isBlank()) {
            return false;
        }
        return getData().stream()
                .anyMatch(request -> normalized.equalsIgnoreCase(valueOrEmpty(request.getUsername()).trim())
                        && (excludeId == null || request.getId() != excludeId)
                        && ("En attente".equalsIgnoreCase(valueOrEmpty(request.getStatus()))
                        || "Acceptee".equalsIgnoreCase(valueOrEmpty(request.getStatus()))));
    }

    private void addModern(ManagerRequest request) throws SQLException {
        String query = """
                INSERT INTO manager_request (username, email, niveau, motivation, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
        pst.setString(1, request.getUsername());
        pst.setString(2, request.getEmail());
        pst.setString(3, request.getNiveau());
        pst.setString(4, request.getMotivation());
        pst.setString(5, request.getStatus());
        pst.setTimestamp(6, Timestamp.valueOf(request.getCreatedAt()));
        pst.executeUpdate();
    }

    private void addLegacy(ManagerRequest request) throws SQLException {
        String query = """
                INSERT INTO manager_request (motivation, status, created_at, user_id, nom, experience, niveau)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
        pst.setString(1, request.getMotivation());
        pst.setString(2, toLegacyStatus(request.getStatus()));
        pst.setTimestamp(3, Timestamp.valueOf(request.getCreatedAt()));
        pst.setInt(4, resolveUserId(request));
        pst.setString(5, request.getUsername());
        pst.setString(6, request.getMotivation());
        pst.setString(7, request.getNiveau());
        pst.executeUpdate();
    }

    private void updateModern(int id, ManagerRequest request) throws SQLException {
        String query = """
                UPDATE manager_request
                SET username = ?, email = ?, niveau = ?, motivation = ?, status = ?, created_at = ?
                WHERE id = ?
                """;
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
        pst.setString(1, request.getUsername());
        pst.setString(2, request.getEmail());
        pst.setString(3, request.getNiveau());
        pst.setString(4, request.getMotivation());
        pst.setString(5, request.getStatus());
        pst.setTimestamp(6, Timestamp.valueOf(request.getCreatedAt()));
        pst.setInt(7, id);
        pst.executeUpdate();
    }

    private void updateLegacy(int id, ManagerRequest request) throws SQLException {
        String query = """
                UPDATE manager_request
                SET motivation = ?, status = ?, created_at = ?, user_id = ?, nom = ?, experience = ?, niveau = ?
                WHERE id = ?
                """;
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
        pst.setString(1, request.getMotivation());
        pst.setString(2, toLegacyStatus(request.getStatus()));
        pst.setTimestamp(3, Timestamp.valueOf(request.getCreatedAt()));
        pst.setInt(4, resolveUserId(request));
        pst.setString(5, request.getUsername());
        pst.setString(6, request.getMotivation());
        pst.setString(7, request.getNiveau());
        pst.setInt(8, id);
        pst.executeUpdate();
    }

    private ManagerRequest mapRequest(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        boolean modernSchema = hasColumn(metaData, "username");

        ManagerRequest request = new ManagerRequest();
        request.setId(rs.getInt("id"));
        request.setUsername(modernSchema ? rs.getString("username") : rs.getString("nom"));
        request.setEmail(modernSchema ? rs.getString("email") : getStringIfPresent(rs, metaData, "user_email"));
        request.setNiveau(rs.getString("niveau"));
        request.setMotivation(modernSchema ? rs.getString("motivation") : coalesce(rs.getString("experience"), rs.getString("motivation")));
        request.setStatus(modernSchema ? rs.getString("status") : fromLegacyStatus(rs.getString("status")));
        Timestamp createdAt = rs.getTimestamp("created_at");
        request.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return request;
    }

    private boolean usesModernSchema() throws SQLException {
        try (Statement st = MyConnection.getInstance().getCnx().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM manager_request LIMIT 1")) {
            return hasColumn(rs.getMetaData(), "username");
        }
    }

    private boolean hasColumn(ResultSetMetaData metaData, String columnName) throws SQLException {
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))
                    || columnName.equalsIgnoreCase(metaData.getColumnName(i))) {
                return true;
            }
        }
        return false;
    }

    private String getStringIfPresent(ResultSet rs, ResultSetMetaData metaData, String columnName) throws SQLException {
        return hasColumn(metaData, columnName) ? rs.getString(columnName) : "";
    }

    private int resolveUserId(ManagerRequest request) throws SQLException {
        String username = valueOrEmpty(request.getUsername()).trim();
        String email = valueOrEmpty(request.getEmail()).trim();

        String query = """
                SELECT id
                FROM user
                WHERE LOWER(COALESCE(pseudo, '')) = LOWER(?)
                   OR LOWER(COALESCE(nom, '')) = LOWER(?)
                   OR LOWER(email) = LOWER(?)
                ORDER BY id
                LIMIT 1
                """;
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
        pst.setString(1, username);
        pst.setString(2, username);
        pst.setString(3, email);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }

        Statement fallbackStatement = MyConnection.getInstance().getCnx().createStatement();
        ResultSet fallback = fallbackStatement.executeQuery("SELECT id FROM user ORDER BY id LIMIT 1");
        if (fallback.next()) {
            return fallback.getInt("id");
        }
        throw new SQLException("Aucun utilisateur disponible pour lier la demande manager.");
    }

    private void saveLocal(ManagerRequest request) {
        if (request.getId() <= 0) {
            request.setId(nextId++);
        } else {
            nextId = Math.max(nextId, request.getId() + 1);
        }
        LOCAL_DATA.removeIf(item -> item.getId() == request.getId());
        LOCAL_DATA.add(request);
    }

    private boolean hasConnection() {
        return MyConnection.getInstance().getCnx() != null;
    }

    private String normalizeLegacyStatus(String status) {
        return valueOrEmpty(fromLegacyStatus(status)).toLowerCase();
    }

    private String fromLegacyStatus(String status) {
        String value = valueOrEmpty(status);
        if ("pending".equalsIgnoreCase(value)) {
            return "En attente";
        }
        if ("accepted".equalsIgnoreCase(value)) {
            return "Acceptee";
        }
        if ("rejected".equalsIgnoreCase(value) || "refused".equalsIgnoreCase(value)) {
            return "Refusee";
        }
        return value;
    }

    private String toLegacyStatus(String status) {
        String value = valueOrEmpty(status);
        if ("En attente".equalsIgnoreCase(value)) {
            return "pending";
        }
        if ("Acceptee".equalsIgnoreCase(value)) {
            return "accepted";
        }
        if ("Refusee".equalsIgnoreCase(value)) {
            return "rejected";
        }
        return value.toLowerCase();
    }

    private String coalesce(String first, String second) {
        return first == null || first.isBlank() ? valueOrEmpty(second) : first;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
