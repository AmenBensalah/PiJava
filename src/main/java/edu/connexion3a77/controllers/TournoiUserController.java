package edu.connexion3a77.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import edu.connexion3a77.entities.DemandeParticipation;
import edu.connexion3a77.entities.Tournoi;
import edu.connexion3a77.services.DemandeParticipationService;
import edu.connexion3a77.services.TournoiService;
import edu.connexion3a77.ui.SceneNavigator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TournoiUserController {

    @FXML
    private TextField tfSearch;
    @FXML
    private ComboBox<String> cbFilterTypeTournoi;
    @FXML
    private FlowPane tournoiCardsPane;
    @FXML
    private Label userStatusLabel;
    @FXML
    private StackPane calendarContainer;

    @FXML
    private TextField tfPartTournoiId;
    @FXML
    private TextField tfPartDescription;
    @FXML
    private ComboBox<String> cbPartNiveau;
    @FXML
    private TableView<DemandeParticipation> participationTable;
    @FXML
    private TableColumn<DemandeParticipation, Integer> colPartId;
    @FXML
    private TableColumn<DemandeParticipation, Integer> colPartTournoiId;
    @FXML
    private TableColumn<DemandeParticipation, String> colPartTournoiNom;
    @FXML
    private TableColumn<DemandeParticipation, String> colPartDescription;
    @FXML
    private TableColumn<DemandeParticipation, String> colPartNiveau;
    @FXML
    private TableColumn<DemandeParticipation, DemandeParticipation> colPartActions;

    private final TournoiService tournoiService = new TournoiService();
    private final DemandeParticipationService demandeParticipationService = new DemandeParticipationService();
    private final ObservableList<Tournoi> tournoiList = FXCollections.observableArrayList();
    private final ObservableList<DemandeParticipation> participationList = FXCollections.observableArrayList();
    private final Map<Integer, Tournoi> tournoiById = new HashMap<>();
    private CalendarView calendarView;
    private Calendar userTournamentsCalendar;

    private DemandeParticipation selectedParticipation;

    @FXML
    public void initialize() {
        cbFilterTypeTournoi.setItems(FXCollections.observableArrayList("Tous", "SOLO", "DUO", "SQUAD", "LIGUE"));
        cbFilterTypeTournoi.setValue("Tous");
        cbPartNiveau.setItems(FXCollections.observableArrayList("Amateur", "Medium", "Pro"));
        cbPartNiveau.setValue("Amateur");

        initCalendarView();
        configureParticipationTable();
        loadTournois();
        loadParticipations();
        renderTournoiCards();
        refreshCalendarFromParticipations();
    }

    @FXML
    private void onBackToAdmin() {
        SceneNavigator.showAdminView();
    }

    @FXML
    private void onRefreshUserData() {
        loadTournois();
        loadParticipations();
        renderTournoiCards();
        refreshCalendarFromParticipations();
        userStatusLabel.setText("Tournois et participations rafraichis.");
    }

    @FXML
    private void onSearch() {
        renderTournoiCards();
    }

    @FXML
    private void onResetFilters() {
        tfSearch.clear();
        cbFilterTypeTournoi.setValue("Tous");
        renderTournoiCards();
        userStatusLabel.setText("Filtres reinitialises.");
    }

    @FXML
    private void onSaveParticipation() {
        if (!validateParticipationForm()) {
            return;
        }

        int tournoiId = Integer.parseInt(tfPartTournoiId.getText().trim());
        String description = tfPartDescription.getText().trim();
        String niveau = cbPartNiveau.getValue();

        if (selectedParticipation == null) {
            DemandeParticipation nouvelleDemande = new DemandeParticipation(tournoiId, description, niveau);
            demandeParticipationService.ajouter(nouvelleDemande);
            userStatusLabel.setText("Participation envoyee.");
        } else {
            DemandeParticipation demandeMaj = new DemandeParticipation(tournoiId, description, niveau);
            demandeParticipationService.updateEntity(selectedParticipation.getId(), demandeMaj);
            userStatusLabel.setText("Participation modifiee.");
        }

        selectedParticipation = null;
        clearParticipationForm();
        loadParticipations();
        renderTournoiCards();
        refreshCalendarFromParticipations();
    }

    @FXML
    private void onCancelParticipationEdit() {
        selectedParticipation = null;
        clearParticipationForm();
        userStatusLabel.setText("Edition participation annulee.");
    }

    private void configureParticipationTable() {
        colPartId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        colPartTournoiId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTournoiId()));
        colPartTournoiNom.setCellValueFactory(data -> new ReadOnlyStringWrapper(getTournoiNom(data.getValue().getTournoiId())));
        colPartDescription.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDescription()));
        colPartNiveau.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNiveau()));

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
                    selectedParticipation = demande;
                    tfPartTournoiId.setText(String.valueOf(demande.getTournoiId()));
                    tfPartDescription.setText(demande.getDescription());
                    cbPartNiveau.setValue(demande.getNiveau());
                    userStatusLabel.setText("Edition participation ID " + demande.getId() + ".");
                });

                deleteButton.setOnAction(event -> {
                    DemandeParticipation demande = getTableView().getItems().get(getIndex());
                    deleteParticipation(demande);
                });
            }

            @Override
            protected void updateItem(DemandeParticipation demandeParticipation, boolean empty) {
                super.updateItem(demandeParticipation, empty);
                setGraphic(empty ? null : actionBar);
            }
        });

        participationTable.setItems(participationList);
    }

    private void loadTournois() {
        tournoiList.setAll(tournoiService.afficher());
        tournoiById.clear();
        for (Tournoi tournoi : tournoiList) {
            tournoiById.put(tournoi.getId(), tournoi);
        }
    }

    private void loadParticipations() {
        participationList.setAll(demandeParticipationService.afficher());
    }

    private void renderTournoiCards() {
        tournoiCardsPane.getChildren().clear();
        String keyword = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
        String filterType = cbFilterTypeTournoi.getValue() == null ? "Tous" : cbFilterTypeTournoi.getValue();

        for (Tournoi tournoi : tournoiList.stream()
                .filter(t -> keyword.isEmpty() || t.getNomTournoi().toLowerCase().contains(keyword) || t.getNomJeu().toLowerCase().contains(keyword))
                .filter(t -> "Tous".equalsIgnoreCase(filterType) || t.getTypeTournoi().equalsIgnoreCase(filterType))
                .collect(Collectors.toList())) {
            tournoiCardsPane.getChildren().add(createTournoiCard(tournoi));
        }
    }

    private VBox createTournoiCard(Tournoi tournoi) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14));
        card.getStyleClass().add("tournoi-card");
        card.setPrefWidth(240);
        card.setMinHeight(225);

        Label nom = new Label(tournoi.getNomTournoi());
        nom.getStyleClass().add("card-title");

        Label tags = new Label(tournoi.getTypeTournoi() + " | " + inferTypeJeu(tournoi.getNomJeu()));
        tags.getStyleClass().add("card-tags");

        Label jeu = new Label("Jeu: " + tournoi.getNomJeu());
        jeu.getStyleClass().add("card-meta");

        Label date = new Label("Du " + tournoi.getDateDebut() + " au " + tournoi.getDateFin());
        date.getStyleClass().add("card-meta");

        Label places = new Label("Places: " + tournoi.getNombreParticipants());
        places.getStyleClass().add("card-meta");

        Label prix = new Label("Prix: $" + String.format("%.2f", tournoi.getCashPrize()));
        prix.getStyleClass().add("card-prize");

        HBox buttonsRow = new HBox(8);
        Button voirButton = new Button("Voir");
        Button joinButton = new Button(isAlreadyJoined(tournoi.getId()) ? "Deja inscrit" : "Rejoindre");
        voirButton.getStyleClass().add("card-secondary-btn");
        joinButton.getStyleClass().add("card-primary-btn");
        joinButton.setDisable(isAlreadyJoined(tournoi.getId()));

        voirButton.setOnAction(event -> userStatusLabel.setText("Tournoi selectionne: " + tournoi.getNomTournoi()));
        joinButton.setOnAction(event -> joinTournoi(tournoi));

        buttonsRow.getChildren().addAll(voirButton, joinButton);
        card.getChildren().addAll(nom, tags, jeu, date, places, prix, buttonsRow);
        return card;
    }

    private void joinTournoi(Tournoi tournoi) {
        if (isAlreadyJoined(tournoi.getId())) {
            userStatusLabel.setText("Tu es deja inscrit a ce tournoi.");
            return;
        }
        String description = tfPartDescription.getText();
        if (description == null || description.trim().isEmpty()) {
            description = "Demande pour le tournoi " + tournoi.getNomTournoi();
            tfPartDescription.setText(description);
        }
        String niveau = cbPartNiveau.getValue();
        if (niveau == null || niveau.trim().isEmpty()) {
            niveau = "Amateur";
            cbPartNiveau.setValue(niveau);
        }

        DemandeParticipation demande = new DemandeParticipation(
                tournoi.getId(),
                description,
                niveau
        );
        demandeParticipationService.ajouter(demande);
        loadParticipations();
        renderTournoiCards();
        refreshCalendarFromParticipations();
        tfPartTournoiId.setText(String.valueOf(tournoi.getId()));
        userStatusLabel.setText("Demande de participation envoyee pour " + tournoi.getNomTournoi() + ".");
    }

    private void deleteParticipation(DemandeParticipation demande) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression participation");
        alert.setHeaderText("Supprimer la participation ID " + demande.getId() + " ?");
        alert.setContentText("Cette action est irreversible.");
        Optional<ButtonType> choice = alert.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            demandeParticipationService.supprimer(demande.getId());
            loadParticipations();
            renderTournoiCards();
            refreshCalendarFromParticipations();
            userStatusLabel.setText("Participation supprimee.");
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

        for (DemandeParticipation participation : participationList) {
            if (!DemandeParticipation.STATUT_ACCEPTEE.equalsIgnoreCase(participation.getStatut())) {
                continue;
            }
            Tournoi tournoi = tournoiById.get(participation.getTournoiId());
            if (tournoi == null || tournoi.getDateDebut() == null || tournoi.getDateFin() == null) {
                continue;
            }

            LocalDate startDate = tournoi.getDateDebut().toLocalDate();
            LocalDate endDate = tournoi.getDateFin().toLocalDate();

            Entry<String> entry = new Entry<>(tournoi.getNomTournoi());
            entry.changeStartDate(startDate);
            entry.changeEndDate(endDate);
            entry.setFullDay(true);
            entry.setLocation(tournoi.getNomJeu());
            entry.setUserObject("Inscription ID " + participation.getId());
            userTournamentsCalendar.addEntry(entry);
        }
    }

    private boolean validateParticipationForm() {
        if (tfPartTournoiId.getText() == null || tfPartTournoiId.getText().trim().isEmpty()) {
            userStatusLabel.setText("Selectionne un tournoi avec Rejoindre.");
            return false;
        }
        try {
            int tournoiId = Integer.parseInt(tfPartTournoiId.getText().trim());
            if (!tournoiById.containsKey(tournoiId)) {
                userStatusLabel.setText("Tournoi introuvable.");
                return false;
            }
        } catch (NumberFormatException ex) {
            userStatusLabel.setText("ID tournoi invalide.");
            return false;
        }
        if (tfPartDescription.getText() == null || tfPartDescription.getText().trim().isEmpty()) {
            userStatusLabel.setText("Description obligatoire.");
            return false;
        }
        if (cbPartNiveau.getValue() == null || cbPartNiveau.getValue().trim().isEmpty()) {
            userStatusLabel.setText("Niveau obligatoire.");
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
        return participationList.stream()
                .anyMatch(dp -> dp.getTournoiId() == tournoiId
                        && !DemandeParticipation.STATUT_REFUSEE.equalsIgnoreCase(dp.getStatut()));
    }

    private String getTournoiNom(int tournoiId) {
        Tournoi tournoi = tournoiById.get(tournoiId);
        return tournoi == null ? "Inconnu" : tournoi.getNomTournoi();
    }

    private String inferTypeJeu(String nomJeu) {
        if (nomJeu == null || nomJeu.trim().isEmpty()) {
            return "MIND";
        }
        String normalized = nomJeu.toLowerCase();
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
}
