package edu.esportify.controllers;

import edu.esportify.entities.Equipe;
import edu.esportify.entities.ManagerRequest;
import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.EquipeService;
import edu.esportify.services.ManagerRequestService;
import edu.esportify.services.RecrutementService;
import java.time.LocalDateTime;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;

public class AdminLayoutController {
    private enum AdminView {
        FEED,
        OVERVIEW,
        TEAMS,
        TEAM_EDITOR,
        REQUESTS,
        PLACEHOLDER
    }

    private final EquipeService equipeService = new EquipeService();
    private final ManagerRequestService managerRequestService = new ManagerRequestService();
    private final RecrutementService recrutementService = new RecrutementService();
    private boolean sidebarVisible = true;

    @FXML private BorderPane rootPane;
    @FXML private StackPane contentContainer;
    @FXML private VBox sidebarContainer;
    @FXML private Accordion sidebarAccordion;
    @FXML private Button menuToggleButton;
    @FXML private Button feedButton;
    @FXML private Button overviewButton;
    @FXML private Button teamsButton;
    @FXML private Button requestsButton;
    @FXML private Button tournamentsButton;
    @FXML private Button storeButton;
    @FXML private Button accountsButton;
    @FXML private Label accountNameLabel;
    @FXML private Label accountRoleLabel;
    @FXML private Label contentBadgeLabel;

    @FXML
    private void initialize() {
        accountNameLabel.setText(valueOrDefault(AppSession.getInstance().getUsername(), "admin"));
        accountRoleLabel.setText(valueOrDefault(AppSession.getInstance().getRole(), "Admin") + " account");
        try {
            ensureDemoData();
        } catch (RuntimeException e) {
            System.out.println("Initialisation admin en mode degrade: " + e.getMessage());
        }
        if (sidebarAccordion != null && !sidebarAccordion.getPanes().isEmpty()) {
            sidebarAccordion.setExpandedPane(sidebarAccordion.getPanes().get(1));
        }
        applySidebarState();
        showOverview();
    }

    @FXML
    private void onFeed() {
        showFeed();
    }

    @FXML
    private void onOverview() {
        showOverview();
    }

    @FXML
    private void onTeams() {
        showTeams();
    }

    @FXML
    private void onRequests() {
        showRequests();
    }

    @FXML
    private void onToggleSidebar() {
        sidebarVisible = !sidebarVisible;
        applySidebarState();
    }

    @FXML
    private void onPlaceholderSection(ActionEvent event) {
        if (event.getSource() instanceof Button button) {
            contentBadgeLabel.setText(button.getText());
        }
        setCenter("/views/admin-overview-view.fxml", AdminView.PLACEHOLDER);
    }

    @FXML
    private void onProfile() {
        contentBadgeLabel.setText("Profil");
    }

    @FXML
    private void onLogout() {
        AppSession.getInstance().logout();
        AppNavigator.goToLogin();
    }

    public void showOverview() {
        setCenter("/views/admin-overview-view.fxml", AdminView.OVERVIEW);
    }

    public void showFeed() {
        setCenter("/views/admin-feed-view.fxml", AdminView.FEED);
    }

    public void showTeams() {
        setCenter("/views/admin-teams-view.fxml", AdminView.TEAMS);
    }

    public void showTeamEditor(Equipe equipe) {
        AppSession.getInstance().setSelectedEquipe(equipe);
        setCenter("/views/admin-team-form-view.fxml", AdminView.TEAM_EDITOR);
    }

    public void showRequests() {
        setCenter("/views/admin-manager-requests-view.fxml", AdminView.REQUESTS);
    }

    private void setCenter(String viewPath, AdminView activeView) {
        try {
            FXMLLoader loader = AppNavigator.createLoader(viewPath);
            Node view = loader.load();
            Object controller = loader.getController();
            if (controller instanceof AdminContentController adminContentController) {
                adminContentController.init(this);
            }
            contentContainer.getChildren().setAll(view);
            updateActiveNav(activeView);
        } catch (Exception e) {
            System.out.println("Impossible de charger " + viewPath + ": " + e.getMessage());
            contentContainer.getChildren().setAll(buildFallbackContent(viewPath));
            updateActiveNav(activeView);
        }
    }

    private void updateActiveNav(AdminView activeView) {
        removeActive(feedButton);
        overviewButton.getStyleClass().remove("nav-button-active");
        teamsButton.getStyleClass().remove("nav-button-active");
        requestsButton.getStyleClass().remove("nav-button-active");
        removeActive(tournamentsButton);
        removeActive(storeButton);
        removeActive(accountsButton);

        switch (activeView) {
            case FEED -> {
                contentBadgeLabel.setText("Fil d'actualite");
                feedButton.getStyleClass().add("nav-button-active");
            }
            case OVERVIEW -> {
                contentBadgeLabel.setText("Dashboard");
                overviewButton.getStyleClass().add("nav-button-active");
            }
            case TEAMS -> {
                contentBadgeLabel.setText("Gestion equipes");
                teamsButton.getStyleClass().add("nav-button-active");
            }
            case TEAM_EDITOR -> {
                contentBadgeLabel.setText("Edition equipe");
                teamsButton.getStyleClass().add("nav-button-active");
            }
            case REQUESTS -> {
                contentBadgeLabel.setText("Demandes");
                requestsButton.getStyleClass().add("nav-button-active");
            }
            case PLACEHOLDER -> {
            }
        }
    }

    private void ensureDemoData() {
        if (equipeService.getData().isEmpty()) {
            equipeService.addEntity(createTeam("Nebula Wolves", "NBL", "Europe", "Diamond", "manager_alpha", false, true, LocalDateTime.now().minusDays(25)));
            equipeService.addEntity(createTeam("Atlas Vortex", "ATV", "North America", "Master", "manager_beta", true, true, LocalDateTime.now().minusDays(18)));
            equipeService.addEntity(createTeam("Solar Reign", "SLR", "Asia", "Immortal", "manager_gamma", false, true, LocalDateTime.now().minusDays(12)));
            equipeService.addEntity(createTeam("Blaze Unit", "BLZ", "Europe", "Ascendant", "manager_delta", false, false, LocalDateTime.now().minusDays(7)));
        }

        if (managerRequestService.getData().isEmpty()) {
            managerRequestService.addEntity(createRequest("zriga", "rayenborgi@gmail.com", "Pro", "Je veux gerer une equipe competitive et encadrer les recrutements.", "En attente", LocalDateTime.now().minusDays(4)));
            managerRequestService.addEntity(createRequest("nova.manager", "nova@esportify.gg", "Legendaire", "Experience en management esport et animation de roster.", "Acceptee", LocalDateTime.now().minusDays(8)));
            managerRequestService.addEntity(createRequest("echo.strat", "echo@esportify.gg", "Heroique", "Je souhaite ouvrir une nouvelle structure sur Valorant.", "Refusee", LocalDateTime.now().minusDays(2)));
        }

        if (recrutementService.getData().isEmpty()) {
            Equipe referenceTeam = equipeService.getData().stream().findFirst().orElse(null);
            if (referenceTeam != null) {
                recrutementService.addEntity(createAnnouncement("Top 5 posts du jour", "highlight", "Annonce mise en avant pour la home page admin.", referenceTeam.getId(), referenceTeam.getNomEquipe(), LocalDateTime.now().minusDays(3)));
                recrutementService.addEntity(createAnnouncement("Opening tryouts Europe", "tryout", "Campagne de recrutement orientee Europe pour les nouveaux talents.", referenceTeam.getId(), referenceTeam.getNomEquipe(), LocalDateTime.now().minusDays(2)));
                recrutementService.addEntity(createAnnouncement("Weekly scrim recap", "recap", "Resume des annonces et activites publiees cette semaine.", referenceTeam.getId(), referenceTeam.getNomEquipe(), LocalDateTime.now().minusHours(18)));
            }
        }
    }

    private Equipe createTeam(String name, String tag, String region, String classement, String manager, boolean isPrivate, boolean active, LocalDateTime createdAt) {
        Equipe equipe = new Equipe();
        equipe.setNomEquipe(name);
        equipe.setTag(tag);
        equipe.setRegion(region);
        equipe.setClassement(classement);
        equipe.setManagerUsername(manager);
        equipe.setPrivate(isPrivate);
        equipe.setActive(active);
        equipe.setDateCreation(createdAt);
        equipe.setDescription("Equipe admin chargee pour la gestion back-office.");
        equipe.setMaxMembers(5);
        equipe.setDiscordInviteUrl("https://discord.gg/esportify");
        return equipe;
    }

    private ManagerRequest createRequest(String username, String email, String niveau, String motivation, String status, LocalDateTime createdAt) {
        ManagerRequest request = new ManagerRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setNiveau(niveau);
        request.setMotivation(motivation);
        request.setStatus(status);
        request.setCreatedAt(createdAt);
        return request;
    }

    private edu.esportify.entities.Recrutement createAnnouncement(String title, String status, String description, int equipeId, String equipeNom, LocalDateTime createdAt) {
        edu.esportify.entities.Recrutement recrutement = new edu.esportify.entities.Recrutement();
        recrutement.setNomRec(title);
        recrutement.setStatus(status);
        recrutement.setDescription(description);
        recrutement.setDatePublication(createdAt);
        recrutement.setEquipeId(equipeId);
        recrutement.setEquipeNom(equipeNom);
        return recrutement;
    }

    private void removeActive(Button button) {
        if (button != null) {
            button.getStyleClass().remove("nav-button-active");
        }
    }

    private void applySidebarState() {
        if (rootPane == null || sidebarContainer == null) {
            return;
        }

        rootPane.setLeft(sidebarVisible ? sidebarContainer : null);

        if (contentContainer != null) {
            contentContainer.getStyleClass().remove("is-expanded");
            if (!sidebarVisible) {
                contentContainer.getStyleClass().add("is-expanded");
            }
        }

        if (menuToggleButton != null) {
            menuToggleButton.getStyleClass().remove("is-collapsed");
            if (!sidebarVisible) {
                menuToggleButton.getStyleClass().add("is-collapsed");
            }
            menuToggleButton.setText(sidebarVisible ? "Masquer menu" : "Afficher menu");
        }
    }

    private VBox buildFallbackContent(String viewPath) {
        Label title = new Label("Interface admin disponible");
        title.getStyleClass().add("admin-reference-title");
        Label body = new Label("La vue " + viewPath + " n'a pas pu etre chargee. Le shell admin reste accessible.");
        body.getStyleClass().add("admin-reference-subtitle");
        body.setWrapText(true);
        VBox box = new VBox(12, title, body);
        box.getStyleClass().addAll("hero-card", "admin-reference-hero");
        box.setPadding(new Insets(28));
        return box;
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
