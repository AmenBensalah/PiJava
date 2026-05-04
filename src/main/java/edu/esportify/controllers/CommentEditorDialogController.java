package edu.esportify.controllers;

import edu.esportify.entities.Commentaire;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDateTime;

public class CommentEditorDialogController {

    @FXML private Label dialogTitleLabel;
    @FXML private Label modeBadgeLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea contentArea;
    @FXML private TextField authorIdField;
    @FXML private TextField postIdField;

    private Stage stage;
    private Commentaire editedComment;
    private boolean saved;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setComment(Commentaire comment) {
        this.editedComment = comment;
        boolean editing = comment != null;
        dialogTitleLabel.setText(editing ? "Modifier le commentaire" : "Nouveau commentaire");
        modeBadgeLabel.setText(editing ? "EDIT" : "CREATE");
        if (!editing) return;
        contentArea.setText(defaultText(comment.getContent()));
        authorIdField.setText(String.valueOf(comment.getAuthorId()));
        postIdField.setText(String.valueOf(comment.getPostId()));
    }

    public boolean isSaved() {
        return saved;
    }

    public Commentaire buildComment() {
        Commentaire comment = new Commentaire();
        comment.setContent(trimToNull(contentArea.getText()));
        comment.setAuthorId(parseRequired(authorIdField.getText()));
        comment.setPostId(parseRequired(postIdField.getText()));
        comment.setCreatedAt(editedComment == null || editedComment.getCreatedAt() == null ? LocalDateTime.now() : editedComment.getCreatedAt());
        return comment;
    }

    @FXML private void handleSave() {
        try {
            buildComment();
            saved = true;
            close();
        } catch (RuntimeException ex) {
            statusLabel.setText(ex.getMessage());
        }
    }
    @FXML private void handleCancel() { saved = false; close(); }

    private void close() {
        if (stage != null) stage.close();
    }

    private int parseRequired(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) throw new IllegalArgumentException("Champ numerique obligatoire.");
        return Integer.parseInt(trimmed);
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }
}
