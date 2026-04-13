package edu.esportify.controllers;

import edu.esportify.entities.Candidature;
import edu.esportify.entities.Equipe;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.CandidatureService;
import edu.esportify.services.EquipeService;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public class UserTeamsController implements UserContentController {
    private final EquipeService equipeService = new EquipeService();
    private final CandidatureService candidatureService = new CandidatureService();

    private UserLayoutController parentController;

    @FXML private Label activeTeamsLabel;
    @FXML private Label candidaturesLabel;
    @FXML private Label recrutementsLabel;
    @FXML private Label myTeamStatusLabel;
    @FXML private Button viewMyTeamButton;
    @FXML private TextField nomField;
    @FXML private ComboBox<String> regionBox;
    @FXML private ComboBox<String> niveauBox;
    @FXML private Label resultsLabel;
    @FXML private FlowPane teamsContainer;

    @FXML
    private void initialize() {
        regionBox.getItems().setAll("Toutes", "Europe", "MENA", "NA");
        niveauBox.getItems().setAll("Tous", "Diamond", "Master", "Platinum", "Gold");
        regionBox.setValue("Toutes");
        niveauBox.setValue("Tous");
        nomField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        regionBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        niveauBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    @Override
    public void init(UserLayoutController parentController) {
        this.parentController = parentController;
        refreshAnalytics();
        applyFilters();
    }

    @FXML
    private void onSearch() {
        applyFilters();
    }

    @FXML
    private void onReset() {
        nomField.clear();
        regionBox.setValue("Toutes");
        niveauBox.setValue("Tous");
        applyFilters();
    }

    @FXML
    private void onMyApplications() {
        parentController.showCandidatures();
    }

    @FXML
    private void onBecomeManager() {
        parentController.showManagerRequestForm();
    }

    @FXML
    private void onViewMyTeam() {
        parentController.showMyTeam();
    }

    private void refreshAnalytics() {
        List<Equipe> equipes = equipeService.getData();
        List<Candidature> candidatures = candidatureService.getByAccountUsername(AppSession.getInstance().getUsername());
        Candidature accepted = candidatureService.getAcceptedForUser(AppSession.getInstance().getUsername());
        int recrutements = Math.max(1, equipes.size() > 0 ? 1 : 0);
        activeTeamsLabel.setText(String.valueOf(equipes.size()));
        candidaturesLabel.setText(String.valueOf(candidatures.size()));
        recrutementsLabel.setText(String.valueOf(recrutements));
        myTeamStatusLabel.setText(accepted == null ? "Aucune equipe rejointe" : "Equipe actuelle: " + value(accepted.getEquipeNom()));
        viewMyTeamButton.setVisible(accepted != null);
        viewMyTeamButton.setManaged(accepted != null);
    }

    private void loadTeams(List<Equipe> equipes) {
        teamsContainer.getChildren().clear();
        resultsLabel.setText(equipes.size() + " equipe(s) trouvee(s)");
        if (equipes.isEmpty()) {
            Label empty = new Label("Aucune equipe disponible.");
            empty.getStyleClass().add("muted-label");
            teamsContainer.getChildren().add(empty);
            return;
        }
        for (Equipe equipe : equipes) {
            teamsContainer.getChildren().add(UserViewFactory.createUserTeamCard(
                    equipe,
                    () -> parentController.showTeamDetail(equipe),
                    () -> parentController.showApply(equipe)
            ));
        }
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private void applyFilters() {
        String nom = value(nomField.getText());
        String region = "Toutes".equals(regionBox.getValue()) ? "" : value(regionBox.getValue());
        String niveau = "Tous".equals(niveauBox.getValue()) ? "" : value(niveauBox.getValue());
        loadTeams(equipeService.search(nom, region, niveau));
    }
}
