package edu.esportify.services;

import edu.esportify.entities.FilActualite;
import edu.esportify.interfaces.IService;
import edu.esportify.tools.MyConnection;

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
import java.util.Optional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class FilActualiteService implements IService<FilActualite> {
    private final MyConnection myConnection;

    public FilActualiteService() {
        this(MyConnection.getInstance());
    }

    public FilActualiteService(MyConnection myConnection) {
        this.myConnection = myConnection;
        this.myConnection.initializeDatabase();
    }

    @Override
    public void addEntity(FilActualite actualite) {
        normalize(actualite, false);
        validate(actualite, false);

        String requete = """
                INSERT INTO posts (content, media_type, media_filename, created_at, image_path, video_url, is_event, event_title, event_date, event_location, max_participants, author_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement pst = getConnection().prepareStatement(requete, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pst, actualite);
            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    actualite.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEntity(FilActualite actualite) {
        if (actualite == null || actualite.getId() <= 0) {
            throw new IllegalArgumentException("Veuillez selectionner une publication valide a supprimer.");
        }
        deleteById(actualite.getId());
    }

    @Override
    public void updateEntity(int id, FilActualite actualite) {
        if (id <= 0) {
            throw new IllegalArgumentException("L'identifiant de la publication est invalide.");
        }
        actualite.setId(id);
        normalize(actualite, true);
        validate(actualite, true);

        String requete = """
                UPDATE posts
                SET content = ?, media_type = ?, media_filename = ?, created_at = ?, image_path = ?, video_url = ?, is_event = ?, event_title = ?, event_date = ?, event_location = ?, max_participants = ?, author_id = ?
                WHERE id = ?
                """;

        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            fillStatement(pst, actualite);
            pst.setInt(13, id);
            int rows = pst.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Aucune publication ne correspond a l'identifiant " + id + ".");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    @Override
    public List<FilActualite> getData() {
        List<FilActualite> data = new ArrayList<>();
        String requete = "SELECT * FROM posts ORDER BY created_at DESC, id DESC";
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(requete)) {
            while (rs.next()) {
                data.add(mapResult(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
        return data;
    }

    public Optional<FilActualite> findById(int id) {
        String requete = "SELECT * FROM posts WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResult(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    public void deleteById(int id) {
        String requete = "DELETE FROM posts WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            pst.setInt(1, id);
            int rows = pst.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Aucune publication ne correspond a l'identifiant " + id + ".");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    public List<FilActualite> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getData();
        }

        List<FilActualite> data = new ArrayList<>();
        String requete = """
                SELECT * FROM posts
                WHERE LOWER(COALESCE(content, '')) LIKE ?
                   OR LOWER(COALESCE(event_title, '')) LIKE ?
                   OR LOWER(COALESCE(event_location, '')) LIKE ?
                ORDER BY created_at DESC, id DESC
                """;
        try (PreparedStatement pst = getConnection().prepareStatement(requete)) {
            String token = "%" + keyword.trim().toLowerCase() + "%";
            pst.setString(1, token);
            pst.setString(2, token);
            pst.setString(3, token);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    data.add(mapResult(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
        return data;
    }

    public List<FilActualite> getRecommendedPosts(int userId) {
        List<Integer> recommendedIds = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "recommender.py", String.valueOf(userId));
            pb.directory(new java.io.File(System.getProperty("user.dir"))); // Project root
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            process.waitFor();
            
            if (output != null && !output.trim().isEmpty()) {
                Gson gson = new Gson();
                recommendedIds = gson.fromJson(output, new TypeToken<List<Integer>>(){}.getType());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        
        // Fetch posts from DB
        List<FilActualite> recommendations = new ArrayList<>();
        if (!recommendedIds.isEmpty()) {
            StringBuilder query = new StringBuilder("SELECT * FROM posts WHERE id IN (");
            for (int i = 0; i < recommendedIds.size(); i++) {
                query.append("?");
                if (i < recommendedIds.size() - 1) query.append(",");
            }
            query.append(") ORDER BY created_at DESC");
            
            try (PreparedStatement pst = getConnection().prepareStatement(query.toString())) {
                for (int i = 0; i < recommendedIds.size(); i++) {
                    pst.setInt(i + 1, recommendedIds.get(i));
                }
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        recommendations.add(mapResult(rs));
                    }
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
            }
        }

        if (recommendations.isEmpty()) {
            List<FilActualite> fallback = getData();
            return fallback.size() <= 5 ? fallback : fallback.subList(0, 5);
        }

        return recommendations;
    }

    public void clearAll() {
        try (Statement st = getConnection().createStatement()) {
            st.executeUpdate("DELETE FROM posts");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    private void fillStatement(PreparedStatement pst, FilActualite actualite) throws SQLException {
        pst.setString(1, actualite.getContent());
        pst.setString(2, actualite.getMediaType());
        pst.setString(3, actualite.getMediaFilename());
        pst.setTimestamp(4, Timestamp.valueOf(actualite.getCreatedAt()));
        pst.setString(5, actualite.getImagePath());
        pst.setString(6, actualite.getVideoUrl());
        pst.setBoolean(7, actualite.isEvent());
        pst.setString(8, actualite.getEventTitle());
        if (actualite.getEventDate() == null) {
            pst.setTimestamp(9, null);
        } else {
            pst.setTimestamp(9, Timestamp.valueOf(actualite.getEventDate()));
        }
        pst.setString(10, actualite.getEventLocation());
        if (actualite.getMaxParticipants() == null) {
            pst.setObject(11, null);
        } else {
            pst.setInt(11, actualite.getMaxParticipants());
        }
        if (actualite.getAuthorId() == null) {
            pst.setObject(12, null);
        } else {
            pst.setInt(12, actualite.getAuthorId());
        }
    }

    private FilActualite mapResult(ResultSet rs) throws SQLException {
        FilActualite actualite = new FilActualite();
        actualite.setId(rs.getInt("id"));
        actualite.setContent(rs.getString("content"));
        actualite.setMediaType(rs.getString("media_type"));
        actualite.setMediaFilename(rs.getString("media_filename"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        actualite.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        actualite.setImagePath(rs.getString("image_path"));
        actualite.setVideoUrl(rs.getString("video_url"));
        actualite.setEvent(rs.getBoolean("is_event"));
        actualite.setEventTitle(rs.getString("event_title"));
        Timestamp eventDate = rs.getTimestamp("event_date");
        actualite.setEventDate(eventDate == null ? null : eventDate.toLocalDateTime());
        actualite.setEventLocation(rs.getString("event_location"));
        Object maxParticipants = rs.getObject("max_participants");
        actualite.setMaxParticipants(maxParticipants == null ? null : rs.getInt("max_participants"));
        Object authorId = rs.getObject("author_id");
        actualite.setAuthorId(authorId == null ? null : rs.getInt("author_id"));
        return actualite;
    }

    private void normalize(FilActualite actualite, boolean keepCreatedAt) {
        if (actualite == null) {
            return;
        }
        actualite.setContent(trimToNull(actualite.getContent()));
        actualite.setImagePath(trimToNull(actualite.getImagePath()));
        actualite.setVideoUrl(trimToNull(actualite.getVideoUrl()));
        actualite.setEventTitle(trimToNull(actualite.getEventTitle()));
        actualite.setEventLocation(trimToNull(actualite.getEventLocation()));
        actualite.setMediaFilename(trimToNull(actualite.getMediaFilename()));
        if (!keepCreatedAt || actualite.getCreatedAt() == null) {
            actualite.setCreatedAt(LocalDateTime.now());
        }

        if (actualite.isEvent()) {
            actualite.setMediaType("event");
        } else if (actualite.getVideoUrl() != null) {
            actualite.setMediaType("video");
        } else if (actualite.getImagePath() != null) {
            actualite.setMediaType("image");
        } else {
            actualite.setMediaType("text");
        }
    }

    private void validate(FilActualite actualite, boolean updateMode) {
        if (actualite == null) {
            throw new IllegalArgumentException("La publication est obligatoire.");
        }
        if (updateMode && actualite.getId() <= 0) {
            throw new IllegalArgumentException("L'identifiant de la publication est obligatoire.");
        }
        if (!actualite.isEvent() && isBlank(actualite.getContent()) && isBlank(actualite.getImagePath()) && isBlank(actualite.getVideoUrl())) {
            throw new IllegalArgumentException("Ajoutez au moins un contenu: texte, image ou video.");
        }
        if (actualite.getContent() != null && actualite.getContent().length() > 5000) {
            throw new IllegalArgumentException("Le contenu ne doit pas depasser 5000 caracteres.");
        }
        if (actualite.getImagePath() != null && !isValidLink(actualite.getImagePath())) {
            throw new IllegalArgumentException("Le lien de l'image est invalide.");
        }
        if (actualite.getVideoUrl() != null && !isValidLink(actualite.getVideoUrl())) {
            throw new IllegalArgumentException("Le lien de la video est invalide.");
        }
        if (actualite.isEvent()) {
            if (isBlank(actualite.getEventTitle())) {
                throw new IllegalArgumentException("Le titre de l'evenement est obligatoire.");
            }
            if (actualite.getEventTitle().length() > 180) {
                throw new IllegalArgumentException("Le titre de l'evenement ne doit pas depasser 180 caracteres.");
            }
            if (actualite.getEventDate() == null) {
                throw new IllegalArgumentException("La date de l'evenement est obligatoire.");
            }
            if (actualite.getEventDate().isBefore(LocalDateTime.now().minusYears(1))) {
                throw new IllegalArgumentException("La date de l'evenement est invalide.");
            }
            if (isBlank(actualite.getEventLocation())) {
                throw new IllegalArgumentException("Le lieu ou lien de l'evenement est obligatoire.");
            }
            if (actualite.getMaxParticipants() == null || actualite.getMaxParticipants() < 1) {
                throw new IllegalArgumentException("Le nombre de places doit etre superieur ou egal a 1.");
            }
        } else {
            actualite.setEventTitle(null);
            actualite.setEventDate(null);
            actualite.setEventLocation(null);
            actualite.setMaxParticipants(null);
        }
        if (actualite.getAuthorId() != null && actualite.getAuthorId() <= 0) {
            throw new IllegalArgumentException("L'identifiant auteur doit etre un entier positif.");
        }
    }

    private boolean isValidLink(String value) {
        if (value == null || value.isBlank()) {
            return true;
        }
        if (value.startsWith("/uploads/")) {
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

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Connection getConnection() {
        return myConnection.getCnx();
    }
}
