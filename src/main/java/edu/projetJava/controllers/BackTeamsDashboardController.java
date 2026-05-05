package edu.projetJava.controllers;

import edu.PROJETPI.AdminDashboardController;
import edu.ProjetPI.controllers.DashboardSession;
import edu.ProjetPI.controllers.SceneManager;
import edu.connexion3a77.controllers.TournoiAdminController;
import edu.esportify.controllers.AdminContentController;
import edu.esportify.controllers.AdminManagerRequestsController;
import edu.esportify.controllers.AdminTeamFormController;
import edu.esportify.controllers.AdminTeamsController;
import edu.esportify.entities.Equipe;
import edu.esportify.navigation.AppSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.io.IOException;

public class BackTeamsDashboardController {

    private enum TeamSection {
        TEAMS,
        REQUESTS
    }

    @FXML private StackPane contentContainer;
    @FXML private VBox teamSubmenuBox;
    @FXML private Button teamManagementMenuButton;
    @FXML private Button teamsListButton;
    @FXML private Button managerRequestsButton;

    @FXML
    private void initialize() {
        switch (edu.esportify.navigation.AppSession.getInstance().getPendingAdminSection()) {
            case REQUESTS -> showManagerRequests();
            case TEAMS -> showTeams();
            default -> showTeams();
        }
    }

    @FXML
    public void showTeams() {
        setTeamSubmenuVisible(true);
        loadContent("/views/admin-teams-view.fxml", TeamSection.TEAMS);
    }

    public void showTeamEditor(Equipe equipe) {
        setTeamSubmenuVisible(true);
        AppSession.getInstance().setSelectedEquipe(equipe);
        loadContent("/views/admin-team-form-view.fxml", TeamSection.TEAMS);
    }

    @FXML
    void showManagerRequests() {
        setTeamSubmenuVisible(true);
        loadContent("/views/admin-manager-requests-view.fxml", TeamSection.REQUESTS);
    }

    @FXML
    void showTeamsHub() {
        setTeamSubmenuVisible(true);

        Label eyebrow = new Label("ADMIN");
        eyebrow.setStyle("-fx-text-fill: #00e5ff; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label title = new Label("GESTION DES EQUIPES");
        title.getStyleClass().add("banner-title");

        Label subtitle = new Label("Choisissez la gestion des demandes manager ou la gestion complete des equipes.");
        subtitle.getStyleClass().add("banner-subtitle");
        subtitle.setWrapText(true);

        Button requestsHubButton = new Button("Gestion demande manager");
        requestsHubButton.getStyleClass().add("btn-rechercher");
        requestsHubButton.setPrefWidth(240);
        requestsHubButton.setPrefHeight(46);
        requestsHubButton.setOnAction(event -> showManagerRequests());

        Button teamsHubButton = new Button("Gestion equipe");
        teamsHubButton.getStyleClass().add("btn-3d-cyan");
        teamsHubButton.setPrefWidth(220);
        teamsHubButton.setPrefHeight(46);
        teamsHubButton.setOnAction(event -> showTeams());

        HBox actions = new HBox(14, requestsHubButton, teamsHubButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        Label requestsInfo = new Label("Demande manager : tableau avec afficher, accepter, refuser et supprimer.");
        requestsInfo.getStyleClass().add("side-label");
        requestsInfo.setWrapText(true);

        Label teamsInfo = new Label("Gestion equipe : tableau avec toutes les equipes et actions creer, modifier, supprimer et bannir.");
        teamsInfo.getStyleClass().add("side-label");
        teamsInfo.setWrapText(true);

        VBox card = new VBox(16, eyebrow, title, subtitle, actions, requestsInfo, teamsInfo);
        card.getStyleClass().addAll("advanced-search-box", "filter-panel");
        card.setMaxWidth(820);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-padding: 28;");

        VBox wrapper = new VBox(card);
        wrapper.getStyleClass().add("main-area-bg");
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setFillWidth(true);

        contentContainer.getChildren().setAll(wrapper);
        updateActiveButtons(null);
    }

    private void loadContent(String resource, TeamSection section) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            Node view = loader.load();
            Object controller = loader.getController();
            if (controller instanceof AdminTeamFormController teamFormController) {
                teamFormController.setLegacyHost(this);
                teamFormController.init(null);
            } else if (controller instanceof AdminTeamsController teamsController) {
                teamsController.setLegacyHost(this);
                teamsController.init(null);
            } else if (controller instanceof AdminManagerRequestsController requestsController) {
                requestsController.init(null);
            } else if (controller instanceof AdminContentController adminContentController) {
                adminContentController.init(null);
            }
            contentContainer.getChildren().setAll(view);
            updateActiveButtons(section);
        } catch (Exception e) {
            contentContainer.getChildren().setAll(buildErrorContent(resource, e));
            updateActiveButtons(section);
        }
    }

    private Node buildErrorContent(String resource, Exception error) {
        Label title = new Label("Impossible d'afficher la page");
        title.getStyleClass().add("banner-title");

        Label subtitle = new Label("Le chargement de " + resource + " a echoue.");
        subtitle.getStyleClass().add("banner-subtitle");
        subtitle.setWrapText(true);

        TextArea details = new TextArea(error.getClass().getSimpleName() + " : " + safe(error.getMessage()));
        details.setEditable(false);
        details.setWrapText(true);
        details.setPrefRowCount(8);
        details.getStyleClass().add("filter-input");

        VBox card = new VBox(14, title, subtitle, details);
        card.getStyleClass().addAll("advanced-search-box", "filter-panel");
        card.setMaxWidth(900);
        card.setStyle("-fx-padding: 24;");

        VBox wrapper = new VBox(card);
        wrapper.getStyleClass().add("main-area-bg");
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }

    private String safe(String value) {
        return value == null ? "Erreur inconnue" : value;
    }

    private void updateActiveButtons(TeamSection section) {
        teamsListButton.getStyleClass().remove("sidebar-submenu-active");
        managerRequestsButton.getStyleClass().remove("sidebar-submenu-active");
        teamManagementMenuButton.getStyleClass().remove("sidebar-submenu-active");
        if (section == TeamSection.TEAMS) {
            teamManagementMenuButton.getStyleClass().add("sidebar-submenu-active");
            teamsListButton.getStyleClass().add("sidebar-submenu-active");
        } else if (section == TeamSection.REQUESTS) {
            teamManagementMenuButton.getStyleClass().add("sidebar-submenu-active");
            managerRequestsButton.getStyleClass().add("sidebar-submenu-active");
        } else {
            teamManagementMenuButton.getStyleClass().add("sidebar-submenu-active");
        }
    }

    private void setTeamSubmenuVisible(boolean visible) {
        if (teamSubmenuBox != null) {
            teamSubmenuBox.setVisible(visible);
            teamSubmenuBox.setManaged(visible);
        }
    }

    @FXML
    void goToBackListProduit(ActionEvent event) {
        SceneManager.switchScene("/backListProduit.fxml", "Gestion des produits");
    }

    @FXML
    void goToFrontOffice(ActionEvent event) {
        SceneManager.switchScene("/ajoutProduit.fxml", "E-SPORTIFY : Boutique");
    }

    @FXML
    void goToNewsFeed(ActionEvent event) {
        SceneManager.switchScene("/FilActualiteView.fxml", "E-SPORTIFY : Fil d'actualite");
    }

    @FXML
    void goToTournoisAdmin(ActionEvent event) {
        TournoiAdminController.openOn(TournoiAdminController.InitialSection.TOURNOIS);
        SceneManager.switchScene("/fxml/tournoi-admin-view.fxml", "Gestion des tournois");
    }

    @FXML
    void goToParticipationsAdmin(ActionEvent event) {
        TournoiAdminController.openOn(TournoiAdminController.InitialSection.PARTICIPATIONS);
        SceneManager.switchScene("/fxml/tournoi-admin-view.fxml", "Gestion des participations");
    }

    @FXML
    void goToMailing(ActionEvent event) {
        SceneManager.switchScene("/backMailing.fxml", "Boutique Admin - Mailing");
    }

    @FXML
    void goToAdminCategorie(ActionEvent event) {
        SceneManager.switchScene("/backListCategorie.fxml", "Boutique Admin - Categories");
    }

    @FXML
    void goToPayments(ActionEvent event) {
        AdminDashboardController.openOn(AdminDashboardController.InitialSection.PAIEMENTS);
        SceneManager.switchScene("/admin-dashboard-view.fxml", "Liste des paiements");
    }

    @FXML
    void goToRevenuePrediction(ActionEvent event) {
        AdminDashboardController.openOn(AdminDashboardController.InitialSection.PREDICTION_CA);
        SceneManager.switchScene("/admin-dashboard-view.fxml", "Prediction chiffre d'affaires");
    }

    @FXML
    void goToCommandes(ActionEvent event) {
        AdminDashboardController.openOn(AdminDashboardController.InitialSection.COMMANDES);
        SceneManager.switchScene("/admin-dashboard-view.fxml", "Liste des commandes");
    }

    @FXML
    void goToGestionComptes(ActionEvent event) {
        SceneManager.switchScene("/edu/ProjetPI/views/admin-dashboard.fxml", "Gestion des comptes");
    }

    @FXML
    void handleViewProfile(ActionEvent event) {
        SceneManager.switchScene("/edu/ProjetPI/views/profile.fxml", "Mon Profil");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        DashboardSession.clear();
        SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "E-SPORTIFY : Connexion");
    }
}
