package edu.ProjetPI.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.ProjetPI.entities.User;
import edu.ProjetPI.services.UserService;
import edu.ProjetPI.tools.UserValidationRules;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProfileController {

    private final UserService userService = new UserService();

    @FXML
    private Label titleLabel;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField pseudoField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label roleLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private Label aiStatusLabel;

    @FXML
    private Label aiSummaryLabel;

    @FXML
    private Label aiTrendLabel;

    @FXML
    private Label aiConfidenceLabel;

    @FXML
    private Label aiBestGameLabel;

    @FXML
    private Label aiWinRateLabel;

    @FXML
    private Label aiMatchesLabel;

    @FXML
    private Label aiPredBestLabel;

    @FXML
    private Label aiTournamentStatsLabel;

    @FXML
    private Label aiTournamentPredLabel;

    @FXML
    private ComboBox<String> aiGameTypeCombo;

    @FXML
    private Label aiGameTitleLabel;

    @FXML
    private Label aiGamePlayedLabel;

    @FXML
    private Label aiGameWinRateLabel;

    @FXML
    private ProgressBar aiGameWinRateBar;

    @FXML
    private Label aiGamePredictionLabel;

    @FXML
    private ProgressBar aiGamePredBar;

    @FXML
    private Label aiGameConfidenceLabel;

    @FXML
    private Label aiGameExpectedLabel;

    @FXML
    private ListView<String> aiGameRecentList;

    private JsonObject currentPerGame;
    private JsonObject currentPredByGame;
    private JsonArray currentRecentMatches;

    @FXML
    public void initialize() {
        FormFeedback.clearMessage(messageLabel);
        FormFeedback.installResetOnInput(messageLabel, fullNameField, pseudoField, emailField, passwordField);
        aiGameTypeCombo.setOnAction(event -> updateSelectedGamePanel(aiGameTypeCombo.getValue()));

        User currentUser = DashboardSession.getCurrentUser();
        if (currentUser != null) {
            titleLabel.setText("Mon Profil - " + currentUser.getFullName());
            fullNameField.setText(currentUser.getFullName());
            pseudoField.setText(currentUser.getPseudo());
            emailField.setText(currentUser.getEmail());
            roleLabel.setText("Role: " + currentUser.getRole());
            loadAiStats(currentUser.getId());
        } else {
            setAiUnavailable("Aucun utilisateur connecte.");
        }
    }

    @FXML
    public void handleUpdateProfile() {
        User currentUser = DashboardSession.getCurrentUser();
        if (currentUser == null) {
            FormFeedback.showError(messageLabel, "No connected user found.");
            return;
        }

        FormFeedback.clearMessage(messageLabel);
        FormFeedback.clearInvalid(fullNameField);
        FormFeedback.clearInvalid(pseudoField);
        FormFeedback.clearInvalid(emailField);
        FormFeedback.clearInvalid(passwordField);

        try {
            UserValidationRules.validateFullName(fullNameField.getText());
        } catch (IllegalArgumentException e) {
            FormFeedback.markInvalid(fullNameField);
            FormFeedback.showError(messageLabel, e.getMessage());
            return;
        }

        try {
            UserValidationRules.validatePseudo(pseudoField.getText());
        } catch (IllegalArgumentException e) {
            FormFeedback.markInvalid(pseudoField);
            FormFeedback.showError(messageLabel, e.getMessage());
            return;
        }

        try {
            UserValidationRules.validateEmail(emailField.getText());
        } catch (IllegalArgumentException e) {
            FormFeedback.markInvalid(emailField);
            FormFeedback.showError(messageLabel, e.getMessage());
            return;
        }

        try {
            UserValidationRules.validatePasswordForUpdate(passwordField.getText());
        } catch (IllegalArgumentException e) {
            FormFeedback.markInvalid(passwordField);
            FormFeedback.showError(messageLabel, e.getMessage());
            return;
        }

        try {
            User updatedUser = new User(
                    currentUser.getId(),
                    fullNameField.getText(),
                    pseudoField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    currentUser.getRole()
            );
            userService.update(updatedUser);

            currentUser.setFullName(updatedUser.getFullName());
            currentUser.setPseudo(updatedUser.getPseudo());
            currentUser.setEmail(updatedUser.getEmail().trim().toLowerCase());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
                currentUser.setPassword(updatedUser.getPassword());
            }
            DashboardSession.setCurrentUser(currentUser);

            titleLabel.setText("Mon Profil - " + currentUser.getFullName());
            roleLabel.setText("Role: " + currentUser.getRole());
            passwordField.clear();
            FormFeedback.showSuccess(messageLabel, "Profile updated successfully.");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage() == null ? "" : e.getMessage();
            if (message.toLowerCase().contains("email")) {
                FormFeedback.markInvalid(emailField);
            }
            FormFeedback.showError(messageLabel, message);
        } catch (Exception e) {
            FormFeedback.showError(messageLabel, e.getMessage());
        }
    }

    @FXML
    public void handleDeleteAccount() {
        User currentUser = DashboardSession.getCurrentUser();
        if (currentUser == null) {
            FormFeedback.showError(messageLabel, "No connected user found.");
            return;
        }

        try {
            userService.delete(currentUser.getId());
            DashboardSession.clear();
            SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "Login");
        } catch (Exception e) {
            FormFeedback.showError(messageLabel, e.getMessage());
        }
    }

    @FXML
    public void handleBack() {
        User currentUser = DashboardSession.getCurrentUser();
        if (currentUser != null && "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            SceneManager.switchScene("/edu/ProjetPI/views/admin-dashboard.fxml", "Admin Dashboard");
        } else {
            SceneManager.switchScene("/edu/ProjetPI/views/user-dashboard.fxml", "Front Office");
        }
    }

    @FXML
    public void handleRefreshAi() {
        User currentUser = DashboardSession.getCurrentUser();
        if (currentUser == null) {
            setAiUnavailable("Aucun utilisateur connecte.");
            return;
        }
        loadAiStats(currentUser.getId());
        FormFeedback.showSuccess(messageLabel, "AI stats refreshed.");
    }

    private void loadAiStats(int userId) {
        Path reportPath = resolveReportsPath();
        if (reportPath == null) {
            setAiUnavailable("Fichier IA introuvable. Lancez le pipeline user_ai_agent.");
            return;
        }

        try (Reader reader = Files.newBufferedReader(reportPath)) {
            JsonElement rootEl = JsonParser.parseReader(reader);
            if (!rootEl.isJsonObject()) {
                setAiUnavailable("Format JSON IA invalide.");
                return;
            }

            JsonObject root = rootEl.getAsJsonObject();
            JsonObject users = asObject(root.get("users"));
            if (users == null) {
                setAiUnavailable("Section users absente dans le rapport IA.");
                return;
            }

            JsonObject userReport = asObject(users.get(String.valueOf(userId)));
            if (userReport == null) {
                setAiUnavailable("Pas de stats IA pour votre utilisateur.");
                return;
            }

            JsonObject summary = asObject(userReport.get("summary"));
            JsonObject mlPrediction = asObject(userReport.get("mlPrediction"));
            String trend = getString(userReport, "trend", "-");
            String confidence = getString(userReport, "confidence", "-");
            String aiInsight = getString(userReport, "aiInsight", "-");

            String matches = "-";
            String winRate = "-";
            String bestGame = "-";
            if (summary != null) {
                matches = String.valueOf(getInt(summary, "matchesPlayed", 0));
                winRate = String.format("%.1f%%", getDouble(summary, "winRate", 0.0));
                bestGame = getString(summary, "bestGameType", "-");
            }

            String predictedBest = "-";
            if (mlPrediction != null) {
                String predGame = getString(mlPrediction, "bestGameType", "-");
                double predProb = getDouble(mlPrediction, "bestWinProbability", 0.0);
                predictedBest = predGame + " (" + String.format("%.1f%%", predProb) + ")";
            }

            aiStatusLabel.setText("Statut: IA chargee");
            aiSummaryLabel.setText("Resume: " + aiInsight);
            aiTrendLabel.setText("Trend: " + trend);
            aiConfidenceLabel.setText("Confiance: " + confidence);
            aiBestGameLabel.setText("Meilleur mode: " + bestGame);
            aiWinRateLabel.setText("Win rate global: " + winRate);
            aiMatchesLabel.setText("Matchs joues: " + matches);
            aiPredBestLabel.setText("Prediction IA globale: " + predictedBest);

            currentPerGame = asObject(userReport.get("perGame"));
            currentPredByGame = mlPrediction == null ? null : asObject(mlPrediction.get("byGameType"));
            currentRecentMatches = userReport.getAsJsonArray("recentMatches");

            aiTournamentStatsLabel.setText("Win rate par type de jeu: " + formatGameTypeStats(currentPerGame));
            aiTournamentPredLabel.setText("Prediction win rate par type de jeu: " + formatGameTypePrediction(currentPredByGame));
            populateGameTypeSelector();
        } catch (Exception e) {
            setAiUnavailable("Erreur de lecture IA: " + e.getMessage());
        }
    }

    private void setAiUnavailable(String reason) {
        aiStatusLabel.setText("Statut: IA indisponible");
        aiSummaryLabel.setText("Resume: " + reason);
        aiTrendLabel.setText("Trend: -");
        aiConfidenceLabel.setText("Confiance: -");
        aiBestGameLabel.setText("Meilleur mode: -");
        aiWinRateLabel.setText("Win rate global: -");
        aiMatchesLabel.setText("Matchs joues: -");
        aiPredBestLabel.setText("Prediction IA globale: -");
        aiTournamentStatsLabel.setText("Win rate par type de jeu: -");
        aiTournamentPredLabel.setText("Prediction win rate par type de jeu: -");

        currentPerGame = null;
        currentPredByGame = null;
        currentRecentMatches = null;

        aiGameTypeCombo.setItems(FXCollections.observableArrayList("fps", "sports", "battle_royale", "mind", "other"));
        aiGameTypeCombo.setValue("fps");
        updateSelectedGamePanel("fps");
    }

    private Path resolveReportsPath() {
        List<Path> candidates = new ArrayList<>();
        String envPath = System.getenv("PROFILE_REPORTS_PATH");
        if (envPath != null && !envPath.isBlank()) {
            candidates.add(Path.of(envPath));
        }
        candidates.add(Path.of("data", "profile_reports_all.json"));
        candidates.add(Path.of("user_ai_agent", "data", "profile_reports_all.json"));
        candidates.add(Path.of("..", "user_ai_agent", "data", "profile_reports_all.json"));

        for (Path candidate : candidates) {
            Path normalized = candidate.toAbsolutePath().normalize();
            if (Files.exists(normalized) && Files.isRegularFile(normalized)) {
                return normalized;
            }
        }
        return null;
    }

    private static JsonObject asObject(JsonElement element) {
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static String getString(JsonObject obj, String key, String fallback) {
        if (obj == null) {
            return fallback;
        }
        JsonElement el = obj.get(key);
        return el != null && !el.isJsonNull() ? el.getAsString() : fallback;
    }

    private static int getInt(JsonObject obj, String key, int fallback) {
        if (obj == null) {
            return fallback;
        }
        JsonElement el = obj.get(key);
        return el != null && !el.isJsonNull() ? el.getAsInt() : fallback;
    }

    private static double getDouble(JsonObject obj, String key, double fallback) {
        if (obj == null) {
            return fallback;
        }
        JsonElement el = obj.get(key);
        return el != null && !el.isJsonNull() ? el.getAsDouble() : fallback;
    }

    private static String formatGameTypeStats(JsonObject stats) {
        String[] keys = {"fps", "sports", "battle_royale", "mind", "other"};
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            JsonObject row = stats == null ? null : asObject(stats.get(key));
            double winRate = row == null ? 0.0 : getDouble(row, "winRate", 0.0);
            int played = row == null ? 0 : getInt(row, "played", 0);
            sb.append(key).append(": ").append(String.format("%.1f%%", winRate)).append(" (").append(played).append(")");
        }
        return sb.toString();
    }

    private static String formatGameTypePrediction(JsonObject preds) {
        String[] keys = {"fps", "sports", "battle_royale", "mind", "other"};
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            JsonObject row = preds == null ? null : asObject(preds.get(key));
            double predicted = row == null ? 50.0 : getDouble(row, "winProbability", 50.0);
            sb.append(key).append(": ").append(String.format("%.1f%%", predicted));
        }
        return sb.toString();
    }

    private void populateGameTypeSelector() {
        String[] keys = {"fps", "sports", "battle_royale", "mind", "other"};
        aiGameTypeCombo.setItems(FXCollections.observableArrayList(keys));
        String defaultGame = "fps";
        if (currentPredByGame != null && currentPredByGame.has("fps")) {
            defaultGame = "fps";
        } else if (currentPredByGame != null && !currentPredByGame.entrySet().isEmpty()) {
            defaultGame = currentPredByGame.entrySet().iterator().next().getKey();
        }
        aiGameTypeCombo.setValue(defaultGame);
        updateSelectedGamePanel(defaultGame);
    }

    private void updateSelectedGamePanel(String gameKey) {
        if (gameKey == null || gameKey.isBlank()) {
            gameKey = "fps";
        }

        JsonObject stats = currentPerGame == null ? null : asObject(currentPerGame.get(gameKey));
        JsonObject pred = currentPredByGame == null ? null : asObject(currentPredByGame.get(gameKey));

        int played = stats == null ? 0 : getInt(stats, "played", 0);
        double realWinRate = stats == null ? 0.0 : getDouble(stats, "winRate", 0.0);
        double predictedWinRate = pred == null ? 50.0 : getDouble(pred, "winProbability", 50.0);
        String confidence = pred == null ? "low" : getString(pred, "confidence", "low");
        String expected = pred == null ? "L" : getString(pred, "expectedResult", "L");

        aiGameTitleLabel.setText("Analyse du mode: " + gameKey);
        aiGamePlayedLabel.setText("Matchs: " + played);
        aiGameWinRateLabel.setText("Win rate reel: " + String.format("%.1f%%", realWinRate));
        aiGameWinRateBar.setProgress(clamp01(realWinRate / 100.0));
        aiGamePredictionLabel.setText("Win rate predit: " + String.format("%.1f%%", predictedWinRate));
        aiGamePredBar.setProgress(clamp01(predictedWinRate / 100.0));
        aiGameConfidenceLabel.setText("Confiance IA: " + confidence);
        aiGameExpectedLabel.setText("Resultat attendu: " + expected);

        aiGameRecentList.setItems(FXCollections.observableArrayList(buildRecentLines(gameKey)));
    }

    private List<String> buildRecentLines(String gameKey) {
        List<String> lines = new ArrayList<>();
        if (currentRecentMatches == null) {
            lines.add("Aucun match recent.");
            return lines;
        }
        for (JsonElement el : currentRecentMatches) {
            JsonObject m = asObject(el);
            if (m == null) {
                continue;
            }
            String typeGame = getString(m, "typeGame", "other");
            if (!gameKey.equals(typeGame)) {
                continue;
            }
            String date = getString(m, "date", "-");
            String tournoi = getString(m, "tournoi", "-");
            String result = getString(m, "result", "-");
            String points = String.format("%.1f", getDouble(m, "points", 0.0));
            lines.add(date + " | " + tournoi + " | " + result + " | " + points + " pts");
        }
        if (lines.isEmpty()) {
            lines.add("Aucun match recent sur ce mode.");
        }
        return lines;
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
