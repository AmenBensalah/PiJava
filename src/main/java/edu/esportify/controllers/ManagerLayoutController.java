package edu.esportify.controllers;

import edu.esportify.entities.Equipe;
import edu.esportify.entities.UserRole;
import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.EquipeService;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ManagerLayoutController {
    private enum ManagerView {
        FEED,
        TEAM_FORM_CREATE,
        TEAM_FORM_EDIT,
        TEAM_DASHBOARD,
        CANDIDATES,
        TOURNAMENTS,
        STORE,
        ORDERS,
        ACCOUNT
    }

    private final EquipeService equipeService = new EquipeService();
    private boolean sidebarVisible = true;

    @FXML
    private BorderPane rootPane;

    @FXML
    private StackPane contentContainer;

    @FXML
    private VBox sidebarContainer;

    @FXML
    private Button menuToggleButton;

    @FXML
    private Button teamButton;

    @FXML
    private Label accountNameLabel;

    @FXML
    private Label accountRoleLabel;

    @FXML
    private Label pageTitleLabel;

    @FXML
    private TextField searchField;

    @FXML
    private void initialize() {
        if (AppSession.getInstance().getCurrentUser() == null
                || AppSession.getInstance().getCurrentUser().getRole() != UserRole.MANAGER) {
            AppNavigator.goToLogin();
            return;
        }
        accountNameLabel.setText(AppSession.getInstance().getUsername());
        accountRoleLabel.setText(AppSession.getInstance().getRole() + " account");
        searchField.setPromptText("Rechercher des equipes, tournois...");
        applySidebarState();

        refreshSelectedEquipe();
        if (AppSession.getInstance().getSelectedEquipe() == null) {
            showTeamCreate();
        } else {
            showTeamDashboard();
        }
    }

    @FXML
    private void onFeed() {
        showFeed();
    }

    @FXML
    private void onTeam() {
        if (AppSession.getInstance().getSelectedEquipe() == null) {
            showTeamCreate();
        } else {
            showTeamDashboard();
        }
    }

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

    @FXML
    private void onTournaments() {
        showTournaments();
    }

    @FXML
    private void onStore() {
        showStore();
    }

    @FXML
    private void onOrders() {
        showOrders();
    }

    @FXML
    private void onAccount() {
        showAccount();
    }

    public void showFeed() {
        setCenter("/views/feed-view.fxml", ManagerView.FEED);
    }

    public void showTeamCreate() {
        setCenter("/views/team-form-view.fxml", ManagerView.TEAM_FORM_CREATE);
    }

    public void showTeamEdit() {
        setCenter("/views/team-form-view.fxml", ManagerView.TEAM_FORM_EDIT);
    }

    public void showTeamDashboard() {
        refreshSelectedEquipe();
        setCenter("/views/team-dashboard-view.fxml", ManagerView.TEAM_DASHBOARD);
    }

    public void showCandidates() {
        setCenter("/views/candidate-management-view.fxml", ManagerView.CANDIDATES);
    }

    public void showTournaments() {
        setCenter("/views/tournaments-view.fxml", ManagerView.TOURNAMENTS);
    }

    public void showStore() {
        setCenter("/views/store-view.fxml", ManagerView.STORE);
    }

    public void showOrders() {
        setCenter("/views/orders-view.fxml", ManagerView.ORDERS);
    }

    public void showAccount() {
        setCenter("/views/account-view.fxml", ManagerView.ACCOUNT);
    }

    public void refreshSelectedEquipe() {
        Equipe equipe = equipeService.getByManagerUsername(AppSession.getInstance().getUsername());
        AppSession.getInstance().setSelectedEquipe(equipe);
    }

    private void setCenter(String viewPath, ManagerView activeView) {
        try {
            FXMLLoader loader = AppNavigator.createLoader(viewPath);
            Node view = loader.load();
            Object controller = loader.getController();
            if (controller instanceof ManagerContentController managerContentController) {
                managerContentController.init(this);
            }
            contentContainer.getChildren().setAll(view);
            updateActiveNav(activeView);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger " + viewPath, e);
        }
    }

    private void updateActiveNav(ManagerView activeView) {
        if (teamButton != null) {
            teamButton.getStyleClass().remove("nav-button-active");
        }

        switch (activeView) {
            case FEED -> {
                pageTitleLabel.setText("Fil D'actualite");
            }
            case TEAM_FORM_CREATE, TEAM_FORM_EDIT, TEAM_DASHBOARD, CANDIDATES -> {
                pageTitleLabel.setText("Hub Equipes");
                if (teamButton != null) {
                    teamButton.getStyleClass().add("nav-button-active");
                }
            }
            case TOURNAMENTS -> {
                pageTitleLabel.setText("Tournois");
            }
            case STORE -> {
                pageTitleLabel.setText("Boutique");
            }
            case ORDERS -> {
                pageTitleLabel.setText("Commandes");
            }
            case ACCOUNT -> {
                pageTitleLabel.setText("Mon Compte");
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
