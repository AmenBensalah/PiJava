package edu.esportify.controllers;

import edu.esportify.entities.Equipe;
import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class UserLayoutController {
    private enum UserView {
        FEED,
        TEAMS,
        TEAM_DETAIL,
        MY_TEAM,
        APPLY,
        CANDIDATURES,
        MANAGER_REQUEST,
        TOURNAMENTS,
        STORE,
        ORDERS,
        ACCOUNT
    }

    private boolean sidebarVisible = true;

    @FXML private BorderPane rootPane;
    @FXML private StackPane contentContainer;
    @FXML private VBox sidebarContainer;
    @FXML private Button menuToggleButton;
    @FXML private Button feedButton;
    @FXML private Button teamButton;
    @FXML private Button tournamentsButton;
    @FXML private Button storeButton;
    @FXML private Button ordersButton;
    @FXML private Button accountButton;
    @FXML private Label accountNameLabel;
    @FXML private Label accountRoleLabel;
    @FXML private Label pageTitleLabel;
    @FXML private TextField searchField;

    @FXML
    private void initialize() {
        accountNameLabel.setText(AppSession.getInstance().getUsername());
        accountRoleLabel.setText(AppSession.getInstance().getRole() + " account");
        searchField.setPromptText("Rechercher des equipes, tournois...");
        applySidebarState();
        showTeams();
    }

    @FXML private void onFeed() { showFeed(); }
    @FXML private void onTeam() { showTeams(); }
    @FXML private void onTournaments() { showTournaments(); }
    @FXML private void onStore() { showStore(); }
    @FXML private void onOrders() { showOrders(); }
    @FXML private void onAccount() { showAccount(); }
    @FXML
    private void onToggleSidebar() {
        sidebarVisible = !sidebarVisible;
        applySidebarState();
    }

    @FXML
    private void onLogout() {
        AppSession.getInstance().logout();
        AppNavigator.goToLogin();
    }

    public void showFeed() { setCenter("/views/user-feed-view.fxml", UserView.FEED); }
    public void showTeams() { setCenter("/views/user-teams-view.fxml", UserView.TEAMS); }
    public void showTeamDetail(Equipe equipe) {
        AppSession.getInstance().setSelectedEquipe(equipe);
        setCenter("/views/user-team-detail-view.fxml", UserView.TEAM_DETAIL);
    }
    public void showMyTeam() {
        var candidatureService = new edu.esportify.services.CandidatureService();
        var equipeService = new edu.esportify.services.EquipeService();
        var membership = candidatureService.getAcceptedForUser(AppSession.getInstance().getUsername());
        if (membership == null) {
            showCandidatures();
            return;
        }
        Equipe equipe = equipeService.getById(membership.getEquipeId());
        if (equipe == null) {
            showTeams();
            return;
        }
        AppSession.getInstance().setSelectedEquipe(equipe);
        setCenter("/views/user-team-detail-view.fxml", UserView.MY_TEAM);
    }
    public void showApply(Equipe equipe) {
        var candidatureService = new edu.esportify.services.CandidatureService();
        var membership = candidatureService.getAcceptedForUser(AppSession.getInstance().getUsername());
        if (membership != null && membership.getEquipeId() != equipe.getId()) {
            showMyTeam();
            return;
        }
        AppSession.getInstance().setSelectedEquipe(equipe);
        setCenter("/views/user-apply-view.fxml", UserView.APPLY);
    }
    public void showCandidatures() { setCenter("/views/user-candidatures-view.fxml", UserView.CANDIDATURES); }
    public void showManagerRequestForm() { setCenter("/views/user-manager-request-view.fxml", UserView.MANAGER_REQUEST); }
    public void showTournaments() { setCenter("/views/user-tournaments-view.fxml", UserView.TOURNAMENTS); }
    public void showStore() { setCenter("/views/user-store-view.fxml", UserView.STORE); }
    public void showOrders() { setCenter("/views/user-orders-view.fxml", UserView.ORDERS); }
    public void showAccount() { setCenter("/views/user-account-view.fxml", UserView.ACCOUNT); }

    private void setCenter(String viewPath, UserView activeView) {
        try {
            FXMLLoader loader = AppNavigator.createLoader(viewPath);
            Node view = loader.load();
            Object controller = loader.getController();
            if (controller instanceof UserContentController userContentController) {
                userContentController.init(this);
            }
            contentContainer.getChildren().setAll(view);
            updateActiveNav(activeView);
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger " + viewPath, e);
        }
    }

    private void updateActiveNav(UserView activeView) {
        feedButton.getStyleClass().remove("nav-button-active");
        teamButton.getStyleClass().remove("nav-button-active");
        tournamentsButton.getStyleClass().remove("nav-button-active");
        storeButton.getStyleClass().remove("nav-button-active");
        ordersButton.getStyleClass().remove("nav-button-active");
        accountButton.getStyleClass().remove("nav-button-active");

        switch (activeView) {
            case FEED -> {
                pageTitleLabel.setText("Fil D'actualite");
                feedButton.getStyleClass().add("nav-button-active");
            }
            case TEAMS, TEAM_DETAIL, MY_TEAM, APPLY, CANDIDATURES, MANAGER_REQUEST -> {
                pageTitleLabel.setText("Explorer une equipe");
                teamButton.getStyleClass().add("nav-button-active");
            }
            case TOURNAMENTS -> {
                pageTitleLabel.setText("Tournois");
                tournamentsButton.getStyleClass().add("nav-button-active");
            }
            case STORE -> {
                pageTitleLabel.setText("Boutique");
                storeButton.getStyleClass().add("nav-button-active");
            }
            case ORDERS -> {
                pageTitleLabel.setText("Commande");
                ordersButton.getStyleClass().add("nav-button-active");
            }
            case ACCOUNT -> {
                pageTitleLabel.setText("Mon compte");
                accountButton.getStyleClass().add("nav-button-active");
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
}
