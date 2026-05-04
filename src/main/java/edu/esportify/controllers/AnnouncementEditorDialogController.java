package edu.esportify.controllers;

import edu.esportify.entities.Announcement;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

public class AnnouncementEditorDialogController {

    private static final String PUBLIC_MEDIA_ROOT = "C:/Users/bouzi/Desktop/PI_DEV_ESPORTIFY/public";
    private static final String UPLOADS_DIRECTORY = PUBLIC_MEDIA_ROOT + "/uploads";

    @FXML private Label dialogTitleLabel;
    @FXML private Label modeBadgeLabel;
    @FXML private Label statusLabel;
    @FXML private TextField titleField;
    @FXML private TextField tagField;
    @FXML private TextField linkField;
    @FXML private TextField mediaTypeField;
    @FXML private TextField mediaFilenameField;
    @FXML private ImageView mediaPreview;
    @FXML private Label mediaPreviewHintLabel;
    @FXML private TextArea contentArea;

    private Stage stage;
    private Announcement editedAnnouncement;
    private boolean saved;

    public void setStage(Stage stage) {
        this.stage = stage;
        bindPreview();
    }

    public void setAnnouncement(Announcement announcement) {
        this.editedAnnouncement = announcement;
        boolean editing = announcement != null;
        dialogTitleLabel.setText(editing ? "Modifier l'annonce" : "Nouvelle annonce");
        modeBadgeLabel.setText(editing ? "EDIT" : "CREATE");
        if (!editing) {
            return;
        }
        titleField.setText(defaultText(announcement.getTitle()));
        tagField.setText(defaultText(announcement.getTag()));
        linkField.setText(defaultText(announcement.getLink()));
        mediaTypeField.setText(defaultText(announcement.getMediaType()));
        mediaFilenameField.setText(defaultText(announcement.getMediaFilename()));
        contentArea.setText(defaultText(announcement.getContent()));
        updateMediaPreview();
    }

    public boolean isSaved() {
        return saved;
    }

    public Announcement buildAnnouncement() {
        Announcement announcement = new Announcement();
        announcement.setTitle(trimToNull(titleField.getText()));
        announcement.setTag(trimToNull(tagField.getText()));
        announcement.setLink(trimToNull(linkField.getText()));
        announcement.setMediaType(trimToNull(mediaTypeField.getText()));
        announcement.setMediaFilename(trimToNull(mediaFilenameField.getText()));
        announcement.setContent(trimToNull(contentArea.getText()));
        announcement.setCreatedAt(editedAnnouncement == null || editedAnnouncement.getCreatedAt() == null ? LocalDateTime.now() : editedAnnouncement.getCreatedAt());
        return announcement;
    }

    @FXML
    private void handleSave() {
        try {
            buildAnnouncement();
            saved = true;
            close();
        } catch (RuntimeException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        saved = false;
        close();
    }

    @FXML
    private void handleFocusLink() {
        File file = chooseMediaFile("Choisir un media", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp", "*.mp4", "*.mov", "*.m4v", "*.avi", "*.mkv", "*.webm");
        if (file != null && linkField != null) {
            String uploadedPath = copyMediaToUploads(file);
            linkField.setText(uploadedPath);
            if (mediaFilenameField != null) {
                mediaFilenameField.setText(uploadedPath);
            }
            if (mediaTypeField != null) {
                mediaTypeField.setText(isImageExtension(file.getName()) ? "image" : "video");
            }
            updateMediaPreview();
        }
    }

    @FXML
    private void handleFocusMediaFilename() {
        File file = chooseMediaFile("Choisir un media", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp", "*.mp4", "*.mov", "*.m4v", "*.avi", "*.mkv", "*.webm");
        if (file != null && mediaFilenameField != null) {
            String uploadedPath = copyMediaToUploads(file);
            mediaFilenameField.setText(uploadedPath);
            if (linkField != null && (linkField.getText() == null || linkField.getText().isBlank())) {
                linkField.setText(uploadedPath);
            }
            if (mediaTypeField != null) {
                mediaTypeField.setText(isImageExtension(file.getName()) ? "image" : "video");
            }
            updateMediaPreview();
        }
    }

    @FXML
    private void handleClearLink() {
        if (linkField != null) {
            linkField.clear();
        }
        updateMediaPreview();
    }

    @FXML
    private void handleClearMediaFilename() {
        if (mediaFilenameField != null) {
            mediaFilenameField.clear();
        }
        updateMediaPreview();
    }

    private void close() {
        if (stage != null) {
            stage.close();
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private void bindPreview() {
        if (linkField != null) {
            linkField.textProperty().addListener((obs, oldValue, newValue) -> updateMediaPreview());
        }
        if (mediaFilenameField != null) {
            mediaFilenameField.textProperty().addListener((obs, oldValue, newValue) -> updateMediaPreview());
        }
        if (mediaTypeField != null) {
            mediaTypeField.textProperty().addListener((obs, oldValue, newValue) -> updateMediaPreview());
        }
        updateMediaPreview();
    }

    private void updateMediaPreview() {
        if (mediaPreview == null || mediaPreviewHintLabel == null) {
            return;
        }
        String link = trimToNull(linkField == null ? null : linkField.getText());
        String mediaFilename = trimToNull(mediaFilenameField == null ? null : mediaFilenameField.getText());
        String mediaType = trimToNull(mediaTypeField == null ? null : mediaTypeField.getText());
        String candidate = pickMediaCandidate(link, mediaFilename, mediaType);
        if (candidate != null && isLikelyImage(candidate, mediaType)) {
            try {
                mediaPreview.setImage(new Image(candidate, true));
                mediaPreview.setVisible(true);
                mediaPreview.setManaged(true);
                mediaPreviewHintLabel.setText("Media image detecte");
                return;
            } catch (IllegalArgumentException ignored) {
                mediaPreviewHintLabel.setText(candidate);
            }
        }
        mediaPreview.setImage(null);
        mediaPreview.setVisible(false);
        mediaPreview.setManaged(false);
        mediaPreviewHintLabel.setText(candidate != null ? candidate : "Aucun media a afficher");
    }

    private String pickMediaCandidate(String link, String mediaFilename, String mediaType) {
        link = normalizeMediaPath(link);
        mediaFilename = normalizeMediaPath(mediaFilename);
        if (isLikelyImage(link, mediaType)) {
            return link;
        }
        if (isLikelyImage(mediaFilename, mediaType)) {
            return mediaFilename;
        }
        return link != null ? link : mediaFilename;
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
            return "image".equalsIgnoreCase(mediaType);
        }
        String normalized = value.toLowerCase();
        return normalized.endsWith(".png")
                || normalized.endsWith(".jpg")
                || normalized.endsWith(".jpeg")
                || normalized.endsWith(".gif")
                || normalized.endsWith(".webp")
                || "image".equalsIgnoreCase(mediaType);
    }

    private File chooseMediaFile(String title, String... extensions) {
        if (stage == null) {
            return null;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Media", extensions));
        return chooser.showOpenDialog(stage);
    }

    private String copyMediaToUploads(File sourceFile) {
        try {
            Files.createDirectories(Path.of(UPLOADS_DIRECTORY));
            String targetName = System.currentTimeMillis() + "-" + sourceFile.getName();
            Path target = Path.of(UPLOADS_DIRECTORY, targetName);
            Files.copy(sourceFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + targetName;
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'importer le media: " + ex.getMessage(), ex);
        }
    }

    private boolean isImageExtension(String fileName) {
        String normalized = fileName.toLowerCase();
        return normalized.endsWith(".png")
                || normalized.endsWith(".jpg")
                || normalized.endsWith(".jpeg")
                || normalized.endsWith(".gif")
                || normalized.endsWith(".webp");
    }
}
