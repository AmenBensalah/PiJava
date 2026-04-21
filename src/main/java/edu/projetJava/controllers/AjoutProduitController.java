package edu.projetJava.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import edu.projetJava.entities.Categorie;
import edu.projetJava.models.Produit;
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
    @FXML private VBox recoOverlay;
    @FXML private HBox recoCardsContainer;
    @FXML private TextField inputFideliteEmail;
    @FXML private Label lblGlobalPromo;

    private ProduitService produitService = new ProduitService();
    private CategorieService categorieService = new CategorieService();
    private edu.projetJava.services.CommandeService commandeService = new edu.projetJava.services.CommandeService();
    private Integer activeCategoryId = null;
    private boolean isGlobalOfferActive = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        chargerCategories();
        chargerProduits();
    }

    @FXML
    void activerOffreGlobale(ActionEvent event) {
        String email = inputFideliteEmail.getText();
        if (email == null || !email.contains("@")) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Email invalide");
            alert.setContentText("Veuillez entrer une adresse e-mail valide.");
            alert.show();
            return;
        }

        if (commandeService.aDejaCommande(email)) {
            isGlobalOfferActive = true;
            lblGlobalPromo.setVisible(true);
            lblGlobalPromo.setText("✓ -10% activés !");
            chargerProduits(); // Recharge les produits pour afficher les prix remisés
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Fidélité Récompensée");
            alert.setContentText("Merci pour votre fidélité !\nTous les prix affichent maintenant -10%.");
            alert.show();
        } else {
            isGlobalOfferActive = false;
            lblGlobalPromo.setVisible(true);
            lblGlobalPromo.setText("❌ Aucune commande passée");
            lblGlobalPromo.setStyle("-fx-text-fill: #ff0055; -fx-font-weight: bold;");
            chargerProduits();
        }
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
        
        if (isGlobalOfferActive) {
            double discountedPrice = p.getPrix() * 0.9;
            Label oldPrice = new Label(p.getPrix() + " €");
            oldPrice.setStyle("-fx-text-fill: #94a3b8; -fx-strikethrough: true; -fx-font-size: 14px;");
            
            Label newPrice = new Label(String.format("%.2f €", discountedPrice));
            newPrice.getStyleClass().add("card-price");
            newPrice.setStyle("-fx-text-fill: #00ffaa; -fx-effect: dropshadow(one-pass-box, #00ffaa, 5, 0, 0, 0);");
            
            priceStockBox.getChildren().addAll(newPrice, oldPrice);
        } else {
            Label price = new Label(p.getPrix() + " €");
            price.getStyleClass().add("card-price");
            priceStockBox.getChildren().add(price);
        }
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        boolean isEnStock = p.getStock() > 0;
        Label stock = new Label(isEnStock ? "EN STOCK" : "RUPTURE");
        stock.getStyleClass().add(isEnStock ? "badge-stock" : "badge-rupture");
        
        priceStockBox.getChildren().addAll(spacer, stock);

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
        cartBtn.setOnAction(e -> ouvrirFenetrePaiement(p));
        
        actionBox.getChildren().addAll(youtubeBtn, spacer2, cartBtn);

        card.getChildren().addAll(imgArea, title, priceStockBox, actionBox);
        return card;
    }

    private void ouvrirFenetrePaiement(Produit p) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/paiement.fxml"));
            Parent root = loader.load();

            PaiementController controller = loader.getController();
            controller.initData(p);
            
            if (isGlobalOfferActive && inputFideliteEmail != null) {
                controller.setGlobalDiscount(true, inputFideliteEmail.getText());
            }

            Stage stage = new Stage();
            stage.setTitle("Paiement - " + p.getNom());
            stage.setScene(new Scene(root));
            stage.setFullScreen(true);
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Impossible d'ouvrir l'interface de paiement : " + e.getMessage());
            alert.show();
        }
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

    @FXML
    void voirRecommandationsIA(ActionEvent event) {
        try {
            edu.projetJava.services.RecommandationIAService aiService = new edu.projetJava.services.RecommandationIAService();
            java.util.Map<Produit, Integer> ventesReelles = aiService.getRecommandationsReelles();
            
            // Trier les produits par nombre de ventes décroissant
            List<Produit> topProduits = ventesReelles.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .map(java.util.Map.Entry::getKey)
                .limit(5)
                .collect(Collectors.toList());
            
            recoCardsContainer.getChildren().clear();
            
            int maxVentes = ventesReelles.values().stream().max(Integer::compare).orElse(1);
            if (maxVentes == 0) maxVentes = 1;
            
            for (Produit p : topProduits) {
                int ventes = ventesReelles.get(p);
                
                // Calcul des étoiles (sur 5)
                int nbEtoiles = (int) Math.round(((double) ventes / maxVentes) * 5.0);
                if (ventes == 0) nbEtoiles = 1; // Minimum 1 étoile même si pas vendu
                if (nbEtoiles > 5) nbEtoiles = 5;
                
                String stars = "⭐".repeat(nbEtoiles);
                
                VBox card = createProductCard(p);
                // 3D Effect : Épaisse bordure brillante avec dégradé et ombres multiples
                card.setStyle("-fx-background-color: linear-gradient(to bottom, #11111a, #0d0d14); -fx-border-color: linear-gradient(to bottom right, #ff0055, #00e5ff); -fx-border-width: 3px; -fx-border-radius: 15px; -fx-background-radius: 15px; -fx-effect: dropshadow(three-pass-box, rgba(0, 229, 255, 0.6), 25, 0.2, 0, 0);");
                card.setPrefWidth(260); // Plus large pour l'effet 3D
                
                Label statsLabel = new Label(stars + " (" + ventes + " Ventes)");
                statsLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 5 0 0 0;");
                
                card.getChildren().add(2, statsLabel); // Insérer sous le titre
                
                recoCardsContainer.getChildren().add(card);
            }
            
            recoOverlay.setVisible(true);
            
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur IA");
            alert.setHeaderText("Un problème est survenu");
            alert.setContentText("Impossible de charger les recommandations : " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void closeRecommandations(ActionEvent event) {
        if (recoOverlay != null) {
            recoOverlay.setVisible(false);
        }
    }

}
