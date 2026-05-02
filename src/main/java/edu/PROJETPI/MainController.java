package edu.PROJETPI;

import edu.PROJETPI.entites.Produit;
import edu.PROJETPI.services.CatalogueProduitService;
import edu.PROJETPI.services.OrderSession;
import edu.PROJETPI.tools.AlertUtils;
import edu.PROJETPI.tools.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private TableView<Produit> tableView;
    @FXML
    private TableColumn<Produit, Integer> colId;
    @FXML
    private TableColumn<Produit, String> colNom;
    @FXML
    private TableColumn<Produit, Double> colPrix;
    @FXML
    private TableColumn<Produit, Integer> colStock;
    @FXML
    private TableColumn<Produit, String> colDescription;
    @FXML
    private TextField quantiteField;

    private final CatalogueProduitService catalogueProduitService = new CatalogueProduitService();
    private final ObservableList<Produit> produits = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prix"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

        produits.setAll(catalogueProduitService.readAll());
        tableView.setItems(produits);
        quantiteField.setText("1");
    }

    @FXML
    private void addSelectedProduct() {
        Produit selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Selectionnez un produit avant de l'ajouter au panier.");
            return;
        }

        try {
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            if (quantite <= 0) {
                AlertUtils.showError("La quantite doit etre superieure a zero.");
                return;
            }
            if (quantite > selected.getStock()) {
                AlertUtils.showError("La quantite demandee depasse le stock disponible.");
                return;
            }

            OrderSession.getInstance().addProduct(selected, quantite);
            AlertUtils.showSuccess("Produit ajoute au panier.");
        } catch (NumberFormatException e) {
            AlertUtils.showError("Saisissez une quantite valide.");
        }
    }

    @FXML
    private void goToCart() {
        SceneNavigator.switchScene(tableView, "/lignecommande-view.fxml", "Panier");
    }

    @FXML
    private void clearCart() {
        OrderSession.getInstance().clearCart();
        AlertUtils.showSuccess("Panier vide.");
    }

    @FXML
    private void openAdminDashboard() {
        SceneNavigator.switchScene(tableView, "/admin-dashboard-view.fxml", "Dashboard admin");
    }
}
