package edu.projetJava.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GeminiAIService {
    private static final String API_KEY = "AIzaSyA3EWqR210VYy84Z_Y30So0PD4yK1dko-Q";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + API_KEY;

    public static String getResponse(String userMessage) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Echapper les guillemets et sauts de ligne pour le JSON
            String safeMessage = userMessage.replace("\"", "\\\"").replace("\n", " ");
            
            // On peut ajouter un "System prompt" contextuel "Tu es un assistant de boutique d'esport. Tu reponds brievement."
            String prompt = "Tu es un assistant pour la boutique E-SPORTIFY, une boutique de matériel d'e-sport et de gaming. Réponds brièvement (2-3 phrases max) à la question suivante de l'utilisateur : " + safeMessage;

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
                
                // Parsing JSON manuel hyper simple pour eviter de rajouter des dependances (Jackson/Gson) a votre projet
                int textIndex = jsonResponse.indexOf("\"text\": \"");
                if (textIndex != -1) {
                    textIndex += 9;
                    int endIndex = jsonResponse.indexOf("\"", textIndex);
                    // Remplacer les retours a la ligne et guillemets recu
                    return jsonResponse.substring(textIndex, endIndex).replace("\\n", "\n").replace("\\\"", "\"");
                }
                
                return "Désolé, je n'ai pas compris la réponse de l'IA.";
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String inputLine;
                StringBuilder errorResponse = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    errorResponse.append(inputLine);
                }
                in.close();
                System.err.println("API Error: " + errorResponse.toString());
                if (errorResponse.toString().contains("leaked") || responseCode == 403) {
                    return "Clé API invalide ou révoquée (403). Veuillez insérer une nouvelle clé Google Gemini dans GeminiAIService.java.";
                }
                return "L'assistant est hors ligne (Code " + responseCode + " - " + conn.getResponseMessage() + ").";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Une erreur serveur interne est survenue avec le chatbot.";
        }
    }
}
