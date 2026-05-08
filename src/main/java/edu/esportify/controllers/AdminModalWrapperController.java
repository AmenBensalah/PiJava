package edu.esportify.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class AdminModalWrapperController {
    
    @FXML private Label modalTitleLabel;
    @FXML private Label modalSubtitleLabel;
    @FXML private Button refreshButton;
    @FXML private Button createButton;
    @FXML private Button deleteButton;
    @FXML private Button closeButton;
    @FXML private StackPane modalContentContainer;
    
    private AdminLayoutController parentController;
    private Runnable onCloseCallback;
    
    public void init(AdminLayoutController parentController) {
        this.parentController = parentController;
    }
    
    public void setTitle(String title, String subtitle) {
        modalTitleLabel.setText(title);
        modalSubtitleLabel.setText(subtitle);
    }
    
    public void setRefreshAction(Runnable action) {
        refreshButton.setOnAction(e -> action.run());
    }
    
    public void setCreateAction(Runnable action) {
        createButton.setVisible(true);
        createButton.setOnAction(e -> action.run());
    }
    
    public void setDeleteAction(Runnable action) {
        deleteButton.setVisible(true);
        deleteButton.setOnAction(e -> action.run());
    }
    
    public void hideCreateButton() {
        createButton.setVisible(false);
    }
    
    public void hideDeleteButton() {
        deleteButton.setVisible(false);
    }
    
    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }
    
    @FXML
    private void onCloseModal() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
        if (parentController != null) {
            parentController.showGridDashboard();
        }
    }
    
    public StackPane getContentContainer() {
        return modalContentContainer;
    }
}
