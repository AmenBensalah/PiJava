package edu.connexion3a77.controllers;

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
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TournoiAdminController {

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
        configureParticipationAdminTable();
        loadTournois();
        loadParticipationsRequests();

        formCard.setVisible(false);
        formCard.setManaged(false);
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
        tournamentPane.setVisible(false);
        tournamentPane.setManaged(false);
        participationPane.setVisible(true);
        participationPane.setManaged(true);
    }

    private void showTournamentView() {
        participationPane.setVisible(false);
        participationPane.setManaged(false);
        tournamentPane.setVisible(true);
        tournamentPane.setManaged(true);
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
        participationRequestList.setAll(demandeParticipationService.afficher());
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
        demandeParticipationService.supprimer(demande.getId());
        loadParticipationsRequests();
        loadTournois();
        participationAdminStatusLabel.setText("Demande acceptee et place decrementee.");
    }

    private void rejectParticipation(DemandeParticipation demande) {
        demandeParticipationService.supprimer(demande.getId());
        loadParticipationsRequests();
        participationAdminStatusLabel.setText("Demande refusee.");
    }

    private String getTournoiNom(int tournoiId) {
        Tournoi tournoi = tournoiById.get(tournoiId);
        return tournoi == null ? "Inconnu" : tournoi.getNomTournoi();
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
    private void onGoToUserView() {
        SceneNavigator.showUserView();
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
}
