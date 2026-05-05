package edu.esportify.services;

import edu.esportify.entities.Equipe;
import edu.esportify.interfaces.IService;
import edu.esportify.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EquipeService implements IService<Equipe> {
    private static final List<Equipe> LOCAL_DATA = new ArrayList<>();
    private static int nextId = 1;

    @Override
    public void addEntity(Equipe equipe) {
        if (!hasConnection()) {
            if (equipe.getId() <= 0) {
                equipe.setId(nextId++);
            } else {
                nextId = Math.max(nextId, equipe.getId() + 1);
            }
            LOCAL_DATA.removeIf(item -> item.getId() == equipe.getId());
            LOCAL_DATA.add(equipe);
            return;
        }
        String requete = """
                INSERT INTO equipe
                (nom_equipe, logo, description, date_creation, classement, tag, region, max_members, is_private, is_active,
                 banned_until, ban_reason, ban_details, banned_by_admin, discord_invite_url, manager_username)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, equipe.getNomEquipe());
            pst.setString(2, equipe.getLogo());
            pst.setString(3, equipe.getDescription());
            pst.setTimestamp(4, Timestamp.valueOf(ensureDateCreation(equipe)));
            pst.setString(5, equipe.getClassement());
            pst.setString(6, equipe.getTag());
            pst.setString(7, equipe.getRegion());
            pst.setInt(8, equipe.getMaxMembers());
            pst.setBoolean(9, equipe.isPrivate());
            pst.setBoolean(10, equipe.isActive());
            pst.setTimestamp(11, equipe.getBannedUntil() == null ? null : Timestamp.valueOf(equipe.getBannedUntil()));
            pst.setString(12, equipe.getBanReason());
            pst.setString(13, equipe.getBanDetails());
            pst.setString(14, equipe.getBannedByAdmin());
            pst.setString(15, equipe.getDiscordInviteUrl());
            pst.setString(16, equipe.getManagerUsername());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout equipe: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEntity(Equipe equipe) {
        if (!hasConnection()) {
            LOCAL_DATA.removeIf(item -> item.getId() == equipe.getId());
            return;
        }
        String requete = "DELETE FROM equipe WHERE id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, equipe.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression equipe: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateEntity(int id, Equipe equipe) {
        if (!hasConnection()) {
            equipe.setId(id);
            for (int i = 0; i < LOCAL_DATA.size(); i++) {
                if (LOCAL_DATA.get(i).getId() == id) {
                    LOCAL_DATA.set(i, equipe);
                    return;
                }
            }
            LOCAL_DATA.add(equipe);
            nextId = Math.max(nextId, id + 1);
            return;
        }
        String requete = """
                UPDATE equipe SET
                nom_equipe = ?, logo = ?, description = ?, date_creation = ?, classement = ?, tag = ?, region = ?,
                max_members = ?, is_private = ?, is_active = ?, banned_until = ?, ban_reason = ?, ban_details = ?,
                banned_by_admin = ?, discord_invite_url = ?, manager_username = ?
                WHERE id = ?
                """;
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, equipe.getNomEquipe());
            pst.setString(2, equipe.getLogo());
            pst.setString(3, equipe.getDescription());
            pst.setTimestamp(4, Timestamp.valueOf(ensureDateCreation(equipe)));
            pst.setString(5, equipe.getClassement());
            pst.setString(6, equipe.getTag());
            pst.setString(7, equipe.getRegion());
            pst.setInt(8, equipe.getMaxMembers());
            pst.setBoolean(9, equipe.isPrivate());
            pst.setBoolean(10, equipe.isActive());
            pst.setTimestamp(11, equipe.getBannedUntil() == null ? null : Timestamp.valueOf(equipe.getBannedUntil()));
            pst.setString(12, equipe.getBanReason());
            pst.setString(13, equipe.getBanDetails());
            pst.setString(14, equipe.getBannedByAdmin());
            pst.setString(15, equipe.getDiscordInviteUrl());
            pst.setString(16, equipe.getManagerUsername());
            pst.setInt(17, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur modification equipe: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Equipe> getData() {
        if (!hasConnection()) {
            return LOCAL_DATA.stream()
                    .sorted(Comparator.comparingInt(Equipe::getId).reversed())
                    .toList();
        }
        List<Equipe> data = new ArrayList<>();
        String requete = "SELECT * FROM equipe ORDER BY id DESC";
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                data.add(mapEquipe(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture equipes: " + e.getMessage(), e);
        }
        return data;
    }

    public List<Equipe> search(String nom, String region, String classement) {
        if (!hasConnection()) {
            String lowerNom = nom.toLowerCase();
            String lowerRegion = region.toLowerCase();
            String lowerClassement = classement.toLowerCase();
            return LOCAL_DATA.stream()
                    .filter(equipe -> lowerNom.isBlank() || valueOrEmpty(equipe.getNomEquipe()).toLowerCase().contains(lowerNom))
                    .filter(equipe -> lowerRegion.isBlank() || valueOrEmpty(equipe.getRegion()).toLowerCase().contains(lowerRegion))
                    .filter(equipe -> lowerClassement.isBlank() || valueOrEmpty(equipe.getClassement()).toLowerCase().contains(lowerClassement))
                    .sorted(Comparator.comparingInt(Equipe::getId).reversed())
                    .toList();
        }
        List<Equipe> data = new ArrayList<>();
        String requete = """
                SELECT * FROM equipe
                WHERE (? = '' OR LOWER(nom_equipe) LIKE ?)
                  AND (? = '' OR LOWER(COALESCE(region, '')) LIKE ?)
                  AND (? = '' OR LOWER(classement) LIKE ?)
                ORDER BY id DESC
                """;
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, nom);
            pst.setString(2, "%" + nom.toLowerCase() + "%");
            pst.setString(3, region);
            pst.setString(4, "%" + region.toLowerCase() + "%");
            pst.setString(5, classement);
            pst.setString(6, "%" + classement.toLowerCase() + "%");
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                data.add(mapEquipe(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur recherche equipes: " + e.getMessage(), e);
        }
        return data;
    }

    public List<Equipe> searchForAdmin(String nom, String region, String manager, String statut) {
        String lowerNom = valueOrEmpty(nom).toLowerCase();
        String lowerRegion = valueOrEmpty(region).toLowerCase();
        String lowerManager = valueOrEmpty(manager).toLowerCase();
        String lowerStatus = valueOrEmpty(statut).toLowerCase();
        return getData().stream()
                .filter(equipe -> lowerNom.isBlank() || valueOrEmpty(equipe.getNomEquipe()).toLowerCase().contains(lowerNom))
                .filter(equipe -> lowerRegion.isBlank() || valueOrEmpty(equipe.getRegion()).toLowerCase().contains(lowerRegion))
                .filter(equipe -> lowerManager.isBlank() || valueOrEmpty(equipe.getManagerUsername()).toLowerCase().contains(lowerManager))
                .filter(equipe -> lowerStatus.isBlank()
                        || "tous".equals(lowerStatus)
                        || ("active".equals(lowerStatus) && equipe.isActive() && !equipe.isCurrentlyBanned())
                        || ("inactive".equals(lowerStatus) && !equipe.isActive() && !equipe.isCurrentlyBanned())
                        || ("bannie".equals(lowerStatus) && equipe.isCurrentlyBanned()))
                .sorted(Comparator.comparing(Equipe::getDateCreation, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public Equipe getById(int id) {
        if (!hasConnection()) {
            return LOCAL_DATA.stream()
                    .filter(equipe -> equipe.getId() == id)
                    .findFirst()
                    .orElse(null);
        }
        String requete = "SELECT * FROM equipe WHERE id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapEquipe(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture equipe: " + e.getMessage(), e);
        }
        return null;
    }

    public Equipe getByManagerUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        if (!hasConnection()) {
            return LOCAL_DATA.stream()
                    .filter(equipe -> username.equalsIgnoreCase(valueOrEmpty(equipe.getManagerUsername())))
                    .findFirst()
                    .orElse(null);
        }
        String requete = "SELECT * FROM equipe WHERE manager_username = ? ORDER BY id DESC LIMIT 1";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return mapEquipe(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture equipe manager: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean existsByName(String name, Integer excludeId) {
        String normalized = valueOrEmpty(name).trim();
        if (normalized.isBlank()) {
            return false;
        }
        return getData().stream()
                .anyMatch(equipe -> normalized.equalsIgnoreCase(valueOrEmpty(equipe.getNomEquipe()).trim())
                        && (excludeId == null || equipe.getId() != excludeId));
    }

    public boolean existsByTag(String tag, Integer excludeId) {
        String normalized = valueOrEmpty(tag).trim();
        if (normalized.isBlank()) {
            return false;
        }
        return getData().stream()
                .anyMatch(equipe -> normalized.equalsIgnoreCase(valueOrEmpty(equipe.getTag()).trim())
                        && (excludeId == null || equipe.getId() != excludeId));
    }

    public boolean managerHasAnotherTeam(String managerUsername, Integer excludeId) {
        String normalized = valueOrEmpty(managerUsername).trim();
        if (normalized.isBlank()) {
            return false;
        }
        return getData().stream()
                .anyMatch(equipe -> normalized.equalsIgnoreCase(valueOrEmpty(equipe.getManagerUsername()).trim())
                        && (excludeId == null || equipe.getId() != excludeId));
    }

    private Equipe mapEquipe(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        Equipe equipe = new Equipe();
        equipe.setId(rs.getInt("id"));
        equipe.setNomEquipe(getStringIfPresent(rs, metaData, "nom_equipe"));
        equipe.setLogo(getStringIfPresent(rs, metaData, "logo"));
        equipe.setDescription(getStringIfPresent(rs, metaData, "description"));
        Timestamp dateCreation = getTimestampIfPresent(rs, metaData, "date_creation");
        equipe.setDateCreation(dateCreation == null ? LocalDateTime.now() : dateCreation.toLocalDateTime());
        equipe.setClassement(getStringIfPresent(rs, metaData, "classement"));
        equipe.setTag(getStringIfPresent(rs, metaData, "tag"));
        equipe.setRegion(getStringIfPresent(rs, metaData, "region"));
        equipe.setMaxMembers(getIntIfPresent(rs, metaData, "max_members", 5));
        equipe.setPrivate(getBooleanIfPresent(rs, metaData, "is_private", false));
        equipe.setActive(getBooleanIfPresent(rs, metaData, "is_active", true));
        Timestamp bannedUntil = getTimestampIfPresent(rs, metaData, "banned_until");
        equipe.setBannedUntil(bannedUntil == null ? null : bannedUntil.toLocalDateTime());
        equipe.setBanReason(getStringIfPresent(rs, metaData, "ban_reason"));
        equipe.setBanDetails(getStringIfPresent(rs, metaData, "ban_details"));
        equipe.setBannedByAdmin(getStringIfPresent(rs, metaData, "banned_by_admin"));
        equipe.setDiscordInviteUrl(getStringIfPresent(rs, metaData, "discord_invite_url"));
        equipe.setManagerUsername(getStringIfPresent(rs, metaData, "manager_username"));
        return equipe;
    }

    private String getStringIfPresent(ResultSet rs, ResultSetMetaData metaData, String column) throws SQLException {
        return hasColumn(metaData, column) ? rs.getString(column) : null;
    }

    private Timestamp getTimestampIfPresent(ResultSet rs, ResultSetMetaData metaData, String column) throws SQLException {
        return hasColumn(metaData, column) ? rs.getTimestamp(column) : null;
    }

    private int getIntIfPresent(ResultSet rs, ResultSetMetaData metaData, String column, int defaultValue) throws SQLException {
        if (!hasColumn(metaData, column)) {
            return defaultValue;
        }
        int value = rs.getInt(column);
        return rs.wasNull() ? defaultValue : value;
    }

    private boolean getBooleanIfPresent(ResultSet rs, ResultSetMetaData metaData, String column, boolean defaultValue) throws SQLException {
        if (!hasColumn(metaData, column)) {
            return defaultValue;
        }
        boolean value = rs.getBoolean(column);
        return rs.wasNull() ? defaultValue : value;
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

    private boolean hasConnection() {
        return MyConnection.getInstance().getCnx() != null;
    }

    private LocalDateTime ensureDateCreation(Equipe equipe) {
        if (equipe.getDateCreation() == null) {
            equipe.setDateCreation(LocalDateTime.now());
        }
        return equipe.getDateCreation();
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
