package edu.projetJava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import edu.projetJava.entities.Categorie;
import edu.projetJava.services.CategorieService;
import edu.projetJava.services.ProduitService;
import edu.PROJETPI.AdminDashboardController;
import edu.ProjetPI.controllers.DashboardSession;
import edu.ProjetPI.controllers.SceneManager;
import edu.connexion3a77.controllers.TournoiAdminController;
import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class BackListCategorieController implements Initializable {

    @FXML private VBox tableContainer;
    @FXML private javafx.scene.chart.PieChart statChart;

    private CategorieService categorieService = new CategorieService();
    private ProduitService produitService = new ProduitService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chargerCategories();
    }

    @FXML
    void chargerCategories() {
        try {
            tableContainer.getChildren().clear();
            if (statChart != null) statChart.getData().clear();
            
            List<Categorie> categories = categorieService.recuperer();
            
            for (Categorie c : categories) {
                HBox row = createTableRow(c);
                tableContainer.getChildren().add(row);
                
                // Remplissage du Graphique 3D
                if (statChart != null) {
                    long count = 0;
                    try {
                        count = produitService.recuperer().stream().filter(p -> p.getCategorieId() == c.getId()).count();
                    } catch (Exception ignored) {}
                    
                    if (count > 0) {
                        javafx.scene.chart.PieChart.Data slice = new javafx.scene.chart.PieChart.Data(c.getNom() + " (" + count + ")", count);
                        statChart.getData().add(slice);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createTableRow(Categorie c) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(10);

        Label lblId = new Label("#" + c.getId());
        lblId.setPrefWidth(50);
        lblId.getStyleClass().add("table-text-muted");

        Label lblNom = new Label(c.getNom());
        lblNom.setPrefWidth(250);
        lblNom.getStyleClass().add("table-text");

        // Compter les produits (Simulé ou réel si facile)
        long count = 0;
        try {
            count = produitService.recuperer().stream().filter(p -> p.getCategorieId() == c.getId()).count();
        } catch (Exception ignored) {}

        Label lblProduits = new Label(count + " produit(s)");
        lblProduits.setPrefWidth(150);
        lblProduits.getStyleClass().add("badge-stock"); // Vert comme sur le screen

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Actions
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnView = new Button("👁");
        btnView.getStyleClass().add("action-btn-small");
        
        Button btnEdit = new Button("✏");
        btnEdit.getStyleClass().add("action-btn-small");
        btnEdit.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/backCategorie.fxml"));
                Parent root = loader.load();
                
                BackCategorieController formController = loader.getController();
                formController.initData(c); // pre-populate with selected category

                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage.getScene().setRoot(root);
                stage.setFullScreen(false); stage.setMaximized(true);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        
        Button btnDelete = new Button("🗑");
        btnDelete.getStyleClass().addAll("action-btn-small", "action-btn-delete");
        btnDelete.setOnAction(e -> supprimerCategorieDirect(c.getId()));

        actionsBox.getChildren().addAll(btnView, btnEdit, btnDelete);

        row.getChildren().addAll(lblId, lblNom, lblProduits, spacer, actionsBox);
        return row;
    }

    private void supprimerCategorieDirect(int id) {
        try {
            categorieService.supprimer(id);
            chargerCategories();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void goToCreateForm(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/backCategorie.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.setFullScreen(false); stage.setMaximized(true);
    }

    // --- NAVIGATION ---
    @FXML
    void goToFrontOffice(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ajoutProduit.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.setFullScreen(false); stage.setMaximized(true);
    }

    @FXML
    void goToAdminProduit(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/backListProduit.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.getScene().setRoot(root);
        stage.setFullScreen(false); stage.setMaximized(true);
    }

    @FXML
    void goToNewsFeed(ActionEvent event) {
        SceneManager.switchScene("/FilActualiteView.fxml", "E-SPORTIFY : Fil d'actualite");
    }

    @FXML
    void goToRawgGames(ActionEvent event) {
        goToTeamsAdmin(event);
    }

    @FXML
    void goToTeamsAdmin(ActionEvent event) {
        AppSession.getInstance().setPendingAdminSection(AppSession.AdminSection.TEAMS);
        SceneManager.switchScene("/backTeamsDashboard.fxml", "Gestion des equipes");
    }

    @FXML
    void goToManagerRequestsAdmin(ActionEvent event) {
        AppSession.getInstance().setPendingAdminSection(AppSession.AdminSection.REQUESTS);
        SceneManager.switchScene("/backTeamsDashboard.fxml", "Gestion des equipes");
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
