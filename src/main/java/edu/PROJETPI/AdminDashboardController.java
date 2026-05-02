package edu.PROJETPI;

import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.entites.LigneCommande;
import edu.PROJETPI.entites.Payment;
import edu.PROJETPI.entites.Produit;
import edu.PROJETPI.services.CatalogueProduitService;
import edu.PROJETPI.services.PaymentReportPdfService;
import edu.PROJETPI.services.RevenueForecastService;
import edu.PROJETPI.services.ServiceCommande;
import edu.PROJETPI.services.ServiceLigneCommande;
import edu.PROJETPI.services.ServicePayment;
import edu.PROJETPI.tools.AlertUtils;
import edu.PROJETPI.tools.SceneNavigator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminDashboardController implements Initializable {

    private static final String MENU_ACTIVE_CLASS = "sidebar-submenu-active";
    private static final SimpleDateFormat PAYMENT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    private static final String FILTER_ALL = "Tous";
    private static InitialSection initialSection = InitialSection.COMMANDES;

    public enum InitialSection {
        COMMANDES,
        PAIEMENTS,
        PREDICTION_CA
    }

    public static void openOn(InitialSection section) {
        initialSection = section == null ? InitialSection.COMMANDES : section;
    }

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
    private VBox paymentFilterCard;
    @FXML
    private DatePicker paymentDateDebutPicker;
    @FXML
    private DatePicker paymentDateFinPicker;
    @FXML
    private Label pageTitleLabel;
    @FXML
    private Label pageSubtitleLabel;
    @FXML
    private Label pageAiIconLabel;
    @FXML
    private Label sectionTitleLabel;
    @FXML
    private Button commandesButton;
    @FXML
    private Button paiementsButton;
    @FXML
    private Button paymentListButton;
    @FXML
    private Button paymentForecastButton;
    @FXML
    private VBox predictionPanel;
    @FXML
    private Label predictionModeLabel;
    @FXML
    private Label predictedRevenueLabel;
    @FXML
    private Label predictedRevenueWeekLabel;
    @FXML
    private Label predictedRevenueMonthLabel;
    @FXML
    private Label predictedTrendLabel;
    @FXML
    private Label predictedOrdersLabel;
    @FXML
    private Label predictedOrdersWeekLabel;
    @FXML
    private Label predictedOrdersMonthLabel;
    @FXML
    private Label averageTicketLabel;
    @FXML
    private Label snapshotTodayRevenueLabel;
    @FXML
    private Label snapshotWeekRevenueLabel;
    @FXML
    private Label snapshotMonthRevenueLabel;
    @FXML
    private Label snapshotPaidCountLabel;
    @FXML
    private VBox dailyEvolutionContainer;
    @FXML
    private VBox weeklyEvolutionContainer;
    @FXML
    private VBox distributionContainer;

    private final ServiceCommande serviceCommande = new ServiceCommande();
    private final ServicePayment servicePayment = new ServicePayment();
    private final ServiceLigneCommande serviceLigneCommande = new ServiceLigneCommande();
    private final CatalogueProduitService catalogueProduitService = new CatalogueProduitService();
    private final PaymentReportPdfService paymentReportPdfService = new PaymentReportPdfService();
    private final RevenueForecastService revenueForecastService = new RevenueForecastService();
    private final ObservableList<Commande> commandes = FXCollections.observableArrayList();
    private final FilteredList<Commande> filteredCommandes = new FilteredList<>(commandes, item -> true);
    private final SortedList<Commande> sortedCommandes = new SortedList<>(filteredCommandes);
    private final ObservableList<PaymentRow> payments = FXCollections.observableArrayList();
    private Timeline paymentAutoRefreshTimeline;
    private boolean paymentMode;
    private boolean paymentForecastMode;
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
        configurePaymentTableLayout();
        configureDarkTableRows();
        forceDarkTableTheme();
        initializeCommandFilters();
        configureCommandeStatusColumn();
        configureModifierColumn();
        configureSupprimerColumn();
        configurePaymentStatusColumn();
        configurePaymentActionsColumn();
        startPaymentAutoRefresh();
        if (initialSection == InitialSection.PREDICTION_CA) {
            showPaymentForecast();
        } else if (initialSection == InitialSection.PAIEMENTS) {
            showPaymentList();
        } else {
            showCommandes();
        }
        initialSection = InitialSection.COMMANDES;
    }

    @FXML
    private void goToCatalogue() {
        SceneNavigator.switchScene(commandeTableView, "/backListProduit.fxml", "Gestion des produits");
    }

    @FXML
    private void goToFrontOffice() {
        SceneNavigator.switchScene(commandeTableView, "/ajoutProduit.fxml", "E-SPORTIFY : Boutique");
    }

    @FXML
    private void goToMailing() {
        SceneNavigator.switchScene(commandeTableView, "/backMailing.fxml", "Boutique Admin - Mailing");
    }

    @FXML
    private void goToAdminCategorie() {
        SceneNavigator.switchScene(commandeTableView, "/backListCategorie.fxml", "Boutique Admin - Categories");
    }

    @FXML
    private void goToGestionComptes() {
        SceneNavigator.switchScene(commandeTableView, "/edu/ProjetPI/views/admin-dashboard.fxml", "Gestion des comptes");
    }

    @FXML
    private void handleLogout() {
        edu.ProjetPI.controllers.DashboardSession.clear();
        edu.ProjetPI.controllers.SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "E-SPORTIFY : Connexion");
    }

    @FXML
    private void refreshData() {
        try {
            if (paymentMode) {
                validatePaymentDateRange();
                payments.setAll(buildPaymentRows());
                paymentTableView.setItems(payments);
                commandeCountLabel.setText(String.valueOf(payments.size()));
                refreshPaymentPredictions();
            } else {
                commandes.setAll(serviceCommande.readAll());
                applyCommandFilters();
            }
        } catch (SQLException e) {
            String message = paymentMode
                    ? "Erreur lors du chargement des paiements : "
                    : "Erreur lors du chargement des commandes : ";
            AlertUtils.showError(message + e.getMessage());
        } catch (IllegalArgumentException e) {
            AlertUtils.showError(e.getMessage());
        }
    }

    @FXML
    private void showCommandes() {
        paymentMode = false;
        paymentForecastMode = false;
        pageTitleLabel.setText("GESTION DES COMMANDES");
        setPredictionTitleMode(false);
        pageSubtitleLabel.setManaged(true);
        pageSubtitleLabel.setVisible(true);
        pageSubtitleLabel.setText("Suivez les commandes, leurs statuts et les actions associees.");
        sectionTitleLabel.setText("Liste des commandes");
        setPaymentSubmenuVisible(false);
        setActiveMenuButton(commandesButton, paiementsButton, paymentListButton, paymentForecastButton);
        commandeFilterCard.setManaged(true);
        commandeFilterCard.setVisible(true);
        paymentFilterCard.setManaged(false);
        paymentFilterCard.setVisible(false);
        predictionPanel.setManaged(false);
        predictionPanel.setVisible(false);
        commandeTableView.setManaged(true);
        commandeTableView.setVisible(true);
        paymentTableView.setManaged(false);
        paymentTableView.setVisible(false);
        commandeTableView.setPrefHeight(560.0);
        refreshData();
    }

    @FXML
    private void showPaiements() {
        setPaymentSubmenuVisible(true);
        showPaymentList();
    }

    @FXML
    private void showPaymentList() {
        paymentMode = true;
        paymentForecastMode = false;
        pageTitleLabel.setText("Liste des paiements");
        setPredictionTitleMode(false);
        sectionTitleLabel.setText("Liste des paiements");
        setPaymentSubmenuVisible(true);
        setActiveMenuButton(paymentListButton, commandesButton, paiementsButton, paymentForecastButton);
        setParentPaymentButtonActive();
        commandeFilterCard.setManaged(false);
        commandeFilterCard.setVisible(false);
        paymentFilterCard.setManaged(true);
        paymentFilterCard.setVisible(true);
        predictionPanel.setManaged(false);
        predictionPanel.setVisible(false);
        commandeTableView.setManaged(false);
        commandeTableView.setVisible(false);
        paymentTableView.setManaged(true);
        paymentTableView.setVisible(true);
        paymentTableView.setPrefHeight(280.0);
        refreshData();
    }

    @FXML
    private void showPaymentForecast() {
        paymentMode = true;
        paymentForecastMode = true;
        pageTitleLabel.setText("Prediction chiffre d'affaires");
        setPredictionTitleMode(true);
        sectionTitleLabel.setText("Prediction chiffre d'affaires");
        setPaymentSubmenuVisible(true);
        setActiveMenuButton(paymentForecastButton, commandesButton, paiementsButton, paymentListButton);
        setParentPaymentButtonActive();
        commandeFilterCard.setManaged(false);
        commandeFilterCard.setVisible(false);
        paymentFilterCard.setManaged(false);
        paymentFilterCard.setVisible(false);
        predictionPanel.setManaged(true);
        predictionPanel.setVisible(true);
        commandeTableView.setManaged(false);
        commandeTableView.setVisible(false);
        paymentTableView.setManaged(false);
        paymentTableView.setVisible(false);
        refreshData();
    }

    @FXML
    private void applyPaymentFilters() {
        try {
            validatePaymentDateRange();
            refreshData();
        } catch (IllegalArgumentException e) {
            AlertUtils.showError(e.getMessage());
        }
    }

    @FXML
    private void resetPaymentFilters() {
        paymentDateDebutPicker.setValue(null);
        paymentDateFinPicker.setValue(null);
        refreshData();
    }

    @FXML
    private void exportPaymentReportPdf() {
        generatePaymentReport(false);
    }

    @FXML
    private void printPaymentReport() {
        generatePaymentReport(true);
    }

    @FXML
    private void trainPaymentForecast() {
        Task<PaymentForecastTrainCommand.TrainingResult> task = new Task<>() {
            @Override
            protected PaymentForecastTrainCommand.TrainingResult call() throws Exception {
                return PaymentForecastTrainCommand.train();
            }
        };

        task.setOnSucceeded(event -> {
            PaymentForecastTrainCommand.TrainingResult result = task.getValue();
            refreshPaymentPredictions();
            AlertUtils.showSuccess("Modele IA entraine avec " + result.exportedRows() + " lignes temporelles.");
        });
        task.setOnFailed(event -> AlertUtils.showError(
                "Impossible d'entrainer le modele IA : " + task.getException().getMessage()
        ));

        Thread thread = new Thread(task, "payment-forecast-training");
        thread.setDaemon(true);
        thread.start();
    }

    private void setActiveMenuButton(Button activeButton, Button... inactiveButtons) {
        if (activeButton != null && !activeButton.getStyleClass().contains(MENU_ACTIVE_CLASS)) {
            activeButton.getStyleClass().add(MENU_ACTIVE_CLASS);
        }
        for (Button inactiveButton : inactiveButtons) {
            if (inactiveButton != null) {
                inactiveButton.getStyleClass().remove(MENU_ACTIVE_CLASS);
            }
        }
    }

    private void setPaymentSubmenuVisible(boolean visible) {
        if (paymentListButton != null) {
            paymentListButton.setManaged(visible);
            paymentListButton.setVisible(visible);
        }
        if (paymentForecastButton != null) {
            paymentForecastButton.setManaged(true);
            paymentForecastButton.setVisible(true);
        }
    }

    private void setPredictionTitleMode(boolean enabled) {
        pageAiIconLabel.setManaged(enabled);
        pageAiIconLabel.setVisible(enabled);
        pageSubtitleLabel.setManaged(enabled);
        pageSubtitleLabel.setVisible(enabled);
        pageSubtitleLabel.setText("Analyse IA des revenus, paiements prevus et tendances");
    }

    private void setParentPaymentButtonActive() {
        if (!paiementsButton.getStyleClass().contains(MENU_ACTIVE_CLASS)) {
            paiementsButton.getStyleClass().add(MENU_ACTIVE_CLASS);
        }
    }

    private void initializeCommandFilters() {
        statutFilterComboBox.setItems(FXCollections.observableArrayList(
                FILTER_ALL, "EN_ATTENTE", "EN_ATTENTE_PAIEMENT", "EN_LIVRAISON", "PAYEE", "ANNULEE"
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
        String normalizedSelected = normalizeCommandeStatus(selectedStatus);
        String normalizedCommande = normalizeCommandeStatus(commande.getStatut());
        return normalizedSelected.equalsIgnoreCase(normalizedCommande);
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

                String normalizedLabel = normalizeCommandeStatus(item);
                Label badge = new Label(normalizedLabel);
                badge.getStyleClass().add("commande-status-badge");

                String normalized = normalizedLabel.trim().toUpperCase();
                switch (normalized) {
                    case "PAYEE" -> badge.getStyleClass().add("commande-status-paid");
                    case "ANNULEE" -> badge.getStyleClass().add("commande-status-cancelled");
                    case "EN_ATTENTE", "EN_ATTENTE_PAIEMENT", "EN_LIVRAISON" ->
                            badge.getStyleClass().add("commande-status-pending");
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
            private final Button deleteButton = new Button("Suppr");
            private final HBox actionsBox = new HBox(5, viewButton, deleteButton);

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

    private void configurePaymentTableLayout() {
        if (!paymentTableView.getStyleClass().contains("payment-data-table")) {
            paymentTableView.getStyleClass().add("payment-data-table");
        }
        paymentColId.setMinWidth(56);
        paymentColCommandeId.setMinWidth(88);
        paymentColMontant.setMinWidth(118);
        paymentColStatut.setMinWidth(106);
        paymentColDate.setMinWidth(116);
        paymentColActions.setMinWidth(112);
        paymentColActions.setMaxWidth(132);
        paymentTableView.widthProperty().addListener((obs, oldWidth, newWidth) -> resizePaymentColumns(newWidth.doubleValue()));
        resizePaymentColumns(paymentTableView.getWidth());
    }

    private void configureDarkTableRows() {
        commandeTableView.setRowFactory(tableView -> createDarkRow());
        paymentTableView.setRowFactory(tableView -> createDarkRow());
    }

    private void forceDarkTableTheme() {
        String tableStyle = "-fx-background-color: #070b18;"
                + "-fx-control-inner-background: #070b18;"
                + "-fx-table-cell-border-color: rgba(255,255,255,0.06);"
                + "-fx-text-background-color: #eef5ff;";
        commandeTableView.setStyle(tableStyle);
        paymentTableView.setStyle(tableStyle);
    }

    private <T> TableRow<T> createDarkRow() {
        return new TableRow<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("-fx-background-color: #070b18;");
                } else {
                    setStyle("-fx-background-color: #0b1020; -fx-table-cell-border-color: rgba(255, 255, 255, 0.055);");
                }
            }
        };
    }

    private void resizePaymentColumns(double tableWidth) {
        if (tableWidth <= 0) {
            return;
        }

        double width = Math.max(680, tableWidth - 28);
        paymentColId.setPrefWidth(width * 0.08);
        paymentColCommandeId.setPrefWidth(width * 0.16);
        paymentColMontant.setPrefWidth(width * 0.17);
        paymentColStatut.setPrefWidth(width * 0.16);
        paymentColDate.setPrefWidth(width * 0.23);
        paymentColActions.setPrefWidth(width * 0.20);
    }

    private ObservableList<PaymentRow> buildPaymentRows() throws SQLException {
        Map<Integer, Commande> commandesById = serviceCommande.readAll().stream()
                .collect(Collectors.toMap(Commande::getId, commande -> commande, (first, second) -> first));
        ObservableList<PaymentRow> rows = FXCollections.observableArrayList();
        for (Payment payment : loadPaymentsForCurrentPeriod()) {
            Commande commande = commandesById.get(payment.getCommandeId());
            Date displayDate = payment.getDatePayment();
            String dateLabel = displayDate == null ? "-" : PAYMENT_DATE_FORMAT.format(displayDate);
            String statut = payment.getStatus();
            if (statut == null || statut.isBlank()) {
                statut = commande != null ? commande.getStatut() : "PAYEE";
            }
            statut = normalizeCommandeStatus(statut);

            rows.add(new PaymentRow(
                    payment.getId(),
                    payment.getCommandeId(),
                    payment.getMontant(),
                    statut,
                    dateLabel,
                    displayDate == null ? null : new java.sql.Date(displayDate.getTime()).toLocalDate(),
                    payment.getId()
            ));
        }

        return rows;
    }

    private String normalizeCommandeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "";
        }

        String normalized = status.trim().toUpperCase();
        return switch (normalized) {
            case "PAID" -> "PAYEE";
            case "CANCELLED", "CANCELED" -> "ANNULEE";
            default -> normalized;
        };
    }

    private List<Payment> loadPaymentsForCurrentPeriod() throws SQLException {
        LocalDate dateDebut = paymentDateDebutPicker != null ? paymentDateDebutPicker.getValue() : null;
        LocalDate dateFin = paymentDateFinPicker != null ? paymentDateFinPicker.getValue() : null;
        if (dateDebut == null && dateFin == null) {
            return servicePayment.readAll();
        }
        return servicePayment.readByPeriod(
                dateDebut != null ? java.sql.Date.valueOf(dateDebut) : null,
                dateFin != null ? java.sql.Date.valueOf(dateFin) : null
        );
    }

    private void validatePaymentDateRange() {
        LocalDate dateDebut = paymentDateDebutPicker != null ? paymentDateDebutPicker.getValue() : null;
        LocalDate dateFin = paymentDateFinPicker != null ? paymentDateFinPicker.getValue() : null;
        if (dateDebut != null && dateFin != null && dateDebut.isAfter(dateFin)) {
            throw new IllegalArgumentException("La date debut doit etre inferieure ou egale a la date fin.");
        }
    }

    private void generatePaymentReport(boolean printMode) {
        try {
            validatePaymentDateRange();
            if (!paymentMode) {
                showPaiements();
            } else if (payments.isEmpty()) {
                payments.setAll(buildPaymentRows());
                paymentTableView.setItems(payments);
            }

            if (payments.isEmpty()) {
                AlertUtils.showError("Aucun paiement a exporter pour cette periode.");
                return;
            }

            var pdfPath = paymentReportPdfService.generateReport(
                    List.copyOf(payments),
                    paymentDateDebutPicker != null ? paymentDateDebutPicker.getValue() : null,
                    paymentDateFinPicker != null ? paymentDateFinPicker.getValue() : null,
                    getConnectedAdminName()
            );

            if (printMode) {
                paymentReportPdfService.printReport(pdfPath);
                AlertUtils.showSuccess("Rapport des paiements envoye vers l'impression.");
            } else {
                paymentReportPdfService.openReport(pdfPath);
                AlertUtils.showSuccess("Rapport des paiements genere en PDF.");
            }
        } catch (IllegalArgumentException e) {
            AlertUtils.showError(e.getMessage());
        } catch (SQLException | IOException e) {
            AlertUtils.showError("Impossible de generer le rapport des paiements : " + e.getMessage());
        }
    }

    private String getConnectedAdminName() {
        String userName = System.getProperty("user.name");
        return userName == null || userName.isBlank() ? "Administrateur" : userName;
    }

    private void refreshPaymentPredictions() {
        RevenueForecastService.PredictionSnapshot snapshot = revenueForecastService.analyze(List.copyOf(payments));
        predictedRevenueLabel.setText(revenueForecastService.formatMoney(snapshot.predictedNextDayRevenue()));
        predictedRevenueWeekLabel.setText(revenueForecastService.formatMoney(snapshot.predictedWeekRevenue()));
        predictedRevenueMonthLabel.setText(revenueForecastService.formatMoney(snapshot.predictedMonthRevenue()));
        predictedTrendLabel.setText("Tendance 7j: " + revenueForecastService.formatTrend(snapshot.trend7Days()));
        predictedOrdersLabel.setText(String.valueOf(snapshot.predictedNextDayOrders()));
        predictedOrdersWeekLabel.setText(String.valueOf(snapshot.predictedWeekOrders()));
        predictedOrdersMonthLabel.setText(String.valueOf(snapshot.predictedMonthOrders()));
        averageTicketLabel.setText(revenueForecastService.formatMoney(snapshot.averageTicket()));
        snapshotTodayRevenueLabel.setText(revenueForecastService.formatMoney(snapshot.todayRevenue()));
        snapshotWeekRevenueLabel.setText(revenueForecastService.formatMoney(snapshot.last7DaysRevenue()));
        snapshotMonthRevenueLabel.setText(revenueForecastService.formatMoney(snapshot.last30DaysRevenue()));
        snapshotPaidCountLabel.setText(String.valueOf(snapshot.totalPaidPayments()));
        predictionModeLabel.setText(snapshot.sourceLabel());

        populateDailyEvolution(snapshot.dailyPoints());
        populateWeeklyEvolution(snapshot.weeklyPoints());
        populateDistribution(snapshot.distributionPoints());
    }

    private void startPaymentAutoRefresh() {
        paymentAutoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            if (paymentMode) {
                refreshData();
            }
        }));
        paymentAutoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        paymentAutoRefreshTimeline.play();
    }

    private void populateDailyEvolution(List<RevenueForecastService.DailyPoint> points) {
        double maxRevenue = points.stream().mapToDouble(RevenueForecastService.DailyPoint::revenue).max().orElse(1);
        dailyEvolutionContainer.getChildren().clear();
        for (RevenueForecastService.DailyPoint point : points) {
            String label = String.format("%1$td/%1$tm", java.sql.Date.valueOf(point.date()));
            dailyEvolutionContainer.getChildren().add(createAnalyticsRow(
                    label,
                    point.revenue(),
                    maxRevenue,
                    revenueForecastService.formatMoney(point.revenue()),
                    point.count() + " pmt"
            ));
        }
    }

    private void populateWeeklyEvolution(List<RevenueForecastService.WeeklyPoint> points) {
        double maxRevenue = points.stream().mapToDouble(RevenueForecastService.WeeklyPoint::revenue).max().orElse(1);
        weeklyEvolutionContainer.getChildren().clear();
        for (RevenueForecastService.WeeklyPoint point : points) {
            weeklyEvolutionContainer.getChildren().add(createAnalyticsRow(
                    point.label(),
                    point.revenue(),
                    maxRevenue,
                    revenueForecastService.formatMoney(point.revenue()),
                    point.count() + " pmt"
            ));
        }
    }

    private void populateDistribution(List<RevenueForecastService.DistributionPoint> points) {
        double maxRevenue = points.stream().mapToDouble(RevenueForecastService.DistributionPoint::revenue).max().orElse(1);
        distributionContainer.getChildren().clear();
        for (RevenueForecastService.DistributionPoint point : points) {
            distributionContainer.getChildren().add(createAnalyticsRow(
                    point.label(),
                    point.revenue(),
                    maxRevenue,
                    revenueForecastService.formatMoney(point.revenue()),
                    point.count() + " pmt"
            ));
        }
    }

    private HBox createAnalyticsRow(String labelText, double value, double maxValue, String valueText, String secondaryText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("analytics-row-label");
        label.setMinWidth(72);
        label.setPrefWidth(72);

        ProgressBar progressBar = new ProgressBar(maxValue <= 0 ? 0 : Math.min(1.0, value / maxValue));
        progressBar.getStyleClass().add("analytics-progress");
        progressBar.setMinWidth(120);
        progressBar.setMaxWidth(Double.MAX_VALUE);

        VBox valueBox = new VBox(2);
        valueBox.setAlignment(Pos.CENTER_RIGHT);
        valueBox.setMinWidth(118);
        valueBox.setPrefWidth(118);
        Label mainValue = new Label(valueText);
        mainValue.getStyleClass().add("analytics-row-value");
        Label secondaryValue = new Label(secondaryText);
        secondaryValue.getStyleClass().add("analytics-row-subvalue");
        valueBox.getChildren().addAll(mainValue, secondaryValue);

        HBox row = new HBox(12);
        row.getStyleClass().add("analytics-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(7, 10, 7, 10));
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        row.getChildren().addAll(label, progressBar, valueBox);
        return row;
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
        private final LocalDate actualDate;
        private final Integer realPaymentId;

        public PaymentRow(int id, int commandeId, double montant, String statut, String dateLabel, LocalDate actualDate, Integer realPaymentId) {
            this.id = id;
            this.commandeId = commandeId;
            this.montant = montant;
            this.statut = statut;
            this.dateLabel = dateLabel;
            this.actualDate = actualDate;
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

        public LocalDate getActualDate() {
            return actualDate;
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
