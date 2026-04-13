package edu.connexion3a77.services;

import edu.connexion3a77.entities.Announcement;
import edu.connexion3a77.interfaces.IService;
import edu.connexion3a77.tools.MyConnection;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementService implements IService<Announcement> {
    private final MyConnection myConnection;

    public AnnouncementService() {
        this(MyConnection.getInstance());
    }

    public AnnouncementService(MyConnection myConnection) {
        this.myConnection = myConnection;
        this.myConnection.initializeDatabase();
    }

    @Override
    public void addEntity(Announcement announcement) {
        normalize(announcement, false);
        validate(announcement, false);

        String sql = """
                INSERT INTO announcements (title, content, tag, link, created_at, media_type, media_filename)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement pst = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pst, announcement);
            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    announcement.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEntity(Announcement announcement) {
        if (announcement == null || announcement.getId() <= 0) {
            throw new IllegalArgumentException("Veuillez selectionner une annonce valide a supprimer.");
        }
        deleteById(announcement.getId());
    }

    @Override
    public void updateEntity(int id, Announcement announcement) {
        if (id <= 0) {
            throw new IllegalArgumentException("L'identifiant de l'annonce est invalide.");
        }
        announcement.setId(id);
        normalize(announcement, true);
        validate(announcement, true);

        String sql = """
                UPDATE announcements
                SET title = ?, content = ?, tag = ?, link = ?, created_at = ?, media_type = ?, media_filename = ?
                WHERE id = ?
                """;
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            fillStatement(pst, announcement);
            pst.setInt(8, id);
            int rows = pst.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Aucune annonce ne correspond a l'identifiant " + id + ".");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Announcement> getData() {
        List<Announcement> data = new ArrayList<>();
        String sql = "SELECT * FROM announcements ORDER BY created_at DESC, id DESC";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                data.add(mapResult(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
        return data;
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM announcements WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            int rows = pst.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Aucune annonce ne correspond a l'identifiant " + id + ".");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    private void fillStatement(PreparedStatement pst, Announcement announcement) throws SQLException {
        pst.setString(1, announcement.getTitle());
        pst.setString(2, announcement.getContent());
        pst.setString(3, announcement.getTag());
        pst.setString(4, announcement.getLink());
        pst.setTimestamp(5, Timestamp.valueOf(announcement.getCreatedAt()));
        pst.setString(6, announcement.getMediaType());
        pst.setString(7, announcement.getMediaFilename());
    }

    private Announcement mapResult(ResultSet rs) throws SQLException {
        Announcement announcement = new Announcement();
        announcement.setId(rs.getInt("id"));
        announcement.setTitle(rs.getString("title"));
        announcement.setContent(rs.getString("content"));
        announcement.setTag(rs.getString("tag"));
        announcement.setLink(rs.getString("link"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        announcement.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        announcement.setMediaType(rs.getString("media_type"));
        announcement.setMediaFilename(rs.getString("media_filename"));
        return announcement;
    }

    private void normalize(Announcement announcement, boolean keepCreatedAt) {
        if (announcement == null) {
            return;
        }
        announcement.setTitle(trimToNull(announcement.getTitle()));
        announcement.setContent(trimToNull(announcement.getContent()));
        announcement.setTag(trimToNull(announcement.getTag()));
        announcement.setLink(trimToNull(announcement.getLink()));
        announcement.setMediaType(trimToNull(announcement.getMediaType()));
        announcement.setMediaFilename(trimToNull(announcement.getMediaFilename()));
        if (!keepCreatedAt || announcement.getCreatedAt() == null) {
            announcement.setCreatedAt(LocalDateTime.now());
        }
        if (announcement.getMediaType() == null) {
            announcement.setMediaType("text");
        }
    }

    private void validate(Announcement announcement, boolean updateMode) {
        if (announcement == null) {
            throw new IllegalArgumentException("L'annonce est obligatoire.");
        }
        if (updateMode && announcement.getId() <= 0) {
            throw new IllegalArgumentException("L'identifiant de l'annonce est obligatoire.");
        }
        if (announcement.getTitle() == null || announcement.getTitle().isBlank()) {
            throw new IllegalArgumentException("Le titre est obligatoire.");
        }
        if (announcement.getTitle().length() > 180) {
            throw new IllegalArgumentException("Le titre ne doit pas depasser 180 caracteres.");
        }
        if (announcement.getTag() == null || announcement.getTag().isBlank()) {
            throw new IllegalArgumentException("Le tag est obligatoire.");
        }
        if (announcement.getTag().length() > 60) {
            throw new IllegalArgumentException("Le tag ne doit pas depasser 60 caracteres.");
        }
        if (announcement.getLink() != null && !isValidLink(announcement.getLink())) {
            throw new IllegalArgumentException("Le lien est invalide.");
        }
    }

    private boolean isValidLink(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            URI uri = new URI(value);
            return "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Connection getConnection() {
        return myConnection.getCnx();
    }
}
