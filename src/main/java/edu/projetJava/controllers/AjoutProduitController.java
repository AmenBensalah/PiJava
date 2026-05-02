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
import edu.PROJETPI.services.OrderSession;
import edu.PROJETPI.tools.AlertUtils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import edu.ProjetPI.controllers.DashboardSession;
import edu.ProjetPI.controllers.SceneManager;

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
        // Chargement asynchrone pour éviter de bloquer l'UI au lancement
        CompletableFuture.runAsync(() -> {
            chargerCategories();
            chargerProduits();
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
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
            List<Categorie> categories = categorieService.recuperer();
            javafx.application.Platform.runLater(() -> {
                searchCat.getItems().clear();
                searchCat.getItems().addAll(categories);
                
                categoryPillsContainer.getChildren().clear();
                addPill("Tous", null);
                for (Categorie c : categories) {
                    addPill(c.getNom(), c.getId());
                }
            });
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
        CompletableFuture.runAsync(() -> {
            try {
                List<Produit> produits = produitService.recuperer();
                javafx.application.Platform.runLater(() -> {
                    produitsContainer.getChildren().clear();
                    java.util.stream.Stream<Produit> stream = produits.stream().filter(p -> {
                        boolean match = true;
                        if (activeCategoryId != null && p.getCategorieId() != activeCategoryId) match = false;
                        if (searchCat != null && searchCat.getValue() != null && p.getCategorieId() != searchCat.getValue().getId()) match = false;
                        if (searchNom.getText() != null && !searchNom.getText().isEmpty() && !p.getNom().toLowerCase().contains(searchNom.getText().toLowerCase())) match = false;
                        return match;
                    });

                    if (triActuel.equals("prixAsc")) {
                        stream = stream.sorted((p1, p2) -> Integer.compare(p1.getPrix(), p2.getPrix()));
                    } else if (triActuel.equals("prixDesc")) {
                        stream = stream.sorted((p1, p2) -> Integer.compare(p2.getPrix(), p1.getPrix()));
                    }

                    List<Produit> filtered = stream.collect(Collectors.toList());
                    for (Produit p : filtered) {
                        produitsContainer.getChildren().add(createProductCard(p));
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        });
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
        SceneManager.switchScene("/backListProduit.fxml", "Boutique Admin Dashboard");
    }

    @FXML
    void handleLogout(ActionEvent event) {
        DashboardSession.clear();
        SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "E-SPORTIFY : Connexion");
    }

    @FXML
    void goToCart(ActionEvent event) {
        SceneManager.switchScene("/lignecommande-view.fxml", "Panier");
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
        cartBtn.setDisable(!isEnStock);
        cartBtn.setOnAction(e -> ajouterAuPanier(p));
        
        actionBox.getChildren().addAll(youtubeBtn, spacer2, cartBtn);

        card.getChildren().addAll(imgArea, title, priceStockBox, actionBox);
        return card;
    }

    private void ajouterAuPanier(Produit p) {
        if (p.getStock() <= 0) {
            AlertUtils.showError("Ce produit est en rupture de stock.");
            return;
        }

        edu.PROJETPI.entites.Produit produitPanier = new edu.PROJETPI.entites.Produit(
                p.getId(),
                p.getNom(),
                getPrixPanier(p),
                p.getStock(),
                p.getDescription()
        );

        OrderSession.getInstance().addProduct(produitPanier, 1);
        AlertUtils.showSuccess(p.getNom() + " a ete ajoute au panier.");
        SceneManager.switchScene("/lignecommande-view.fxml", "Panier");
    }

    private double getPrixPanier(Produit p) {
        if (isGlobalOfferActive) {
            return p.getPrix() * 0.9;
        }
        return p.getPrix();
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
            // Lier la fenêtre modale à la fenêtre principale pour qu'elle s'affiche par-dessus
            if (produitsContainer != null && produitsContainer.getScene() != null) {
                stage.initOwner(produitsContainer.getScene().getWindow());
                stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            }
            
            stage.setTitle("Paiement - " + p.getNom());
            stage.setScene(new Scene(root)); // Création d'une nouvelle scène (essentiel)
            // On ne la met pas en plein écran pour la différencier, c'est une modale (popup)
            stage.showAndWait();
            
            // Une fois l'achat terminé et la fenêtre fermée, on actualise les recommandations si elles sont ouvertes
            if (recoOverlay != null && recoOverlay.isVisible()) {
                voirRecommandationsIA(null); // Rafraîchit les scores
            }
            
        } catch (Exception e) {
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
        new Thread(() -> {
            try {
                edu.projetJava.services.RecommandationIAService aiService = new edu.projetJava.services.RecommandationIAService();
                java.util.Map<String, Object> data = aiService.getRecommandationsAvancees();
                
                javafx.application.Platform.runLater(() -> {
                    try {
                        @SuppressWarnings("unchecked")
                        java.util.Map<Produit, java.util.Map<String, String>> rawProduitsStats = 
                            (java.util.Map<Produit, java.util.Map<String, String>>) data.get("produits");
                            
                        final java.util.Map<Produit, java.util.Map<String, String>> produitsStats = 
                            (rawProduitsStats == null) ? new java.util.HashMap<>() : rawProduitsStats;
                        
                        // Trier les produits par nombre de ventes décroissant
                        List<Produit> topProduits = produitsStats.keySet().stream()
                            .sorted((p1, p2) -> {
                                int v1 = Integer.parseInt(produitsStats.get(p1).getOrDefault("ventes", "0"));
                                int v2 = Integer.parseInt(produitsStats.get(p2).getOrDefault("ventes", "0"));
                                return Integer.compare(v2, v1);
                            })
                            .limit(5)
                            .collect(Collectors.toList());
                        
                        recoCardsContainer.getChildren().clear();
                        
                        int maxVentes = produitsStats.values().stream()
                            .mapToInt(m -> Integer.parseInt(m.getOrDefault("ventes", "0")))
                            .max().orElse(1);
                        if (maxVentes == 0) maxVentes = 1;
                        
                        for (Produit p : topProduits) {
                            java.util.Map<String, String> stats = produitsStats.get(p);
                            int ventes = Integer.parseInt(stats.getOrDefault("ventes", "0"));
                            String tendance = stats.getOrDefault("tendance", "Stable →");
                            String prediction = stats.getOrDefault("prediction", "0");
                            
                            // Le principe : 
                            // 10 ventes = 1 étoile
                            // 100 ventes = 5 étoiles
                            // Et on n'affiche QUE les étoiles gagnées (pas les étoiles vides)
                            int nbEtoiles = 0;
                            if (ventes >= 90) nbEtoiles = 5;
                            else if (ventes >= 70) nbEtoiles = 4;
                            else if (ventes >= 50) nbEtoiles = 3;
                            else if (ventes >= 30) nbEtoiles = 2;
                            else if (ventes >= 10) nbEtoiles = 1;
                            
                            String stars = "";
                            if (nbEtoiles > 0) {
                                stars = "⭐".repeat(nbEtoiles);
                            }
                            
                            VBox card = createProductCard(p);
                            // 3D Effect : Épaisse bordure brillante avec dégradé et ombres multiples
                            card.setStyle("-fx-background-color: linear-gradient(to bottom, #11111a, #0d0d14); -fx-border-color: linear-gradient(to bottom right, #ff0055, #00e5ff); -fx-border-width: 3px; -fx-border-radius: 15px; -fx-background-radius: 15px; -fx-effect: dropshadow(three-pass-box, rgba(0, 229, 255, 0.6), 25, 0.2, 0, 0);");
                            card.setPrefWidth(260); // Plus large pour l'effet 3D
                            
                            // Ajout des infos IA Avancées
                            VBox aiInfoBox = new VBox(5);
                            aiInfoBox.setStyle("-fx-padding: 10 0 5 0; -fx-border-color: #334155; -fx-border-width: 1 0 0 0; -fx-margin-top: 10;");
                            
                            int score = ventes;
                            Label statsLabel = new Label("Score: " + score + " " + stars);
                            statsLabel.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold; -fx-font-size: 13px;");
                            
                            Label trendLabel = new Label("Tendance: " + tendance);
                            trendLabel.setStyle("-fx-text-fill: #00e5ff; -fx-font-size: 11px;");
                            
                            Label predLabel = new Label("Prédiction M+1: " + prediction + " unités");
                            predLabel.setStyle("-fx-text-fill: #bd00ff; -fx-font-weight: bold; -fx-font-size: 11px;");
                            
                            aiInfoBox.getChildren().addAll(statsLabel, trendLabel, predLabel);
                            
                            card.getChildren().add(2, aiInfoBox); // Insérer sous le titre
                            
                            recoCardsContainer.getChildren().add(card);
                        }
                        
                        recoOverlay.setVisible(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur IA");
                    alert.setHeaderText("Un problème est survenu");
                    alert.setContentText("Impossible de charger les recommandations : " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    @FXML
    void closeRecommandations(ActionEvent event) {
        if (recoOverlay != null) {
            recoOverlay.setVisible(false);
        }
    }

}
