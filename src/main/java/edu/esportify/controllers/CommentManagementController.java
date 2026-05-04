package edu.esportify.controllers;

import edu.esportify.entities.Commentaire;
import edu.esportify.entities.UserProfile;
import edu.esportify.services.CommentaireService;
import edu.esportify.services.UserDirectoryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class CommentManagementController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm", Locale.FRENCH);

    private final CommentaireService commentaireService = new CommentaireService();
    private final UserDirectoryService userDirectoryService = new UserDirectoryService();
    private final ObservableList<Commentaire> allComments = FXCollections.observableArrayList();
    private final ObservableList<Commentaire> visibleComments = FXCollections.observableArrayList();

    private Runnable onDataChanged;
    private Commentaire selectedComment;
    private Predicate<Commentaire> activeFilter = comment -> true;

    @FXML private Label heroStatusLabel;
    @FXML private Label heroCountLabel;
    @FXML private Label totalCommentsLabel;
    @FXML private Label activeAuthorsLabel;
    @FXML private Label targetPostsLabel;
    @FXML private Label commentsDeltaLabel;
    @FXML private Label authorsDeltaLabel;
    @FXML private Label targetsDeltaLabel;
    @FXML private TextField searchField;
    @FXML private Button filterAllButton;
    @FXML private Button filterShortButton;
    @FXML private Button filterLongButton;
    @FXML private ListView<Commentaire> commentsListView;
    @FXML private Label resultCountLabel;
    @FXML private Label postBadgeLabel;
    @FXML private Label selectedTitleLabel;
    @FXML private Label selectedMetaLabel;
    @FXML private Label selectedContentLabel;
    @FXML private Label selectedAuthorLabel;
    @FXML private Label selectedPostLabel;
    @FXML private Label selectedDateLabel;
    @FXML private Label activityStatusLabel;

    @FXML
    private void initialize() {
        configureList();
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        }
        refreshComments();
    }

    public void setOnDataChanged(Runnable onDataChanged) { this.onDataChanged = onDataChanged; }
    public void onViewShown() { refreshComments(); }
    @FXML private void handleRefresh() { refreshComments(); activityStatusLabel.setText("Flux actualise"); }
    @FXML private void handleCreateComment() { openEditor(null); }
    @FXML private void handleEditSelectedComment() {
        if (selectedComment == null) { activityStatusLabel.setText("Selectionnez un commentaire"); return; }
        openEditor(selectedComment);
    }
    @FXML private void handleDeleteSelectedComment() {
        if (selectedComment == null) { activityStatusLabel.setText("Selectionnez un commentaire"); return; }
        try {
            commentaireService.deleteEntity(selectedComment);
            activityStatusLabel.setText("Commentaire supprime");
            refreshComments();
            notifyParent();
        } catch (RuntimeException ex) {
            activityStatusLabel.setText("Erreur suppression: " + ex.getMessage());
        }
    }
    @FXML private void handleOpenDetailView() {
        if (selectedComment == null) { activityStatusLabel.setText("Selectionnez un commentaire"); return; }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Details commentaire");
        alert.setHeaderText("Commentaire #" + selectedComment.getId());
        alert.setContentText(buildDetailText(selectedComment));
        alert.showAndWait();
    }
    @FXML private void handleFilterAll() { activeFilter = comment -> true; applyFilters(); markActiveFilter(filterAllButton); }
    @FXML private void handleFilterShort() { activeFilter = comment -> comment.getContent() != null && comment.getContent().length() < 80; applyFilters(); markActiveFilter(filterShortButton); }
    @FXML private void handleFilterLong() { activeFilter = comment -> comment.getContent() != null && comment.getContent().length() >= 80; applyFilters(); markActiveFilter(filterLongButton); }

    private void configureList() {
        commentsListView.setItems(visibleComments);
        commentsListView.setCellFactory(list -> new CommentCell());
        commentsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedComment = newValue;
            updateInspector(newValue);
            commentsListView.refresh();
        });
    }

    private void refreshComments() {
        allComments.setAll(commentaireService.getData());
        applyFilters();
        updateStats();
        if (!visibleComments.isEmpty()) {
            commentsListView.getSelectionModel().select(visibleComments.get(0));
        } else {
            updateInspector(null);
        }
    }

    private void applyFilters() {
        String query = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        visibleComments.setAll(allComments.stream()
                .filter(activeFilter)
                .filter(comment -> query.isBlank() || matchesQuery(comment, query))
                .toList());
        resultCountLabel.setText(visibleComments.size() + " resultats");
        heroCountLabel.setText(allComments.size() + " commentaires");
        heroStatusLabel.setText(visibleComments.isEmpty() ? "Aucun resultat" : "Watch actif");
    }

    private boolean matchesQuery(Commentaire comment, String query) {
        return contains(comment.getContent(), query)
                || String.valueOf(comment.getAuthorId()).contains(query)
                || String.valueOf(comment.getPostId()).contains(query);
    }

    private boolean contains(String source, String query) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(query);
    }

    private void markActiveFilter(Button activeButton) {
        for (Button button : List.of(filterAllButton, filterShortButton, filterLongButton)) {
            if (button == null) continue;
            button.getStyleClass().remove("active");
            if (button == activeButton) button.getStyleClass().add("active");
        }
    }

    private void updateStats() {
        long authors = allComments.stream().map(Commentaire::getAuthorId).distinct().count();
        long posts = allComments.stream().map(Commentaire::getPostId).distinct().count();
        totalCommentsLabel.setText(String.valueOf(allComments.size()));
        activeAuthorsLabel.setText(String.valueOf(authors));
        targetPostsLabel.setText(String.valueOf(posts));
        commentsDeltaLabel.setText("Flux global");
        authorsDeltaLabel.setText(authors + " auteurs actifs");
        targetsDeltaLabel.setText(posts + " posts cibles");
    }

    private void updateInspector(Commentaire comment) {
        if (comment == null) {
            postBadgeLabel.setText("POST");
            selectedTitleLabel.setText("Selectionnez un commentaire");
            selectedMetaLabel.setText("Auteur - Post");
            selectedContentLabel.setText("Le detail du commentaire apparait ici.");
            selectedAuthorLabel.setText("-");
            selectedPostLabel.setText("-");
            selectedDateLabel.setText("-");
            return;
        }
        postBadgeLabel.setText("POST #" + comment.getPostId());
        selectedTitleLabel.setText("Commentaire #" + comment.getId());
        selectedMetaLabel.setText(resolveAuthorName(comment.getAuthorId()) + " - Post #" + comment.getPostId());
        selectedContentLabel.setText(comment.getContent());
        selectedAuthorLabel.setText(resolveAuthorName(comment.getAuthorId()));
        selectedPostLabel.setText("#" + comment.getPostId());
        selectedDateLabel.setText(comment.getCreatedAt() == null ? "-" : DATE_FORMAT.format(comment.getCreatedAt()));
    }

    private void openEditor(Commentaire comment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/CommentEditorDialog.fxml"));
            Parent root = loader.load();
            CommentEditorDialogController controller = loader.getController();
            Stage stage = new Stage();
            controller.setStage(stage);
            controller.setComment(comment);
            Scene scene = new Scene(root, 620, 500);
            scene.getStylesheets().add(getClass().getResource("/post-management.css").toExternalForm());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(comment == null ? "Nouveau commentaire" : "Modifier le commentaire");
            stage.setScene(scene);
            stage.showAndWait();
            if (!controller.isSaved()) return;
            Commentaire edited = controller.buildComment();
            try {
                if (comment == null) {
                    commentaireService.addEntity(edited);
                    activityStatusLabel.setText("Commentaire cree");
                } else {
                    commentaireService.updateEntity(comment.getId(), edited);
                    activityStatusLabel.setText("Commentaire mis a jour");
                }
                refreshComments();
                notifyParent();
            } catch (RuntimeException ex) {
                activityStatusLabel.setText("Erreur CRUD commentaire: " + ex.getMessage());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'ouvrir l'editeur: " + ex.getMessage(), ex);
        }
    }

    private String buildDetailText(Commentaire comment) {
        return "ID: " + comment.getId()
                + "\nAuteur: " + resolveAuthorName(comment.getAuthorId())
                + "\nPost: #" + comment.getPostId()
                + "\nDate: " + (comment.getCreatedAt() == null ? "-" : DATE_FORMAT.format(comment.getCreatedAt()))
                + "\n\nContenu:\n" + comment.getContent();
    }

    private String resolveAuthorName(int authorId) {
        UserProfile user = userDirectoryService.getUsersById().get(authorId);
        return user == null ? "Auteur #" + authorId : user.getDisplayName();
    }

    private void notifyParent() {
        if (onDataChanged != null) onDataChanged.run();
    }

    private final class CommentCell extends ListCell<Commentaire> {
        @Override
        protected void updateItem(Commentaire item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            VBox card = new VBox(12);
            card.getStyleClass().add("pm-post-card");
            if (selectedComment != null && selectedComment.getId() == item.getId()) {
                card.getStyleClass().add("pm-post-card-selected");
            }
            HBox top = new HBox(10);
            top.setAlignment(Pos.CENTER_LEFT);
            Label badge = new Label("POST #" + item.getPostId());
            badge.getStyleClass().addAll("pm-card-badge", "pm-badge-text");
            Label title = new Label(resolveAuthorName(item.getAuthorId()));
            title.getStyleClass().add("pm-card-title");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label date = new Label(item.getCreatedAt() == null ? "-" : DATE_FORMAT.format(item.getCreatedAt()));
            date.getStyleClass().add("pm-card-meta");
            top.getChildren().addAll(badge, title, spacer, date);
            Label snippet = new Label(item.getDisplayContent());
            snippet.setWrapText(true);
            snippet.getStyleClass().add("pm-card-snippet");
            card.getChildren().addAll(top, snippet);
            VBox.setMargin(card, new Insets(0, 0, 2, 0));
            setGraphic(card);
        }
    }
}
