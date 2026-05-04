package edu.connexion3a77.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.connexion3a77.entities.RawgGame;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class RawgApiService {

    private static final String API_BASE_URL = "https://api.rawg.io/api/games";
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<RawgGame> searchGames(String query, int pageSize) {
        String apiKey = resolveApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("API key RAWG manquante. Configure RAWG_API_KEY ou config/local.properties.");
        }

        String normalizedQuery = query == null ? "" : query.trim();
        String encodedQuery = URLEncoder.encode(normalizedQuery, StandardCharsets.UTF_8);
        String url = API_BASE_URL + "?key=" + apiKey + "&page_size=" + pageSize + "&search=" + encodedQuery;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("Erreur RAWG (" + response.statusCode() + "). Verifie la cle API et les quotas.");
            }
            return parseGames(response.body());
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de contacter RAWG. Verifie ta connexion internet.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Requete RAWG interrompue.", e);
        }
    }

    private List<RawgGame> parseGames(String json) {
        List<RawgGame> games = new ArrayList<>();

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray results = root.has("results") && root.get("results").isJsonArray()
                ? root.getAsJsonArray("results")
                : new JsonArray();

        for (JsonElement element : results) {
            JsonObject obj = element.getAsJsonObject();
            int id = readInt(obj, "id", 0);
            String name = readString(obj, "name", "Inconnu");
            double rating = readDouble(obj, "rating", 0.0);
            String released = readString(obj, "released", "N/A");
            String imageUrl = readString(obj, "background_image", "");
            String slug = readString(obj, "slug", "");
            String rawgUrl = slug.isBlank() ? ("https://rawg.io/games/" + id) : ("https://rawg.io/games/" + slug);

            games.add(new RawgGame(id, name, rating, released, imageUrl, rawgUrl));
        }

        return games;
    }

    private String resolveApiKey() {
        String envKey = System.getenv("RAWG_API_KEY");
        if (envKey != null && !envKey.isBlank()) {
            return envKey.trim();
        }

        String systemProperty = System.getProperty("rawg.api.key");
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty.trim();
        }

        Path localConfig = Path.of("config", "local.properties");
        if (Files.exists(localConfig)) {
            try {
                Properties p = new Properties();
                p.load(Files.newInputStream(localConfig));
                String fileKey = p.getProperty("rawg.api.key");
                if (fileKey != null && !fileKey.isBlank()) {
                    return fileKey.trim();
                }
            } catch (IOException ignored) {
                // Ignore local file read issue and continue.
            }
        }
        return null;
    }

    private String readString(JsonObject obj, String key, String fallback) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            return fallback;
        }
        return obj.get(key).getAsString();
    }

    private int readInt(JsonObject obj, String key, int fallback) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private double readDouble(JsonObject obj, String key, double fallback) {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            return fallback;
        }
        try {
            return obj.get(key).getAsDouble();
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
