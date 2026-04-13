package edu.esportify.controllers;

import edu.esportify.entities.Candidature;
import edu.esportify.entities.Equipe;
import edu.esportify.entities.Recrutement;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.CandidatureService;
import edu.esportify.services.RecrutementService;
import java.awt.Desktop;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class UserTeamDetailController implements UserContentController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final RecrutementService recrutementService = new RecrutementService();
    private final CandidatureService candidatureService = new CandidatureService();
    private UserLayoutController parentController;
    private Equipe equipe;
    private Candidature membership;

    @FXML private ImageView teamImageView;
    @FXML private Label tagChipLabel;
    @FXML private Label teamNameLabel;
    @FXML private Label teamDescriptionLabel;
    @FXML private Label regionChipLabel;
    @FXML private Label classementChipLabel;
    @FXML private Label visibiliteChipLabel;
    @FXML private Label membersCountLabel;
    @FXML private Label recruitmentCountLabel;
    @FXML private Label regionStatLabel;
    @FXML private Label creationDateLabel;
    @FXML private Button discordButton;
    @FXML private VBox rosterContainer;
    @FXML private VBox recrutementsContainer;
    @FXML private Button applyButton;
    @FXML private Button backButton;

    @Override
    public void init(UserLayoutController parentController) {
        this.parentController = parentController;
        equipe = AppSession.getInstance().getSelectedEquipe();
        if (equipe == null) {
            parentController.showTeams();
            return;
        }
        teamImageView.setImage(UserViewFactory.resolveTeamImage(equipe));
        tagChipLabel.setText(value(equipe.getTag()));
        teamNameLabel.setText(value(equipe.getNomEquipe()));
        teamDescriptionLabel.setText(value(equipe.getDescription()));
        regionChipLabel.setText(value(equipe.getRegion()));
        classementChipLabel.setText(value(equipe.getClassement()));
        visibiliteChipLabel.setText(equipe.isPrivate() ? "Privee" : "Publique");
        regionStatLabel.setText(value(equipe.getRegion()));
        membership = candidatureService.getAcceptedForUser(AppSession.getInstance().getUsername());
        creationDateLabel.setText(equipe.getDateCreation().format(DATE_FORMATTER));
        updateDiscordButton();
        updateActionButtons();

        int members = 1 + (int) candidatureService.getByEquipe(equipe.getId()).stream()
                .filter(c -> "Acceptee".equalsIgnoreCase(value(c.getStatut())))
                .count();
        membersCountLabel.setText(String.valueOf(members));

        var recrutements = recrutementService.getByEquipe(equipe.getId());
        recruitmentCountLabel.setText(String.valueOf(recrutements.size()));
        renderRoster();
        renderRecrutements(recrutements);
    }

    @FXML
    private void onApply() {
        if (membership != null && membership.getEquipeId() == equipe.getId()) {
            candidatureService.excludeMember(membership);
            membership = null;
            parentController.showTeams();
            return;
        }
        if (membership != null) {
            applyButton.setDisable(true);
            applyButton.setText("Deja dans une equipe");
            return;
        }
        parentController.showApply(equipe);
    }

    @FXML
    private void onBack() {
        parentController.showTeams();
    }

    @FXML
    private void onJoinDiscord() {
        String url = value(equipe.getDiscordInviteUrl());
        if (url.isBlank()) {
            discordButton.setText("Serveur Discord indisponible");
            discordButton.setDisable(true);
            return;
        }
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (Exception e) {
            discordButton.setText("Impossible d'ouvrir le lien Discord");
        }
    }

    private void renderRoster() {
        rosterContainer.getChildren().clear();
        rosterContainer.getChildren().add(UserViewFactory.createRosterRow(value(equipe.getManagerUsername()), "MANAGER", equipe.getDateCreation().format(DATE_FORMATTER)));
        for (Candidature candidature : candidatureService.getAcceptedMembersByEquipe(equipe.getId())) {
                rosterContainer.getChildren().add(UserViewFactory.createRosterRow(
                        value(candidature.getPseudoJoueur()),
                        value(candidature.getRolePrefere()).toUpperCase(),
                        candidature.getDateCandidature().format(DATE_FORMATTER)
                ));
        }
    }

    private void renderRecrutements(java.util.List<Recrutement> recrutements) {
        recrutementsContainer.getChildren().clear();
        if (recrutements.isEmpty()) {
            recrutementsContainer.getChildren().add(UserViewFactory.createMuted("Aucune offre publiee pour cette equipe."));
            return;
        }
        for (Recrutement recrutement : recrutements) {
            recrutementsContainer.getChildren().add(UserViewFactory.createRecruitmentCard(recrutement));
        }
    }

    private String value(String value) {
        return value == null ? "" : value;
    }

    private void updateActionButtons() {
        if (membership != null && membership.getEquipeId() == equipe.getId()) {
            applyButton.setText("Quitter l'equipe");
            applyButton.getStyleClass().remove("manager-action-button");
            if (!applyButton.getStyleClass().contains("manager-delete-button")) {
                applyButton.getStyleClass().add("manager-delete-button");
            }
            backButton.setText("Retour au dashboard");
            return;
        }
        if (membership != null) {
            applyButton.setText("Deja dans une equipe");
            applyButton.setDisable(true);
        }
    }

    private void updateDiscordButton() {
        boolean hasDiscord = !value(equipe.getDiscordInviteUrl()).isBlank();
        discordButton.setDisable(!hasDiscord);
        discordButton.setText(hasDiscord ? "Rejoindre serveur Discord" : "Serveur Discord indisponible");
    }
}
