package edu.esportify.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreamingIntegrationService {
    private static final String YOUTUBE_API_KEY_ENV = "YOUTUBE_API_KEY";
    private static final String YOUTUBE_API_KEY_PROP = "streaming.youtube.api_key";
    private static final String YOUTUBE_LIVE_QUERY_PROP = "streaming.youtube.live_query";
    private static final String YOUTUBE_HIGHLIGHTS_QUERY_PROP = "streaming.youtube.highlights_query";
    private static final String DB_PROPERTIES_RESOURCE = "db.properties";
    private static final String DEFAULT_LIVE_QUERY = "esports live";
    private static final String DEFAULT_HIGHLIGHTS_QUERY = "esports highlights";

    private static final Pattern JSON_FIELD_PATTERN = Pattern.compile("\"%s\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Properties properties = loadProperties(DB_PROPERTIES_RESOURCE);

    public StreamingSnapshot loadSnapshot() {
        List<String> status = new ArrayList<>();
        String apiKey = resolveApiKey();
        if (apiKey == null) {
            status.add("YouTube non configure: ajoute YOUTUBE_API_KEY ou streaming.youtube.api_key dans db.properties.");
            return new StreamingSnapshot(List.of(), List.of(), String.join("  ", status));
        }

        List<LiveStreamCard> lives = fetchYoutubeLiveStreams(apiKey, status);
        List<HighlightCard> highlights = fetchYoutubeHighlights(apiKey, status);
        if (status.isEmpty()) {
            status.add("Flux YouTube synchronises.");
        }
        return new StreamingSnapshot(lives, highlights, String.join("  ", status));
    }

    private List<LiveStreamCard> fetchYoutubeLiveStreams(String apiKey, List<String> status) {
        String liveQuery = trimToNull(properties.getProperty(YOUTUBE_LIVE_QUERY_PROP, DEFAULT_LIVE_QUERY));
        if (liveQuery == null) {
            liveQuery = DEFAULT_LIVE_QUERY;
        }
        try {
            String url = "https://www.googleapis.com/youtube/v3/search"
                    + "?part=snippet"
                    + "&maxResults=10"
                    + "&order=viewCount"
                    + "&eventType=live"
                    + "&type=video"
                    + "&q=" + URLEncoder.encode(liveQuery, StandardCharsets.UTF_8)
                    + "&key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            List<LiveStreamCard> cards = parseYoutubeLiveStreams(response.body());
            status.add(cards.isEmpty() ? "YouTube Live connecte (aucun live sur la requete actuelle)." : "YouTube Live connecte.");
            return cards;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            status.add("YouTube Live indisponible: " + ex.getMessage());
            return List.of();
        }
    }

    private List<HighlightCard> fetchYoutubeHighlights(String apiKey, List<String> status) {
        String highlightsQuery = trimToNull(properties.getProperty(YOUTUBE_HIGHLIGHTS_QUERY_PROP, DEFAULT_HIGHLIGHTS_QUERY));
        if (highlightsQuery == null) {
            highlightsQuery = DEFAULT_HIGHLIGHTS_QUERY;
        }
        try {
            String url = "https://www.googleapis.com/youtube/v3/search"
                    + "?part=snippet"
                    + "&maxResults=10"
                    + "&order=date"
                    + "&type=video"
                    + "&q=" + URLEncoder.encode(highlightsQuery, StandardCharsets.UTF_8)
                    + "&key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            List<HighlightCard> cards = parseYoutubeHighlights(response.body());
            status.add(cards.isEmpty() ? "YouTube Highlights connecte (aucun resultat)." : "YouTube Highlights connecte.");
            return cards;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            status.add("YouTube Highlights indisponible: " + ex.getMessage());
            return List.of();
        }
    }

    private List<LiveStreamCard> parseYoutubeLiveStreams(String json) {
        String itemsArray = extractArray(json, "items");
        if (itemsArray == null) {
            return List.of();
        }
        List<LiveStreamCard> cards = new ArrayList<>();
        for (String object : parseObjectArray(itemsArray)) {
            String videoId = extractJsonValue(object, "videoId");
            String title = extractJsonValue(object, "title");
            String channel = extractJsonValue(object, "channelTitle");
            String publishedAt = extractJsonValue(object, "publishedAt");
            String thumbnail = extractFirstUrl(object);
            if (videoId == null || title == null) {
                continue;
            }
            cards.add(new LiveStreamCard(
                    "YouTube Live",
                    defaultText(channel),
                    title,
                    defaultText(publishedAt),
                    0,
                    "https://www.youtube.com/watch?v=" + videoId,
                    thumbnail
            ));
        }
        return cards;
    }

    private List<HighlightCard> parseYoutubeHighlights(String json) {
        String itemsArray = extractArray(json, "items");
        if (itemsArray == null) {
            return List.of();
        }
        List<HighlightCard> cards = new ArrayList<>();
        for (String object : parseObjectArray(itemsArray)) {
            String videoId = extractJsonValue(object, "videoId");
            String title = extractJsonValue(object, "title");
            String channel = extractJsonValue(object, "channelTitle");
            String publishedAt = extractJsonValue(object, "publishedAt");
            String thumbnail = extractFirstUrl(object);
            if (videoId == null || title == null) {
                continue;
            }
            cards.add(new HighlightCard(
                    "YouTube",
                    title,
                    defaultText(channel),
                    defaultText(publishedAt),
                    "https://www.youtube.com/watch?v=" + videoId,
                    thumbnail
            ));
        }
        return cards;
    }

    private String resolveApiKey() {
        String envValue = trimToNull(System.getenv(YOUTUBE_API_KEY_ENV));
        if (envValue != null) {
            return envValue;
        }
        return trimToNull(properties.getProperty(YOUTUBE_API_KEY_PROP));
    }

    private String extractArray(String json, String fieldName) {
        String token = "\"" + fieldName + "\"";
        int fieldIndex = json.indexOf(token);
        if (fieldIndex < 0) {
            return null;
        }
        int start = json.indexOf('[', fieldIndex);
        if (start < 0) {
            return null;
        }
        int depth = 0;
        for (int i = start; i < json.length(); i++) {
            char current = json.charAt(i);
            if (current == '[') {
                depth++;
            } else if (current == ']') {
                depth--;
                if (depth == 0) {
                    return json.substring(start + 1, i);
                }
            }
        }
        return null;
    }

    private List<String> parseObjectArray(String arrayContent) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < arrayContent.length(); i++) {
            char current = arrayContent.charAt(i);
            if (current == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (current == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objects.add(arrayContent.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }

    private String extractJsonValue(String source, String key) {
        Pattern pattern = Pattern.compile(String.format(Locale.ROOT, JSON_FIELD_PATTERN.pattern(), Pattern.quote(key)));
        Matcher matcher = pattern.matcher(source);
        if (!matcher.find()) {
            return null;
        }
        return unescapeJson(matcher.group(1));
    }

    private String extractFirstUrl(String source) {
        Matcher matcher = Pattern.compile("\"url\"\\s*:\\s*\"(https:[^\"]+)\"").matcher(source);
        if (!matcher.find()) {
            return null;
        }
        return unescapeJson(matcher.group(1));
    }

    private String unescapeJson(String value) {
        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\/", "/")
                .replace("\\u0026", "&");
    }

    private String defaultText(String value) {
        return value == null || value.isBlank() ? "--" : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Properties loadProperties(String resource) {
        Properties loaded = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (inputStream != null) {
                loaded.load(inputStream);
            }
        } catch (IOException ignored) {
            return loaded;
        }
        return loaded;
    }

    public record StreamingSnapshot(List<LiveStreamCard> liveStreams, List<HighlightCard> highlights, String status) {}

    public record LiveStreamCard(String platform, String channelName, String title, String gameName,
                                 int viewerCount, String link, String thumbnailUrl) {}

    public record HighlightCard(String platform, String title, String channelName, String publishedAt,
                                String link, String thumbnailUrl) {}
}
