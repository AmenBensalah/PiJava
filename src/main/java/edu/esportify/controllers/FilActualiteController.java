package edu.esportify.controllers;

import edu.esportify.entities.Announcement;
import edu.esportify.entities.Commentaire;
import edu.esportify.entities.ConversationPreview;
import edu.esportify.entities.FilActualite;
import edu.esportify.entities.MessengerMessage;
import edu.esportify.entities.UserProfile;
import edu.esportify.services.AnnouncementService;
import edu.esportify.services.CommentaireService;
import edu.esportify.services.FilActualiteService;
import edu.esportify.services.MessengerRealtimeBridge;
import edu.esportify.services.MessengerService;
import edu.esportify.services.MessengerCallService;
import edu.esportify.services.SocialInteractionService;
import edu.esportify.services.StreamingIntegrationService;
import edu.esportify.services.UserDirectoryService;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.CacheHint;
import javafx.scene.layout.HBox;
import javafx.scene.Parent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilActualiteController {
    private static final Pattern MEDIA_URL_PATTERN = Pattern.compile("(https?://\\S+|/uploads/\\S+|[A-Za-z]:\\\\\\S+)");
    private static final Pattern CALL_INVITE_PATTERN = Pattern.compile("^\\[CALL_INVITE]\\[(voice|video)]\\s+(https?://\\S+)$");

    private static final String FILTER_ALL = "tout";
    private static final String FILTER_TEAMS = "equipes";
    private static final String FILTER_PLAYERS = "joueurs";
    private static final String FILTER_TOURNAMENTS = "tournois";
    private static final String FILTER_MEDIA = "media";
    private static final String FILTER_AI_RECOMMENDATIONS = "ai_recommendations";
    private static final String PUBLIC_ROOT = "C:/Users/bouzi/Desktop/PI_DEV_ESPORTIFY/public";
    private static final String PUBLIC_MEDIA_ROOT = "C:/Users/bouzi/Desktop/PI_DEV_ESPORTIFY/public";
    private static final String PUBLIC_UPLOADS_DIRECTORY = PUBLIC_MEDIA_ROOT + "/uploads";
    private static final DateTimeFormatter TABLE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FRONT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);
    private static final DateTimeFormatter STREAM_SYNC_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int FRONT_CONTENT_MAX_LENGTH = 5000;
    private static final Duration FRONT_PUBLISH_DELAY = Duration.millis(700);
    private static final Duration POST_APPEAR_DURATION = Duration.millis(260);
    private static final Duration FRONT_SEARCH_DEBOUNCE = Duration.millis(160);
    private static final Duration MESSENGER_POLL_RATE = Duration.seconds(2);
    private static final int MESSENGER_PAGE_SIZE = 18;
    private static final int FRONT_POST_PREVIEW_LIMIT = 280;

    private final FilActualiteService service = new FilActualiteService();
    private final AnnouncementService announcementService = new AnnouncementService();
    private final CommentaireService commentaireService = new CommentaireService();
    private final UserDirectoryService userDirectoryService = new UserDirectoryService();
    private final SocialInteractionService socialInteractionService = new SocialInteractionService();
    private final MessengerService messengerService = new MessengerService();
    private final MessengerCallService messengerCallService = new MessengerCallService();
    private final StreamingIntegrationService streamingIntegrationService = new StreamingIntegrationService();
    private final ObservableList<FilActualite> posts = FXCollections.observableArrayList();
    private final ObservableList<FilActualite> frontFeedItems = FXCollections.observableArrayList();
    private final ObservableList<Announcement> announcements = FXCollections.observableArrayList();
    private final ObservableList<Commentaire> commentaires = FXCollections.observableArrayList();
    private final ObservableList<Announcement> frontAnnouncementsItems = FXCollections.observableArrayList();
    private final Map<Integer, List<Commentaire>> commentsByPost = new HashMap<>();
    private final ObservableList<String> profileItems = FXCollections.observableArrayList();
    private final ObservableList<ConversationPreview> messengerConversations = FXCollections.observableArrayList();
    private final ObservableList<UserProfile> messengerContacts = FXCollections.observableArrayList();
    private final ObservableList<AppNotification> appNotifications = FXCollections.observableArrayList();
    private final Set<Integer> savedPostIds = new HashSet<>();
    private final Set<Integer> likedPostIds = new HashSet<>();
    private final Set<String> readNotificationKeys = new HashSet<>();
    private final Map<Integer, Integer> likeCountsByPost = new HashMap<>();
    private final Map<Integer, Integer> shareCountsByPost = new HashMap<>();
    private Map<Integer, UserProfile> usersById = new HashMap<>();
    private UserProfile currentUser;
    private ConversationPreview activeConversation;
    private Integer oldestLoadedMessengerMessageId;
    private int lastMessengerUnreadCount = -1;
    private boolean messengerOpen;
    private boolean messengerInboxOpen;
    private boolean notificationsOpen;
    private boolean messengerBubbleOpen;
    private boolean messengerRefreshInProgress;
    private final Timeline messengerRefreshTimeline = new Timeline();
    private final Timeline streamingRefreshTimeline = new Timeline();
    private final PauseTransition messengerTypingPause = new PauseTransition(Duration.seconds(1.2));
    private final MessengerRealtimeBridge.Listener messengerRealtimeListener = this::refreshMessengerRealtime;

    private String currentFilter = FILTER_ALL;
    private FilActualite selectedPost;
    private Announcement selectedAnnouncement;
    private Commentaire selectedComment;

    @FXML private VBox backOfficeShell;
    @FXML private VBox frontOfficeShell;
    @FXML private Button switchToBackButton;
    @FXML private Button switchToFrontButton;
    @FXML private Button dashboardButton;
    @FXML private Button postsButton;
    @FXML private Button announcementsButton;
    @FXML private Button commentsButton;
    @FXML private Button aiButton;
    @FXML private StackPane backContentStack;
    @FXML private VBox dashboardView;
    @FXML private StackPane backofficeDashboard;
    @FXML private BackofficeController backofficeDashboardController;
    @FXML private VBox postsView;
    @FXML private VBox postManagementModule;
    @FXML private PostManagementController postManagementModuleController;
    @FXML private VBox announcementsView;
    @FXML private VBox announcementManagementModule;
    @FXML private AnnouncementManagementController announcementManagementModuleController;
    @FXML private VBox commentsView;
    @FXML private VBox commentManagementModule;
    @FXML private CommentManagementController commentManagementModuleController;
    @FXML private VBox aiView;
    @FXML private TextField searchField;
    @FXML private TextArea contentArea;
    @FXML private TextField imageField;
    @FXML private TextField videoField;
    @FXML private CheckBox eventCheckBox;
    @FXML private TextField eventTitleField;
    @FXML private DatePicker eventDateField;
    @FXML private TextField eventLocationField;
    @FXML private TextField maxParticipantsField;
    @FXML private TextField authorIdField;
    @FXML private TextField announcementTitleField;
    @FXML private TextField announcementTagField;
    @FXML private TextField announcementLinkField;
    @FXML private TextField announcementMediaTypeField;
    @FXML private TextField announcementMediaFilenameField;
    @FXML private TextArea announcementContentArea;
    @FXML private TextArea commentContentArea;
    @FXML private TextField commentAuthorIdField;
    @FXML private TextField commentPostIdField;
    @FXML private TableView<FilActualite> postsTable;
    @FXML private TableColumn<FilActualite, Number> idColumn;
    @FXML private TableColumn<FilActualite, String> contentColumn;
    @FXML private TableColumn<FilActualite, String> mediaColumn;
    @FXML private TableColumn<FilActualite, String> eventColumn;
    @FXML private TableColumn<FilActualite, String> authorColumn;
    @FXML private TableColumn<FilActualite, String> createdAtColumn;
    @FXML private TableView<Announcement> announcementsTable;
    @FXML private TableColumn<Announcement, Number> announcementIdColumn;
    @FXML private TableColumn<Announcement, String> announcementTagColumn;
    @FXML private TableColumn<Announcement, String> announcementTitleColumn;
    @FXML private TableColumn<Announcement, String> announcementLinkColumn;
    @FXML private TableColumn<Announcement, String> announcementCreatedAtColumn;
    @FXML private TableView<Commentaire> commentsTable;
    @FXML private TableColumn<Commentaire, Number> commentIdColumn;
    @FXML private TableColumn<Commentaire, String> commentAuthorColumn;
    @FXML private TableColumn<Commentaire, String> commentPostColumn;
    @FXML private TableColumn<Commentaire, String> commentContentColumn;
    @FXML private TableColumn<Commentaire, String> commentCreatedAtColumn;
    @FXML private Button frontFeedButton;
    @FXML private Button frontEquipesButton;
    @FXML private Button frontTournoisButton;
    @FXML private Button frontBoutiqueButton;
    @FXML private Button frontCommandesButton;
    @FXML private Button frontSavedButton;
    @FXML private Button frontProfileButton;
    @FXML private StackPane frontContentStack;
    @FXML private VBox frontFeedView;
    @FXML private VBox frontPostDetailView;
    @FXML private VBox frontPostDetailContainer;
    @FXML private Label frontPostDetailHeaderLabel;
    @FXML private VBox frontStreamingView;
    @FXML private VBox frontSavedView;
    @FXML private VBox frontProfileView;
    @FXML private VBox frontComposerCard;
    @FXML private TextField frontSearchField;
    @FXML private TextArea frontContentArea;
    @FXML private TextField frontImageField;
    @FXML private TextField frontVideoField;
    @FXML private ImageView frontMediaPreview;
    @FXML private Label frontMediaHintLabel;
    @FXML private CheckBox frontEventCheckBox;
    @FXML private HBox frontEventFieldsRow;
    @FXML private TextField frontEventTitleField;
    @FXML private DatePicker frontEventDateField;
    @FXML private TextField frontEventLocationField;
    @FXML private TextField frontMaxParticipantsField;
    @FXML private TextField frontAuthorIdField;
    @FXML private Button frontPublishButton;
    @FXML private Label frontComposerUserLabel;
    @FXML private Label frontComposerAvatarLabel;
    @FXML private ProgressIndicator frontPublishProgress;
    @FXML private Button filterAllButton;
    @FXML private Button filterTeamsButton;
    @FXML private Button filterPlayersButton;
    @FXML private Button filterTournamentsButton;
    @FXML private Button filterMediaButton;
    @FXML private Button filterAiButton;
    @FXML private VBox frontFeedBox;
    @FXML private VBox frontAnnouncementsBox;
    @FXML private VBox frontTrendingBox;
    @FXML private VBox streamingLiveBox;
    @FXML private VBox streamingHighlightsBox;
    @FXML private Label streamingStatusLabel;
    @FXML private Label streamingLiveCountLabel;
    @FXML private Label streamingHighlightCountLabel;
    @FXML private Label streamingLastSyncLabel;
    @FXML private VBox frontSavedBox;
    @FXML private ListView<String> frontProfileList;
    @FXML private Label frontBestTimeLabel;
    @FXML private Label frontBestTimeDescriptionLabel;
    @FXML private Label frontComposerStatusLabel;
    @FXML private Label frontCharCountLabel;
    @FXML private Label statusLabel;
    @FXML private StackPane messengerInboxPopup;
    @FXML private ListView<ConversationPreview> messengerInboxList;
    @FXML private StackPane messengerBubbleWindow;
    @FXML private VBox messengerBubbleMessageBox;
    @FXML private ScrollPane messengerBubbleScroll;
    @FXML private TextField messengerBubbleInputField;
    @FXML private Label messengerBubbleTypingLabel;
    @FXML private Label messengerBubbleAvatarLabel;
    @FXML private Label messengerBubbleNameLabel;
    @FXML private Label messengerBubbleStatusLabel;
    @FXML private StackPane messengerWindow;
    @FXML private StackPane notificationsPopup;
    @FXML private ListView<AppNotification> notificationsListView;
    @FXML private Button topbarNotificationsButton;
    @FXML private Label topbarNotificationsBadgeLabel;
    @FXML private Button topbarMessengerButton;
    @FXML private Label topbarMessengerBadgeLabel;
    @FXML private Label messengerUnreadBadgeLabel;
    @FXML private ListView<ConversationPreview> messengerConversationList;
    @FXML private VBox messengerMessageBox;
    @FXML private ScrollPane messengerMessageScroll;
    @FXML private TextField messengerInputField;
    @FXML private Label messengerTypingLabel;
    @FXML private Label messengerHeaderAvatarLabel;
    @FXML private Label messengerHeaderSubtitleLabel;
    @FXML private Label messengerPeerAvatarLabel;
    @FXML private Label messengerPeerNameLabel;
    @FXML private Label messengerPresenceLabel;
    @FXML private Region messengerPresenceDot;
    @FXML private Button messengerVoiceCallButton;
    @FXML private Button messengerVideoCallButton;
    @FXML private ComboBox<UserProfile> messengerContactPicker;
    @FXML private HBox messengerContactPickerRow;
    @FXML private TextField messengerSearchField;
    @FXML private Button messengerLoadOlderButton;
    @FXML private Button messengerSendButton;

    @FXML private Button adminFilGroupButton;
    @FXML private Button adminEquipesButton;
    @FXML private Button adminTournoiButton;
    @FXML private Button adminBoutiqueButton;
    @FXML private Button adminComptesButton;
    @FXML private VBox adminFilSubmenu;
    @FXML private Label adminFilChevron;
    @FXML private VBox adminSidebar;
    @FXML private Button adminMenuButton;

    @FXML private VBox frontSidebar;
    @FXML private Button frontMenuButton;
    @FXML private Button frontAdminButton;
    @FXML private Label frontAdminBadgeLabel;
    @FXML private Button adminLogoutButton;
    @FXML private Button frontLogoutButton;

    private boolean adminCollapsed = false;
    private boolean frontCollapsed = false;
    private boolean frontPublishInFlight = false;
    private Integer highlightedFrontPostId;
    private final PauseTransition frontSearchPause = new PauseTransition(FRONT_SEARCH_DEBOUNCE);

    @FXML
    private void initialize() {
        resolveUserContext();
        configureTable();
        configureAnnouncementTable();
        configureCommentTable();
        configureFeedLists();
        configureStaticPanels();
        configureFrontSearch();
        configureFrontComposer();
        configureNotifications();
        configureMessenger();
        configureStreaming();
        refreshStreamingHub();
        refreshAll();
        configureBackofficeDashboard();
        configurePostManagementModule();
        configureAnnouncementManagementModule();
        configureCommentManagementModule();
        applySidebarState(adminSidebar, false);
        applySidebarState(frontSidebar, false);
        showFrontOffice();
        showFrontView(frontFeedView, frontFeedButton);
    }

    @FXML private void handleShowDashboard() { if (showBackOffice()) { showBackView(dashboardView, dashboardButton); } }
    @FXML private void handleShowPosts() { if (showBackOffice()) { showBackView(postsView, postsButton); } }
    @FXML private void handleShowAnnouncements() { if (showBackOffice()) { showBackView(announcementsView, announcementsButton); } }
    @FXML private void handleShowComments() { if (showBackOffice()) { showBackView(commentsView, commentsButton); } }
    @FXML private void handleShowAi() { if (showBackOffice()) { showBackView(aiView, aiButton); } }
    @FXML private void handleShowFrontOffice() { showFrontOffice(); showFrontView(frontFeedView, frontFeedButton); }
    @FXML private void handleShowBackOffice() { if (showBackOffice()) { showBackView(postsView, postsButton); } }
    @FXML private void handleShowFrontFeed() { showFrontOffice(); showFrontView(frontFeedView, frontFeedButton); }
    @FXML private void handleBackFromPostDetail() { showFrontOffice(); showFrontView(frontFeedView, frontFeedButton); }
    @FXML private void handleShowFrontStreaming() { showFrontOffice(); showFrontView(frontStreamingView, frontTournoisButton); refreshStreamingHub(); }
    @FXML private void handleRefreshStreaming() { refreshStreamingHub(); }
    @FXML private void handleShowFrontSaved() { showFrontOffice(); showFrontView(frontSavedView, frontSavedButton); }
    @FXML private void handleShowFrontProfile() { showFrontOffice(); showFrontView(frontProfileView, frontProfileButton); }
    @FXML private void handleFrontEquipes() { showFrontOffice(); markFrontActive(frontEquipesButton); showInfo("Equipes"); }
    @FXML private void handleFrontBoutique() { showFrontOffice(); markFrontActive(frontBoutiqueButton); showInfo("Boutique"); }
    @FXML private void handleFrontCommandes() { showFrontOffice(); markFrontActive(frontCommandesButton); showInfo("Commandes"); }
    @FXML private void handleAdminEquipes() { if (showBackOffice()) { markAdminActive(adminEquipesButton); showInfo("Gestion Equipes"); } }
    @FXML private void handleAdminTournoi() { if (showBackOffice()) { markAdminActive(adminTournoiButton); showInfo("Gestion Tournoi"); } }
    @FXML private void handleAdminBoutique() { if (showBackOffice()) { markAdminActive(adminBoutiqueButton); showInfo("Gestion Boutique"); } }
    @FXML private void handleAdminComptes() { if (showBackOffice()) { markAdminActive(adminComptesButton); showInfo("Gestion Comptes"); } }
    @FXML private void handleFilterAll() { applyFrontFilter(FILTER_ALL, filterAllButton); }
    @FXML private void handleFilterTeams() { applyFrontFilter(FILTER_TEAMS, filterTeamsButton); }
    @FXML private void handleFilterPlayers() { applyFrontFilter(FILTER_PLAYERS, filterPlayersButton); }
    @FXML private void handleFilterTournaments() { applyFrontFilter(FILTER_TOURNAMENTS, filterTournamentsButton); }
    @FXML private void handleFilterMedia() { applyFrontFilter(FILTER_MEDIA, filterMediaButton); }
    @FXML private void handleFilterAiRecommendations() { applyFrontFilter(FILTER_AI_RECOMMENDATIONS, null); }
    @FXML private void handleFrontPickMedia() {
        File file = chooseMediaFile("Choisir une image", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp");
        if (file != null && frontImageField != null) {
            String mediaPath = copyMediaToUploads(file);
            frontImageField.setText(mediaPath);
            if (frontVideoField != null) {
                frontVideoField.clear();
            }
            upsertMediaInFrontContent(mediaPath);
            updateFrontMediaPreview();
            showSuccess("Image ajoutee au compositeur.");
        }
    }
    @FXML private void handleFrontClearImage() {
        if (frontImageField != null) {
            frontImageField.clear();
        }
        if (frontVideoField == null || trimToNull(frontVideoField.getText()) == null) {
            upsertMediaInFrontContent(null);
        }
        updateFrontMediaPreview();
    }
    @FXML private void handleFrontToggleVideo() {
        File file = chooseMediaFile("Choisir une video", "*.mp4", "*.mov", "*.m4v", "*.avi", "*.mkv", "*.webm");
        if (file != null && frontVideoField != null) {
            String mediaPath = copyMediaToUploads(file);
            frontVideoField.setText(mediaPath);
            if (frontImageField != null) {
                frontImageField.clear();
            }
            upsertMediaInFrontContent(mediaPath);
            updateFrontMediaPreview();
            showSuccess("Video ajoutee au compositeur.");
        }
    }
    @FXML private void handleFrontClearVideo() {
        if (frontVideoField != null) {
            frontVideoField.clear();
        }
        if (frontImageField == null || trimToNull(frontImageField.getText()) == null) {
            upsertMediaInFrontContent(null);
        }
        updateFrontMediaPreview();
    }
    @FXML private void handleToggleAdminMenu() { adminCollapsed = !adminCollapsed; animateSidebar(adminSidebar, adminCollapsed); }
    @FXML private void handleToggleFrontMenu() { frontCollapsed = !frontCollapsed; animateSidebar(frontSidebar, frontCollapsed); }
    @FXML private void handleToggleAdminFilGroup() {
        if (!showBackOffice()) {
            return;
        }
        setAdminFilSubmenuVisible(true);
        showBackView(dashboardView, adminFilGroupButton);
    }
    @FXML
    private void handleLogout() {
        activeConversation = null;
        savedPostIds.clear();
        likedPostIds.clear();
        appNotifications.clear();
        readNotificationKeys.clear();
        updateNotificationsBadge();
        closeNotificationsPopup();
        notificationsOpen = false;
        currentUser = userDirectoryService.resolveCurrentUser();
        refreshAll();
        showFrontOffice();
        showFrontView(frontFeedView, frontFeedButton);
        showInfo("Authentification non integree: session utilisateur appliquee automatiquement.");
    }
    @FXML private void handleAnnouncementAdd() { handleAnnouncementCreate(); }
    @FXML private void handleAnnouncementUpdate() { handleAnnouncementEdit(); }
    @FXML private void handleAnnouncementDelete() { handleAnnouncementRemove(); }
    @FXML private void handleCommentAdd() { handleCommentCreate(); }
    @FXML private void handleCommentUpdate() { handleCommentEdit(); }
    @FXML private void handleCommentDelete() { handleCommentRemove(); }

    @FXML
    private void handleAdd() {
        try {
            FilActualite post = buildBackForm();
            service.addEntity(post);
            refreshAll();
            selectById(post.getId());
            clearBackForm();
            showSuccess("Publication ajoutee avec succes.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        if (selectedPost == null) {
            showError("Selectionnez une publication a modifier.");
            return;
        }
        try {
            FilActualite post = buildBackForm();
            service.updateEntity(selectedPost.getId(), post);
            refreshAll();
            selectById(selectedPost.getId());
            showSuccess("Publication modifiee avec succes.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedPost == null) {
            showError("Selectionnez une publication a supprimer.");
            return;
        }
        try {
            service.deleteEntity(selectedPost);
            refreshAll();
            clearBackForm();
            showSuccess("Publication supprimee avec succes.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void handleRefresh() {
        refreshAll();
        showSuccess("Les donnees ont ete actualisees.");
    }

    @FXML
    private void handleSearch() {
        try {
            String keyword = searchField.getText() == null ? "" : searchField.getText().trim();
            posts.setAll(service.search(keyword));
            postsTable.setItems(posts);
            updateDashboard();
            applyFrontFilter(currentFilter, resolveFilterButton(currentFilter));
            showSuccess("Recherche appliquee sur le backoffice.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void handleFrontPublish() {
        if (frontPublishInFlight) {
            return;
        }
        try {
            FilActualite post = buildFrontForm();
            setFrontPublishState(true, "Publication en cours...");
            PauseTransition pauseTransition = new PauseTransition(FRONT_PUBLISH_DELAY);
            pauseTransition.setOnFinished(event -> {
                try {
                    service.addEntity(post);
                    highlightedFrontPostId = post.getId();
                    refreshAll();
                    clearFrontComposer();
                    showFrontOffice();
                    showFrontView(frontFeedView, frontFeedButton);
                    applyFrontFilter(currentFilter, resolveFilterButton(currentFilter));
                    setFrontComposerMessage("Publication partagee dans le fil d'actualite.", false);
                    showSuccess("Publication partagee dans le fil d'actualite.");
                } catch (RuntimeException ex) {
                    setFrontComposerMessage(ex.getMessage(), true);
                    showError(ex.getMessage());
                } finally {
                    setFrontPublishState(false, null);
                }
            });
            pauseTransition.play();
        } catch (RuntimeException ex) {
            setFrontComposerMessage(ex.getMessage(), true);
            showError(ex.getMessage());
        }
    }


    private void refreshAll() {
        try {
            resolveUserContext();
            posts.setAll(service.getData());
            if (postsTable != null) {
                postsTable.setItems(posts);
            }
            announcements.setAll(announcementService.getData());
            if (announcementsTable != null) {
                announcementsTable.setItems(announcements);
            }
            commentaires.setAll(commentaireService.getData());
            if (commentsTable != null) {
                commentsTable.setItems(commentaires);
            }
            rebuildCommentsIndex();
            refreshSocialState();
            updateDashboard();
            updateProfilePanel();
            applyFrontFilter(currentFilter, resolveFilterButton(currentFilter));
            frontAnnouncementsItems.setAll(announcements);
            renderFrontAnnouncements();
            refreshBackofficeDashboard();
            refreshPostManagementModule();
            refreshAnnouncementManagementModule();
            refreshCommentManagementModule();
            refreshMessengerRealtime();
            refreshStreamingHub();
            rebuildNotifications();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateDashboard() {
        refreshBackofficeDashboard();
    }

    private void updateProfilePanel() {
        long savedCount = savedPostIds.size();
        long likedCount = likedPostIds.size();
        long totalComments = commentaires.size();
        String dominantType = posts.stream()
                .map(this::toFrontCategory)
                .filter(value -> !"General".equals(value))
                .max(Comparator.comparingLong(category -> posts.stream().filter(post -> category.equals(toFrontCategory(post))).count()))
                .orElse("General");
        profileItems.setAll(
                "Compte actif: " + currentUserDisplayName(),
                "Publications visibles: " + posts.size(),
                "Evenements actifs: " + posts.stream().filter(FilActualite::isEvent).count(),
                "Posts avec media: " + posts.stream().filter(post -> resolveEmbeddedMediaUrl(post) != null).count(),
                "Commentaires publics: " + totalComments,
                "J'aime laisses: " + likedCount,
                "Publications sauvegardees: " + savedCount,
                "Categorie dominante: " + dominantType,
                "Derniere activite: " + (posts.isEmpty() ? "Aucune" : formatFrontDate(posts.get(0).getCreatedAt()))
        );
    }

    private void configureStaticPanels() {
        renderSavedPosts();
    }

    private void resolveUserContext() {
        usersById = new HashMap<>(userDirectoryService.getUsersById());
        if (currentUser == null) {
            currentUser = userDirectoryService.resolveCurrentUser();
        }
        if (currentUser != null) {
            UserProfile refreshed = usersById.get(currentUser.getId());
            if (refreshed != null) {
                currentUser = refreshed;
            } else {
                usersById.putIfAbsent(currentUser.getId(), currentUser);
            }
        }
        if (frontComposerUserLabel != null) {
            frontComposerUserLabel.setText(currentUserDisplayName());
        }
        if (frontComposerAvatarLabel != null) {
            frontComposerAvatarLabel.setText(currentUser == null ? "?" : currentUser.getAvatarLabel());
        }
        if (frontAuthorIdField != null) {
            frontAuthorIdField.setText(currentUser == null ? "" : String.valueOf(currentUser.getId()));
        }
        updateRoleBasedAccess();
    }

    private void updateRoleBasedAccess() {
        boolean admin = isCurrentUserAdmin();
        if (frontAdminButton != null) {
            frontAdminButton.setVisible(admin);
            frontAdminButton.setManaged(admin);
        }
        if (frontAdminBadgeLabel != null) {
            frontAdminBadgeLabel.setVisible(admin);
            frontAdminBadgeLabel.setManaged(admin);
        }
    }

    private boolean isCurrentUserAdmin() {
        return isAdmin(currentUser);
    }

    private boolean isAdmin(UserProfile user) {
        if (user == null || user.getRole() == null) {
            return false;
        }
        String normalizedRole = user.getRole().trim().toLowerCase(Locale.ROOT);
        return normalizedRole.equals("admin") || normalizedRole.contains("admin");
    }

    private void refreshSocialState() {
        if (currentUser == null) {
            return;
        }
        savedPostIds.clear();
        savedPostIds.addAll(socialInteractionService.getSavedPostIds(currentUser.getId()));
        likedPostIds.clear();
        likedPostIds.addAll(socialInteractionService.getLikedPostIds(currentUser.getId()));
        likeCountsByPost.clear();
        likeCountsByPost.putAll(socialInteractionService.getLikeCounts());
        shareCountsByPost.clear();
        shareCountsByPost.putAll(socialInteractionService.getShareCounts());
    }

    private int resolveCurrentUserId() {
        if (currentUser == null) {
            resolveUserContext();
        }
        return currentUser == null ? userDirectoryService.resolveCurrentUser().getId() : currentUser.getId();
    }

    private String currentUserDisplayName() {
        return currentUser == null ? "Vous" : currentUser.getDisplayName();
    }

    private String resolveAuthorName(Integer authorId) {
        if (authorId == null) {
            return "System";
        }
        UserProfile user = usersById.get(authorId);
        return user == null ? "Auteur #" + authorId : user.getDisplayName();
    }

    private void configureTable() {
        if (postsTable == null) {
            return;
        }
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));
        contentColumn.setCellValueFactory(data -> new SimpleStringProperty(safeSummary(data.getValue())));
        mediaColumn.setCellValueFactory(data -> new SimpleStringProperty(buildMediaSummary(data.getValue())));
        eventColumn.setCellValueFactory(data -> new SimpleStringProperty(buildEventSummary(data.getValue())));
        authorColumn.setCellValueFactory(data -> new SimpleStringProperty(resolveAuthorName(data.getValue().getAuthorId())));
        createdAtColumn.setCellValueFactory(data -> new SimpleStringProperty(formatTableDate(data.getValue().getCreatedAt())));

        postsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedPost = newValue;
            if (newValue != null) {
                fillBackForm(newValue);
            }
        });
    }

    private void configureAnnouncementTable() {
        if (announcementsTable == null) {
            return;
        }
        announcementIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));
        announcementTagColumn.setCellValueFactory(data -> new SimpleStringProperty(defaultText(data.getValue().getTag())));
        announcementTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDisplayTitle()));
        announcementLinkColumn.setCellValueFactory(data -> new SimpleStringProperty(defaultText(data.getValue().getLink())));
        announcementCreatedAtColumn.setCellValueFactory(data -> new SimpleStringProperty(formatTableDate(data.getValue().getCreatedAt())));

        announcementsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedAnnouncement = newValue;
            if (newValue != null) {
                fillAnnouncementForm(newValue);
            }
        });
    }

    private void configureCommentTable() {
        if (commentsTable == null) {
            return;
        }
        commentIdColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));
        commentAuthorColumn.setCellValueFactory(data -> new SimpleStringProperty(resolveAuthorName(data.getValue().getAuthorId())));
        commentPostColumn.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getPostId()));
        commentContentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDisplayContent()));
        commentCreatedAtColumn.setCellValueFactory(data -> new SimpleStringProperty(formatTableDate(data.getValue().getCreatedAt())));

        commentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            selectedComment = newValue;
            if (newValue != null) {
                fillCommentForm(newValue);
            }
        });
    }

    private void configureFeedLists() {
        frontProfileList.setItems(profileItems);
    }

    private void configureFrontSearch() {
        if (frontSearchField == null) {
            return;
        }
        frontSearchPause.setOnFinished(event -> applyFrontFilter(currentFilter, resolveFilterButton(currentFilter)));
        frontSearchField.textProperty().addListener((obs, oldValue, newValue) -> {
            frontSearchPause.stop();
            frontSearchPause.playFromStart();
        });
    }

    private void configureFrontComposer() {
        if (frontContentArea == null) {
            return;
        }
        frontContentArea.textProperty().addListener((obs, oldValue, newValue) -> {
            String value = newValue == null ? "" : newValue;
            if (value.length() > FRONT_CONTENT_MAX_LENGTH) {
                String trimmed = value.substring(0, FRONT_CONTENT_MAX_LENGTH);
                frontContentArea.setText(trimmed);
                frontContentArea.positionCaret(trimmed.length());
                updateFrontComposerMeta(trimmed.length(), true);
                setFrontComposerMessage("Limite de caracteres atteinte.", true);
                return;
            }
            boolean warning = value.length() >= FRONT_CONTENT_MAX_LENGTH * 0.9;
            updateFrontComposerMeta(value.length(), warning);
            if (!frontPublishInFlight) {
                setFrontComposerMessage(warning
                        ? "Vous approchez de la limite autorisee."
                        : "Pret a publier", false);
            }
        });
        if (frontImageField != null) {
            frontImageField.textProperty().addListener((obs, oldValue, newValue) -> updateFrontMediaPreview());
        }
        if (frontVideoField != null) {
            frontVideoField.textProperty().addListener((obs, oldValue, newValue) -> updateFrontMediaPreview());
        }
        if (frontAuthorIdField != null && currentUser != null) {
            frontAuthorIdField.setText(String.valueOf(currentUser.getId()));
            frontAuthorIdField.setDisable(true);
        }
        if (frontEventCheckBox != null) {
            frontEventCheckBox.selectedProperty().addListener((obs, oldValue, selected) -> setFrontEventFieldsVisible(selected));
            setFrontEventFieldsVisible(frontEventCheckBox.isSelected());
        } else {
            setFrontEventFieldsVisible(false);
        }
        updateFrontComposerMeta(0, false);
        setFrontComposerMessage("Pret a publier", false);
        updateFrontMediaPreview();
    }

    private void configureNotifications() {
        if (notificationsListView == null) {
            return;
        }
        notificationsListView.setItems(appNotifications);
        notificationsListView.setCellFactory(list -> new NotificationCell());
        notificationsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected == null) {
                return;
            }
            openNotification(selected);
            notificationsListView.getSelectionModel().clearSelection();
        });
        updateNotificationsBadge();
    }

    private void configureMessenger() {
        MessengerRealtimeBridge.subscribe(messengerRealtimeListener);
        messengerRefreshTimeline.getKeyFrames().setAll(new KeyFrame(MESSENGER_POLL_RATE, event -> refreshMessengerRealtime()));
        messengerRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        messengerRefreshTimeline.play();

        if (messengerConversationList != null) {
            messengerConversationList.setItems(messengerConversations);
            messengerConversationList.setCellFactory(list -> new ConversationPreviewCell());
            messengerConversationList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    openConversation(newValue, false);
                }
            });
        }
        if (messengerInboxList != null) {
            messengerInboxList.setItems(messengerConversations);
            messengerInboxList.setCellFactory(list -> new ConversationPreviewCell());
            messengerInboxList.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    openConversationFromInbox(newValue);
                }
            });
        }

        if (messengerContactPicker != null) {
            messengerContactPicker.setItems(messengerContacts);
            messengerContactPicker.setCellFactory(list -> new UserProfileCell());
            messengerContactPicker.setButtonCell(new UserProfileCell());
        }

        if (messengerSearchField != null) {
            messengerSearchField.textProperty().addListener((obs, oldValue, newValue) -> refreshMessengerRealtime());
        }

        if (messengerInputField != null) {
            messengerInputField.textProperty().addListener((obs, oldValue, newValue) -> handleMessengerTypingChanged(newValue));
            messengerInputField.setOnAction(event -> handleMessengerSend());
        }
        if (messengerBubbleInputField != null) {
            messengerBubbleInputField.textProperty().addListener((obs, oldValue, newValue) -> handleMessengerTypingChanged(newValue));
            messengerBubbleInputField.setOnAction(event -> handleMessengerSendFromBubble());
        }

        messengerTypingPause.setOnFinished(event -> {
            if (activeConversation != null) {
                messengerService.getPresenceService().setTyping(activeConversation.getConversationId(), resolveCurrentUserId(), false);
            }
        });

        refreshMessengerRealtime();
    }

    @FXML
    private void handleToggleNotifications() {
        notificationsOpen = !notificationsOpen;
        if (notificationsOpen) {
            openNotificationsPopup();
        } else {
            closeNotificationsPopup();
        }
    }

    @FXML
    private void handleMarkAllNotificationsRead() {
        for (AppNotification notification : appNotifications) {
            readNotificationKeys.add(notification.key());
        }
        updateNotificationsBadge();
        if (notificationsListView != null) {
            notificationsListView.refresh();
        }
    }

    @FXML
    private void handleToggleMessenger() {
        closeNotificationsPopup();
        notificationsOpen = false;
        messengerInboxOpen = !messengerInboxOpen;
        if (messengerInboxOpen) {
            openMessengerInbox();
        } else {
            closeMessengerInbox();
        }
    }

    @FXML
    private void handleCloseMessenger() {
        messengerOpen = false;
        closeMessengerWindow();
    }

    @FXML
    private void handleCloseMessengerBubble() {
        messengerBubbleOpen = false;
        if (messengerBubbleWindow != null) {
            messengerBubbleWindow.setVisible(false);
            messengerBubbleWindow.setManaged(false);
        }
    }

    @FXML
    private void handleOpenMessengerFull() {
        messengerInboxOpen = false;
        messengerBubbleOpen = false;
        closeMessengerInbox();
        handleCloseMessengerBubble();
        messengerOpen = true;
        openMessengerWindow();
        refreshMessengerRealtime();
    }

    private void configureStreaming() {
        streamingRefreshTimeline.getKeyFrames().setAll(new KeyFrame(Duration.seconds(45), event -> {
            if (frontStreamingView != null && frontStreamingView.isVisible() && frontStreamingView.isManaged()) {
                refreshStreamingHub();
            }
        }));
        streamingRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        streamingRefreshTimeline.play();
    }

    @FXML
    private void handleStartVoiceCall() {
        startMessengerCall(false);
    }

    @FXML
    private void handleStartVideoCall() {
        startMessengerCall(true);
    }

    @FXML
    private void handleMessengerSend() {
        if (activeConversation == null) {
            showError("Selectionnez une conversation.");
            return;
        }
        try {
            String payload = messengerInputField == null ? null : messengerInputField.getText();
            MessengerMessage sent = messengerService.sendMessage(
                    activeConversation.getConversationId(),
                    resolveCurrentUserId(),
                    payload,
                    null
            );
            messengerService.getPresenceService().setTyping(activeConversation.getConversationId(), resolveCurrentUserId(), false);
            if (messengerInputField != null) {
                messengerInputField.clear();
                messengerInputField.requestFocus();
            }
            if (messengerBubbleInputField != null) {
                messengerBubbleInputField.clear();
                messengerBubbleInputField.requestFocus();
            }
            appendMessengerMessage(sent);
            appendMessengerBubbleMessage(sent);
            refreshMessengerRealtime();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void handleMessengerSendFromBubble() {
        if (messengerBubbleInputField != null && messengerInputField != null) {
            messengerInputField.setText(messengerBubbleInputField.getText());
        }
        handleMessengerSend();
        if (messengerBubbleInputField != null) {
            messengerBubbleInputField.clear();
        }
    }

    @FXML
    private void handleMessengerEmoji() {
        if (messengerInputField != null) {
            messengerInputField.appendText(" :)");
            messengerInputField.requestFocus();
            messengerInputField.positionCaret(messengerInputField.getText().length());
        }
    }

    @FXML
    private void handleMessengerAttach() {
        if (activeConversation == null) {
            showError("Selectionnez une conversation.");
            return;
        }
        File file = chooseMediaFile("Choisir une piece jointe", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp", "*.mp4", "*.mov", "*.webm");
        if (file == null) {
            return;
        }
        try {
            messengerService.sendMessage(activeConversation.getConversationId(), resolveCurrentUserId(), null, copyMediaToUploads(file));
            refreshMessengerRealtime();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void handleStartConversation() {
        if (messengerContactPickerRow != null) {
            boolean show = !messengerContactPickerRow.isVisible();
            messengerContactPickerRow.setVisible(show);
            messengerContactPickerRow.setManaged(show);
            if (show && messengerContactPicker != null) {
                messengerContactPicker.requestFocus();
            }
        }
    }

    @FXML
    private void handleConfirmStartConversation() {
        if (messengerContactPicker == null || messengerContactPicker.getValue() == null) {
            showError("Choisissez un contact.");
            return;
        }
        try {
            int conversationId = messengerService.findOrCreateConversation(resolveCurrentUserId(), messengerContactPicker.getValue().getId());
            refreshMessengerRealtime();
            messengerConversations.stream()
                    .filter(preview -> preview.getConversationId() == conversationId)
                    .findFirst()
                    .ifPresent(preview -> {
                        if (messengerConversationList != null) {
                            messengerConversationList.getSelectionModel().select(preview);
                        }
                        openConversation(preview, true);
                    });
            messengerContactPicker.getSelectionModel().clearSelection();
            if (messengerContactPickerRow != null) {
                messengerContactPickerRow.setVisible(false);
                messengerContactPickerRow.setManaged(false);
            }
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    private void handleLoadOlderMessages() {
        if (activeConversation == null || oldestLoadedMessengerMessageId == null) {
            return;
        }
        try {
            List<MessengerMessage> older = messengerService.getMessages(
                    activeConversation.getConversationId(),
                    resolveCurrentUserId(),
                    MESSENGER_PAGE_SIZE,
                    oldestLoadedMessengerMessageId
            );
            prependMessengerMessages(older);
            oldestLoadedMessengerMessageId = older.isEmpty() ? oldestLoadedMessengerMessageId : older.get(0).getId();
            updateLoadOlderVisibility(older.size() >= MESSENGER_PAGE_SIZE);
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void openMessengerInbox() {
        if (messengerInboxPopup == null) {
            return;
        }
        if (messengerWindow != null) {
            messengerWindow.setVisible(false);
            messengerWindow.setManaged(false);
        }
        messengerInboxPopup.setVisible(true);
        messengerInboxPopup.setManaged(true);
        messengerInboxPopup.toFront();
        refreshMessengerRealtime();
    }

    private void closeMessengerInbox() {
        if (messengerInboxPopup == null) {
            return;
        }
        messengerInboxPopup.setVisible(false);
        messengerInboxPopup.setManaged(false);
    }

    private void openNotificationsPopup() {
        if (notificationsPopup == null) {
            return;
        }
        closeMessengerInbox();
        messengerInboxOpen = false;
        notificationsPopup.setVisible(true);
        notificationsPopup.setManaged(true);
        notificationsPopup.toFront();
        if (notificationsListView != null && !appNotifications.isEmpty()) {
            notificationsListView.scrollTo(0);
        }
    }

    private void closeNotificationsPopup() {
        if (notificationsPopup == null) {
            return;
        }
        notificationsPopup.setVisible(false);
        notificationsPopup.setManaged(false);
    }

    private void rebuildNotifications() {
        List<AppNotification> next = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (FilActualite post : posts) {
            if (post == null || post.getId() <= 0) {
                continue;
            }
            if (post.getAuthorId() != null && currentUser != null && post.getAuthorId().intValue() == currentUser.getId()) {
                continue;
            }
            LocalDateTime createdAt = post.getCreatedAt() == null ? now : post.getCreatedAt();
            String summary = safeSummary(post);
            next.add(new AppNotification(
                    "post:" + post.getId(),
                    "Nouvelle publication",
                    resolveAuthorName(post.getAuthorId()) + " a publie: " + summary,
                    createdAt,
                    NotificationTarget.POST,
                    post.getId()
            ));
            if (post.isEvent() && post.getEventDate() != null && post.getEventDate().isAfter(now.minusHours(1))) {
                next.add(new AppNotification(
                        "event:" + post.getId(),
                        "Evenement annonce",
                        defaultText(post.getEventTitle()) + " commence le " + formatFrontDate(post.getEventDate()),
                        createdAt,
                        NotificationTarget.POST,
                        post.getId()
                ));
            }
        }

        for (Commentaire comment : commentaires) {
            if (comment == null || comment.getId() <= 0 || comment.getPostId() <= 0) {
                continue;
            }
            FilActualite post = findPostById(comment.getPostId());
            if (post == null || post.getAuthorId() == null || currentUser == null) {
                continue;
            }
            if (post.getAuthorId().intValue() != currentUser.getId()) {
                continue;
            }
            if (comment.getAuthorId() == currentUser.getId()) {
                continue;
            }
            LocalDateTime createdAt = comment.getCreatedAt() == null ? now : comment.getCreatedAt();
            next.add(new AppNotification(
                    "comment:" + comment.getId(),
                    "Nouveau commentaire",
                    resolveAuthorName(comment.getAuthorId()) + " a commente votre publication.",
                    createdAt,
                    NotificationTarget.COMMENT,
                    comment.getPostId()
            ));
        }

        for (Announcement announcement : announcements) {
            if (announcement == null || announcement.getId() <= 0) {
                continue;
            }
            LocalDateTime createdAt = announcement.getCreatedAt() == null ? now : announcement.getCreatedAt();
            next.add(new AppNotification(
                    "announcement:" + announcement.getId(),
                    "Nouvelle annonce officielle",
                    announcement.getDisplayTitle(),
                    createdAt,
                    NotificationTarget.ANNOUNCEMENT,
                    announcement.getId()
            ));
        }

        int messengerUnread = messengerService.getUnreadCount(resolveCurrentUserId());
        if (messengerUnread > 0) {
            next.add(new AppNotification(
                    "messenger:unread",
                    "Messages non lus",
                    "Vous avez " + messengerUnread + " message(s) non lu(s).",
                    now,
                    NotificationTarget.MESSENGER,
                    0
            ));
        }

        if (streamingStatusLabel != null && trimToNull(streamingStatusLabel.getText()) != null) {
            next.add(new AppNotification(
                    "streaming:status",
                    "Streaming Hub",
                    streamingStatusLabel.getText(),
                    now.minusSeconds(30),
                    NotificationTarget.STREAMING,
                    0
            ));
        }

        next.sort((left, right) -> {
            LocalDateTime l = left.createdAt() == null ? LocalDateTime.MIN : left.createdAt();
            LocalDateTime r = right.createdAt() == null ? LocalDateTime.MIN : right.createdAt();
            return r.compareTo(l);
        });
        appNotifications.setAll(next.stream().limit(80).toList());
        updateNotificationsBadge();
    }

    private void updateNotificationsBadge() {
        if (topbarNotificationsBadgeLabel == null) {
            return;
        }
        int unread = 0;
        for (AppNotification notification : appNotifications) {
            if (!readNotificationKeys.contains(notification.key())) {
                unread++;
            }
        }
        String badgeText = unread > 99 ? "99+" : String.valueOf(unread);
        topbarNotificationsBadgeLabel.setText(badgeText);
        topbarNotificationsBadgeLabel.setVisible(unread > 0);
        topbarNotificationsBadgeLabel.setManaged(unread > 0);
    }

    private void openNotification(AppNotification notification) {
        if (notification == null) {
            return;
        }
        readNotificationKeys.add(notification.key());
        updateNotificationsBadge();
        if (notificationsListView != null) {
            notificationsListView.refresh();
        }
        closeNotificationsPopup();
        notificationsOpen = false;

        switch (notification.target()) {
            case POST, COMMENT -> {
                FilActualite post = findPostById(notification.targetId());
                if (post != null) {
                    openPostDetail(post);
                } else {
                    showFrontOffice();
                    showFrontView(frontFeedView, frontFeedButton);
                    showInfo("Publication");
                }
            }
            case ANNOUNCEMENT -> {
                showFrontOffice();
                showFrontView(frontFeedView, frontFeedButton);
                showSuccess("Annonce: " + notification.title());
            }
            case MESSENGER -> handleOpenMessengerFull();
            case STREAMING -> {
                showFrontOffice();
                showFrontView(frontStreamingView, frontTournoisButton);
                refreshStreamingHub();
            }
        }
    }

    private void openMessengerWindow() {
        if (messengerWindow == null) {
            return;
        }
        closeMessengerInbox();
        messengerWindow.setVisible(true);
        messengerWindow.setManaged(true);
        messengerWindow.setOpacity(0);
        messengerWindow.setTranslateY(24);
        messengerWindow.toFront();
        FadeTransition fade = new FadeTransition(Duration.millis(240), messengerWindow);
        fade.setFromValue(0);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(240), messengerWindow);
        slide.setFromY(24);
        slide.setToY(0);
        fade.play();
        slide.play();
        refreshMessengerRealtime();
        if (messengerInputField != null) {
            messengerInputField.requestFocus();
        }
    }

    private void closeMessengerWindow() {
        if (messengerWindow == null) {
            return;
        }
        messengerWindow.setVisible(false);
        messengerWindow.setManaged(false);
    }

    private void openConversationFromInbox(ConversationPreview preview) {
        closeMessengerInbox();
        messengerInboxOpen = false;
        messengerBubbleOpen = true;
        openConversation(preview, true);
        if (messengerBubbleWindow != null) {
            messengerBubbleWindow.setVisible(true);
            messengerBubbleWindow.setManaged(true);
            messengerBubbleWindow.toFront();
        }
    }

    private void refreshMessengerRealtime() {
        if (currentUser == null) {
            return;
        }
        if (messengerRefreshInProgress) {
            return;
        }
        messengerRefreshInProgress = true;
        try {
            messengerService.getPresenceService().heartbeat(resolveCurrentUserId());
            messengerContacts.setAll(messengerService.getAvailableContacts(resolveCurrentUserId()));

            List<ConversationPreview> filtered = messengerService.getConversationsForUser(resolveCurrentUserId()).stream()
                    .filter(this::matchesMessengerSearch)
                    .toList();
            messengerConversations.setAll(filtered);
            int unread = messengerService.getUnreadCount(resolveCurrentUserId());
            updateMessengerBadge(unread);
            if (unread > lastMessengerUnreadCount && lastMessengerUnreadCount >= 0) {
                Toolkit.getDefaultToolkit().beep();
            }
            lastMessengerUnreadCount = unread;
            rebuildNotifications();

            if (activeConversation == null && !messengerConversations.isEmpty()) {
                openConversation(messengerConversations.get(0), false);
                if (messengerConversationList != null) {
                    messengerConversationList.getSelectionModel().select(0);
                }
            } else if (activeConversation != null) {
                messengerConversations.stream()
                        .filter(preview -> preview.getConversationId() == activeConversation.getConversationId())
                        .findFirst()
                        .ifPresent(preview -> {
                            activeConversation = preview;
                            syncActiveConversationChrome(preview);
                            openConversation(preview, false);
                        });
            }
            refreshTypingIndicator();
        } finally {
            messengerRefreshInProgress = false;
        }
    }

    private boolean matchesMessengerSearch(ConversationPreview preview) {
        String query = messengerSearchField == null ? null : trimToNull(messengerSearchField.getText());
        if (query == null) {
            return true;
        }
        String normalized = query.toLowerCase(Locale.ROOT);
        return preview.getPeerDisplayName().toLowerCase(Locale.ROOT).contains(normalized)
                || (preview.getLastMessage() != null && preview.getLastMessage().toLowerCase(Locale.ROOT).contains(normalized));
    }

    private void openConversation(ConversationPreview preview, boolean focusInput) {
        activeConversation = preview;
        syncActiveConversationChrome(preview);

        List<MessengerMessage> messages = messengerService.getMessages(preview.getConversationId(), resolveCurrentUserId(), MESSENGER_PAGE_SIZE, null);
        renderMessengerMessages(messages);
        renderMessengerBubbleMessages(messages);
        oldestLoadedMessengerMessageId = messages.isEmpty() ? null : messages.get(0).getId();
        updateLoadOlderVisibility(messages.size() >= MESSENGER_PAGE_SIZE);
        refreshTypingIndicator();
        updateMessengerBadge(messengerService.getUnreadCount(resolveCurrentUserId()));
        if (focusInput && messengerInputField != null) {
            messengerInputField.requestFocus();
        }
        if (focusInput && messengerBubbleInputField != null) {
            messengerBubbleInputField.requestFocus();
        }
    }

    private void syncActiveConversationChrome(ConversationPreview preview) {
        if (preview == null) {
            return;
        }
        if (messengerHeaderAvatarLabel != null) {
            messengerHeaderAvatarLabel.setText(preview.getPeerAvatarLabel());
        }
        if (messengerHeaderSubtitleLabel != null) {
            messengerHeaderSubtitleLabel.setText(preview.getUnreadCount() > 0 ? preview.getUnreadCount() + " non lus" : "Canal securise");
        }
        if (messengerPeerAvatarLabel != null) {
            messengerPeerAvatarLabel.setText(preview.getPeerAvatarLabel());
        }
        if (messengerPeerNameLabel != null) {
            messengerPeerNameLabel.setText(preview.getPeerDisplayName());
        }
        if (messengerPresenceLabel != null) {
            messengerPresenceLabel.setText(preview.isPeerOnline() ? "En ligne" : "Hors ligne");
        }
        if (messengerBubbleAvatarLabel != null) {
            messengerBubbleAvatarLabel.setText(preview.getPeerAvatarLabel());
        }
        if (messengerBubbleNameLabel != null) {
            messengerBubbleNameLabel.setText(preview.getPeerDisplayName());
        }
        if (messengerBubbleStatusLabel != null) {
            messengerBubbleStatusLabel.setText(preview.isPeerOnline() ? "En ligne" : "Hors ligne");
        }
        if (messengerPresenceDot != null) {
            messengerPresenceDot.getStyleClass().removeAll("online", "offline");
            messengerPresenceDot.getStyleClass().add(preview.isPeerOnline() ? "online" : "offline");
        }
    }

    private void renderMessengerMessages(List<MessengerMessage> messages) {
        if (messengerMessageBox == null) {
            return;
        }
        messengerMessageBox.getChildren().clear();
        for (MessengerMessage message : messages) {
            messengerMessageBox.getChildren().add(buildMessengerMessageBubble(message));
        }
        scrollMessengerToBottom();
    }

    private void prependMessengerMessages(List<MessengerMessage> older) {
        if (messengerMessageBox == null || older.isEmpty()) {
            return;
        }
        List<Node> nodes = new ArrayList<>();
        for (MessengerMessage message : older) {
            nodes.add(buildMessengerMessageBubble(message));
        }
        messengerMessageBox.getChildren().addAll(0, nodes);
        if (messengerBubbleMessageBox != null) {
            List<Node> bubbleNodes = new ArrayList<>();
            for (MessengerMessage message : older) {
                bubbleNodes.add(buildMessengerMessageBubble(message));
            }
            messengerBubbleMessageBox.getChildren().addAll(0, bubbleNodes);
        }
    }

    private void appendMessengerMessage(MessengerMessage message) {
        if (messengerMessageBox == null || message == null) {
            return;
        }
        messengerMessageBox.getChildren().add(buildMessengerMessageBubble(message));
        scrollMessengerToBottom();
    }

    private void renderMessengerBubbleMessages(List<MessengerMessage> messages) {
        if (messengerBubbleMessageBox == null) {
            return;
        }
        messengerBubbleMessageBox.getChildren().clear();
        for (MessengerMessage message : messages) {
            messengerBubbleMessageBox.getChildren().add(buildMessengerMessageBubble(message));
        }
        scrollMessengerBubbleToBottom();
    }

    private void appendMessengerBubbleMessage(MessengerMessage message) {
        if (messengerBubbleMessageBox == null || message == null) {
            return;
        }
        messengerBubbleMessageBox.getChildren().add(buildMessengerMessageBubble(message));
        scrollMessengerBubbleToBottom();
    }

    private Node buildMessengerMessageBubble(MessengerMessage message) {
        boolean mine = message.getSenderId() == resolveCurrentUserId();
        VBox wrap = new VBox(4);
        wrap.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        wrap.getStyleClass().add("messenger-message-wrap");

        String callInviteUrl = parseCallInviteUrl(message.getContent());
        String callType = parseCallInviteType(message.getContent());
        String bubbleText = callInviteUrl == null
                ? (message.getContent() == null ? "Piece jointe" : message.getContent())
                : ("Invitation d'appel " + ("video".equals(callType) ? "video" : "vocal"));

        Label bubble = new Label(bubbleText);
        bubble.setWrapText(true);
        bubble.getStyleClass().addAll("messenger-bubble", mine ? "mine" : "theirs");

        if (callInviteUrl != null) {
            VBox bubbleBox = new VBox(6);
            bubbleBox.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            bubbleBox.getChildren().add(bubble);
            Button joinButton = new Button("Rejoindre l'appel");
            joinButton.getStyleClass().add("messenger-send-button");
            joinButton.setOnAction(event -> openExternalMedia(callInviteUrl));
            bubbleBox.getChildren().add(joinButton);
            wrap.getChildren().add(bubbleBox);
        } else if (message.getAttachmentPath() != null) {
            VBox bubbleBox = new VBox(6);
            bubbleBox.setAlignment(mine ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            bubbleBox.getChildren().add(bubble);
            ImageView attachmentPreview = buildPostImage(message.getAttachmentPath());
            if (attachmentPreview != null) {
                attachmentPreview.setFitWidth(180);
                attachmentPreview.setFitHeight(120);
                bubbleBox.getChildren().add(attachmentPreview);
            } else {
                Label attachment = new Label("Piece jointe");
                attachment.getStyleClass().add("messenger-attachment-label");
                bubbleBox.getChildren().add(attachment);
            }
            wrap.getChildren().add(bubbleBox);
        } else {
            wrap.getChildren().add(bubble);
        }

        Label meta = new Label(formatFrontDate(message.getCreatedAt()) + (mine && message.getSeenAt() != null ? "  â€¢  Vu" : ""));
        meta.getStyleClass().add("messenger-message-meta");
        wrap.getChildren().add(meta);
        return wrap;
    }

    private StackPane buildMessengerAvatarNode(String label, double size) {
        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("messenger-avatar");
        avatar.setMinSize(size, size);
        avatar.setPrefSize(size, size);
        avatar.setMaxSize(size, size);

        ImageView imageView = new ImageView(new Image(getClass().getResource("/images/logo5.png").toExternalForm(), true));
        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        Circle clip = new Circle(size / 2);
        clip.setCenterX(size / 2);
        clip.setCenterY(size / 2);
        imageView.setClip(clip);

        Label avatarLabel = new Label(label);
        avatarLabel.getStyleClass().add("messenger-avatar-text");
        avatar.getChildren().addAll(imageView, avatarLabel);
        return avatar;
    }

    private void refreshTypingIndicator() {
        if (messengerTypingLabel == null || activeConversation == null) {
            return;
        }
        boolean peerTyping = messengerService.getPresenceService().isTyping(activeConversation.getConversationId(), activeConversation.getPeerUserId());
        messengerTypingLabel.setText(activeConversation.getPeerDisplayName() + " est en train d'ecrire...");
        messengerTypingLabel.setVisible(peerTyping);
        messengerTypingLabel.setManaged(peerTyping);
        if (messengerBubbleTypingLabel != null) {
            messengerBubbleTypingLabel.setText(activeConversation.getPeerDisplayName() + " est en train d'ecrire...");
            messengerBubbleTypingLabel.setVisible(peerTyping);
            messengerBubbleTypingLabel.setManaged(peerTyping);
        }
    }

    private void handleMessengerTypingChanged(String value) {
        if (activeConversation == null) {
            return;
        }
        boolean typing = trimToNull(value) != null;
        messengerService.getPresenceService().setTyping(activeConversation.getConversationId(), resolveCurrentUserId(), typing);
        messengerTypingPause.stop();
        if (typing) {
            messengerTypingPause.playFromStart();
        }
    }

    private void updateMessengerBadge(int unread) {
        if (messengerUnreadBadgeLabel != null) {
            messengerUnreadBadgeLabel.setText(String.valueOf(unread));
            messengerUnreadBadgeLabel.setVisible(unread > 0);
            messengerUnreadBadgeLabel.setManaged(unread > 0);
        }
        if (topbarMessengerBadgeLabel != null) {
            topbarMessengerBadgeLabel.setText(String.valueOf(unread));
            topbarMessengerBadgeLabel.setVisible(unread > 0);
            topbarMessengerBadgeLabel.setManaged(unread > 0);
        }
    }

    private void updateLoadOlderVisibility(boolean visible) {
        if (messengerLoadOlderButton == null) {
            return;
        }
        messengerLoadOlderButton.setVisible(visible);
        messengerLoadOlderButton.setManaged(visible);
    }

    private void scrollMessengerToBottom() {
        if (messengerMessageScroll == null) {
            return;
        }
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(60), event -> messengerMessageScroll.setVvalue(1.0)));
        timeline.play();
    }

    private void scrollMessengerBubbleToBottom() {
        if (messengerBubbleScroll == null) {
            return;
        }
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(60), event -> messengerBubbleScroll.setVvalue(1.0)));
        timeline.play();
    }

    private FilActualite buildBackForm() {
        if (contentArea == null) {
            throw new IllegalStateException("L'editeur backoffice integre n'est plus actif dans cette vue.");
        }
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
        post.setCreatedAt(selectedPost == null ? LocalDateTime.now() : selectedPost.getCreatedAt());
        post.setMediaFilename(extractMediaFilename(post));
        return post;
    }

    private FilActualite buildFrontForm() {
        FilActualite post = new FilActualite();
        post.setContent(trimToNull(frontContentArea.getText()));
        post.setImagePath(trimToNull(frontImageField.getText()));
        post.setVideoUrl(trimToNull(frontVideoField.getText()));
        boolean eventMode = frontEventCheckBox != null && frontEventCheckBox.isSelected();
        post.setEvent(eventMode);
        post.setEventTitle(eventMode ? trimToNull(frontEventTitleField.getText()) : null);
        post.setEventDate(eventMode ? toDateTime(frontEventDateField == null ? null : frontEventDateField.getValue()) : null);
        post.setEventLocation(eventMode ? trimToNull(frontEventLocationField.getText()) : null);
        post.setMaxParticipants(eventMode ? parseInteger(frontMaxParticipantsField.getText()) : null);
        post.setAuthorId(resolveCurrentUserId());
        post.setCreatedAt(LocalDateTime.now());
        post.setMediaFilename(extractMediaFilename(post));
        return post;
    }

    private void fillBackForm(FilActualite post) {
        if (contentArea == null) {
            return;
        }
        contentArea.setText(defaultText(post.getContent()));
        imageField.setText(defaultText(post.getImagePath()));
        videoField.setText(defaultText(post.getVideoUrl()));
        eventCheckBox.setSelected(post.isEvent());
        eventTitleField.setText(defaultText(post.getEventTitle()));
        if (eventDateField != null) {
            eventDateField.setValue(post.getEventDate() == null ? null : post.getEventDate().toLocalDate());
        }
        eventLocationField.setText(defaultText(post.getEventLocation()));
        maxParticipantsField.setText(post.getMaxParticipants() == null ? "" : String.valueOf(post.getMaxParticipants()));
        authorIdField.setText(post.getAuthorId() == null ? "" : String.valueOf(post.getAuthorId()));
    }

    private void clearBackForm() {
        selectedPost = null;
        if (postsTable != null) {
            postsTable.getSelectionModel().clearSelection();
        }
        if (contentArea == null) {
            return;
        }
        contentArea.clear();
        imageField.clear();
        videoField.clear();
        eventCheckBox.setSelected(false);
        eventTitleField.clear();
        if (eventDateField != null) {
            eventDateField.setValue(null);
        }
        eventLocationField.clear();
        maxParticipantsField.clear();
        authorIdField.clear();
    }

    private Announcement buildAnnouncementForm() {
        if (announcementTitleField == null) {
            throw new IllegalStateException("L'editeur annonces integre n'est plus actif dans cette vue.");
        }
        Announcement announcement = new Announcement();
        announcement.setTitle(trimToNull(announcementTitleField.getText()));
        announcement.setTag(trimToNull(announcementTagField.getText()));
        announcement.setLink(trimToNull(announcementLinkField.getText()));
        announcement.setMediaType(trimToNull(announcementMediaTypeField.getText()));
        announcement.setMediaFilename(trimToNull(announcementMediaFilenameField.getText()));
        announcement.setContent(trimToNull(announcementContentArea.getText()));
        announcement.setCreatedAt(selectedAnnouncement == null ? LocalDateTime.now() : selectedAnnouncement.getCreatedAt());
        return announcement;
    }

    private void fillAnnouncementForm(Announcement announcement) {
        if (announcementTitleField == null) {
            return;
        }
        announcementTitleField.setText(defaultText(announcement.getTitle()));
        announcementTagField.setText(defaultText(announcement.getTag()));
        announcementLinkField.setText(defaultText(announcement.getLink()));
        announcementMediaTypeField.setText(defaultText(announcement.getMediaType()));
        announcementMediaFilenameField.setText(defaultText(announcement.getMediaFilename()));
        announcementContentArea.setText(defaultText(announcement.getContent()));
    }

    private void clearAnnouncementForm() {
        selectedAnnouncement = null;
        if (announcementsTable != null) {
            announcementsTable.getSelectionModel().clearSelection();
        }
        if (announcementTitleField == null) {
            return;
        }
        announcementTitleField.clear();
        announcementTagField.clear();
        announcementLinkField.clear();
        announcementMediaTypeField.clear();
        announcementMediaFilenameField.clear();
        announcementContentArea.clear();
    }

    private Commentaire buildCommentForm() {
        if (commentContentArea == null) {
            throw new IllegalStateException("L'editeur commentaires integre n'est plus actif dans cette vue.");
        }
        Commentaire comment = new Commentaire();
        comment.setContent(trimToNull(commentContentArea.getText()));
        comment.setAuthorId(parseRequiredInteger(commentAuthorIdField.getText(), "L'identifiant auteur est obligatoire."));
        comment.setPostId(parseRequiredInteger(commentPostIdField.getText(), "L'identifiant de la publication est obligatoire."));
        comment.setCreatedAt(selectedComment == null ? LocalDateTime.now() : selectedComment.getCreatedAt());
        return comment;
    }


    private void fillCommentForm(Commentaire comment) {
        if (commentContentArea == null) {
            return;
        }
        commentContentArea.setText(defaultText(comment.getContent()));
        commentAuthorIdField.setText(String.valueOf(comment.getAuthorId()));
        commentPostIdField.setText(String.valueOf(comment.getPostId()));
    }

    private void clearCommentForm() {
        selectedComment = null;
        if (commentsTable != null) {
            commentsTable.getSelectionModel().clearSelection();
        }
        if (commentContentArea == null) {
            return;
        }
        commentContentArea.clear();
        commentAuthorIdField.clear();
        commentPostIdField.clear();
    }

    private void clearFrontComposer() {
        frontContentArea.clear();
        frontImageField.clear();
        frontVideoField.clear();
        frontEventCheckBox.setSelected(false);
        frontEventTitleField.clear();
        if (frontEventDateField != null) {
            frontEventDateField.setValue(null);
        }
        frontEventLocationField.clear();
        frontMaxParticipantsField.clear();
        if (frontAuthorIdField != null && currentUser != null) {
            frontAuthorIdField.setText(String.valueOf(currentUser.getId()));
        }
        setFrontEventFieldsVisible(false);
        updateFrontComposerMeta(0, false);
        setFrontComposerMessage("Pret a publier", false);
        updateFrontMediaPreview();
    }

    private void setFrontEventFieldsVisible(boolean visible) {
        if (frontEventFieldsRow != null) {
            frontEventFieldsRow.setVisible(visible);
            frontEventFieldsRow.setManaged(visible);
        }
        if (frontEventTitleField != null) {
            frontEventTitleField.setDisable(!visible);
        }
        if (frontEventDateField != null) {
            frontEventDateField.setDisable(!visible);
        }
        if (frontEventLocationField != null) {
            frontEventLocationField.setDisable(!visible);
        }
        if (frontMaxParticipantsField != null) {
            frontMaxParticipantsField.setDisable(!visible);
        }
    }

    private void selectById(int id) {
        if (postsTable == null) {
            return;
        }
        for (FilActualite post : posts) {
            if (post.getId() == id) {
                postsTable.getSelectionModel().select(post);
                postsTable.scrollTo(post);
                selectedPost = post;
                return;
            }
        }
    }

    private void selectAnnouncementById(int id) {
        if (announcementsTable == null) {
            return;
        }
        for (Announcement announcement : announcements) {
            if (announcement.getId() == id) {
                announcementsTable.getSelectionModel().select(announcement);
                announcementsTable.scrollTo(announcement);
                selectedAnnouncement = announcement;
                return;
            }
        }
    }

    private void selectCommentById(int id) {
        if (commentsTable == null) {
            return;
        }
        for (Commentaire comment : commentaires) {
            if (comment.getId() == id) {
                commentsTable.getSelectionModel().select(comment);
                commentsTable.scrollTo(comment);
                selectedComment = comment;
                return;
            }
        }
    }

    private boolean showBackOffice() {
        if (!isCurrentUserAdmin()) {
            showError("Seul un compte admin peut acceder au backoffice.");
            showFrontOffice();
            return false;
        }
        backOfficeShell.setVisible(true);
        backOfficeShell.setManaged(true);
        frontOfficeShell.setVisible(false);
        frontOfficeShell.setManaged(false);
        markActive(switchToBackButton, switchToBackButton, switchToFrontButton);
        markAdminActive(null);
        return true;
    }

    private void showFrontOffice() {
        frontOfficeShell.setVisible(true);
        frontOfficeShell.setManaged(true);
        backOfficeShell.setVisible(false);
        backOfficeShell.setManaged(false);
        markActive(switchToFrontButton, switchToBackButton, switchToFrontButton);
        markFrontActive(frontFeedButton);
    }

    private void showBackView(VBox targetView, Button activeButton) {
        for (Node child : backContentStack.getChildren()) {
            toggleManaged(child, child == targetView);
        }
        markActive(activeButton, adminFilGroupButton, dashboardButton, postsButton, announcementsButton, commentsButton, aiButton);
        if (targetView == dashboardView && backofficeDashboardController != null) {
            backofficeDashboardController.onDashboardShown();
        }
        if (targetView == postsView && postManagementModuleController != null) {
            postManagementModuleController.onViewShown();
        }
        if (targetView == announcementsView && announcementManagementModuleController != null) {
            announcementManagementModuleController.onViewShown();
        }
        if (targetView == commentsView && commentManagementModuleController != null) {
            commentManagementModuleController.onViewShown();
        }
    }

    private void showFrontView(VBox targetView, Button activeButton) {
        for (Node child : frontContentStack.getChildren()) {
            toggleManaged(child, child == targetView);
        }
        markActive(activeButton, frontFeedButton, frontSavedButton, frontProfileButton,
                frontEquipesButton, frontTournoisButton, frontBoutiqueButton, frontCommandesButton);
    }

    private void markFrontActive(Button activeButton) {
        markActive(activeButton, frontFeedButton, frontSavedButton, frontProfileButton,
                frontEquipesButton, frontTournoisButton, frontBoutiqueButton, frontCommandesButton);
    }

    private void markAdminActive(Button activeButton) {
        markActive(activeButton, adminFilGroupButton, postsButton, announcementsButton, commentsButton, aiButton,
                adminEquipesButton, adminTournoiButton, adminBoutiqueButton, adminComptesButton);
    }

    private void animateSidebar(VBox sidebar, boolean collapsed) {
        if (sidebar == null) {
            return;
        }
        double from = collapsed ? 320 : 80;
        double to = collapsed ? 80 : 320;
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(sidebar.prefWidthProperty(), from)),
                new KeyFrame(Duration.millis(220), new KeyValue(sidebar.prefWidthProperty(), to))
        );
        timeline.setOnFinished(e -> applySidebarState(sidebar, collapsed));
        timeline.play();
        applySidebarState(sidebar, collapsed);
    }

    private void applySidebarState(VBox sidebar, boolean collapsed) {
        if (sidebar == null) {
            return;
        }
        sidebar.setMinWidth(collapsed ? 80 : 320);
        sidebar.setMaxWidth(collapsed ? 80 : 320);
        sidebar.setPrefWidth(collapsed ? 80 : 320);
        if (collapsed) {
            if (!sidebar.getStyleClass().contains("collapsed")) {
                sidebar.getStyleClass().add("collapsed");
            }
            sidebar.getStyleClass().remove("expanded");
        } else {
            if (!sidebar.getStyleClass().contains("expanded")) {
                sidebar.getStyleClass().add("expanded");
            }
            sidebar.getStyleClass().remove("collapsed");
        }
        updateSidebarButtons(collapsed);
    }

    private void updateSidebarButtons(boolean collapsed) {
        setContentDisplay(adminMenuButton, collapsed);
        setContentDisplay(frontMenuButton, collapsed);
        setContentDisplay(adminFilGroupButton, collapsed);
        setContentDisplay(postsButton, collapsed);
        setContentDisplay(announcementsButton, collapsed);
        setContentDisplay(commentsButton, collapsed);
        setContentDisplay(aiButton, collapsed);
        setContentDisplay(adminEquipesButton, collapsed);
        setContentDisplay(adminTournoiButton, collapsed);
        setContentDisplay(adminBoutiqueButton, collapsed);
        setContentDisplay(adminComptesButton, collapsed);
        setContentDisplay(frontFeedButton, collapsed);
        setContentDisplay(frontEquipesButton, collapsed);
        setContentDisplay(frontTournoisButton, collapsed);
        setContentDisplay(frontBoutiqueButton, collapsed);
        setContentDisplay(frontCommandesButton, collapsed);
        setContentDisplay(frontSavedButton, collapsed);
        setContentDisplay(frontProfileButton, collapsed);
    }

    private void handleAnnouncementCreate() {
        try {
            Announcement announcement = buildAnnouncementForm();
            announcementService.addEntity(announcement);
            refreshAll();
            selectAnnouncementById(announcement.getId());
            clearAnnouncementForm();
            showSuccess("Annonce ajoutee avec succes.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleAnnouncementEdit() {
        if (selectedAnnouncement == null) {
            showError("Selectionnez une annonce a modifier.");
            return;
        }
        try {
            Announcement announcement = buildAnnouncementForm();
            announcementService.updateEntity(selectedAnnouncement.getId(), announcement);
            refreshAll();
            selectAnnouncementById(selectedAnnouncement.getId());
            showSuccess("Annonce modifiee avec succes.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleAnnouncementRemove() {
        if (selectedAnnouncement == null) {
            showError("Selectionnez une annonce a supprimer.");
            return;
        }
        try {
            announcementService.deleteEntity(selectedAnnouncement);
            refreshAll();
            clearAnnouncementForm();
            showSuccess("Annonce supprimee avec succes.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleCommentCreate() {
        try {
            Commentaire comment = buildCommentForm();
            commentaireService.addEntity(comment);
            refreshAll();
            selectCommentById(comment.getId());
            clearCommentForm();
            showSuccess("Commentaire ajoute avec succes.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleCommentEdit() {
        if (selectedComment == null) {
            showError("Selectionnez un commentaire a modifier.");
            return;
        }
        try {
            Commentaire comment = buildCommentForm();
            commentaireService.updateEntity(selectedComment.getId(), comment);
            refreshAll();
            selectCommentById(selectedComment.getId());
            showSuccess("Commentaire modifie avec succes.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleCommentRemove() {
        if (selectedComment == null) {
            showError("Selectionnez un commentaire a supprimer.");
            return;
        }
        try {
            commentaireService.deleteEntity(selectedComment);
            refreshAll();
            clearCommentForm();
            showSuccess("Commentaire supprime avec succes.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void rebuildCommentsIndex() {
        commentsByPost.clear();
        for (Commentaire comment : commentaires) {
            int postId = comment.getPostId();
            commentsByPost.computeIfAbsent(postId, k -> new ArrayList<>()).add(comment);
        }
    }

    private void configureBackofficeDashboard() {
        if (backofficeDashboardController != null) {
            backofficeDashboardController.setOnDataChanged(this::refreshAll);
        }
    }

    private void configurePostManagementModule() {
        if (postManagementModuleController != null) {
            postManagementModuleController.setOnDataChanged(this::refreshAll);
        }
    }

    private void configureAnnouncementManagementModule() {
        if (announcementManagementModuleController != null) {
            announcementManagementModuleController.setOnDataChanged(this::refreshAll);
        }
    }

    private void configureCommentManagementModule() {
        if (commentManagementModuleController != null) {
            commentManagementModuleController.setOnDataChanged(this::refreshAll);
        }
    }

    private void refreshBackofficeDashboard() {
        if (backofficeDashboardController != null) {
            backofficeDashboardController.refreshDashboard();
        }
    }

    private void refreshPostManagementModule() {
        if (postManagementModuleController != null) {
            postManagementModuleController.onViewShown();
        }
    }

    private void refreshAnnouncementManagementModule() {
        if (announcementManagementModuleController != null) {
            announcementManagementModuleController.onViewShown();
        }
    }

    private void refreshCommentManagementModule() {
        if (commentManagementModuleController != null) {
            commentManagementModuleController.onViewShown();
        }
    }

    private void setAdminFilSubmenuVisible(boolean show) {
        if (adminFilSubmenu == null) {
            return;
        }
        adminFilSubmenu.setVisible(show);
        adminFilSubmenu.setManaged(show);
        if (adminFilChevron != null) {
            adminFilChevron.setText(show ? "Ã¢â€“Â¾" : "Ã¢â€“Â¸");
        }
    }

    private void toggleAdminFilSubmenu() {
        if (adminFilSubmenu == null) {
            return;
        }
        boolean show = !adminFilSubmenu.isVisible();
        adminFilSubmenu.setVisible(show);
        adminFilSubmenu.setManaged(show);
        if (adminFilChevron != null) {
            adminFilChevron.setText(show ? "â–¾" : "â–¸");
        }
    }

    private void setContentDisplay(Button button, boolean collapsed) {
        if (button == null) {
            return;
        }
        button.setContentDisplay(collapsed ? ContentDisplay.GRAPHIC_ONLY : ContentDisplay.LEFT);
        button.setTextFill(button.getTextFill());
    }

    private void applyFrontFilter(String filterKey, Button activeButton) {
        currentFilter = filterKey;
        frontFeedItems.setAll(filterPosts(filterKey));
        renderFrontFeed();
        renderSavedPosts();
        renderTrendPanel();
        refreshBestPostingTime();
        markActive(activeButton, filterAllButton, filterTeamsButton, filterPlayersButton, filterTournamentsButton, filterMediaButton, filterAiButton);
    }

    private List<FilActualite> filterPosts(String filterKey) {
        if (FILTER_AI_RECOMMENDATIONS.equals(filterKey)) {
            int userId = resolveCurrentUserId();
            return service.getRecommendedPosts(userId);
        }
        List<FilActualite> filtered = new ArrayList<>();
        String searchToken = normalize(frontSearchField == null ? null : frontSearchField.getText()).trim();
        for (FilActualite post : posts) {
            boolean matches = switch (filterKey) {
                case FILTER_TEAMS -> contains(post, "equipe") || contains(post, "team") || contains(post, "club");
                case FILTER_PLAYERS -> contains(post, "joueur") || contains(post, "player") || contains(post, "mvp");
                case FILTER_TOURNAMENTS -> post.isEvent() || contains(post, "tournoi") || contains(post, "cup");
                case FILTER_MEDIA -> post.getImagePath() != null || post.getVideoUrl() != null;
                default -> true;
            };
            if (matches && matchesFrontSearch(post, searchToken)) {
                filtered.add(post);
            }
        }
        return filtered;
    }

    private boolean matchesFrontSearch(FilActualite post, String searchToken) {
        if (searchToken == null || searchToken.isBlank()) {
            return true;
        }
        return normalize(post.getContent()).contains(searchToken)
                || normalize(post.getEventTitle()).contains(searchToken)
                || normalize(post.getEventLocation()).contains(searchToken)
                || normalize(toFrontCategory(post)).contains(searchToken)
                || normalize(post.getMediaType()).contains(searchToken)
                || String.valueOf(post.getAuthorId() == null ? "" : post.getAuthorId()).contains(searchToken);
    }

    private boolean contains(FilActualite post, String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        return normalize(post.getContent()).contains(lower)
                || normalize(post.getEventTitle()).contains(lower)
                || normalize(post.getEventLocation()).contains(lower);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private Button resolveFilterButton(String filterKey) {
        return switch (filterKey) {
            case FILTER_TEAMS -> filterTeamsButton;
            case FILTER_PLAYERS -> filterPlayersButton;
            case FILTER_TOURNAMENTS -> filterTournamentsButton;
            case FILTER_MEDIA -> filterMediaButton;
            default -> filterAllButton;
        };
    }

    private void markActive(Button activeButton, Button... buttons) {
        for (Button button : buttons) {
            if (button == null) {
                continue;
            }
            button.getStyleClass().remove("active");
        }
        if (activeButton != null && !activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    private void toggleManaged(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer parseInteger(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return Integer.parseInt(trimmed);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Veuillez saisir un nombre entier valide.");
        }
    }

    private int parseRequiredInteger(String value, String message) {
        Integer parsed = parseInteger(value);
        if (parsed == null) {
            throw new IllegalArgumentException(message);
        }
        return parsed;
    }

    private LocalDateTime toDateTime(LocalDate value) {
        return value == null ? null : value.atStartOfDay();
    }

    private String extractMediaFilename(FilActualite post) {
        String source = post.getImagePath() != null ? post.getImagePath() : post.getVideoUrl();
        if (source == null || source.isBlank()) {
            return null;
        }
        int slash = source.lastIndexOf('/');
        return slash >= 0 ? source.substring(slash + 1) : source;
    }

    private String buildMediaSummary(FilActualite post) {
        if (post.getVideoUrl() != null) {
            return "Video";
        }
        if (post.getImagePath() != null) {
            return "Publication";
        }
        return post.getMediaType() == null ? "Texte" : capitalize(post.getMediaType());
    }

    private String buildEventSummary(FilActualite post) {
        if (!post.isEvent()) {
            return "general";
        }
        return post.getEventTitle() == null ? "event" : "event";
    }

    private String safeSummary(FilActualite post) {
        if (post == null) {
            return "Aucune publication";
        }
        String summary = trimToNull(stripEmbeddedMediaUrl(post.getDisplayTitle()));
        if (summary != null) {
            return summary;
        }
        return trimToNull(resolveEmbeddedMediaUrl(post)) != null ? "Publication media" : "Aucune publication";
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "Texte";
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1).toLowerCase(Locale.ROOT);
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private String formatTableDate(LocalDateTime dateTime) {
        return dateTime == null ? "-" : TABLE_DATE_FORMAT.format(dateTime);
    }

    private String formatFrontDate(LocalDateTime dateTime) {
        return dateTime == null ? "maintenant" : FRONT_DATE_FORMAT.format(dateTime);
    }

    private ImageView buildPostImage(String source) {
        String url = resolveImageUrl(source);
        if (url == null) {
            return null;
        }
        ImageView imageView = new ImageView(new Image(url, true));
        imageView.setFitWidth(760);
        imageView.setFitHeight(420);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.getStyleClass().add("post-image");
        return imageView;
    }

    private Node buildVideoPreview(String videoUrl) {
        String normalizedUrl = trimToNull(videoUrl);
        if (normalizedUrl == null) {
            return null;
        }

        String youtubeId = extractYouTubeVideoId(normalizedUrl);
        if (youtubeId != null) {
            return buildEmbeddedYouTubePlayer(youtubeId);
        }

        String playableSource = resolvePlayableVideoUrl(normalizedUrl);
        if (playableSource != null) {
            return buildEmbeddedMediaPlayer(playableSource, normalizedUrl);
        }

        Button openButton = new Button("Ouvrir la video");
        openButton.getStyleClass().addAll("hero-button", "primary", "video-preview-action");
        openButton.setOnAction(event -> openExternalMedia(normalizedUrl));
        return new VBox(8, new Label("Lecture integree non supportee pour cette source."), openButton);
    }

    private Node buildEmbeddedYouTubePlayer(String youtubeId) {
        WebView webView = new WebView();
        webView.setPrefSize(760, 420);
        webView.setMinHeight(300);
        webView.setMaxWidth(Double.MAX_VALUE);
        webView.setContextMenuEnabled(false);
        String embedUrl = "https://www.youtube.com/embed/" + youtubeId + "?rel=0&modestbranding=1";
        webView.getEngine().load(embedUrl);
        webView.getStyleClass().add("inline-video-webview");
        return webView;
    }

    private Node buildEmbeddedMediaPlayer(String mediaSource, String originalUrl) {
        Media media;
        try {
            media = new Media(mediaSource);
        } catch (Exception ex) {
            Button openButton = new Button("Ouvrir la video");
            openButton.getStyleClass().addAll("hero-button", "primary", "video-preview-action");
            openButton.setOnAction(event -> openExternalMedia(originalUrl));
            return new VBox(8, new Label("Lecture integree indisponible pour ce media."), openButton);
        }

        MediaPlayer player = new MediaPlayer(media);
        player.setAutoPlay(false);

        MediaView mediaView = new MediaView(player);
        mediaView.setFitWidth(760);
        mediaView.setFitHeight(420);
        mediaView.setPreserveRatio(true);
        mediaView.setSmooth(true);

        Button toggle = new Button("Lire");
        toggle.getStyleClass().addAll("hero-button", "primary", "video-preview-action");
        toggle.setOnAction(event -> {
            MediaPlayer.Status status = player.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                player.pause();
                toggle.setText("Lire");
            } else {
                player.play();
                toggle.setText("Pause");
            }
        });
        player.setOnEndOfMedia(() -> {
            player.seek(Duration.ZERO);
            player.pause();
            toggle.setText("Lire");
        });

        Button openExternal = new Button("Ouvrir");
        openExternal.getStyleClass().addAll("hero-button", "ghost", "video-preview-action");
        openExternal.setOnAction(event -> openExternalMedia(originalUrl));

        HBox controls = new HBox(8, toggle, openExternal);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.getStyleClass().add("inline-video-controls");

        VBox container = new VBox(8, mediaView, controls);
        container.getStyleClass().add("inline-video-container");
        container.setFillWidth(true);
        return container;
    }

    private String resolvePlayableVideoUrl(String source) {
        String normalized = trimToNull(source);
        if (normalized == null) {
            return null;
        }
        if (normalized.startsWith("/uploads/")) {
            File file = new File(PUBLIC_MEDIA_ROOT + normalized);
            return file.exists() ? file.toURI().toString() : null;
        }
        if (normalized.matches("^[A-Za-z]:\\\\.*")) {
            File file = new File(normalized);
            return file.exists() ? file.toURI().toString() : null;
        }
        if (normalized.startsWith("http://") || normalized.startsWith("https://") || normalized.startsWith("file:/")) {
            return isDirectVideoFileUrl(normalized) ? normalized : null;
        }
        return null;
    }

    private boolean isDirectVideoFileUrl(String url) {
        String normalized = url.toLowerCase(Locale.ROOT);
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        int fragmentIndex = normalized.indexOf('#');
        if (fragmentIndex >= 0) {
            normalized = normalized.substring(0, fragmentIndex);
        }
        return normalized.endsWith(".mp4")
                || normalized.endsWith(".m4v")
                || normalized.endsWith(".mov")
                || normalized.endsWith(".webm")
                || normalized.endsWith(".mkv");
    }

    private String resolveEmbeddedMediaUrl(FilActualite post) {
        if (post == null) {
            return null;
        }
        String explicitImage = trimToNull(post.getImagePath());
        if (explicitImage != null) {
            return explicitImage;
        }
        String explicitVideo = trimToNull(post.getVideoUrl());
        if (explicitVideo != null) {
            return explicitVideo;
        }
        return extractEmbeddedMediaUrl(post.getContent());
    }

    private String extractEmbeddedMediaUrl(String content) {
        String normalized = trimToNull(content);
        if (normalized == null) {
            return null;
        }
        Matcher matcher = MEDIA_URL_PATTERN.matcher(normalized);
        while (matcher.find()) {
            String candidate = trimPunctuation(matcher.group(1));
            if (trimToNull(candidate) != null) {
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
        String stripped = MEDIA_URL_PATTERN.matcher(normalized)
                .replaceAll(" ")
                .replaceAll("\\s{2,}", " ")
                .trim();
        return stripped.isEmpty() ? null : stripped;
    }

    private String trimPunctuation(String token) {
        if (token == null) {
            return "";
        }
        return token.replaceAll("^[\\p{Punct}]+|[\\p{Punct}]+$", "");
    }

    private boolean isVideoLike(String source) {
        String normalized = trimToNull(source);
        if (normalized == null) {
            return false;
        }
        String lower = normalized.toLowerCase(Locale.ROOT);
        return lower.contains("youtube.com")
                || lower.contains("youtu.be")
                || lower.endsWith(".mp4")
                || lower.endsWith(".mov")
                || lower.endsWith(".webm")
                || lower.endsWith(".mkv");
    }

    private String resolveImageUrl(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        if (source.startsWith("http://") || source.startsWith("https://") || source.startsWith("file:/")) {
            return source;
        }
        String normalized = source.startsWith("/") ? source : "/uploads/" + source;
        File file = new File(PUBLIC_ROOT + normalized);
        if (file.exists()) {
            return file.toURI().toString();
        }
        File direct = new File(source);
        return direct.exists() ? direct.toURI().toString() : null;
    }

    private String extractYouTubeVideoId(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
            if (host.contains("youtu.be")) {
                String path = uri.getPath();
                return path != null && path.length() > 1 ? path.substring(1) : null;
            }
            if (host.contains("youtube.com")) {
                String query = uri.getQuery();
                if (query != null) {
                    for (String token : query.split("&")) {
                        if (token.startsWith("v=") && token.length() > 2) {
                            return token.substring(2);
                        }
                    }
                }
                String path = uri.getPath();
                if (path != null && path.startsWith("/embed/")) {
                    return path.substring("/embed/".length());
                }
            }
        } catch (URISyntaxException ignored) {
            return null;
        }
        return null;
    }

    private String compactMediaLabel(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return url;
            }
            return host.replace("www.", "");
        } catch (URISyntaxException ignored) {
            return url;
        }
    }

    private void openExternalMedia(String url) {
        String normalized = trimToNull(url);
        if (normalized == null) {
            return;
        }
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(normalized));
            } else {
                showInfo("Ouverture externe indisponible");
            }
        } catch (Exception ex) {
            showError("Impossible d'ouvrir le media externe.");
        }
    }

    private void startMessengerCall(boolean videoEnabled) {
        if (activeConversation == null) {
            showError("Selectionnez une conversation avant de lancer un appel.");
            return;
        }
        String callUrl = messengerCallService.buildCallUrl(
                activeConversation.getConversationId(),
                activeConversation.getPeerDisplayName(),
                videoEnabled
        );
        try {
            messengerService.sendMessage(
                    activeConversation.getConversationId(),
                    resolveCurrentUserId(),
                    buildCallInviteMessageContent(callUrl, videoEnabled),
                    null
            );
        } catch (RuntimeException ex) {
            showError("Appel lance, mais invitation non envoyee: " + ex.getMessage());
        }
        openExternalMedia(callUrl);
        showSuccess(videoEnabled ? "Appel video lance dans votre navigateur." : "Appel vocal lance dans votre navigateur.");
        refreshMessengerRealtime();
    }

    private String buildCallInviteMessageContent(String callUrl, boolean videoEnabled) {
        return "[CALL_INVITE][" + (videoEnabled ? "video" : "voice") + "] " + callUrl;
    }

    private String parseCallInviteUrl(String content) {
        String value = trimToNull(content);
        if (value == null) {
            return null;
        }
        Matcher matcher = CALL_INVITE_PATTERN.matcher(value);
        return matcher.matches() ? matcher.group(2) : null;
    }

    private String parseCallInviteType(String content) {
        String value = trimToNull(content);
        if (value == null) {
            return null;
        }
        Matcher matcher = CALL_INVITE_PATTERN.matcher(value);
        return matcher.matches() ? matcher.group(1) : null;
    }

    private void refreshStreamingHub() {
        if (streamingLiveBox == null || streamingHighlightsBox == null) {
            return;
        }
        StreamingIntegrationService.StreamingSnapshot snapshot = streamingIntegrationService.loadSnapshot();
        streamingLiveBox.getChildren().clear();
        streamingHighlightsBox.getChildren().clear();

        for (StreamingIntegrationService.LiveStreamCard stream : snapshot.liveStreams()) {
            streamingLiveBox.getChildren().add(buildStreamCard(stream));
        }
        for (StreamingIntegrationService.HighlightCard highlight : snapshot.highlights()) {
            streamingHighlightsBox.getChildren().add(buildHighlightCard(highlight));
        }
        if (snapshot.liveStreams().isEmpty()) {
            streamingLiveBox.getChildren().add(buildStreamingHint("Aucun live YouTube en cours pour la requete configuree."));
        }
        if (snapshot.highlights().isEmpty()) {
            streamingHighlightsBox.getChildren().add(buildStreamingHint("Aucun highlight YouTube disponible (verifie la cle API)."));
        }
        if (streamingLiveCountLabel != null) {
            streamingLiveCountLabel.setText(String.valueOf(snapshot.liveStreams().size()));
        }
        if (streamingHighlightCountLabel != null) {
            streamingHighlightCountLabel.setText(String.valueOf(snapshot.highlights().size()));
        }
        if (streamingLastSyncLabel != null) {
            streamingLastSyncLabel.setText(STREAM_SYNC_FORMAT.format(LocalDateTime.now()));
        }
        if (streamingStatusLabel != null) {
            streamingStatusLabel.setText(snapshot.status() == null || snapshot.status().isBlank()
                    ? "Flux synchronises."
                    : snapshot.status());
        }
    }

    private Label buildStreamingHint(String message) {
        Label hint = new Label(message);
        hint.getStyleClass().add("streaming-empty-hint");
        hint.setWrapText(true);
        return hint;
    }

    private VBox buildStreamCard(StreamingIntegrationService.LiveStreamCard stream) {
        VBox card = new VBox(10);
        card.getStyleClass().add("streaming-card");

        ImageView preview = new ImageView();
        preview.getStyleClass().add("streaming-thumb");
        preview.setFitWidth(480);
        preview.setFitHeight(220);
        preview.setPreserveRatio(true);
        String thumbnail = trimToNull(stream.thumbnailUrl());
        if (thumbnail != null) {
            preview.setImage(new Image(thumbnail, true));
        }
        StackPane mediaShell = new StackPane(preview);
        mediaShell.getStyleClass().add("streaming-card-media-shell");
        Label livePill = new Label("LIVE");
        livePill.getStyleClass().add("streaming-live-pill");
        StackPane.setAlignment(livePill, Pos.TOP_LEFT);
        mediaShell.getChildren().add(livePill);

        Label title = new Label(defaultText(stream.title()));
        title.getStyleClass().add("streaming-card-title");
        title.setWrapText(true);

        String viewers = stream.viewerCount() > 0 ? stream.viewerCount() + " viewers" : "Audience live";
        Label meta = new Label(stream.channelName() + "  |  " + stream.gameName() + "  |  " + viewers);
        meta.getStyleClass().add("streaming-card-meta");

        HBox actions = new HBox(10);
        actions.getStyleClass().add("streaming-card-actions");
        Button open = new Button("Regarder");
        open.getStyleClass().addAll("messenger-send-button", "streaming-watch-button");
        open.setOnAction(event -> openExternalMedia(stream.link()));
        Label badge = new Label(stream.platform());
        badge.getStyleClass().add("streaming-platform-badge");
        actions.getChildren().addAll(open, badge);

        card.getChildren().addAll(mediaShell, title, meta, actions);
        return card;
    }

    private VBox buildHighlightCard(StreamingIntegrationService.HighlightCard highlight) {
        VBox card = new VBox(10);
        card.getStyleClass().add("streaming-card");

        ImageView preview = new ImageView();
        preview.getStyleClass().add("streaming-thumb");
        preview.setFitWidth(480);
        preview.setFitHeight(220);
        preview.setPreserveRatio(true);
        String thumbnail = trimToNull(highlight.thumbnailUrl());
        if (thumbnail != null) {
            preview.setImage(new Image(thumbnail, true));
        }
        StackPane mediaShell = new StackPane(preview);
        mediaShell.getStyleClass().add("streaming-card-media-shell");

        Label title = new Label(defaultText(highlight.title()));
        title.getStyleClass().add("streaming-card-title");
        title.setWrapText(true);

        Label meta = new Label(highlight.channelName() + "  |  " + highlight.publishedAt());
        meta.getStyleClass().add("streaming-card-meta");

        HBox actions = new HBox(10);
        actions.getStyleClass().add("streaming-card-actions");
        Button open = new Button("Voir le highlight");
        open.getStyleClass().addAll("messenger-send-button", "streaming-watch-button");
        open.setOnAction(event -> openExternalMedia(highlight.link()));
        Label badge = new Label(highlight.platform());
        badge.getStyleClass().add("streaming-platform-badge");
        actions.getChildren().addAll(open, badge);

        card.getChildren().addAll(mediaShell, title, meta, actions);
        return card;
    }

    private void showSuccess(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.getStyleClass().remove("status-error");
            if (!statusLabel.getStyleClass().contains("status-success")) {
                statusLabel.getStyleClass().add("status-success");
            }
        }
        if (frontComposerStatusLabel != null && !frontPublishInFlight) {
            setFrontComposerMessage(message, false);
        }
    }

    private void showError(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.getStyleClass().remove("status-success");
            if (!statusLabel.getStyleClass().contains("status-error")) {
                statusLabel.getStyleClass().add("status-error");
            }
        }
        if (frontComposerStatusLabel != null && !frontPublishInFlight) {
            setFrontComposerMessage(message, true);
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Operation impossible");
        alert.setContentText(message);
        Platform.runLater(alert::show);
    }

    private void showInfo(String label) {
        if (statusLabel != null) {
            statusLabel.setText(label + " : section a venir.");
        }
    }

    private void setFrontPublishState(boolean loading, String message) {
        frontPublishInFlight = loading;
        if (frontPublishButton != null) {
            frontPublishButton.setDisable(loading);
        }
        if (frontPublishProgress != null) {
            frontPublishProgress.setVisible(loading);
            frontPublishProgress.setManaged(loading);
        }
        setFrontComposerInputsDisabled(loading);
        if (message != null) {
            setFrontComposerMessage(message, false);
        }
    }

    private void setFrontComposerInputsDisabled(boolean disabled) {
        if (frontContentArea != null) frontContentArea.setDisable(disabled);
        if (frontImageField != null) frontImageField.setDisable(disabled);
        if (frontVideoField != null) frontVideoField.setDisable(disabled);
        if (frontEventCheckBox != null) frontEventCheckBox.setDisable(disabled);
        if (frontEventTitleField != null) frontEventTitleField.setDisable(disabled);
        if (frontEventDateField != null) frontEventDateField.setDisable(disabled);
        if (frontEventLocationField != null) frontEventLocationField.setDisable(disabled);
        if (frontMaxParticipantsField != null) frontMaxParticipantsField.setDisable(disabled);
        if (frontAuthorIdField != null) frontAuthorIdField.setDisable(disabled);
    }

    private void setFrontComposerMessage(String message, boolean error) {
        if (frontComposerStatusLabel == null) {
            return;
        }
        frontComposerStatusLabel.setText(message);
        frontComposerStatusLabel.getStyleClass().remove("composer-helper-text-error");
        if (error && !frontComposerStatusLabel.getStyleClass().contains("composer-helper-text-error")) {
            frontComposerStatusLabel.getStyleClass().add("composer-helper-text-error");
        }
    }

    private void updateFrontComposerMeta(int currentLength, boolean warning) {
        if (frontCharCountLabel == null) {
            return;
        }
        frontCharCountLabel.setText(currentLength + " / " + FRONT_CONTENT_MAX_LENGTH);
        frontCharCountLabel.getStyleClass().remove("composer-counter-warning");
        if (warning && !frontCharCountLabel.getStyleClass().contains("composer-counter-warning")) {
            frontCharCountLabel.getStyleClass().add("composer-counter-warning");
        }
    }

    private void updateFrontMediaPreview() {
        updateMediaPreview(frontMediaPreview, frontMediaHintLabel,
                normalizeMediaPath(frontImageField == null ? null : frontImageField.getText()),
                trimToNull(frontVideoField == null ? null : frontVideoField.getText()),
                "Ajoutez un lien image ou video pour afficher un apercu.");
    }

    private void updateMediaPreview(ImageView preview, Label hintLabel, String imageCandidate, String fallbackText, String emptyText) {
        if (preview == null || hintLabel == null) {
            return;
        }
        if (imageCandidate != null && isImagePath(imageCandidate)) {
            try {
                preview.setImage(new Image(imageCandidate, true));
                preview.setVisible(true);
                preview.setManaged(true);
                hintLabel.setText("Apercu media actif");
                return;
            } catch (IllegalArgumentException ignored) {
                hintLabel.setText(imageCandidate);
            }
        }
        preview.setImage(null);
        preview.setVisible(false);
        preview.setManaged(false);
        hintLabel.setText(trimToNull(fallbackText) != null ? trimToNull(fallbackText) : emptyText);
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

    private void upsertMediaInFrontContent(String mediaPath) {
        if (frontContentArea == null) {
            return;
        }
        String baseContent = trimToNull(stripEmbeddedMediaUrl(frontContentArea.getText()));
        String normalizedMedia = trimToNull(mediaPath);
        if (normalizedMedia == null) {
            frontContentArea.setText(baseContent == null ? "" : baseContent);
            return;
        }
        String nextContent = (baseContent == null ? "" : baseContent + System.lineSeparator()) + normalizedMedia;
        frontContentArea.setText(nextContent);
        frontContentArea.positionCaret(nextContent.length());
    }

    private boolean isImagePath(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.endsWith(".png")
                || normalized.endsWith(".jpg")
                || normalized.endsWith(".jpeg")
                || normalized.endsWith(".gif")
                || normalized.endsWith(".webp");
    }

    private File chooseMediaFile(String title, String... extensions) {
        if (frontContentArea == null || frontContentArea.getScene() == null) {
            return null;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Media", extensions));
        return chooser.showOpenDialog(frontContentArea.getScene().getWindow());
    }

    private String copyMediaToUploads(File sourceFile) {
        try {
            Files.createDirectories(Path.of(PUBLIC_UPLOADS_DIRECTORY));
            String targetName = System.currentTimeMillis() + "-" + sourceFile.getName();
            Path target = Path.of(PUBLIC_UPLOADS_DIRECTORY, targetName);
            Files.copy(sourceFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + targetName;
        } catch (IOException ex) {
            throw new IllegalStateException("Impossible d'importer le media: " + ex.getMessage(), ex);
        }
    }

    private void renderFrontFeed() {
        if (frontFeedBox == null) {
            return;
        }
        frontFeedBox.setCache(true);
        frontFeedBox.setCacheHint(CacheHint.SPEED);
        frontFeedBox.getChildren().clear();
        if (frontFeedItems.isEmpty()) {
            boolean noPostsAvailable = posts.isEmpty();
            frontFeedBox.getChildren().add(buildEmptyState(
                    noPostsAvailable ? "Le feed attend son premier drop" : "Aucun contenu trouve",
                    noPostsAvailable
                            ? "Partage la premiere publication pour lancer l'arene sociale E-Sportify."
                            : "Essaie une autre recherche ou change de filtre pour explorer plus de publications."
            ));
            return;
        }
        for (FilActualite post : frontFeedItems) {
            Node card = buildPostCard(post, false);
            frontFeedBox.getChildren().add(card);
            if (highlightedFrontPostId != null && highlightedFrontPostId == post.getId()) {
                playPostAppearAnimation(card);
                highlightedFrontPostId = null;
            }
        }
    }

    private void renderFrontAnnouncements() {
        if (frontAnnouncementsBox == null) {
            return;
        }
        frontAnnouncementsBox.setCache(true);
        frontAnnouncementsBox.setCacheHint(CacheHint.SPEED);
        frontAnnouncementsBox.getChildren().clear();
        if (frontAnnouncementsItems.isEmpty()) {
            frontAnnouncementsBox.getChildren().add(buildEmptyState(
                    "Aucune annonce",
                    "Les annonces officielles apparaitront ici des qu'elles seront publiees."
            ));
            return;
        }
        for (int i = 0; i < frontAnnouncementsItems.size(); i++) {
            Announcement announcement = frontAnnouncementsItems.get(i);
            frontAnnouncementsBox.getChildren().add(buildAnnouncementCard(announcement, i == 0));
        }
    }

    private Node buildPostCard(FilActualite item) {
        return buildPostCard(item, false);
    }

    private Node buildPostCard(FilActualite item, boolean detailMode) {
        VBox card = new VBox(16);
        card.getStyleClass().add("post-card");
        card.setCache(true);
        card.setCacheHint(CacheHint.SPEED);
        if (item.isEvent()) {
            card.setStyle("-fx-border-color: rgba(226,200,55,0.70);");
        }

        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        Circle avatar = new Circle(28, Color.web("#7ed0ff"));
        Label avatarLabel = new Label(item.getAuthorId() == null ? "A" : String.valueOf(item.getAuthorId()));
        avatarLabel.getStyleClass().add("avatar-text");
        StackPane avatarPane = new StackPane(avatar, avatarLabel);
        avatarPane.setPadding(new Insets(2));
        avatarPane.setCache(true);
        avatarPane.setCacheHint(CacheHint.SPEED);

        VBox titleBox = new VBox(4);
        Label nameLabel = new Label(resolveAuthorName(item.getAuthorId()));
        nameLabel.getStyleClass().add("post-author");
        Label metaLabel = new Label(formatFrontDate(item.getCreatedAt()));
        metaLabel.getStyleClass().add("post-meta");
        titleBox.getChildren().addAll(nameLabel, metaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button floating = new Button(detailMode ? "..." : "Voir");
        floating.getStyleClass().add("icon-btn");
        floating.setContentDisplay(ContentDisplay.CENTER);
        floating.setOnAction(event -> openPostDetail(item));

        header.getChildren().addAll(avatarPane, titleBox, spacer, floating);
        if (canManagePost(item)) {
            Button editPostButton = createFeedIconButton("icon-edit", "Modifier");
            editPostButton.setOnAction(event -> editOwnPost(item));
            Button deletePostButton = createFeedIconButton("icon-delete", "Supprimer");
            deletePostButton.setOnAction(event -> deleteOwnPost(item));
            header.getChildren().addAll(editPostButton, deletePostButton);
        }

        HBox actions = new HBox(10);
        actions.getChildren().addAll(
                buildPill(toFrontCategory(item)),
                buildPill(item.isEvent() ? "Evenement" : "Publication")
        );

        VBox topPanel = new VBox(14);
        topPanel.getStyleClass().add("post-top-panel");
        topPanel.getChildren().addAll(header, actions);
        card.getChildren().add(topPanel);

        String embeddedMediaUrl = extractEmbeddedMediaUrl(item.getContent());
        String displayContent = stripEmbeddedMediaUrl(item.getContent());
        String imageSource = trimToNull(item.getImagePath());
        String videoSource = trimToNull(item.getVideoUrl());
        if (imageSource == null && videoSource == null && embeddedMediaUrl != null) {
            if (isImagePath(embeddedMediaUrl)) {
                imageSource = embeddedMediaUrl;
            } else if (isVideoLike(embeddedMediaUrl)) {
                videoSource = embeddedMediaUrl;
            }
        }

        Node mediaNode = null;
        ImageView imageView = buildPostImage(imageSource);
        if (imageView != null) {
            imageView.setCache(true);
            imageView.setCacheHint(CacheHint.SPEED);
            mediaNode = imageView;
        } else if (videoSource != null) {
            mediaNode = buildVideoPreview(videoSource);
        }

        if (mediaNode != null) {
            StackPane mediaFrame = new StackPane(mediaNode);
            mediaFrame.getStyleClass().add("post-media-frame");
            card.getChildren().add(mediaFrame);
        }

        if (displayContent != null) {
            card.getChildren().add(buildPostBodyPanel(displayContent, detailMode));
        }

        if (item.isEvent()) {
            VBox eventBox = new VBox(8);
            eventBox.getStyleClass().add("event-box");
            Label eventDate = new Label(item.getEventDate() == null ? "Date a confirmer" : item.getEventDate().format(FRONT_DATE_FORMAT));
            eventDate.getStyleClass().add("event-title");
            Label eventTitle = new Label(defaultText(item.getEventTitle()));
            eventTitle.getStyleClass().add("event-title");
            Label eventLocation = new Label(defaultText(item.getEventLocation()));
            eventLocation.getStyleClass().add("event-details");
            eventBox.getChildren().addAll(eventDate, eventTitle, eventLocation);
            card.getChildren().add(eventBox);
        }

        HBox engagementRow = new HBox(10);
        engagementRow.getStyleClass().add("engagement-row");
        HBox likeMetric = createLikeMetric(item);
        VBox commentsWrap = new VBox(8);
        commentsWrap.getStyleClass().add("comments-wrap");
        commentsWrap.setCache(true);
        commentsWrap.setCacheHint(CacheHint.SPEED);
        commentsWrap.setVisible(detailMode);
        commentsWrap.setManaged(detailMode);
        HBox commentMetric = createSocialMetric(
                "icon-comment",
                buildInteractionSummary(item),
                "Commenter",
                () -> toggleCommentsSection(commentsWrap)
        );
        HBox shareMetric = createSocialMetric(
                "icon-share",
                buildShareSummary(item),
                "Partager",
                () -> handleFeedPrimaryAction(item)
        );
        HBox saveMetric = createSaveMetric(item);
        engagementRow.getChildren().addAll(likeMetric, commentMetric, shareMetric, saveMetric);
        card.getChildren().add(engagementRow);

        Label commentsTitle = new Label("Commentaires");
        commentsTitle.getStyleClass().add("panel-title");
        commentsWrap.getChildren().add(commentsTitle);

        List<Commentaire> postComments = commentsByPost.getOrDefault(item.getId(), List.of());
        if (postComments.isEmpty()) {
            Label emptyLabel = new Label("Aucun commentaire pour le moment.");
            emptyLabel.getStyleClass().add("panel-text");
            commentsWrap.getChildren().add(emptyLabel);
        } else {
            for (Commentaire comment : postComments) {
                VBox commentBox = new VBox(4);
                commentBox.getStyleClass().add("comment-box");
                Label meta = new Label(resolveAuthorName(comment.getAuthorId()) + " â€¢ " + formatFrontDate(comment.getCreatedAt()));
                meta.getStyleClass().add("comment-meta");
                Label text = new Label(comment.getDisplayContent());
                text.getStyleClass().add("comment-text");
                text.setWrapText(true);
                commentBox.getChildren().addAll(meta, text);
                commentsWrap.getChildren().add(commentBox);
            }
        }

        HBox commentInputRow = new HBox(10);
        commentInputRow.getStyleClass().add("comment-input-row");
        TextField commentInput = new TextField();
        commentInput.setPromptText("Ecrire un commentaire...");
        commentInput.getStyleClass().add("comment-input");
        Button sendBtn = createFeedIconButton("icon-send", "Commenter");
        sendBtn.setOnAction(evt -> submitInlineComment(item.getId(), commentInput.getText()));
        commentInputRow.getChildren().addAll(commentInput, sendBtn);
        HBox.setHgrow(commentInput, Priority.ALWAYS);
        commentsWrap.getChildren().add(commentInputRow);

        card.getChildren().add(commentsWrap);
        if (!detailMode) {
            card.setOnMouseClicked(event -> {
                if (event.getButton() != MouseButton.PRIMARY || isInteractiveTarget(event.getTarget())) {
                    return;
                }
                openPostDetail(item);
            });
        }
        return card;
    }

    private void toggleCommentsSection(VBox commentsWrap) {
        if (commentsWrap == null) {
            return;
        }
        boolean visible = !commentsWrap.isVisible();
        commentsWrap.setVisible(visible);
        commentsWrap.setManaged(visible);
    }

    private VBox buildPostBodyPanel(String content, boolean detailMode) {
        VBox bodyPanel = new VBox(8);
        bodyPanel.getStyleClass().add("post-body-panel");

        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.getStyleClass().add("post-content");
        bodyPanel.getChildren().add(contentLabel);

        if (detailMode || content.length() <= FRONT_POST_PREVIEW_LIMIT) {
            return bodyPanel;
        }

        final String preview = content.substring(0, FRONT_POST_PREVIEW_LIMIT).trim() + "...";
        contentLabel.setText(preview);
        Hyperlink toggle = new Hyperlink("Voir plus");
        toggle.getStyleClass().add("post-see-more-link");
        toggle.setOnMouseClicked(MouseEvent::consume);
        toggle.setOnAction(event -> {
            event.consume();
            boolean expanded = "Voir moins".equals(toggle.getText());
            if (expanded) {
                contentLabel.setText(preview);
                toggle.setText("Voir plus");
            } else {
                contentLabel.setText(content);
                toggle.setText("Voir moins");
            }
            bodyPanel.requestLayout();
            Node parent = bodyPanel.getParent();
            if (parent instanceof Region region) {
                region.requestLayout();
            } else if (parent instanceof Parent container) {
                container.requestLayout();
            }
        });
        bodyPanel.getChildren().add(toggle);
        return bodyPanel;
    }

    private void openPostDetail(FilActualite post) {
        if (post == null || frontPostDetailView == null || frontPostDetailContainer == null) {
            return;
        }
        frontPostDetailContainer.getChildren().clear();
        frontPostDetailContainer.getChildren().add(buildPostCard(post, true));
        if (frontPostDetailHeaderLabel != null) {
            frontPostDetailHeaderLabel.setText("Publication de " + resolveAuthorName(post.getAuthorId()));
        }
        showFrontOffice();
        showFrontView(frontPostDetailView, frontFeedButton);
    }

    private boolean canManagePost(FilActualite post) {
        if (post == null || post.getAuthorId() == null || currentUser == null) {
            return false;
        }
        return post.getAuthorId().intValue() == currentUser.getId();
    }

    private boolean isInteractiveTarget(Object target) {
        if (!(target instanceof Node node)) {
            return false;
        }
        Node cursor = node;
        while (cursor != null) {
            if (cursor instanceof ButtonBase
                    || cursor instanceof TextInputControl
                    || cursor instanceof ComboBoxBase<?>
                    || cursor instanceof CheckBox
                    || cursor instanceof MediaView
                    || cursor instanceof WebView) {
                return true;
            }
            cursor = cursor.getParent();
        }
        return false;
    }

    private void editOwnPost(FilActualite post) {
        if (!canManagePost(post)) {
            showError("Seul le createur peut modifier cette publication.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog(defaultText(post.getContent()));
        dialog.setTitle("Modifier publication");
        dialog.setHeaderText("Modification rapide (texte + media)");
        dialog.setContentText("Contenu:");
        dialog.showAndWait().ifPresent(newContent -> {
            String sanitized = trimToNull(newContent);
            if (sanitized == null) {
                showError("Le contenu ne peut pas etre vide.");
                return;
            }
            try {
                FilActualite updated = copyPost(post);
                updated.setContent(sanitized);
                service.updateEntity(post.getId(), updated);
                highlightedFrontPostId = post.getId();
                refreshAll();
                showSuccess("Publication modifiee.");
                openPostDetail(findPostById(post.getId()));
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });
    }

    private void deleteOwnPost(FilActualite post) {
        if (!canManagePost(post)) {
            showError("Seul le createur peut supprimer cette publication.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer publication");
        confirm.setHeaderText("Confirmer la suppression");
        confirm.setContentText("Cette action est definitive.");
        confirm.showAndWait().ifPresent(buttonType -> {
            if (buttonType != ButtonType.OK) {
                return;
            }
            try {
                service.deleteById(post.getId());
                refreshAll();
                showSuccess("Publication supprimee.");
                showFrontOffice();
                showFrontView(frontFeedView, frontFeedButton);
            } catch (RuntimeException ex) {
                showError(ex.getMessage());
            }
        });
    }

    private FilActualite findPostById(int id) {
        for (FilActualite post : posts) {
            if (post.getId() == id) {
                return post;
            }
        }
        return null;
    }

    private FilActualite copyPost(FilActualite source) {
        FilActualite copy = new FilActualite();
        copy.setId(source.getId());
        copy.setContent(source.getContent());
        copy.setMediaType(source.getMediaType());
        copy.setMediaFilename(source.getMediaFilename());
        copy.setCreatedAt(source.getCreatedAt());
        copy.setImagePath(source.getImagePath());
        copy.setVideoUrl(source.getVideoUrl());
        copy.setEvent(source.isEvent());
        copy.setEventTitle(source.getEventTitle());
        copy.setEventDate(source.getEventDate());
        copy.setEventLocation(source.getEventLocation());
        copy.setMaxParticipants(source.getMaxParticipants());
        copy.setAuthorId(source.getAuthorId());
        return copy;
    }

    private Node buildEmptyState(String titleText, String bodyText) {
        VBox box = new VBox(10);
        box.getStyleClass().add("empty-state-card");
        Label glyph = new Label("â—Ž");
        glyph.getStyleClass().add("empty-state-glyph");
        Label title = new Label(titleText);
        title.getStyleClass().add("empty-state-title");
        Label body = new Label(bodyText);
        body.getStyleClass().add("panel-text");
        body.setWrapText(true);
        box.getChildren().addAll(glyph, title, body);
        return box;
    }

    private void playPostAppearAnimation(Node node) {
        node.setOpacity(0);
        node.setTranslateY(-20);

        FadeTransition fadeTransition = new FadeTransition(POST_APPEAR_DURATION, node);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);

        TranslateTransition translateTransition = new TranslateTransition(POST_APPEAR_DURATION, node);
        translateTransition.setFromY(-20);
        translateTransition.setToY(0);

        fadeTransition.play();
        translateTransition.play();
    }

    private Node buildAnnouncementCard(Announcement announcement, boolean featured) {
        VBox card = new VBox(10);
        card.getStyleClass().add("announcement-card");
        if (featured) {
            card.getStyleClass().add("featured");
        }

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);
        Label badge = new Label(featured ? "A la une" : "Annonce");
        badge.getStyleClass().add("announcement-badge");
        if (featured) {
            badge.getStyleClass().add("featured");
        }
        Label date = new Label(formatFrontDate(announcement.getCreatedAt()));
        date.getStyleClass().add("announcement-date");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topRow.getChildren().addAll(badge, spacer, date);

        Label title = new Label(announcement.getDisplayTitle());
        title.getStyleClass().add("announcement-title");
        title.setWrapText(true);

        String content = trimToNull(announcement.getContent());
        if (content != null) {
            Label contentLabel = new Label(content);
            contentLabel.getStyleClass().add("announcement-excerpt");
            contentLabel.setWrapText(true);
            card.getChildren().addAll(topRow, title, contentLabel);
        } else {
            card.getChildren().addAll(topRow, title);
        }

        String announcementMedia = resolveAnnouncementMedia(announcement);
        if (announcementMedia != null) {
            ImageView preview = buildPostImage(announcementMedia);
            if (preview != null) {
                preview.setFitWidth(320);
                preview.setFitHeight(170);
                StackPane mediaFrame = new StackPane(preview);
                mediaFrame.getStyleClass().add("announcement-media-frame");
                card.getChildren().add(mediaFrame);
            } else if (isVideoLike(announcementMedia)) {
                HBox mediaRow = new HBox(8);
                mediaRow.setAlignment(Pos.CENTER_LEFT);
                Label mediaHint = new Label("Media video");
                mediaHint.getStyleClass().add("announcement-excerpt");
                Button openMedia = new Button("Ouvrir media");
                openMedia.getStyleClass().add("announcement-link-button");
                openMedia.setOnAction(event -> openExternalMedia(announcementMedia));
                mediaRow.getChildren().addAll(mediaHint, openMedia);
                card.getChildren().add(mediaRow);
            }
        }

        String tagValue = trimToNull(announcement.getTag());
        String linkValue = trimToNull(announcement.getLink());
        if (tagValue != null || linkValue != null) {
            HBox footer = new HBox(8);
            footer.setAlignment(Pos.CENTER_LEFT);
            if (tagValue != null) {
                Label tag = new Label("#" + tagValue);
                tag.getStyleClass().add("announcement-tag");
                footer.getChildren().add(tag);
            }
            if (linkValue != null) {
                Hyperlink link = new Hyperlink("Ouvrir");
                link.getStyleClass().add("announcement-link");
                link.setOnAction(event -> openExternalMedia(linkValue));
                footer.getChildren().add(link);
            }
            card.getChildren().add(footer);
        }
        return card;
    }

    private String resolveAnnouncementMedia(Announcement announcement) {
        if (announcement == null) {
            return null;
        }
        String link = normalizeMediaPath(announcement.getLink());
        String file = normalizeMediaPath(announcement.getMediaFilename());
        String mediaType = trimToNull(announcement.getMediaType());
        if (link != null && ("image".equalsIgnoreCase(mediaType) || isImagePath(link) || isVideoLike(link))) {
            return link;
        }
        if (file != null && ("image".equalsIgnoreCase(mediaType) || isImagePath(file) || isVideoLike(file))) {
            return file;
        }
        return link != null ? link : file;
    }

    private Label buildPill(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("reaction-pill");
        return label;
    }

    private HBox createLikeMetric(FilActualite post) {
        boolean active = likedPostIds.contains(post.getId());

        Button actionButton = createFeedIconButton("icon-like", active ? "Retirer le j'aime" : "Aimer cette publication");
        if (active) {
            actionButton.getStyleClass().add("like-active");
        }
        actionButton.setOnAction(evt -> toggleLikePost(post));

        Label countLabel = new Label(buildLikeSummary(post));
        countLabel.getStyleClass().add("social-metric-label");
        countLabel.getStyleClass().add("social-metric-link");
        countLabel.setTooltip(new Tooltip("Voir les personnes qui ont aime"));
        countLabel.setOnMouseClicked(evt -> showLikeDetails(post));

        HBox metric = new HBox(8, actionButton, countLabel);
        metric.setAlignment(Pos.CENTER_LEFT);
        metric.getStyleClass().add("social-metric-group");
        if (active) {
            metric.getStyleClass().add("active");
        }
        return metric;
    }

    private HBox createSocialMetric(String iconClass, String textValue, String tooltipText, Runnable action) {
        Button actionButton = createFeedIconButton(iconClass, tooltipText);
        actionButton.setOnAction(evt -> action.run());

        Label countLabel = new Label(textValue);
        countLabel.getStyleClass().add("social-metric-label");
        countLabel.setOnMouseClicked(evt -> action.run());

        HBox metric = new HBox(8, actionButton, countLabel);
        metric.setAlignment(Pos.CENTER_LEFT);
        metric.getStyleClass().add("social-metric-group");
        metric.setOnMouseClicked(evt -> {
            if (!(evt.getTarget() instanceof Button)) {
                action.run();
            }
        });
        return metric;
    }

    private HBox createSaveMetric(FilActualite post) {
        boolean saved = savedPostIds.contains(post.getId());
        HBox metric = createSocialMetric(
                "icon-save",
                saved ? "Sauvegarde" : "Sauvegarder",
                saved ? "Retirer des sauvegardes" : "Sauvegarder cette publication",
                () -> toggleSavedPost(post)
        );
        if (saved) {
            metric.getStyleClass().add("active");
        }
        return metric;
    }

    private Button createFeedIconButton(String iconClass, String tooltipText) {
        Button button = new Button();
        button.getStyleClass().addAll("icon-button", iconClass);
        button.setTooltip(new Tooltip(tooltipText));
        button.setCache(true);
        button.setCacheHint(CacheHint.SPEED);
        return button;
    }

    private void toggleSavedPost(FilActualite post) {
        if (post == null || post.getId() <= 0) {
            return;
        }
        boolean saved = socialInteractionService.toggleSave(post.getId(), resolveCurrentUserId());
        if (!saved) {
            savedPostIds.remove(post.getId());
            showSuccess("Publication retiree des sauvegardes.");
        } else {
            savedPostIds.add(post.getId());
            showSuccess("Publication ajoutee aux sauvegardes.");
        }
        renderSavedPosts();
        updateProfilePanel();
        renderFrontFeed();
    }

    private void renderSavedPosts() {
        if (frontSavedBox == null) {
            return;
        }
        frontSavedBox.getChildren().clear();
        List<FilActualite> savedPosts = new ArrayList<>();
        for (FilActualite post : posts) {
            if (savedPostIds.contains(post.getId())) {
                savedPosts.add(post);
            }
        }
        if (savedPosts.isEmpty()) {
            frontSavedBox.getChildren().add(buildEmptyState(
                    "Aucune publication sauvegardee",
                    "Les publications marquees Sauvegarder apparaitront ici."
            ));
            return;
        }
        for (FilActualite post : savedPosts) {
            frontSavedBox.getChildren().add(buildPostCard(post, false));
        }
    }

    private void renderTrendPanel() {
        if (frontTrendingBox == null) {
            return;
        }
        frontTrendingBox.getChildren().clear();
        List<Map.Entry<String, Integer>> trends = buildTrendEntries();
        if (trends.isEmpty()) {
            frontTrendingBox.getChildren().add(buildEmptyState(
                    "Aucune tendance",
                    "Les tendances apparaitront ici des que le fil contiendra plus de contenu."
            ));
            return;
        }
        for (Map.Entry<String, Integer> entry : trends) {
            HBox row = new HBox(10);
            row.getStyleClass().add("trend-row");
            Label tokenLabel = new Label("#" + entry.getKey());
            tokenLabel.getStyleClass().add("trend-text");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label scoreLabel = new Label(String.valueOf(entry.getValue()));
            scoreLabel.getStyleClass().add("trend-badge");
            row.getChildren().addAll(tokenLabel, spacer, scoreLabel);
            frontTrendingBox.getChildren().add(row);
        }
    }

    private List<Map.Entry<String, Integer>> buildTrendEntries() {
        Map<String, Integer> counts = new HashMap<>();
        for (FilActualite post : posts) {
            for (String token : extractTrendTokens(post)) {
                counts.merge(token, 1, Integer::sum);
            }
        }
        for (Announcement announcement : announcements) {
            for (String token : extractTrendTokens(announcement)) {
                counts.merge(token, 1, Integer::sum);
            }
        }
        return counts.entrySet().stream()
                .sorted((left, right) -> {
                    int compare = Integer.compare(right.getValue(), left.getValue());
                    return compare != 0 ? compare : left.getKey().compareTo(right.getKey());
                })
                .limit(5)
                .toList();
    }

    private List<String> extractTrendTokens(FilActualite post) {
        List<String> tokens = new ArrayList<>();
        addTrendTokens(tokens, post.getContent());
        addTrendTokens(tokens, post.getEventTitle());
        addTrendTokens(tokens, post.getEventLocation());
        addTrendTokens(tokens, toFrontCategory(post));
        return tokens;
    }

    private List<String> extractTrendTokens(Announcement announcement) {
        List<String> tokens = new ArrayList<>();
        addTrendTokens(tokens, announcement.getTitle());
        addTrendTokens(tokens, announcement.getTag());
        addTrendTokens(tokens, announcement.getContent());
        return tokens;
    }

    private void addTrendTokens(List<String> target, String source) {
        if (source == null || source.isBlank()) {
            return;
        }
        for (String raw : source.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{Nd}]+")) {
            if (raw.length() < 4) {
                continue;
            }
            if (raw.equals("avec") || raw.equals("pour") || raw.equals("dans") || raw.equals("vous")) {
                continue;
            }
            target.add(raw);
        }
    }

    private void refreshBestPostingTime() {
        if (frontBestTimeLabel == null || frontBestTimeDescriptionLabel == null) {
            return;
        }
        if (posts.isEmpty()) {
            frontBestTimeLabel.setText("Heure ideale: --:--");
            frontBestTimeDescriptionLabel.setText("Nous afficherons ici un meilleur creneau apres les premieres publications.");
            return;
        }
        Map<Integer, Integer> hours = new HashMap<>();
        for (FilActualite post : posts) {
            if (post.getCreatedAt() != null) {
                hours.merge(post.getCreatedAt().getHour(), 1, Integer::sum);
            }
        }
        int bestHour = hours.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
        frontBestTimeLabel.setText(String.format("Heure ideale: %02d:00", bestHour));
        frontBestTimeDescriptionLabel.setText("Creneau estime d'apres les heures de publication les plus actives du fil actuel.");
    }

    private void handleFeedPrimaryAction(FilActualite item) {
        if (item == null) {
            return;
        }
        socialInteractionService.addShare(item.getId(), resolveCurrentUserId());
        shareCountsByPost.merge(item.getId(), 1, Integer::sum);
        if (item.isEvent()) {
            showSuccess("Evenement ajoute a votre suivi: " + defaultText(item.getEventTitle()));
        } else {
            showSuccess("Publication partagee depuis le compte " + currentUserDisplayName() + ": " + safeSummary(item));
        }
        renderFrontFeed();
    }

    private String toFrontCategory(FilActualite post) {
        if (post == null) {
            return "General";
        }
        if (post.isEvent()) {
            return "Tournois";
        }
        if (contains(post, "equipe") || contains(post, "team") || contains(post, "club")) {
            return "Equipes";
        }
        if (contains(post, "joueur") || contains(post, "player") || contains(post, "mvp")) {
            return "Joueurs";
        }
        if (resolveEmbeddedMediaUrl(post) != null) {
            return "Media";
        }
        return "General";
    }

    private String buildInteractionSummary(FilActualite post) {
        int commentCount = commentsByPost.getOrDefault(post.getId(), List.of()).size();
        return commentCount + (commentCount > 1 ? " commentaires" : " commentaire");
    }

    private String buildLikeSummary(FilActualite post) {
        int likeCount = likeCountsByPost.getOrDefault(post.getId(), 0);
        return likeCount + (likeCount > 1 ? " j'aime" : " j'aime");
    }

    private String buildShareSummary(FilActualite post) {
        int shareCount = shareCountsByPost.getOrDefault(post.getId(), 0);
        return shareCount + (shareCount > 1 ? " partages" : " partage");
    }

    private void showLikeDetails(FilActualite post) {
        if (post == null || post.getId() <= 0) {
            return;
        }
        List<Integer> likerIds = socialInteractionService.getLikeUserIds(post.getId());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("J'aime");
        alert.setHeaderText(buildLikeSummary(post));
        if (likerIds.isEmpty()) {
            alert.setContentText("Personne n'a aime cette publication pour le moment.");
        } else {
            String likers = likerIds.stream()
                    .map(this::resolveAuthorName)
                    .distinct()
                    .reduce((left, right) -> left + "\n" + right)
                    .orElse("Aucun");
            alert.setContentText(likers);
        }
        alert.showAndWait();
    }

    private void toggleLikePost(FilActualite post) {
        try {
            if (post == null || post.getId() <= 0) {
                return;
            }
            boolean liked = socialInteractionService.toggleLike(post.getId(), resolveCurrentUserId());
            if (liked) {
                likedPostIds.add(post.getId());
                likeCountsByPost.merge(post.getId(), 1, Integer::sum);
                showSuccess("Vous aimez cette publication.");
            } else {
                likedPostIds.remove(post.getId());
                likeCountsByPost.computeIfPresent(post.getId(), (key, value) -> Math.max(0, value - 1));
                showSuccess("J'aime retire.");
            }
            refreshSocialState();
            updateProfilePanel();
            renderFrontFeed();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void submitInlineComment(int postId, String text) {
        try {
            String content = trimToNull(text);
            if (content == null) {
                throw new IllegalArgumentException("Le commentaire est obligatoire.");
            }
            Commentaire comment = new Commentaire();
            comment.setContent(content);
            comment.setAuthorId(resolveCurrentUserId());
            comment.setPostId(postId);
            comment.setCreatedAt(LocalDateTime.now());
            commentaireService.addEntity(comment);
            refreshAll();
            showSuccess("Commentaire ajoute.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private enum NotificationTarget {
        POST,
        COMMENT,
        ANNOUNCEMENT,
        MESSENGER,
        STREAMING
    }

    private record AppNotification(
            String key,
            String title,
            String body,
            LocalDateTime createdAt,
            NotificationTarget target,
            int targetId
    ) {}

    private final class NotificationCell extends ListCell<AppNotification> {
        @Override
        protected void updateItem(AppNotification item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                getStyleClass().remove("unread");
                return;
            }

            VBox card = new VBox(4);
            card.getStyleClass().add("notification-card");

            Label title = new Label(item.title());
            title.getStyleClass().add("notification-title");
            title.setWrapText(true);

            Label body = new Label(defaultText(item.body()));
            body.getStyleClass().add("notification-body");
            body.setWrapText(true);

            Label time = new Label(formatFrontDate(item.createdAt()));
            time.getStyleClass().add("notification-time");

            card.getChildren().addAll(title, body, time);
            setGraphic(card);
            setText(null);

            boolean unread = !readNotificationKeys.contains(item.key());
            if (unread) {
                if (!getStyleClass().contains("unread")) {
                    getStyleClass().add("unread");
                }
            } else {
                getStyleClass().remove("unread");
            }
        }
    }

    private final class FeedCell extends ListCell<FilActualite> {
        @Override
        protected void updateItem(FilActualite item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            VBox card = new VBox(16);
            card.getStyleClass().add("post-card");
            if (item.isEvent()) {
                card.setStyle("-fx-border-color: rgba(226,200,55,0.70);");
            }

            HBox header = new HBox(14);
            header.setAlignment(Pos.CENTER_LEFT);

            Circle avatar = new Circle(28, Color.web("#7ed0ff"));
            Label avatarLabel = new Label(item.getAuthorId() == null ? "A" : String.valueOf(item.getAuthorId()));
            avatarLabel.getStyleClass().add("avatar-text");
            StackPane avatarPane = new StackPane(avatar, avatarLabel);
            avatarPane.setPadding(new Insets(2));

            VBox titleBox = new VBox(4);
            Label nameLabel = new Label(resolveAuthorName(item.getAuthorId()));
            nameLabel.getStyleClass().add("post-author");
            Label metaLabel = new Label(formatFrontDate(item.getCreatedAt()));
            metaLabel.getStyleClass().add("post-meta");
            titleBox.getChildren().addAll(nameLabel, metaLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button floating = new Button("?");
            floating.getStyleClass().add("icon-btn");
            floating.setContentDisplay(ContentDisplay.CENTER);

            header.getChildren().addAll(avatarPane, titleBox, spacer, floating);

            HBox actions = new HBox(10);
            actions.getChildren().addAll(
                    buildPill("Afficher resume IA"),
                    buildPill("Original"),
                    buildPill("EN"),
                    buildPill("FR"),
                    buildPill("AR")
            );
            VBox topPanel = new VBox(14);
            topPanel.getStyleClass().add("post-top-panel");
            topPanel.getChildren().addAll(header, actions);
            card.getChildren().add(topPanel);

            String embeddedMediaUrl = extractEmbeddedMediaUrl(item.getContent());
            String displayContent = stripEmbeddedMediaUrl(item.getContent());
            String imageSource = trimToNull(item.getImagePath());
            String videoSource = trimToNull(item.getVideoUrl());
            if (imageSource == null && videoSource == null && embeddedMediaUrl != null) {
                if (isImagePath(embeddedMediaUrl)) {
                    imageSource = embeddedMediaUrl;
                } else if (isVideoLike(embeddedMediaUrl)) {
                    videoSource = embeddedMediaUrl;
                }
            }

            Node mediaNode = null;
            ImageView imageView = buildPostImage(imageSource);
            if (imageView != null) {
                mediaNode = imageView;
            } else if (videoSource != null) {
                mediaNode = buildVideoPreview(videoSource);
            }

            if (mediaNode != null) {
                StackPane mediaFrame = new StackPane(mediaNode);
                mediaFrame.getStyleClass().add("post-media-frame");
                card.getChildren().add(mediaFrame);
            }

            if (displayContent != null) {
                VBox bodyPanel = new VBox(10);
                bodyPanel.getStyleClass().add("post-body-panel");
                Label contentLabel = new Label(displayContent);
                contentLabel.setWrapText(true);
                contentLabel.getStyleClass().add("post-content");
                bodyPanel.getChildren().add(contentLabel);
                card.getChildren().add(bodyPanel);
            }

            if (item.isEvent()) {
                VBox eventBox = new VBox(8);
                eventBox.getStyleClass().add("event-box");
                Label eventDate = new Label(item.getEventDate() == null ? "Date a confirmer" : item.getEventDate().format(FRONT_DATE_FORMAT));
                eventDate.getStyleClass().add("event-title");
                Label eventTitle = new Label(defaultText(item.getEventTitle()));
                eventTitle.getStyleClass().add("event-title");
                Label eventLocation = new Label(defaultText(item.getEventLocation()));
                eventLocation.getStyleClass().add("event-details");
                eventBox.getChildren().addAll(eventDate, eventTitle, eventLocation);
                card.getChildren().add(eventBox);
            }

            VBox commentsWrap = new VBox(8);
            commentsWrap.getStyleClass().add("comments-wrap");
            Label commentsTitle = new Label("Commentaires");
            commentsTitle.getStyleClass().add("panel-title");
            commentsWrap.getChildren().add(commentsTitle);

            List<Commentaire> postComments = commentsByPost.getOrDefault(item.getId(), List.of());
            if (postComments.isEmpty()) {
                Label emptyLabel = new Label("Aucun commentaire pour le moment.");
                emptyLabel.getStyleClass().add("panel-text");
                commentsWrap.getChildren().add(emptyLabel);
            } else {
                for (Commentaire comment : postComments) {
                    VBox commentBox = new VBox(4);
                    commentBox.getStyleClass().add("comment-box");
                    Label meta = new Label(resolveAuthorName(comment.getAuthorId()) + " â€¢ " + formatFrontDate(comment.getCreatedAt()));
                    meta.getStyleClass().add("comment-meta");
                    Label text = new Label(comment.getDisplayContent());
                    text.getStyleClass().add("comment-text");
                    text.setWrapText(true);
                    commentBox.getChildren().addAll(meta, text);
                    commentsWrap.getChildren().add(commentBox);
                }
            }

            HBox commentInputRow = new HBox(10);
            commentInputRow.getStyleClass().add("comment-input-row");
            TextField commentInput = new TextField();
            commentInput.setPromptText("Ecrire un commentaire...");
            commentInput.getStyleClass().add("comment-input");
            Button sendBtn = new Button("Commenter");
            sendBtn.getStyleClass().add("hero-button");
            sendBtn.getStyleClass().add("primary");
            sendBtn.setOnAction(evt -> submitInlineComment(item.getId(), commentInput.getText()));
            commentInputRow.getChildren().addAll(commentInput, sendBtn);
            HBox.setHgrow(commentInput, Priority.ALWAYS);
            commentsWrap.getChildren().add(commentInputRow);

            card.getChildren().add(commentsWrap);
            setGraphic(card);
        }

        private Label buildPill(String text) {
            Label label = new Label(text);
            label.getStyleClass().add("reaction-pill");
            return label;
        }
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

            VBox card = new VBox(8);
            card.getStyleClass().add("post-card");

            Label title = new Label(item.getDisplayTitle());
            title.getStyleClass().add("post-author");

            Label meta = new Label(formatTableDate(item.getCreatedAt()) + "  |  " + defaultText(item.getTag()));
            meta.getStyleClass().add("post-meta");

            card.getChildren().addAll(title, meta);
            setGraphic(card);
        }
    }

    private final class CommentCell extends ListCell<Commentaire> {
        @Override
        protected void updateItem(Commentaire item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            VBox card = new VBox(8);
            card.getStyleClass().add("post-card");

            Label title = new Label("Post #" + item.getPostId() + " â€¢ Auteur #" + item.getAuthorId());
            title.getStyleClass().add("post-author");

            Label meta = new Label(formatTableDate(item.getCreatedAt()));
            meta.getStyleClass().add("post-meta");

            Label content = new Label(item.getDisplayContent());
            content.getStyleClass().add("post-content");
            content.setWrapText(true);

            card.getChildren().addAll(title, meta, content);
            setGraphic(card);
        }
    }

    private final class ConversationPreviewCell extends ListCell<ConversationPreview> {
        @Override
        protected void updateItem(ConversationPreview item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            HBox row = new HBox(10);
            row.getStyleClass().add("messenger-conversation-row");
            if (item.getUnreadCount() > 0) {
                row.getStyleClass().add("unread");
            }

            StackPane avatar = buildMessengerAvatarNode(item.getPeerAvatarLabel(), 38);

            VBox body = new VBox(4);
            body.setAlignment(Pos.CENTER_LEFT);
            body.setFillWidth(true);
            Label name = new Label(item.getPeerDisplayName());
            name.getStyleClass().add("messenger-conversation-name");
            name.setMaxWidth(Double.MAX_VALUE);
            name.setTextOverrun(OverrunStyle.ELLIPSIS);
            Label preview = new Label(defaultText(item.getLastMessage()));
            preview.getStyleClass().add("messenger-conversation-preview");
            preview.setWrapText(false);
            preview.setMaxWidth(Double.MAX_VALUE);
            preview.setTextOverrun(OverrunStyle.ELLIPSIS);
            body.getChildren().addAll(name, preview);
            HBox.setHgrow(body, Priority.ALWAYS);

            VBox right = new VBox(6);
            right.setAlignment(Pos.CENTER_RIGHT);
            Label time = new Label(item.getLastMessageAt() == null ? "--" : formatFrontDate(item.getLastMessageAt()));
            time.getStyleClass().add("messenger-time-label");
            time.setMinWidth(92);
            right.getChildren().add(time);
            if (item.getUnreadCount() > 0) {
                Label badge = new Label(String.valueOf(item.getUnreadCount()));
                badge.getStyleClass().add("messenger-mini-unread");
                right.getChildren().add(badge);
            }

            row.getChildren().addAll(avatar, body, right);
            setGraphic(row);
        }
    }

    private final class UserProfileCell extends ListCell<UserProfile> {
        @Override
        protected void updateItem(UserProfile item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                return;
            }
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            StackPane avatar = buildMessengerAvatarNode(item.getAvatarLabel(), 34);

            VBox body = new VBox(2);
            Label name = new Label(item.getDisplayName());
            name.getStyleClass().add("messenger-conversation-name");
            Label role = new Label(item.getRole());
            role.getStyleClass().add("messenger-conversation-preview");
            body.getChildren().addAll(name, role);
            row.getChildren().addAll(avatar, body);
            setGraphic(row);
        }
    }
}

