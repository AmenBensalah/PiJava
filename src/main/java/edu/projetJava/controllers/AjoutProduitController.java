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
        
        Label details = new Label("Voir détails");
        details.getStyleClass().add("link-details");
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        Button cartBtn = new Button("Ajouter");
        cartBtn.getStyleClass().add("btn-cart");
        
        actionBox.getChildren().addAll(details, spacer2, cartBtn);

        card.getChildren().addAll(imgArea, title, priceStockBox, actionBox);
        return card;
    }
}
