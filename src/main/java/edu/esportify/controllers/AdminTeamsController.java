package edu.esportify.controllers;

import edu.esportify.entities.Equipe;
import edu.esportify.services.CandidatureService;
import edu.esportify.services.EquipeService;
import edu.esportify.services.RecrutementService;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

    private AdminLayoutController parentController;
    private Equipe selectedEquipe;
    private SortMode currentSortMode = SortMode.DATE_DESC;

    @FXML private TextField searchNameField;
    @FXML private ComboBox<String> searchRegionBox;
    @FXML private ComboBox<String> statusFilterBox;
    @FXML private VBox searchShell;
    @FXML private HBox tableToolbar;
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

    private void configure() {
        ensureStyleClass(searchShell, "admin-search-shell", "admin-premium-search-shell");
        ensureStyleClass(tableToolbar, "admin-table-toolbar");
        ensureStyleClass(searchNameField, "field", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(searchRegionBox, "dark-combo", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(statusFilterBox, "dark-combo", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(searchButton, "admin-glow-button");
        ensureStyleClass(resetButton, "admin-ghost-button");
        ensureStyleClass(createTeamButton, "admin-glow-button", "admin-create-team-button");
        ensureStyleClass(sortNameAscButton, "admin-sort-pill", "admin-sort-button");
        ensureStyleClass(sortNameDescButton, "admin-sort-pill", "admin-sort-button");
        ensureStyleClass(sortDateDescButton, "admin-sort-pill", "admin-sort-button");
        ensureStyleClass(sortDateAscButton, "admin-sort-pill", "admin-sort-button");

        if (!teamsTable.getStyleClass().contains("admin-teams-table")) {
            teamsTable.getStyleClass().add("admin-teams-table");
        }
        teamsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        teamsTable.setPlaceholder(new Label("Aucune equipe disponible pour ces filtres."));
        applyColumnWidths();

        if (statusFilterBox.getItems().isEmpty()) {
            statusFilterBox.getItems().setAll("Tous", "Active", "Inactive");
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
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActive() ? "Active" : "Inactive"));
        statusColumn.setCellFactory(param -> new BadgeCell("admin-chip admin-chip-status"));
        tagColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getTag())));
        tagColumn.setCellFactory(param -> new BadgeCell("admin-chip admin-chip-tag"));
        regionColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getRegion())));
        regionColumn.setCellFactory(param -> new AlignedTextCell(Pos.CENTER_LEFT, "admin-region-cell"));
        membersColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(memberCount(data.getValue()))));
        membersColumn.setCellFactory(param -> new BadgeCell("admin-chip admin-chip-count"));
        visibilityColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isPrivate() ? "Prive" : "Public"));
        visibilityColumn.setCellFactory(param -> new BadgeCell("admin-chip admin-chip-visibility"));
        reportsColumn.setCellValueFactory(data -> new SimpleStringProperty("0"));
        reportsColumn.setCellFactory(param -> new BadgeCell("admin-chip admin-chip-count"));
        createdAtColumn.setCellValueFactory(data -> Bindings.createStringBinding(
                () -> data.getValue().getDateCreation() == null ? "-" : data.getValue().getDateCreation().format(DATE_FORMATTER)
        ));
        createdAtColumn.setCellFactory(param -> new AlignedTextCell(Pos.CENTER_LEFT, "admin-date-cell"));
        actionsColumn.setCellFactory(param -> new TeamActionCell());

        teamsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedEquipe = newValue;
        });

        updateSortButtons();
    }

    private void applyColumnWidths() {
        setColumnWidth(idColumn, 86);
        setColumnWidth(nameColumn, 250);
        setColumnWidth(statusColumn, 126);
        setColumnWidth(tagColumn, 118);
        setColumnWidth(regionColumn, 180);
        setColumnWidth(membersColumn, 112);
        setColumnWidth(visibilityColumn, 132);
        setColumnWidth(reportsColumn, 130);
        setColumnWidth(createdAtColumn, 138);
        setColumnWidth(actionsColumn, 210);
        teamsTable.setMinWidth(1482);
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

    private void deleteEquipe(Equipe equipe) {
        candidatureService.deleteByEquipe(equipe.getId());
        recrutementService.deleteByEquipe(equipe.getId());
        equipeService.deleteEntity(equipe);
        refreshTable();
        selectedEquipe = null;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void applyFiltersAndSort() {
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

    private Image loadLogo(String path) {
        try {
            if (path != null && !path.isBlank()) {
                return new Image(path, true);
            }
        } catch (IllegalArgumentException ignored) {
        }
        return new Image(String.valueOf(getClass().getResource(DEFAULT_LOGO)));
    }

    private final class TeamActionCell extends TableCell<Equipe, Void> {
        private final javafx.scene.control.Button editButton = new javafx.scene.control.Button("Modifier");
        private final javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("Supprimer");
        private final HBox box = new HBox(10, editButton, deleteButton);

        private TeamActionCell() {
            editButton.getStyleClass().addAll("admin-inline-action", "is-info");
            deleteButton.getStyleClass().addAll("admin-inline-action", "is-danger");
            box.setAlignment(Pos.CENTER_LEFT);

            editButton.setOnAction(event -> {
                Equipe equipe = getTableView().getItems().get(getIndex());
                teamsTable.getSelectionModel().select(equipe);
                selectedEquipe = equipe;
                if (parentController != null) {
                    parentController.showTeamEditor(equipe);
                }
            });
            deleteButton.setOnAction(event -> {
                Equipe equipe = getTableView().getItems().get(getIndex());
                deleteEquipe(equipe);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : box);
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
