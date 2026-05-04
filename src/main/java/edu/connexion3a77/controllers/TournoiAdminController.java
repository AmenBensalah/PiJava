package edu.connexion3a77.controllers;

import edu.connexion3a77.entities.DemandeParticipation;
import edu.connexion3a77.entities.Tournoi;
import edu.connexion3a77.services.DemandeParticipationService;
import edu.connexion3a77.services.TournoiService;
import edu.connexion3a77.ui.SceneNavigator;
import edu.ProjetPI.controllers.DashboardSession;
import edu.ProjetPI.controllers.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class TournoiAdminController {
    private static InitialSection initialSection = InitialSection.TOURNOIS;

    public enum InitialSection {
        TOURNOIS,
        PARTICIPATIONS
    }

    public static void openOn(InitialSection section) {
        initialSection = section == null ? InitialSection.TOURNOIS : section;
    }

    @FXML
    private VBox formCard;
    @FXML
    private Label formTitle;
    @FXML
    private Button btnCreateTournoi;

    @FXML
    private TextField tfNomTournoi;
    @FXML
    private ComboBox<String> cbTypeTournoi;
    @FXML
    private ComboBox<String> cbTypeJeu;
    @FXML
    private TextField tfNomJeu;
    @FXML
    private DatePicker dpDateDebut;
    @FXML
    private DatePicker dpDateFin;
    @FXML
    private TextField tfCashPrize;
    @FXML
    private TextField tfNombreParticipants;
    @FXML
    private Label statusLabel;
    @FXML
    private Label lblTotalTournois;
    @FXML
    private Label lblTypesTournoiActifs;
    @FXML
    private Label lblTypesJeuActifs;
    @FXML
    private Label lblPredictionType;
    @FXML
    private Label lblPredictionJeu;
    @FXML
    private Label lblPredictionConfidence;
    @FXML
    private Label predictionStatusLabel;
    @FXML
    private BarChart<String, Number> barTypeTournoiChart;
    @FXML
    private CategoryAxis xTypeTournoiAxis;
    @FXML
    private NumberAxis yTypeTournoiAxis;
    @FXML
    private BarChart<String, Number> barTypeJeuChart;
    @FXML
    private CategoryAxis xTypeJeuAxis;
    @FXML
    private NumberAxis yTypeJeuAxis;

    @FXML
    private TableView<Tournoi> tournoiTable;
    @FXML
    private TableColumn<Tournoi, Integer> colId;
    @FXML
    private TableColumn<Tournoi, String> colNom;
    @FXML
    private TableColumn<Tournoi, String> colTypeTournoi;
    @FXML
    private TableColumn<Tournoi, String> colTypeJeu;
    @FXML
    private TableColumn<Tournoi, String> colJeu;
    @FXML
    private TableColumn<Tournoi, String> colDateDebut;
    @FXML
    private TableColumn<Tournoi, String> colDateFin;
    @FXML
    private TableColumn<Tournoi, Integer> colParticipants;
    @FXML
    private TableColumn<Tournoi, String> colCashPrize;
    @FXML
    private TableColumn<Tournoi, Tournoi> colActions;

    @FXML
    private VBox tournamentPane;
    @FXML
    private VBox participationPane;
    @FXML
    private ScrollPane tournamentScroll;
    @FXML
    private ScrollPane participationScroll;
    @FXML
    private TableView<DemandeParticipation> participationAdminTable;
    @FXML
    private TableColumn<DemandeParticipation, Integer> colPartAdminId;
    @FXML
    private TableColumn<DemandeParticipation, Integer> colPartAdminTournoiId;
    @FXML
    private TableColumn<DemandeParticipation, String> colPartAdminTournoiNom;
    @FXML
    private TableColumn<DemandeParticipation, String> colPartAdminDescription;
    @FXML
    private TableColumn<DemandeParticipation, String> colPartAdminNiveau;
    @FXML
    private TableColumn<DemandeParticipation, DemandeParticipation> colPartAdminActions;
    @FXML
    private Label participationAdminStatusLabel;

    private final TournoiService tournoiService = new TournoiService();
    private final DemandeParticipationService demandeParticipationService = new DemandeParticipationService();
    private final ObservableList<Tournoi> tournoiList = FXCollections.observableArrayList();
    private final ObservableList<DemandeParticipation> participationRequestList = FXCollections.observableArrayList();
    private final Map<Integer, Tournoi> tournoiById = new HashMap<>();
    private Tournoi selectedTournoi;

    @FXML
    public void initialize() {
        cbTypeTournoi.setItems(FXCollections.observableArrayList("SOLO", "DUO", "SQUAD", "LIGUE"));
        cbTypeJeu.setItems(FXCollections.observableArrayList("FPS", "SPORTS", "MIND", "BATTLE ROYALE"));

        configureColumns();
        configureDashboardCharts();
        configureParticipationAdminTable();
        loadTournois();
        loadParticipationsRequests();

        formCard.setVisible(false);
        formCard.setManaged(false);

        if (initialSection == InitialSection.PARTICIPATIONS) {
            showParticipationView();
        } else {
            showTournamentView();
        }
        initialSection = InitialSection.TOURNOIS;
    }

    private void configureColumns() {
        colId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        colNom.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNomTournoi()));
        colTypeTournoi.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getTypeTournoi()));
        colTypeJeu.setCellValueFactory(data -> new ReadOnlyStringWrapper(inferTypeJeu(data.getValue().getNomJeu())));
        colJeu.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNomJeu()));
        colDateDebut.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDateDebut().toString()));
        colDateFin.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDateFin().toString()));
        colParticipants.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getNombreParticipants()));
        colCashPrize.setCellValueFactory(data -> new ReadOnlyStringWrapper(String.format("$%.2f", data.getValue().getCashPrize())));

        colActions.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final ToolBar actionBar = new ToolBar(editButton, deleteButton);

            {
                actionBar.getStyleClass().add("action-toolbar");
                editButton.getStyleClass().add("action-edit-btn");
                deleteButton.getStyleClass().add("action-delete-btn");

                editButton.setOnAction(event -> {
                    Tournoi tournoi = getTableView().getItems().get(getIndex());
                    editTournoi(tournoi);
                });

                deleteButton.setOnAction(event -> {
                    Tournoi tournoi = getTableView().getItems().get(getIndex());
                    deleteTournoi(tournoi);
                });
            }

            @Override
            protected void updateItem(Tournoi tournoi, boolean empty) {
                super.updateItem(tournoi, empty);
                setGraphic(empty ? null : actionBar);
            }
        });

        tournoiTable.setItems(tournoiList);
    }

    private void configureParticipationAdminTable() {
        colPartAdminId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        colPartAdminTournoiId.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getTournoiId()));
        colPartAdminTournoiNom.setCellValueFactory(data -> new ReadOnlyStringWrapper(getTournoiNom(data.getValue().getTournoiId())));
        colPartAdminDescription.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDescription()));
        colPartAdminNiveau.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getNiveau()));

        colPartAdminActions.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        colPartAdminActions.setCellFactory(col -> new TableCell<>() {
            private final Button acceptButton = new Button("Accepter");
            private final Button rejectButton = new Button("Refuser");
            private final ToolBar actionBar = new ToolBar(acceptButton, rejectButton);

            {
                actionBar.getStyleClass().add("action-toolbar");
                acceptButton.getStyleClass().add("action-edit-btn");
                rejectButton.getStyleClass().add("action-delete-btn");

                acceptButton.setOnAction(event -> {
                    DemandeParticipation demande = getTableView().getItems().get(getIndex());
                    acceptParticipation(demande);
                });
                rejectButton.setOnAction(event -> {
                    DemandeParticipation demande = getTableView().getItems().get(getIndex());
                    rejectParticipation(demande);
                });
            }

            @Override
            protected void updateItem(DemandeParticipation demandeParticipation, boolean empty) {
                super.updateItem(demandeParticipation, empty);
                setGraphic(empty ? null : actionBar);
            }
        });

        participationAdminTable.setItems(participationRequestList);
    }

    private void showParticipationView() {
        tournamentScroll.setVisible(false);
        tournamentScroll.setManaged(false);
        participationScroll.setVisible(true);
        participationScroll.setManaged(true);
    }

    private void showTournamentView() {
        participationScroll.setVisible(false);
        participationScroll.setManaged(false);
        tournamentScroll.setVisible(true);
        tournamentScroll.setManaged(true);
    }

    @FXML
    private void onShowParticipationView() {
        loadParticipationsRequests();
        showParticipationView();
        participationAdminStatusLabel.setText("Demandes chargees.");
    }

    @FXML
    private void onBackToTournois() {
        showTournamentView();
        statusLabel.setText("Retour aux tournois.");
    }

    @FXML
    private void onRefreshParticipations() {
        loadParticipationsRequests();
        participationAdminStatusLabel.setText("Demandes rafraichies.");
    }

    private void loadParticipationsRequests() {
        participationRequestList.setAll(demandeParticipationService.afficherParStatut(DemandeParticipation.STATUT_EN_ATTENTE));
    }

    private void acceptParticipation(DemandeParticipation demande) {
        Tournoi tournoi = tournoiById.get(demande.getTournoiId());
        if (tournoi == null) {
            participationAdminStatusLabel.setText("Tournoi introuvable pour cette demande.");
            return;
        }
        if (tournoi.getNombreParticipants() <= 0) {
            participationAdminStatusLabel.setText("Impossible d'accepter: aucune place disponible.");
            return;
        }

        tournoi.setNombreParticipants(tournoi.getNombreParticipants() - 1);
        tournoiService.updateEntity(tournoi.getId(), tournoi);
        demandeParticipationService.updateStatut(demande.getId(), DemandeParticipation.STATUT_ACCEPTEE);
        loadParticipationsRequests();
        loadTournois();
        participationAdminStatusLabel.setText("Demande acceptee et place decrementee.");
    }

    private void rejectParticipation(DemandeParticipation demande) {
        demandeParticipationService.updateStatut(demande.getId(), DemandeParticipation.STATUT_REFUSEE);
        loadParticipationsRequests();
        participationAdminStatusLabel.setText("Demande refusee.");
    }

    private String getTournoiNom(int tournoiId) {
        Tournoi tournoi = tournoiById.get(tournoiId);
        return tournoi == null ? "Inconnu" : tournoi.getNomTournoi();
    }

    @FXML
    private void goToTournoisAdmin(ActionEvent event) {
        showTournamentView();
    }

    @FXML
    private void goToParticipationsAdmin(ActionEvent event) {
        onShowParticipationView();
    }

    @FXML
    private void goToFrontOffice(ActionEvent event) {
        SceneManager.switchScene("/ajoutProduit.fxml", "E-SPORTIFY : Boutique");
    }

    @FXML
    private void goToGestionComptes(ActionEvent event) {
        SceneManager.switchScene("/edu/ProjetPI/views/admin-dashboard.fxml", "Gestion des comptes");
    }

    @FXML
    private void goToMailing(ActionEvent event) {
        SceneManager.switchScene("/backMailing.fxml", "Boutique Admin - Mailing");
    }

    @FXML
    private void goToCatalogue(ActionEvent event) {
        SceneManager.switchScene("/backListProduit.fxml", "Gestion des produits");
    }

    @FXML
    private void goToAdminCategorie(ActionEvent event) {
        SceneManager.switchScene("/backListCategorie.fxml", "Boutique Admin - Categories");
    }

    @FXML
    private void goToPayments(ActionEvent event) {
        edu.PROJETPI.AdminDashboardController.openOn(edu.PROJETPI.AdminDashboardController.InitialSection.PAIEMENTS);
        SceneManager.switchScene("/admin-dashboard-view.fxml", "Liste des paiements");
    }

    @FXML
    private void goToRevenuePrediction(ActionEvent event) {
        edu.PROJETPI.AdminDashboardController.openOn(edu.PROJETPI.AdminDashboardController.InitialSection.PREDICTION_CA);
        SceneManager.switchScene("/admin-dashboard-view.fxml", "Prediction chiffre d'affaires");
    }

    @FXML
    private void goToCommandes(ActionEvent event) {
        edu.PROJETPI.AdminDashboardController.openOn(edu.PROJETPI.AdminDashboardController.InitialSection.COMMANDES);
        SceneManager.switchScene("/admin-dashboard-view.fxml", "Liste des commandes");
    }

    @FXML
    private void handleViewProfile(ActionEvent event) {
        SceneManager.switchScene("/edu/ProjetPI/views/profile.fxml", "Mon Profil");
    }

    @FXML
    private void handleThemeToggle() {
        statusLabel.setText("Theme conserve (style admin actif).");
    }

    @FXML
    private void handleLogout() {
        DashboardSession.clear();
        SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "Login");
    }

    @FXML
    private void onToggleCreateForm() {
        if (formCard.isVisible()) {
            hideForm();
            clearForm();
            selectedTournoi = null;
            statusLabel.setText("Formulaire masque.");
            return;
        }
        showFormForCreate();
    }

    @FXML
    private void onRefresh() {
        loadTournois();
        statusLabel.setText("Liste des tournois rafraichie.");
    }

    @FXML
    private void onGeneratePrediction() {
        refreshPredictionPanel();
    }

    @FXML
    private void onGoToUserView() {
        SceneNavigator.showUserView();
    }

    @FXML
    private void onConsultJeux() {
        SceneManager.switchScene("/fxml/rawg-games-view.fxml", "E-Sportify Admin - Consulter Jeux");
    }

    @FXML
    private void onCancelForm() {
        clearForm();
        selectedTournoi = null;
        hideForm();
        statusLabel.setText("Creation/modification annulee.");
    }

    @FXML
    private void onSaveTournoi() {
        if (!validateInputs()) {
            return;
        }

        Tournoi tournoi = buildTournoiFromForm();
        if (selectedTournoi == null) {
            tournoiService.ajouter(tournoi);
            statusLabel.setText("Tournoi ajoute avec succes.");
        } else {
            tournoiService.updateEntity(selectedTournoi.getId(), tournoi);
            statusLabel.setText("Tournoi modifie avec succes.");
        }

        loadTournois();
        clearForm();
        selectedTournoi = null;
        hideForm();
    }

    private void editTournoi(Tournoi tournoi) {
        selectedTournoi = tournoi;
        formTitle.setText("Modifier un Tournoi");
        btnCreateTournoi.setText("Fermer formulaire");

        tfNomTournoi.setText(tournoi.getNomTournoi());
        cbTypeTournoi.setValue(tournoi.getTypeTournoi());
        cbTypeJeu.setValue(inferTypeJeu(tournoi.getNomJeu()));
        tfNomJeu.setText(tournoi.getNomJeu());
        dpDateDebut.setValue(tournoi.getDateDebut().toLocalDate());
        dpDateFin.setValue(tournoi.getDateFin().toLocalDate());
        tfCashPrize.setText(String.valueOf(tournoi.getCashPrize()));
        tfNombreParticipants.setText(String.valueOf(tournoi.getNombreParticipants()));

        formCard.setVisible(true);
        formCard.setManaged(true);
        statusLabel.setText("Edition du tournoi ID " + tournoi.getId() + ".");
    }

    private void deleteTournoi(Tournoi tournoi) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation suppression");
        alert.setHeaderText("Supprimer le tournoi \"" + tournoi.getNomTournoi() + "\" ?");
        alert.setContentText("Cette action est irreversible.");
        Optional<ButtonType> choice = alert.showAndWait();

        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            tournoiService.supprimer(tournoi.getId());
            loadTournois();
            statusLabel.setText("Tournoi supprime.");
        }
    }

    private Tournoi buildTournoiFromForm() {
        String nomTournoi = tfNomTournoi.getText().trim();
        String typeTournoi = cbTypeTournoi.getValue();
        String nomJeu = tfNomJeu.getText().trim();
        LocalDate dateDebut = dpDateDebut.getValue();
        LocalDate dateFin = dpDateFin.getValue();
        int participants = tfNombreParticipants.getText().trim().isEmpty() ? 4 : Integer.parseInt(tfNombreParticipants.getText().trim());
        double cashPrize = Double.parseDouble(tfCashPrize.getText().trim());

        return new Tournoi(
                nomTournoi,
                typeTournoi,
                nomJeu,
                Date.valueOf(dateDebut),
                Date.valueOf(dateFin),
                participants,
                cashPrize
        );
    }

    private boolean validateInputs() {
        if (tfNomTournoi.getText() == null || tfNomTournoi.getText().trim().isEmpty()) {
            statusLabel.setText("Nom du tournoi obligatoire.");
            return false;
        }
        if (cbTypeTournoi.getValue() == null) {
            statusLabel.setText("Type de tournoi obligatoire.");
            return false;
        }
        if (tfNomJeu.getText() == null || tfNomJeu.getText().trim().isEmpty()) {
            statusLabel.setText("Nom du jeu obligatoire.");
            return false;
        }
        if (dpDateDebut.getValue() == null || dpDateFin.getValue() == null) {
            statusLabel.setText("Date debut et date fin obligatoires.");
            return false;
        }
        if (dpDateFin.getValue().isBefore(dpDateDebut.getValue())) {
            statusLabel.setText("La date de fin doit etre apres la date de debut.");
            return false;
        }
        try {
            if (!tfNombreParticipants.getText().trim().isEmpty()) {
                int participants = Integer.parseInt(tfNombreParticipants.getText().trim());
                if (participants < 4) {
                    statusLabel.setText("Le nombre de participants doit etre >= 4.");
                    return false;
                }
            }
            double cashPrize = Double.parseDouble(tfCashPrize.getText().trim());
            if (cashPrize < 0) {
                statusLabel.setText("Le cash prize ne doit pas etre negatif.");
                return false;
            }
        } catch (NumberFormatException ex) {
            statusLabel.setText("Montant/prix ou participants invalide.");
            return false;
        }

        return true;
    }

    private void loadTournois() {
        tournoiList.setAll(tournoiService.afficher());
        tournoiById.clear();
        for (Tournoi tournoi : tournoiList) {
            tournoiById.put(tournoi.getId(), tournoi);
        }
        refreshDashboard();
        refreshPredictionPanel();
    }

    private void configureDashboardCharts() {
        xTypeTournoiAxis.setCategories(FXCollections.observableArrayList("SOLO", "DUO", "SQUAD", "LIGUE"));
        xTypeJeuAxis.setCategories(FXCollections.observableArrayList("FPS", "SPORTS", "MIND", "BATTLE ROYALE"));
        yTypeTournoiAxis.setAutoRanging(true);
        yTypeJeuAxis.setAutoRanging(true);
    }

    private void refreshDashboard() {
        Map<String, Integer> typeTournoiCounts = new LinkedHashMap<>();
        typeTournoiCounts.put("SOLO", 0);
        typeTournoiCounts.put("DUO", 0);
        typeTournoiCounts.put("SQUAD", 0);
        typeTournoiCounts.put("LIGUE", 0);

        Map<String, Integer> typeJeuCounts = new LinkedHashMap<>();
        typeJeuCounts.put("FPS", 0);
        typeJeuCounts.put("SPORTS", 0);
        typeJeuCounts.put("MIND", 0);
        typeJeuCounts.put("BATTLE ROYALE", 0);

        for (Tournoi tournoi : tournoiList) {
            String typeTournoi = normalizeTypeTournoi(tournoi.getTypeTournoi());
            typeTournoiCounts.computeIfPresent(typeTournoi, (k, v) -> v + 1);

            String typeJeu = inferTypeJeu(tournoi.getNomJeu());
            typeJeuCounts.computeIfPresent(typeJeu, (k, v) -> v + 1);
        }

        lblTotalTournois.setText(String.valueOf(tournoiList.size()));
        lblTypesTournoiActifs.setText(String.valueOf(countActiveCategories(typeTournoiCounts.values().stream().toList())));
        lblTypesJeuActifs.setText(String.valueOf(countActiveCategories(typeJeuCounts.values().stream().toList())));

        XYChart.Series<String, Number> tournoiSeries = new XYChart.Series<>();
        typeTournoiCounts.forEach((key, value) -> tournoiSeries.getData().add(new XYChart.Data<>(key, value)));
        barTypeTournoiChart.getData().setAll(tournoiSeries);

        XYChart.Series<String, Number> jeuSeries = new XYChart.Series<>();
        typeJeuCounts.forEach((key, value) -> jeuSeries.getData().add(new XYChart.Data<>(key, value)));
        barTypeJeuChart.getData().setAll(jeuSeries);
    }

    private int countActiveCategories(List<Integer> values) {
        int active = 0;
        for (Integer value : values) {
            if (value != null && value > 0) {
                active++;
            }
        }
        return active;
    }

    private String normalizeTypeTournoi(String typeTournoi) {
        if (typeTournoi == null || typeTournoi.trim().isEmpty()) {
            return "SOLO";
        }
        String normalized = typeTournoi.trim().toUpperCase();
        if ("LEAGUE".equals(normalized)) {
            return "LIGUE";
        }
        if (normalized.startsWith("LIG")) {
            return "LIGUE";
        }
        if (normalized.contains("SQUAD")) {
            return "SQUAD";
        }
        if (normalized.contains("DUO")) {
            return "DUO";
        }
        if (normalized.contains("SOLO")) {
            return "SOLO";
        }
        return "SOLO";
    }

    private void showFormForCreate() {
        selectedTournoi = null;
        clearForm();
        formTitle.setText("Ajouter un Tournoi");
        btnCreateTournoi.setText("Fermer formulaire");
        formCard.setVisible(true);
        formCard.setManaged(true);
        statusLabel.setText("Remplis les champs pour creer un tournoi.");
    }

    private void hideForm() {
        formCard.setVisible(false);
        formCard.setManaged(false);
        btnCreateTournoi.setText("Creer un tournoi");
    }

    private void clearForm() {
        tfNomTournoi.clear();
        cbTypeTournoi.setValue(null);
        cbTypeJeu.setValue(null);
        tfNomJeu.clear();
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);
        tfCashPrize.clear();
        tfNombreParticipants.clear();
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

    private void refreshPredictionPanel() {
        PredictionData prediction = buildPrediction();
        lblPredictionType.setText(prediction.typeTournoi);
        lblPredictionJeu.setText(prediction.nomJeu);
        lblPredictionConfidence.setText(String.format(Locale.US, "%.0f%%", prediction.confidence));
        predictionStatusLabel.setText(prediction.message);
    }

    private PredictionData buildPrediction() {
        if (tournoiList.isEmpty()) {
            return new PredictionData(
                    "SOLO",
                    "Valorant",
                    0,
                    "Aucune donnee disponible pour predire. Ajoute quelques tournois pour activer l'analyse."
            );
        }

        Map<String, Integer> typeTournoiCounts = new LinkedHashMap<>();
        typeTournoiCounts.put("SOLO", 0);
        typeTournoiCounts.put("DUO", 0);
        typeTournoiCounts.put("SQUAD", 0);
        typeTournoiCounts.put("LIGUE", 0);

        Map<String, Integer> jeuCounts = new LinkedHashMap<>();

        for (Tournoi tournoi : tournoiList) {
            String typeTournoi = normalizeTypeTournoi(tournoi.getTypeTournoi());
            typeTournoiCounts.computeIfPresent(typeTournoi, (k, v) -> v + 1);

            String nomJeu = tournoi.getNomJeu() == null ? "" : tournoi.getNomJeu().trim();
            if (!nomJeu.isEmpty()) {
                jeuCounts.merge(nomJeu, 1, Integer::sum);
            }
        }

        String predictedType = getMaxKey(typeTournoiCounts, "SOLO");
        String predictedJeu = getMaxKey(jeuCounts, "Valorant");
        int totalTournois = tournoiList.size();
        int topTypeCount = typeTournoiCounts.getOrDefault(predictedType, 0);
        int topJeuCount = jeuCounts.getOrDefault(predictedJeu, 0);

        double typeShare = (double) topTypeCount / totalTournois;
        double jeuShare = (double) topJeuCount / totalTournois;
        double confidence = ((typeShare * 0.55) + (jeuShare * 0.45)) * 100;
        double dataFactor = Math.min(1.0, totalTournois / 8.0);
        confidence = confidence * (0.65 + (0.35 * dataFactor));

        String message = String.format(
                Locale.US,
                "Prediction basee sur %d tournois: priorise %s sur %s (confiance %.0f%%).",
                totalTournois,
                predictedType,
                predictedJeu,
                confidence
        );

        return new PredictionData(predictedType, predictedJeu, confidence, message);
    }

    private String getMaxKey(Map<String, Integer> values, String fallback) {
        return values.entrySet()
                .stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .filter(entry -> entry.getValue() > 0)
                .map(Map.Entry::getKey)
                .orElse(fallback);
    }

    private static final class PredictionData {
        private final String typeTournoi;
        private final String nomJeu;
        private final double confidence;
        private final String message;

        private PredictionData(String typeTournoi, String nomJeu, double confidence, String message) {
            this.typeTournoi = typeTournoi;
            this.nomJeu = nomJeu;
            this.confidence = confidence;
            this.message = message;
        }
    }
}
