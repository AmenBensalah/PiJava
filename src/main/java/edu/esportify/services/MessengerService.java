package edu.esportify.services;

import edu.esportify.entities.ConversationPreview;
import edu.esportify.entities.MessengerMessage;
import edu.esportify.entities.UserProfile;
import edu.esportify.tools.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MessengerService {
    private final MyConnection myConnection;
    private final UserDirectoryService userDirectoryService = new UserDirectoryService();
    private final MessengerPresenceService presenceService = new MessengerPresenceService();

    public MessengerService() {
        this(MyConnection.getInstance());
    }

    public MessengerService(MyConnection myConnection) {
        this.myConnection = myConnection;
        this.myConnection.initializeDatabase();
    }

    public List<ConversationPreview> getConversationsForUser(int userId) {
        Map<Integer, UserProfile> usersById = userDirectoryService.getUsersById();
        List<ConversationPreview> conversations = new ArrayList<>();
        String sql = """
                SELECT c.id AS conversation_id,
                       cp_other.user_id AS peer_user_id,
                       m.content AS last_content,
                       m.attachment_path AS last_attachment,
                       m.created_at AS last_created_at,
                       m.seen_at AS last_seen_at,
                       (
                           SELECT COUNT(*)
                           FROM messages mu
                           WHERE mu.conversation_id = c.id
                             AND mu.sender_id <> ?
                             AND mu.seen_at IS NULL
                       ) AS unread_count
                FROM conversations c
                JOIN conversation_participants cp_self ON cp_self.conversation_id = c.id AND cp_self.user_id = ?
                JOIN conversation_participants cp_other ON cp_other.conversation_id = c.id AND cp_other.user_id <> ?
                LEFT JOIN messages m ON m.id = c.last_message_id
                ORDER BY COALESCE(m.created_at, c.updated_at) DESC, c.id DESC
                """;
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.setInt(2, userId);
            pst.setInt(3, userId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    int peerUserId = rs.getInt("peer_user_id");
                    UserProfile peer = usersById.get(peerUserId);
                    ConversationPreview preview = new ConversationPreview();
                    preview.setConversationId(rs.getInt("conversation_id"));
                    preview.setPeerUserId(peerUserId);
                    preview.setPeerDisplayName(peer == null ? "User " + peerUserId : peer.getDisplayName());
                    preview.setPeerAvatarLabel(peer == null ? "U" : peer.getAvatarLabel());
                    preview.setPeerOnline(presenceService.isOnline(peerUserId));
                    preview.setLastMessage(buildPreviewText(rs.getString("last_content"), rs.getString("last_attachment")));
                    Timestamp createdAt = rs.getTimestamp("last_created_at");
                    preview.setLastMessageAt(createdAt == null ? null : createdAt.toLocalDateTime());
                    preview.setUnreadCount(rs.getInt("unread_count"));
                    preview.setSeen(rs.getTimestamp("last_seen_at") != null || rs.getInt("unread_count") == 0);
                    conversations.add(preview);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees Messenger: " + e.getMessage(), e);
        }
        return conversations;
    }

    public List<MessengerMessage> getMessages(int conversationId, int currentUserId, int limit, Integer beforeMessageId) {
        Map<Integer, UserProfile> usersById = userDirectoryService.getUsersById();
        List<MessengerMessage> messages = new ArrayList<>();
        String sql = """
                SELECT m.*
                FROM messages m
                WHERE m.conversation_id = ?
                """ + (beforeMessageId == null ? "" : " AND m.id < ? ") + """
                ORDER BY m.id DESC
                LIMIT ?
                """;
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, conversationId);
            int index = 2;
            if (beforeMessageId != null) {
                pst.setInt(index++, beforeMessageId);
            }
            pst.setInt(index, limit);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    MessengerMessage message = mapMessage(rs, usersById);
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees Messenger: " + e.getMessage(), e);
        }
        messages.sort(Comparator.comparingInt(MessengerMessage::getId));
        markConversationSeen(conversationId, currentUserId);
        return messages;
    }

    public int findOrCreateConversation(int userA, int userB) {
        if (userA == userB) {
            throw new IllegalArgumentException("Une conversation directe requiert deux utilisateurs differents.");
        }
        String existingSql = """
                SELECT cp1.conversation_id
                FROM conversation_participants cp1
                JOIN conversation_participants cp2 ON cp2.conversation_id = cp1.conversation_id
                WHERE cp1.user_id = ? AND cp2.user_id = ?
                LIMIT 1
                """;
        try (PreparedStatement pst = getConnection().prepareStatement(existingSql)) {
            pst.setInt(1, userA);
            pst.setInt(2, userB);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees Messenger: " + e.getMessage(), e);
        }

        String createConversationSql = "INSERT INTO conversations (created_at, updated_at, last_message_id) VALUES (?, ?, NULL)";
        String createParticipantSql = "INSERT INTO conversation_participants (conversation_id, user_id, joined_at) VALUES (?, ?, ?)";
        try (PreparedStatement conversationPst = getConnection().prepareStatement(createConversationSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement participantPst = getConnection().prepareStatement(createParticipantSql)) {
            LocalDateTime now = LocalDateTime.now();
            conversationPst.setTimestamp(1, Timestamp.valueOf(now));
            conversationPst.setTimestamp(2, Timestamp.valueOf(now));
            conversationPst.executeUpdate();
            int conversationId;
            try (ResultSet rs = conversationPst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Impossible de creer la conversation.");
                }
                conversationId = rs.getInt(1);
            }
            addParticipant(participantPst, conversationId, userA, now);
            addParticipant(participantPst, conversationId, userB, now);
            MessengerRealtimeBridge.publish();
            return conversationId;
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees Messenger: " + e.getMessage(), e);
        }
    }

    public MessengerMessage sendMessage(int conversationId, int senderId, String content, String attachmentPath) {
        String normalizedContent = trimToNull(content);
        String normalizedAttachment = trimToNull(attachmentPath);
        if (normalizedContent == null && normalizedAttachment == null) {
            throw new IllegalArgumentException("Le message ne peut pas etre vide.");
        }
        String sql = """
                INSERT INTO messages (conversation_id, sender_id, content, attachment_path, created_at, seen_at)
                VALUES (?, ?, ?, ?, ?, NULL)
                """;
        try (PreparedStatement pst = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            LocalDateTime now = LocalDateTime.now();
            pst.setInt(1, conversationId);
            pst.setInt(2, senderId);
            pst.setString(3, normalizedContent);
            pst.setString(4, normalizedAttachment);
            pst.setTimestamp(5, Timestamp.valueOf(now));
            pst.executeUpdate();

            int messageId;
            try (ResultSet rs = pst.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new IllegalStateException("Impossible d'enregistrer le message.");
                }
                messageId = rs.getInt(1);
            }
            touchConversation(conversationId, messageId, now);
            MessengerRealtimeBridge.publish();
            return getMessageById(messageId);
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees Messenger: " + e.getMessage(), e);
        }
    }

    public void markConversationSeen(int conversationId, int viewerUserId) {
        String sql = """
                UPDATE messages
                SET seen_at = ?
                WHERE conversation_id = ?
                  AND sender_id <> ?
                  AND seen_at IS NULL
                """;
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pst.setInt(2, conversationId);
            pst.setInt(3, viewerUserId);
            int updated = pst.executeUpdate();
            if (updated > 0) {
                MessengerRealtimeBridge.publish();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees Messenger: " + e.getMessage(), e);
        }
    }

    public int getUnreadCount(int userId) {
        String sql = """
                SELECT COUNT(*)
                FROM messages m
                JOIN conversation_participants cp ON cp.conversation_id = m.conversation_id
                WHERE cp.user_id = ?
                  AND m.sender_id <> ?
                  AND m.seen_at IS NULL
                """;
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, userId);
            pst.setInt(2, userId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees Messenger: " + e.getMessage(), e);
        }
    }

    public List<UserProfile> getAvailableContacts(int currentUserId) {
        return userDirectoryService.getUsers().stream()
                .filter(user -> user.getId() != currentUserId)
                .sorted(Comparator.comparing(UserProfile::getDisplayName))
                .toList();
    }

    public MessengerPresenceService getPresenceService() {
        return presenceService;
    }

    private MessengerMessage getMessageById(int id) {
        String sql = "SELECT * FROM messages WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapMessage(rs, userDirectoryService.getUsersById());
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erreur base de donnees Messenger: " + e.getMessage(), e);
        }
        throw new IllegalStateException("Message introuvable.");
    }

    private MessengerMessage mapMessage(ResultSet rs, Map<Integer, UserProfile> usersById) throws SQLException {
        MessengerMessage message = new MessengerMessage();
        message.setId(rs.getInt("id"));
        message.setConversationId(rs.getInt("conversation_id"));
        message.setSenderId(rs.getInt("sender_id"));
        UserProfile sender = usersById.get(message.getSenderId());
        message.setSenderDisplayName(sender == null ? "User " + message.getSenderId() : sender.getDisplayName());
        message.setSenderAvatarLabel(sender == null ? "U" : sender.getAvatarLabel());
        message.setContent(rs.getString("content"));
        message.setAttachmentPath(rs.getString("attachment_path"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        message.setCreatedAt(createdAt == null ? null : createdAt.toLocalDateTime());
        Timestamp seenAt = rs.getTimestamp("seen_at");
        message.setSeenAt(seenAt == null ? null : seenAt.toLocalDateTime());
        return message;
    }

    private void touchConversation(int conversationId, int messageId, LocalDateTime updatedAt) throws SQLException {
        String sql = "UPDATE conversations SET updated_at = ?, last_message_id = ? WHERE id = ?";
        try (PreparedStatement pst = getConnection().prepareStatement(sql)) {
            pst.setTimestamp(1, Timestamp.valueOf(updatedAt));
            pst.setInt(2, messageId);
            pst.setInt(3, conversationId);
            pst.executeUpdate();
        }
    }

    private void addParticipant(PreparedStatement pst, int conversationId, int userId, LocalDateTime joinedAt) throws SQLException {
        pst.setInt(1, conversationId);
        pst.setInt(2, userId);
        pst.setTimestamp(3, Timestamp.valueOf(joinedAt));
        pst.executeUpdate();
    }

    private String buildPreviewText(String content, String attachmentPath) {
        String normalizedContent = trimToNull(content);
        if (normalizedContent != null) {
            return normalizedContent.length() > 56 ? normalizedContent.substring(0, 56) + "..." : normalizedContent;
        }
        return trimToNull(attachmentPath) == null ? "Conversation ouverte" : "Piece jointe";
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
