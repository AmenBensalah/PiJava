package edu.esportify.controllers;

import edu.esportify.entities.Announcement;
import edu.esportify.services.AnnouncementService;
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

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class AnnouncementManagementController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm", Locale.FRENCH);
    private static final String PUBLIC_MEDIA_ROOT = "C:/Users/bouzi/Desktop/PI_DEV_ESPORTIFY/public";

    private final AnnouncementService announcementService = new AnnouncementService();
    private final ObservableList<Announcement> allAnnouncements = FXCollections.observableArrayList();
    private final ObservableList<Announcement> visibleAnnouncements = FXCollections.observableArrayList();

    private Runnable onDataChanged;
    private Announcement selectedAnnouncement;
    private Predicate<Announcement> activeFilter = announcement -> true;

    @FXML private Label heroStatusLabel;
    @FXML private Label heroCountLabel;
    @FXML private Label totalAnnouncementsLabel;
    @FXML private Label activeTagsLabel;
    @FXML private Label linkedAnnouncementsLabel;
    @FXML private Label announcementsDeltaLabel;
    @FXML private Label tagsDeltaLabel;
    @FXML private Label linksDeltaLabel;
    @FXML private TextField searchField;
    @FXML private Button filterAllButton;
    @FXML private Button filterTaggedButton;
    @FXML private Button filterLinkedButton;
    @FXML private ListView<Announcement> announcementsListView;
    @FXML private Label resultCountLabel;
    @FXML private Label tagBadgeLabel;
    @FXML private Label selectedTitleLabel;
    @FXML private Label selectedMetaLabel;
    @FXML private Label selectedContentLabel;
    @FXML private Label selectedTagLabel;
    @FXML private Label selectedDateLabel;
    @FXML private Label selectedLinkLabel;
    @FXML private ImageView selectedMediaPreview;
    @FXML private Label selectedMediaHintLabel;
    @FXML private Label activityStatusLabel;

    @FXML
    private void initialize() {
        configureList();
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        }
        refreshAnnouncements();
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void onViewShown() {
        refreshAnnouncements();
    }

    @FXML private void handleRefresh() { refreshAnnouncements(); activityStatusLabel.setText("Canal actualise"); }
    @FXML private void handleCreateAnnouncement() { openEditor(null); }
    @FXML private void handleEditSelectedAnnouncement() {
        if (selectedAnnouncement == null) { activityStatusLabel.setText("Selectionnez une annonce"); return; }
        openEditor(selectedAnnouncement);
    }
    @FXML private void handleDeleteSelectedAnnouncement() {
        if (selectedAnnouncement == null) { activityStatusLabel.setText("Selectionnez une annonce"); return; }
        try {
            announcementService.deleteEntity(selectedAnnouncement);
            activityStatusLabel.setText("Annonce supprimee");
            refreshAnnouncements();
            notifyParent();
        } catch (RuntimeException ex) {
            activityStatusLabel.setText("Erreur suppression: " + ex.getMessage());
        }
    }
    @FXML private void handleOpenDetailView() {
        if (selectedAnnouncement == null) { activityStatusLabel.setText("Selectionnez une annonce"); return; }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Details annonce");
        alert.setHeaderText(selectedAnnouncement.getDisplayTitle());
        alert.setContentText(buildDetailText(selectedAnnouncement));
        alert.showAndWait();
    }
    @FXML private void handleFilterAll() { activeFilter = announcement -> true; applyFilters(); markActiveFilter(filterAllButton); }
    @FXML private void handleFilterTagged() { activeFilter = announcement -> announcement.getTag() != null && !announcement.getTag().isBlank(); applyFilters(); markActiveFilter(filterTaggedButton); }
    @FXML private void handleFilterLinked() { activeFilter = announcement -> announcement.getLink() != null && !announcement.getLink().isBlank(); applyFilters(); markActiveFilter(filterLinkedButton); }

    private void configureList() {
        announcementsListView.setItems(visibleAnnouncements);
        announcementsListView.setCellFactory(list -> new AnnouncementCell());
        announcementsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedAnnouncement = newValue;
            updateInspector(newValue);
            announcementsListView.refresh();
        });
    }

    private void refreshAnnouncements() {
        allAnnouncements.setAll(announcementService.getData());
        applyFilters();
        updateStats();
        if (!visibleAnnouncements.isEmpty()) {
            announcementsListView.getSelectionModel().select(visibleAnnouncements.get(0));
        } else {
            updateInspector(null);
        }
    }

    private void applyFilters() {
        String query = searchField == null || searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase(Locale.ROOT);
        visibleAnnouncements.setAll(allAnnouncements.stream()
                .filter(activeFilter)
                .filter(announcement -> query.isBlank() || matchesQuery(announcement, query))
                .toList());
        resultCountLabel.setText(visibleAnnouncements.size() + " resultats");
        heroCountLabel.setText(allAnnouncements.size() + " annonces");
        heroStatusLabel.setText(visibleAnnouncements.isEmpty() ? "Aucun resultat" : "Canal actif");
    }

    private boolean matchesQuery(Announcement announcement, String query) {
        return contains(announcement.getTitle(), query)
                || contains(announcement.getTag(), query)
                || contains(announcement.getContent(), query);
    }

    private boolean contains(String source, String query) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(query);
    }

    private void markActiveFilter(Button activeButton) {
        for (Button button : List.of(filterAllButton, filterTaggedButton, filterLinkedButton)) {
            if (button == null) continue;
            button.getStyleClass().remove("active");
            if (button == activeButton) button.getStyleClass().add("active");
        }
    }

    private void updateStats() {
        long tagCount = allAnnouncements.stream().map(Announcement::getTag).filter(tag -> tag != null && !tag.isBlank()).distinct().count();
        long linkedCount = allAnnouncements.stream().filter(a -> a.getLink() != null && !a.getLink().isBlank()).count();
        totalAnnouncementsLabel.setText(String.valueOf(allAnnouncements.size()));
        activeTagsLabel.setText(String.valueOf(tagCount));
        linkedAnnouncementsLabel.setText(String.valueOf(linkedCount));
        announcementsDeltaLabel.setText("Canal global");
        tagsDeltaLabel.setText(tagCount + " tags visibles");
        linksDeltaLabel.setText(linkedCount + " liens actifs");
    }

    private void updateInspector(Announcement announcement) {
        if (announcement == null) {
            tagBadgeLabel.setText("TAG");
            selectedTitleLabel.setText("Selectionnez une annonce");
            selectedMetaLabel.setText("Tag - Date");
            selectedContentLabel.setText("Le detail de l'annonce apparait ici.");
            selectedTagLabel.setText("-");
            selectedDateLabel.setText("-");
            selectedLinkLabel.setText("Aucun");
            updateMediaPreview(null, null, null);
            return;
        }
        tagBadgeLabel.setText(announcement.getTag() == null || announcement.getTag().isBlank() ? "INFO" : announcement.getTag().toUpperCase(Locale.ROOT));
        selectedTitleLabel.setText(announcement.getDisplayTitle());
        selectedMetaLabel.setText((announcement.getTag() == null ? "Sans tag" : announcement.getTag()) + " - " + formatDate(announcement));
        selectedContentLabel.setText(announcement.getContent() == null || announcement.getContent().isBlank() ? "Annonce sans contenu detaille" : announcement.getContent());
        selectedTagLabel.setText(announcement.getTag() == null || announcement.getTag().isBlank() ? "-" : announcement.getTag());
        selectedDateLabel.setText(formatDate(announcement));
        selectedLinkLabel.setText(announcement.getLink() == null || announcement.getLink().isBlank() ? "Aucun" : announcement.getLink());
        updateMediaPreview(announcement.getLink(), announcement.getMediaFilename(), announcement.getMediaType());
    }

    private void openEditor(Announcement announcement) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AnnouncementEditorDialog.fxml"));
            Parent root = loader.load();
            AnnouncementEditorDialogController controller = loader.getController();
            Stage stage = new Stage();
            controller.setStage(stage);
            controller.setAnnouncement(announcement);
            Scene scene = new Scene(root, 620, 620);
            scene.getStylesheets().add(getClass().getResource("/post-management.css").toExternalForm());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(announcement == null ? "Nouvelle annonce" : "Modifier l'annonce");
            stage.setScene(scene);
            stage.showAndWait();
            if (!controller.isSaved()) return;
            Announcement edited = controller.buildAnnouncement();
            try {
                if (announcement == null) {
                    announcementService.addEntity(edited);
                    activityStatusLabel.setText("Annonce creee");
                } else {
                    announcementService.updateEntity(announcement.getId(), edited);
                    activityStatusLabel.setText("Annonce mise a jour");
                }
                refreshAnnouncements();
                notifyParent();
            } catch (RuntimeException ex) {
                activityStatusLabel.setText("Erreur CRUD annonce: " + ex.getMessage());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'ouvrir l'editeur: " + ex.getMessage(), ex);
        }
    }

    private String buildDetailText(Announcement announcement) {
        return "ID: " + announcement.getId()
                + "\nTag: " + defaultText(announcement.getTag())
                + "\nDate: " + formatDate(announcement)
                + "\nLien: " + defaultText(announcement.getLink())
                + "\n\nContenu:\n" + defaultText(announcement.getContent());
    }

    private String formatDate(Announcement announcement) {
        return announcement.getCreatedAt() == null ? "-" : DATE_FORMAT.format(announcement.getCreatedAt());
    }

    private String defaultText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void notifyParent() {
        if (onDataChanged != null) onDataChanged.run();
    }

    private void updateMediaPreview(String link, String mediaFilename, String mediaType) {
        if (selectedMediaPreview == null || selectedMediaHintLabel == null) {
            return;
        }
        String candidate = pickMediaCandidate(link, mediaFilename, mediaType);
        if (candidate != null && isLikelyImage(candidate, mediaType)) {
            try {
                selectedMediaPreview.setImage(new Image(candidate, true));
                selectedMediaPreview.setVisible(true);
                selectedMediaPreview.setManaged(true);
                selectedMediaHintLabel.setText("Media image detecte");
                return;
            } catch (IllegalArgumentException ignored) {
                selectedMediaHintLabel.setText(candidate);
            }
        }
        selectedMediaPreview.setImage(null);
        selectedMediaPreview.setVisible(false);
        selectedMediaPreview.setManaged(false);
        selectedMediaHintLabel.setText(candidate != null ? candidate : "Aucun media a afficher");
    }

    private String pickMediaCandidate(String link, String mediaFilename, String mediaType) {
        link = normalizeMediaPath(link);
        mediaFilename = normalizeMediaPath(mediaFilename);
        String normalizedLink = trimToNull(link);
        String normalizedFilename = trimToNull(mediaFilename);
        if (isLikelyImage(normalizedLink, mediaType)) {
            return normalizedLink;
        }
        if (isLikelyImage(normalizedFilename, mediaType)) {
            return normalizedFilename;
        }
        return normalizedLink != null ? normalizedLink : normalizedFilename;
    }

    private String normalizeMediaPath(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        if (trimmed.startsWith("/uploads/")) {
            return new File(PUBLIC_MEDIA_ROOT + trimmed).toURI().toString();
        }
        if (trimmed.matches("^[A-Za-z]:\\\\.*")) {
            return new File(trimmed).toURI().toString();
        }
        return trimmed;
    }

    private boolean isLikelyImage(String value, String mediaType) {
        if (value == null) {
            return "image".equalsIgnoreCase(trimToNull(mediaType));
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.endsWith(".png")
                || normalized.endsWith(".jpg")
                || normalized.endsWith(".jpeg")
                || normalized.endsWith(".gif")
                || normalized.endsWith(".webp")
                || "image".equalsIgnoreCase(trimToNull(mediaType));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private final class AnnouncementCell extends ListCell<Announcement> {
        @Override
        protected void updateItem(Announcement item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            VBox card = new VBox(12);
            card.getStyleClass().add("pm-post-card");
            if (selectedAnnouncement != null && selectedAnnouncement.getId() == item.getId()) {
                card.getStyleClass().add("pm-post-card-selected");
            }
            HBox top = new HBox(10);
            top.setAlignment(Pos.CENTER_LEFT);
            Label badge = new Label(item.getTag() == null || item.getTag().isBlank() ? "INFO" : item.getTag().toUpperCase(Locale.ROOT));
            badge.getStyleClass().addAll("pm-card-badge", "pm-badge-media");
            Label title = new Label(item.getDisplayTitle());
            title.getStyleClass().add("pm-card-title");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label date = new Label(formatDate(item));
            date.getStyleClass().add("pm-card-meta");
            top.getChildren().addAll(badge, title, spacer, date);
            Label snippet = new Label(item.getContent() == null || item.getContent().isBlank() ? "Annonce sans texte principal" : item.getContent());
            snippet.setWrapText(true);
            snippet.getStyleClass().add("pm-card-snippet");
            card.getChildren().addAll(top, snippet);
            VBox.setMargin(card, new Insets(0, 0, 2, 0));
            setGraphic(card);
        }
    }
}
