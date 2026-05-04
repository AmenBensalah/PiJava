package edu.connexion3a77.controllers;

import edu.connexion3a77.entities.RawgGame;
import edu.connexion3a77.services.RawgApiService;
import edu.ProjetPI.controllers.DashboardSession;
import edu.ProjetPI.controllers.SceneManager;
import edu.connexion3a77.controllers.TournoiAdminController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RawgGamesController {

    @FXML
    private TextField tfGameSearch;
    @FXML
    private FlowPane gamesCardsPane;
    @FXML
    private Label gamesStatusLabel;
    @FXML
    private Button btnSearchGames;

    private final RawgApiService rawgApiService = new RawgApiService();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @FXML
    public void initialize() {
        searchGames();
    }

    @FXML
    private void onBackToAdmin() {
        executor.shutdownNow();
        SceneManager.switchScene("/fxml/tournoi-admin-view.fxml", "Gestion des tournois");
    }

    @FXML
    private void goToFrontOffice() {
        SceneManager.switchScene("/ajoutProduit.fxml", "E-SPORTIFY : Boutique");
    }

    @FXML
    private void goToNewsFeed() {
        SceneManager.switchScene("/FilActualiteView.fxml", "E-SPORTIFY : Fil d'actualite");
    }

    @FXML
    private void goToTournoisAdmin() {
        TournoiAdminController.openOn(TournoiAdminController.InitialSection.TOURNOIS);
        SceneManager.switchScene("/fxml/tournoi-admin-view.fxml", "Gestion des tournois");
    }

    @FXML
    private void goToParticipationsAdmin() {
        TournoiAdminController.openOn(TournoiAdminController.InitialSection.PARTICIPATIONS);
        SceneManager.switchScene("/fxml/tournoi-admin-view.fxml", "Gestion des participations");
    }

    @FXML
    private void goToGestionComptes() {
        SceneManager.switchScene("/edu/ProjetPI/views/admin-dashboard.fxml", "Gestion des comptes");
    }

    @FXML
    private void goToMailing() {
        SceneManager.switchScene("/backMailing.fxml", "Boutique Admin - Mailing");
    }

    @FXML
    private void goToCatalogue() {
        SceneManager.switchScene("/backListProduit.fxml", "Gestion des produits");
    }

    @FXML
    private void goToAdminCategorie() {
        SceneManager.switchScene("/backListCategorie.fxml", "Boutique Admin - Categories");
    }

    @FXML
    private void goToPayments() {
        edu.PROJETPI.AdminDashboardController.openOn(edu.PROJETPI.AdminDashboardController.InitialSection.PAIEMENTS);
        SceneManager.switchScene("/admin-dashboard-view.fxml", "Liste des paiements");
    }

    @FXML
    private void goToRevenuePrediction() {
        edu.PROJETPI.AdminDashboardController.openOn(edu.PROJETPI.AdminDashboardController.InitialSection.PREDICTION_CA);
        SceneManager.switchScene("/admin-dashboard-view.fxml", "Prediction chiffre d'affaires");
    }

    @FXML
    private void goToCommandes() {
        edu.PROJETPI.AdminDashboardController.openOn(edu.PROJETPI.AdminDashboardController.InitialSection.COMMANDES);
        SceneManager.switchScene("/admin-dashboard-view.fxml", "Liste des commandes");
    }

    @FXML
    private void handleViewProfile() {
        SceneManager.switchScene("/edu/ProjetPI/views/profile.fxml", "Mon Profil");
    }

    @FXML
    private void handleThemeToggle() {
        gamesStatusLabel.setText("Theme conserve (style admin actif).");
    }

    @FXML
    private void handleLogout() {
        executor.shutdownNow();
        DashboardSession.clear();
        SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "Login");
    }

    @FXML
    private void onSearchGames() {
        searchGames();
    }

    private void searchGames() {
        String query = tfGameSearch.getText() == null ? "" : tfGameSearch.getText().trim();

        Task<List<RawgGame>> task = new Task<>() {
            @Override
            protected List<RawgGame> call() {
                return rawgApiService.searchGames(query, 18);
            }
        };

        btnSearchGames.setDisable(true);
        gamesStatusLabel.setText("Chargement des jeux depuis RAWG...");

        task.setOnSucceeded(event -> {
            List<RawgGame> games = task.getValue();
            renderGames(games);
            gamesStatusLabel.setText(games.size() + " jeu(x) trouve(s).");
            btnSearchGames.setDisable(false);
        });

        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            String message = ex == null ? "Erreur inconnue." : ex.getMessage();
            gamesStatusLabel.setText("Erreur: " + message);
            gamesCardsPane.getChildren().clear();
            btnSearchGames.setDisable(false);
        });

        executor.submit(task);
    }

    private void renderGames(List<RawgGame> games) {
        Platform.runLater(() -> {
            gamesCardsPane.getChildren().clear();
            for (RawgGame game : games) {
                gamesCardsPane.getChildren().add(buildGameCard(game));
            }
        });
    }

    private VBox buildGameCard(RawgGame game) {
        VBox card = new VBox(8);
        card.getStyleClass().add("game-card");
        card.setPadding(new Insets(10));
        card.setPrefWidth(220);
        card.setMinHeight(320);

        if (game.getImageUrl() != null && !game.getImageUrl().isBlank()) {
            ImageView imageView = new ImageView(new Image(game.getImageUrl(), 220, 120, true, true, true));
            imageView.setFitWidth(200);
            imageView.setFitHeight(120);
            imageView.getStyleClass().add("game-image");
            card.getChildren().add(imageView);
        } else {
            Region placeholder = new Region();
            placeholder.getStyleClass().add("game-image-placeholder");
            placeholder.setPrefSize(200, 120);
            card.getChildren().add(placeholder);
        }

        Label name = new Label(game.getName());
        name.getStyleClass().add("game-title");
        name.setWrapText(true);

        Label rating = new Label("Rating: " + String.format(Locale.US, "%.1f", game.getRating()));
        rating.getStyleClass().add("game-meta");

        Label released = new Label("Sortie: " + game.getReleased());
        released.getStyleClass().add("game-meta");

        Button rawgLinkBtn = new Button("Voir sur RAWG");
        rawgLinkBtn.getStyleClass().add("game-link-btn");
        rawgLinkBtn.setOnAction(e -> openInBrowser(game.getRawgUrl()));

        HBox footer = new HBox(rawgLinkBtn);
        card.getChildren().addAll(name, rating, released, footer);
        return card;
    }

    private void openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                gamesStatusLabel.setText("Ouverture navigateur non supportee sur cette machine.");
            }
        } catch (Exception e) {
            gamesStatusLabel.setText("Impossible d'ouvrir le lien RAWG.");
        }
    }
}
