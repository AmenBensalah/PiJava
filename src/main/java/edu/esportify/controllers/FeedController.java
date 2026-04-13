package edu.esportify.controllers;

import edu.esportify.entities.Equipe;
import edu.esportify.entities.Recrutement;
import edu.esportify.services.EquipeService;
import edu.esportify.services.RecrutementService;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class FeedController implements ManagerContentController {
    private final EquipeService equipeService = new EquipeService();
    private final RecrutementService recrutementService = new RecrutementService();

    @FXML private Label equipesCountLabel;
    @FXML private Label recrutementsCountLabel;
    @FXML private TextArea postArea;
    @FXML private VBox postsContainer;

    @Override
    public void init(ManagerLayoutController parentController) {
        List<Equipe> equipes = equipeService.getData();
        List<Recrutement> recrutements = recrutementService.getData();
        equipesCountLabel.setText(equipes.size() + " equipes");
        recrutementsCountLabel.setText(recrutements.size() + " recrutements");

        postsContainer.getChildren().clear();
        if (equipes.isEmpty() && recrutements.isEmpty()) {
            postsContainer.getChildren().add(createPostCard(
                    "Communaute E-sportify",
                    "Publie des annonces, partage des videos et suis les tendances de la communaute E-sportify."
            ));
            return;
        }

        for (Equipe equipe : equipes) {
            postsContainer.getChildren().add(createPostCard(equipe.getNomEquipe(), equipe.getDescription()));
        }
        for (Recrutement recrutement : recrutements) {
            postsContainer.getChildren().add(createPostCard(recrutement.getNomRec(), recrutement.getDescription()));
        }
    }

    @FXML
    private void onPublish() {
        String message = postArea.getText() == null ? "" : postArea.getText().trim();
        if (message.isBlank()) {
            return;
        }
        postsContainer.getChildren().add(0, createPostCard("Vous", message));
        postArea.clear();
    }

    private VBox createPostCard(String title, String content) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        Label contentLabel = new Label(content == null || content.isBlank() ? "Aucun contenu." : content);
        contentLabel.getStyleClass().add("muted-label");
        contentLabel.setWrapText(true);
        VBox box = new VBox(8, titleLabel, contentLabel);
        box.getStyleClass().add("summary-card");
        return box;
    }
}
