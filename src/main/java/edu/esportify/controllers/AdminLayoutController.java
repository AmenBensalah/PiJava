package edu.esportify.controllers;

import edu.PROJETPI.AdminDashboardController;
import edu.connexion3a77.controllers.TournoiAdminController;
import edu.esportify.entities.Equipe;
import edu.esportify.entities.ManagerRequest;
import edu.esportify.entities.UserRole;
import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.EquipeService;
import edu.esportify.services.ManagerRequestService;
import edu.esportify.services.RecrutementService;
import java.time.LocalDateTime;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
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
        ACCOUNTS,
        TOURNAMENTS,
        PARTICIPATIONS,
        STORE_PRODUCTS,
        STORE_CATEGORIES,
        PAYMENTS,
        FORECAST,
        ORDERS,
        PROFILE,
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
    @FXML private TitledPane feedGroupPane;
    @FXML private TitledPane teamsGroupPane;
    @FXML private TitledPane tournamentsGroupPane;
    @FXML private TitledPane storeGroupPane;
    @FXML private TitledPane accountsGroupPane;
    @FXML private Button menuToggleButton;
    @FXML private Button feedButton;
    @FXML private Button overviewButton;
    @FXML private Button teamsButton;
    @FXML private Button requestsButton;
    @FXML private Button tournamentsButton;
    @FXML private Button participationsButton;
    @FXML private Button storeButton;
    @FXML private Button categoriesButton;
    @FXML private Button paymentsButton;
    @FXML private Button forecastButton;
    @FXML private Button ordersButton;
    @FXML private Button accountsButton;
    @FXML private Label accountNameLabel;
    @FXML private Label accountRoleLabel;
    @FXML private Label contentBadgeLabel;

    @FXML
    private void initialize() {
        if (AppSession.getInstance().getCurrentUser() == null
                || AppSession.getInstance().getCurrentUser().getRole() != UserRole.ADMIN) {
            AppNavigator.goToLogin();
            return;
        }
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
        openPendingAdminSection();
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
    private void onAccounts() {
        showAccounts();
    }

    @FXML
    private void onTournaments() {
        showTournaments();
    }

    @FXML
    private void onParticipations() {
        showParticipations();
    }

    @FXML
    private void onStoreProducts() {
        showStoreProducts();
    }

    @FXML
    private void onStoreCategories() {
        showStoreCategories();
    }

    @FXML
    private void onPayments() {
        showPayments();
    }

    @FXML
    private void onForecast() {
        showForecast();
    }

    @FXML
    private void onOrders() {
        showOrders();
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
        showProfile();
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

    public void showAccounts() {
        setCenter("/views/admin-accounts-view.fxml", AdminView.ACCOUNTS);
    }

    public void showTournaments() {
        TournoiAdminController.openOn(TournoiAdminController.InitialSection.TOURNOIS);
        setLegacyCenter("/fxml/tournoi-admin-view.fxml", AdminView.TOURNAMENTS);
    }

    public void showParticipations() {
        TournoiAdminController.openOn(TournoiAdminController.InitialSection.PARTICIPATIONS);
        setLegacyCenter("/fxml/tournoi-admin-view.fxml", AdminView.PARTICIPATIONS);
    }

    public void showStoreProducts() {
        setLegacyCenter("/backListProduit.fxml", AdminView.STORE_PRODUCTS);
    }

    public void showStoreCategories() {
        setLegacyCenter("/backListCategorie.fxml", AdminView.STORE_CATEGORIES);
    }

    public void showPayments() {
        AdminDashboardController.openOn(AdminDashboardController.InitialSection.PAIEMENTS);
        setLegacyCenter("/admin-dashboard-view.fxml", AdminView.PAYMENTS);
    }

    public void showForecast() {
        AdminDashboardController.openOn(AdminDashboardController.InitialSection.PREDICTION_CA);
        setLegacyCenter("/admin-dashboard-view.fxml", AdminView.FORECAST);
    }

    public void showOrders() {
        AdminDashboardController.openOn(AdminDashboardController.InitialSection.COMMANDES);
        setLegacyCenter("/admin-dashboard-view.fxml", AdminView.ORDERS);
    }

    public void showProfile() {
        setLegacyCenter("/edu/ProjetPI/views/profile.fxml", AdminView.PROFILE);
    }

    private void openPendingAdminSection() {
        AppSession.AdminSection section = AppSession.getInstance().getPendingAdminSection();
        AppSession.getInstance().setPendingAdminSection(AppSession.AdminSection.STORE);
        switch (section) {
            case OVERVIEW -> showOverview();
            case TEAMS -> showTeams();
            case REQUESTS -> showRequests();
            case STORE -> showStoreProducts();
        }
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

    private void setLegacyCenter(String viewPath, AdminView activeView) {
        try {
            FXMLLoader loader = AppNavigator.createLoader(viewPath);
            Node root = loader.load();
            Node content = extractLegacyContent(root);
            StackPane wrapper = buildLegacyWrapper(root, content, activeView);
            contentContainer.getChildren().setAll(wrapper);
            updateActiveNav(activeView);
        } catch (Exception e) {
            System.out.println("Impossible d'integrer " + viewPath + ": " + e.getMessage());
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
        removeActive(participationsButton);
        removeActive(storeButton);
        removeActive(categoriesButton);
        removeActive(paymentsButton);
        removeActive(forecastButton);
        removeActive(ordersButton);
        removeActive(accountsButton);
        updateActiveGroup(null);

        switch (activeView) {
            case FEED -> {
                contentBadgeLabel.setText("Fil d'actualite");
                feedButton.getStyleClass().add("nav-button-active");
                updateActiveGroup(feedGroupPane);
            }
            case OVERVIEW -> {
                contentBadgeLabel.setText("Dashboard");
                overviewButton.getStyleClass().add("nav-button-active");
            }
            case TEAMS -> {
                contentBadgeLabel.setText("Gestion equipes");
                teamsButton.getStyleClass().add("nav-button-active");
                updateActiveGroup(teamsGroupPane);
            }
            case TEAM_EDITOR -> {
                contentBadgeLabel.setText("Edition equipe");
                teamsButton.getStyleClass().add("nav-button-active");
                updateActiveGroup(teamsGroupPane);
            }
            case REQUESTS -> {
                contentBadgeLabel.setText("Demandes");
                requestsButton.getStyleClass().add("nav-button-active");
                updateActiveGroup(teamsGroupPane);
            }
            case ACCOUNTS -> {
                contentBadgeLabel.setText("Comptes");
                accountsButton.getStyleClass().add("nav-button-active");
                updateActiveGroup(accountsGroupPane);
            }
            case TOURNAMENTS -> {
                contentBadgeLabel.setText("Tournois");
                tournamentsButton.getStyleClass().add("nav-button-active");
                updateActiveGroup(tournamentsGroupPane);
            }
            case PARTICIPATIONS -> {
                contentBadgeLabel.setText("Participations");
                participationsButton.getStyleClass().add("nav-button-active");
                updateActiveGroup(tournamentsGroupPane);
            }
            case STORE_PRODUCTS -> {
                contentBadgeLabel.setText("Produits");
                storeButton.getStyleClass().add("nav-button-active");
                updateActiveGroup(storeGroupPane);
            }
            case STORE_CATEGORIES -> {
                contentBadgeLabel.setText("Categories");
                categoriesButton.getStyleClass().add("nav-button-active");
                updateActiveGroup(storeGroupPane);
            }
            case PAYMENTS -> {
                contentBadgeLabel.setText("Paiements");
                paymentsButton.getStyleClass().add("nav-button-active");
                updateActiveGroup(storeGroupPane);
            }
            case FORECAST -> {
                contentBadgeLabel.setText("Prediction CA");
                forecastButton.getStyleClass().add("nav-button-active");
                updateActiveGroup(storeGroupPane);
            }
            case ORDERS -> {
                contentBadgeLabel.setText("Commandes");
                ordersButton.getStyleClass().add("nav-button-active");
                updateActiveGroup(storeGroupPane);
            }
            case PROFILE -> {
                contentBadgeLabel.setText("Profil");
            }
            case PLACEHOLDER -> {
            }
        }
    }

    private Node extractLegacyContent(Node root) {
        if (root instanceof BorderPane borderPane) {
            Node center = borderPane.getCenter();
            if (center != null) {
                borderPane.setCenter(null);
                return center;
            }
        }
        if (root instanceof StackPane stackPane) {
            for (Node child : stackPane.getChildren()) {
                if (child instanceof BorderPane borderPane) {
                    Node center = borderPane.getCenter();
                    if (center != null) {
                        borderPane.setCenter(null);
                        return center;
                    }
                }
            }
        }
        if (root instanceof Pane pane) {
            pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
        return root;
    }

    private StackPane buildLegacyWrapper(Node root, Node content, AdminView activeView) {
        StackPane wrapper = new StackPane(content);
        wrapper.getStyleClass().add("admin-legacy-host");

        if (root instanceof Parent parentRoot) {
            wrapper.getStylesheets().addAll(parentRoot.getStylesheets());
            wrapper.getStyleClass().addAll(parentRoot.getStyleClass());
            if (parentRoot.getStyle() != null && !parentRoot.getStyle().isBlank()) {
                wrapper.setStyle(parentRoot.getStyle());
            }
        }

        if (content instanceof Region region) {
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }

        switch (activeView) {
            case TOURNAMENTS, PARTICIPATIONS -> wrapper.getStyleClass().add("admin-legacy-tournament");
            case PAYMENTS, FORECAST, ORDERS -> wrapper.getStyleClass().add("admin-legacy-command");
            default -> wrapper.getStyleClass().add("admin-legacy-generic");
        }

        return wrapper;
    }

    private void ensureDemoData() {
        if (equipeService.getData().isEmpty()) {
            equipeService.addEntity(createTeam("Nebula Wolves", "NBL", "Europe", "Diamond", "manager_alpha", false, true, LocalDateTime.now().minusDays(25)));
            equipeService.addEntity(createTeam("Atlas Vortex", "ATV", "North America", "Master", "manager_beta", true, true, LocalDateTime.now().minusDays(18)));
            equipeService.addEntity(createTeam("Solar Reign", "SLR", "Asia", "Immortal", "manager_gamma", false, true, LocalDateTime.now().minusDays(12)));
            equipeService.addEntity(createTeam("Blaze Unit", "BLZ", "Europe", "Ascendant", "manager_delta", false, false, LocalDateTime.now().minusDays(7)));
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

    private void updateActiveGroup(TitledPane activePane) {
        setGroupActive(feedGroupPane, activePane == feedGroupPane);
        setGroupActive(teamsGroupPane, activePane == teamsGroupPane);
        setGroupActive(tournamentsGroupPane, activePane == tournamentsGroupPane);
        setGroupActive(storeGroupPane, activePane == storeGroupPane);
        setGroupActive(accountsGroupPane, activePane == accountsGroupPane);
        if (sidebarAccordion != null && activePane != null) {
            sidebarAccordion.setExpandedPane(activePane);
        }
    }

    private void setGroupActive(TitledPane pane, boolean active) {
        if (pane == null) {
            return;
        }
        pane.getStyleClass().remove("is-active");
        if (active) {
            pane.setExpanded(true);
            if (!pane.getStyleClass().contains("is-active")) {
                pane.getStyleClass().add("is-active");
            }
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
