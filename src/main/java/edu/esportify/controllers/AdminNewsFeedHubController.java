package edu.esportify.controllers;

import edu.esportify.navigation.AppNavigator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class AdminNewsFeedHubController implements AdminContentController {
    private enum FeedView {
        POSTS,
        ANNOUNCEMENTS,
        COMMENTS,
        MODERATION,
        AI
    }

    private AdminLayoutController parentController;
    private FeedView activeView = FeedView.POSTS;

    @FXML private StackPane feedContentContainer;
    @FXML private Label statusLabel;
    @FXML private Button postsButton;
    @FXML private Button announcementsButton;
    @FXML private Button commentsButton;
    @FXML private Button moderationButton;
    @FXML private Button aiButton;

    @FXML
    private void initialize() {
        showPosts();
    }

    @Override
    public void init(AdminLayoutController parentController) {
        this.parentController = parentController;
        reloadActiveView();
    }

    @FXML
    private void showPosts() {
        loadFeedView("/PostManagementView.fxml", FeedView.POSTS, "Publications connectees au dashboard admin");
    }

    @FXML
    private void showAnnouncements() {
        loadFeedView("/AnnouncementManagementView.fxml", FeedView.ANNOUNCEMENTS, "Annonces connectees au dashboard admin");
    }

    @FXML
    private void showComments() {
        loadFeedView("/CommentManagementView.fxml", FeedView.COMMENTS, "Commentaires connectes au dashboard admin");
    }

    @FXML
    private void showModeration() {
        loadFeedView("/BackofficeDashboard.fxml", FeedView.MODERATION, "Moderation du fil dans le dashboard admin");
    }

    @FXML
    private void showAi() {
        loadFeedView("/views/admin-feed-view.fxml", FeedView.AI, "Centre IA connecte au dashboard admin");
    }

    @FXML
    private void refreshActiveView() {
        reloadActiveView();
    }

    private void reloadActiveView() {
        switch (activeView) {
            case POSTS -> showPosts();
            case ANNOUNCEMENTS -> showAnnouncements();
            case COMMENTS -> showComments();
            case MODERATION -> showModeration();
            case AI -> showAi();
        }
    }

    private void loadFeedView(String resourcePath, FeedView view, String status) {
        activeView = view;
        try {
            FXMLLoader loader = AppNavigator.createLoader(resourcePath);
            Node node = loader.load();
            Object controller = loader.getController();
            if (controller instanceof AdminContentController adminContentController) {
                adminContentController.init(parentController);
            }
            feedContentContainer.getChildren().setAll(node);
            statusLabel.setText(status);
            updateActiveButton(view);
        } catch (Exception e) {
            Label title = new Label("Impossible de charger la section");
            title.getStyleClass().add("pm-selected-title");
            Label details = new Label(resourcePath + " : " + (e.getMessage() == null ? "erreur inconnue" : e.getMessage()));
            details.getStyleClass().add("pm-selected-content");
            details.setWrapText(true);
            feedContentContainer.getChildren().setAll(new javafx.scene.layout.VBox(12, title, details));
            statusLabel.setText("Erreur de chargement");
            updateActiveButton(view);
        }
    }

    private void updateActiveButton(FeedView view) {
        clearActive(postsButton);
        clearActive(announcementsButton);
        clearActive(commentsButton);
        clearActive(moderationButton);
        clearActive(aiButton);

        Button activeButton = switch (view) {
            case POSTS -> postsButton;
            case ANNOUNCEMENTS -> announcementsButton;
            case COMMENTS -> commentsButton;
            case MODERATION -> moderationButton;
            case AI -> aiButton;
        };
        if (activeButton != null && !activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    private void clearActive(Button button) {
        if (button != null) {
            button.getStyleClass().remove("active");
        }
    }
}
