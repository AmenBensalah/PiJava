package edu.esportify.controllers;

import edu.esportify.entities.Equipe;
import edu.esportify.entities.ManagerRequest;
import edu.esportify.entities.UserRole;
import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.CandidatureService;
import edu.esportify.services.EquipeService;
import edu.esportify.services.ManagerRequestService;
import edu.esportify.services.RecrutementService;
import java.util.List;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public class UserTeamsController implements UserContentController {
    private static final String ALL_OPTION = "Tous";

    private final EquipeService equipeService = new EquipeService();
    private final CandidatureService candidatureService = new CandidatureService();
    private final RecrutementService recrutementService = new RecrutementService();
    private final ManagerRequestService managerRequestService = new ManagerRequestService();

    private UserLayoutController parentController;
    private Equipe recommendedEquipe;

    @FXML private TextField nomField;
    @FXML private ComboBox<String> regionBox;
    @FXML private ComboBox<String> niveauBox;
    @FXML private Label resultsLabel;
    @FXML private Label activeTeamsLabel;
    @FXML private Label candidaturesLabel;
    @FXML private Label recrutementsLabel;
    @FXML private Label aiRecommendationTitleLabel;
    @FXML private Label aiRecommendationReasonLabel;
    @FXML private Label aiRecommendationMetaLabel;
    @FXML private Label managerStatusLabel;
    @FXML private Label myTeamStatusLabel;
    @FXML private Button recommendedProfileButton;
    @FXML private Button recommendedApplyButton;
    @FXML private Button viewMyTeamButton;
    @FXML private Button manageMyTeamButton;
    @FXML private Button managerDashboardButton;
    @FXML private FlowPane teamsContainer;

    @FXML
    private void initialize() {
        regionBox.getItems().setAll(
                ALL_OPTION,
                "Europe",
                "North America",
                "South America",
                "Asia",
                "Middle East",
                "Africa"
        );
        niveauBox.getItems().setAll(
                ALL_OPTION,
                "Bronze",
                "Argent",
                "Or",
                "Platine",
                "Diamond",
                "Master"
        );
        regionBox.setValue(ALL_OPTION);
        niveauBox.setValue(ALL_OPTION);
    }

    @Override
    public void init(UserLayoutController parentController) {
        this.parentController = parentController;
        refreshDashboard();
        renderTeams(equipeService.getData());
    }

    @FXML
    private void onSearch() {
        renderTeams(searchTeams());
    }

    @FXML
    private void onReset() {
        nomField.clear();
        regionBox.setValue(ALL_OPTION);
        niveauBox.setValue(ALL_OPTION);
        renderTeams(equipeService.getData());
    }

    @FXML
    private void onMyApplications() {
        if (parentController != null) {
            parentController.showCandidatures();
        }
    }

    @FXML
    private void onViewMyTeam() {
        if (parentController != null) {
            parentController.showMyTeam();
        }
    }

    @FXML
    private void onBecomeManager() {
        if (isCurrentUserManager()) {
            AppNavigator.goToManager();
            return;
        }
        if (parentController != null) {
            parentController.showManagerRequestForm();
        }
    }

    @FXML
    private void onOpenManagerDashboard() {
        if (isCurrentUserManager()) {
            AppNavigator.goToManager();
        } else if (parentController != null) {
            parentController.showManagerRequestForm();
        }
    }

    @FXML
    private void onOpenRecommendedProfile() {
        if (recommendedEquipe != null && parentController != null) {
            parentController.showTeamDetail(recommendedEquipe);
        }
    }

    @FXML
    private void onApplyRecommendedTeam() {
        if (recommendedEquipe != null && parentController != null) {
            parentController.showApply(recommendedEquipe);
        }
    }

    @FXML
    private void onShowAiRecommendation() {
        refreshRecommendation(searchTeams());
    }

    private void refreshDashboard() {
        List<Equipe> teams = equipeService.getData();
        activeTeamsLabel.setText(String.valueOf(teams.stream().filter(Equipe::isActive).count()));
        candidaturesLabel.setText(String.valueOf(
                candidatureService.getByAccountUsername(AppSession.getInstance().getUsername()).size()
        ));
        recrutementsLabel.setText(String.valueOf(recrutementService.getData().size()));

        boolean hasTeam = candidatureService.getAcceptedForUser(AppSession.getInstance().getUsername()) != null;
        viewMyTeamButton.setVisible(hasTeam);
        viewMyTeamButton.setManaged(hasTeam);
        myTeamStatusLabel.setText(hasTeam ? "Equipe active detectee" : "Aucune equipe rejointe pour l'instant");

        boolean canManage = isCurrentUserManager();
        manageMyTeamButton.setText(canManage ? "Gerer mon equipe" : "Demander acces manager");
        managerDashboardButton.setText(canManage ? "Dashboard IA" : "Demande manager");
        managerStatusLabel.setText(resolveManagerStatus());

        refreshRecommendation(teams);
    }

    private void refreshRecommendation(List<Equipe> teams) {
        recommendedEquipe = findRecommendedTeam(teams);
        boolean hasRecommendation = recommendedEquipe != null;
        recommendedProfileButton.setDisable(!hasRecommendation);
        recommendedApplyButton.setDisable(!hasRecommendation);

        if (!hasRecommendation) {
            aiRecommendationTitleLabel.setText("Aucune recommandation disponible");
            aiRecommendationReasonLabel.setText("Ajoute des filtres differents ou attends de nouvelles equipes.");
            aiRecommendationMetaLabel.setText("IA indisponible");
            return;
        }

        aiRecommendationTitleLabel.setText(value(recommendedEquipe.getNomEquipe()));
        aiRecommendationReasonLabel.setText(buildRecommendationReason(recommendedEquipe));
        aiRecommendationMetaLabel.setText(
                value(recommendedEquipe.getClassement()) + " • " + value(recommendedEquipe.getRegion())
        );
    }

    private void renderTeams(List<Equipe> teams) {
        teamsContainer.getChildren().clear();

        if (teams.isEmpty()) {
            resultsLabel.setText("0 equipe trouvee.");
            teamsContainer.getChildren().add(UserViewFactory.createMuted("Aucune equipe ne correspond a la recherche."));
            refreshRecommendation(List.of());
            return;
        }

        for (Equipe equipe : teams) {
            teamsContainer.getChildren().add(UserViewFactory.createUserTeamCard(
                    equipe,
                    () -> openTeamProfile(equipe),
                    () -> applyToTeam(equipe)
            ));
        }

        resultsLabel.setText(teams.size() + (teams.size() > 1 ? " equipes trouvees." : " equipe trouvee."));
        refreshRecommendation(teams);
    }

    private List<Equipe> searchTeams() {
        String nom = value(nomField.getText()).trim();
        String region = normalizeFilter(regionBox.getValue());
        String niveau = normalizeFilter(niveauBox.getValue());
        return equipeService.search(nom, region, niveau);
    }

    private void openTeamProfile(Equipe equipe) {
        if (parentController != null) {
            parentController.showTeamDetail(equipe);
        }
    }

    private void applyToTeam(Equipe equipe) {
        if (parentController != null) {
            parentController.showApply(equipe);
        }
    }

    private Equipe findRecommendedTeam(List<Equipe> teams) {
        String username = value(AppSession.getInstance().getUsername()).toLowerCase(Locale.ROOT);
        String currentRole = value(AppSession.getInstance().getRole()).toLowerCase(Locale.ROOT);
        return teams.stream()
                .filter(Equipe::isActive)
                .filter(equipe -> !equipe.isCurrentlyBanned())
                .sorted((left, right) -> Integer.compare(scoreTeam(right, username, currentRole), scoreTeam(left, username, currentRole)))
                .findFirst()
                .orElse(null);
    }

    private int scoreTeam(Equipe equipe, String username, String currentRole) {
        int score = 0;
        if (!value(equipe.getRegion()).isBlank()) {
            score += 2;
        }
        if (!value(equipe.getClassement()).isBlank()) {
            score += 2;
        }
        if (!value(equipe.getDescription()).isBlank()) {
            score += 1;
        }
        if (value(equipe.getManagerUsername()).toLowerCase(Locale.ROOT).contains(username) && !username.isBlank()) {
            score -= 10;
        }
        if (currentRole.contains("manager")) {
            score -= 3;
        }
        return score;
    }

    private String buildRecommendationReason(Equipe equipe) {
        StringBuilder reason = new StringBuilder("Equipe active");
        if (!value(equipe.getRegion()).isBlank()) {
            reason.append(" en region ").append(value(equipe.getRegion()));
        }
        if (!value(equipe.getClassement()).isBlank()) {
            reason.append(" avec un niveau ").append(value(equipe.getClassement()));
        }
        if (!value(equipe.getDescription()).isBlank()) {
            reason.append(". ").append(value(equipe.getDescription()));
        }
        return reason.toString();
    }

    private String resolveManagerStatus() {
        if (isCurrentUserManager()) {
            return "Acces manager actif";
        }

        String username = AppSession.getInstance().getUsername();
        ManagerRequest request = managerRequestService.getData().stream()
                .filter(item -> value(item.getUsername()).equalsIgnoreCase(value(username)))
                .findFirst()
                .orElse(null);

        if (request == null) {
            return "Aucune demande manager en cours";
        }
        return "Demande manager: " + value(request.getStatus());
    }

    private boolean isCurrentUserManager() {
        if (AppSession.getInstance().getCurrentUser() != null
                && AppSession.getInstance().getCurrentUser().getRole() == UserRole.MANAGER) {
            return true;
        }
        return equipeService.getByManagerUsername(AppSession.getInstance().getUsername()) != null;
    }

    private String normalizeFilter(String value) {
        return ALL_OPTION.equalsIgnoreCase(value(value)) ? "" : value(value);
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
