package edu.esportify.entities;

import java.time.LocalDateTime;

public class ConversationPreview {
    private int conversationId;
    private int peerUserId;
    private String peerDisplayName;
    private String peerAvatarLabel;
    private boolean peerOnline;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
    private boolean seen;

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public int getPeerUserId() {
        return peerUserId;
    }

    public void setPeerUserId(int peerUserId) {
        this.peerUserId = peerUserId;
    }

    public String getPeerDisplayName() {
        return peerDisplayName;
    }

    public void setPeerDisplayName(String peerDisplayName) {
        this.peerDisplayName = peerDisplayName;
    }

    public String getPeerAvatarLabel() {
        return peerAvatarLabel;
    }

    public void setPeerAvatarLabel(String peerAvatarLabel) {
        this.peerAvatarLabel = peerAvatarLabel;
    }

    public boolean isPeerOnline() {
        return peerOnline;
    }

    public void setPeerOnline(boolean peerOnline) {
        this.peerOnline = peerOnline;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}
