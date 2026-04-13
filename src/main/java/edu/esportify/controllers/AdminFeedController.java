package edu.esportify.controllers;

import edu.esportify.entities.Recrutement;
import edu.esportify.services.RecrutementService;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminFeedController implements AdminContentController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RecrutementService recrutementService = new RecrutementService();

    @FXML private TextField searchField;
    @FXML private TextField dateFromField;
    @FXML private TextField dateToField;
    @FXML private ComboBox<String> perPageBox;
    @FXML private Label totalPublicationsLabel;
    @FXML private Label totalAnnouncementsLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private TableView<Recrutement> postsTable;
    @FXML private TableColumn<Recrutement, String> idColumn;
    @FXML private TableColumn<Recrutement, String> tagColumn;
    @FXML private TableColumn<Recrutement, String> titleColumn;
    @FXML private TableColumn<Recrutement, String> linkColumn;
    @FXML private TableColumn<Recrutement, String> createdColumn;
    @FXML private TableColumn<Recrutement, Void> actionsColumn;

    @Override
    public void init(AdminLayoutController parentController) {
        configure();
        refreshTable();
    }

    private void configure() {
        if (perPageBox.getItems().isEmpty()) {
            perPageBox.getItems().setAll("10", "25", "50");
            perPageBox.setValue("10");
        }

        idColumn.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getId()));
        tagColumn.setCellValueFactory(data -> new SimpleStringProperty("#" + safe(data.getValue().getStatus())));
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getNomRec())));
        linkColumn.setCellValueFactory(data -> new SimpleStringProperty("Voir annonce"));
        createdColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getDatePublication() == null ? "-" : data.getValue().getDatePublication().format(DATE_FORMATTER)
        ));
        actionsColumn.setCellFactory(param -> new FeedActionCell());
    }

    @FXML
    private void onSearch() {
        String keyword = safe(searchField.getText()).trim().toLowerCase();
        List<Recrutement> items = recrutementService.getData().stream()
                .filter(item -> keyword.isBlank()
                        || safe(item.getNomRec()).toLowerCase().contains(keyword)
                        || safe(item.getStatus()).toLowerCase().contains(keyword)
                        || safe(item.getDescription()).toLowerCase().contains(keyword))
                .sorted(Comparator.comparing(Recrutement::getDatePublication, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit())
                .toList();
        postsTable.setItems(FXCollections.observableArrayList(items));
        updateCounters();
    }

    @FXML
    private void onReset() {
        searchField.clear();
        dateFromField.clear();
        dateToField.clear();
        perPageBox.setValue("10");
        refreshTable();
    }

    @FXML
    private void onRefresh() {
        refreshTable();
    }

    private void refreshTable() {
        List<Recrutement> items = recrutementService.getData().stream()
                .sorted(Comparator.comparing(Recrutement::getDatePublication, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit())
                .toList();
        postsTable.setItems(FXCollections.observableArrayList(items));
        updateCounters();
    }

    private void updateCounters() {
        int size = recrutementService.getData().size();
        totalPublicationsLabel.setText("Publications");
        totalAnnouncementsLabel.setText("Annonces");
        totalCommentsLabel.setText("Commentaires");
        if (size > 0) {
            totalPublicationsLabel.setText("Publications (" + size + ")");
            totalAnnouncementsLabel.setText("Annonces (" + size + ")");
            totalCommentsLabel.setText("Commentaires (" + Math.max(1, size / 2) + ")");
        }
    }

    private int limit() {
        try {
            return Integer.parseInt(safe(perPageBox.getValue()));
        } catch (NumberFormatException e) {
            return 10;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private final class FeedActionCell extends TableCell<Recrutement, Void> {
        private final javafx.scene.control.Button viewButton = new javafx.scene.control.Button("Voir");
        private final javafx.scene.control.Button editButton = new javafx.scene.control.Button("Edit");
        private final javafx.scene.control.Button deleteButton = new javafx.scene.control.Button("Supprimer");
        private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(8, viewButton, editButton, deleteButton);

        private FeedActionCell() {
            viewButton.getStyleClass().addAll("admin-inline-action", "is-neutral");
            editButton.getStyleClass().addAll("admin-inline-action", "is-info");
            deleteButton.getStyleClass().addAll("admin-inline-action", "is-danger");

            deleteButton.setOnAction(event -> {
                Recrutement recrutement = getTableView().getItems().get(getIndex());
                recrutementService.deleteEntity(recrutement);
                refreshTable();
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty ? null : box);
        }
    }
}
