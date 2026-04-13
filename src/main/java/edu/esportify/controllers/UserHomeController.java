package edu.esportify.controllers;

import edu.esportify.entities.Equipe;
import edu.esportify.entities.Recrutement;
import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.EquipeService;
import edu.esportify.services.RecrutementService;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class UserHomeController {
    private final EquipeService equipeService = new EquipeService();
    private final RecrutementService recrutementService = new RecrutementService();

    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox teamsContainer;

    @FXML
    private VBox recrutementsContainer;

    @FXML
    private void initialize() {
        welcomeLabel.setText("Bienvenue " + AppSession.getInstance().getUsername());

        List<Equipe> equipes = equipeService.getData();
        if (equipes.isEmpty()) {
            teamsContainer.getChildren().add(createCard("Aucune equipe", "Aucune equipe publiee pour le moment."));
        } else {
            for (Equipe equipe : equipes) {
                teamsContainer.getChildren().add(createCard(equipe.getNomEquipe(), equipe.getDescription()));
            }
        }

        List<Recrutement> recrutements = recrutementService.getData();
        if (recrutements.isEmpty()) {
            recrutementsContainer.getChildren().add(createCard("Aucun recrutement", "Aucune offre disponible."));
        } else {
            for (Recrutement recrutement : recrutements) {
                recrutementsContainer.getChildren().add(createCard(recrutement.getNomRec(), recrutement.getDescription()));
            }
        }
    }

    @FXML
    private void onLogout() {
        AppSession.getInstance().logout();
        AppNavigator.goToLogin();
    }

    private VBox createCard(String title, String description) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        Label descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("muted-label");
        descriptionLabel.setWrapText(true);
        VBox box = new VBox(8, titleLabel, descriptionLabel);
        box.getStyleClass().add("summary-card");
        return box;
    }
}
