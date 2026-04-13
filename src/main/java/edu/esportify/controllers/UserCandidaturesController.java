package edu.esportify.controllers;

import edu.esportify.entities.Candidature;
import edu.esportify.entities.Equipe;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.CandidatureService;
import edu.esportify.services.EquipeService;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class UserCandidaturesController implements UserContentController {
    private final CandidatureService candidatureService = new CandidatureService();
    private final EquipeService equipeService = new EquipeService();

    private UserLayoutController parentController;

    @FXML private Button viewMyTeamButton;
    @FXML private VBox candidaturesContainer;

    @Override
    public void init(UserLayoutController parentController) {
        this.parentController = parentController;
        refresh();
    }

    @FXML
    private void onBack() {
        if (parentController != null) {
            parentController.showTeams();
        }
    }

    @FXML
    private void onViewMyTeam() {
        if (parentController != null) {
            parentController.showMyTeam();
        }
    }

    private void refresh() {
        List<Candidature> candidatures = candidatureService.getData().stream()
                .filter(candidature -> AppSession.getInstance().getUsername().equalsIgnoreCase(value(candidature.getAccountUsername())))
                .toList();
        boolean hasTeam = candidatureService.getAcceptedForUser(AppSession.getInstance().getUsername()) != null;
        viewMyTeamButton.setVisible(hasTeam);
        viewMyTeamButton.setManaged(hasTeam);
        candidaturesContainer.getChildren().clear();
        if (candidatures.isEmpty()) {
            candidaturesContainer.getChildren().add(UserViewFactory.createMuted("Aucune candidature envoyee."));
            return;
        }
        for (Candidature candidature : candidatures) {
            candidaturesContainer.getChildren().add(UserViewFactory.createUserApplicationCard(
                    candidature,
                    () -> edit(candidature),
                    () -> delete(candidature)
            ));
        }
    }

    private void edit(Candidature candidature) {
        if (parentController == null) {
            return;
        }
        Equipe equipe = equipeService.getById(candidature.getEquipeId());
        if (equipe != null) {
            parentController.showApply(equipe);
        }
    }

    private void delete(Candidature candidature) {
        candidatureService.deleteEntity(candidature);
        refresh();
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
