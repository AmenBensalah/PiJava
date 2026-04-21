package edu.projetJava.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TestGemini {
    private static final String API_KEY = "AIzaSyA3EWqR210VYy84Z_Y30So0PD4yK1dko-Q";
    public static void main(String[] args) {
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models?key=" + API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int rc = conn.getResponseCode();
            System.out.println("Code: " + rc);
            BufferedReader in = new BufferedReader(new InputStreamReader(rc == 200 ? conn.getInputStream() : conn.getErrorStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
