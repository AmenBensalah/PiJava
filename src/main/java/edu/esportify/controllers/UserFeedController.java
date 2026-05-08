package edu.esportify.controllers;

import edu.esportify.entities.AiRoleInsight;
import edu.esportify.entities.Announcement;
import edu.esportify.entities.Candidature;
import edu.esportify.entities.Commentaire;
import edu.esportify.entities.ConversationPreview;
import edu.esportify.entities.Equipe;
import edu.esportify.entities.FilActualite;
import edu.esportify.entities.MessengerMessage;
import edu.esportify.entities.Task;
import edu.esportify.entities.TaskStatus;
import edu.esportify.entities.UserProfile;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.AnnouncementService;
import edu.esportify.services.CandidatureService;
import edu.esportify.services.CommentaireService;
import edu.esportify.services.EquipeService;
import edu.esportify.services.FilActualiteService;
import edu.esportify.services.MessengerService;
import edu.esportify.services.SocialInteractionService;
import edu.esportify.services.StreamingIntegrationService;
import edu.esportify.services.TaskService;
import edu.esportify.services.TeamAiAdvisorService;
import edu.esportify.services.UserDirectoryService;
import java.awt.Desktop;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.StringConverter;

public class UserFeedController implements UserContentController {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy - HH:mm", Locale.FRENCH);
    private static final DateTimeFormatter DAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM", Locale.FRENCH);
    private static final int MESSENGER_PAGE_SIZE = 18;

    private enum Filter {
        ALL,
        MEDIA,
        EVENTS,
        RECOMMENDED
    }

    private final FilActualiteService postService = new FilActualiteService();
    private final AnnouncementService announcementService = new AnnouncementService();
    private final UserDirectoryService userDirectoryService = new UserDirectoryService();
    private final MessengerService messengerService = new MessengerService();
    private final CommentaireService commentaireService = new CommentaireService();
    private final SocialInteractionService socialInteractionService = new SocialInteractionService();
    private final StreamingIntegrationService streamingIntegrationService = new StreamingIntegrationService();
    private final TeamAiAdvisorService teamAiAdvisorService = new TeamAiAdvisorService();
    private final TaskService taskService = new TaskService();
    private final EquipeService equipeService = new EquipeService();
    private final CandidatureService candidatureService = new CandidatureService();

    private Filter activeFilter = Filter.ALL;
    private UserProfile currentUser;
    private Equipe contextTeam;
    private Map<Integer, UserProfile> usersById = new LinkedHashMap<>();
    private ConversationPreview activeConversation;
    private final Map<Integer, List<Commentaire>> commentsByPost = new HashMap<>();
    private final Map<Integer, Integer> likeCountsByPost = new HashMap<>();
    private final Map<Integer, Integer> shareCountsByPost = new HashMap<>();
    private final Map<Integer, VBox> postCardsById = new HashMap<>();
    private final Set<Integer> likedPostIds = new HashSet<>();
    private final Set<Integer> savedPostIds = new HashSet<>();
    private final Set<Integer> expandedCommentPostIds = new HashSet<>();
    private final Set<String> readNotificationKeys = new HashSet<>();
    private Stage messengerPopupStage;
    private Stage notificationsPopupStage;

    private enum NotificationTarget {
        POST,
        COMMENT,
        MESSENGER,
        ANNOUNCEMENT,
        INFO
    }

    @FXML private ScrollPane feedScrollPane;
    @FXML private Label postsCountLabel;
    @FXML private Label eventsCountLabel;
    @FXML private Label mediaCountLabel;
    @FXML private Label resultCountLabel;
    @FXML private Label statusLabel;
    @FXML private TextArea contentArea;
    @FXML private TextField imageField;
    @FXML private TextField videoField;
    @FXML private CheckBox eventCheckBox;
    @FXML private HBox eventFieldsRow;
    @FXML private TextField eventTitleField;
    @FXML private TextField eventLocationField;
    @FXML private TextField maxParticipantsField;
    @FXML private TextField searchField;
    @FXML private Button allFilterButton;
    @FXML private Button mediaFilterButton;
    @FXML private Button eventFilterButton;
    @FXML private Button aiFilterButton;
    @FXML private Button topMessengerButton;
    @FXML private Button topNotificationsButton;
    @FXML private VBox postsContainer;
    @FXML private VBox announcementsContainer;
    @FXML private VBox trendingContainer;
    @FXML private VBox notificationsSectionCard;
    @FXML private VBox notificationsContainer;
    @FXML private Label notificationsCountLabel;
    @FXML private VBox streamingContainer;
    @FXML private Label streamingStatusLabel;
    @FXML private VBox aiSectionCard;
    @FXML private VBox aiContainer;
    @FXML private Label aiContextLabel;
    @FXML private FlowPane messengerQuickButtonsContainer;
    @FXML private VBox messengerSectionCard;
    @FXML private VBox messengerConversationsContainer;
    @FXML private VBox messengerMessagesContainer;
    @FXML private ComboBox<UserProfile> messengerContactPicker;
    @FXML private TextField messengerInputField;
    @FXML private Label messengerStatusLabel;
    @FXML private Label messengerActiveConversationLabel;
    @FXML private Label messengerPresenceLabel;
    @FXML private ScrollPane messengerMessagesScroll;

    @Override
    public void init(UserLayoutController parentController) {
        usersById = userDirectoryService.getUsersById();
        currentUser = userDirectoryService.resolveCurrentUser();
        contextTeam = resolveContextTeam();
        configureSearch();
        configureMessengerPicker();
        onToggleEvent();
        refreshAll();
    }

    @FXML
    private void onRefresh() {
        refreshAll();
        setStatus("Fil actualise");
    }

    @FXML
    private void onPublish() {
        try {
            FilActualite post = buildPost();
            postService.addEntity(post);
            clearComposer();
            setStatus("Publication ajoutee");
            setFilter(Filter.ALL, allFilterButton);
        } catch (RuntimeException e) {
            setStatus(e.getMessage());
        }
    }

    @FXML private void onFilterAll() { setFilter(Filter.ALL, allFilterButton); }
    @FXML private void onFilterMedia() { setFilter(Filter.MEDIA, mediaFilterButton); }
    @FXML private void onFilterEvents() { setFilter(Filter.EVENTS, eventFilterButton); }
    @FXML
    private void onFilterRecommended() {
        renderAiAndTasks();
        setFilter(Filter.RECOMMENDED, aiFilterButton);
        if (aiSectionCard != null) {
            scrollToSection(aiSectionCard);
        }
        if (contextTeam == null) {
            setStatus("Mode IA actif. Recommandations generales affichees, meme sans equipe.");
        } else {
            setStatus("Mode IA actif. Publications recommandees et taches equipe actualisees.");
        }
    }

    @FXML
    private void onToggleEvent() {
        boolean eventMode = eventCheckBox != null && eventCheckBox.isSelected();
        if (eventFieldsRow != null) {
            eventFieldsRow.setVisible(eventMode);
            eventFieldsRow.setManaged(eventMode);
        }
    }

    @FXML
    private void onRefreshStreaming() {
        renderStreaming();
    }

    @FXML
    private void onRefreshMessenger() {
        refreshMessenger();
    }

    @FXML
    private void onOpenMessengerHub() {
        toggleMessengerPopup();
    }

    @FXML
    private void onOpenNotificationsHub() {
        toggleNotificationsPopup();
    }

    @FXML
    private void onStartVoiceCall() {
        startCallInvite("voice", true);
    }

    @FXML
    private void onStartVideoCall() {
        startCallInvite("video", false);
    }

    @FXML
    private void onStartConversation() {
        if (messengerContactPicker == null || messengerContactPicker.getValue() == null) {
            setMessengerStatus("Choisissez d'abord un contact.");
            return;
        }
        openConversationWithPeer(messengerContactPicker.getValue(), true);
    }

    @FXML
    private void onSendMessage() {
        sendMessengerMessage(messengerInputField == null ? null : messengerInputField.getText(), () -> {
            if (messengerInputField != null) {
                messengerInputField.clear();
            }
        });
    }

    private void configureSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldValue, newValue) -> refreshFeed());
        }
    }

    private void configureMessengerPicker() {
        configureMessengerPicker(messengerContactPicker);
    }

    private void configureMessengerPicker(ComboBox<UserProfile> picker) {
        if (picker == null) {
            return;
        }
        picker.setConverter(new StringConverter<>() {
            @Override
            public String toString(UserProfile object) {
                return object == null ? "" : object.getDisplayName() + " - " + object.getRole();
            }

            @Override
            public UserProfile fromString(String string) {
                return null;
            }
        });
        picker.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(UserProfile item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getDisplayName() + " - " + item.getRole());
            }
        });
        picker.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(UserProfile item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Choisir un contact..." : item.getDisplayName());
            }
        });
    }

    private void setFilter(Filter filter, Button activeButton) {
        activeFilter = filter;
        for (Button button : new Button[]{allFilterButton, mediaFilterButton, eventFilterButton, aiFilterButton}) {
            if (button != null) {
                button.getStyleClass().remove("cat-pill-active");
            }
        }
        if (activeButton != null) {
            activeButton.getStyleClass().add("cat-pill-active");
        }
        refreshFeed();
    }

    private void refreshAll() {
        refreshInteractionState();
        refreshFeed();
        renderAnnouncements();
        renderStreaming();
        renderAiAndTasks();
        refreshMessenger();
        renderNotifications();
        refreshFloatingPanels();
    }

    private void refreshFeed() {
        List<FilActualite> allPosts = loadPosts();
        List<FilActualite> visiblePosts = filterPosts(allPosts);
        postsCountLabel.setText(allPosts.size() + " posts");
        eventsCountLabel.setText(allPosts.stream().filter(FilActualite::isEvent).count() + " events");
        mediaCountLabel.setText(allPosts.stream().filter(this::hasMedia).count() + " media");
        resultCountLabel.setText(visiblePosts.size() + " resultats");

        postsContainer.getChildren().clear();
        postCardsById.clear();
        if (visiblePosts.isEmpty()) {
            postsContainer.getChildren().add(emptyCard("Aucune publication", "Le fil attend son premier contenu pour cette selection."));
        } else {
            visiblePosts.forEach(post -> {
                VBox card = createPostCard(post);
                postCardsById.put(post.getId(), card);
                postsContainer.getChildren().add(card);
            });
        }
        renderTrending(allPosts);
    }

    private void renderNotifications() {
        notificationsContainer.getChildren().clear();
        List<NotificationItem> items = buildNotificationItems();
        int unread = countUnreadNotifications(items);
        notificationsCountLabel.setText(unread + " alerte(s)");
        if (topNotificationsButton != null) {
            topNotificationsButton.setText(unread <= 0 ? "Notifications" : "Notifications " + unread);
        }
        if (items.isEmpty()) {
            notificationsContainer.getChildren().add(emptySideCard("Rien a signaler", "Le centre de notifications est synchronise."));
            return;
        }
        for (NotificationItem item : items.stream().limit(6).toList()) {
            VBox card = new VBox(6);
            card.getStyleClass().add("notification-card-mini");
            if (!readNotificationKeys.contains(item.key())) {
                card.getStyleClass().add("notification-card-mini-unread");
            }
            Label title = new Label(item.title());
            title.getStyleClass().add("notification-title-mini");
            title.setWrapText(true);
            Label body = new Label(item.body());
            body.getStyleClass().add("notification-body-mini");
            body.setWrapText(true);
            Label meta = new Label(item.createdAt() == null ? "Maintenant" : DATE_FORMAT.format(item.createdAt()));
            meta.getStyleClass().add("message-meta-mini");
            card.getChildren().addAll(title, body, meta);
            card.setOnMouseClicked(event -> openNotification(item));
            notificationsContainer.getChildren().add(card);
        }
        refreshNotificationsPopup();
    }

    private void renderStreaming() {
        streamingContainer.getChildren().clear();
        try {
            StreamingIntegrationService.StreamingSnapshot snapshot = streamingIntegrationService.loadSnapshot();
            String status = defaultText(snapshot.status(), "Streaming synchronise.");
            streamingStatusLabel.setText(status);

            for (StreamingIntegrationService.LiveStreamCard stream : snapshot.liveStreams().stream().limit(2).toList()) {
                streamingContainer.getChildren().add(buildStreamingCard(
                        stream.platform(),
                        stream.title(),
                        stream.channelName() + " - " + stream.gameName(),
                        stream.link()
                ));
            }
            for (StreamingIntegrationService.HighlightCard highlight : snapshot.highlights().stream().limit(2).toList()) {
                streamingContainer.getChildren().add(buildStreamingCard(
                        highlight.platform(),
                        highlight.title(),
                        highlight.channelName() + " - " + highlight.publishedAt(),
                        highlight.link()
                ));
            }
            if (streamingContainer.getChildren().isEmpty()) {
                streamingContainer.getChildren().add(emptySideCard("Aucun flux", "Ajoutez votre cle YouTube pour charger les lives et highlights."));
            }
        } catch (RuntimeException e) {
            streamingStatusLabel.setText("Streaming indisponible.");
            streamingContainer.getChildren().add(emptySideCard("Erreur streaming", e.getMessage()));
        }
    }

    private void renderAiAndTasks() {
        aiContainer.getChildren().clear();
        if (contextTeam == null) {
            aiContextLabel.setText("Mode IA general");
            aiContainer.getChildren().add(buildGenericAiOverviewCard());
            aiContainer.getChildren().add(buildGenericAiTaskCard());
            aiContainer.getChildren().add(buildGenericAiRecommendationsCard());
            return;
        }

        try {
            aiContextLabel.setText(contextTeam.getNomEquipe() + " - " + defaultText(contextTeam.getTag(), "TEAM"));

            List<Task> tasks = taskService.getByTeam(contextTeam.getId());
            List<Task> delayedTasks = taskService.getDelayedTasks(contextTeam.getId());
            List<AiRoleInsight> insights = teamAiAdvisorService.buildInsights(contextTeam, AppSession.getInstance().getUsername());

            aiContainer.getChildren().add(buildTaskSummaryCard(tasks, delayedTasks));
            if (tasks.isEmpty() && insights.isEmpty()) {
                aiContainer.getChildren().add(emptySideCard(
                        "Aucune tache IA",
                        "L'equipe est connectee, mais aucune tache ni recommandation n'est encore disponible."
                ));
                return;
            }
            for (Task task : tasks.stream().limit(3).toList()) {
                aiContainer.getChildren().add(buildTaskCard(task));
            }
            for (AiRoleInsight insight : insights.stream().limit(3).toList()) {
                aiContainer.getChildren().add(buildInsightCard(insight));
            }
        } catch (RuntimeException e) {
            aiContextLabel.setText("IA & taches indisponibles");
            aiContainer.getChildren().add(emptySideCard("Erreur IA", defaultText(e.getMessage(), "Chargement impossible")));
        }
    }

    private void refreshMessenger() {
        refreshMessengerContacts();
        refreshMessengerConversations();
        renderActiveConversationMessages();
        refreshMessengerPopup();
    }

    private void refreshMessengerContacts() {
        if (messengerContactPicker == null) {
            return;
        }
        try {
            messengerContactPicker.getItems().setAll(messengerService.getAvailableContacts(resolveCurrentUserId()));
        } catch (RuntimeException e) {
            setMessengerStatus("Contacts indisponibles: " + e.getMessage());
        }
    }

    private void refreshMessengerConversations() {
        if (messengerConversationsContainer != null) {
            messengerConversationsContainer.getChildren().clear();
        }
        if (messengerQuickButtonsContainer != null) {
            messengerQuickButtonsContainer.getChildren().clear();
        }
        try {
            List<ConversationPreview> conversations = messengerService.getConversationsForUser(resolveCurrentUserId());
            int unread = messengerService.getUnreadCount(resolveCurrentUserId());
            setMessengerStatus(unread > 0 ? unread + " message(s) non lu(s)." : "Messenger synchronise.");
            if (topMessengerButton != null) {
                topMessengerButton.setText(unread > 0 ? "Discussions " + unread : "Discussions");
            }

            if (activeConversation != null) {
                activeConversation = conversations.stream()
                        .filter(item -> item.getConversationId() == activeConversation.getConversationId())
                        .findFirst()
                        .orElse(activeConversation);
            } else if (!conversations.isEmpty()) {
                activeConversation = conversations.get(0);
            }

            if (conversations.isEmpty()) {
                if (topMessengerButton != null) {
                    topMessengerButton.setText("Discussions");
                }
                if (messengerQuickButtonsContainer != null) {
                    Label emptyQuick = new Label("Aucune discussion recente.");
                    emptyQuick.getStyleClass().add("user-feed-muted");
                    messengerQuickButtonsContainer.getChildren().add(emptyQuick);
                }
                if (messengerConversationsContainer != null) {
                    messengerConversationsContainer.getChildren().add(
                            emptySideCard("Aucune conversation", "Demarrez un chat depuis la liste des contacts.")
                    );
                }
                return;
            }

            renderMessengerQuickButtons(conversations);
            if (messengerConversationsContainer != null) {
                for (ConversationPreview preview : conversations) {
                    messengerConversationsContainer.getChildren().add(buildConversationButton(preview));
                }
            }
        } catch (RuntimeException e) {
            setMessengerStatus("Messenger indisponible.");
            if (topMessengerButton != null) {
                topMessengerButton.setText("Discussions");
            }
            if (messengerQuickButtonsContainer != null) {
                Label errorQuick = new Label("Messenger indisponible.");
                errorQuick.getStyleClass().add("user-feed-muted");
                messengerQuickButtonsContainer.getChildren().add(errorQuick);
            }
            if (messengerConversationsContainer != null) {
                messengerConversationsContainer.getChildren().add(emptySideCard("Erreur messenger", e.getMessage()));
            }
        }
    }

    private void renderActiveConversationMessages() {
        if (messengerMessagesContainer == null) {
            return;
        }
        messengerMessagesContainer.getChildren().clear();
        if (activeConversation == null) {
            if (messengerActiveConversationLabel != null) {
                messengerActiveConversationLabel.setText("Selectionnez une conversation");
            }
            if (messengerPresenceLabel != null) {
                messengerPresenceLabel.setText("Pas de canal actif");
            }
            messengerMessagesContainer.getChildren().add(emptySideCard(
                    "Messagerie prete",
                    "Ouvrez une conversation existante ou creez-en une nouvelle."
            ));
            return;
        }

        if (messengerActiveConversationLabel != null) {
            messengerActiveConversationLabel.setText(activeConversation.getPeerDisplayName());
        }
        if (messengerPresenceLabel != null) {
            messengerPresenceLabel.setText(activeConversation.isPeerOnline() ? "En ligne" : "Hors ligne");
        }

        try {
            List<MessengerMessage> messages = messengerService.getMessages(
                    activeConversation.getConversationId(),
                    resolveCurrentUserId(),
                    MESSENGER_PAGE_SIZE,
                    null
            );
            if (messages.isEmpty()) {
                messengerMessagesContainer.getChildren().add(emptySideCard("Conversation vide", "Envoyez le premier message."));
            } else {
                for (MessengerMessage message : messages) {
                    messengerMessagesContainer.getChildren().add(buildMessageBubble(message));
                }
            }
            if (messengerMessagesScroll != null) {
                messengerMessagesScroll.setVvalue(1.0);
            }
        } catch (RuntimeException e) {
            messengerMessagesContainer.getChildren().add(emptySideCard("Chargement impossible", e.getMessage()));
        }
    }

    private List<NotificationItem> buildNotificationItems() {
        List<NotificationItem> items = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        try {
            List<Announcement> announcements = announcementService.getData();
            if (!announcements.isEmpty()) {
                Announcement latest = announcements.get(0);
                items.add(new NotificationItem(
                        "announcement:" + latest.getId(),
                        "Annonce officielle",
                        defaultText(latest.getDisplayTitle(), "Nouvelle annonce Esportify"),
                        latest.getCreatedAt(),
                        NotificationTarget.ANNOUNCEMENT,
                        latest.getId()
                ));
            }
        } catch (RuntimeException ignored) {
        }

        try {
            int unread = messengerService.getUnreadCount(resolveCurrentUserId());
            if (unread > 0) {
                items.add(new NotificationItem(
                        "messenger:" + unread,
                        "Messenger",
                        "Vous avez " + unread + " message(s) non lu(s).",
                        now,
                        NotificationTarget.MESSENGER,
                        0
                ));
            }
        } catch (RuntimeException ignored) {
        }

        List<FilActualite> posts = safePosts();
        if (!posts.isEmpty()) {
            FilActualite latestPost = posts.stream()
                    .max(Comparator.comparing(FilActualite::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                    .orElse(null);
            if (latestPost != null && !isCurrentUsersPost(latestPost)) {
                items.add(new NotificationItem(
                        "post:" + latestPost.getId(),
                        "Activite du feed",
                        defaultText(latestPost.getDisplayTitle(), "Nouvelle publication dans le feed"),
                        latestPost.getCreatedAt(),
                        NotificationTarget.POST,
                        latestPost.getId()
                ));
            }
        }

        for (FilActualite post : posts) {
            if (!isCurrentUsersPost(post)) {
                continue;
            }
            int likeCount = likeCountsByPost.getOrDefault(post.getId(), 0);
            if (likeCount > 0) {
                items.add(new NotificationItem(
                        "like:" + post.getId() + ":" + likeCount,
                        "Nouveaux j'aime",
                        "\"" + defaultText(post.getDisplayTitle(), "Votre publication") + "\" a recu " + likeCount + " j'aime.",
                        post.getCreatedAt() == null ? now : post.getCreatedAt(),
                        NotificationTarget.POST,
                        post.getId()
                ));
            }
            for (Commentaire comment : commentsByPost.getOrDefault(post.getId(), List.of())) {
                if (comment.getAuthorId() == resolveCurrentUserId()) {
                    continue;
                }
                items.add(new NotificationItem(
                        "comment:" + comment.getId(),
                        "Nouveau commentaire",
                        resolveAuthorName(comment.getAuthorId()) + " a reagi a votre publication.",
                        comment.getCreatedAt() == null ? now : comment.getCreatedAt(),
                        NotificationTarget.COMMENT,
                        post.getId()
                ));
            }
        }

        if (contextTeam != null) {
            try {
                List<Task> delayedTasks = taskService.getDelayedTasks(contextTeam.getId());
                if (!delayedTasks.isEmpty()) {
                    items.add(new NotificationItem(
                            "tasks:" + delayedTasks.size(),
                            "Taches en retard",
                            delayedTasks.size() + " action(s) de l'equipe demandent une relance rapide.",
                            now.minusMinutes(5),
                            NotificationTarget.INFO,
                            0
                    ));
                }
            } catch (RuntimeException ignored) {
            }
            try {
                List<AiRoleInsight> insights = teamAiAdvisorService.buildInsights(contextTeam, AppSession.getInstance().getUsername());
                if (!insights.isEmpty()) {
                    items.add(new NotificationItem(
                            "ai:" + insights.get(0).getRoleName(),
                            "IA equipe",
                            insights.get(0).getRoleName() + " : " + insights.get(0).getPriority(),
                            now.minusMinutes(10),
                            NotificationTarget.INFO,
                            0
                    ));
                }
            } catch (RuntimeException ignored) {
            }
        }

        items.sort((left, right) -> {
            LocalDateTime leftDate = left.createdAt() == null ? LocalDateTime.MIN : left.createdAt();
            LocalDateTime rightDate = right.createdAt() == null ? LocalDateTime.MIN : right.createdAt();
            return rightDate.compareTo(leftDate);
        });
        return items.stream().limit(20).toList();
    }

    private List<FilActualite> loadPosts() {
        if (activeFilter == Filter.RECOMMENDED) {
            return postService.getRecommendedPosts(resolveCurrentUserId());
        }
        return postService.getData();
    }

    private List<FilActualite> safePosts() {
        try {
            return postService.getData();
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private List<FilActualite> filterPosts(List<FilActualite> posts) {
        String query = searchField == null || searchField.getText() == null
                ? ""
                : searchField.getText().trim().toLowerCase(Locale.ROOT);
        return posts.stream()
                .filter(post -> switch (activeFilter) {
                    case MEDIA -> hasMedia(post);
                    case EVENTS -> post.isEvent();
                    case ALL, RECOMMENDED -> true;
                })
                .filter(post -> query.isBlank() || matches(post, query))
                .sorted(Comparator.comparing(FilActualite::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private boolean matches(FilActualite post, String query) {
        return contains(post.getContent(), query)
                || contains(post.getEventTitle(), query)
                || contains(post.getEventLocation(), query)
                || contains(resolveAuthorName(post.getAuthorId()), query);
    }

    private VBox createPostCard(FilActualite post) {
        Label badge = new Label(resolveBadge(post));
        badge.getStyleClass().addAll("user-post-badge", post.isEvent() ? "event" : (hasMedia(post) ? "media" : "text"));

        Label title = new Label(post.getDisplayTitle());
        title.getStyleClass().add("user-post-title");
        title.setWrapText(true);

        Label date = new Label(post.getCreatedAt() == null ? "-" : DATE_FORMAT.format(post.getCreatedAt()));
        date.getStyleClass().add("user-feed-muted");

        HBox head = new HBox(12, badge, title, spacer(), date);
        head.setAlignment(Pos.CENTER_LEFT);

        String content = trimToNull(post.getContent());
        Label body = new Label(content == null ? "Publication media / evenement" : content);
        body.getStyleClass().add("user-post-body");
        body.setWrapText(true);

        VBox card = new VBox(12, head, body);
        card.getStyleClass().add("user-post-card");

        ImageView preview = buildPreview(post);
        if (preview != null) {
            card.getChildren().add(preview);
        }

        HBox meta = new HBox(12,
                metaLabel("Auteur " + resolveAuthorName(post.getAuthorId())),
                metaLabel(hasMedia(post) ? "Media" : "Texte"),
                metaLabel(post.isEvent() ? buildEventText(post) : "Fil general")
        );
        meta.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(meta);
        card.getChildren().add(buildEngagementBar(post));

        VBox commentsSection = buildCommentsSection(post);
        boolean expanded = expandedCommentPostIds.contains(post.getId());
        commentsSection.setVisible(expanded);
        commentsSection.setManaged(expanded);
        card.getChildren().add(commentsSection);
        return card;
    }

    private ImageView buildPreview(FilActualite post) {
        String imageSource = firstNonBlank(post.getImagePath(), isImageLike(post.getMediaFilename()) ? post.getMediaFilename() : null);
        if (imageSource == null) {
            return null;
        }
        try {
            ImageView imageView = new ImageView(new Image(imageSource, true));
            imageView.setFitHeight(190);
            imageView.setFitWidth(620);
            imageView.setPreserveRatio(true);
            imageView.getStyleClass().add("user-post-preview");
            return imageView;
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private void renderAnnouncements() {
        announcementsContainer.getChildren().clear();
        List<Announcement> announcements = announcementService.getData().stream().limit(4).toList();
        if (announcements.isEmpty()) {
            announcementsContainer.getChildren().add(emptyCard("Aucune annonce", "Les annonces officielles apparaitront ici."));
            return;
        }
        for (Announcement announcement : announcements) {
            Label tag = new Label(defaultText(announcement.getTag(), "INFO"));
            tag.getStyleClass().add("user-post-badge");
            Label title = new Label(announcement.getDisplayTitle());
            title.getStyleClass().add("card-title");
            title.setWrapText(true);
            Label body = new Label(defaultText(announcement.getContent(), "Annonce officielle E-sportify."));
            body.getStyleClass().add("user-feed-muted");
            body.setWrapText(true);
            VBox box = new VBox(8, tag, title, body);
            box.getStyleClass().add("user-side-card");
            announcementsContainer.getChildren().add(box);
        }
    }

    private void renderTrending(List<FilActualite> posts) {
        trendingContainer.getChildren().clear();
        long events = posts.stream().filter(FilActualite::isEvent).count();
        long media = posts.stream().filter(this::hasMedia).count();
        trendingContainer.getChildren().add(trend("#media", media + " publications avec image/video"));
        trendingContainer.getChildren().add(trend("#events", events + " evenements actifs"));
        trendingContainer.getChildren().add(trend("#communaute", posts.size() + " posts visibles"));
    }

    private FilActualite buildPost() {
        FilActualite post = new FilActualite();
        post.setContent(trimToNull(contentArea.getText()));
        post.setImagePath(trimToNull(imageField.getText()));
        post.setVideoUrl(trimToNull(videoField.getText()));
        post.setAuthorId(currentUser == null ? null : currentUser.getId());
        post.setCreatedAt(LocalDateTime.now());
        boolean eventMode = eventCheckBox != null && eventCheckBox.isSelected();
        post.setEvent(eventMode);
        if (eventMode) {
            post.setEventTitle(trimToNull(eventTitleField.getText()));
            post.setEventLocation(trimToNull(eventLocationField.getText()));
            post.setEventDate(LocalDateTime.now().plusDays(1));
            post.setMaxParticipants(parsePositiveInt(maxParticipantsField.getText()));
        }
        return post;
    }

    private void clearComposer() {
        contentArea.clear();
        imageField.clear();
        videoField.clear();
        eventCheckBox.setSelected(false);
        eventTitleField.clear();
        eventLocationField.clear();
        maxParticipantsField.clear();
        onToggleEvent();
    }

    private boolean hasMedia(FilActualite post) {
        return trimToNull(post.getImagePath()) != null
                || trimToNull(post.getVideoUrl()) != null
                || trimToNull(post.getMediaFilename()) != null;
    }

    private String resolveBadge(FilActualite post) {
        if (post.isEvent()) {
            return "EVENT";
        }
        return hasMedia(post) ? "MEDIA" : "TEXT";
    }

    private String buildEventText(FilActualite post) {
        String location = trimToNull(post.getEventLocation());
        return location == null ? "Evenement" : location;
    }

    private Label metaLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("user-feed-muted");
        return label;
    }

    private VBox emptyCard(String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("user-post-title");
        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("user-post-body");
        bodyLabel.setWrapText(true);
        VBox card = new VBox(8, titleLabel, bodyLabel);
        card.getStyleClass().add("user-post-card");
        return card;
    }

    private VBox emptySideCard(String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("user-feed-muted");
        bodyLabel.setWrapText(true);
        VBox card = new VBox(8, titleLabel, bodyLabel);
        card.getStyleClass().add("user-side-card");
        return card;
    }

    private VBox trend(String title, String body) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");
        Label bodyLabel = new Label(body);
        bodyLabel.getStyleClass().add("user-feed-muted");
        VBox box = new VBox(6, titleLabel, bodyLabel);
        box.getStyleClass().add("user-side-card");
        return box;
    }

    private VBox buildStreamingCard(String badgeText, String titleText, String metaText, String link) {
        Label badge = new Label(defaultText(badgeText, "LIVE"));
        badge.getStyleClass().add("user-post-badge");
        Label title = new Label(defaultText(titleText, "Flux e-sport"));
        title.getStyleClass().add("card-title");
        title.setWrapText(true);
        Label meta = new Label(defaultText(metaText, "Plateforme connectee"));
        meta.getStyleClass().add("user-feed-muted");
        meta.setWrapText(true);
        Hyperlink watchLink = new Hyperlink("Ouvrir le stream");
        watchLink.getStyleClass().add("streaming-link");
        watchLink.setOnAction(event -> openExternalLink(link));
        VBox card = new VBox(8, badge, title, meta, watchLink);
        card.getStyleClass().addAll("user-side-card", "streaming-mini-card");
        return card;
    }

    private VBox buildTaskSummaryCard(List<Task> tasks, List<Task> delayedTasks) {
        long done = tasks.stream().filter(task -> task.getStatus() == TaskStatus.DONE).count();
        Label title = new Label("Pilotage des taches");
        title.getStyleClass().add("card-title");
        Label body = new Label(
                tasks.size() + " tache(s) suivies - "
                        + done + " terminee(s) - "
                        + delayedTasks.size() + " en retard"
        );
        body.getStyleClass().add("user-feed-muted");
        body.setWrapText(true);
        VBox card = new VBox(8, title, body);
        card.getStyleClass().add("user-side-card");
        return card;
    }

    private VBox buildTaskCard(Task task) {
        Label title = new Label(defaultText(task.getTitle(), "Tache"));
        title.getStyleClass().add("card-title");
        Label meta = new Label(
                defaultText(task.getAssigneeUsername(), "Non assigne")
                        + " - "
                        + formatTaskStatus(task.getStatus())
                        + " - echeance "
                        + formatTaskDate(task.getDueDate())
        );
        meta.getStyleClass().add("user-feed-muted");
        meta.setWrapText(true);
        Label description = new Label(defaultText(task.getDescription(), "Aucune description"));
        description.getStyleClass().add("user-post-body");
        description.setWrapText(true);
        VBox card = new VBox(8, title, meta, description);
        card.getStyleClass().addAll("user-side-card", "task-card");
        return card;
    }

    private VBox buildInsightCard(AiRoleInsight insight) {
        Label title = new Label(insight.getRoleName());
        title.getStyleClass().add("card-title");
        Label badge = new Label(insight.getBadge() + " - " + insight.getPriority());
        badge.getStyleClass().add("user-post-badge");
        Label summary = new Label(defaultText(insight.getSummary(), "Analyse IA indisponible"));
        summary.getStyleClass().add("user-feed-muted");
        summary.setWrapText(true);
        VBox card = new VBox(8, badge, title, summary);
        card.getStyleClass().addAll("user-side-card", "ai-insight-card");
        for (String recommendation : insight.getRecommendations().stream().limit(2).toList()) {
            Label rec = new Label("• " + recommendation);
            rec.getStyleClass().add("ai-rec-line");
            rec.setWrapText(true);
            card.getChildren().add(rec);
        }
        return card;
    }

    private VBox buildGenericAiOverviewCard() {
        String displayName = currentUser == null ? "joueur" : defaultText(currentUser.getDisplayName(), "joueur");
        Label title = new Label("Assistant IA pret");
        title.getStyleClass().add("card-title");
        Label body = new Label(
                "Bonjour " + displayName + ". Aucune equipe active n'est detectee, "
                        + "mais l'IA peut deja te guider sur les prochaines priorites."
        );
        body.getStyleClass().add("user-feed-muted");
        body.setWrapText(true);
        VBox card = new VBox(8, title, body);
        card.getStyleClass().addAll("user-side-card", "ai-insight-card");
        return card;
    }

    private VBox buildGenericAiTaskCard() {
        Label title = new Label("Taches conseillees");
        title.getStyleClass().add("card-title");
        Label body = new Label("Actions utiles pour debloquer ton experience equipe et recommandations.");
        body.getStyleClass().add("user-feed-muted");
        body.setWrapText(true);

        VBox card = new VBox(8, title, body);
        card.getStyleClass().addAll("user-side-card", "task-card");
        card.getChildren().addAll(
                buildGenericAiLine("1. Rejoins une equipe ou ouvre ton espace equipe."),
                buildGenericAiLine("2. Mets a jour ton profil, ton niveau et tes disponibilites."),
                buildGenericAiLine("3. Interagis avec le feed pour affiner les suggestions IA.")
        );
        return card;
    }

    private VBox buildGenericAiRecommendationsCard() {
        Label title = new Label("Recommandations IA");
        title.getStyleClass().add("card-title");
        Label body = new Label(
                "Le bouton IA filtre deja les publications suggerees. Une fois dans une equipe, "
                        + "ce panneau affichera aussi les taches, retards et conseils de roster."
        );
        body.getStyleClass().add("user-feed-muted");
        body.setWrapText(true);

        VBox card = new VBox(8, title, body);
        card.getStyleClass().addAll("user-side-card", "ai-insight-card");
        card.getChildren().addAll(
                buildGenericAiLine("Priorite: rejoindre une equipe pour activer les analyses tactiques."),
                buildGenericAiLine("Suivi: les taches staff apparaissent ici automatiquement."),
                buildGenericAiLine("Conseil: utilise aussi les tournois et candidatures pour enrichir l'IA.")
        );
        return card;
    }

    private Label buildGenericAiLine(String text) {
        Label line = new Label(text);
        line.getStyleClass().add("ai-rec-line");
        line.setWrapText(true);
        return line;
    }

    private Button buildConversationButton(ConversationPreview preview) {
        return buildConversationButton(preview, true);
    }

    private Button buildConversationButton(ConversationPreview preview, boolean revealSection) {
        Label name = new Label(preview.getPeerDisplayName());
        name.getStyleClass().add("conversation-title");
        Label meta = new Label(
                defaultText(preview.getLastMessage(), "Conversation ouverte")
                        + (preview.getUnreadCount() > 0 ? " - " + preview.getUnreadCount() + " non lu(s)" : "")
        );
        meta.getStyleClass().add("conversation-meta");
        meta.setWrapText(true);
        VBox textBox = new VBox(4, name, meta);
        textBox.setFillWidth(true);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        Label avatar = new Label(defaultText(preview.getPeerAvatarLabel(), "U"));
        avatar.getStyleClass().add("user-post-badge");

        HBox row = new HBox(10, avatar, textBox);
        row.setAlignment(Pos.CENTER_LEFT);

        Button button = new Button();
        button.setGraphic(row);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("conversation-button");
        button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        if (activeConversation != null && preview.getConversationId() == activeConversation.getConversationId()) {
            button.getStyleClass().add("conversation-button-active");
        }
        button.setOnAction(event -> activateConversation(preview, revealSection));
        return button;
    }

    private void renderMessengerQuickButtons(List<ConversationPreview> conversations) {
        if (messengerQuickButtonsContainer == null) {
            return;
        }
        messengerQuickButtonsContainer.getChildren().clear();
        for (ConversationPreview preview : conversations.stream().limit(5).toList()) {
            messengerQuickButtonsContainer.getChildren().add(buildQuickConversationButton(preview));
        }
    }

    private Button buildQuickConversationButton(ConversationPreview preview) {
        Label avatar = new Label(defaultText(preview.getPeerAvatarLabel(), "U"));
        avatar.getStyleClass().add("conversation-quick-avatar");

        Label name = new Label(preview.getPeerDisplayName());
        name.getStyleClass().add("conversation-quick-name");

        String snippet = defaultText(preview.getLastMessage(), "Conversation ouverte");
        if (snippet.length() > 30) {
            snippet = snippet.substring(0, 27) + "...";
        }
        Label meta = new Label(snippet);
        meta.getStyleClass().add("conversation-meta");

        VBox textBox = new VBox(3, name, meta);
        HBox row = new HBox(10, avatar, textBox);
        row.setAlignment(Pos.CENTER_LEFT);

        Button button = new Button();
        button.setGraphic(row);
        button.getStyleClass().add("conversation-quick-button");
        if (activeConversation != null && preview.getConversationId() == activeConversation.getConversationId()) {
            button.getStyleClass().add("conversation-quick-button-active");
        }
        button.setOnAction(event -> activateConversation(preview, true));
        return button;
    }

    private void activateConversation(ConversationPreview preview) {
        activateConversation(preview, true);
    }

    private void activateConversation(ConversationPreview preview, boolean revealSection) {
        activeConversation = preview;
        renderActiveConversationMessages();
        refreshMessengerConversations();
        refreshFloatingPanels();
        if (revealSection) {
            scrollToSection(messengerSectionCard);
        }
    }

    private VBox buildMessageBubble(MessengerMessage message) {
        boolean mine = message.getSenderId() == resolveCurrentUserId();
        Label author = new Label(mine ? "Vous" : defaultText(message.getSenderDisplayName(), "Contact"));
        author.getStyleClass().add("message-meta-mini");
        Label content = new Label(defaultText(message.getContent(), defaultText(message.getAttachmentPath(), "Piece jointe")));
        content.setWrapText(true);
        Label time = new Label(message.getCreatedAt() == null ? "-" : DATE_FORMAT.format(message.getCreatedAt()));
        time.getStyleClass().add("message-meta-mini");

        VBox bubble = new VBox(4, author, content, time);
        bubble.getStyleClass().addAll("message-bubble-mini", mine ? "message-bubble-mine" : "message-bubble-theirs");
        HBox wrap = new HBox(bubble);
        wrap.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return new VBox(wrap);
    }

    private void openExternalLink(String link) {
        String normalized = trimToNull(link);
        if (normalized == null) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(normalized));
            } else {
                streamingStatusLabel.setText("Ouverture externe non supportee sur cette machine.");
            }
        } catch (Exception e) {
            streamingStatusLabel.setText("Impossible d'ouvrir le lien.");
        }
    }

    private void startCallInvite(String mode, boolean voiceOnly) {
        scrollToSection(messengerSectionCard);
        if (activeConversation == null) {
            setMessengerStatus("Choisissez d'abord une discussion pour lancer l'appel.");
            return;
        }

        String link = buildCallLink(mode, voiceOnly);
        String message = "[CALL_INVITE][" + mode + "] " + link;
        try {
            messengerService.sendMessage(
                    activeConversation.getConversationId(),
                    resolveCurrentUserId(),
                    message,
                    null
            );
            setMessengerStatus(voiceOnly ? "Invitation appel vocal envoyee." : "Invitation appel video envoyee.");
            renderActiveConversationMessages();
            refreshMessengerConversations();
            renderNotifications();
            refreshFloatingPanels();
            openMessengerCallLink(link);
        } catch (RuntimeException e) {
            setMessengerStatus("Appel impossible: " + e.getMessage());
        }
    }

    private String buildCallLink(String mode, boolean voiceOnly) {
        String room = "esportify-" + activeConversation.getConversationId() + "-" + mode;
        String suffix = voiceOnly
                ? "?config.prejoinPageEnabled=false#config.startWithVideoMuted=true"
                : "?config.prejoinPageEnabled=false#config.startWithVideoMuted=false";
        return "https://meet.jit.si/" + room + suffix;
    }

    private void openMessengerCallLink(String link) {
        String normalized = trimToNull(link);
        if (normalized == null) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(normalized));
            } else {
                setMessengerStatus("Ouverture externe non supportee pour l'appel.");
            }
        } catch (Exception e) {
            setMessengerStatus("Lien d'appel non ouvert.");
        }
    }

    private void scrollToSection(VBox target) {
        if (feedScrollPane == null || target == null || target.getScene() == null || feedScrollPane.getContent() == null) {
            return;
        }
        Platform.runLater(() -> {
            Point2D point = feedScrollPane.getContent().sceneToLocal(target.localToScene(0, 0));
            double viewportHeight = feedScrollPane.getViewportBounds().getHeight();
            double contentHeight = feedScrollPane.getContent().getLayoutBounds().getHeight();
            double targetY = Math.max(point.getY() - 20, 0);
            double denominator = Math.max(contentHeight - viewportHeight, 1);
            feedScrollPane.setVvalue(Math.min(targetY / denominator, 1.0));
        });
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private void refreshInteractionState() {
        commentsByPost.clear();
        likeCountsByPost.clear();
        shareCountsByPost.clear();
        likedPostIds.clear();
        savedPostIds.clear();

        try {
            List<Commentaire> comments = new ArrayList<>(commentaireService.getData());
            comments.sort(Comparator.comparing(Commentaire::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));
            for (Commentaire comment : comments) {
                commentsByPost.computeIfAbsent(comment.getPostId(), key -> new ArrayList<>()).add(comment);
            }
        } catch (RuntimeException e) {
            setStatus("Commentaires indisponibles: " + e.getMessage());
        }

        try {
            likedPostIds.addAll(socialInteractionService.getLikedPostIds(resolveCurrentUserId()));
            savedPostIds.addAll(socialInteractionService.getSavedPostIds(resolveCurrentUserId()));
            likeCountsByPost.putAll(socialInteractionService.getLikeCounts());
            shareCountsByPost.putAll(socialInteractionService.getShareCounts());
        } catch (RuntimeException e) {
            setStatus("Interactions sociales indisponibles: " + e.getMessage());
        }
    }

    private HBox buildEngagementBar(FilActualite post) {
        int likeCount = likeCountsByPost.getOrDefault(post.getId(), 0);
        int shareCount = shareCountsByPost.getOrDefault(post.getId(), 0);
        int commentCount = commentsByPost.getOrDefault(post.getId(), List.of()).size();
        boolean liked = likedPostIds.contains(post.getId());
        boolean saved = savedPostIds.contains(post.getId());
        boolean commentsExpanded = expandedCommentPostIds.contains(post.getId());

        Button likeButton = buildPostActionButton("J'aime " + likeCount, liked);
        likeButton.setOnAction(event -> handleToggleLike(post));

        Button commentButton = buildPostActionButton("Commentaires " + commentCount, commentsExpanded);
        commentButton.setOnAction(event -> toggleComments(post.getId()));

        Button shareButton = buildPostActionButton("Partager " + shareCount, false);
        shareButton.setOnAction(event -> handleSharePost(post));

        Button saveButton = buildPostActionButton(saved ? "Enregistre" : "Sauvegarder", saved);
        saveButton.setOnAction(event -> handleToggleSave(post));

        HBox actions = new HBox(10, likeButton, commentButton, shareButton, saveButton);
        actions.getStyleClass().add("post-actions-row");
        return actions;
    }

    private Button buildPostActionButton(String text, boolean active) {
        Button button = new Button(text);
        button.getStyleClass().add("post-engagement-button");
        if (active) {
            button.getStyleClass().add("post-engagement-button-active");
        }
        return button;
    }

    private VBox buildCommentsSection(FilActualite post) {
        VBox box = new VBox(10);
        box.getStyleClass().add("post-comments-shell");

        Label title = new Label("Discussion");
        title.getStyleClass().add("card-title");

        VBox commentsList = new VBox(8);
        List<Commentaire> comments = commentsByPost.getOrDefault(post.getId(), List.of());
        List<Commentaire> visibleComments = comments.size() > 5 ? comments.subList(comments.size() - 5, comments.size()) : comments;
        if (visibleComments.isEmpty()) {
            Label empty = new Label("Aucun commentaire pour le moment. Lance la discussion.");
            empty.getStyleClass().add("user-feed-muted");
            commentsList.getChildren().add(empty);
        } else {
            for (Commentaire comment : visibleComments) {
                commentsList.getChildren().add(buildCommentBubble(post, comment));
            }
        }

        TextField input = new TextField();
        input.setPromptText("Ecrire un commentaire...");
        input.getStyleClass().add("filter-input");
        HBox.setHgrow(input, Priority.ALWAYS);

        Button sendButton = new Button("Envoyer");
        sendButton.getStyleClass().add("feed-mini-action-button");
        sendButton.setOnAction(event -> handleAddComment(post, input));
        input.setOnAction(event -> handleAddComment(post, input));

        HBox composer = new HBox(10, input, sendButton);
        composer.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(title, commentsList, composer);
        return box;
    }

    private VBox buildCommentBubble(FilActualite post, Commentaire comment) {
        Label author = new Label(resolveAuthorName(comment.getAuthorId()));
        author.getStyleClass().add("conversation-title");

        Label date = new Label(comment.getCreatedAt() == null ? "Maintenant" : DATE_FORMAT.format(comment.getCreatedAt()));
        date.getStyleClass().add("message-meta-mini");

        HBox header = new HBox(10, author, spacer(), date);
        header.setAlignment(Pos.CENTER_LEFT);

        if (comment.getAuthorId() == resolveCurrentUserId()) {
            Button deleteButton = new Button("Supprimer");
            deleteButton.getStyleClass().add("comment-delete-button");
            deleteButton.setOnAction(event -> handleDeleteComment(post, comment));
            header.getChildren().add(deleteButton);
        }

        Label content = new Label(defaultText(comment.getContent(), "Commentaire"));
        content.getStyleClass().add("user-post-body");
        content.setWrapText(true);

        VBox bubble = new VBox(6, header, content);
        bubble.getStyleClass().add("comment-bubble");
        return bubble;
    }

    private void toggleComments(int postId) {
        if (expandedCommentPostIds.contains(postId)) {
            expandedCommentPostIds.remove(postId);
        } else {
            expandedCommentPostIds.add(postId);
        }
        refreshFeed();
        if (expandedCommentPostIds.contains(postId)) {
            focusPost(postId);
        }
    }

    private void handleToggleLike(FilActualite post) {
        try {
            boolean liked = socialInteractionService.toggleLike(post.getId(), resolveCurrentUserId());
            setStatus(liked ? "Publication aimee." : "J'aime retire.");
            refreshPostInteractionViews(post.getId(), false);
        } catch (RuntimeException e) {
            setStatus("Impossible de gerer le j'aime: " + e.getMessage());
        }
    }

    private void handleToggleSave(FilActualite post) {
        try {
            boolean saved = socialInteractionService.toggleSave(post.getId(), resolveCurrentUserId());
            setStatus(saved ? "Publication sauvegardee." : "Sauvegarde retiree.");
            refreshPostInteractionViews(post.getId(), false);
        } catch (RuntimeException e) {
            setStatus("Impossible de sauvegarder: " + e.getMessage());
        }
    }

    private void handleSharePost(FilActualite post) {
        try {
            socialInteractionService.addShare(post.getId(), resolveCurrentUserId());
            setStatus("Publication partagee.");
            refreshPostInteractionViews(post.getId(), false);
        } catch (RuntimeException e) {
            setStatus("Partage impossible: " + e.getMessage());
        }
    }

    private void handleAddComment(FilActualite post, TextField input) {
        String content = input == null ? null : trimToNull(input.getText());
        if (content == null) {
            setStatus("Ajoute un commentaire avant d'envoyer.");
            return;
        }
        try {
            Commentaire comment = new Commentaire();
            comment.setAuthorId(resolveCurrentUserId());
            comment.setPostId(post.getId());
            comment.setContent(content);
            comment.setCreatedAt(LocalDateTime.now());
            commentaireService.addEntity(comment);
            expandedCommentPostIds.add(post.getId());
            if (input != null) {
                input.clear();
            }
            setStatus("Commentaire ajoute.");
            refreshPostInteractionViews(post.getId(), true);
        } catch (RuntimeException e) {
            setStatus("Commentaire impossible: " + e.getMessage());
        }
    }

    private void handleDeleteComment(FilActualite post, Commentaire comment) {
        try {
            commentaireService.deleteEntity(comment);
            expandedCommentPostIds.add(post.getId());
            setStatus("Commentaire supprime.");
            refreshPostInteractionViews(post.getId(), true);
        } catch (RuntimeException e) {
            setStatus("Suppression impossible: " + e.getMessage());
        }
    }

    private void refreshPostInteractionViews(int postId, boolean keepCommentsExpanded) {
        if (keepCommentsExpanded) {
            expandedCommentPostIds.add(postId);
        }
        refreshInteractionState();
        refreshFeed();
        renderNotifications();
        focusPost(postId);
    }

    private void focusPost(int postId) {
        VBox card = postCardsById.get(postId);
        if (card == null) {
            return;
        }
        Platform.runLater(() -> scrollToSection(card));
    }

    private boolean isCurrentUsersPost(FilActualite post) {
        return post != null
                && post.getAuthorId() != null
                && post.getAuthorId() == resolveCurrentUserId();
    }

    private int countUnreadNotifications(List<NotificationItem> items) {
        int unread = 0;
        for (NotificationItem item : items) {
            if (!readNotificationKeys.contains(item.key())) {
                unread++;
            }
        }
        return unread;
    }

    private void markAllNotificationsRead() {
        for (NotificationItem item : buildNotificationItems()) {
            readNotificationKeys.add(item.key());
        }
        renderNotifications();
    }

    private void openNotification(NotificationItem item) {
        if (item == null) {
            return;
        }
        readNotificationKeys.add(item.key());
        renderNotifications();
        hideNotificationsPopup();
        switch (item.target()) {
            case POST -> revealPost(item.targetId(), false);
            case COMMENT -> revealPost(item.targetId(), true);
            case MESSENGER -> openMessengerPopup();
            case ANNOUNCEMENT, INFO -> {
                if (notificationsSectionCard != null) {
                    scrollToSection(notificationsSectionCard);
                }
            }
        }
    }

    private void revealPost(int postId, boolean showComments) {
        if (searchField != null) {
            searchField.clear();
        }
        if (showComments) {
            expandedCommentPostIds.add(postId);
        }
        setFilter(Filter.ALL, allFilterButton);
        focusPost(postId);
    }

    private void openConversationWithPeer(UserProfile peer, boolean revealSection) {
        if (peer == null) {
            setMessengerStatus("Choisissez d'abord un contact.");
            return;
        }
        try {
            int conversationId = messengerService.findOrCreateConversation(resolveCurrentUserId(), peer.getId());
            ConversationPreview preview = new ConversationPreview();
            preview.setConversationId(conversationId);
            preview.setPeerUserId(peer.getId());
            preview.setPeerDisplayName(peer.getDisplayName());
            preview.setPeerAvatarLabel(peer.getAvatarLabel());
            preview.setPeerOnline(messengerService.getPresenceService().isOnline(peer.getId()));
            preview.setLastMessage("Conversation ouverte");
            preview.setUnreadCount(0);
            activeConversation = preview;
            refreshMessenger();
            renderNotifications();
            refreshFloatingPanels();
            setMessengerStatus("Conversation ouverte avec " + peer.getDisplayName() + ".");
            if (revealSection) {
                scrollToSection(messengerSectionCard);
            }
        } catch (RuntimeException e) {
            setMessengerStatus("Messenger indisponible: " + e.getMessage());
        }
    }

    private void sendMessengerMessage(String content, Runnable clearInput) {
        if (activeConversation == null) {
            setMessengerStatus("Selectionnez une conversation.");
            return;
        }
        try {
            messengerService.sendMessage(
                    activeConversation.getConversationId(),
                    resolveCurrentUserId(),
                    content,
                    null
            );
            if (clearInput != null) {
                clearInput.run();
            }
            setMessengerStatus("Message envoye.");
            refreshMessenger();
            renderNotifications();
            refreshFloatingPanels();
        } catch (RuntimeException e) {
            setMessengerStatus("Envoi impossible: " + e.getMessage());
        }
    }

    private void toggleMessengerPopup() {
        if (messengerPopupStage != null && messengerPopupStage.isShowing()) {
            hideMessengerPopup();
            return;
        }
        openMessengerPopup();
    }

    private void toggleNotificationsPopup() {
        if (notificationsPopupStage != null && notificationsPopupStage.isShowing()) {
            hideNotificationsPopup();
            return;
        }
        openNotificationsPopup();
    }

    private void openMessengerPopup() {
        hideNotificationsPopup();
        Parent root = buildMessengerPopupRoot();
        messengerPopupStage = prepareFloatingStage(messengerPopupStage, root, 470, 590);
        showAnchoredStage(messengerPopupStage, topMessengerButton);
    }

    private void openNotificationsPopup() {
        hideMessengerPopup();
        Parent root = buildNotificationsPopupRoot();
        notificationsPopupStage = prepareFloatingStage(notificationsPopupStage, root, 360, 500);
        showAnchoredStage(notificationsPopupStage, topNotificationsButton);
    }

    private void hideMessengerPopup() {
        if (messengerPopupStage != null) {
            messengerPopupStage.hide();
        }
    }

    private void hideNotificationsPopup() {
        if (notificationsPopupStage != null) {
            notificationsPopupStage.hide();
        }
    }

    private void refreshFloatingPanels() {
        refreshMessengerPopup();
        refreshNotificationsPopup();
    }

    private void refreshMessengerPopup() {
        if (messengerPopupStage != null && messengerPopupStage.isShowing()) {
            Parent root = buildMessengerPopupRoot();
            messengerPopupStage = prepareFloatingStage(messengerPopupStage, root, 470, 590);
        }
    }

    private void refreshNotificationsPopup() {
        if (notificationsPopupStage != null && notificationsPopupStage.isShowing()) {
            Parent root = buildNotificationsPopupRoot();
            notificationsPopupStage = prepareFloatingStage(notificationsPopupStage, root, 360, 500);
        }
    }

    private Parent buildMessengerPopupRoot() {
        VBox shell = new VBox(14);
        shell.getStyleClass().add("floating-panel-shell");

        int unread = 0;
        List<ConversationPreview> conversations = List.of();
        try {
            conversations = messengerService.getConversationsForUser(resolveCurrentUserId());
            unread = messengerService.getUnreadCount(resolveCurrentUserId());
        } catch (RuntimeException e) {
            setMessengerStatus("Messenger indisponible.");
        }

        if (activeConversation == null && !conversations.isEmpty()) {
            activeConversation = conversations.get(0);
        }

        Label title = new Label("Discussions");
        title.getStyleClass().add("floating-title");
        Label badge = new Label(unread <= 0 ? "A jour" : unread + " non lu(s)");
        badge.getStyleClass().add("cat-pill");
        Button close = buildFloatingCloseButton(this::hideMessengerPopup);
        HBox header = new HBox(10, title, spacer(), badge, close);
        header.setAlignment(Pos.CENTER_LEFT);

        ComboBox<UserProfile> picker = new ComboBox<>();
        picker.getStyleClass().add("filter-input");
        picker.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(picker, Priority.ALWAYS);
        configureMessengerPicker(picker);
        try {
            picker.getItems().setAll(messengerService.getAvailableContacts(resolveCurrentUserId()));
        } catch (RuntimeException ignored) {
        }
        Button openButton = new Button("Ouvrir");
        openButton.getStyleClass().add("feed-mini-action-button");
        openButton.setOnAction(event -> openConversationWithPeer(picker.getValue(), false));
        HBox composer = new HBox(10, picker, openButton);
        composer.setAlignment(Pos.CENTER_LEFT);

        VBox conversationsBox = new VBox(8);
        if (conversations.isEmpty()) {
            conversationsBox.getChildren().add(emptySideCard("Aucune discussion", "Choisissez un contact pour commencer."));
        } else {
            for (ConversationPreview preview : conversations.stream().limit(8).toList()) {
                conversationsBox.getChildren().add(buildConversationButton(preview, false));
            }
        }
        ScrollPane conversationsScroll = new ScrollPane(conversationsBox);
        conversationsScroll.setFitToWidth(true);
        conversationsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        conversationsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        conversationsScroll.getStyleClass().add("user-feed-message-scroll");
        VBox sidebar = new VBox(10, sectionLabel("Recents"), conversationsScroll);
        sidebar.getStyleClass().add("floating-sidebar");

        VBox messagesBox = new VBox(8);
        if (activeConversation == null) {
            messagesBox.getChildren().add(emptySideCard("Messagerie prete", "Ouvrez une conversation existante ou creez-en une nouvelle."));
        } else {
            try {
                List<MessengerMessage> messages = messengerService.getMessages(
                        activeConversation.getConversationId(),
                        resolveCurrentUserId(),
                        MESSENGER_PAGE_SIZE,
                        null
                );
                if (messages.isEmpty()) {
                    messagesBox.getChildren().add(emptySideCard("Conversation vide", "Envoyez le premier message."));
                } else {
                    for (MessengerMessage message : messages) {
                        messagesBox.getChildren().add(buildMessageBubble(message));
                    }
                }
            } catch (RuntimeException e) {
                messagesBox.getChildren().add(emptySideCard("Chargement impossible", e.getMessage()));
            }
        }

        Label activeLabel = new Label(activeConversation == null ? "Selectionnez une conversation" : activeConversation.getPeerDisplayName());
        activeLabel.getStyleClass().add("card-title");
        Label presenceLabel = new Label(activeConversation != null && activeConversation.isPeerOnline() ? "En ligne" : "Pas de canal actif");
        presenceLabel.getStyleClass().add("user-feed-muted");

        Button voiceButton = new Button("Vocal");
        voiceButton.getStyleClass().add("feed-mini-action-button");
        voiceButton.setDisable(activeConversation == null);
        voiceButton.setOnAction(event -> onStartVoiceCall());

        Button videoButton = new Button("Video");
        videoButton.getStyleClass().add("feed-mini-action-button");
        videoButton.setDisable(activeConversation == null);
        videoButton.setOnAction(event -> onStartVideoCall());

        HBox chatHeader = new HBox(10, activeLabel, spacer(), presenceLabel, voiceButton, videoButton);
        chatHeader.setAlignment(Pos.CENTER_LEFT);

        ScrollPane messagesScroll = new ScrollPane(messagesBox);
        messagesScroll.setFitToWidth(true);
        messagesScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        messagesScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        messagesScroll.getStyleClass().add("user-feed-message-scroll");
        Platform.runLater(() -> messagesScroll.setVvalue(1.0));

        TextField popupInput = new TextField();
        popupInput.setPromptText("Ecrire un message...");
        popupInput.getStyleClass().add("filter-input");
        HBox.setHgrow(popupInput, Priority.ALWAYS);
        Button sendButton = new Button("Envoyer");
        sendButton.getStyleClass().add("feed-mini-action-button");
        sendButton.setOnAction(event -> sendMessengerMessage(popupInput.getText(), popupInput::clear));
        popupInput.setOnAction(event -> sendMessengerMessage(popupInput.getText(), popupInput::clear));
        HBox sendRow = new HBox(10, popupInput, sendButton);
        sendRow.setAlignment(Pos.CENTER_LEFT);

        VBox chatBox = new VBox(12, chatHeader, messagesScroll, sendRow);
        chatBox.getStyleClass().add("floating-chat");
        HBox.setHgrow(chatBox, Priority.ALWAYS);

        HBox body = new HBox(12, sidebar, chatBox);
        body.getStyleClass().add("floating-panel-body");
        shell.getChildren().addAll(header, composer, body);
        return shell;
    }

    private Parent buildNotificationsPopupRoot() {
        VBox shell = new VBox(14);
        shell.getStyleClass().add("floating-panel-shell");

        List<NotificationItem> items = buildNotificationItems();
        int unread = countUnreadNotifications(items);

        Label title = new Label("Notifications");
        title.getStyleClass().add("floating-title");
        Label badge = new Label(unread + " alerte(s)");
        badge.getStyleClass().add("cat-pill");
        Button markAll = new Button("Tout lire");
        markAll.getStyleClass().add("feed-mini-action-button");
        markAll.setOnAction(event -> markAllNotificationsRead());
        Button close = buildFloatingCloseButton(this::hideNotificationsPopup);
        HBox header = new HBox(10, title, spacer(), badge, markAll, close);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox list = new VBox(10);
        if (items.isEmpty()) {
            list.getChildren().add(emptySideCard("Boite vide", "Aucune notification pour le moment."));
        } else {
            for (NotificationItem item : items) {
                list.getChildren().add(buildNotificationPopupItem(item));
            }
        }
        ScrollPane scrollPane = new ScrollPane(list);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("user-feed-message-scroll");

        shell.getChildren().addAll(header, scrollPane);
        return shell;
    }

    private VBox buildNotificationPopupItem(NotificationItem item) {
        VBox card = new VBox(6);
        card.getStyleClass().add("notification-popup-item");
        if (!readNotificationKeys.contains(item.key())) {
            card.getStyleClass().add("notification-popup-item-unread");
        }
        Label title = new Label(item.title());
        title.getStyleClass().add("notification-title-mini");
        title.setWrapText(true);
        Label body = new Label(item.body());
        body.getStyleClass().add("notification-body-mini");
        body.setWrapText(true);
        Label date = new Label(item.createdAt() == null ? "Maintenant" : DATE_FORMAT.format(item.createdAt()));
        date.getStyleClass().add("message-meta-mini");
        card.getChildren().addAll(title, body, date);
        card.setOnMouseClicked(event -> openNotification(item));
        return card;
    }

    private Button buildFloatingCloseButton(Runnable action) {
        Button close = new Button("Fermer");
        close.getStyleClass().add("floating-close-button");
        close.setOnAction(event -> action.run());
        return close;
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("user-feed-muted");
        return label;
    }

    private Stage prepareFloatingStage(Stage existingStage, Parent root, double width, double height) {
        Stage stage = existingStage;
        Window owner = feedScrollPane == null || feedScrollPane.getScene() == null ? null : feedScrollPane.getScene().getWindow();
        if (stage == null) {
            stage = new Stage(StageStyle.TRANSPARENT);
            if (owner != null) {
                stage.initOwner(owner);
            }
            stage.setResizable(false);
            Stage floatingStage = stage;
            stage.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
                if (!isFocused) {
                    floatingStage.hide();
                }
            });
        }

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, width, height);
            scene.setFill(Color.TRANSPARENT);
            String stylesheet = resolveAppStylesheet();
            if (stylesheet != null) {
                scene.getStylesheets().add(stylesheet);
            }
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
            scene.setFill(Color.TRANSPARENT);
        }
        stage.sizeToScene();
        stage.setWidth(width);
        stage.setHeight(height);
        return stage;
    }

    private String resolveAppStylesheet() {
        return getClass().getResource("/styles/app.css") == null
                ? null
                : getClass().getResource("/styles/app.css").toExternalForm();
    }

    private void showAnchoredStage(Stage stage, Button anchor) {
        if (stage == null || anchor == null || anchor.getScene() == null) {
            return;
        }
        Point2D anchorPoint = anchor.localToScreen(0, anchor.getHeight());
        if (anchorPoint == null) {
            return;
        }
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double stageWidth = stage.getWidth() > 0 ? stage.getWidth() : stage.getScene().getWidth();
        double x = anchorPoint.getX() - stageWidth + anchor.getWidth();
        double y = anchorPoint.getY() + 12;
        x = Math.max(bounds.getMinX() + 12, Math.min(x, bounds.getMaxX() - stageWidth - 12));
        y = Math.max(bounds.getMinY() + 12, Math.min(y, bounds.getMaxY() - stage.getHeight() - 12));
        stage.setX(x);
        stage.setY(y);
        stage.show();
        stage.requestFocus();
    }

    private String resolveAuthorName(Integer authorId) {
        if (authorId == null) {
            return "System";
        }
        UserProfile user = usersById.get(authorId);
        return user == null ? "Auteur #" + authorId : user.getDisplayName();
    }

    private Equipe resolveContextTeam() {
        Equipe selectedEquipe = AppSession.getInstance().getSelectedEquipe();
        if (selectedEquipe != null && selectedEquipe.getId() > 0) {
            return selectedEquipe;
        }

        String username = AppSession.getInstance().getUsername();
        Equipe managerTeam = equipeService.getByManagerUsername(username);
        if (managerTeam != null) {
            return managerTeam;
        }

        Candidature accepted = candidatureService.getAcceptedForUser(username);
        if (accepted != null) {
            return equipeService.getById(accepted.getEquipeId());
        }
        return null;
    }

    private int resolveCurrentUserId() {
        return currentUser == null ? 1 : currentUser.getId();
    }

    private String formatTaskStatus(TaskStatus status) {
        if (status == null) {
            return "TODO";
        }
        return switch (status) {
            case TODO -> "A faire";
            case IN_PROGRESS -> "En cours";
            case DONE -> "Terminee";
            case BLOCKED -> "Bloquee";
        };
    }

    private String formatTaskDate(LocalDate date) {
        return date == null ? "--" : DAY_FORMAT.format(date);
    }

    private boolean contains(String source, String query) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(query);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String trimmed = trimToNull(value);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return null;
    }

    private boolean isImageLike(String value) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            return false;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".webp");
    }

    private Integer parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value == null ? "" : value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String defaultText(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private void setMessengerStatus(String message) {
        if (messengerStatusLabel != null) {
            messengerStatusLabel.setText(message);
        } else {
            setStatus(message);
        }
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message == null || message.isBlank() ? "Pret" : message);
        }
    }

    private record NotificationItem(
            String key,
            String title,
            String body,
            LocalDateTime createdAt,
            NotificationTarget target,
            int targetId
    ) { }
}
