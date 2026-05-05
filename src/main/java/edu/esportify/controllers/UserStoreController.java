package edu.esportify.controllers;

import edu.projetJava.models.Produit;
import edu.projetJava.controllers.AjoutProduitController;
import edu.projetJava.services.ProduitService;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserStoreController implements UserContentController {
    private enum SortMode {
        NONE,
        PRICE_ASC,
        PRICE_DESC
    }

    private final ProduitService produitService = new ProduitService();
    private SortMode sortMode = SortMode.NONE;
    private boolean offerActive;

    @FXML private TextField emailField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> stockFilterBox;
    @FXML private Label resultsLabel;
    @FXML private FlowPane productsContainer;

    @FXML
    private void initialize() {
        stockFilterBox.getItems().setAll("Tous", "En stock", "Rupture");
        stockFilterBox.setValue("Tous");
        nameField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        stockFilterBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    @Override
    public void init(UserLayoutController parentController) {
        applyFilters();
    }

    @FXML
    private void onReset() {
        emailField.clear();
        nameField.clear();
        stockFilterBox.setValue("Tous");
        sortMode = SortMode.NONE;
        offerActive = false;
        applyFilters();
    }

    @FXML
    private void onShowAll() {
        stockFilterBox.setValue("Tous");
        applyFilters();
    }

    @FXML
    private void onShowAvailableOnly() {
        stockFilterBox.setValue("En stock");
        applyFilters();
    }

    @FXML
    private void onSortPriceAsc() {
        sortMode = SortMode.PRICE_ASC;
        applyFilters();
    }

    @FXML
    private void onSortPriceDesc() {
        sortMode = SortMode.PRICE_DESC;
        applyFilters();
    }

    @FXML
    private void onActivateOffer() {
        offerActive = emailField.getText() != null && emailField.getText().contains("@");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Offre");
        alert.setHeaderText(offerActive ? "Promotion activee" : "Email invalide");
        alert.setContentText(offerActive
                ? "La reduction visuelle de 10% est active sur les cartes produit."
                : "Entrez un email valide pour activer l'offre.");
        alert.showAndWait();
        applyFilters();
    }

    @FXML
    private void onShowRecommendations() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajoutProduit.fxml"));
            Parent root = loader.load();
            AjoutProduitController controller = loader.getController();
            controller.openRecommendationsPage();

            Stage stage = (Stage) productsContainer.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("IA Recommandations");
            alert.setHeaderText("Ouverture impossible");
            alert.setContentText("La page des recommandations boutique n'a pas pu etre chargee : " + exception.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void onOpenCart() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Panier");
        alert.setHeaderText("Panier utilisateur");
        alert.setContentText("Le bouton Panier est integre au design. On peut ensuite le brancher a la vraie vue panier.");
        alert.showAndWait();
    }

    private void applyFilters() {
        String keyword = normalize(nameField.getText());
        String stockFilter = stockFilterBox.getValue();
        List<Produit> produits = new ArrayList<>(loadProducts());

        produits = produits.stream()
                .filter(produit -> normalize(produit.getNom()).contains(keyword))
                .filter(produit -> matchesStockFilter(produit, stockFilter))
                .sorted(resolveComparator())
                .toList();

        renderProducts(produits);
    }

    private Comparator<Produit> resolveComparator() {
        return switch (sortMode) {
            case PRICE_ASC -> Comparator.comparingInt(Produit::getPrix);
            case PRICE_DESC -> Comparator.comparingInt(Produit::getPrix).reversed();
            case NONE -> Comparator.comparing(produit -> normalize(produit.getNom()));
        };
    }

    private boolean matchesStockFilter(Produit produit, String stockFilter) {
        if (stockFilter == null || "Tous".equalsIgnoreCase(stockFilter)) {
            return true;
        }
        boolean inStock = produit.getStock() > 0;
        return switch (stockFilter) {
            case "En stock" -> inStock;
            case "Rupture" -> !inStock;
            default -> true;
        };
    }

    private List<Produit> loadProducts() {
        try {
            return produitService.recuperer();
        } catch (SQLException exception) {
            return demoProducts();
        }
    }

    private void renderProducts(List<Produit> produits) {
        productsContainer.getChildren().clear();
        resultsLabel.setText(produits.size() + " produit(s) trouve(s)");

        if (produits.isEmpty()) {
            Label emptyLabel = new Label("Aucun produit ne correspond aux filtres.");
            emptyLabel.getStyleClass().add("muted-label");
            productsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Produit produit : produits) {
            productsContainer.getChildren().add(createProductCard(produit));
        }
    }

    private VBox createProductCard(Produit produit) {
        VBox card = new VBox();
        card.setPrefWidth(300);
        card.setSpacing(10);
        card.getStyleClass().add("product-card");

        AnchorPane imgArea = new AnchorPane();
        imgArea.setPrefHeight(160);
        imgArea.getStyleClass().add("product-card-img-area");

        Label title = new Label(value(produit.getNom(), "Produit gaming"));
        title.getStyleClass().add("card-title");

        HBox priceStockBox = new HBox();
        priceStockBox.setAlignment(Pos.CENTER_LEFT);
        priceStockBox.setSpacing(10);

        VBox priceBox = new VBox(4);
        if (offerActive) {
            Label discounted = new Label(String.format(Locale.ROOT, "%.0f EUR", produit.getPrix() * 0.9));
            discounted.getStyleClass().add("card-price");
            Label original = new Label(produit.getPrix() + " EUR");
            original.setStyle("-fx-text-fill: #94a3b8; -fx-strikethrough: true; -fx-font-size: 13px;");
            priceBox.getChildren().addAll(discounted, original);
        } else {
            Label price = new Label(produit.getPrix() + " EUR");
            price.getStyleClass().add("card-price");
            priceBox.getChildren().add(price);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        boolean inStock = produit.getStock() > 0;
        Label stock = new Label(inStock ? "EN STOCK" : "RUPTURE");
        stock.getStyleClass().add(inStock ? "badge-stock" : "badge-rupture");

        priceStockBox.getChildren().addAll(priceBox, spacer, stock);

        Label description = new Label(value(produit.getDescription(), "Produit disponible dans la boutique e-sport."));
        description.getStyleClass().add("muted-label");
        description.setWrapText(true);

        HBox actionBox = new HBox();
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setSpacing(10);

        Button detailsButton = new Button("Voir details");
        detailsButton.getStyleClass().add("cat-pill");
        detailsButton.setOnAction(event -> showProductDetails(produit));

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button cartButton = new Button("Ajouter");
        cartButton.getStyleClass().add("btn-cart");
        cartButton.setDisable(!inStock);
        cartButton.setOnAction(event -> showCartMessage(produit));

        actionBox.getChildren().addAll(detailsButton, spacer2, cartButton);
        card.getChildren().addAll(imgArea, title, priceStockBox, description, actionBox);
        return card;
    }

    private void showProductDetails(Produit produit) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Produit");
        alert.setHeaderText(value(produit.getNom(), "Produit"));
        alert.setContentText(
                "Prix: " + produit.getPrix() + " EUR\n"
                        + "Stock: " + produit.getStock() + "\n"
                        + "Description: " + value(produit.getDescription(), "Aucune description.")
        );
        alert.showAndWait();
    }

    private void showCartMessage(Produit produit) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Panier");
        alert.setHeaderText("Produit ajoute");
        alert.setContentText(value(produit.getNom(), "Produit") + " est pret a etre envoye vers le panier.");
        alert.showAndWait();
    }

    private List<Produit> demoProducts() {
        List<Produit> produits = new ArrayList<>();
        produits.add(new Produit(1, "carte mere hytts", 100, 12, "Une base solide pour config gaming.", "", true, "actif", 0, 0, 0, "", "", ""));
        produits.add(new Produit(2, "carte mere ttht7410", 140, 9, "Carte mere fiable pour setup competitif.", "", true, "actif", 0, 0, 0, "", "", ""));
        produits.add(new Produit(3, "pc gamer mpla", 140, 6, "Configuration gaming prete pour la scene e-sport.", "", true, "actif", 0, 0, 0, "", "", ""));
        produits.add(new Produit(4, "pc gamer", 1405, 3, "Tour premium pour joueurs exigeants.", "", true, "actif", 0, 0, 0, "", "", ""));
        return produits;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
