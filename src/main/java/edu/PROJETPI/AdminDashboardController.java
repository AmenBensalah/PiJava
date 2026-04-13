package edu.PROJETPI;

import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.entites.LigneCommande;
import edu.PROJETPI.entites.Payment;
import edu.PROJETPI.entites.Produit;
import edu.PROJETPI.services.CatalogueProduitService;
import edu.PROJETPI.services.ServiceCommande;
import edu.PROJETPI.services.ServiceLigneCommande;
import edu.PROJETPI.services.ServicePayment;
import edu.PROJETPI.tools.AlertUtils;
import edu.PROJETPI.tools.SceneNavigator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    private static final String MENU_ACTIVE_CLASS = "side-menu-item-active";
    private static final SimpleDateFormat PAYMENT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final String FILTER_ALL = "Tous";

    @FXML
    private TableView<Commande> commandeTableView;
    @FXML
    private TableColumn<Commande, Integer> colId;
    @FXML
    private TableColumn<Commande, java.util.Date> colDate;
    @FXML
    private TableColumn<Commande, Double> colTotal;
    @FXML
    private TableColumn<Commande, Integer> colClientId;
    @FXML
    private TableColumn<Commande, String> colStatut;
    @FXML
    private TableColumn<Commande, Commande> colModifier;
    @FXML
    private TableColumn<Commande, Commande> colSupprimer;
    @FXML
    private Label commandeCountLabel;
    @FXML
    private VBox commandeFilterCard;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statutFilterComboBox;
    @FXML
    private Button sortIdDescButton;
    @FXML
    private Button sortIdAscButton;
    @FXML
    private Button sortMontantDescButton;
    @FXML
    private Button sortMontantAscButton;
    @FXML
    private Button sortDateDescButton;
    @FXML
    private Button sortDateAscButton;
    @FXML
    private TableView<PaymentRow> paymentTableView;
    @FXML
    private TableColumn<PaymentRow, Integer> paymentColId;
    @FXML
    private TableColumn<PaymentRow, Integer> paymentColCommandeId;
    @FXML
    private TableColumn<PaymentRow, String> paymentColMontant;
    @FXML
    private TableColumn<PaymentRow, String> paymentColStatut;
    @FXML
    private TableColumn<PaymentRow, String> paymentColDate;
    @FXML
    private TableColumn<PaymentRow, PaymentRow> paymentColActions;
    @FXML
    private Label pageTitleLabel;
    @FXML
    private Label sectionTitleLabel;
    @FXML
    private Button commandesButton;
    @FXML
    private Button paiementsButton;

    private final ServiceCommande serviceCommande = new ServiceCommande();
    private final ServicePayment servicePayment = new ServicePayment();
    private final ServiceLigneCommande serviceLigneCommande = new ServiceLigneCommande();
    private final CatalogueProduitService catalogueProduitService = new CatalogueProduitService();
    private final ObservableList<Commande> commandes = FXCollections.observableArrayList();
    private final FilteredList<Commande> filteredCommandes = new FilteredList<>(commandes, item -> true);
    private final SortedList<Commande> sortedCommandes = new SortedList<>(filteredCommandes);
    private final ObservableList<PaymentRow> payments = FXCollections.observableArrayList();
    private boolean paymentMode;
    private SortMode currentSortMode = SortMode.ID_DESC;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCommande"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colClientId.setCellValueFactory(new PropertyValueFactory<>("clientId"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colModifier.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
        colSupprimer.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));
        paymentColId.setCellValueFactory(new PropertyValueFactory<>("id"));
        paymentColCommandeId.setCellValueFactory(new PropertyValueFactory<>("commandeId"));
        paymentColMontant.setCellValueFactory(cellData ->
                new ReadOnlyStringWrapper(String.format("%.2f TND", cellData.getValue().getMontant())));
        paymentColStatut.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getStatut()));
        paymentColDate.setCellValueFactory(cellData -> new ReadOnlyStringWrapper(cellData.getValue().getDateLabel()));
        paymentColActions.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue()));

        commandeTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        paymentTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        initializeCommandFilters();
        configureCommandeStatusColumn();
        configureModifierColumn();
        configureSupprimerColumn();
        configurePaymentStatusColumn();
        configurePaymentActionsColumn();
        showCommandes();
    }

    @FXML
    private void goToCatalogue() {
        SceneNavigator.switchScene(commandeTableView, "/main-view.fxml", "Catalogue produits");
    }

    @FXML
    private void refreshData() {
        try {
            if (paymentMode) {
                payments.setAll(buildPaymentRows());
                paymentTableView.setItems(payments);
                commandeCountLabel.setText(String.valueOf(payments.size()));
            } else {
                commandes.setAll(serviceCommande.readAll());
                applyCommandFilters();
            }
        } catch (SQLException e) {
            String message = paymentMode
                    ? "Erreur lors du chargement des paiements : "
                    : "Erreur lors du chargement des commandes : ";
            AlertUtils.showError(message + e.getMessage());
        }
    }

    @FXML
    private void showCommandes() {
        paymentMode = false;
        pageTitleLabel.setText("Liste des commandes");
        sectionTitleLabel.setText("Liste des commandes");
        setActiveMenuButton(commandesButton, paiementsButton);
        commandeFilterCard.setManaged(true);
        commandeFilterCard.setVisible(true);
        commandeTableView.setManaged(true);
        commandeTableView.setVisible(true);
        paymentTableView.setManaged(false);
        paymentTableView.setVisible(false);
        refreshData();
    }

    @FXML
    private void showPaiements() {
        paymentMode = true;
        pageTitleLabel.setText("Liste des paiements");
        sectionTitleLabel.setText("Liste des paiements");
        setActiveMenuButton(paiementsButton, commandesButton);
        commandeFilterCard.setManaged(false);
        commandeFilterCard.setVisible(false);
        commandeTableView.setManaged(false);
        commandeTableView.setVisible(false);
        paymentTableView.setManaged(true);
        paymentTableView.setVisible(true);
        refreshData();
    }

    private void setActiveMenuButton(Button activeButton, Button inactiveButton) {
        if (!activeButton.getStyleClass().contains(MENU_ACTIVE_CLASS)) {
            activeButton.getStyleClass().add(MENU_ACTIVE_CLASS);
        }
        inactiveButton.getStyleClass().remove(MENU_ACTIVE_CLASS);
    }

    private void initializeCommandFilters() {
        statutFilterComboBox.setItems(FXCollections.observableArrayList(
                FILTER_ALL, "EN_ATTENTE", "PAYEE", "ANNULEE"
        ));
        statutFilterComboBox.setValue(FILTER_ALL);

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyCommandFilters());
        statutFilterComboBox.valueProperty().addListener((obs, oldValue, newValue) -> applyCommandFilters());
        filteredCommandes.addListener((javafx.collections.ListChangeListener<? super Commande>) change ->
                commandeCountLabel.setText(String.valueOf(filteredCommandes.size())));

        sortedCommandes.setComparator(buildComparator(currentSortMode));
        commandeTableView.setItems(sortedCommandes);
        updateSortButtons();
    }

    @FXML
    private void applyCommandFilters() {
        String searchValue = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedStatus = statutFilterComboBox.getValue() == null ? FILTER_ALL : statutFilterComboBox.getValue();

        filteredCommandes.setPredicate(commande -> matchesSearch(commande, searchValue) && matchesStatus(commande, selectedStatus));
        sortedCommandes.setComparator(buildComparator(currentSortMode));
        commandeCountLabel.setText(String.valueOf(filteredCommandes.size()));
    }

    @FXML
    private void resetCommandFilters() {
        searchField.clear();
        statutFilterComboBox.setValue(FILTER_ALL);
        currentSortMode = SortMode.ID_DESC;
        sortedCommandes.setComparator(buildComparator(currentSortMode));
        updateSortButtons();
        applyCommandFilters();
    }

    @FXML
    private void sortByIdDesc() {
        setSortMode(SortMode.ID_DESC);
    }

    @FXML
    private void sortByIdAsc() {
        setSortMode(SortMode.ID_ASC);
    }

    @FXML
    private void sortByMontantDesc() {
        setSortMode(SortMode.MONTANT_DESC);
    }

    @FXML
    private void sortByMontantAsc() {
        setSortMode(SortMode.MONTANT_ASC);
    }

    @FXML
    private void sortByDateDesc() {
        setSortMode(SortMode.DATE_DESC);
    }

    @FXML
    private void sortByDateAsc() {
        setSortMode(SortMode.DATE_ASC);
    }

    private void setSortMode(SortMode sortMode) {
        currentSortMode = sortMode;
        sortedCommandes.setComparator(buildComparator(sortMode));
        updateSortButtons();
    }

    private Comparator<Commande> buildComparator(SortMode sortMode) {
        return switch (sortMode) {
            case ID_ASC -> Comparator.comparingInt(Commande::getId);
            case ID_DESC -> Comparator.comparingInt(Commande::getId).reversed();
            case MONTANT_ASC -> Comparator.comparingDouble(Commande::getTotal);
            case MONTANT_DESC -> Comparator.comparingDouble(Commande::getTotal).reversed();
            case DATE_ASC -> Comparator.comparing(Commande::getDateCommande, Comparator.nullsLast(Comparator.naturalOrder()));
            case DATE_DESC -> Comparator.comparing(Commande::getDateCommande, Comparator.nullsLast(Comparator.reverseOrder()));
        };
    }

    private boolean matchesSearch(Commande commande, String searchValue) {
        if (searchValue.isBlank()) {
            return true;
        }

        return String.valueOf(commande.getId()).contains(searchValue)
                || String.valueOf(commande.getClientId()).contains(searchValue)
                || safe(commande.getStatut()).contains(searchValue)
                || safe(commande.getNom()).contains(searchValue)
                || safe(commande.getPrenom()).contains(searchValue)
                || String.valueOf(commande.getTotal()).contains(searchValue);
    }

    private boolean matchesStatus(Commande commande, String selectedStatus) {
        if (selectedStatus == null || FILTER_ALL.equalsIgnoreCase(selectedStatus)) {
            return true;
        }
        return selectedStatus.equalsIgnoreCase(safe(commande.getStatut()));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private void updateSortButtons() {
        updateSortButtonState(sortIdDescButton, currentSortMode == SortMode.ID_DESC);
        updateSortButtonState(sortIdAscButton, currentSortMode == SortMode.ID_ASC);
        updateSortButtonState(sortMontantDescButton, currentSortMode == SortMode.MONTANT_DESC);
        updateSortButtonState(sortMontantAscButton, currentSortMode == SortMode.MONTANT_ASC);
        updateSortButtonState(sortDateDescButton, currentSortMode == SortMode.DATE_DESC);
        updateSortButtonState(sortDateAscButton, currentSortMode == SortMode.DATE_ASC);
    }

    private void updateSortButtonState(Button button, boolean active) {
        button.getStyleClass().remove("sort-chip-button-active");
        if (active) {
            button.getStyleClass().add("sort-chip-button-active");
        }
    }

    private void configureCommandeStatusColumn() {
        colStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(item);
                badge.getStyleClass().add("commande-status-badge");

                String normalized = item.trim().toUpperCase();
                switch (normalized) {
                    case "PAYEE" -> badge.getStyleClass().add("commande-status-paid");
                    case "ANNULEE" -> badge.getStyleClass().add("commande-status-cancelled");
                    case "EN_ATTENTE" -> badge.getStyleClass().add("commande-status-pending");
                    default -> badge.getStyleClass().add("commande-status-neutral");
                }

                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void configurePaymentStatusColumn() {
        paymentColStatut.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(item);
                badge.getStyleClass().add("payment-status-badge");
                setGraphic(badge);
                setText(null);
            }
        });
    }

    private void configurePaymentActionsColumn() {
        paymentColActions.setCellFactory(column -> new TableCell<>() {
            private final Button viewButton = new Button("...");
            private final Button deleteButton = new Button("Suppr.");
            private final HBox actionsBox = new HBox(6, viewButton, deleteButton);

            {
                viewButton.getStyleClass().add("table-action-view");
                deleteButton.getStyleClass().add("table-action-delete");
                actionsBox.setFillHeight(false);
                actionsBox.setStyle("-fx-alignment: center;");
                setStyle("-fx-alignment: CENTER;");

                viewButton.setOnAction(event -> {
                    PaymentRow payment = getTableRow().getItem();
                    if (payment != null) {
                        showCommandeDetails(payment);
                    }
                });

                deleteButton.setOnAction(event -> {
                    PaymentRow payment = getTableRow().getItem();
                    if (payment != null) {
                        deletePayment(payment);
                    }
                });
            }

            @Override
            protected void updateItem(PaymentRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                deleteButton.setDisable(!item.hasRealPayment());
                setGraphic(actionsBox);
                setText(null);
            }
        });
    }

    private ObservableList<PaymentRow> buildPaymentRows() throws SQLException {
        Map<Integer, Payment> paymentsByCommandeId = new HashMap<>();
        for (Payment payment : servicePayment.readAll()) {
            paymentsByCommandeId.put(payment.getCommandeId(), payment);
        }

        ObservableList<PaymentRow> rows = FXCollections.observableArrayList();
        for (Commande commande : serviceCommande.readAll()) {
            String statut = commande.getStatut();
            if (statut == null || !"PAYEE".equalsIgnoreCase(statut.trim())) {
                continue;
            }

            Payment payment = paymentsByCommandeId.get(commande.getId());
            int displayId = payment != null ? payment.getId() : commande.getId();
            double montant = payment != null ? payment.getMontant() : commande.getTotal();
            Date displayDate = payment != null ? payment.getDatePayment() : commande.getDateCommande();
            String dateLabel = displayDate == null ? "-" : PAYMENT_DATE_FORMAT.format(displayDate);

            rows.add(new PaymentRow(
                    displayId,
                    commande.getId(),
                    montant,
                    "Valide",
                    dateLabel,
                    payment != null ? payment.getId() : null
            ));
        }

        return rows;
    }

    private void configureModifierColumn() {
        colModifier.setCellFactory(column -> new TableCell<>() {
            private final Button button = new Button("Modifier");

            {
                button.getStyleClass().add("table-action-edit");
                button.setOnAction(event -> {
                    Commande commande = getTableRow().getItem();
                    if (commande != null) {
                        openEditStatusWindow(commande);
                    }
                });
            }

            @Override
            protected void updateItem(Commande item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
            }
        });
    }

    private void configureSupprimerColumn() {
        colSupprimer.setCellFactory(column -> new TableCell<>() {
            private final Button button = new Button("Supprimer");

            {
                button.getStyleClass().add("table-action-delete");
                button.setOnAction(event -> {
                    Commande commande = getTableRow().getItem();
                    if (commande != null) {
                        deleteCommande(commande);
                    }
                });
            }

            @Override
            protected void updateItem(Commande item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : button);
            }
        });
    }

    private void openEditStatusWindow(Commande commande) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edit-commande-status-view.fxml"));
            Scene scene = new Scene(loader.load(), 520, 320);
            scene.getStylesheets().add(getClass().getResource("/styles/esportify-theme.css").toExternalForm());

            EditCommandeStatusController controller = loader.getController();
            controller.setCommande(commande);
            controller.setOnSaved(this::refreshData);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Modifier statut commande");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait();
        } catch (IOException e) {
            AlertUtils.showError("Impossible d'ouvrir la fenetre de modification.");
        }
    }

    private void deleteCommande(Commande commande) {
        try {
            serviceCommande.delete(commande.getId());
            refreshData();
            AlertUtils.showSuccess("Commande supprimee definitivement.");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    private void deletePayment(PaymentRow payment) {
        if (!payment.hasRealPayment()) {
            AlertUtils.showError("Aucune ligne payment physique n'existe pour cette commande payee.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer le paiement");
        confirmation.setContentText(
                "Voulez-vous vraiment supprimer le paiement #" + payment.getRealPaymentId()
                        + " de la commande #" + payment.getCommandeId() + " ?"
        );
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            servicePayment.delete(payment.getRealPaymentId());
            refreshData();
            AlertUtils.showSuccess("Paiement supprime avec succes.");
        } catch (SQLException e) {
            AlertUtils.showError("Erreur lors de la suppression du paiement : " + e.getMessage());
        }
    }

    private void showCommandeDetails(PaymentRow paymentRow) {
        try {
            List<LigneCommande> lignes = serviceLigneCommande.readAll().stream()
                    .filter(ligne -> ligne.getCommandeId() == paymentRow.getCommandeId())
                    .collect(Collectors.toList());

            Map<Integer, Produit> produitsById = catalogueProduitService.readAll().stream()
                    .collect(Collectors.toMap(Produit::getId, produit -> produit));

            StringBuilder details = new StringBuilder();
            details.append("Commande #").append(paymentRow.getCommandeId()).append("\n");
            details.append("Paiement #").append(paymentRow.getId()).append("\n");
            details.append("Montant: ").append(String.format("%.2f TND", paymentRow.getMontant())).append("\n");
            details.append("Statut: ").append(paymentRow.getStatut()).append("\n");
            details.append("Date: ").append(paymentRow.getDateLabel()).append("\n\n");

            if (lignes.isEmpty()) {
                details.append("Aucune ligne de commande trouvee.");
            } else {
                details.append("Produits:\n");
                for (LigneCommande ligne : lignes) {
                    Produit produit = produitsById.get(ligne.getProduitId());
                    String nomProduit = produit != null ? produit.getNom() : "Produit ID " + ligne.getProduitId();
                    details.append("- ")
                            .append(nomProduit)
                            .append(" | Quantite: ").append(ligne.getQuantite())
                            .append(" | Prix unitaire: ").append(String.format("%.2f TND", ligne.getPrixUnitaire()))
                            .append("\n");
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Detail commande payee");
            alert.setHeaderText("Contenu de la commande");
            alert.setContentText(details.toString());
            alert.showAndWait();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur lors du chargement du detail de la commande : " + e.getMessage());
        }
    }

    public static class PaymentRow {
        private final int id;
        private final int commandeId;
        private final double montant;
        private final String statut;
        private final String dateLabel;
        private final Integer realPaymentId;

        public PaymentRow(int id, int commandeId, double montant, String statut, String dateLabel, Integer realPaymentId) {
            this.id = id;
            this.commandeId = commandeId;
            this.montant = montant;
            this.statut = statut;
            this.dateLabel = dateLabel;
            this.realPaymentId = realPaymentId;
        }

        public int getId() {
            return id;
        }

        public int getCommandeId() {
            return commandeId;
        }

        public double getMontant() {
            return montant;
        }

        public String getStatut() {
            return statut;
        }

        public String getDateLabel() {
            return dateLabel;
        }

        public Integer getRealPaymentId() {
            return realPaymentId;
        }

        public boolean hasRealPayment() {
            return realPaymentId != null;
        }
    }

    private enum SortMode {
        ID_DESC,
        ID_ASC,
        MONTANT_DESC,
        MONTANT_ASC,
        DATE_DESC,
        DATE_ASC
    }
}
