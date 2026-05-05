package edu.esportify.controllers;

import edu.esportify.entities.Candidature;
import edu.esportify.entities.Equipe;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.CandidatureService;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class CandidateManagementController implements ManagerContentController {
    private final CandidatureService candidatureService = new CandidatureService();

    private ManagerLayoutController parentController;
    private Equipe equipe;

    @FXML private Label subtitleLabel;
    @FXML private TableView<Candidature> candidatureTable;
    @FXML private TableColumn<Candidature, String> pseudoColumn;
    @FXML private TableColumn<Candidature, String> niveauColumn;
    @FXML private TableColumn<Candidature, String> disponibiliteColumn;
    @FXML private TableColumn<Candidature, String> statutColumn;
    @FXML private TableColumn<Candidature, Void> actionColumn;
    @FXML private Label emptyLabel;

    @FXML
    private void initialize() {
        pseudoColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getPseudoJoueur())));
        niveauColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getNiveau())));
        disponibiliteColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getDisponibilite())));
        statutColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getStatut())));
        actionColumn.setCellFactory(param -> new ActionCell());
    }

    @Override
    public void init(ManagerLayoutController parentController) {
        this.parentController = parentController;
        equipe = AppSession.getInstance().getSelectedEquipe();
        if (equipe == null) {
            parentController.showTeamCreate();
            return;
        }
        subtitleLabel.setText("Joueurs qui demandent l'acces a ton equipe: " + safe(equipe.getNomEquipe()));
        refreshTable();
    }

    @FXML
    private void onBack() {
        parentController.showTeamDashboard();
    }

    private void refreshTable() {
        List<Candidature> candidatures = candidatureService.getByEquipe(equipe.getId());
        candidatureTable.setItems(FXCollections.observableArrayList(candidatures));
        boolean empty = candidatures.isEmpty();
        candidatureTable.setVisible(!empty);
        candidatureTable.setManaged(!empty);
        emptyLabel.setVisible(empty);
        emptyLabel.setManaged(empty);
    }

    private final class ActionCell extends TableCell<Candidature, Void> {
        private final Button acceptButton = new Button("Accepter");
        private final Button refuseButton = new Button("Refuser");
        private final Button deleteButton = new Button("Supprimer");
        private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8, acceptButton, refuseButton, deleteButton);

        private ActionCell() {
            acceptButton.getStyleClass().addAll("classic-button", "table-action-button");
            refuseButton.getStyleClass().addAll("classic-button", "table-action-button");
            deleteButton.getStyleClass().addAll("classic-button", "table-action-button");

            acceptButton.setOnAction(event -> updateStatus("Acceptee"));
            refuseButton.setOnAction(event -> updateStatus("Refusee"));
            deleteButton.setOnAction(event -> {
                Candidature candidature = getTableView().getItems().get(getIndex());
                candidatureService.deleteEntity(candidature);
                refreshTable();
            });
        }

        private void updateStatus(String status) {
            Candidature candidature = getTableView().getItems().get(getIndex());
            if ("Acceptee".equalsIgnoreCase(status)) {
                candidatureService.acceptMembership(candidature);
            } else {
                candidature.setStatut(status);
                candidatureService.updateEntity(candidature.getId(), candidature);
            }
            refreshTable();
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : box);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
