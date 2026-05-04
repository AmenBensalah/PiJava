package edu.esportify.controllers;

import edu.esportify.entities.FilActualite;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PostEditorDialogController {

    private static final String PUBLIC_MEDIA_ROOT = "C:/Users/bouzi/Desktop/PI_DEV_ESPORTIFY/public";
    private static final String UPLOADS_DIRECTORY = PUBLIC_MEDIA_ROOT + "/uploads";

    @FXML private Label dialogTitleLabel;
    @FXML private Label modeBadgeLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea contentArea;
    @FXML private TextField imageField;
    @FXML private TextField videoField;
    @FXML private ImageView mediaPreview;
    @FXML private Label mediaPreviewHintLabel;
    @FXML private CheckBox eventCheckBox;
    @FXML private TextField eventTitleField;
    @FXML private DatePicker eventDateField;
    @FXML private TextField eventLocationField;
    @FXML private TextField maxParticipantsField;
    @FXML private TextField authorIdField;

    private Stage stage;
    private FilActualite editedPost;
    private boolean saved;

    public void setStage(Stage stage) {
        this.stage = stage;
        bindPreview();
    }

    public void setPost(FilActualite post) {
        this.editedPost = post;
        boolean editing = post != null;
        dialogTitleLabel.setText(editing ? "Modifier le post" : "Nouveau post");
        modeBadgeLabel.setText(editing ? "EDIT" : "CREATE");
        if (!editing) {
            return;
        }
        contentArea.setText(defaultText(post.getContent()));
        imageField.setText(defaultText(post.getImagePath()));
        videoField.setText(defaultText(post.getVideoUrl()));
        eventCheckBox.setSelected(post.isEvent());
        eventTitleField.setText(defaultText(post.getEventTitle()));
        eventDateField.setValue(post.getEventDate() == null ? null : post.getEventDate().toLocalDate());
        eventLocationField.setText(defaultText(post.getEventLocation()));
        maxParticipantsField.setText(post.getMaxParticipants() == null ? "" : String.valueOf(post.getMaxParticipants()));
        authorIdField.setText(post.getAuthorId() == null ? "" : String.valueOf(post.getAuthorId()));
        updateMediaPreview();
    }

    public boolean isSaved() {
        return saved;
    }

    public FilActualite buildPost() {
        FilActualite post = new FilActualite();
        post.setContent(trimToNull(contentArea.getText()));
        post.setImagePath(trimToNull(imageField.getText()));
        post.setVideoUrl(trimToNull(videoField.getText()));
        post.setEvent(eventCheckBox.isSelected());
        post.setEventTitle(trimToNull(eventTitleField.getText()));
        post.setEventDate(toDateTime(eventDateField == null ? null : eventDateField.getValue()));
        post.setEventLocation(trimToNull(eventLocationField.getText()));
        post.setMaxParticipants(parseInteger(maxParticipantsField.getText()));
        post.setAuthorId(parseInteger(authorIdField.getText()));
        post.setCreatedAt(editedPost == null || editedPost.getCreatedAt() == null ? LocalDateTime.now() : editedPost.getCreatedAt());
        post.setMediaFilename(extractMediaFilename(post));
        return post;
    }

    @FXML
    private void handleSave() {
        try {
            buildPost();
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
    private void handleFocusImage() {
        File file = chooseMediaFile("Choisir une image", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp");
        if (file != null && imageField != null) {
            imageField.setText(copyMediaToUploads(file));
            if (videoField != null) {
                videoField.clear();
            }
            updateMediaPreview();
        }
    }

    @FXML
    private void handleFocusVideo() {
        File file = chooseMediaFile("Choisir une video", "*.mp4", "*.mov", "*.m4v", "*.avi", "*.mkv", "*.webm");
        if (file != null && videoField != null) {
            videoField.setText(copyMediaToUploads(file));
            if (imageField != null) {
                imageField.clear();
            }
            updateMediaPreview();
        }
    }

    @FXML
    private void handleClearImage() {
        if (imageField != null) {
            imageField.clear();
        }
        updateMediaPreview();
    }

    @FXML
    private void handleClearVideo() {
        if (videoField != null) {
            videoField.clear();
        }
        updateMediaPreview();
    }

    private void close() {
        if (stage != null) {
            stage.close();
        }
    }

    private LocalDateTime toDateTime(LocalDate value) {
        return value == null ? null : value.atStartOfDay();
    }

    private Integer parseInteger(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : Integer.parseInt(trimmed);
    }

    private String extractMediaFilename(FilActualite post) {
        String media = post.getImagePath() != null ? post.getImagePath() : post.getVideoUrl();
        if (media == null || media.isBlank()) {
            return null;
        }
        int slash = media.lastIndexOf('/');
        return slash >= 0 && slash < media.length() - 1 ? media.substring(slash + 1) : media;
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
        if (imageField != null) {
            imageField.textProperty().addListener((obs, oldValue, newValue) -> updateMediaPreview());
        }
        if (videoField != null) {
            videoField.textProperty().addListener((obs, oldValue, newValue) -> updateMediaPreview());
        }
        updateMediaPreview();
    }

    private void updateMediaPreview() {
        if (mediaPreview == null || mediaPreviewHintLabel == null) {
            return;
        }
        String imageUrl = normalizeMediaPath(imageField == null ? null : imageField.getText());
        if (imageUrl != null) {
            try {
                mediaPreview.setImage(new Image(imageUrl, true));
                mediaPreview.setVisible(true);
                mediaPreview.setManaged(true);
                mediaPreviewHintLabel.setText("Image detectee");
                return;
            } catch (IllegalArgumentException ignored) {
                mediaPreviewHintLabel.setText(imageUrl);
            }
        }
        mediaPreview.setImage(null);
        mediaPreview.setVisible(false);
        mediaPreview.setManaged(false);
        String videoUrl = trimToNull(videoField == null ? null : videoField.getText());
        mediaPreviewHintLabel.setText(videoUrl != null ? videoUrl : "Aucun media a afficher");
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
}
