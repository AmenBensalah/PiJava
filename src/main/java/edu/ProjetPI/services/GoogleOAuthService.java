package edu.ProjetPI.services;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleOAuthService {

    private static final String AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_ENDPOINT = "https://openidconnect.googleapis.com/v1/userinfo";
    private static final String DEFAULT_REDIRECT_URI = "http://127.0.0.1:53682/callback";
    private static final Pattern JSON_STRING_PATTERN_TEMPLATE = Pattern.compile("\"%s\"\\s*:\\s*\"([^\"]*)\"");
    private static final Map<String, String> LOCAL_CONFIG = loadLocalConfig();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();
    private final SecureRandom secureRandom = new SecureRandom();

    public GoogleProfile authenticate() {
        String clientId = settingRequired("PIJAVA_GOOGLE_CLIENT_ID");
        String clientSecret = settingRequired("PIJAVA_GOOGLE_CLIENT_SECRET");
        String redirectUri = setting("PIJAVA_GOOGLE_REDIRECT_URI", DEFAULT_REDIRECT_URI);
        URI redirect = URI.create(redirectUri);
        int port = redirect.getPort();
        if (port <= 0) {
            throw new IllegalStateException("PIJAVA_GOOGLE_REDIRECT_URI must include a localhost port.");
        }

        String state = randomToken();
        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        HttpServer server = startCallbackServer(redirect, state, codeFuture);
        try {
            server.start();
            String authUrl = buildAuthUrl(clientId, redirectUri, state);
            openBrowser(authUrl);
            String code = codeFuture.get(180, TimeUnit.SECONDS);
            String accessToken = exchangeCodeForAccessToken(code, clientId, clientSecret, redirectUri);
            return fetchGoogleProfile(accessToken);
        } catch (Exception e) {
            throw new IllegalStateException("Google login failed: " + e.getMessage(), e);
        } finally {
            server.stop(0);
        }
    }

    private HttpServer startCallbackServer(URI redirect, String state, CompletableFuture<String> codeFuture) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", redirect.getPort()), 0);
            String path = redirect.getPath() == null || redirect.getPath().isBlank() ? "/" : redirect.getPath();
            server.createContext(path, exchange -> handleCallback(exchange, state, codeFuture));
            return server;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start local OAuth callback server: " + e.getMessage(), e);
        }
    }

    private void handleCallback(HttpExchange exchange, String expectedState, CompletableFuture<String> codeFuture) throws IOException {
        try {
            Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
            String returnedState = query.getOrDefault("state", "");
            String error = query.get("error");
            String code = query.get("code");

            if (error != null && !error.isBlank()) {
                codeFuture.completeExceptionally(new IllegalStateException("Google returned error: " + error));
                writeHtml(exchange, 400, "<h3>Google sign-in canceled or denied.</h3><p>You can close this tab.</p>");
                return;
            }
            if (!expectedState.equals(returnedState)) {
                codeFuture.completeExceptionally(new IllegalStateException("OAuth state mismatch."));
                writeHtml(exchange, 400, "<h3>Invalid OAuth state.</h3><p>You can close this tab.</p>");
                return;
            }
            if (code == null || code.isBlank()) {
                codeFuture.completeExceptionally(new IllegalStateException("Missing authorization code."));
                writeHtml(exchange, 400, "<h3>Missing authorization code.</h3><p>You can close this tab.</p>");
                return;
            }

            codeFuture.complete(code);
            writeHtml(exchange, 200, "<h3>Google sign-in successful.</h3><p>You can close this tab and return to the app.</p>");
        } catch (Exception e) {
            codeFuture.completeExceptionally(new IllegalStateException("Invalid OAuth callback: " + e.getMessage(), e));
            writeHtml(exchange, 500, "<h3>OAuth callback failed.</h3><p>You can close this tab.</p>");
        }
    }

    private static void writeHtml(HttpExchange exchange, int status, String html) throws IOException {
        byte[] body = ("<!doctype html><html><body style=\"font-family:sans-serif;\">" + html + "</body></html>")
                .getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private String buildAuthUrl(String clientId, String redirectUri, String state) {
        StringJoiner params = new StringJoiner("&");
        params.add("client_id=" + enc(clientId));
        params.add("redirect_uri=" + enc(redirectUri));
        params.add("response_type=code");
        params.add("scope=" + enc("openid email profile"));
        params.add("state=" + enc(state));
        params.add("prompt=select_account");
        params.add("access_type=online");
        return AUTH_ENDPOINT + "?" + params;
    }

    private String exchangeCodeForAccessToken(String code, String clientId, String clientSecret, String redirectUri) {
        StringJoiner form = new StringJoiner("&");
        form.add("code=" + enc(code));
        form.add("client_id=" + enc(clientId));
        form.add("client_secret=" + enc(clientSecret));
        form.add("redirect_uri=" + enc(redirectUri));
        form.add("grant_type=authorization_code");

        HttpRequest request = HttpRequest.newBuilder(URI.create(TOKEN_ENDPOINT))
                .timeout(Duration.ofSeconds(25))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form.toString(), StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Token exchange failed (HTTP " + response.statusCode() + "): " + trim(response.body()));
            }
            String accessToken = jsonString(response.body(), "access_token");
            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalStateException("Google token response did not include access_token.");
            }
            return accessToken;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Token exchange interrupted.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to exchange authorization code: " + e.getMessage(), e);
        }
    }

    private GoogleProfile fetchGoogleProfile(String accessToken) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(USERINFO_ENDPOINT))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Unable to fetch Google profile (HTTP " + response.statusCode() + "): " + trim(response.body()));
            }
            String body = response.body();
            String email = jsonString(body, "email");
            String name = jsonString(body, "name");
            String sub = jsonString(body, "sub");
            String picture = jsonString(body, "picture");
            if (email == null || email.isBlank()) {
                throw new IllegalStateException("Google profile did not include an email.");
            }
            return new GoogleProfile(email, name == null ? "" : name, sub == null ? "" : sub, picture == null ? "" : picture);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Profile fetch interrupted.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to fetch Google profile: " + e.getMessage(), e);
        }
    }

    private void openBrowser(String url) {
        try {
            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                throw new IllegalStateException("Desktop browser opening is not supported.");
            }
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to open browser for Google login: " + e.getMessage(), e);
        }
    }

    private String randomToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(48);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> out = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return out;
        }
        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            String k = idx >= 0 ? pair.substring(0, idx) : pair;
            String v = idx >= 0 ? pair.substring(idx + 1) : "";
            String key = URLDecoder.decode(k, StandardCharsets.UTF_8);
            String val = URLDecoder.decode(v, StandardCharsets.UTF_8);
            out.put(key, val);
        }
        return out;
    }

    private static String jsonString(String json, String key) {
        Pattern pattern = Pattern.compile(String.format(JSON_STRING_PATTERN_TEMPLATE.pattern(), Pattern.quote(key)));
        Matcher matcher = pattern.matcher(json == null ? "" : json);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1)
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    private static String trim(String body) {
        if (body == null) {
            return "";
        }
        String t = body.trim();
        return t.length() > 320 ? t.substring(0, 320) + "..." : t;
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
                String k = line.substring(0, idx).trim();
                String v = line.substring(idx + 1).trim();
                if (v.length() >= 2) {
                    boolean quoted = (v.startsWith("\"") && v.endsWith("\""))
                            || (v.startsWith("'") && v.endsWith("'"));
                    if (quoted) {
                        v = v.substring(1, v.length() - 1);
                    }
                }
                if (!k.isBlank()) {
                    values.putIfAbsent(k, v);
                }
            }
        } catch (Exception ignored) {
            // Ignore malformed local file and fallback to env/system properties.
        }
    }

    public record GoogleProfile(String email, String name, String sub, String pictureUrl) {
    }
}
