package edu.esportify.controllers;

import edu.projetJava.controllers.BackTeamsDashboardController;
import edu.esportify.entities.Equipe;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.CandidatureService;
import edu.esportify.services.EquipeService;
import edu.esportify.services.RecrutementService;
import edu.esportify.services.TeamAlertService;
import edu.esportify.services.TeamBanNotificationService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class AdminTeamsController implements AdminContentController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String DEFAULT_LOGO = "/images/default-team-logo.png";
    private static final String[] CONTINENTS = {
            "Afrique",
            "Amerique",
            "Asie",
            "Europe",
            "Oceanie",
            "Antarctique"
    };

    private enum SortMode {
        NAME_ASC,
        NAME_DESC,
        DATE_DESC,
        DATE_ASC
    }

    private final EquipeService equipeService = new EquipeService();
    private final CandidatureService candidatureService = new CandidatureService();
    private final RecrutementService recrutementService = new RecrutementService();
    private final TeamAlertService teamAlertService = new TeamAlertService();
    private final TeamBanNotificationService teamBanNotificationService = new TeamBanNotificationService();

    private AdminLayoutController parentController;
    private BackTeamsDashboardController legacyHost;
    private Equipe selectedEquipe;
    private SortMode currentSortMode = SortMode.DATE_DESC;

    @FXML private TextField searchNameField;
    @FXML private ComboBox<String> searchRegionBox;
    @FXML private ComboBox<String> statusFilterBox;
    @FXML private VBox searchShell;
    @FXML private VBox tableToolbar;
    @FXML private Button searchButton;
    @FXML private Button resetButton;
    @FXML private Button createTeamButton;
    @FXML private Button sortNameAscButton;
    @FXML private Button sortNameDescButton;
    @FXML private Button sortDateDescButton;
    @FXML private Button sortDateAscButton;
    @FXML private TableView<Equipe> teamsTable;
    @FXML private TableColumn<Equipe, String> idColumn;
    @FXML private TableColumn<Equipe, String> nameColumn;
    @FXML private TableColumn<Equipe, String> tagColumn;
    @FXML private TableColumn<Equipe, String> regionColumn;
    @FXML private TableColumn<Equipe, String> membersColumn;
    @FXML private TableColumn<Equipe, String> visibilityColumn;
    @FXML private TableColumn<Equipe, String> reportsColumn;
    @FXML private TableColumn<Equipe, String> statusColumn;
    @FXML private TableColumn<Equipe, String> createdAtColumn;
    @FXML private TableColumn<Equipe, Void> actionsColumn;

    @Override
    public void init(AdminLayoutController parentController) {
        this.parentController = parentController;
        configure();
        refreshTable();
    }

    public void setLegacyHost(BackTeamsDashboardController legacyHost) {
        this.legacyHost = legacyHost;
    }

    private void configure() {
        ensureStyleClass(searchShell, "advanced-search-box");
        ensureStyleClass(tableToolbar, "filter-panel");
        ensureStyleClass(searchNameField, "filter-input");
        ensureStyleClass(searchRegionBox, "filter-input");
        ensureStyleClass(statusFilterBox, "filter-input");
        ensureStyleClass(searchButton, "btn-rechercher");
        ensureStyleClass(resetButton, "btn-reinitialiser");
        ensureStyleClass(createTeamButton, "btn-3d-cyan");
        ensureStyleClass(sortNameAscButton, "sort-pill");
        ensureStyleClass(sortNameDescButton, "sort-pill");
        ensureStyleClass(sortDateDescButton, "sort-pill");
        ensureStyleClass(sortDateAscButton, "sort-pill");

        if (!teamsTable.getStyleClass().contains("admin-teams-table")) {
            teamsTable.getStyleClass().add("admin-teams-table");
        }
        teamsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        teamsTable.setPlaceholder(new Label("Aucune equipe disponible pour ces filtres."));
        applyColumnWidths();

        if (statusFilterBox.getItems().isEmpty()) {
            statusFilterBox.getItems().setAll("Tous", "Active", "Inactive", "Bannie");
            statusFilterBox.setValue("Tous");
        }
        if (searchRegionBox.getItems().isEmpty()) {
            searchRegionBox.getItems().setAll("Toutes les regions");
            searchRegionBox.getItems().addAll(CONTINENTS);
            searchRegionBox.setValue("Toutes les regions");
        }

        searchNameField.textProperty().addListener((obs, oldValue, newValue) -> applyFiltersAndSort());
        searchRegionBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFiltersAndSort());
        statusFilterBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFiltersAndSort());

        idColumn.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getId()));
        idColumn.setCellFactory(param -> new AlignedTextCell(Pos.CENTER_LEFT, "admin-id-cell"));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getNomEquipe())));
        nameColumn.setCellFactory(param -> new TeamIdentityCell());
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(resolveStatusLabel(data.getValue())));
        statusColumn.setCellFactory(param -> new BadgeCell("admin-chip admin-chip-status"));
        tagColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getTag())));
        tagColumn.setCellFactory(param -> new BadgeCell("admin-chip admin-chip-tag"));
        regionColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getRegion())));
        regionColumn.setCellFactory(param -> new AlignedTextCell(Pos.CENTER_LEFT, "admin-region-cell"));
        membersColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(memberCount(data.getValue()))));
        membersColumn.setCellFactory(param -> new BadgeCell("admin-chip admin-chip-count"));
        visibilityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isPrivate() ? "Prive" : "Public"));
        visibilityColumn.setCellFactory(param -> new BadgeCell("admin-chip admin-chip-visibility"));
        reportsColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(teamAlertService.countBanReports(data.getValue().getId()))));
        reportsColumn.setCellFactory(param -> new BadgeCell("admin-chip admin-chip-count"));
        createdAtColumn.setCellValueFactory(data -> Bindings.createStringBinding(
                () -> data.getValue().getDateCreation() == null ? "-" : data.getValue().getDateCreation().format(DATE_FORMATTER)
        ));
        createdAtColumn.setCellFactory(param -> new AlignedTextCell(Pos.CENTER_LEFT, "admin-date-cell"));
        actionsColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(null));
        actionsColumn.setCellFactory(param -> new TeamActionCell());

        teamsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedEquipe = newValue;
        });

        updateSortButtons();
    }

    private void applyColumnWidths() {
        setColumnWidth(idColumn, 65);
        setColumnWidth(nameColumn, 180);
        setColumnWidth(statusColumn, 95);
        setColumnWidth(tagColumn, 85);
        setColumnWidth(regionColumn, 140);
        setColumnWidth(membersColumn, 75);
        setColumnWidth(visibilityColumn, 95);
        setColumnWidth(reportsColumn, 85);
        setColumnWidth(createdAtColumn, 105);
        setColumnWidth(actionsColumn, 380);
        teamsTable.setMinWidth(0);
    }

    private void setColumnWidth(TableColumn<?, ?> column, double width) {
        column.setMinWidth(width);
        column.setPrefWidth(width);
    }

    @FXML
    private void onSearch() {
        applyFiltersAndSort();
    }

    @FXML
    private void onResetFilters() {
        searchNameField.clear();
        searchRegionBox.setValue("Toutes les regions");
        statusFilterBox.setValue("Tous");
        currentSortMode = SortMode.DATE_DESC;
        updateSortButtons();
        applyFiltersAndSort();
    }

    @FXML
    private void onRefresh() {
        applyFiltersAndSort();
    }

    @FXML
    private void onNewTeam() {
        if (parentController != null) {
            parentController.showTeamEditor(null);
        } else if (legacyHost != null) {
            legacyHost.showTeamEditor(null);
        }
    }

    @FXML
    private void onDeleteSelected() {
        if (selectedEquipe == null) {
            return;
        }
        deleteEquipe(selectedEquipe);
    }

    private void refreshTable() {
        applyFiltersAndSort();
    }

    @FXML
    private void onSortNameAsc() {
        currentSortMode = SortMode.NAME_ASC;
        updateSortButtons();
        applyFiltersAndSort();
    }

    @FXML
    private void onSortNameDesc() {
        currentSortMode = SortMode.NAME_DESC;
        updateSortButtons();
        applyFiltersAndSort();
    }

    @FXML
    private void onSortDateDesc() {
        currentSortMode = SortMode.DATE_DESC;
        updateSortButtons();
        applyFiltersAndSort();
    }

    @FXML
    private void onSortDateAsc() {
        currentSortMode = SortMode.DATE_ASC;
        updateSortButtons();
        applyFiltersAndSort();
    }

    private void banEquipe(Equipe equipe) {
        BanReportInput banReport = promptBanReport(equipe);
        if (banReport == null) {
            return;
        }

        equipe.setBannedUntil(banReport.bannedUntil());
        equipe.setBanReason(banReport.reason());
        equipe.setBanDetails(banReport.details());
        equipe.setBannedByAdmin(AppSession.getInstance().getUsername());
        equipeService.updateEntity(equipe.getId(), equipe);

        int notifiedRecipients = 0;
        try {
            notifiedRecipients = teamBanNotificationService.notifyTeamBan(
                    equipe,
                    banReport.reason(),
                    banReport.details(),
                    AppSession.getInstance().getUsername()
            );
        } catch (RuntimeException e) {
            showMessage(
                    Alert.AlertType.WARNING,
                    "Email non envoye",
                    "Le bannissement va continuer, mais l'email Brevo a echoue.",
                    e.getMessage()
            );
        }

        refreshTable();
        selectedEquipe = equipe;

        String emailInfo;
        if (!teamBanNotificationService.isEmailConfigured()) {
            emailInfo = "Brevo n'est pas configure. Aucun email n'a ete envoye.";
        } else if (notifiedRecipients <= 0) {
            emailInfo = "Aucun destinataire email valide n'a ete trouve pour cette equipe.";
        } else {
            emailInfo = notifiedRecipients + " destinataire(s) ont recu le rapport de bannissement.";
        }
        showMessage(
                Alert.AlertType.INFORMATION,
                "Equipe bannie",
                "L'equipe " + safe(equipe.getNomEquipe()) + " est bannie jusqu'au "
                        + banReport.bannedUntil().format(DATE_FORMATTER) + ".",
                emailInfo
        );
    }

    private void removeBan(Equipe equipe) {
        if (equipe == null || !equipe.isCurrentlyBanned()) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Retirer le ban");
        confirmation.setHeaderText("Retirer le bannissement de " + safe(equipe.getNomEquipe()) + " ?");
        confirmation.setContentText("L'equipe redeviendra disponible immediatement.");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        equipe.clearBan();
        equipeService.updateEntity(equipe.getId(), equipe);
        refreshTable();
        selectedEquipe = equipe;
        showMessage(
                Alert.AlertType.INFORMATION,
                "Ban retire",
                "Le bannissement de " + safe(equipe.getNomEquipe()) + " a ete retire.",
                "L'equipe est de nouveau accessible."
        );
    }

    private void deleteEquipe(Equipe equipe) {
        if (equipe == null) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Supprimer l'equipe");
        confirmation.setHeaderText("Supprimer definitivement " + safe(equipe.getNomEquipe()) + " ?");
        confirmation.setContentText("Cette action supprimera l'equipe, ses candidatures et ses recrutements sans envoyer de rapport.");

        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        candidatureService.deleteByEquipe(equipe.getId());
        recrutementService.deleteByEquipe(equipe.getId());
        equipeService.deleteEntity(equipe);
        refreshTable();
        selectedEquipe = null;

        showMessage(
                Alert.AlertType.INFORMATION,
                "Equipe supprimee",
                "L'equipe " + safe(equipe.getNomEquipe()) + " a ete supprimee.",
                "Aucun email de bannissement n'a ete envoye."
        );
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void applyFiltersAndSort() {
        try {
            List<Equipe> equipes = equipeService.searchForAdmin(
                    safe(searchNameField.getText()),
                    normalizeRegionFilter(searchRegionBox.getValue()),
                    "",
                    safe(statusFilterBox.getValue())
            );

            equipes = equipes.stream()
                    .sorted(getComparator())
                    .toList();

            teamsTable.setItems(FXCollections.observableArrayList(equipes));
        } catch (RuntimeException e) {
            teamsTable.setItems(FXCollections.observableArrayList());
            teamsTable.setPlaceholder(new Label("Erreur chargement equipes: " + safe(e.getMessage())));
            System.out.println("Chargement equipes impossible: " + e.getMessage());
        }
    }

    private Comparator<Equipe> getComparator() {
        return switch (currentSortMode) {
            case NAME_ASC -> Comparator.comparing(
                    equipe -> safe(equipe.getNomEquipe()).toLowerCase(),
                    Comparator.nullsLast(String::compareTo)
            );
            case NAME_DESC -> Comparator.comparing(
                    (Equipe equipe) -> safe(equipe.getNomEquipe()).toLowerCase(),
                    Comparator.nullsLast(String::compareTo)
            ).reversed();
            case DATE_ASC -> Comparator.comparing(
                    Equipe::getDateCreation,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case DATE_DESC -> Comparator.comparing(
                    Equipe::getDateCreation,
                    Comparator.nullsLast(Comparator.reverseOrder())
            );
        };
    }

    private void updateSortButtons() {
        setSortButtonState(sortNameAscButton, currentSortMode == SortMode.NAME_ASC);
        setSortButtonState(sortNameDescButton, currentSortMode == SortMode.NAME_DESC);
        setSortButtonState(sortDateDescButton, currentSortMode == SortMode.DATE_DESC);
        setSortButtonState(sortDateAscButton, currentSortMode == SortMode.DATE_ASC);
    }

    private void setSortButtonState(Button button, boolean active) {
        if (button == null) {
            return;
        }
        button.getStyleClass().remove("is-active");
        if (active && !button.getStyleClass().contains("is-active")) {
            button.getStyleClass().add("is-active");
        }
    }

    private String normalizeRegionFilter(String region) {
        String value = safe(region).trim();
        return "Toutes les regions".equalsIgnoreCase(value) ? "" : value;
    }

    private void ensureStyleClass(javafx.scene.Node node, String... styleClasses) {
        if (node == null) {
            return;
        }
        for (String styleClass : styleClasses) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        }
    }

    private int memberCount(Equipe equipe) {
        return candidatureService.getAcceptedMembersByEquipe(equipe.getId()).size() + 1;
    }

    private BanReportInput promptBanReport(Equipe equipe) {
        Dialog<BanReportInput> dialog = new Dialog<>();
        dialog.setTitle("Rapport de bannissement");
        dialog.setHeaderText("Bannir l'equipe " + safe(equipe.getNomEquipe()) + " et envoyer un rapport.");
        dialog.getDialogPane().getStyleClass().addAll("admin-premium-form-shell", "admin-ban-dialog");

        ButtonType confirmButtonType = new ButtonType("Bannir l'equipe", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        TextField reasonField = new TextField();
        reasonField.setPromptText("Motif principal du bannissement");
        reasonField.getStyleClass().addAll("admin-search-field", "admin-elevated-field");

        DatePicker banEndDatePicker = new DatePicker(LocalDate.now().plusWeeks(1));
        banEndDatePicker.setPromptText("Date de fin du bannissement");
        banEndDatePicker.getStyleClass().addAll("admin-search-field", "admin-elevated-field");

        TextArea detailsArea = new TextArea();
        detailsArea.setPromptText("Details du rapport envoyes aux membres et au manager");
        detailsArea.setWrapText(true);
        detailsArea.setPrefRowCount(8);
        detailsArea.getStyleClass().addAll("admin-search-field", "admin-elevated-field", "admin-team-editor-area");

        Label introLabel = new Label("Remplissez un rapport clair pour tracer la moderation et prevenir les destinataires concernes.");
        introLabel.getStyleClass().add("admin-reference-subtitle");
        introLabel.setWrapText(true);
        Label configLabel = new Label(teamBanNotificationService.isEmailConfigured()
                ? "Le rapport sera envoye par email aux membres acceptes et au manager."
                : "Brevo n'est pas configure. Le bannissement sera applique sans envoi d'email.");
        configLabel.getStyleClass().add("muted-label");
        configLabel.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 0, 0, 0));
        Label reasonLabel = new Label("Motif");
        Label endDateLabel = new Label("Date de fin");
        Label detailsLabel = new Label("Rapport detaille");
        reasonLabel.getStyleClass().add("field-label");
        endDateLabel.getStyleClass().add("field-label");
        detailsLabel.getStyleClass().add("field-label");
        grid.add(introLabel, 0, 0);
        grid.add(reasonLabel, 0, 1);
        grid.add(reasonField, 0, 2);
        grid.add(endDateLabel, 0, 3);
        grid.add(banEndDatePicker, 0, 4);
        grid.add(detailsLabel, 0, 5);
        grid.add(detailsArea, 0, 6);
        grid.add(configLabel, 0, 7);
        GridPane.setHgrow(reasonField, Priority.ALWAYS);
        GridPane.setHgrow(banEndDatePicker, Priority.ALWAYS);
        GridPane.setHgrow(detailsArea, Priority.ALWAYS);
        GridPane.setVgrow(detailsArea, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node confirmButton = dialog.getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setDisable(true);
        Runnable refreshConfirmState = () -> confirmButton.setDisable(
                safe(reasonField.getText()).length() < 5 || parseDurationDays(banEndDatePicker.getValue()) <= 0
        );
        reasonField.textProperty().addListener((obs, oldValue, newValue) -> refreshConfirmState.run());
        banEndDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> refreshConfirmState.run());

        dialog.setResultConverter(buttonType -> {
            if (buttonType != confirmButtonType) {
                return null;
            }
            int durationDays = parseDurationDays(banEndDatePicker.getValue());
            LocalDateTime bannedUntil = banEndDatePicker.getValue().atTime(23, 59);
            return new BanReportInput(safe(reasonField.getText()), safe(detailsArea.getText()), durationDays, bannedUntil);
        });

        return dialog.showAndWait().orElse(null);
    }

    private int parseDurationDays(LocalDate endDate) {
        if (endDate == null || !endDate.isAfter(LocalDate.now())) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }

    private String resolveStatusLabel(Equipe equipe) {
        if (equipe == null) {
            return "";
        }
        if (equipe.isCurrentlyBanned()) {
            return "Bannie";
        }
        return equipe.isActive() ? "Active" : "Inactive";
    }

    private void showMessage(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Image loadLogo(String path) {
        try {
            if (path != null && !path.isBlank()) {
                return new Image(path, true);
            }
        } catch (IllegalArgumentException ignored) {
        }
        return new Image(String.valueOf(getClass().getResource(DEFAULT_LOGO)));
    }

    private record BanReportInput(String reason, String details, int durationDays, LocalDateTime bannedUntil) {
    }

    private final class TeamActionCell extends TableCell<Equipe, Void> {
        private final javafx.scene.control.Button editButton = new javafx.scene.control.Button("Modifier");
        private final javafx.scene.control.Button banButton = new javafx.scene.control.Button("Bannir");
        private final javafx.scene.control.Button unbanButton = new javafx.scene.control.Button("Retirer ban");
        private final javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("Supprimer");
        private final FlowPane box = new FlowPane(8, 8, editButton, banButton, unbanButton, deleteButton);

        private TeamActionCell() {
            editButton.getStyleClass().addAll("admin-inline-action", "is-info");
            banButton.getStyleClass().addAll("admin-inline-action", "is-warning");
            unbanButton.getStyleClass().addAll("admin-inline-action", "is-neutral");
            deleteButton.getStyleClass().addAll("admin-inline-action", "is-danger");
            box.getStyleClass().add("admin-action-pane");
            editButton.setPrefWidth(104);
            banButton.setPrefWidth(94);
            unbanButton.setPrefWidth(108);
            deleteButton.setPrefWidth(104);
            editButton.setPrefHeight(36);
            banButton.setPrefHeight(36);
            unbanButton.setPrefHeight(36);
            deleteButton.setPrefHeight(36);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setPrefWrapLength(238);

            editButton.setOnAction(event -> {
                Equipe equipe = getTableView().getItems().get(getIndex());
                teamsTable.getSelectionModel().select(equipe);
                selectedEquipe = equipe;
                if (parentController != null) {
                    parentController.showTeamEditor(equipe);
                } else if (legacyHost != null) {
                    legacyHost.showTeamEditor(equipe);
                }
            });
            banButton.setOnAction(event -> {
                Equipe equipe = getTableView().getItems().get(getIndex());
                teamsTable.getSelectionModel().select(equipe);
                selectedEquipe = equipe;
                banEquipe(equipe);
            });
            unbanButton.setOnAction(event -> {
                Equipe equipe = getTableView().getItems().get(getIndex());
                teamsTable.getSelectionModel().select(equipe);
                selectedEquipe = equipe;
                removeBan(equipe);
            });
            deleteButton.setOnAction(event -> {
                Equipe equipe = getTableView().getItems().get(getIndex());
                teamsTable.getSelectionModel().select(equipe);
                selectedEquipe = equipe;
                deleteEquipe(equipe);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                setGraphic(null);
                return;
            }
            Equipe equipe = getTableView().getItems().get(getIndex());
            boolean banned = equipe != null && equipe.isCurrentlyBanned();
            banButton.setDisable(banned);
            unbanButton.setDisable(!banned);
            setGraphic(box);
        }
    }

    private final class BadgeCell extends TableCell<Equipe, String> {
        private final String styleClasses;

        private BadgeCell(String styleClasses) {
            this.styleClasses = styleClasses;
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item.isBlank()) {
                setGraphic(null);
                return;
            }
            Label badge = new Label(item);
            badge.getStyleClass().addAll(styleClasses.split(" "));
            setGraphic(badge);
        }
    }

    private final class AlignedTextCell extends TableCell<Equipe, String> {
        private final Pos alignment;
        private final String styleClass;

        private AlignedTextCell(Pos alignment, String styleClass) {
            this.alignment = alignment;
            this.styleClass = styleClass;
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item.isBlank()) {
                setText(null);
                setGraphic(null);
                return;
            }
            setAlignment(alignment);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
            setText(item);
            getStyleClass().removeAll("admin-id-cell", "admin-region-cell", "admin-date-cell");
            if (!getStyleClass().contains(styleClass)) {
                getStyleClass().add(styleClass);
            }
        }
    }

    private final class TeamIdentityCell extends TableCell<Equipe, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                setGraphic(null);
                return;
            }

            Equipe equipe = getTableView().getItems().get(getIndex());
            ImageView logoView = new ImageView(loadLogo(equipe.getLogo()));
            logoView.setFitWidth(36);
            logoView.setFitHeight(36);
            logoView.getStyleClass().add("admin-team-logo");

            Label teamLabel = new Label(safe(equipe.getNomEquipe()));
            teamLabel.getStyleClass().add("admin-team-name");

            HBox box = new HBox(12, logoView, teamLabel);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setFillHeight(true);
            setGraphic(box);
        }
    }
}
