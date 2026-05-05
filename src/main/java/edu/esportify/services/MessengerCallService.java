package edu.esportify.services;

import edu.esportify.config.EnvConfig;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MessengerCallService {
    private static final String DAILY_ROOM_BASE_URL_ENV = "DAILY_ROOM_BASE_URL";
    private static final String JITSI_BASE_URL = "https://meet.jit.si";

    public String buildCallUrl(int conversationId, String peerDisplayName, boolean videoEnabled) {
        String roomName = "esportify-chat-" + conversationId;
        String displayName = peerDisplayName == null || peerDisplayName.isBlank() ? "Esportify User" : peerDisplayName;

        String dailyBase = trimToNull(EnvConfig.get(DAILY_ROOM_BASE_URL_ENV));
        if (dailyBase != null) {
            String base = dailyBase.endsWith("/") ? dailyBase.substring(0, dailyBase.length() - 1) : dailyBase;
            String query = "?name=" + URLEncoder.encode(displayName, StandardCharsets.UTF_8);
            if (!videoEnabled) {
                query += "&audioOnly=1";
            }
            return base + "/" + roomName + query;
        }

        String jitsiUrl = JITSI_BASE_URL + "/" + roomName;
        if (!videoEnabled) {
            return jitsiUrl + "#config.startWithVideoMuted=true";
        }
        return jitsiUrl;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
