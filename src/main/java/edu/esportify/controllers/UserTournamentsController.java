package edu.esportify.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import edu.ProjetPI.controllers.DashboardSession;
import edu.connexion3a77.entities.DemandeParticipation;
import edu.connexion3a77.entities.Tournoi;
import edu.connexion3a77.services.DemandeParticipationService;
import edu.connexion3a77.services.TournoiService;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class UserTournamentsController implements UserContentController {
    private final TournoiService tournoiService = new TournoiService();
    private final DemandeParticipationService demandeParticipationService = new DemandeParticipationService();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    private final List<DemandeParticipation> participationCache = new ArrayList<>();
    private final Map<Integer, Tournoi> tournoiById = new HashMap<>();

    private CalendarView calendarView;
    private Calendar userTournamentsCalendar;
    private DemandeParticipation selectedParticipation;

    @FXML private TextField nameField;
    @FXML private ComboBox<String> typeFilterBox;
    @FXML private Label resultsLabel;
    @FXML private FlowPane tournamentsContainer;
    @FXML private StackPane calendarContainer;
    @FXML private TextField tfPartTournoiId;
    @FXML private TextField tfPartDescription;
    @FXML private ComboBox<String> cbPartNiveau;
    @FXML private TableView<DemandeParticipation> participationTable;
    @FXML private TableColumn<DemandeParticipation, Integer> colPartId;
    @FXML private TableColumn<DemandeParticipation, Integer> colPartTournoiId;
    @FXML private TableColumn<DemandeParticipation, String> colPartTournoiNom;
    @FXML private TableColumn<DemandeParticipation, String> colPartDescription;
    @FXML private TableColumn<DemandeParticipation, String> colPartNiveau;
    @FXML private TableColumn<DemandeParticipation, String> colPartStatut;
    @FXML private TableColumn<DemandeParticipation, DemandeParticipation> colPartActions;
    @FXML private Label userStatusLabel;

    @FXML
    private void initialize() {
        typeFilterBox.getItems().setAll("Tous", "SOLO", "DUO", "SQUAD", "LIGUE", "5V5", "TEAM");
        typeFilterBox.setValue("Tous");
        cbPartNiveau.getItems().setAll("Amateur", "Medium", "Pro");
        cbPartNiveau.setValue("Amateur");
        nameField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        typeFilterBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        initCalendarView();
        configureParticipationTable();
    }

    @Override
    public void init(UserLayoutController parentController) {
        loadParticipations();
        applyFilters();
        refreshCalendarFromParticipations();
        setStatus("Choisis un tournoi puis clique sur Rejoindre pour preparer ta demande.");
    }

    @FXML
    private void onSearch() {
        loadParticipations();
        applyFilters();
        refreshCalendarFromParticipations();
        setStatus("Tournois rafraichis.");
    }

    @FXML
    private void onReset() {
        nameField.clear();
        typeFilterBox.setValue("Tous");
        clearParticipationForm();
        selectedParticipation = null;
        loadParticipations();
        applyFilters();
        refreshCalendarFromParticipations();
        setStatus("Filtres reinitialises.");
    }

    @FXML
    private void onSaveParticipation() {
        if (!validateParticipationForm()) {
            return;
        }

        int tournoiId = Integer.parseInt(tfPartTournoiId.getText().trim());
        String description = tfPartDescription.getText().trim();
        String niveau = cbPartNiveau.getValue();
        Integer currentUserId = resolveCurrentUserId();
        if (currentUserId == null) {
            setStatus("Utilisateur non connecte. Reconnecte-toi puis reessaie.");
            return;
        }

        if (selectedParticipation == null) {
            DemandeParticipation demande = new DemandeParticipation(tournoiId, description, niveau);
            demande.setUserId(currentUserId);
            demandeParticipationService.ajouterPourUtilisateur(demande, currentUserId);
            setStatus("Demande envoyee pour le tournoi cible.");
        } else {
            DemandeParticipation demande = new DemandeParticipation(tournoiId, description, niveau);
            demande.setUserId(currentUserId);
            demandeParticipationService.updateEntity(selectedParticipation.getId(), demande);
            setStatus("Participation modifiee.");
        }

        selectedParticipation = null;
        clearParticipationForm();
        loadParticipations();
        applyFilters();
        refreshCalendarFromParticipations();
    }

    @FXML
    private void onCancelParticipationEdit() {
        selectedParticipation = null;
        clearParticipationForm();
        setStatus("Edition annulee.");
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
        List<Tournoi> source = data.isEmpty() ? demoTournaments() : data;
        tournoiById.clear();
        for (Tournoi tournoi : source) {
            tournoiById.put(tournoi.getId(), tournoi);
        }
        return source;
    }

    private void loadParticipations() {
        participationCache.clear();
        Integer currentUserId = resolveCurrentUserId();
        if (currentUserId != null) {
            participationCache.addAll(demandeParticipationService.afficherPourUtilisateur(currentUserId));
        }
        if (participationTable != null) {
            participationTable.getItems().setAll(participationCache);
        }
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
        card.getStyleClass().addAll("coord-card", "tournoi-card");
        card.setPadding(new Insets(14));
        card.setPrefWidth(290);

        Label title = new Label(value(tournoi.getNomTournoi(), "Tournoi"));
        title.getStyleClass().add("section-title");
        title.setWrapText(true);

        Label type = new Label(value(tournoi.getTypeTournoi(), "N/A") + " | " + inferTypeJeu(tournoi.getNomJeu()));
        type.getStyleClass().add("card-title");

        Label game = new Label("Jeu: " + value(tournoi.getNomJeu(), "N/A"));
        game.getStyleClass().add("muted-label");

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
        joinButton.setOnAction(event -> prepareParticipationForTournoi(tournoi));

        HBox actions = new HBox(10, detailsButton, joinButton);
        HBox.setHgrow(detailsButton, Priority.ALWAYS);
        HBox.setHgrow(joinButton, Priority.ALWAYS);
        detailsButton.setMaxWidth(Double.MAX_VALUE);
        joinButton.setMaxWidth(Double.MAX_VALUE);

        card.getChildren().addAll(title, type, game, date, participants, prize, actions);
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

    private void prepareParticipationForTournoi(Tournoi tournoi) {
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

        selectedParticipation = null;
        tfPartTournoiId.setText(String.valueOf(tournoi.getId()));
        if (tfPartDescription.getText() == null || tfPartDescription.getText().isBlank()) {
            tfPartDescription.setText("Demande pour le tournoi " + value(tournoi.getNomTournoi(), "ce tournoi"));
        }
        if (cbPartNiveau.getValue() == null || cbPartNiveau.getValue().isBlank()) {
            cbPartNiveau.setValue("Amateur");
        }
        tfPartDescription.requestFocus();
        tfPartDescription.positionCaret(tfPartDescription.getText().length());
        setStatus("Tournoi cible selectionne: " + value(tournoi.getNomTournoi(), "Tournoi") + ". Remplis puis clique Enregistrer.");
    }

    private void configureParticipationTable() {
        colPartId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        colPartTournoiId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTournoiId()));
        colPartTournoiNom.setCellValueFactory(data -> new ReadOnlyStringWrapper(getTournoiNom(data.getValue().getTournoiId())));
        colPartDescription.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDescription()));
        colPartNiveau.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNiveau()));
        colPartStatut.setCellValueFactory(data -> new ReadOnlyStringWrapper(formatStatus(data.getValue().getStatut())));

        colPartActions.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        colPartActions.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final ToolBar actionBar = new ToolBar(editButton, deleteButton);

            {
                actionBar.getStyleClass().add("action-toolbar");
                editButton.getStyleClass().add("action-edit-btn");
                deleteButton.getStyleClass().add("action-delete-btn");

                editButton.setOnAction(event -> {
                    DemandeParticipation demande = getTableView().getItems().get(getIndex());
                    if (DemandeParticipation.STATUT_ACCEPTEE.equalsIgnoreCase(demande.getStatut())) {
                        setStatus("Une participation acceptee ne peut plus etre modifiee.");
                        return;
                    }
                    selectedParticipation = demande;
                    tfPartTournoiId.setText(String.valueOf(demande.getTournoiId()));
                    tfPartDescription.setText(demande.getDescription());
                    cbPartNiveau.setValue(demande.getNiveau());
                    setStatus("Edition participation ID " + demande.getId() + ".");
                });

                deleteButton.setOnAction(event -> {
                    DemandeParticipation demande = getTableView().getItems().get(getIndex());
                    deleteParticipation(demande);
                });
            }

            @Override
            protected void updateItem(DemandeParticipation item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actionBar);
            }
        });

        participationTable.getItems().setAll(participationCache);
    }

    private void deleteParticipation(DemandeParticipation demande) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression participation");
        alert.setHeaderText("Supprimer la participation ID " + demande.getId() + " ?");
        alert.setContentText("Cette action est irreversible.");
        Optional<javafx.scene.control.ButtonType> choice = alert.showAndWait();
        if (choice.isPresent() && choice.get() == javafx.scene.control.ButtonType.OK) {
            demandeParticipationService.supprimer(demande.getId());
            selectedParticipation = null;
            clearParticipationForm();
            loadParticipations();
            applyFilters();
            refreshCalendarFromParticipations();
            setStatus("Participation supprimee.");
        }
    }

    private void initCalendarView() {
        userTournamentsCalendar = new Calendar("Mes tournois");
        userTournamentsCalendar.setStyle(Calendar.Style.STYLE3);

        CalendarSource source = new CalendarSource("Tournois");
        source.getCalendars().add(userTournamentsCalendar);

        calendarView = new CalendarView();
        calendarView.getCalendarSources().setAll(source);
        calendarView.showMonthPage();
        calendarView.setShowAddCalendarButton(false);
        calendarView.setShowSourceTray(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowPageSwitcher(true);
        calendarView.setRequestedTime(LocalDate.now().atTime(9, 0).toLocalTime());

        calendarContainer.getChildren().setAll(calendarView);
    }

    private void refreshCalendarFromParticipations() {
        if (userTournamentsCalendar == null) {
            return;
        }

        userTournamentsCalendar.clear();
        for (DemandeParticipation participation : participationCache) {
            if (!DemandeParticipation.STATUT_ACCEPTEE.equalsIgnoreCase(participation.getStatut())) {
                continue;
            }
            Tournoi tournoi = tournoiById.get(participation.getTournoiId());
            if (tournoi == null || tournoi.getDateDebut() == null || tournoi.getDateFin() == null) {
                continue;
            }

            Entry<String> entry = new Entry<>(value(tournoi.getNomTournoi(), "Tournoi"));
            entry.changeStartDate(tournoi.getDateDebut().toLocalDate());
            entry.changeEndDate(tournoi.getDateFin().toLocalDate());
            entry.setFullDay(true);
            entry.setLocation(value(tournoi.getNomJeu(), "Jeu"));
            entry.setUserObject("Inscription ID " + participation.getId());
            userTournamentsCalendar.addEntry(entry);
        }
    }

    private boolean validateParticipationForm() {
        if (tfPartTournoiId.getText() == null || tfPartTournoiId.getText().trim().isEmpty()) {
            setStatus("Selectionne un tournoi avec Rejoindre.");
            return false;
        }
        try {
            int tournoiId = Integer.parseInt(tfPartTournoiId.getText().trim());
            if (!tournoiById.containsKey(tournoiId)) {
                setStatus("Tournoi introuvable.");
                return false;
            }
            if (isAlreadyJoined(tournoiId) && selectedParticipation == null) {
                setStatus("Une demande existe deja pour ce tournoi.");
                return false;
            }
        } catch (NumberFormatException ex) {
            setStatus("ID tournoi invalide.");
            return false;
        }
        if (tfPartDescription.getText() == null || tfPartDescription.getText().trim().isEmpty()) {
            setStatus("Description obligatoire.");
            return false;
        }
        if (cbPartNiveau.getValue() == null || cbPartNiveau.getValue().trim().isEmpty()) {
            setStatus("Niveau obligatoire.");
            return false;
        }
        return true;
    }

    private void clearParticipationForm() {
        tfPartTournoiId.clear();
        tfPartDescription.clear();
        cbPartNiveau.setValue("Amateur");
    }

    private boolean isAlreadyJoined(int tournoiId) {
        return participationCache.stream()
                .anyMatch(dp -> dp.getTournoiId() == tournoiId
                        && !DemandeParticipation.STATUT_REFUSEE.equalsIgnoreCase(dp.getStatut()));
    }

    private boolean isOpen(Tournoi tournoi) {
        if (tournoi.getDateFin() == null) {
            return true;
        }
        return !tournoi.getDateFin().toLocalDate().isBefore(LocalDate.now());
    }

    private String getTournoiNom(int tournoiId) {
        Tournoi tournoi = tournoiById.get(tournoiId);
        return tournoi == null ? "Inconnu" : value(tournoi.getNomTournoi(), "Tournoi");
    }

    private String formatStatus(String status) {
        if (status == null || status.isBlank()) {
            return "En attente";
        }
        return switch (status.toUpperCase(Locale.ROOT)) {
            case DemandeParticipation.STATUT_ACCEPTEE -> "Acceptee";
            case DemandeParticipation.STATUT_REFUSEE -> "Refusee";
            default -> "En attente";
        };
    }

    private String inferTypeJeu(String nomJeu) {
        if (nomJeu == null || nomJeu.trim().isEmpty()) {
            return "MIND";
        }
        String normalized = nomJeu.toLowerCase(Locale.ROOT);
        if (normalized.contains("fifa") || normalized.contains("nba") || normalized.contains("pes")) {
            return "SPORTS";
        }
        if (normalized.contains("valorant") || normalized.contains("cs") || normalized.contains("call of duty")) {
            return "FPS";
        }
        if (normalized.contains("fortnite") || normalized.contains("pubg") || normalized.contains("apex")) {
            return "BATTLE ROYALE";
        }
        return "MIND";
    }

    private Integer resolveCurrentUserId() {
        var currentUser = DashboardSession.getCurrentUser();
        return currentUser == null || currentUser.getId() <= 0 ? null : currentUser.getId();
    }

    private void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setStatus(String message) {
        if (userStatusLabel != null) {
            userStatusLabel.setText(message == null || message.isBlank() ? "Pret." : message);
        }
    }

    private List<Tournoi> demoTournaments() {
        List<Tournoi> tournois = new ArrayList<>();
        tournois.add(new Tournoi(1, "Solo Sports Test Cup", "SOLO", "EA Sports FC 25",
                java.sql.Date.valueOf(LocalDate.now().plusDays(3)),
                java.sql.Date.valueOf(LocalDate.now().plusDays(3)), 2, 1000));
        tournois.add(new Tournoi(2, "Solo Mind Test Cup", "SOLO", "Chess",
                java.sql.Date.valueOf(LocalDate.now().plusDays(5)),
                java.sql.Date.valueOf(LocalDate.now().plusDays(5)), 2, 1000));
        tournois.add(new Tournoi(3, "Solo FPS Test Cup", "SOLO", "Valorant",
                java.sql.Date.valueOf(LocalDate.now().plusDays(4)),
                java.sql.Date.valueOf(LocalDate.now().plusDays(4)), 2, 1000));
        tournois.add(new Tournoi(4, "Warzone Night Royale", "SOLO", "Warzone",
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
