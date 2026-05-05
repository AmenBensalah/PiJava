package edu.esportify.services;

import edu.esportify.entities.Candidature;
import edu.esportify.entities.Equipe;
import edu.esportify.entities.TeamComment;
import edu.esportify.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TeamCommentService {
    private static final List<TeamComment> LOCAL_DATA = new ArrayList<>();
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private final CandidatureService candidatureService = new CandidatureService();

    public List<TeamComment> getByTeam(int teamId) {
        if (teamId <= 0) {
            return List.of();
        }
        if (!hasConnection()) {
            ensureSeedData(teamId);
            return LOCAL_DATA.stream()
                    .filter(comment -> comment.getTeamId() == teamId)
                    .sorted(Comparator.comparing(TeamComment::getCreatedAt))
                    .toList();
        }

        ensureSeedData(teamId);
        List<TeamComment> comments = new ArrayList<>();
        String query = """
                SELECT id, team_id, author_username, content, created_at
                FROM team_comment
                WHERE team_id = ?
                ORDER BY created_at ASC, id ASC
                """;
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
            pst.setInt(1, teamId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                comments.add(mapComment(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture discussion equipe: " + e.getMessage(), e);
        }
        return comments;
    }

    public void addMessage(int teamId, String authorUsername, String content) {
        String normalizedAuthor = safe(authorUsername);
        String normalizedContent = safe(content);
        if (teamId <= 0 || normalizedAuthor.isBlank() || normalizedContent.isBlank()) {
            return;
        }

        TeamComment comment = new TeamComment();
        comment.setTeamId(teamId);
        comment.setAuthorUsername(normalizedAuthor);
        comment.setContent(normalizedContent);
        comment.setCreatedAt(LocalDateTime.now());

        if (!hasConnection()) {
            comment.setId(NEXT_ID.getAndIncrement());
            LOCAL_DATA.add(comment);
            return;
        }

        String query = """
                INSERT INTO team_comment (team_id, author_username, content, created_at)
                VALUES (?, ?, ?, ?)
                """;
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
            pst.setInt(1, comment.getTeamId());
            pst.setString(2, comment.getAuthorUsername());
            pst.setString(3, comment.getContent());
            pst.setTimestamp(4, Timestamp.valueOf(comment.getCreatedAt()));
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout message equipe: " + e.getMessage(), e);
        }
    }

    public void deleteByTeam(int teamId) {
        if (teamId <= 0) {
            return;
        }
        if (!hasConnection()) {
            LOCAL_DATA.removeIf(comment -> comment.getTeamId() == teamId);
            return;
        }
        String query = "DELETE FROM team_comment WHERE team_id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
            pst.setInt(1, teamId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression discussion equipe: " + e.getMessage(), e);
        }
    }

    public boolean canParticipate(Equipe equipe, String username) {
        if (equipe == null || equipe.getId() <= 0) {
            return false;
        }
        String normalized = safe(username);
        if (normalized.isBlank()) {
            return false;
        }
        if (normalized.equalsIgnoreCase(safe(equipe.getManagerUsername()))) {
            return true;
        }
        return candidatureService.getAcceptedMembersByEquipe(equipe.getId()).stream()
                .map(Candidature::getAccountUsername)
                .anyMatch(memberUsername -> normalized.equalsIgnoreCase(safe(memberUsername)));
    }

    private TeamComment mapComment(ResultSet rs) throws SQLException {
        TeamComment comment = new TeamComment();
        comment.setId(rs.getInt("id"));
        comment.setTeamId(rs.getInt("team_id"));
        comment.setAuthorUsername(rs.getString("author_username"));
        comment.setContent(rs.getString("content"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            comment.setCreatedAt(createdAt.toLocalDateTime());
        }
        return comment;
    }

    private boolean hasConnection() {
        return MyConnection.getInstance().getCnx() != null;
    }

    private void ensureSeedData(int teamId) {
        if (teamId <= 0 || hasAnyMessage(teamId)) {
            return;
        }
        addSeedMessage(teamId, "system", "Salon d'equipe initialise. Utilisez cet espace pour vos annonces et votre coordination.");
        addSeedMessage(teamId, "coach.bot", "Pensez a partager ici vos disponibilites, vos plans de scrim et vos retours de match.");
    }

    private boolean hasAnyMessage(int teamId) {
        if (!hasConnection()) {
            return LOCAL_DATA.stream().anyMatch(comment -> comment.getTeamId() == teamId);
        }
        String query = "SELECT COUNT(*) FROM team_comment WHERE team_id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
            pst.setInt(1, teamId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Erreur verification discussion equipe: " + e.getMessage(), e);
        }
    }

    private void addSeedMessage(int teamId, String authorUsername, String content) {
        TeamComment comment = new TeamComment();
        comment.setTeamId(teamId);
        comment.setAuthorUsername(authorUsername);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());

        if (!hasConnection()) {
            comment.setId(NEXT_ID.getAndIncrement());
            LOCAL_DATA.add(comment);
            return;
        }

        String query = """
                INSERT INTO team_comment (team_id, author_username, content, created_at)
                VALUES (?, ?, ?, ?)
                """;
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(query);
            pst.setInt(1, comment.getTeamId());
            pst.setString(2, comment.getAuthorUsername());
            pst.setString(3, comment.getContent());
            pst.setTimestamp(4, Timestamp.valueOf(comment.getCreatedAt()));
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur initialisation discussion equipe: " + e.getMessage(), e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
