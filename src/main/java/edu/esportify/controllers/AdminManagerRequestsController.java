package edu.esportify.controllers;

import edu.esportify.entities.ManagerRequest;
import edu.esportify.services.ManagerRequestService;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminManagerRequestsController implements AdminContentController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private enum SortMode {
        DATE_DESC,
        DATE_ASC,
        ID_DESC,
        ID_ASC
    }

    private final ManagerRequestService managerRequestService = new ManagerRequestService();

    private SortMode currentSortMode = SortMode.DATE_DESC;
    private ManagerRequest selectedRequest;

    @FXML private TextField keywordField;
    @FXML private javafx.scene.control.ComboBox<String> statusFilterBox;
    @FXML private VBox searchShell;
    @FXML private HBox tableToolbar;
    @FXML private Button searchButton;
    @FXML private Button resetButton;
    @FXML private Button refreshButton;
    @FXML private Button sortDateDescButton;
    @FXML private Button sortDateAscButton;
    @FXML private Button sortIdDescButton;
    @FXML private Button sortIdAscButton;
    @FXML private TableView<ManagerRequest> requestsTable;
    @FXML private TableColumn<ManagerRequest, String> idColumn;
    @FXML private TableColumn<ManagerRequest, String> usernameColumn;
    @FXML private TableColumn<ManagerRequest, String> emailColumn;
    @FXML private TableColumn<ManagerRequest, String> niveauColumn;
    @FXML private TableColumn<ManagerRequest, String> motivationColumn;
    @FXML private TableColumn<ManagerRequest, String> statusColumn;
    @FXML private TableColumn<ManagerRequest, String> dateColumn;
    @FXML private TableColumn<ManagerRequest, Void> actionsColumn;
    @FXML private Label infoLabel;

    @Override
    public void init(AdminLayoutController parentController) {
        configure();
        refreshTable();
    }

    private void configure() {
        ensureStyleClass(searchShell, "admin-search-shell", "admin-premium-search-shell");
        ensureStyleClass(tableToolbar, "admin-table-toolbar");
        ensureStyleClass(keywordField, "field", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(statusFilterBox, "dark-combo", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(searchButton, "admin-glow-button");
        ensureStyleClass(resetButton, "admin-ghost-button");
        ensureStyleClass(refreshButton, "admin-ghost-button");
        ensureStyleClass(sortDateDescButton, "admin-sort-pill", "admin-sort-button");
        ensureStyleClass(sortDateAscButton, "admin-sort-pill", "admin-sort-button");
        ensureStyleClass(sortIdDescButton, "admin-sort-pill", "admin-sort-button");
        ensureStyleClass(sortIdAscButton, "admin-sort-pill", "admin-sort-button");

        if (!requestsTable.getStyleClass().contains("admin-teams-table")) {
            requestsTable.getStyleClass().add("admin-teams-table");
        }
        requestsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        requestsTable.setPlaceholder(new Label("Aucune demande manager disponible."));
        applyColumnWidths();

        if (statusFilterBox.getItems().isEmpty()) {
            statusFilterBox.getItems().setAll("Tous", "En attente", "Acceptee", "Refusee");
            statusFilterBox.setValue("Tous");
        }

        keywordField.textProperty().addListener((obs, oldValue, newValue) -> applyFiltersAndSort());
        statusFilterBox.valueProperty().addListener((obs, oldValue, newValue) -> applyFiltersAndSort());

        idColumn.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getId()));
        idColumn.setCellFactory(param -> new AlignedTextCell(Pos.CENTER_LEFT, "admin-id-cell"));
        usernameColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getUsername())));
        usernameColumn.setCellFactory(param -> new RequestIdentityCell());
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getEmail())));
        emailColumn.setCellFactory(param -> new AlignedTextCell(Pos.CENTER_LEFT, ""));
        niveauColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getNiveau())));
        niveauColumn.setCellFactory(param -> new BadgeCell("admin-chip admin-chip-tag"));
        motivationColumn.setCellValueFactory(data -> new SimpleStringProperty(trimText(safe(data.getValue().getMotivation()))));
        motivationColumn.setCellFactory(param -> new AlignedTextCell(Pos.CENTER_LEFT, ""));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getStatus())));
        statusColumn.setCellFactory(param -> new BadgeCell(resolveStatusStyle()));
        dateColumn.setCellValueFactory(data -> Bindings.createStringBinding(
                () -> data.getValue().getCreatedAt() == null ? "-" : data.getValue().getCreatedAt().format(DATE_FORMATTER)
        ));
        dateColumn.setCellFactory(param -> new AlignedTextCell(Pos.CENTER_LEFT, "admin-date-cell"));
        actionsColumn.setCellFactory(param -> new RequestActionCell());

        requestsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedRequest = newValue;
            updateInfo(newValue == null ? "Selectionnez une demande pour voir les actions." :
                    "Demande de " + safe(newValue.getUsername()) + " selectionnee.");
        });

        updateSortButtons();
        updateInfo("Les actions du tableau sont dynamiques.");
    }

    private void applyColumnWidths() {
        setColumnWidth(idColumn, 86);
        setColumnWidth(usernameColumn, 220);
        setColumnWidth(emailColumn, 270);
        setColumnWidth(niveauColumn, 140);
        setColumnWidth(motivationColumn, 420);
        setColumnWidth(statusColumn, 126);
        setColumnWidth(dateColumn, 170);
        setColumnWidth(actionsColumn, 260);
        requestsTable.setMinWidth(1692);
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
        keywordField.clear();
        statusFilterBox.setValue("Tous");
        currentSortMode = SortMode.DATE_DESC;
        updateSortButtons();
        applyFiltersAndSort();
        updateInfo("Filtres reinitialises.");
    }

    @FXML
    private void onRefresh() {
        refreshTable();
        updateInfo("Tableau des demandes manager actualise.");
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

    @FXML
    private void onSortIdDesc() {
        currentSortMode = SortMode.ID_DESC;
        updateSortButtons();
        applyFiltersAndSort();
    }

    @FXML
    private void onSortIdAsc() {
        currentSortMode = SortMode.ID_ASC;
        updateSortButtons();
        applyFiltersAndSort();
    }

    private void refreshTable() {
        applyFiltersAndSort();
    }

    private void applyFiltersAndSort() {
        List<ManagerRequest> requests = getRequestsSafe().stream()
                .filter(request -> matchesKeyword(request, safe(keywordField.getText())))
                .filter(request -> matchesStatus(request, safe(statusFilterBox.getValue())))
                .sorted(getComparator())
                .toList();
        requestsTable.setItems(FXCollections.observableArrayList(requests));
    }

    private List<ManagerRequest> getRequestsSafe() {
        try {
            List<ManagerRequest> requests = managerRequestService.getData();
            if (requests != null) {
                return requests;
            }
        } catch (RuntimeException e) {
            System.out.println("Lecture demandes manager impossible: " + e.getMessage());
            updateInfo("Impossible de lire les demandes manager.");
        }
        return List.of();
    }

    private boolean matchesKeyword(ManagerRequest request, String keyword) {
        String normalizedKeyword = safe(keyword).trim().toLowerCase();
        if (normalizedKeyword.isBlank()) {
            return true;
        }
        return safe(request.getUsername()).toLowerCase().contains(normalizedKeyword)
                || safe(request.getEmail()).toLowerCase().contains(normalizedKeyword)
                || safe(request.getNiveau()).toLowerCase().contains(normalizedKeyword)
                || safe(request.getMotivation()).toLowerCase().contains(normalizedKeyword);
    }

    private boolean matchesStatus(ManagerRequest request, String status) {
        String normalizedStatus = safe(status).trim();
        return normalizedStatus.isBlank()
                || "Tous".equalsIgnoreCase(normalizedStatus)
                || safe(request.getStatus()).equalsIgnoreCase(normalizedStatus);
    }

    private Comparator<ManagerRequest> getComparator() {
        return switch (currentSortMode) {
            case DATE_ASC -> Comparator.comparing(
                    ManagerRequest::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );
            case DATE_DESC -> Comparator.comparing(
                    ManagerRequest::getCreatedAt,
                    Comparator.nullsLast(Comparator.reverseOrder())
            );
            case ID_ASC -> Comparator.comparingInt(ManagerRequest::getId);
            case ID_DESC -> Comparator.comparingInt(ManagerRequest::getId).reversed();
        };
    }

    private void updateSortButtons() {
        setSortButtonState(sortDateDescButton, currentSortMode == SortMode.DATE_DESC);
        setSortButtonState(sortDateAscButton, currentSortMode == SortMode.DATE_ASC);
        setSortButtonState(sortIdDescButton, currentSortMode == SortMode.ID_DESC);
        setSortButtonState(sortIdAscButton, currentSortMode == SortMode.ID_ASC);
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

    private void deleteRequest(ManagerRequest request) {
        managerRequestService.deleteEntity(request);
        refreshTable();
        selectedRequest = null;
        updateInfo("Demande supprimee avec succes.");
    }

    private void updateRequestStatus(ManagerRequest request, String status) {
        if (request == null) {
            return;
        }
        request.setStatus(status);
        managerRequestService.updateEntity(request.getId(), request);
        refreshTable();
        ManagerRequest refreshedRequest = managerRequestService.getById(request.getId());
        if (refreshedRequest != null) {
            requestsTable.getSelectionModel().select(refreshedRequest);
            selectedRequest = refreshedRequest;
        }
        updateInfo("Statut de la demande #" + request.getId() + " mis a jour: " + status + ".");
    }

    private String resolveStatusStyle() {
        return "admin-chip admin-chip-status";
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

    private void updateInfo(String message) {
        if (infoLabel != null) {
            infoLabel.setText(message);
        }
    }

    private String trimText(String value) {
        return value.length() > 68 ? value.substring(0, 68) + "..." : value;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private final class RequestActionCell extends TableCell<ManagerRequest, Void> {
        private final Button acceptButton = new Button("Accepter");
        private final Button rejectButton = new Button("Refuser");
        private final Button deleteButton = new Button("Supprimer");
        private final HBox box = new HBox(10, acceptButton, rejectButton, deleteButton);

        private RequestActionCell() {
            acceptButton.getStyleClass().addAll("admin-inline-action", "is-info");
            rejectButton.getStyleClass().addAll("admin-inline-action", "is-neutral");
            deleteButton.getStyleClass().addAll("admin-inline-action", "is-danger");
            box.setAlignment(Pos.CENTER_LEFT);

            acceptButton.setOnAction(event -> {
                ManagerRequest request = getTableView().getItems().get(getIndex());
                requestsTable.getSelectionModel().select(request);
                updateRequestStatus(request, "Acceptee");
            });
            rejectButton.setOnAction(event -> {
                ManagerRequest request = getTableView().getItems().get(getIndex());
                requestsTable.getSelectionModel().select(request);
                updateRequestStatus(request, "Refusee");
            });
            deleteButton.setOnAction(event -> {
                ManagerRequest request = getTableView().getItems().get(getIndex());
                requestsTable.getSelectionModel().select(request);
                deleteRequest(request);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : box);
        }
    }

    private final class BadgeCell extends TableCell<ManagerRequest, String> {
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

    private final class AlignedTextCell extends TableCell<ManagerRequest, String> {
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
            getStyleClass().removeAll("admin-id-cell", "admin-date-cell");
            if (!styleClass.isBlank() && !getStyleClass().contains(styleClass)) {
                getStyleClass().add(styleClass);
            }
        }
    }

    private final class RequestIdentityCell extends TableCell<ManagerRequest, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                setGraphic(null);
                return;
            }

            ManagerRequest request = getTableView().getItems().get(getIndex());

            Label initials = new Label(resolveInitials(request.getUsername()));
            initials.getStyleClass().addAll("admin-chip", "admin-chip-count");
            initials.setMinWidth(40);
            initials.setPrefWidth(40);
            initials.setAlignment(Pos.CENTER);

            Label userLabel = new Label(safe(request.getUsername()));
            userLabel.getStyleClass().add("admin-team-name");

            VBox content = new VBox(4, userLabel);
            HBox box = new HBox(12, initials, content);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setFillHeight(true);
            setGraphic(box);
        }

        private String resolveInitials(String username) {
            String value = safe(username).trim();
            if (value.isBlank()) {
                return "?";
            }
            String[] parts = value.split("\\s+|\\.|_|-");
            StringBuilder builder = new StringBuilder();
            for (String part : parts) {
                if (!part.isBlank()) {
                    builder.append(Character.toUpperCase(part.charAt(0)));
                }
                if (builder.length() == 2) {
                    break;
                }
            }
            return builder.isEmpty() ? value.substring(0, 1).toUpperCase() : builder.toString();
        }
    }
}
