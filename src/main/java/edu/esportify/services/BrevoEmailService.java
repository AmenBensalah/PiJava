package edu.esportify.services;

import edu.esportify.config.EnvConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BrevoEmailService {
    private static final String API_URL = "https://api.brevo.com/v3/smtp/email";

    public boolean isConfigured() {
        return !resolveApiKey().isBlank() && !resolveSenderEmail().isBlank();
    }

    public void sendTeamBanReport(List<String> recipients, String teamName, String reportBody) {
        if (recipients == null || recipients.isEmpty() || !isConfigured()) {
            return;
        }

        String payload = buildPayload(recipients, teamName, reportBody);
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(API_URL).toURL().openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("content-type", "application/json");
            connection.setRequestProperty("api-key", resolveApiKey());

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                String errorBody = readResponseBody(connection);
                throw new IllegalStateException(buildErrorMessage(status, errorBody));
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'envoyer l'email Brevo: " + e.getMessage(), e);
        }
    }

    private String buildPayload(List<String> recipients, String teamName, String reportBody) {
        String senderEmail = escapeJson(resolveSenderEmail());
        String senderName = escapeJson(resolveSenderName());
        String subject = escapeJson("Bannissement de l'equipe " + safe(teamName));
        String htmlContent = escapeJson(buildHtmlContent(teamName, reportBody));
        String recipientJson = recipients.stream()
                .map(email -> "{\"email\":\"" + escapeJson(email) + "\"}")
                .reduce((left, right) -> left + "," + right)
                .orElse("");

        return """
                {
                  "sender": {
                    "name": "%s",
                    "email": "%s"
                  },
                  "to": [%s],
                  "subject": "%s",
                  "htmlContent": "%s"
                }
                """.formatted(senderName, senderEmail, recipientJson, subject, htmlContent);
    }

    private String buildHtmlContent(String teamName, String reportBody) {
        return "<html><body>"
                + "<h2>Bannissement d'equipe</h2>"
                + "<p>L'equipe <strong>" + escapeHtml(safe(teamName)) + "</strong> a ete bannie par un administrateur.</p>"
                + "<p><strong>Rapport:</strong></p>"
                + "<pre style=\"font-family:Arial,sans-serif;white-space:pre-wrap;\">"
                + escapeHtml(safe(reportBody))
                + "</pre>"
                + "</body></html>";
    }

    private String resolveApiKey() {
        return readConfig("brevo.api.key", "BREVO_API_KEY");
    }

    private String resolveSenderEmail() {
        return readConfig("brevo.sender.email", "BREVO_SENDER_EMAIL");
    }

    private String resolveSenderName() {
        String value = readConfig("brevo.sender.name", "BREVO_SENDER_NAME");
        return value.isBlank() ? "E-sportify" : value;
    }

    private String readResponseBody(HttpURLConnection connection) {
        InputStream stream = null;
        try {
            stream = connection.getErrorStream();
            if (stream == null) {
                stream = connection.getInputStream();
            }
            if (stream == null) {
                return "";
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (IOException ignored) {
            return "";
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private String buildErrorMessage(int status, String errorBody) {
        if (errorBody == null || errorBody.isBlank()) {
            return "Brevo a retourne le statut HTTP " + status;
        }

        String normalizedBody = errorBody.toLowerCase();
        if (status == 401 && normalizedBody.contains("unrecognised ip address")) {
            return "Brevo refuse la requete car l'adresse IP publique actuelle n'est pas autorisee. "
                    + "Ajoute cette IP dans Brevo > Security > Authorised IPs, puis reessaie. "
                    + "Details: HTTP " + status + " - " + errorBody;
        }

        return "Brevo a retourne le statut HTTP " + status + " - " + errorBody;
    }

    private String readConfig(String propertyName, String envName) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }
        String envValue = EnvConfig.get(envName);
        return envValue == null ? "" : envValue.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private String escapeJson(String value) {
        return safe(value)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "")
                .replace("\n", "\\n");
    }
}
