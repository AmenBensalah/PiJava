package edu.esportify.controllers;

import edu.PROJETPI.services.OrderSession;
import edu.esportify.navigation.AppNavigator;
import edu.esportify.navigation.AppSession;
import edu.projetJava.controllers.AjoutProduitController;
import edu.projetJava.models.Produit;
import edu.projetJava.services.ProduitService;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserStoreController implements UserContentController {
    private static final String[] FALLBACK_IMAGES = {
            "/images/gaming.jpg",
            "/images/esportify-card.jpg",
            "/images/logo3.png",
            "/images/logo5.png"
    };

    private enum SortMode {
        NONE,
        PRICE_ASC,
        PRICE_DESC
    }

    private final ProduitService produitService = new ProduitService();
    private UserLayoutController parentController;
    private SortMode sortMode = SortMode.NONE;
    private boolean offerActive;

    @FXML private TextField emailField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> stockFilterBox;
    @FXML private Label resultsLabel;
    @FXML private FlowPane productsContainer;
    @FXML private VBox chatbotWindow;
    @FXML private VBox chatMessages;
    @FXML private TextField chatInput;

    @FXML
    private void initialize() {
        stockFilterBox.getItems().setAll("Tous", "En stock", "Rupture");
        stockFilterBox.setValue("Tous");
        nameField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        stockFilterBox.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
    }

    @Override
    public void init(UserLayoutController parentController) {
        this.parentController = parentController;
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

            Scene currentScene = productsContainer.getScene();
            Scene scene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
            Stage stage = (Stage) currentScene.getWindow();
            stage.setScene(scene);
            stage.show();
            applyMainWindowMode(stage);
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
        openCartView();
    }

    @FXML
    private void toggleChatbot() {
        chatbotWindow.setVisible(!chatbotWindow.isVisible());
        if (chatbotWindow.isVisible() && chatMessages.getChildren().isEmpty()) {
            addMessageBubble(
                    "IA Assistant",
                    "Bonjour! Je suis l'assistant E-SPORTIFY. Comment puis-je vous aider aujourd'hui?",
                    "-fx-background-color: #1a1a2e; -fx-border-color: linear-gradient(to right, #8a2be2, #4a00e0); -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(138,43,226,0.6), 15, 0, 0, 0); -fx-text-fill: white;"
            );
        }
    }

    @FXML
    private void sendChatMessage() {
        String text = chatInput.getText() == null ? "" : chatInput.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        addMessageBubble(
                "Vous",
                text,
                "-fx-background-color: #0f3443; -fx-border-color: #00e5ff; -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(0,229,255,0.7), 15, 0, 0, 0); -fx-text-fill: white;"
        );
        chatInput.clear();

        Thread aiThread = new Thread(() -> {
            String response = edu.projetJava.services.GeminiAIService.getResponse(text);
            javafx.application.Platform.runLater(() -> addMessageBubble(
                    "IA Assistant",
                    response,
                    "-fx-background-color: #1a1a2e; -fx-border-color: linear-gradient(to right, #8a2be2, #4a00e0); -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(138,43,226,0.6), 15, 0, 0, 0); -fx-text-fill: white;"
            ));
        });
        aiThread.setDaemon(true);
        aiThread.start();
    }

    private void addMessageBubble(String sender, String text, String style) {
        VBox bubbleBox = new VBox(5);
        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");

        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(260);
        messageLabel.setStyle(style + " -fx-padding: 10; -fx-background-radius: 10;");

        bubbleBox.setAlignment("Vous".equals(sender) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        bubbleBox.getChildren().addAll(senderLabel, messageLabel);
        chatMessages.getChildren().add(bubbleBox);
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

        ImageView productImage = buildProductImageView(produit);
        if (productImage != null) {
            AnchorPane.setTopAnchor(productImage, 0.0);
            AnchorPane.setRightAnchor(productImage, 0.0);
            AnchorPane.setBottomAnchor(productImage, 0.0);
            AnchorPane.setLeftAnchor(productImage, 0.0);
            imgArea.getChildren().add(productImage);
        } else {
            Label placeholder = new Label("Visuel produit");
            placeholder.getStyleClass().add("product-image-placeholder");
            placeholder.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            AnchorPane.setTopAnchor(placeholder, 0.0);
            AnchorPane.setRightAnchor(placeholder, 0.0);
            AnchorPane.setBottomAnchor(placeholder, 0.0);
            AnchorPane.setLeftAnchor(placeholder, 0.0);
            imgArea.getChildren().add(placeholder);
        }

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
        cartButton.setOnAction(event -> addToCartAndOpen(produit));

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

    private void addToCartAndOpen(Produit produit) {
        if (produit.getStock() <= 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Panier");
            alert.setHeaderText("Produit indisponible");
            alert.setContentText("Ce produit est actuellement en rupture de stock.");
            alert.showAndWait();
            return;
        }

        try {
            OrderSession.getInstance().addProduct(toCartProduct(produit), 1);
            openCartView();
        } catch (IllegalArgumentException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Panier");
            alert.setHeaderText("Ajout impossible");
            alert.setContentText(exception.getMessage());
            alert.showAndWait();
        }
    }

    private edu.PROJETPI.entites.Produit toCartProduct(Produit produit) {
        return new edu.PROJETPI.entites.Produit(
                produit.getId(),
                value(produit.getNom(), "Produit"),
                getDisplayedPrice(produit),
                produit.getStock(),
                value(produit.getDescription(), "Produit de la boutique Esportify.")
        );
    }

    private double getDisplayedPrice(Produit produit) {
        return offerActive ? produit.getPrix() * 0.9 : produit.getPrix();
    }

    private void openCartView() {
        if (parentController != null) {
            parentController.showOrders();
            return;
        }
        AppNavigator.goToUserHome(AppSession.UserHomeSection.ORDERS);
    }

    private void applyMainWindowMode(Stage stage) {
        if (stage == null) {
            return;
        }
        stage.setResizable(true);
        stage.setMinWidth(1200);
        stage.setMinHeight(760);
        stage.setFullScreen(false);
        stage.setMaximized(true);
        javafx.application.Platform.runLater(() -> {
            stage.setFullScreen(false);
            stage.setMaximized(true);
        });
    }

    private ImageView buildProductImageView(Produit produit) {
        String imageSource = resolveProductImageSource(produit);
        if (imageSource == null) {
            return null;
        }
        try {
            Image image = new Image(imageSource, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(300);
            imageView.setFitHeight(160);
            imageView.setPreserveRatio(false);
            imageView.getStyleClass().add("product-image-view");
            return imageView;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private String resolveProductImageSource(Produit produit) {
        String imagePath = produit.getImage();
        if (imagePath != null) {
            imagePath = imagePath.trim();
        }
        if (imagePath != null && !imagePath.isBlank() && !"placeholder.png".equalsIgnoreCase(imagePath)) {
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://") || imagePath.startsWith("file:")) {
                return imagePath;
            }
            URL resource = getClass().getResource(imagePath.startsWith("/") ? imagePath : "/" + imagePath);
            if (resource != null) {
                return resource.toExternalForm();
            }
            File file = new File(imagePath);
            if (file.exists()) {
                return file.toURI().toString();
            }
        }

        String fallback = FALLBACK_IMAGES[Math.floorMod(produit.getId(), FALLBACK_IMAGES.length)];
        URL fallbackResource = getClass().getResource(fallback);
        return fallbackResource == null ? null : fallbackResource.toExternalForm();
    }

    private List<Produit> demoProducts() {
        List<Produit> produits = new ArrayList<>();
        produits.add(new Produit(1, "carte mere hytts", 100, 12, "Une base solide pour config gaming.", "/images/logo3.png", true, "actif", 0, 0, 0, "", "", ""));
        produits.add(new Produit(2, "carte mere ttht7410", 140, 9, "Carte mere fiable pour setup competitif.", "/images/logo5.png", true, "actif", 0, 0, 0, "", "", ""));
        produits.add(new Produit(3, "pc gamer mpla", 140, 6, "Configuration gaming prete pour la scene e-sport.", "/images/esportify-card.jpg", true, "actif", 0, 0, 0, "", "", ""));
        produits.add(new Produit(4, "pc gamer", 1405, 3, "Tour premium pour joueurs exigeants.", "/images/gaming.jpg", true, "actif", 0, 0, 0, "", "", ""));
        return produits;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String value(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
