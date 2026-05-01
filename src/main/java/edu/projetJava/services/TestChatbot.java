package edu.projetJava.services;

public class TestChatbot {
    public static void main(String[] args) {
        String response = GeminiAIService.getResponse("Hello");
        System.out.println("Response: " + response);
    }
}
