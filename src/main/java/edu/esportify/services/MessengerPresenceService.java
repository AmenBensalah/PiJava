package edu.esportify.services;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessengerPresenceService {
    private static final int ONLINE_WINDOW_SECONDS = 35;
    private static final Map<Integer, LocalDateTime> LAST_SEEN_BY_USER = new ConcurrentHashMap<>();
    private static final Map<String, LocalDateTime> TYPING_BY_KEY = new ConcurrentHashMap<>();

    public void heartbeat(int userId) {
        LAST_SEEN_BY_USER.put(userId, LocalDateTime.now());
    }

    public boolean isOnline(int userId) {
        LocalDateTime lastSeen = LAST_SEEN_BY_USER.get(userId);
        return lastSeen != null && lastSeen.isAfter(LocalDateTime.now().minusSeconds(ONLINE_WINDOW_SECONDS));
    }

    public void setTyping(int conversationId, int userId, boolean typing) {
        String key = key(conversationId, userId);
        if (typing) {
            TYPING_BY_KEY.put(key, LocalDateTime.now());
        } else {
            TYPING_BY_KEY.remove(key);
        }
        MessengerRealtimeBridge.publish();
    }

    public boolean isTyping(int conversationId, int userId) {
        LocalDateTime value = TYPING_BY_KEY.get(key(conversationId, userId));
        if (value == null) {
            return false;
        }
        if (value.isBefore(LocalDateTime.now().minusSeconds(4))) {
            TYPING_BY_KEY.remove(key(conversationId, userId));
            return false;
        }
        return true;
    }

    private String key(int conversationId, int userId) {
        return conversationId + ":" + userId;
    }
}
