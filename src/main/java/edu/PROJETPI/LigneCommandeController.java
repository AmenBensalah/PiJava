package edu.PROJETPI;

import edu.PROJETPI.entites.CartItem;
import edu.PROJETPI.services.OrderSession;
import edu.PROJETPI.tools.AlertUtils;
import edu.PROJETPI.tools.SceneNavigator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class LigneCommandeController implements Initializable {

    @FXML
    private TableView<CartItem> tableView;
    @FXML
    private TableColumn<CartItem, Integer> colProduitId;
    @FXML
    private TableColumn<CartItem, String> colNom;
    @FXML
    private TableColumn<CartItem, Integer> colQuantite;
    @FXML
    private TableColumn<CartItem, Double> colPrixUnitaire;
    @FXML
    private TableColumn<CartItem, Double> colSousTotal;
    @FXML
    private TextField quantiteField;
    @FXML
    private Label totalItemsLabel;
    @FXML
    private Label totalPriceLabel;

    private final ObservableList<CartItem> lignes = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colProduitId.setCellValueFactory(new PropertyValueFactory<>("produitId"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colPrixUnitaire.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colSousTotal.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> fillForm(selected));
        refresh();
    }

    @FXML
    private void refresh() {
        lignes.setAll(OrderSession.getInstance().getCartItems());
        tableView.setItems(lignes);
        totalItemsLabel.setText(String.valueOf(OrderSession.getInstance().getTotalItems()));
        totalPriceLabel.setText(String.format("%.2f TND", OrderSession.getInstance().getCartTotal()));
    }

    @FXML
    private void updateQuantite() {
        CartItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Selectionnez une ligne du panier.");
            return;
        }

        try {
            int quantite = Integer.parseInt(quantiteField.getText().trim());
            OrderSession.getInstance().updateQuantity(selected.getProduitId(), quantite);
            refresh();
            AlertUtils.showSuccess("Quantite mise a jour.");
        } catch (NumberFormatException e) {
            AlertUtils.showError("Saisissez une quantite valide.");
        }
    }

    @FXML
    private void removeLigne() {
        CartItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Selectionnez une ligne du panier a supprimer.");
            return;
        }

        OrderSession.getInstance().removeProduct(selected.getProduitId());
        refresh();
        clearForm();
        AlertUtils.showSuccess("Produit retire du panier.");
    }

    @FXML
    private void clearCart() {
        OrderSession.getInstance().clearCart();
        refresh();
        clearForm();
        AlertUtils.showSuccess("Panier vide.");
    }

    @FXML
    private void continueShopping() {
        SceneNavigator.switchScene(tableView, "/main-view.fxml", "Catalogue produits");
    }

    @FXML
    private void goToCommande() {
        if (OrderSession.getInstance().isCartEmpty()) {
            AlertUtils.showError("Ajoutez au moins un produit avant de continuer.");
            return;
        }
        SceneNavigator.switchScene(tableView, "/commande-view.fxml", "Informations commande");
    }

    @FXML
    private void clearForm() {
        tableView.getSelectionModel().clearSelection();
        quantiteField.clear();
    }

    private void fillForm(CartItem cartItem) {
        if (cartItem == null) {
            return;
        }
        quantiteField.setText(String.valueOf(cartItem.getQuantite()));
    }
}
