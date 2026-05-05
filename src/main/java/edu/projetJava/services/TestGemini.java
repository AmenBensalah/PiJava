package edu.projetJava.services;

import edu.esportify.config.EnvConfig;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestGemini {
    private static final String DEFAULT_API_KEY = "AIzaSyBny7McZPfvUKJqlTNj2UW5H_kF5yaQB-w";
    public static void main(String[] args) {
        String m = "gemini-flash-latest";
        System.out.println("Testing " + m);
        try {
            String apiKey = EnvConfig.get("TEST_GEMINI_API_KEY", EnvConfig.get("GEMINI_API_KEY", DEFAULT_API_KEY));
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/" + m + ":generateContent?key=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            String body = "{\"contents\":[{\"parts\":[{\"text\":\"Hello\"}]}]}";
            conn.getOutputStream().write(body.getBytes());
            int rc = conn.getResponseCode();
            System.out.println("Code: " + rc);
            if (rc != 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println(line);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
