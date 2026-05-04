package edu.esportify.services;

import edu.esportify.tools.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SocialInteractionService {
    private final MyConnection myConnection;

    public SocialInteractionService() {
        this(MyConnection.getInstance());
    }

    public SocialInteractionService(MyConnection myConnection) {
        this.myConnection = myConnection;
        this.myConnection.initializeDatabase();
    }

    public boolean toggleLike(int postId, int userId) {
        if (exists("post_likes", postId, userId)) {
            delete("post_likes", postId, userId);
            return false;
        }
        insertToggle("post_likes", postId, userId);
        return true;
    }

    public boolean toggleSave(int postId, int userId) {
        if (exists("post_saves", postId, userId)) {
            delete("post_saves", postId, userId);
            return false;
        }
        insertToggle("post_saves", postId, userId);
        return true;
    }

    public void addShare(int postId, int userId) {
        String sql = "INSERT INTO post_shares (post_id, user_id, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, postId);
            pst.setInt(2, userId);
            pst.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    public Set<Integer> getSavedPostIds(int userId) {
        return getPostIdSet("post_saves", userId);
    }

    public Set<Integer> getLikedPostIds(int userId) {
        return getPostIdSet("post_likes", userId);
    }

    public Map<Integer, Integer> getLikeCounts() {
        return getCounts("post_likes");
    }

    public Map<Integer, Integer> getShareCounts() {
        return getCounts("post_shares");
    }

    public List<Integer> getLikeUserIds(int postId) {
        String sql = "SELECT user_id FROM post_likes WHERE post_id = ? ORDER BY created_at DESC";
        List<Integer> userIds = new ArrayList<>();
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, postId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getInt("user_id"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
        return userIds;
    }

    private Set<Integer> getPostIdSet(String table, int userId) {
        String sql = "SELECT post_id FROM " + table + " WHERE user_id = ?";
        Set<Integer> ids = new HashSet<>();
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("post_id"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
        return ids;
    }

    private Map<Integer, Integer> getCounts(String table) {
        String sql = "SELECT post_id, COUNT(*) AS total FROM " + table + " GROUP BY post_id";
        Map<Integer, Integer> counts = new HashMap<>();
        try (Statement st = getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                counts.put(rs.getInt("post_id"), rs.getInt("total"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
        return counts;
    }

    private boolean exists(String table, int postId, int userId) {
        String sql = "SELECT 1 FROM " + table + " WHERE post_id = ? AND user_id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, postId);
            pst.setInt(2, userId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    private void insertToggle(String table, int postId, int userId) {
        String sql = "INSERT INTO " + table + " (post_id, user_id, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, postId);
            pst.setInt(2, userId);
            pst.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    private void delete(String table, int postId, int userId) {
        String sql = "DELETE FROM " + table + " WHERE post_id = ? AND user_id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, postId);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees: " + e.getMessage(), e);
        }
    }

    private Connection getConnection() {
        return myConnection.getCnx();
    }
}
