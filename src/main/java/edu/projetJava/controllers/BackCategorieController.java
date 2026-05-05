package edu.projetJava.controllers;

import edu.PROJETPI.AdminDashboardController;
import edu.ProjetPI.controllers.DashboardSession;
import edu.ProjetPI.controllers.SceneManager;
import edu.connexion3a77.controllers.TournoiAdminController;
import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import edu.projetJava.entities.Categorie;
import edu.projetJava.services.CategorieService;

import java.sql.SQLException;

public class BackCategorieController {

    @FXML private TextField tfId;
    @FXML private TextField tfNom;

    private CategorieService categorieService = new CategorieService();

    public void initData(Categorie c) {
        tfId.setText(String.valueOf(c.getId()));
        tfNom.setText(c.getNom());
    }

    @FXML
    void ajouterCategorie(ActionEvent event) {
        try {
            if (tfNom.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez remplir le Nom de la catégorie.");
                return;
            }

            Categorie c = new Categorie();
            c.setNom(tfNom.getText());
            categorieService.ajouter(c);
            
            showAlert(Alert.AlertType.INFORMATION, "Succès!", "La catégorie '"+c.getNom()+"' a été ajoutée !");
            tfNom.clear();
            tfId.clear();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Impossible d'insérer: " + e.getMessage());
        }
    }

    @FXML
    void modifierCategorie(ActionEvent event) {
        try {
            if (tfId.getText().isEmpty() || tfNom.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez fournir l'ID et le nouveau Nom.");
                return;
            }

            Categorie c = new Categorie();
            c.setId(Integer.parseInt(tfId.getText()));
            c.setNom(tfNom.getText());

            categorieService.modifier(c);
            showAlert(Alert.AlertType.INFORMATION, "Succès!", "La catégorie modifiée avec succès.");
            tfNom.clear();
            tfId.clear();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Format", "L'ID doit être un nombre.");
        }
    }

    @FXML
    void supprimerCategorie(ActionEvent event) {
        try {
            if (tfId.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez fournir l'ID de la catégorie à supprimer.");
                return;
            }
            int id = Integer.parseInt(tfId.getText());
            categorieService.supprimer(id);
            showAlert(Alert.AlertType.INFORMATION, "Succès!", "Catégorie supprimée.");
            tfNom.clear();
            tfId.clear();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", e.getMessage());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur Format", "L'ID doit être un nombre.");
        }
    }

    // --- NAVIGATION ---
    @FXML
    void goToFrontOffice(ActionEvent event) {
        SceneManager.switchScene("/ajoutProduit.fxml", "E-SPORTIFY : Boutique");
    }

    @FXML
    void goToAdminProduit(ActionEvent event) {
        SceneManager.switchScene("/backListProduit.fxml", "Gestion des produits");
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
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
