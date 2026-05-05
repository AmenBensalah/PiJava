package edu.esportify.controllers;

import edu.esportify.entities.Commentaire;
import edu.esportify.entities.FilActualite;
import edu.esportify.entities.UserProfile;
import edu.esportify.services.CommentaireService;
import edu.esportify.services.FilActualiteService;
import edu.esportify.services.UserDirectoryService;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BackofficeController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);
    private static final List<String> REPORT_KEYWORDS = List.of("signal", "report", "spam", "abuse", "fake", "toxic");
    private static final int OBSOLETE_DAYS_THRESHOLD = 90;

    private final FilActualiteService filActualiteService = new FilActualiteService();
    private final CommentaireService commentaireService = new CommentaireService();
    private final UserDirectoryService userDirectoryService = new UserDirectoryService();
    private final ObservableList<ModerationEntry> moderationItems = FXCollections.observableArrayList();

    private Runnable onDataChanged;
    private boolean publishingEnabled = true;

    @FXML private StackPane dashboardRoot;
    @FXML private Label totalPostsLabel;
    @FXML private Label pendingReportsLabel;
    @FXML private Label pendingReportsHintLabel;
    @FXML private Label engagementRateLabel;
    @FXML private ProgressBar engagementProgressBar;
    @FXML private Label eventsCountLabel;
    @FXML private Label mediaCountLabel;
    @FXML private Label eventsTrendLabel;
    @FXML private Label mediaTrendLabel;
    @FXML private TableView<ModerationEntry> moderationTable;
    @FXML private TableColumn<ModerationEntry, Number> idColumn;
    @FXML private TableColumn<ModerationEntry, String> authorColumn;
    @FXML private TableColumn<ModerationEntry, String> contentColumn;
    @FXML private TableColumn<ModerationEntry, String> dateColumn;
    @FXML private TableColumn<ModerationEntry, ModerationEntry> actionsColumn;
    @FXML private CheckBox publishingToggle;
    @FXML private Label publishingStatusLabel;
    @FXML private Label dashboardStatusLabel;
    @FXML private Region activityBar1;
    @FXML private Region activityBar2;
    @FXML private Region activityBar3;
    @FXML private Region activityBar4;
    @FXML private Region activityBar5;
    @FXML private Region activityBar6;
    @FXML private Region activityBar7;
    @FXML private ProgressBar contentMixProgressBar;
    @FXML private ProgressBar textMixBar;
    @FXML private ProgressBar imageMixBar;
    @FXML private ProgressBar videoMixBar;
    @FXML private Label contentMixPercentLabel;
    @FXML private Label textPostsLabel;
    @FXML private Label imagePostsLabel;
    @FXML private Label videoPostsLabel;
    @FXML private Label topAuthor1Label;
    @FXML private Label topAuthor1CountLabel;
    @FXML private ProgressBar topAuthor1Bar;
    @FXML private Label topAuthor2Label;
    @FXML private Label topAuthor2CountLabel;
    @FXML private ProgressBar topAuthor2Bar;
    @FXML private Label topAuthor3Label;
    @FXML private Label topAuthor3CountLabel;
    @FXML private ProgressBar topAuthor3Bar;

    @FXML
    private void initialize() {
        configureTable();
        refreshDashboard();
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void onDashboardShown() {
        refreshDashboard();
        playEntranceAnimation();
    }

    public void refreshDashboard() {
        loadModerationData();
        updatePublishingStatus();
    }

    @FXML
    private void handleRefreshDashboard() {
        refreshDashboard();
        dashboardStatusLabel.setText("SYNC OK");
        playEntranceAnimation();
    }

    @FXML
    private void handleTogglePublishing() {
        publishingEnabled = publishingToggle.isSelected();
        updatePublishingStatus();
        dashboardStatusLabel.setText(publishingEnabled ? "PUBLISH ON" : "PUBLISH OFF");
    }

    @FXML
    private void handleCleanObsoletePosts() {
        List<FilActualite> posts = filActualiteService.getData();
        LocalDateTime limit = LocalDateTime.now().minusDays(OBSOLETE_DAYS_THRESHOLD);
        int deletedCount = 0;

        for (FilActualite post : posts) {
            if (post.getCreatedAt() != null && post.getCreatedAt().isBefore(limit)) {
                filActualiteService.deleteById(post.getId());
                deletedCount++;
            }
        }

        refreshDashboard();
        notifyParent();
        dashboardStatusLabel.setText(deletedCount == 0 ? "NO OLD POSTS" : deletedCount + " CLEARED");
    }

    private void loadModerationData() {
        List<FilActualite> posts = filActualiteService.getData();
        List<Commentaire> comments = commentaireService.getData();

        moderationItems.setAll(posts.stream().limit(12).map(this::toModerationEntry).toList());
        moderationTable.setItems(moderationItems);

        totalPostsLabel.setText(String.valueOf(posts.size()));

        int pendingReports = calculatePendingReports(posts, comments);
        pendingReportsLabel.setText(String.valueOf(pendingReports));
        pendingReportsHintLabel.setText(pendingReports == 0
                ? "STABLE"
                : "CHECK");

        double engagementRate = calculateEngagementRate(posts, comments);
        engagementRateLabel.setText(String.format(Locale.US, "%.0f%%", engagementRate * 100));
        engagementProgressBar.setProgress(engagementRate);

        long eventsCount = posts.stream().filter(FilActualite::isEvent).count();
        long mediaCount = posts.stream().filter(post -> post.getImagePath() != null || post.getVideoUrl() != null).count();
        eventsCountLabel.setText(String.valueOf(eventsCount));
        mediaCountLabel.setText(String.valueOf(mediaCount));
        eventsTrendLabel.setText(posts.isEmpty() ? "+0%" : "+" + Math.round((eventsCount * 100.0) / posts.size()) + "%");
        mediaTrendLabel.setText(posts.isEmpty() ? "+0%" : "+" + Math.round((mediaCount * 100.0) / posts.size()) + "%");

        updateActivityChart(posts);
        updateContentMix(posts);
        updateTopAuthors(posts);
    }

    private void configureTable() {
        moderationTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().id()));
        authorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().author()));
        contentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().excerpt()));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().formattedDate()));
        actionsColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue()));

        contentColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                Label label = new Label(item);
                label.setWrapText(true);
                label.getStyleClass().add("table-content-cell");
                setGraphic(label);
                setText(null);
            }
        });

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(ModerationEntry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                Button viewButton = createIconButton("view-button", "view-icon");
                viewButton.setOnAction(event -> showDetails(item.post()));

                Button deleteButton = createIconButton("delete-button", "delete-icon");
                deleteButton.setOnAction(event -> deletePost(item.post()));

                HBox actionsBox = new HBox(8, viewButton, deleteButton);
                actionsBox.getStyleClass().add("actions-box");
                setGraphic(actionsBox);
                setText(null);
            }
        });
    }

    private Button createIconButton(String buttonClass, String iconClass) {
        Region icon = new Region();
        icon.getStyleClass().addAll("action-icon", iconClass);

        Button button = new Button();
        button.setGraphic(icon);
        button.getStyleClass().addAll("icon-action-button", buttonClass);
        return button;
    }

    private void showDetails(FilActualite post) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Details publication");
        alert.setHeaderText(post.getDisplayTitle());
        alert.setContentText(buildDetailsText(post));
        alert.showAndWait();
        dashboardStatusLabel.setText("POST " + post.getId());
    }

    private String resolveAuthorName(Integer authorId) {
        if (authorId == null) {
            return "System";
        }
        UserProfile user = userDirectoryService.getUsersById().get(authorId);
        return user == null ? "Auteur #" + authorId : user.getDisplayName();
    }

    private void deletePost(FilActualite post) {
        filActualiteService.deleteById(post.getId());
        refreshDashboard();
        notifyParent();
        dashboardStatusLabel.setText("DELETE " + post.getId());
    }

    private int calculatePendingReports(List<FilActualite> posts, List<Commentaire> comments) {
        int postFlags = (int) posts.stream()
                .filter(post -> containsModerationSignal(post.getContent()) || containsModerationSignal(post.getEventTitle()))
                .count();

        int commentFlags = (int) comments.stream()
                .filter(comment -> containsModerationSignal(comment.getContent()))
                .count();

        return postFlags + commentFlags;
    }

    private double calculateEngagementRate(List<FilActualite> posts, List<Commentaire> comments) {
        if (posts.isEmpty()) {
            return 0;
        }

        Set<Integer> commentedPostIds = new HashSet<>();
        for (Commentaire comment : comments) {
            commentedPostIds.add(comment.getPostId());
        }

        long engagedPosts = posts.stream()
                .filter(post -> commentedPostIds.contains(post.getId())
                        || post.isEvent()
                        || post.getImagePath() != null
                        || post.getVideoUrl() != null)
                .count();

        double rate = (double) engagedPosts / posts.size();
        return Math.max(0, Math.min(rate, 1));
    }

    private boolean containsModerationSignal(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return REPORT_KEYWORDS.stream().anyMatch(normalized::contains);
    }

    private ModerationEntry toModerationEntry(FilActualite post) {
        return new ModerationEntry(
                post.getId(),
                resolveAuthorName(post.getAuthorId()),
                buildExcerpt(post),
                formatDate(post.getCreatedAt()),
                post
        );
    }

    private String buildExcerpt(FilActualite post) {
        String source = post.getContent();
        if (source == null || source.isBlank()) {
            source = post.getEventTitle();
        }
        if (source == null || source.isBlank()) {
            source = "Publication sans contenu texte";
        }
        return source.length() > 90 ? source.substring(0, 90) + "..." : source;
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "--" : DATE_FORMAT.format(dateTime);
    }

    private String buildDetailsText(FilActualite post) {
        StringBuilder builder = new StringBuilder();
        builder.append("ID : ").append(post.getId()).append('\n');
        builder.append("Auteur : ").append(resolveAuthorName(post.getAuthorId())).append('\n');
        builder.append("Date : ").append(formatDate(post.getCreatedAt())).append('\n');
        builder.append("Type : ").append(post.isEvent() ? "Evenement" : safeText(post.getMediaType())).append('\n');
        builder.append("Contenu : ").append(safeText(post.getContent())).append('\n');
        builder.append("Image : ").append(safeText(post.getImagePath())).append('\n');
        builder.append("Video : ").append(safeText(post.getVideoUrl())).append('\n');
        return builder.toString();
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "--" : value;
    }

    private void updatePublishingStatus() {
        if (publishingToggle != null) {
            publishingToggle.setSelected(publishingEnabled);
        }
        publishingStatusLabel.setText(publishingEnabled ? "ON" : "OFF");
    }

    private void updateActivityChart(List<FilActualite> posts) {
        List<Region> bars = List.of(activityBar1, activityBar2, activityBar3, activityBar4, activityBar5, activityBar6, activityBar7);
        LocalDateTime now = LocalDateTime.now();
        int[] counts = new int[7];

        for (FilActualite post : posts) {
            if (post.getCreatedAt() == null) {
                continue;
            }
            long days = java.time.Duration.between(post.getCreatedAt(), now).toDays();
            if (days >= 0 && days < 7) {
                counts[6 - (int) days]++;
            }
        }

        int max = 1;
        for (int count : counts) {
            max = Math.max(max, count);
        }

        for (int i = 0; i < bars.size(); i++) {
            Region bar = bars.get(i);
            if (bar == null) {
                continue;
            }
            double height = 38 + (counts[i] * 120.0 / max);
            bar.setPrefHeight(height);
            bar.setMinHeight(height);
            bar.setMaxHeight(height);
        }
    }

    private void updateContentMix(List<FilActualite> posts) {
        long textCount = posts.stream()
                .filter(post -> post.getImagePath() == null && post.getVideoUrl() == null && !post.isEvent())
                .count();
        long imageCount = posts.stream().filter(post -> post.getImagePath() != null).count();
        long videoCount = posts.stream().filter(post -> post.getVideoUrl() != null).count();
        long mediaCount = imageCount + videoCount;
        double progress = posts.isEmpty() ? 0 : (double) mediaCount / posts.size();

        if (contentMixProgressBar != null) {
            contentMixProgressBar.setProgress(progress);
        }
        if (contentMixPercentLabel != null) {
            contentMixPercentLabel.setText(String.format(Locale.US, "%.0f%%", progress * 100));
        }
        textPostsLabel.setText("Text " + textCount);
        imagePostsLabel.setText("Image " + imageCount);
        videoPostsLabel.setText("Video " + videoCount);
        if (textMixBar != null) {
            textMixBar.setProgress(posts.isEmpty() ? 0 : (double) textCount / posts.size());
        }
        if (imageMixBar != null) {
            imageMixBar.setProgress(posts.isEmpty() ? 0 : (double) imageCount / posts.size());
        }
        if (videoMixBar != null) {
            videoMixBar.setProgress(posts.isEmpty() ? 0 : (double) videoCount / posts.size());
        }
    }

    private void updateTopAuthors(List<FilActualite> posts) {
        List<Map.Entry<String, Long>> topAuthors = posts.stream()
                .collect(Collectors.groupingBy(
                        post -> resolveAuthorName(post.getAuthorId()),
                        Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(3)
                .toList();

        applyAuthorSlot(topAuthor1Label, topAuthor1CountLabel, topAuthors, 0);
        applyAuthorSlot(topAuthor2Label, topAuthor2CountLabel, topAuthors, 1);
        applyAuthorSlot(topAuthor3Label, topAuthor3CountLabel, topAuthors, 2);
    }

    private void applyAuthorSlot(Label nameLabel, Label countLabel, List<Map.Entry<String, Long>> topAuthors, int index) {
        if (nameLabel == null || countLabel == null) {
            return;
        }
        if (index < topAuthors.size()) {
            Map.Entry<String, Long> entry = topAuthors.get(index);
            nameLabel.setText(entry.getKey());
            countLabel.setText(String.valueOf(entry.getValue()));
            double max = topAuthors.get(0).getValue();
            ProgressBar bar = switch (index) {
                case 0 -> topAuthor1Bar;
                case 1 -> topAuthor2Bar;
                default -> topAuthor3Bar;
            };
            if (bar != null) {
                bar.setProgress(max == 0 ? 0 : entry.getValue() / max);
            }
        } else {
            nameLabel.setText("Aucun auteur");
            countLabel.setText("0");
            ProgressBar bar = switch (index) {
                case 0 -> topAuthor1Bar;
                case 1 -> topAuthor2Bar;
                default -> topAuthor3Bar;
            };
            if (bar != null) {
                bar.setProgress(0);
            }
        }
    }

    private void playEntranceAnimation() {
        if (dashboardRoot == null) {
            return;
        }
        dashboardRoot.setOpacity(0);
        dashboardRoot.setTranslateY(24);

        FadeTransition fade = new FadeTransition(Duration.millis(320), dashboardRoot);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(320), dashboardRoot);
        slide.setFromY(24);
        slide.setToY(0);

        new ParallelTransition(fade, slide).play();
    }

    private void notifyParent() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    private record ModerationEntry(int id, String author, String excerpt, String formattedDate, FilActualite post) {
    }
}
