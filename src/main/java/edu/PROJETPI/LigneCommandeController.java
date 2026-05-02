package edu.PROJETPI;

import edu.PROJETPI.entites.CartItem;
import edu.PROJETPI.services.OrderSession;
import edu.PROJETPI.tools.AlertUtils;
import edu.PROJETPI.tools.SceneNavigator;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

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
    private Label quantiteValueLabel;
    @FXML
    private Label totalItemsLabel;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private Button decreaseQuantityButton;
    @FXML
    private Button increaseQuantityButton;

    private final ObservableList<CartItem> lignes = FXCollections.observableArrayList();
    private double lastDisplayedTotal = -1;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colProduitId.setCellValueFactory(new PropertyValueFactory<>("produitId"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomProduit"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colPrixUnitaire.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        colSousTotal.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> updateQuantityDisplay(selected));
        refresh();
    }

    @FXML
    private void refresh() {
        Integer selectedProduitId = tableView.getSelectionModel().getSelectedItem() != null
                ? tableView.getSelectionModel().getSelectedItem().getProduitId()
                : null;
        refresh(selectedProduitId);
    }

    private void refresh(Integer selectedProduitId) {
        lignes.setAll(OrderSession.getInstance().getCartItems());
        tableView.setItems(lignes);
        totalItemsLabel.setText(String.valueOf(OrderSession.getInstance().getTotalItems()));
        double newTotal = OrderSession.getInstance().getCartTotal();
        totalPriceLabel.setText(String.format("%.2f TND", newTotal));

        if (selectedProduitId != null) {
            lignes.stream()
                    .filter(item -> item.getProduitId() == selectedProduitId)
                    .findFirst()
                    .ifPresent(item -> tableView.getSelectionModel().select(item));
        }
        updateQuantityDisplay(tableView.getSelectionModel().getSelectedItem());

        if (lastDisplayedTotal >= 0 && Double.compare(lastDisplayedTotal, newTotal) != 0) {
            animateTotalPrice();
        }
        lastDisplayedTotal = newTotal;
    }

    @FXML
    private void increaseQuantity() {
        CartItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Selectionnez une ligne du panier.");
            return;
        }

        OrderSession.getInstance().updateQuantity(selected.getProduitId(), selected.getQuantite() + 1);
        refresh(selected.getProduitId());
    }

    @FXML
    private void decreaseQuantity() {
        CartItem selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showError("Selectionnez une ligne du panier.");
            return;
        }

        if (selected.getQuantite() <= 1) {
            AlertUtils.showError("La quantite minimale est 1.");
            return;
        }

        OrderSession.getInstance().updateQuantity(selected.getProduitId(), selected.getQuantite() - 1);
        refresh(selected.getProduitId());
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
        SceneNavigator.switchScene(tableView, "/ajoutProduit.fxml", "E-SPORTIFY : Dashboard");
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
        updateQuantityDisplay(null);
    }

    private void updateQuantityDisplay(CartItem cartItem) {
        if (cartItem == null) {
            quantiteValueLabel.setText("0");
            if (decreaseQuantityButton != null) {
                decreaseQuantityButton.setDisable(true);
            }
            if (increaseQuantityButton != null) {
                increaseQuantityButton.setDisable(true);
            }
            return;
        }
        quantiteValueLabel.setText(String.valueOf(cartItem.getQuantite()));
        if (decreaseQuantityButton != null) {
            decreaseQuantityButton.setDisable(cartItem.getQuantite() <= 1);
        }
        if (increaseQuantityButton != null) {
            increaseQuantityButton.setDisable(false);
        }
    }

    private void animateTotalPrice() {
        totalPriceLabel.setOpacity(0.78);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(220), totalPriceLabel);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.08);
        scaleTransition.setToY(1.08);
        scaleTransition.setAutoReverse(true);
        scaleTransition.setCycleCount(2);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(220), totalPriceLabel);
        fadeTransition.setFromValue(0.78);
        fadeTransition.setToValue(1.0);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setCycleCount(2);

        ParallelTransition animation = new ParallelTransition(scaleTransition, fadeTransition);
        animation.setOnFinished(event -> {
            totalPriceLabel.setScaleX(1.0);
            totalPriceLabel.setScaleY(1.0);
            totalPriceLabel.setOpacity(1.0);
        });
        animation.playFromStart();
    }
}
