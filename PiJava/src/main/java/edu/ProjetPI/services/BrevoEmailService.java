package edu.ProjetPI.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrevoEmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final Map<String, String> LOCAL_CONFIG = loadLocalConfig();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(12))
            .build();

    public void sendPasswordResetCode(String toEmail, String resetCode) {
        String apiKey = settingRequired("PIJAVA_BREVO_API_KEY");
        String senderEmail = setting("PIJAVA_BREVO_SENDER_EMAIL", "").trim();
        String senderName = setting("PIJAVA_BREVO_SENDER_NAME", "PI Java").trim();

        if (senderEmail.isBlank()) {
            throw new IllegalStateException("PIJAVA_BREVO_SENDER_EMAIL is required.");
        }

        String subject = "Code de reinitialisation de mot de passe";
        String html = "<p>Bonjour,</p>"
                + "<p>Votre code de reinitialisation est :</p>"
                + "<h2 style=\"letter-spacing:2px;\">" + escapeHtml(resetCode) + "</h2>"
                + "<p>Ce code expire dans 10 minutes.</p>"
                + "<p>Si vous n'etes pas a l'origine de cette demande, ignorez cet email.</p>";

        String payload = "{"
                + "\"sender\":{\"name\":\"" + jsonEscape(senderName) + "\",\"email\":\"" + jsonEscape(senderEmail) + "\"},"
                + "\"to\":[{\"email\":\"" + jsonEscape(toEmail) + "\"}],"
                + "\"subject\":\"" + jsonEscape(subject) + "\","
                + "\"htmlContent\":\"" + jsonEscape(html) + "\""
                + "}";

        HttpRequest request = HttpRequest.newBuilder(URI.create(BREVO_API_URL))
                .timeout(Duration.ofSeconds(20))
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int status = response.statusCode();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Brevo send failed (HTTP " + status + "): " + trimBody(response.body()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Email sending interrupted.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to send reset email: " + e.getMessage(), e);
        }
    }

    private static String settingRequired(String key) {
        String value = setting(key, "").trim();
        if (value.isBlank()) {
            throw new IllegalStateException(key + " is required.");
        }
        return value;
    }

    private static String setting(String key, String defaultValue) {
        String fromEnv = System.getenv().getOrDefault(key, "").trim();
        if (!fromEnv.isBlank()) {
            return fromEnv;
        }
        String fromProps = System.getProperty(key, "").trim();
        if (!fromProps.isBlank()) {
            return fromProps;
        }
        String fromDotEnv = LOCAL_CONFIG.getOrDefault(key, "").trim();
        if (!fromDotEnv.isBlank()) {
            return fromDotEnv;
        }
        return defaultValue;
    }

    private static Map<String, String> loadLocalConfig() {
        Map<String, String> values = new HashMap<>();
        loadDotEnvFile(Path.of(".env"), values);
        loadDotEnvFile(Path.of("PiJava", ".env"), values);
        return values;
    }

    private static void loadDotEnvFile(Path path, Map<String, String> values) {
        if (!Files.exists(path)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            for (String rawLine : lines) {
                if (rawLine == null) {
                    continue;
                }
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int idx = line.indexOf('=');
                if (idx <= 0) {
                    continue;
                }
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                if (value.length() >= 2) {
                    boolean quoted = (value.startsWith("\"") && value.endsWith("\""))
                            || (value.startsWith("'") && value.endsWith("'"));
                    if (quoted) {
                        value = value.substring(1, value.length() - 1);
                    }
                }
                if (!key.isBlank()) {
                    values.putIfAbsent(key, value);
                }
            }
        } catch (Exception ignored) {
            // Ignore local config parse errors; env vars remain the primary source.
        }
    }

    private static String jsonEscape(String value) {
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String trimBody(String body) {
        if (body == null) {
            return "";
        }
        String trimmed = body.trim();
        return trimmed.length() > 300 ? trimmed.substring(0, 300) + "..." : trimmed;
    }
}
