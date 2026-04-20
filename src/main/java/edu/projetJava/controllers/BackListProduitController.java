package edu.projetJava.controllers;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import edu.projetJava.entities.Categorie;
import edu.projetJava.entities.Produit;
import edu.projetJava.services.CategorieService;
import edu.projetJava.services.ProduitService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BackListProduitController implements Initializable {

    @FXML private VBox tableContainer;
    @FXML private HBox categoryPillsContainer;
    @FXML private TextField searchNom;
    @FXML private TextField searchPrixMin;
    @FXML private TextField searchPrixMax;
    @FXML private ComboBox<String> searchStatut;

    private ProduitService produitService = new ProduitService();
    private CategorieService categorieService = new CategorieService();

    private String currentSortMode = "none";
    private Integer activeCategoryId = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (searchStatut != null) {
            searchStatut.getItems().addAll("Tous", "Disponible", "Rupture");
            searchStatut.getSelectionModel().select(0);
        }
        chargerCategoriesPills();
        chargerProduits();
    }

    @FXML
    void chargerCategoriesPills() {
        try {
            categoryPillsContainer.getChildren().clear();
            addPill("Tous", null);
            List<Categorie> cats = categorieService.recuperer();
            for (Categorie c : cats) {
                addPill(c.getNom(), c.getId());
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void addPill(String label, Integer catId) {
        Label pill = new Label(label);
        pill.getStyleClass().add("cat-pill");
        if ((catId == null && activeCategoryId == null) || (catId != null && catId.equals(activeCategoryId))) {
            pill.getStyleClass().add("cat-pill-active");
        }
        pill.setOnMouseClicked(e -> {
            activeCategoryId = catId;
            chargerCategoriesPills();
            appliquerFiltres();
        });
        categoryPillsContainer.getChildren().add(pill);
    }

    @FXML
    void appliquerFiltres() {
        try {
            tableContainer.getChildren().clear();
            List<Produit> produits = produitService.recuperer();
            List<Categorie> allCats = categorieService.recuperer();

            List<Produit> filtered = produits.stream().filter(p -> {
                boolean match = true;
                if (activeCategoryId != null && p.getCategorieId() != activeCategoryId) match = false;
                if (searchNom != null && !searchNom.getText().isEmpty()) {
                    if (!p.getNom().toLowerCase().contains(searchNom.getText().toLowerCase())) match = false;
                }
                if (searchPrixMin != null && !searchPrixMin.getText().isEmpty()) {
                    try { if (p.getPrix() < Integer.parseInt(searchPrixMin.getText())) match = false; } catch (Exception ignored) {}
                }
                if (searchPrixMax != null && !searchPrixMax.getText().isEmpty()) {
                    try { if (p.getPrix() > Integer.parseInt(searchPrixMax.getText())) match = false; } catch (Exception ignored) {}
                }
                if (searchStatut != null && searchStatut.getValue() != null && !searchStatut.getValue().equals("Tous")) {
                    boolean isDisp = p.getStock() > 0;
                    if (searchStatut.getValue().equals("Disponible") && !isDisp) match = false;
                    if (searchStatut.getValue().equals("Rupture") && isDisp) match = false;
                }
                return match;
            }).collect(Collectors.toList());

            if (currentSortMode.equals("prix_asc")) filtered.sort(java.util.Comparator.comparingInt(Produit::getPrix));
            else if (currentSortMode.equals("prix_desc")) filtered.sort((p1, p2) -> Integer.compare(p2.getPrix(), p1.getPrix()));
            else if (currentSortMode.equals("stock")) filtered.sort((p1, p2) -> Integer.compare(p2.getStock(), p1.getStock()));

            for (Produit p : filtered) {
                String cName = allCats.stream().filter(c -> c.getId() == p.getCategorieId()).map(Categorie::getNom).findFirst().orElse("Aucune");
                tableContainer.getChildren().add(createTableRowWithCat(p, cName));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private HBox createTableRowWithCat(Produit p, String catName) {
        return createTableRow(p);
    }

    @FXML void triPrixAsc() { currentSortMode = "prix_asc"; appliquerFiltres(); }
    @FXML void triPrixDesc() { currentSortMode = "prix_desc"; appliquerFiltres(); }
    @FXML void triStock() { currentSortMode = "stock"; appliquerFiltres(); }

    @FXML private Label statTotal;
    @FXML private Label statValeur;
    @FXML private Label statRupture;
    @FXML private javafx.scene.chart.PieChart statChartProduit;

    @FXML void chargerProduits() {
        if(searchNom != null) searchNom.clear();
        if(searchPrixMin != null) searchPrixMin.clear();
        if(searchPrixMax != null) searchPrixMax.clear();
        if(searchStatut != null) searchStatut.getSelectionModel().select(0);
        activeCategoryId = null;
        currentSortMode = "none";
        chargerCategoriesPills();
        appliquerFiltres();
        mettreAJourStatistiques();
    }

    private void mettreAJourStatistiques() {
        try {
            List<Produit> tous = produitService.recuperer();
            int total = tous.size();
            long rupture = tous.stream().filter(p -> p.getStock() <= 0).count();
            
            // Calcul exact de la valeur totale en stock : (Prix * Quantité) pour tous les produits dispo
            long valeurCatalogue = tous.stream()
                .filter(p -> p.getStock() > 0)
                .mapToLong(p -> (long)(p.getPrix() * p.getStock()))
                .sum();

            if (statTotal != null) statTotal.setText(String.valueOf(total));
            if (statValeur != null) statValeur.setText(valeurCatalogue + " €");
            if (statRupture != null) statRupture.setText(String.valueOf(rupture));

            if (statChartProduit != null) {
                statChartProduit.getData().clear();
                
                // Trie les produits par stock (décroissant) et prend les 10 plus gros (ou tous si moins de 10)
                tous.stream()
                    .filter(p -> p.getStock() > 0)
                    .sorted((p1, p2) -> Integer.compare(p2.getStock(), p1.getStock()))
                    .limit(10)
                    .forEach(p -> {
                        javafx.scene.chart.PieChart.Data slice = new javafx.scene.chart.PieChart.Data(p.getNom(), p.getStock());
                        statChartProduit.getData().add(slice);
                    });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private HBox createTableRow(Produit p) {
        HBox row = new HBox();
        row.getStyleClass().add("table-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setSpacing(10);
        
        // --- MODERN HOVER EFFECT ---
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(0, 229, 255, 0.05); -fx-border-color: transparent transparent transparent #00e5ff; -fx-border-width: 0 0 0 4;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;"));
        row.setCursor(javafx.scene.Cursor.HAND);

        Label lblId = new Label("#" + p.getId());
        lblId.setPrefWidth(50);
        lblId.getStyleClass().add("table-text-muted");

        Label lblNom = new Label(p.getNom());
        lblNom.setPrefWidth(250);
        lblNom.getStyleClass().add("table-text");

        String catName = "Aucune";
        try {
            List<Categorie> allCats = categorieService.recuperer();
            catName = allCats.stream().filter(c -> c.getId() == p.getCategorieId()).map(Categorie::getNom).findFirst().orElse("Inconnu");
        } catch (Exception ignored) {}

        Label lblCat = new Label(catName);
        lblCat.setPrefWidth(120);
        lblCat.getStyleClass().add("cat-pill");
        lblCat.setStyle("-fx-font-size: 10px; -fx-padding: 3 8;");

        Label lblPrix = new Label(p.getPrix() + " €");
        lblPrix.setPrefWidth(80);
        lblPrix.getStyleClass().add("table-text");

        Label lblStock = new Label(String.valueOf(p.getStock()));
        lblStock.setPrefWidth(80);
        lblStock.getStyleClass().add("table-text");

        Label lblStatut = new Label((p.getStock() > 0) ? "DISPONIBLE" : "RUPTURE");
        lblStatut.setPrefWidth(120);
        lblStatut.getStyleClass().add(p.getStock() > 0 ? "badge-payee" : "badge-annulee"); // Utilise les nouveaux styles de badges

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionsBox = new HBox(12);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button btnView = new Button("👁");
        btnView.getStyleClass().add("action-btn-small");
        btnView.setOnAction(e -> afficherInfosProduit(p));
        
        Button btnEdit = new Button("✏");
        btnEdit.getStyleClass().add("action-btn-small");
        btnEdit.setOnAction(e -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/backAjoutProduit.fxml"));
                Parent root = loader.load();
                BackAjoutProduitController formController = loader.getController();
                formController.initData(p);
                Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setFullScreen(true);
            } catch (IOException ex) { ex.printStackTrace(); }
        });

        Button btnPdf = new Button("📄");
        btnPdf.getStyleClass().addAll("action-btn-small");
        btnPdf.setOnAction(e -> exporterPDF(p));
        
        Button btnDelete = new Button("🗑");
        btnDelete.getStyleClass().addAll("action-btn-small", "action-btn-delete");
        btnDelete.setOnAction(e -> {
            try {
                produitService.supprimer(p.getId());
                chargerProduits();
                System.out.println("Produit supprimé : " + p.getNom());
            } catch (SQLException ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de suppression");
                alert.setHeaderText("Action impossible");
                alert.setContentText("Ce produit ne peut pas être supprimé car il est lié à d'autres données (commandes, favoris, etc.).");
                alert.show();
            }
        });

        actionsBox.getChildren().addAll(btnView, btnEdit, btnPdf, btnDelete);
        row.getChildren().addAll(lblId, lblNom, lblCat, lblPrix, lblStock, lblStatut, spacer, actionsBox);
        return row;
    }

    private void afficherInfosProduit(Produit p) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails du Produit");
        alert.setHeaderText(p.getNom());
        alert.setContentText("ID: " + p.getId() + "\n" +
                           "Prix: " + p.getPrix() + " €\n" +
                           "Stock: " + p.getStock() + "\n" +
                           "Statut: " + p.getStatut() + "\n" +
                           "Description: " + p.getDescription());
        alert.showAndWait();
    }

    private void exporterPDF(Produit p) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Télécharger la fiche produit");
        fileChooser.setInitialFileName("Produit_" + p.getNom().replaceAll(" ", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Document PDF", "*.pdf"));
        
        java.io.File file = fileChooser.showSaveDialog(null);
        
        if (file != null) {
            Document document = new Document();
            try {
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();
                document.add(new Paragraph("      E-SPORTIFY - FICHE PRODUIT"));
                document.add(new Paragraph("-------------------------------------------------"));
                document.add(new Paragraph("NOM : " + p.getNom()));
                document.add(new Paragraph("PRIX : " + p.getPrix() + " EUR"));
                document.add(new Paragraph("STOCK : " + p.getStock()));
                document.add(new Paragraph("STATUT : " + p.getStatut()));
                document.add(new Paragraph("DESCRIPTION : " + p.getDescription()));
                document.close();
                
                System.out.println("PDF téléchargé à : " + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void viderBoutique() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Nettoyage Complet");
        alert.setHeaderText("Êtes-vous sûr de vouloir tout supprimer ?");
        alert.setContentText("Cette action supprimera TOUS les produits de la base de données. C'est irréversible.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    produitService.viderTouteLaTable();
                    chargerProduits();
                    System.out.println("La table produit a été vidée.");
                } catch (SQLException e) {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setContentText("Erreur : Impossible de vider la table (liens avec d'autres tables).");
                    err.show();
                }
            }
        });
    }

    @FXML
    void supprimerDisponibles() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Nettoyage Ciblé");
        alert.setHeaderText("Supprimer uniquement les produits disponibles ?");
        alert.setContentText("Cette action supprimera tous les produits ayant un stock > 0.");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    List<Produit> produits = produitService.recuperer();
                    int count = 0;
                    for (Produit p : produits) {
                        if (p.getStock() > 0) {
                            produitService.supprimer(p.getId());
                            count++;
                        }
                    }
                    chargerProduits();
                    System.out.println(count + " produits disponibles supprimés.");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    void goToCreateForm(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/backAjoutProduit.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
    }

    @FXML
    void goToFrontOffice(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ajoutProduit.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
    }

    @FXML
    void goToMailing(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/backMailing.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
    }

    @FXML
    void goToAdminCategorie(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/backListCategorie.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setFullScreen(true);
    }
}
