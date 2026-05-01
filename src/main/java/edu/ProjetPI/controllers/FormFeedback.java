package edu.ProjetPI.controllers;

import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;

public final class FormFeedback {

    private static final String MESSAGE_BOX = "message-box";
    private static final String MESSAGE_ERROR = "message-error";
    private static final String MESSAGE_SUCCESS = "message-success";
    private static final String FIELD_INVALID = "field-invalid";

    private FormFeedback() {
    }

    public static void clearMessage(Label messageLabel) {
        if (messageLabel == null) {
            return;
        }
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll(MESSAGE_BOX, MESSAGE_ERROR, MESSAGE_SUCCESS);
    }

    public static void showError(Label messageLabel, String message) {
        if (messageLabel == null) {
            return;
        }
        messageLabel.setText(message);
        messageLabel.getStyleClass().add(MESSAGE_BOX);
        messageLabel.getStyleClass().remove(MESSAGE_SUCCESS);
        if (!messageLabel.getStyleClass().contains(MESSAGE_ERROR)) {
            messageLabel.getStyleClass().add(MESSAGE_ERROR);
        }
    }

    public static void showSuccess(Label messageLabel, String message) {
        if (messageLabel == null) {
            return;
        }
        messageLabel.setText(message);
        messageLabel.getStyleClass().add(MESSAGE_BOX);
        messageLabel.getStyleClass().remove(MESSAGE_ERROR);
        if (!messageLabel.getStyleClass().contains(MESSAGE_SUCCESS)) {
            messageLabel.getStyleClass().add(MESSAGE_SUCCESS);
        }
    }

    public static void clearInvalid(Node node) {
        Node target = resolveHighlightTarget(node);
        if (target != null) {
            target.getStyleClass().remove(FIELD_INVALID);
        }
    }

    public static void markInvalid(Node node) {
        Node target = resolveHighlightTarget(node);
        if (target == null) {
            return;
        }
        if (!target.getStyleClass().contains(FIELD_INVALID)) {
            target.getStyleClass().add(FIELD_INVALID);
        }
    }

    public static void installResetOnInput(Label messageLabel, TextInputControl... fields) {
        for (TextInputControl field : fields) {
            field.textProperty().addListener((obs, oldValue, newValue) -> {
                clearInvalid(field);
                clearMessage(messageLabel);
            });
        }
    }

    public static void installResetOnSelection(Label messageLabel, ComboBoxBase<?> comboBox) {
        comboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            clearInvalid(comboBox);
            clearMessage(messageLabel);
        });
    }

    private static Node resolveHighlightTarget(Node node) {
        if (node == null) {
            return null;
        }
        Node parent = node.getParent();
        if (parent != null && parent.getStyleClass().contains("input-shell")) {
            return parent;
        }
        return node;
    }
}
