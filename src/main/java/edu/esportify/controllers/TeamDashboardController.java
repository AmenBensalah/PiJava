package edu.esportify.controllers;

import edu.esportify.entities.Candidature;
import edu.esportify.entities.Equipe;
import edu.esportify.entities.Recrutement;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.CandidatureService;
import edu.esportify.services.EquipeService;
import edu.esportify.services.RecrutementService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TeamDashboardController implements ManagerContentController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final EquipeService equipeService = new EquipeService();
    private final RecrutementService recrutementService = new RecrutementService();
    private final CandidatureService candidatureService = new CandidatureService();

    private ManagerLayoutController parentController;
    private Equipe equipe;

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
    @FXML private Label discordLinkLabel;
    @FXML private VBox rosterContainer;
    @FXML private VBox recrutementsContainer;
    @FXML private VBox candidaturesContainer;

    @FXML
    private void initialize() {
        teamImageView.setImage(loadImage("/images/hero-team-reference.png"));
    }

    @Override
    public void init(ManagerLayoutController parentController) {
        this.parentController = parentController;
        equipe = equipeService.getByManagerUsername(AppSession.getInstance().getUsername());
        AppSession.getInstance().setSelectedEquipe(equipe);
        if (equipe == null) {
            parentController.showTeamCreate();
            return;
        }

        tagChipLabel.setText(safe(equipe.getTag()));
        teamNameLabel.setText(safe(equipe.getNomEquipe()));
        teamDescriptionLabel.setText(safe(equipe.getDescription()));
        regionChipLabel.setText(safe(equipe.getRegion()));
        classementChipLabel.setText(safe(equipe.getClassement()));
        visibiliteChipLabel.setText(equipe.isPrivate() ? "Privee" : "Publique");
        creationDateLabel.setText(equipe.getDateCreation().format(DATE_FORMATTER));
        regionStatLabel.setText(safe(equipe.getRegion()));
        discordLinkLabel.setText(equipe.getDiscordInviteUrl() == null || equipe.getDiscordInviteUrl().isBlank()
                ? "Lien non disponible."
                : equipe.getDiscordInviteUrl());

        if (equipe.getLogo() != null && !equipe.getLogo().isBlank()) {
            try {
                teamImageView.setImage(new Image(equipe.getLogo()));
            } catch (IllegalArgumentException ignored) {
                teamImageView.setImage(loadImage("/images/hero-team-reference.png"));
            }
        }

        renderRoster();
        renderRecrutements();
        renderCandidatures();
    }

    @FXML
    private void onEditTeam() {
        parentController.showTeamEdit();
    }

    @FXML
    private void onManageCandidates() {
        parentController.showCandidates();
    }

    @FXML
    private void onDeleteTeam() {
        if (equipe == null) {
            return;
        }
        for (Candidature candidature : candidatureService.getByEquipe(equipe.getId())) {
            candidatureService.deleteEntity(candidature);
        }
        for (Recrutement recrutement : recrutementService.getByEquipe(equipe.getId())) {
            recrutementService.deleteEntity(recrutement);
        }
        equipeService.deleteEntity(equipe);
        AppSession.getInstance().setSelectedEquipe(null);
        parentController.showTeamCreate();
    }

    @FXML
    private void onBackToTeams() {
        parentController.showTeamCreate();
    }

    private void renderRoster() {
        rosterContainer.getChildren().clear();
        int members = 1;
        rosterContainer.getChildren().add(createRosterRow(
                AppSession.getInstance().getUsername(),
                "MANAGER",
                equipe.getDateCreation().format(DATE_FORMATTER),
                null
        ));

        for (Candidature candidature : candidatureService.getAcceptedMembersByEquipe(equipe.getId())) {
            members++;
            rosterContainer.getChildren().add(createRosterRow(
                    safe(candidature.getPseudoJoueur()),
                    safe(candidature.getRolePrefere()).toUpperCase(),
                    candidature.getDateCandidature().format(DATE_FORMATTER),
                    () -> excludeMember(candidature)
            ));
        }
        membersCountLabel.setText(String.valueOf(members));
    }

    private void renderRecrutements() {
        List<Recrutement> recrutements = recrutementService.getByEquipe(equipe.getId());
        recruitmentCountLabel.setText(String.valueOf(recrutements.size()));
        recrutementsContainer.getChildren().clear();
        if (recrutements.isEmpty()) {
            recrutementsContainer.getChildren().add(createMutedLabel("Aucune offre publiee pour cette equipe."));
            return;
        }
        for (Recrutement recrutement : recrutements) {
            VBox card = new VBox(
                    createSectionLabel(recrutement.getNomRec()),
                    createMutedLabel(recrutement.getDescription()),
                    createChip(recrutement.getStatus())
            );
            card.getStyleClass().add("mini-card");
            recrutementsContainer.getChildren().add(card);
        }
    }

    private void renderCandidatures() {
        List<Candidature> candidatures = candidatureService.getByEquipe(equipe.getId());
        candidaturesContainer.getChildren().clear();
        if (candidatures.isEmpty()) {
            candidaturesContainer.getChildren().add(createMutedLabel("Aucune candidature recue pour cette equipe."));
            return;
        }
        for (Candidature candidature : candidatures) {
            HBox row = new HBox(
                    12,
                    createSectionLabel(safe(candidature.getPseudoJoueur())),
                    createChip(safe(candidature.getNiveau())),
                    createChip(safe(candidature.getStatut()))
            );
            row.getStyleClass().add("recruitment-row");
            candidaturesContainer.getChildren().add(row);
        }
    }

    private HBox createRosterRow(String joueur, String role, String date, Runnable onExclude) {
        Label joueurLabel = createSectionLabel(joueur);
        Label roleLabel = createChip(role);
        Label dateLabel = createSectionLabel(date);
        Button profilButton = new Button("Profil");
        profilButton.getStyleClass().add("ghost-button");
        HBox row;
        if (onExclude == null) {
            row = new HBox(18, joueurLabel, roleLabel, dateLabel, profilButton);
        } else {
            Button excludeButton = new Button("Exclure");
            excludeButton.getStyleClass().add("manager-delete-button");
            excludeButton.setOnAction(event -> onExclude.run());
            row = new HBox(18, joueurLabel, roleLabel, dateLabel, profilButton, excludeButton);
        }
        row.getStyleClass().add("recruitment-row");
        return row;
    }

    private void excludeMember(Candidature candidature) {
        candidatureService.excludeMember(candidature);
        renderRoster();
        renderCandidatures();
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("card-title");
        return label;
    }

    private Label createMutedLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-label");
        label.setWrapText(true);
        return label;
    }

    private Label createChip(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("chip-active");
        return label;
    }

    private Image loadImage(String path) {
        return new Image(String.valueOf(getClass().getResource(path)));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
