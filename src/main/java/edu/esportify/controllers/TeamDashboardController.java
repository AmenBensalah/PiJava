package edu.esportify.controllers;

import edu.esportify.entities.Candidature;
import edu.esportify.entities.Equipe;
import edu.esportify.entities.Recrutement;
import edu.esportify.entities.AiRoleInsight;
import edu.esportify.entities.Task;
import edu.esportify.entities.TeamComment;
import edu.esportify.entities.TeamAlert;
import edu.esportify.entities.TeamHistory;
import edu.esportify.entities.TeamMember;
import edu.esportify.entities.TeamPerformance;
import edu.esportify.entities.Workload;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.CandidatureService;
import edu.esportify.services.EquipeService;
import edu.esportify.services.RecrutementService;
import edu.esportify.services.TaskService;
import edu.esportify.services.TeamAlertService;
import edu.esportify.services.TeamAiAdvisorService;
import edu.esportify.services.TeamCommentService;
import edu.esportify.services.TeamHistoryService;
import edu.esportify.services.TeamPerformanceService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class TeamDashboardController implements ManagerContentController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String PRIMARY_BUTTON_STYLE = String.join("; ",
            "-fx-background-color: linear-gradient(to right, #56d6ff 0%, #74b9ff 30%, #9a7bff 68%, #ff4fd8 100%)",
            "-fx-text-fill: #06111d",
            "-fx-font-size: 18px",
            "-fx-font-weight: 800",
            "-fx-font-family: 'Segoe UI Semibold'",
            "-fx-padding: 14 28",
            "-fx-background-radius: 18",
            "-fx-border-radius: 18",
            "-fx-border-width: 1",
            "-fx-border-color: rgba(99, 224, 255, 0.26)",
            "-fx-cursor: hand"
    );
    private static final String SECONDARY_BUTTON_STYLE = String.join("; ",
            "-fx-background-color: linear-gradient(to bottom, rgba(12, 19, 34, 0.96), rgba(8, 14, 28, 0.96))",
            "-fx-text-fill: #eef5ff",
            "-fx-font-size: 17px",
            "-fx-font-weight: 800",
            "-fx-font-family: 'Segoe UI Semibold'",
            "-fx-padding: 14 24",
            "-fx-background-radius: 18",
            "-fx-border-radius: 18",
            "-fx-border-width: 1.2",
            "-fx-border-color: rgba(64, 212, 255, 0.62)",
            "-fx-cursor: hand"
    );
    private static final String DELETE_BUTTON_STYLE = String.join("; ",
            "-fx-background-color: linear-gradient(to bottom, rgba(44, 16, 29, 0.96), rgba(20, 8, 16, 0.96))",
            "-fx-text-fill: #ff91aa",
            "-fx-font-size: 17px",
            "-fx-font-weight: 800",
            "-fx-font-family: 'Segoe UI Semibold'",
            "-fx-padding: 14 24",
            "-fx-background-radius: 18",
            "-fx-border-radius: 18",
            "-fx-border-width: 1.2",
            "-fx-border-color: rgba(255, 102, 140, 0.40)",
            "-fx-cursor: hand"
    );
    private static final String LINK_BUTTON_STYLE = String.join("; ",
            "-fx-background-color: linear-gradient(to right, rgba(34, 202, 255, 0.18), rgba(88, 97, 255, 0.16))",
            "-fx-text-fill: #eef6ff",
            "-fx-font-size: 15px",
            "-fx-font-weight: 800",
            "-fx-font-family: 'Segoe UI Semibold'",
            "-fx-padding: 12 22",
            "-fx-background-radius: 18",
            "-fx-border-radius: 18",
            "-fx-border-width: 1.1",
            "-fx-border-color: rgba(83, 216, 255, 0.42)",
            "-fx-cursor: hand"
    );

    private final EquipeService equipeService = new EquipeService();
    private final RecrutementService recrutementService = new RecrutementService();
    private final CandidatureService candidatureService = new CandidatureService();
    private final TaskService taskService = new TaskService();
    private final TeamPerformanceService teamPerformanceService = new TeamPerformanceService();
    private final TeamAlertService teamAlertService = new TeamAlertService();
    private final TeamHistoryService teamHistoryService = new TeamHistoryService();
    private final TeamAiAdvisorService teamAiAdvisorService = new TeamAiAdvisorService();
    private final TeamCommentService teamCommentService = new TeamCommentService();

    private ManagerLayoutController parentController;
    private Equipe equipe;

    @FXML private ImageView teamImageView;
    @FXML private Label teamStatusBannerLabel;
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
    @FXML private Button editTeamButton;
    @FXML private Button manageCandidatesButton;
    @FXML private Button deleteTeamButton;
    @FXML private Button backToHubButton;
    @FXML private Button editDiscordButton;
    @FXML private Button analyseIaButton;
    @FXML private VBox activeMembersContainer;
    @FXML private VBox recrutementsContainer;
    @FXML private VBox candidaturesContainer;
    @FXML private VBox aiRolesContainer;
    @FXML private ScrollPane dashboardScrollPane;
    @FXML private ScrollPane discussionScrollPane;
    @FXML private VBox discussionMessagesContainer;
    @FXML private TextArea discussionInputArea;
    @FXML private Label discussionInfoLabel;
    @FXML private Button sendDiscussionButton;
    @FXML private VBox chatPanel;
    @FXML private Button chatToggleButton;
    @FXML private Label candidatureAiInfoLabel;

    private boolean aiCandidateRankingEnabled;
    private boolean chatOpen;

    @FXML
    private void initialize() {
        teamImageView.setImage(loadImage("/images/hero-team-reference.png"));
        applyButtonStyles();
        setChatOpen(false);
        Platform.runLater(() -> {
            if (dashboardScrollPane != null) {
                dashboardScrollPane.setVvalue(0);
            }
        });
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

        updateBanState();

        tagChipLabel.setText(safe(equipe.getTag()));
        teamNameLabel.setText(safe(equipe.getNomEquipe()));
        teamDescriptionLabel.setText(safe(equipe.getDescription()));
        regionChipLabel.setText(safe(equipe.getRegion()));
        classementChipLabel.setText(safe(equipe.getClassement()));
        visibiliteChipLabel.setText(equipe.isPrivate() ? "Privee" : "Publique");
        creationDateLabel.setText(formatDate(equipe.getDateCreation()));
        regionStatLabel.setText(safe(equipe.getRegion()));
        discordLinkLabel.setText(equipe.getDiscordInviteUrl() == null || equipe.getDiscordInviteUrl().isBlank()
                ? "Ajoutez le lien dans \"Modifier\"."
                : equipe.getDiscordInviteUrl());

        if (equipe.getLogo() != null && !equipe.getLogo().isBlank()) {
            try {
                teamImageView.setImage(new Image(equipe.getLogo()));
            } catch (IllegalArgumentException ignored) {
                teamImageView.setImage(loadImage("/images/hero-team-reference.png"));
            }
        }

        runSafeSection("membres actifs", this::renderActiveMembers);
        runSafeSection("recrutements", this::renderRecrutements);
        runSafeSection("candidatures", this::renderCandidatures);
        runSafeSection("statistiques ia", this::renderAiRoles);
        runSafeSection("discussion", this::renderDiscussion);
    }

    @FXML
    private void onEditTeam() {
        parentController.showTeamEdit();
    }

    @FXML
    private void onEditDiscordLink() {
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
        teamCommentService.deleteByTeam(equipe.getId());
        equipeService.deleteEntity(equipe);
        AppSession.getInstance().setSelectedEquipe(null);
        parentController.showTeamCreate();
    }

    @FXML
    private void onSendDiscussionMessage() {
        if (equipe == null || equipe.isCurrentlyBanned()) {
            if (discussionInfoLabel != null && equipe != null && equipe.isCurrentlyBanned()) {
                discussionInfoLabel.setText(buildBanMessage(equipe));
            }
            return;
        }
        String content = safe(discussionInputArea.getText()).trim();
        if (content.isBlank()) {
            discussionInfoLabel.setText("Le message ne peut pas etre vide.");
            return;
        }
        if (content.length() < 3) {
            discussionInfoLabel.setText("Le message doit contenir au moins 3 caracteres.");
            return;
        }
        teamCommentService.addMessage(equipe.getId(), AppSession.getInstance().getUsername(), content);
        discussionInputArea.clear();
        discussionInfoLabel.setText("Message envoye au groupe.");
        renderDiscussion();
    }

    @FXML
    private void onBackToTeams() {
        parentController.showTeamCreate();
    }

    @FXML
    private void onAnalyseCandidaturesIa() {
        aiCandidateRankingEnabled = true;
        renderCandidatures();
    }

    @FXML
    private void onToggleChat() {
        setChatOpen(!chatOpen);
    }

    @FXML
    private void onCloseChat() {
        setChatOpen(false);
    }

    private void renderRecrutements() {
        if (equipe.isCurrentlyBanned()) {
            recruitmentCountLabel.setText(String.valueOf(recrutementService.getByEquipe(equipe.getId()).size()));
            recrutementsContainer.getChildren().setAll(createMutedLabel(buildBanMessage(equipe)));
            return;
        }
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
        if (equipe.isCurrentlyBanned()) {
            if (candidatureAiInfoLabel != null) {
                candidatureAiInfoLabel.setText(buildBanMessage(equipe));
            }
            candidaturesContainer.getChildren().setAll(createMutedLabel("Les candidatures sont bloquees pendant le bannissement."));
            return;
        }
        List<Candidature> candidatures = candidatureService.getByEquipe(equipe.getId());
        candidaturesContainer.getChildren().clear();
        if (candidatures.isEmpty()) {
            if (candidatureAiInfoLabel != null) {
                candidatureAiInfoLabel.setText("Aucune candidature a analyser.");
            }
            candidaturesContainer.getChildren().add(createMutedLabel("Aucune candidature recue pour cette equipe."));
            return;
        }

        List<AnalyzedCandidate> analyzedCandidates = candidatures.stream()
                .map(candidature -> new AnalyzedCandidate(candidature, computeCandidateScore(candidature), buildCandidateReason(candidature)))
                .collect(Collectors.toList());

        if (aiCandidateRankingEnabled) {
            analyzedCandidates.sort(Comparator.comparingDouble(AnalyzedCandidate::score).reversed());
            AnalyzedCandidate best = analyzedCandidates.get(0);
            if (candidatureAiInfoLabel != null) {
                candidatureAiInfoLabel.setText("Classement IA actif. Top candidat: "
                        + safe(best.candidature().getPseudoJoueur())
                        + " - " + String.format("%.0f%%", best.score()));
            }
        } else if (candidatureAiInfoLabel != null) {
            candidatureAiInfoLabel.setText("Cliquez sur Analyse IA pour classer automatiquement les candidats.");
        }

        for (AnalyzedCandidate analyzedCandidate : analyzedCandidates) {
            candidaturesContainer.getChildren().add(createCandidateAnalysisCard(analyzedCandidate));
        }
    }

    private VBox createCandidateAnalysisCard(AnalyzedCandidate analyzedCandidate) {
        Candidature candidature = analyzedCandidate.candidature();
        Label nameLabel = createSectionLabel(safe(candidature.getPseudoJoueur()));
        Label scoreLabel = new Label(String.format("%.0f%%", analyzedCandidate.score()));
        scoreLabel.getStyleClass().add("team-dashboard-score-badge");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(12,
                nameLabel,
                createChip(safe(candidature.getNiveau())),
                createChip(safe(candidature.getDisponibilite())),
                spacer,
                scoreLabel);

        Label metaLabel = createMutedLabel(
                "Region: " + safe(candidature.getRegion())
                        + " | Disponibilite: " + safe(candidature.getDisponibilite())
                        + " | Statut: " + safe(candidature.getStatut())
        );
        Label motivationLabel = createMutedLabel("Motivation: " + safe(candidature.getMotivation()));
        Label reasonLabel = createRecommendationLabel("Analyse IA: " + analyzedCandidate.reason());

        VBox card = new VBox(10, header, metaLabel, motivationLabel, reasonLabel);
        card.getStyleClass().addAll("recruitment-row", "team-dashboard-candidate-analysis-card");
        return card;
    }

    private VBox createInfoCard(String title, String lineOne, String lineTwo, Label pill) {
        Label titleLabel = createSectionLabel(title);
        titleLabel.getStyleClass().add("team-dashboard-info-title");

        Label lineOneLabel = createMutedLabel(lineOne);
        lineOneLabel.getStyleClass().add("team-dashboard-info-text");

        Label lineTwoLabel = createMutedLabel(lineTwo);
        lineTwoLabel.getStyleClass().add("team-dashboard-info-text");

        VBox card = new VBox(12, titleLabel, lineOneLabel, lineTwoLabel, pill);
        card.getStyleClass().addAll("mini-card", "team-dashboard-info-card");
        return card;
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
        renderActiveMembers();
        renderCandidatures();
    }

    private void renderActiveMembers() {
        activeMembersContainer.getChildren().clear();
        for (TeamMember member : teamPerformanceService.getActiveMembers(equipe.getId(), AppSession.getInstance().getUsername())) {
            Label usernameLabel = createSectionLabel(member.getUsername());
            usernameLabel.getStyleClass().add("team-dashboard-member-name");

            HBox badges = new HBox(
                    10,
                    createChip(safe(member.getRole()).toUpperCase()),
                    createStatusChip(member.isActive() ? "ACTIF" : "INACTIF", member.isActive())
            );
            badges.getStyleClass().add("team-dashboard-member-badges");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(16, usernameLabel, spacer, badges);
            row.getStyleClass().addAll("recruitment-row", "team-dashboard-info-card", "team-dashboard-member-card");
            activeMembersContainer.getChildren().add(row);
        }
    }

    private void renderAiRoles() {
        aiRolesContainer.getChildren().clear();
        List<AiRoleInsight> insights = teamAiAdvisorService.buildInsights(equipe, AppSession.getInstance().getUsername());
        List<Candidature> accepted = candidatureService.getAcceptedMembersByEquipe(equipe.getId());
        List<Candidature> allCandidatures = candidatureService.getByEquipe(equipe.getId());
        TeamPerformance performance = teamPerformanceService.calculatePerformance(equipe.getId());
        List<Recrutement> recrutements = recrutementService.getByEquipe(equipe.getId());

        if (insights.isEmpty() && accepted.isEmpty() && allCandidatures.isEmpty()) {
            aiRolesContainer.getChildren().add(createMutedLabel("Aucune analyse IA disponible."));
            return;
        }

        aiRolesContainer.getChildren().add(createAiOverviewCard(accepted, allCandidatures, performance));
        aiRolesContainer.getChildren().add(createAiDistributionRow(accepted, allCandidatures));
        aiRolesContainer.getChildren().add(createAiPerformanceCard(accepted, allCandidatures));
        aiRolesContainer.getChildren().add(createAiInsightGrid(accepted, allCandidatures, performance, recrutements, insights));
    }

    private VBox createAiOverviewCard(List<Candidature> accepted, List<Candidature> allCandidatures, TeamPerformance performance) {
        VBox card = createAiShellCard("VUE D'ENSEMBLE DE L'ÉQUIPE");
        card.getStyleClass().add("team-dashboard-ai-overview-card");

        card.getChildren().add(createAiMetricRow("Équilibre", computeRoleBalance(accepted), "team-dashboard-metric-cyan"));
        card.getChildren().add(createAiMetricRow("Niveau moyen", computeAverageLevelScore(accepted), "team-dashboard-metric-blue"));
        card.getChildren().add(createAiMetricRow("Activité", performance.getProductivityScore(), "team-dashboard-metric-violet"));
        card.getChildren().add(createAiMetricRow("Recrutement", computeAcceptanceRate(allCandidatures), "team-dashboard-metric-pink"));
        card.getChildren().add(createAiMetricRow("Effectif", computeRosterFillRate(accepted), "team-dashboard-metric-green"));
        return card;
    }

    private HBox createAiDistributionRow(List<Candidature> accepted, List<Candidature> allCandidatures) {
        HBox row = new HBox(18);
        row.getChildren().add(createAiLevelsCard(accepted));
        row.getChildren().add(createAiAvailabilityCard(allCandidatures));
        HBox.setHgrow(row.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        return row;
    }

    private VBox createAiLevelsCard(List<Candidature> accepted) {
        VBox card = createAiShellCard("RÉPARTITION DES NIVEAUX");
        Map<String, Long> levels = new LinkedHashMap<>();
        levels.put("Débutant", countByLevel(accepted, "debut", "bronze", "silver", "fer"));
        levels.put("Intermédiaire", countByLevel(accepted, "gold", "plat", "inter"));
        levels.put("Confirmé", countByLevel(accepted, "diamond", "emerald", "confirm"));
        levels.put("Expert", countByLevel(accepted, "master", "grandmaster", "challenger", "expert", "radiant", "immortal"));
        levels.forEach((label, count) -> card.getChildren().add(createAiDistributionBar(label, count.intValue(), Math.max(1, accepted.size()), "team-dashboard-level-bar")));
        return card;
    }

    private VBox createAiAvailabilityCard(List<Candidature> candidatures) {
        VBox card = createAiShellCard("RÉPARTITION DES DISPONIBILITÉS");
        Map<String, Long> availability = new LinkedHashMap<>();
        availability.put("SOIR", countByAvailability(candidatures, "soir"));
        availability.put("TOUTE LA JOURNEE", countByAvailability(candidatures, "toute la journee"));
        availability.put("JOURS WEEKEND", countByAvailability(candidatures, "weekend"));
        availability.forEach((label, count) -> card.getChildren().add(createAiDistributionBar(label, count.intValue(), Math.max(1, candidatures.size()), "team-dashboard-role-bar")));
        return card;
    }

    private VBox createAiPerformanceCard(List<Candidature> accepted, List<Candidature> allCandidatures) {
        VBox card = createAiShellCard("PERFORMANCE (30 JOURS)");
        card.getChildren().add(createAiProgressLine("Acceptés", accepted.size(), Math.max(1, allCandidatures.size()), "team-dashboard-accepted-line"));
        card.getChildren().add(createAiProgressLine("Candidatures", allCandidatures.size(), Math.max(1, equipe.getMaxMembers()), "team-dashboard-candidature-line"));
        return card;
    }

    private HBox createAiInsightGrid(List<Candidature> accepted, List<Candidature> allCandidatures,
                                     TeamPerformance performance, List<Recrutement> recrutements, List<AiRoleInsight> insights) {
        VBox left = new VBox(18);
        VBox right = new VBox(18);

        left.getChildren().add(createInsightSummaryCard(accepted, performance, recrutements));
        left.getChildren().add(createStrengthWeaknessCard(insights));
        right.getChildren().add(createRankCard(accepted, allCandidatures, performance));
        right.getChildren().add(createRoleDetailCard(accepted, allCandidatures));

        HBox row = new HBox(18, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);
        return row;
    }

    private VBox createInsightSummaryCard(List<Candidature> accepted, TeamPerformance performance, List<Recrutement> recrutements) {
        VBox card = createAiShellCard("ÉQUILIBRE DE L'EFFECTIF");
        card.getChildren().add(createBigBadge(computeRoleBalance(accepted) >= 70 ? "ÉQUILIBRÉE" : "À RENFORCER", computeRoleBalance(accepted) >= 70 ? "team-dashboard-good-badge" : "team-dashboard-warn-badge"));
        card.getChildren().add(createMutedLabel("Niveau dominant"));
        card.getChildren().add(createSectionLabel(findDominantLevel(accepted)));
        card.getChildren().add(createMutedLabel("Type de jeu"));
        card.getChildren().add(createBigBadge(recrutements.isEmpty() ? "GENERIC" : "RECRUTEMENT ACTIF", "team-dashboard-neutral-badge"));
        card.getChildren().add(createMutedLabel("Score d'équilibre"));
        card.getChildren().add(createBigBadge(String.format("%.0f/100", computeRoleBalance(accepted)), "team-dashboard-pink-badge"));
        card.getChildren().add(createMutedLabel("Tendance recrutements"));
        card.getChildren().add(createBigBadge(String.format("%.0f%%", computeAcceptanceRate(recrutements.size(), Math.max(1, recrutements.size() + 1))), "team-dashboard-neutral-badge"));
        return card;
    }

    private VBox createRankCard(List<Candidature> accepted, List<Candidature> allCandidatures, TeamPerformance performance) {
        VBox card = createAiShellCard("JUSTIFICATION RANK IA");
        double confidence = Math.min(95, 35 + (computeAverageLevelScore(accepted) * 0.4) + (performance.getProductivityScore() * 0.25));
        card.getChildren().add(createMutedLabel("Confiance IA: " + String.format("%.1f%%", confidence) + "   Source: php_fallback"));
        card.getChildren().add(createRecommendationLabel("• Équilibre d'équipe: " + String.format("%.0f/100", computeRoleBalance(accepted))));
        card.getChildren().add(createRecommendationLabel("• Niveau moyen: " + String.format("%.1f/4", computeAverageLevelScore(accepted) / 25.0)));
        card.getChildren().add(createRecommendationLabel("• Taux d'acceptation récent: " + String.format("%.0f%%", computeAcceptanceRate(allCandidatures))));
        card.getChildren().add(createRecommendationLabel("• Activité récente: " + activityLabel(performance.getProductivityScore())));
        card.getChildren().add(createBigBadge(computePredictedRank(accepted, performance), "team-dashboard-rank-badge"));
        return card;
    }

    private VBox createRoleDetailCard(List<Candidature> accepted, List<Candidature> allCandidatures) {
        VBox card = createAiShellCard("NIVEAU MOYEN & RÉPARTITION");
        card.getChildren().add(createMutedLabel("Niveau moyen : " + (accepted.isEmpty() ? "N/A" : String.format("%.1f/4", computeAverageLevelScore(accepted) / 25.0))));
        card.getChildren().add(createAiDistributionBar("DÉBUTANT", (int) countByLevel(accepted, "debut", "bronze", "silver", "fer"), Math.max(1, accepted.size()), "team-dashboard-level-bar"));
        card.getChildren().add(createAiDistributionBar("INTERMÉDIAIRE", (int) countByLevel(accepted, "gold", "plat", "inter"), Math.max(1, accepted.size()), "team-dashboard-level-bar"));
        card.getChildren().add(createAiDistributionBar("CONFIRMÉ", (int) countByLevel(accepted, "diamond", "emerald", "confirm"), Math.max(1, accepted.size()), "team-dashboard-level-bar"));
        card.getChildren().add(createAiDistributionBar("EXPERT", (int) countByLevel(accepted, "master", "grandmaster", "challenger", "expert", "radiant", "immortal"), Math.max(1, accepted.size()), "team-dashboard-level-bar"));
        card.getChildren().add(createMutedLabel("Candidatures totales : " + allCandidatures.size()));
        return card;
    }

    private VBox createStrengthWeaknessCard(List<AiRoleInsight> insights) {
        VBox card = createAiShellCard("POINTS FORTS & FAIBLESSES");
        if (insights.isEmpty()) {
            card.getChildren().add(createMutedLabel("Aucun point fort détecté."));
            return card;
        }
        card.getChildren().add(createSectionLabel("POINTS FORTS"));
        card.getChildren().add(createMutedLabel(insights.get(0).getSummary()));
        card.getChildren().add(createSectionLabel("FAIBLESSES"));
        card.getChildren().add(createMutedLabel(insights.get(insights.size() - 1).getSummary()));
        card.getChildren().add(createBigBadge(insights.size() > 2 ? "ÉQUIPE ACTIVE" : "ÉQUIPE INACTIVE", insights.size() > 2 ? "team-dashboard-good-badge" : "team-dashboard-warn-badge"));
        return card;
    }

    private VBox createAiShellCard(String title) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("coord-card", "team-dashboard-card", "team-dashboard-graph-card");
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().addAll("field-label", "team-dashboard-graph-title");
        card.getChildren().add(titleLabel);
        return card;
    }

    private VBox createAiMetricRow(String label, double value, String colorStyle) {
        VBox box = new VBox(6);
        HBox header = new HBox();
        Label title = createMutedLabel(label);
        Label val = createSectionLabel(String.format("%.0f", value));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, val);
        VBox bar = new VBox();
        bar.getStyleClass().addAll("team-dashboard-progress-track");
        Region fill = new Region();
        fill.setPrefWidth(Math.max(18, value * 4));
        fill.getStyleClass().addAll("team-dashboard-progress-fill", colorStyle);
        bar.getChildren().add(fill);
        box.getChildren().addAll(header, bar);
        return box;
    }

    private HBox createAiDistributionBar(String label, int value, int total, String styleClass) {
        Label left = new Label(label);
        left.getStyleClass().addAll("field-label", "team-dashboard-distribution-label");
        Region fill = new Region();
        fill.getStyleClass().addAll("team-dashboard-progress-fill", styleClass);
        fill.setPrefWidth(Math.max(8, (value * 260.0) / Math.max(1, total)));
        VBox track = new VBox(fill);
        track.getStyleClass().add("team-dashboard-progress-track");
        Label right = createSectionLabel(String.valueOf(value));
        Region spacer = new Region();
        HBox.setHgrow(track, Priority.ALWAYS);
        HBox.setHgrow(spacer, Priority.NEVER);
        HBox row = new HBox(12, left, track, right);
        row.setFillHeight(false);
        return row;
    }

    private VBox createAiProgressLine(String label, int current, int max, String styleClass) {
        VBox box = new VBox(8);
        box.getChildren().add(createSectionLabel(label + " : " + current));
        Region fill = new Region();
        fill.getStyleClass().addAll("team-dashboard-progress-fill", styleClass);
        fill.setPrefWidth(Math.max(12, (current * 420.0) / Math.max(1, max)));
        VBox track = new VBox(fill);
        track.getStyleClass().add("team-dashboard-line-track");
        box.getChildren().add(track);
        return box;
    }

    private Label createBigBadge(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("team-dashboard-big-badge", styleClass);
        return label;
    }

    private void renderDiscussion() {
        discussionMessagesContainer.getChildren().clear();
        if (equipe.isCurrentlyBanned()) {
            discussionMessagesContainer.getChildren().add(createMutedLabel(buildBanMessage(equipe)));
            discussionInputArea.setManaged(false);
            discussionInputArea.setVisible(false);
            sendDiscussionButton.setManaged(false);
            sendDiscussionButton.setVisible(false);
            discussionInfoLabel.setText(buildBanMessage(equipe));
            return;
        }
        List<TeamComment> comments = teamCommentService.getByTeam(equipe.getId());
        if (comments.isEmpty()) {
            discussionMessagesContainer.getChildren().add(createMutedLabel("Aucun message pour le moment."));
        } else {
            for (TeamComment comment : comments) {
                if (shouldHideDiscussionMessage(comment)) {
                    continue;
                }
                discussionMessagesContainer.getChildren().add(createDiscussionCard(comment));
            }
        }
        if (discussionMessagesContainer.getChildren().isEmpty()) {
            discussionMessagesContainer.getChildren().add(createMutedLabel("Aucun message pour le moment."));
        }
        discussionInfoLabel.setText("Discussion reservee aux membres de l'equipe.");
        if (sendDiscussionButton != null) {
            sendDiscussionButton.setDisable(false);
        }
        scrollDiscussionToBottom();
    }

    private void runSafeSection(String sectionName, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            System.out.println("Erreur dashboard section " + sectionName + ": " + e.getMessage());
            e.printStackTrace();
            applySectionFallback(sectionName);
        }
    }

    private void applySectionFallback(String sectionName) {
        switch (sectionName) {
            case "membres actifs" -> {
                if (activeMembersContainer != null) {
                    activeMembersContainer.getChildren().setAll(createMutedLabel("Section membres indisponible."));
                }
            }
            case "recrutements" -> {
                if (recrutementsContainer != null) {
                    recrutementsContainer.getChildren().setAll(createMutedLabel("Section recrutements indisponible."));
                }
            }
            case "candidatures" -> {
                if (candidaturesContainer != null) {
                    candidaturesContainer.getChildren().setAll(createMutedLabel("Section candidatures indisponible."));
                }
                if (candidatureAiInfoLabel != null) {
                    candidatureAiInfoLabel.setText("Analyse IA indisponible pour le moment.");
                }
            }
            case "statistiques ia" -> {
                if (aiRolesContainer != null) {
                    aiRolesContainer.getChildren().setAll(createMutedLabel("Analyse IA indisponible."));
                }
            }
            case "discussion" -> {
                if (discussionMessagesContainer != null) {
                    discussionMessagesContainer.getChildren().setAll(createMutedLabel("Discussion indisponible."));
                }
                if (discussionInfoLabel != null) {
                    discussionInfoLabel.setText("Le chat est temporairement indisponible.");
                }
                if (sendDiscussionButton != null) {
                    sendDiscussionButton.setDisable(true);
                }
            }
            default -> System.out.println("Aucun fallback specifique pour la section: " + sectionName);
        }
    }

    private void setChatOpen(boolean open) {
        chatOpen = open;
        if (chatPanel != null) {
            chatPanel.setVisible(open);
            chatPanel.setManaged(open);
        }
        if (chatToggleButton != null) {
            chatToggleButton.setText(open ? "✕" : "💬");
            chatToggleButton.getStyleClass().removeAll("team-chat-fab-open", "team-chat-fab-closed");
            chatToggleButton.getStyleClass().add(open ? "team-chat-fab-open" : "team-chat-fab-closed");
        }
        if (open) {
            scrollDiscussionToBottom();
        }
    }

    private void applyButtonStyles() {
        applyStyle(analyseIaButton, PRIMARY_BUTTON_STYLE);
        applyStyle(editDiscordButton, LINK_BUTTON_STYLE);
    }

    private void updateBanState() {
        if (teamStatusBannerLabel != null) {
            teamStatusBannerLabel.setText(equipe.isCurrentlyBanned()
                    ? buildBanMessage(equipe)
                    : "Equipe creee et selectionnee comme votre equipe.");
        }
        boolean banned = equipe.isCurrentlyBanned();
        if (editTeamButton != null) {
            editTeamButton.setDisable(banned);
        }
        if (manageCandidatesButton != null) {
            manageCandidatesButton.setDisable(banned);
        }
        if (editDiscordButton != null) {
            editDiscordButton.setDisable(banned);
        }
        if (analyseIaButton != null) {
            analyseIaButton.setDisable(banned);
        }
        if (sendDiscussionButton != null) {
            sendDiscussionButton.setDisable(banned);
        }
    }

    private String buildBanMessage(Equipe equipe) {
        long days = Math.max(1, ChronoUnit.DAYS.between(LocalDateTime.now(), equipe.getBannedUntil()));
        return "Votre equipe a ete bannie pendant " + formatBanDuration(days) + ".";
    }

    private String formatBanDuration(long days) {
        if (days % 7 == 0) {
            long weeks = days / 7;
            return weeks + " " + (weeks > 1 ? "semaines" : "semaine");
        }
        return days + " " + (days > 1 ? "jours" : "jour");
    }

    private void applyStyle(Button button, String style) {
        if (button != null) {
            button.setStyle(style);
        }
    }

    private void scrollDiscussionToBottom() {
        Platform.runLater(() -> {
            if (discussionScrollPane != null) {
                discussionScrollPane.applyCss();
                discussionScrollPane.layout();
                discussionScrollPane.setVvalue(1.0);
            }
        });
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

    private Label createStatusChip(String text, boolean positive) {
        return createPill(text, positive ? "team-dashboard-good-pill" : "team-dashboard-warn-pill");
    }

    private Label createPill(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("team-dashboard-pill", styleClass);
        return label;
    }

    private Label createPriorityChip(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("small-tag");
        return label;
    }

    private Label createRecommendationLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-label");
        label.getStyleClass().add("ai-recommendation");
        label.setWrapText(true);
        return label;
    }

    private VBox createDiscussionCard(TeamComment comment) {
        Label authorLabel = UserViewFactory.createSection(safe(comment.getAuthorUsername()));
        Label dateLabel = UserViewFactory.createMuted(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        Label contentLabel = UserViewFactory.createMuted(safe(comment.getContent()));
        HBox header = new HBox(12, authorLabel, UserViewFactory.createChip("Message"), dateLabel);

        VBox card = new VBox(8, header, contentLabel);
        card.getStyleClass().addAll("mini-card", "team-message-card", "team-chat-user-card");
        if (safe(comment.getAuthorUsername()).equalsIgnoreCase(AppSession.getInstance().getUsername())) {
            card.getStyleClass().add("team-message-card-own");
        }
        return card;
    }

    private boolean shouldHideDiscussionMessage(TeamComment comment) {
        String author = safe(comment.getAuthorUsername()).trim().toLowerCase();
        String content = safe(comment.getContent()).trim().toLowerCase();
        return ("system".equals(author)
                && content.contains("salon d'equipe initialise"))
                || ("coach.bot".equals(author)
                && content.contains("pensez a partager ici vos disponibilites"));
    }

    private Image loadImage(String path) {
        return new Image(String.valueOf(getClass().getResource(path)));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String formatDate(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_FORMATTER);
    }

    private String formatDateValue(LocalDateTime value) {
        return formatDate(value);
    }

    private String formatPercent(double value) {
        return String.format("%.0f%%", value);
    }

    private double computeAcceptanceRate(List<Candidature> candidatures) {
        if (candidatures.isEmpty()) {
            return 0;
        }
        long accepted = candidatures.stream().filter(item -> "acceptee".equalsIgnoreCase(safe(item.getStatut())) || "accepté".equalsIgnoreCase(safe(item.getStatut())) || "acceptée".equalsIgnoreCase(safe(item.getStatut()))).count();
        return (accepted * 100.0) / candidatures.size();
    }

    private double computeAcceptanceRate(int accepted, int total) {
        if (total <= 0) {
            return 0;
        }
        return (accepted * 100.0) / total;
    }

    private double computeRosterFillRate(List<Candidature> accepted) {
        return ((accepted.size() + 1) * 100.0) / Math.max(1, equipe.getMaxMembers());
    }

    private double computeRoleBalance(List<Candidature> accepted) {
        long activeRanks = FormOptions.RANKS.stream()
                .filter(rank -> accepted.stream().anyMatch(item -> rank.equalsIgnoreCase(safe(item.getNiveau()))))
                .count();
        return Math.min(100.0, activeRanks * 12.5);
    }

    private long countByAvailability(List<Candidature> candidatures, String keyword) {
        return candidatures.stream().filter(item -> safe(item.getDisponibilite()).toLowerCase().contains(keyword)).count();
    }

    private long countByLevel(List<Candidature> accepted, String... keywords) {
        return accepted.stream().filter(item -> {
            String level = safe(item.getNiveau()).toLowerCase();
            for (String keyword : keywords) {
                if (level.contains(keyword)) {
                    return true;
                }
            }
            return false;
        }).count();
    }

    private double computeAverageLevelScore(List<Candidature> accepted) {
        if (accepted.isEmpty()) {
            return 0;
        }
        return accepted.stream().collect(Collectors.averagingInt(item -> mapLevelScore(item.getNiveau())));
    }

    private int mapLevelScore(String level) {
        String normalized = safe(level).toLowerCase();
        if (normalized.contains("radiant") || normalized.contains("challenger") || normalized.contains("immortal")) {
            return 100;
        }
        if (normalized.contains("grandmaster")) {
            return 100;
        }
        if (normalized.contains("legend")) {
            return 92;
        }
        if (normalized.contains("heroique")) {
            return 84;
        }
        if (normalized.contains("diamenet")) {
            return 76;
        }
        if (normalized.contains("master") || normalized.contains("expert")) {
            return 80;
        }
        if (normalized.contains("diamond") || normalized.contains("confirm")) {
            return 60;
        }
        if (normalized.contains("silver")) {
            return 32;
        }
        if (normalized.contains("amateur")) {
            return 26;
        }
        if (normalized.contains("gold") || normalized.contains("plat") || normalized.contains("inter")) {
            return 40;
        }
        return 20;
    }

    private String findDominantLevel(List<Candidature> accepted) {
        return FormOptions.RANKS.stream()
                .max(Comparator.comparingLong(rank -> accepted.stream()
                        .filter(item -> rank.equalsIgnoreCase(safe(item.getNiveau())))
                        .count()))
                .orElse("N/A");
    }

    private String computePredictedRank(List<Candidature> accepted, TeamPerformance performance) {
        double score = (computeAverageLevelScore(accepted) * 0.55) + (performance.getProductivityScore() * 0.45);
        if (score >= 85) {
            return "RANK IA PRÉDIT : DIAMANT";
        }
        if (score >= 65) {
            return "RANK IA PRÉDIT : OR";
        }
        if (score >= 40) {
            return "RANK IA PRÉDIT : ARGENT";
        }
        return "RANK IA PRÉDIT : BRONZE";
    }

    private String activityLabel(double productivity) {
        if (productivity >= 70) {
            return "forte";
        }
        if (productivity >= 40) {
            return "modérée";
        }
        return "faible";
    }

    private String extractInitial(String value) {
        String normalized = safe(value).trim();
        return normalized.isBlank() ? "?" : normalized.substring(0, 1).toUpperCase();
    }

    private String formatRelativeTime(java.time.LocalDateTime createdAt) {
        long minutes = Math.max(0, ChronoUnit.MINUTES.between(createdAt, java.time.LocalDateTime.now()));
        if (minutes < 1) {
            return "A l'instant";
        }
        if (minutes < 60) {
            return "Il y a " + minutes + " min";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return "Il y a " + hours + " h";
        }
        long days = hours / 24;
        return "Il y a " + days + " j";
    }

    private double computeCandidateScore(Candidature candidature) {
        double score = 25;
        score += computeRegionScore(candidature) * 0.30;
        score += mapLevelScore(candidature.getNiveau()) * 0.40;
        score += computeAvailabilityScore(candidature) * 0.20;
        score += computeMotivationScore(candidature) * 0.10;
        return Math.max(0, Math.min(100, score));
    }

    private double computeRegionScore(Candidature candidature) {
        String teamRegion = normalizeText(equipe.getRegion());
        String candidateRegion = normalizeText(candidature.getRegion());
        if (!teamRegion.isBlank() && teamRegion.equals(candidateRegion)) {
            return 100;
        }
        if (!teamRegion.isBlank() && !candidateRegion.isBlank()
                && (teamRegion.contains(candidateRegion) || candidateRegion.contains(teamRegion))) {
            return 80;
        }
        return candidateRegion.isBlank() ? 35 : 55;
    }

    private double computeAvailabilityScore(Candidature candidature) {
        String disponibility = normalizeText(candidature.getDisponibilite());
        if (disponibility.contains("soir") || disponibility.contains("week")) {
            return 100;
        }
        if (disponibility.contains("toute") || disponibility.contains("journee")) {
            return 90;
        }
        if (disponibility.isBlank()) {
            return 35;
        }
        return 65;
    }

    private double computeMotivationScore(Candidature candidature) {
        String motivation = safe(candidature.getMotivation()).trim();
        if (motivation.isBlank()) {
            return 25;
        }
        String normalized = normalizeText(motivation);
        if (isLowQualityMotivation(normalized)) {
            return 10;
        }

        int wordCount = countWords(normalized);
        int uniqueWordCount = countUniqueWords(normalized);
        int lengthScore = Math.min(35, motivation.length() / 8);
        int structureScore = Math.min(25, wordCount * 3);
        int vocabularyScore = wordCount == 0 ? 0 : Math.min(20, (uniqueWordCount * 20) / wordCount);
        int keywordBonus = 0;
        for (String keyword : List.of("team", "equipe", "motivation", "objectif", "progress", "scrim", "tournoi", "serieux")) {
            if (normalized.contains(keyword)) {
                keywordBonus += 5;
            }
        }
        return Math.max(10, Math.min(100, lengthScore + structureScore + vocabularyScore + keywordBonus));
    }

    private String buildCandidateReason(Candidature candidature) {
        List<String> reasons = new java.util.ArrayList<>();
        if (computeRegionScore(candidature) >= 80) {
            reasons.add("region compatible");
        }
        if (mapLevelScore(candidature.getNiveau()) >= 80) {
            reasons.add("niveau eleve");
        } else if (mapLevelScore(candidature.getNiveau()) >= 60) {
            reasons.add("niveau solide");
        }
        if (computeAvailabilityScore(candidature) >= 90) {
            reasons.add("bonne disponibilite");
        }
        if (computeMotivationScore(candidature) >= 70) {
            reasons.add("lettre de motivation convaincante");
        }
        if (reasons.isEmpty()) {
            reasons.add("profil a verifier manuellement");
        }
        return String.join(", ", reasons) + ".";
    }

    private String normalizeText(String value) {
        return safe(value).toLowerCase().trim();
    }

    private boolean isLowQualityMotivation(String motivation) {
        String compact = motivation.replaceAll("\\s+", "");
        if (compact.length() < 12) {
            return true;
        }
        if (compact.matches("(.)\\1{5,}.*")) {
            return true;
        }

        int wordCount = countWords(motivation);
        int uniqueWordCount = countUniqueWords(motivation);
        if (wordCount <= 2) {
            return true;
        }
        if (uniqueWordCount <= 2 && compact.length() > 15) {
            return true;
        }
        return false;
    }

    private int countWords(String value) {
        String normalized = safe(value).trim();
        if (normalized.isBlank()) {
            return 0;
        }
        return (int) java.util.Arrays.stream(normalized.split("\\s+"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .count();
    }

    private int countUniqueWords(String value) {
        String normalized = safe(value).trim();
        if (normalized.isBlank()) {
            return 0;
        }
        return (int) java.util.Arrays.stream(normalized.split("\\s+"))
                .map(token -> token.replaceAll("[^a-z0-9]", ""))
                .filter(token -> !token.isBlank())
                .distinct()
                .count();
    }

    private record AnalyzedCandidate(Candidature candidature, double score, String reason) { }
}
