package edu.esportify.services;

import edu.esportify.entities.Commentaire;
import edu.esportify.interfaces.IService;
import edu.esportify.tools.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentaireService implements IService<Commentaire> {
    private final MyConnection myConnection;

    public CommentaireService() {
        this(MyConnection.getInstance());
    }

    public CommentaireService(MyConnection myConnection) {
        this.myConnection = myConnection;
        this.myConnection.initializeDatabase();
    }

    @Override
    public void addEntity(Commentaire comment) {
        normalize(comment, false);
        validate(comment, false);

        String sql = """
                INSERT INTO commentaires (author_id, post_id, content, created_at)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement pst = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillStatement(pst, comment);
            pst.executeUpdate();
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (rs.next()) {
                    comment.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEntity(Commentaire comment) {
        if (comment == null || comment.getId() <= 0) {
            throw new IllegalArgumentException("Veuillez selectionner un commentaire valide a supprimer.");
        }
        deleteById(comment.getId());
    }

    @Override
    public void updateEntity(int id, Commentaire comment) {
        if (id <= 0) {
            throw new IllegalArgumentException("L'identifiant du commentaire est invalide.");
        }
        comment.setId(id);
        normalize(comment, true);
        validate(comment, true);

        String sql = """
                UPDATE commentaires
                SET author_id = ?, post_id = ?, content = ?, created_at = ?
                WHERE id = ?
                """;
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            fillStatement(pst, comment);
            pst.setInt(5, id);
            int rows = pst.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Aucun commentaire ne correspond a l'identifiant " + id + ".");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Commentaire> getData() {
        List<Commentaire> data = new ArrayList<>();
        String sql = "SELECT * FROM commentaires ORDER BY created_at DESC, id DESC";
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

    public void clearAll() {
        try (Statement st = getConnection().createStatement()) {
            st.executeUpdate("DELETE FROM commentaires");
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    public void deleteById(int id) {
        String sql = "DELETE FROM commentaires WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            int rows = pst.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Aucun commentaire ne correspond a l'identifiant " + id + ".");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    private void fillStatement(PreparedStatement pst, Commentaire comment) throws SQLException {
        pst.setInt(1, comment.getAuthorId());
        pst.setInt(2, comment.getPostId());
        pst.setString(3, comment.getContent());
        pst.setTimestamp(4, Timestamp.valueOf(comment.getCreatedAt()));
    }

    private Commentaire mapResult(ResultSet rs) throws SQLException {
        Commentaire comment = new Commentaire();
        comment.setId(rs.getInt("id"));
        comment.setAuthorId(rs.getInt("author_id"));
        comment.setPostId(rs.getInt("post_id"));
        comment.setContent(rs.getString("content"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        comment.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        return comment;
    }

    private void normalize(Commentaire comment, boolean keepCreatedAt) {
        if (comment == null) {
            return;
        }
        comment.setContent(trimToNull(comment.getContent()));
        if (!keepCreatedAt || comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }
    }

    private void validate(Commentaire comment, boolean updateMode) {
        if (comment == null) {
            throw new IllegalArgumentException("Le commentaire est obligatoire.");
        }
        if (updateMode && comment.getId() <= 0) {
            throw new IllegalArgumentException("L'identifiant du commentaire est obligatoire.");
        }
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            throw new IllegalArgumentException("Le contenu du commentaire est obligatoire.");
        }
        if (comment.getAuthorId() <= 0) {
            throw new IllegalArgumentException("L'identifiant auteur est obligatoire.");
        }
        if (comment.getPostId() <= 0) {
            throw new IllegalArgumentException("L'identifiant de la publication est obligatoire.");
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
