package edu.esportify.controllers;

import edu.esportify.entities.Commentaire;
import edu.esportify.entities.FilActualite;
import edu.esportify.entities.UserProfile;
import edu.esportify.services.CommentaireService;
import edu.esportify.services.FilActualiteService;
import edu.esportify.services.UserDirectoryService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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

public class PostManagementController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm", Locale.FRENCH);

    private final FilActualiteService postService = new FilActualiteService();
    private final CommentaireService commentaireService = new CommentaireService();
    private final UserDirectoryService userDirectoryService = new UserDirectoryService();
    private final ObservableList<FilActualite> allPosts = FXCollections.observableArrayList();
    private final ObservableList<FilActualite> visiblePosts = FXCollections.observableArrayList();

    private Runnable onDataChanged;
    private FilActualite selectedPost;
    private Predicate<FilActualite> activeFilter = post -> true;

    @FXML private Label heroStatusLabel;
    @FXML private Label heroCountLabel;
    @FXML private Label totalPostsLabel;
    @FXML private Label eventPostsLabel;
    @FXML private Label mediaPostsLabel;
    @FXML private Label engagementLabel;
    @FXML private Label postsDeltaLabel;
    @FXML private Label eventDeltaLabel;
    @FXML private Label mediaDeltaLabel;
    @FXML private Label engagementHintLabel;
    @FXML private TextField searchField;
    @FXML private Button filterAllButton;
    @FXML private Button filterMediaButton;
    @FXML private Button filterEventsButton;
    @FXML private Button filterTextButton;
    @FXML private ListView<FilActualite> postsListView;
    @FXML private Label resultCountLabel;
    @FXML private Label typeBadgeLabel;
    @FXML private Label selectedTitleLabel;
    @FXML private Label selectedMetaLabel;
    @FXML private Label selectedContentLabel;
    @FXML private Label selectedAuthorLabel;
    @FXML private Label selectedDateLabel;
    @FXML private Label selectedMediaLabel;
    @FXML private Label selectedEventLabel;
    @FXML private ImageView selectedMediaPreview;
    @FXML private Label selectedMediaHintLabel;
    @FXML private Label activityStatusLabel;

    @FXML
    private void initialize() {
        configureList();
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        }
        refreshPosts();
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void onViewShown() {
        refreshPosts();
    }

    @FXML
    private void handleRefresh() {
        refreshPosts();
        activityStatusLabel.setText("Flux actualise");
    }

    @FXML
    private void handleCreatePost() {
        openEditor(null);
    }

    @FXML
    private void handleEditSelectedPost() {
        if (selectedPost == null) {
            activityStatusLabel.setText("Selectionnez un post");
            return;
        }
        openEditor(selectedPost);
    }

    @FXML
    private void handleDeleteSelectedPost() {
        if (selectedPost == null) {
            activityStatusLabel.setText("Selectionnez un post");
            return;
        }
        try {
            postService.deleteById(selectedPost.getId());
            activityStatusLabel.setText("Post supprime");
            refreshPosts();
            notifyParent();
        } catch (RuntimeException ex) {
            activityStatusLabel.setText("Erreur suppression: " + ex.getMessage());
        }
    }

    @FXML
    private void handleOpenDetailView() {
        if (selectedPost == null) {
            activityStatusLabel.setText("Selectionnez un post");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Details publication");
        alert.setHeaderText(selectedPost.getDisplayTitle());
        alert.setContentText(buildDetailText(selectedPost));
        alert.showAndWait();
    }

    @FXML private void handleFilterAll() { activeFilter = post -> true; applyFilters(); markActiveFilter(filterAllButton); }
    @FXML private void handleFilterMedia() { activeFilter = post -> resolveMediaUrl(post) != null; applyFilters(); markActiveFilter(filterMediaButton); }
    @FXML private void handleFilterEvents() { activeFilter = FilActualite::isEvent; applyFilters(); markActiveFilter(filterEventsButton); }
    @FXML private void handleFilterText() { activeFilter = post -> !post.isEvent() && resolveMediaUrl(post) == null; applyFilters(); markActiveFilter(filterTextButton); }

    private void configureList() {
        postsListView.setItems(visiblePosts);
        postsListView.setCellFactory(list -> new PostCardCell());
        postsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedPost = newValue;
            updateInspector(newValue);
            postsListView.refresh();
        });
    }

    private void refreshPosts() {
        allPosts.setAll(postService.getData());
        applyFilters();
        updateStats();
        if (!visiblePosts.isEmpty()) {
            postsListView.getSelectionModel().select(visiblePosts.get(0));
        } else {
            updateInspector(null);
        }
    }

    private void applyFilters() {
        String query = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        visiblePosts.setAll(allPosts.stream()
                .filter(activeFilter)
                .filter(post -> query.isBlank() || matchesQuery(post, query))
                .toList());
        resultCountLabel.setText(visiblePosts.size() + " resultats");
        heroCountLabel.setText(allPosts.size() + " posts");
        heroStatusLabel.setText(visiblePosts.isEmpty() ? "Aucun resultat" : "Studio actif");
        if (selectedPost != null && visiblePosts.stream().noneMatch(post -> post.getId() == selectedPost.getId())) {
            selectedPost = null;
        }
    }

    private void markActiveFilter(Button activeButton) {
        for (Button button : List.of(filterAllButton, filterMediaButton, filterEventsButton, filterTextButton)) {
            if (button == null) {
                continue;
            }
            button.getStyleClass().remove("active");
            if (button == activeButton) {
                button.getStyleClass().add("active");
            }
        }
    }

    private boolean matchesQuery(FilActualite post, String query) {
        return contains(post.getContent(), query)
                || contains(post.getEventTitle(), query)
                || contains(post.getEventLocation(), query)
                || contains(post.getAuthorId() == null ? null : String.valueOf(post.getAuthorId()), query);
    }

    private boolean contains(String source, String query) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(query);
    }

    private void updateStats() {
        long eventCount = allPosts.stream().filter(FilActualite::isEvent).count();
        long mediaCount = allPosts.stream().filter(post -> resolveMediaUrl(post) != null).count();
        List<Commentaire> comments = commentaireService.getData();
        long engagedCount = allPosts.stream().filter(post -> comments.stream().anyMatch(comment -> comment.getPostId() == post.getId())).count();
        int engagement = allPosts.isEmpty() ? 0 : (int) Math.round((engagedCount * 100.0) / allPosts.size());

        totalPostsLabel.setText(String.valueOf(allPosts.size()));
        eventPostsLabel.setText(String.valueOf(eventCount));
        mediaPostsLabel.setText(String.valueOf(mediaCount));
        engagementLabel.setText(engagement + "%");
        postsDeltaLabel.setText("Mur global");
        eventDeltaLabel.setText(eventCount == 0 ? "Aucun live" : eventCount + " posts evenement");
        mediaDeltaLabel.setText(mediaCount == 0 ? "Aucun media" : mediaCount + " posts media");
        engagementHintLabel.setText(engagement == 0 ? "Encore calme" : "Interaction visible");
    }

    private void updateInspector(FilActualite post) {
        if (post == null) {
            typeBadgeLabel.setText("TYPE");
            selectedTitleLabel.setText("Selectionnez une publication");
            selectedMetaLabel.setText("Auteur - Date");
            selectedContentLabel.setText("Le detail du post apparait ici.");
            selectedAuthorLabel.setText("-");
            selectedDateLabel.setText("-");
            selectedMediaLabel.setText("Aucun");
            selectedEventLabel.setText("Non");
            updateMediaPreview(null, null);
            return;
        }
        typeBadgeLabel.setText(resolveBadge(post));
        selectedTitleLabel.setText(post.getDisplayTitle());
        selectedMetaLabel.setText("Auteur " + resolveAuthorName(post.getAuthorId()));
        String displayContent = stripEmbeddedMediaUrl(post.getContent());
        selectedContentLabel.setText(displayContent == null ? "Publication media / evenement" : displayContent);
        selectedAuthorLabel.setText(resolveAuthorName(post.getAuthorId()));
        selectedDateLabel.setText(post.getCreatedAt() == null ? "-" : DATE_FORMAT.format(post.getCreatedAt()));
        selectedMediaLabel.setText(buildMediaLabel(post));
        selectedEventLabel.setText(post.isEvent() ? "Oui" : "Non");
        updateMediaPreview(resolveImageSource(post), resolveVideoSource(post));
    }

    private String buildMediaLabel(FilActualite post) {
        if (resolveVideoSource(post) != null) {
            return "Video";
        }
        if (resolveImageSource(post) != null) {
            return "Image";
        }
        return "Aucun";
    }

    private String resolveBadge(FilActualite post) {
        if (post.isEvent()) {
            return "EVENT";
        }
        if (resolveMediaUrl(post) != null) {
            return "MEDIA";
        }
        return "TEXT";
    }

    private void openEditor(FilActualite post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PostEditorDialog.fxml"));
            Parent root = loader.load();
            PostEditorDialogController controller = loader.getController();
            Stage stage = new Stage();
            controller.setStage(stage);
            controller.setPost(post);
            Scene scene = new Scene(root, 620, 680);
            scene.getStylesheets().add(getClass().getResource("/post-management.css").toExternalForm());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(post == null ? "Nouveau post" : "Modifier le post");
            stage.setScene(scene);
            stage.showAndWait();
            if (!controller.isSaved()) {
                return;
            }
            FilActualite edited = controller.buildPost();
            try {
                if (post == null) {
                    postService.addEntity(edited);
                    activityStatusLabel.setText("Nouveau post cree");
                } else {
                    postService.updateEntity(post.getId(), edited);
                    activityStatusLabel.setText("Post mis a jour");
                }
                refreshPosts();
                notifyParent();
            } catch (RuntimeException ex) {
                activityStatusLabel.setText("Erreur CRUD post: " + ex.getMessage());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'ouvrir l'editeur: " + ex.getMessage(), ex);
        }
    }

    private String buildDetailText(FilActualite post) {
        return "ID: " + post.getId()
                + "\nAuteur: " + resolveAuthorName(post.getAuthorId())
                + "\nDate: " + (post.getCreatedAt() == null ? "-" : DATE_FORMAT.format(post.getCreatedAt()))
                + "\nType: " + resolveBadge(post)
                + "\nMedia: " + buildMediaLabel(post)
                + "\nEvent: " + (post.isEvent() ? "Oui" : "Non")
                + "\n\nContenu:\n" + (stripEmbeddedMediaUrl(post.getContent()) == null ? "-" : stripEmbeddedMediaUrl(post.getContent()));
    }

    private void notifyParent() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    private void updateMediaPreview(String imageUrl, String videoUrl) {
        if (selectedMediaPreview == null || selectedMediaHintLabel == null) {
            return;
        }
        String imageCandidate = trimToNull(imageUrl);
        if (imageCandidate != null) {
            try {
                selectedMediaPreview.setImage(new Image(imageCandidate, true));
                selectedMediaPreview.setVisible(true);
                selectedMediaPreview.setManaged(true);
                selectedMediaHintLabel.setText("Image detectee");
                return;
            } catch (IllegalArgumentException ignored) {
                selectedMediaHintLabel.setText(imageCandidate);
            }
        }
        selectedMediaPreview.setImage(null);
        selectedMediaPreview.setVisible(false);
        selectedMediaPreview.setManaged(false);
        selectedMediaHintLabel.setText(trimToNull(videoUrl) != null ? trimToNull(videoUrl) : "Aucun media a afficher");
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String resolveAuthorName(Integer authorId) {
        if (authorId == null) {
            return "System";
        }
        UserProfile user = userDirectoryService.getUsersById().get(authorId);
        return user == null ? "Auteur #" + authorId : user.getDisplayName();
    }

    private String extractEmbeddedMediaUrl(String content) {
        String normalized = trimToNull(content);
        if (normalized == null) {
            return null;
        }
        for (String token : normalized.split("\\s+")) {
            String candidate = token.replaceAll("^[\\p{Punct}]+|[\\p{Punct}]+$", "");
            if (candidate.startsWith("http://")
                    || candidate.startsWith("https://")
                    || candidate.startsWith("/uploads/")
                    || candidate.matches("^[A-Za-z]:\\\\.*")) {
                return candidate;
            }
        }
        return null;
    }

    private String stripEmbeddedMediaUrl(String content) {
        String normalized = trimToNull(content);
        if (normalized == null) {
            return null;
        }
        String mediaUrl = extractEmbeddedMediaUrl(normalized);
        if (mediaUrl == null) {
            return normalized;
        }
        String stripped = normalized.replace(mediaUrl, "").replaceAll("\\s{2,}", " ").trim();
        return stripped.isEmpty() ? null : stripped;
    }

    private String resolveMediaUrl(FilActualite post) {
        if (post == null) {
            return null;
        }
        String imageSource = resolveImageSource(post);
        if (imageSource != null) {
            return imageSource;
        }
        String videoSource = resolveVideoSource(post);
        if (videoSource != null) {
            return videoSource;
        }
        return null;
    }

    private String resolveImageSource(FilActualite post) {
        if (post == null) {
            return null;
        }
        String explicit = trimToNull(post.getImagePath());
        if (explicit != null) {
            return explicit;
        }
        String embedded = extractEmbeddedMediaUrl(post.getContent());
        return isImageLike(embedded) ? embedded : null;
    }

    private String resolveVideoSource(FilActualite post) {
        if (post == null) {
            return null;
        }
        String explicit = trimToNull(post.getVideoUrl());
        if (explicit != null) {
            return explicit;
        }
        String embedded = extractEmbeddedMediaUrl(post.getContent());
        return embedded != null && !isImageLike(embedded) ? embedded : null;
    }

    private boolean isImageLike(String source) {
        String normalized = trimToNull(source);
        if (normalized == null) {
            return false;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png")
                || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg")
                || lower.endsWith(".gif")
                || lower.endsWith(".webp");
    }

    private final class PostCardCell extends ListCell<FilActualite> {
        @Override
        protected void updateItem(FilActualite item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            VBox card = new VBox(12);
            card.getStyleClass().add("pm-post-card");
            if (selectedPost != null && selectedPost.getId() == item.getId()) {
                card.getStyleClass().add("pm-post-card-selected");
            }

            HBox top = new HBox(10);
            top.setAlignment(Pos.CENTER_LEFT);

            Label badge = new Label(resolveBadge(item));
            badge.getStyleClass().add("pm-card-badge");
            badge.getStyleClass().add(item.isEvent() ? "pm-badge-event" : (resolveMediaUrl(item) != null ? "pm-badge-media" : "pm-badge-text"));

            Label title = new Label(item.getDisplayTitle());
            title.getStyleClass().add("pm-card-title");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label date = new Label(item.getCreatedAt() == null ? "-" : DATE_FORMAT.format(item.getCreatedAt()));
            date.getStyleClass().add("pm-card-meta");

            top.getChildren().addAll(badge, title, spacer, date);

            String displayContent = stripEmbeddedMediaUrl(item.getContent());
            Label snippet = new Label(displayContent == null ? "Publication sans texte principal" : displayContent);
            snippet.setWrapText(true);
            snippet.getStyleClass().add("pm-card-snippet");

            HBox meta = new HBox(10);
            meta.setAlignment(Pos.CENTER_LEFT);
            Label author = new Label("Auteur " + resolveAuthorName(item.getAuthorId()));
            author.getStyleClass().add("pm-card-meta");
            Label media = new Label(buildMediaLabel(item));
            media.getStyleClass().add("pm-card-meta");
            meta.getChildren().addAll(author, media);

            card.getChildren().addAll(top, snippet, meta);
            VBox.setMargin(card, new Insets(0, 0, 2, 0));
            setGraphic(card);
        }
    }
}
