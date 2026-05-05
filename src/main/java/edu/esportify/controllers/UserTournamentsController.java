package edu.esportify.controllers;

import edu.connexion3a77.entities.DemandeParticipation;
import edu.connexion3a77.entities.Tournoi;
import edu.connexion3a77.services.DemandeParticipationService;
import edu.connexion3a77.services.TournoiService;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class UserTournamentsController implements UserContentController {
    private final TournoiService tournoiService = new TournoiService();
    private final DemandeParticipationService demandeParticipationService = new DemandeParticipationService();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private final List<DemandeParticipation> participationCache = new ArrayList<>();

    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeFilterBox;
    @FXML private Label resultsLabel;
    @FXML private FlowPane tournamentsContainer;

    @FXML
    private void initialize() {
        typeFilterBox.getItems().setAll("Tous", "solo", "5v5", "team", "duo");
        typeFilterBox.setValue("Tous");
        nameField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        typeFilterBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    @Override
    public void init(UserLayoutController parentController) {
        loadParticipations();
        applyFilters();
    }

    @FXML
    private void onSearch() {
        loadParticipations();
        applyFilters();
    }

    @FXML
    private void onReset() {
        nameField.clear();
        typeFilterBox.setValue("Tous");
        loadParticipations();
        applyFilters();
    }

    private void applyFilters() {
        String keyword = normalize(nameField.getText());
        String typeFilter = normalize(typeFilterBox.getValue());
        List<Tournoi> tournois = loadTournaments();
        List<Tournoi> filtered = tournois.stream()
                .filter(tournoi -> normalize(tournoi.getNomTournoi()).contains(keyword)
                        || normalize(tournoi.getNomJeu()).contains(keyword))
                .filter(tournoi -> "tous".equals(typeFilter) || typeFilter.isBlank()
                        || normalize(tournoi.getTypeTournoi()).contains(typeFilter))
                .toList();
        renderTournaments(filtered);
    }

    private List<Tournoi> loadTournaments() {
        List<Tournoi> data = tournoiService.getData();
        return data.isEmpty() ? demoTournaments() : data;
    }

    private void loadParticipations() {
        participationCache.clear();
        participationCache.addAll(demandeParticipationService.afficher());
    }

    private void renderTournaments(List<Tournoi> tournois) {
        tournamentsContainer.getChildren().clear();
        resultsLabel.setText(tournois.size() + " tournoi(s) trouve(s)");

        if (tournois.isEmpty()) {
            Label emptyLabel = new Label("Aucun tournoi ne correspond aux filtres.");
            emptyLabel.getStyleClass().add("muted-label");
            tournamentsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Tournoi tournoi : tournois) {
            tournamentsContainer.getChildren().add(createTournamentCard(tournoi));
        }
    }

    private VBox createTournamentCard(Tournoi tournoi) {
        VBox card = new VBox(10);
        card.getStyleClass().add("coord-card");
        card.setPrefWidth(290);

        Label title = new Label(value(tournoi.getNomTournoi(), "Tournoi"));
        title.getStyleClass().add("section-title");
        title.setWrapText(true);

        Label type = new Label(value(tournoi.getTypeTournoi(), "N/A") + " | " + value(tournoi.getNomJeu(), "N/A"));
        type.getStyleClass().add("card-title");

        Label date = new Label("Du " + tournoi.getDateDebut() + " au " + tournoi.getDateFin());
        date.getStyleClass().add("muted-label");

        Label participants = new Label("Places: " + tournoi.getNombreParticipants());
        participants.getStyleClass().add("muted-label");

        Label prize = new Label("Prix: " + currencyFormat.format(tournoi.getCashPrize()));
        prize.getStyleClass().add("summary-value");

        Button detailsButton = new Button("Voir");
        detailsButton.getStyleClass().add("manager-outline-button");
        detailsButton.setOnAction(event -> showTournamentDetails(tournoi));

        boolean alreadyJoined = isAlreadyJoined(tournoi.getId());
        Button joinButton = new Button(!isOpen(tournoi)
                ? "Deja termine"
                : alreadyJoined ? "Deja inscrit" : "Rejoindre");
        joinButton.getStyleClass().add("classic-button");
        joinButton.setDisable(!isOpen(tournoi) || alreadyJoined);
        joinButton.setOnAction(event -> joinTournament(tournoi));

        HBox actions = new HBox(10, detailsButton, joinButton);
        HBox.setHgrow(detailsButton, Priority.ALWAYS);
        HBox.setHgrow(joinButton, Priority.ALWAYS);
        detailsButton.setMaxWidth(Double.MAX_VALUE);
        joinButton.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(title, type, date, participants, prize, actions);
        VBox.setMargin(actions, new Insets(6, 0, 0, 0));
        return card;
    }

    private void showTournamentDetails(Tournoi tournoi) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tournoi");
        alert.setHeaderText(value(tournoi.getNomTournoi(), "Tournoi"));
        alert.setContentText(
                "Jeu: " + value(tournoi.getNomJeu(), "N/A") + "\n"
                        + "Type: " + value(tournoi.getTypeTournoi(), "N/A") + "\n"
                        + "Dates: " + tournoi.getDateDebut() + " -> " + tournoi.getDateFin() + "\n"
                        + "Places: " + tournoi.getNombreParticipants() + "\n"
                        + "Prize: " + currencyFormat.format(tournoi.getCashPrize())
        );
        alert.showAndWait();
    }

    private void joinTournament(Tournoi tournoi) {
        if (!isOpen(tournoi)) {
            showInfo("Inscription", "Tournoi ferme", "Ce tournoi n'accepte plus de participations.");
            return;
        }
        if (isAlreadyJoined(tournoi.getId())) {
            showInfo("Inscription", "Deja inscrit", "Une demande existe deja pour " + value(tournoi.getNomTournoi(), "ce tournoi") + ".");
            return;
        }
        if (tournoi.getNombreParticipants() <= 0) {
            showInfo("Inscription", "Plus de places", "Aucune place n'est disponible pour ce tournoi.");
            return;
        }

        DemandeParticipation demande = new DemandeParticipation(
                tournoi.getId(),
                "Demande depuis l'espace utilisateur pour " + value(tournoi.getNomTournoi(), "ce tournoi"),
                "Amateur"
        );
        demandeParticipationService.ajouter(demande);
        loadParticipations();
        applyFilters();
        showInfo(
                "Inscription",
                "Participation envoyee",
                "La demande pour " + value(tournoi.getNomTournoi(), "ce tournoi") + " est maintenant visible cote admin."
        );
    }

    private boolean isAlreadyJoined(int tournoiId) {
        return participationCache.stream()
                .anyMatch(dp -> dp.getTournoiId() == tournoiId
                        && !DemandeParticipation.STATUT_REFUSEE.equalsIgnoreCase(dp.getStatut()));
    }

    private void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean isOpen(Tournoi tournoi) {
        if (tournoi.getDateFin() == null) {
            return true;
        }
        return !tournoi.getDateFin().toLocalDate().isBefore(LocalDate.now());
    }

    private List<Tournoi> demoTournaments() {
        List<Tournoi> tournois = new ArrayList<>();
        tournois.add(new Tournoi(1, "Solo Sports Test Cup", "solo", "EA Sports FC 25",
                java.sql.Date.valueOf(LocalDate.now().plusDays(3)),
                java.sql.Date.valueOf(LocalDate.now().plusDays(3)), 2, 1000));
        tournois.add(new Tournoi(2, "Solo Mind Test Cup", "solo", "Chess",
                java.sql.Date.valueOf(LocalDate.now().plusDays(5)),
                java.sql.Date.valueOf(LocalDate.now().plusDays(5)), 2, 1000));
        tournois.add(new Tournoi(3, "Solo FPS Test Cup", "solo", "Valorant",
                java.sql.Date.valueOf(LocalDate.now().plusDays(4)),
                java.sql.Date.valueOf(LocalDate.now().plusDays(4)), 2, 1000));
        tournois.add(new Tournoi(4, "Warzone Night Royale", "solo", "Warzone",
                java.sql.Date.valueOf(LocalDate.now().plusDays(2)),
                java.sql.Date.valueOf(LocalDate.now().plusDays(8)), 24, 100000));
        return tournois;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
