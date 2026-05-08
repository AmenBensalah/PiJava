package edu.projetJava.services;

import edu.esportify.config.EnvConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class GeminiAIService {
    private static final String DEFAULT_MODEL = "gemini-2.5-flash";
    private static final String DEFAULT_API_KEY = "";
    private static final Properties APP_PROPERTIES = loadAppProperties();

    public static String getResponse(String userMessage) {
        try {
            String apiKey = resolveApiKey();
            if (apiKey.isBlank()) {
                return "Le chatbot IA n'est pas configure. Ajoutez GEMINI_API_KEY ou gemini.api_key.";
            }

            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/" + resolveModel() + ":generateContent?key=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String safeMessage = userMessage.replace("\"", "\\\"").replace("\n", " ");
            String prompt = "Tu es un assistant pour la boutique E-SPORTIFY, une boutique de materiel d'e-sport et de gaming. Reponds brievement (2-3 phrases max) a la question suivante de l'utilisateur : " + safeMessage;

            String requestBody = "{\n  \"contents\": [\n    {\n      \"parts\": [\n        {\n          \"text\": \"" + prompt + "\"\n        }\n      ]\n    }\n  ]\n}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String jsonResponse = response.toString();
                java.util.regex.Matcher matcher = java.util.regex.Pattern
                        .compile("\"text\"\\s*:\\s*\"((?:\\\\\"|[^\"])*)\"")
                        .matcher(jsonResponse);
                if (matcher.find()) {
                    return matcher.group(1).replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
                }

                return "Desole, je n'ai pas compris la reponse de l'IA.";
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            String inputLine;
            StringBuilder errorResponse = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                errorResponse.append(inputLine);
            }
            in.close();
            System.err.println("API Error: " + errorResponse);
            if (errorResponse.toString().contains("leaked") || responseCode == 403) {
                return "Cle API invalide ou revoquee (403). Veuillez verifier GEMINI_API_KEY ou gemini.api_key.";
            }
            return "L'assistant est hors ligne (Code " + responseCode + " - " + conn.getResponseMessage() + ").";
        } catch (Exception e) {
            e.printStackTrace();
            return "Une erreur serveur interne est survenue avec le chatbot.";
        }
    }

    private static String resolveApiKey() {
        String configuredKey = APP_PROPERTIES.getProperty("gemini.api_key", DEFAULT_API_KEY);
        return EnvConfig.get("GEMINI_API_KEY", configuredKey).trim();
    }

    private static String resolveModel() {
        return APP_PROPERTIES.getProperty("gemini.model", DEFAULT_MODEL).trim();
    }

    private static Properties loadAppProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignored) {
            // Optional config file.
        }
        return properties;
    }
}
