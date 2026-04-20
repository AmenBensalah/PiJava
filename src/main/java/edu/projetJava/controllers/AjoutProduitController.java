package edu.projetJava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import edu.projetJava.entities.Categorie;
import edu.projetJava.entities.Produit;
import edu.projetJava.services.CategorieService;
import edu.projetJava.services.ProduitService;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AjoutProduitController implements Initializable {

    @FXML private FlowPane produitsContainer;
    @FXML private HBox categoryPillsContainer;
    @FXML private TextField searchNom;
    @FXML private ComboBox<Categorie> searchCat;

    private ProduitService produitService = new ProduitService();
    private CategorieService categorieService = new CategorieService();
    private Integer activeCategoryId = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chargerCategories();
        chargerProduits();
    }

    private void chargerCategories() {
        try {
            searchCat.getItems().clear();
            searchCat.getItems().addAll(categorieService.recuperer());
            
            categoryPillsContainer.getChildren().clear();
            addPill("Tous", null);
            for (Categorie c : categorieService.recuperer()) {
                addPill(c.getNom(), c.getId());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addPill(String label, Integer catId) {
        Button pill = new Button(label.toUpperCase());
        // Utilisation de Objects.equals pour la comparaison d'objets Integer
        boolean isActive = (catId == null && activeCategoryId == null) || (catId != null && catId.equals(activeCategoryId));
        pill.getStyleClass().add(isActive ? "cat-pill-active" : "cat-pill");
        
        pill.setOnAction(e -> {
            activeCategoryId = catId;
            // On désélectionne le ComboBox pour ne pas créer de conflit
            if (searchCat != null) searchCat.setValue(null);
            chargerCategories(); // Recharge pour mettre à jour les styles des pilules
            appliquerFiltres();
        });
        categoryPillsContainer.getChildren().add(pill);
    }

    private String triActuel = "none";

    @FXML
    void trierPrixCroissant() {
        triActuel = "prixAsc";
        appliquerFiltres();
    }

    @FXML
    void trierPrixDecroissant() {
        triActuel = "prixDesc";
        appliquerFiltres();
    }

    @FXML
    void appliquerFiltres() {
        try {
            produitsContainer.getChildren().clear();
            List<Produit> produits = produitService.recuperer();

            java.util.stream.Stream<Produit> stream = produits.stream().filter(p -> {
                boolean match = true;
                // Filtrage par Pilule
                if (activeCategoryId != null && p.getCategorieId() != activeCategoryId) match = false;
                
                // Filtrage par ComboBox (si sélectionné)
                if (searchCat != null && searchCat.getValue() != null && p.getCategorieId() != searchCat.getValue().getId()) match = false;
                
                // Filtrage par Nom
                if (searchNom.getText() != null && !searchNom.getText().isEmpty() && !p.getNom().toLowerCase().contains(searchNom.getText().toLowerCase())) match = false;
                return match;
            });

            // --- TRI MODERNE ---
            if (triActuel.equals("prixAsc")) {
                stream = stream.sorted((p1, p2) -> Integer.compare(p1.getPrix(), p2.getPrix()));
            } else if (triActuel.equals("prixDesc")) {
                stream = stream.sorted((p1, p2) -> Integer.compare(p2.getPrix(), p1.getPrix()));
            }

            List<Produit> filtered = stream.collect(Collectors.toList());

            for (Produit p : filtered) {
                produitsContainer.getChildren().add(createProductCard(p));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void chargerProduits() {
        if(searchNom != null) searchNom.clear();
        if(searchCat != null) searchCat.setValue(null);
        activeCategoryId = null;
        triActuel = "none";
        appliquerFiltres();
    }

    @FXML
    void goToAdmin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/backListProduit.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setFullScreen(true); // Persiste le mode plein écran
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private VBox createProductCard(Produit p) {
        VBox card = new VBox();
        card.setPrefWidth(220.0);
        card.setSpacing(8.0);
        card.getStyleClass().add("product-card");

        AnchorPane imgArea = new AnchorPane();
        imgArea.setPrefHeight(130.0);
        imgArea.getStyleClass().add("product-card-img-area");

        if (p.getImage() != null && !p.getImage().isEmpty() && !p.getImage().equals("placeholder.png")) {
            try {
                String urlPath = p.getImage();
                if (!urlPath.startsWith("http") && !urlPath.startsWith("file:")) {
                    urlPath = new java.io.File(urlPath).toURI().toString();
                }
                javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(urlPath));
                imgView.setFitWidth(220);
                imgView.setFitHeight(130);
                imgView.setPreserveRatio(false);
                imgArea.getChildren().add(imgView);
            } catch (Exception ex) { }
        }

        Label title = new Label(p.getNom());
        title.getStyleClass().add("card-title");

        HBox priceStockBox = new HBox();
        priceStockBox.setAlignment(Pos.CENTER_LEFT);
        priceStockBox.setSpacing(10.0);
        
        Label price = new Label(p.getPrix() + " €");
        price.getStyleClass().add("card-price");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        boolean isEnStock = p.getStock() > 0;
        Label stock = new Label(isEnStock ? "EN STOCK" : "RUPTURE");
        stock.getStyleClass().add(isEnStock ? "badge-stock" : "badge-rupture");
        
        priceStockBox.getChildren().addAll(price, spacer, stock);

        HBox actionBox = new HBox();
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setSpacing(10.0);
        
        Button youtubeBtn = new Button("▶ Tuto Install");
        youtubeBtn.setStyle("-fx-background-color: #e52d27; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5px; -fx-cursor: hand; -fx-font-size: 10px;");
        youtubeBtn.setOnAction(e -> {
            try {
                String query = "comment installer " + p.getNom();
                String url = "https://www.youtube.com/results?search_query=" + java.net.URLEncoder.encode(query, "UTF-8");
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        Button cartBtn = new Button("Ajouter");
        cartBtn.getStyleClass().add("btn-cart");
        
        actionBox.getChildren().addAll(youtubeBtn, spacer2, cartBtn);

        card.getChildren().addAll(imgArea, title, priceStockBox, actionBox);
        return card;
    }

    // --- CHATBOT GEMINI INTEGRATION ---
    @FXML private VBox chatbotWindow;
    @FXML private VBox chatMessages;
    @FXML private TextField chatInput;

    @FXML
    void toggleChatbot(ActionEvent event) {
        chatbotWindow.setVisible(!chatbotWindow.isVisible());
        if (chatbotWindow.isVisible() && chatMessages.getChildren().isEmpty()) {
            addMessageBubble("IA Assistant", "Bonjour! Je suis l'assistant E-SPORTIFY. Comment puis-je vous aider aujourd'hui?", "-fx-background-color: #1a1a2e; -fx-border-color: linear-gradient(to right, #8a2be2, #4a00e0); -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(138,43,226,0.6), 15, 0, 0, 0); -fx-text-fill: white;");
        }
    }

    @FXML
    void sendChatMessage(ActionEvent event) {
        String text = chatInput.getText().trim();
        if(text.isEmpty()) return;
        
        addMessageBubble("Vous", text, "-fx-background-color: #0f3443; -fx-border-color: #00e5ff; -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(0,229,255,0.7), 15, 0, 0, 0); -fx-text-fill: white;");
        chatInput.clear();
        
        // Appeler Gemini API de manière asynchrone pour ne pas bloquer l'interface
        new Thread(() -> {
            String response = edu.projetJava.services.GeminiAIService.getResponse(text);
            javafx.application.Platform.runLater(() -> {
                addMessageBubble("IA Assistant", response, "-fx-background-color: #1a1a2e; -fx-border-color: linear-gradient(to right, #8a2be2, #4a00e0); -fx-border-width: 1px; -fx-effect: dropshadow(three-pass-box, rgba(138,43,226,0.6), 15, 0, 0, 0); -fx-text-fill: white;");
            });
        }).start();
    }

    private void addMessageBubble(String sender, String text, String style) {
        VBox bubbleBox = new VBox(5);
        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #aaa;");
        
        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(260); // Evite de depasser la largeur de la fenetre
        messageLabel.setStyle(style + " -fx-padding: 10; -fx-background-radius: 10;");
        
        if (sender.equals("Vous")) {
            bubbleBox.setAlignment(Pos.CENTER_RIGHT);
        } else {
            bubbleBox.setAlignment(Pos.CENTER_LEFT);
        }
        
        bubbleBox.getChildren().addAll(senderLabel, messageLabel);
        chatMessages.getChildren().add(bubbleBox);
    }
}
