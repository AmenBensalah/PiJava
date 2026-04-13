package edu.esportify.controllers;

import edu.esportify.entities.Announcement;
import edu.esportify.entities.Commentaire;
import edu.esportify.entities.FilActualite;
import edu.esportify.services.AnnouncementService;
import edu.esportify.services.CommentaireService;
import edu.esportify.services.FilActualiteService;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FilActualiteController {

    private static final String FILTER_ALL = "tout";
    private static final String FILTER_TEAMS = "equipes";
    private static final String FILTER_PLAYERS = "joueurs";
    private static final String FILTER_TOURNAMENTS = "tournois";
    private static final String FILTER_MEDIA = "media";
    private static final String PUBLIC_ROOT = "C:/Users/bouzi/Desktop/PI_DEV_ESPORTIFY/public";
    private static final DateTimeFormatter TABLE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FRONT_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.FRENCH);

    private final FilActualiteService service = new FilActualiteService();
    private final AnnouncementService announcementService = new AnnouncementService();
    private final CommentaireService commentaireService = new CommentaireService();
    private final ObservableList<FilActualite> posts = FXCollections.observableArrayList();
    private final ObservableList<FilActualite> frontFeedItems = FXCollections.observableArrayList();
    private final ObservableList<Announcement> announcements = FXCollections.observableArrayList();
    private final ObservableList<Commentaire> commentaires = FXCollections.observableArrayList();
    private final ObservableList<Announcement> frontAnnouncementsItems = FXCollections.observableArrayList();
    private final Map<Integer, List<Commentaire>> commentsByPost = new HashMap<>();
    private final ObservableList<String> savedItems = FXCollections.observableArrayList();
    private final ObservableList<String> profileItems = FXCollections.observableArrayList();

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
    @FXML private VBox postsView;
    @FXML private VBox announcementsView;
    @FXML private VBox commentsView;
    @FXML private VBox aiView;
    @FXML private Label totalPostsLabel;
    @FXML private Label eventsLabel;
    @FXML private Label mediaLabel;
    @FXML private Label latestPostLabel;
    @FXML private TextField searchField;
    @FXML private TextArea contentArea;
    @FXML private TextField imageField;
    @FXML private TextField videoField;
    @FXML private CheckBox eventCheckBox;
    @FXML private TextField eventTitleField;
    @FXML private TextField eventDateField;
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
    @FXML private VBox frontSavedView;
    @FXML private VBox frontProfileView;
    @FXML private TextArea frontContentArea;
    @FXML private TextField frontImageField;
    @FXML private TextField frontVideoField;
    @FXML private CheckBox frontEventCheckBox;
    @FXML private TextField frontEventTitleField;
    @FXML private TextField frontEventDateField;
    @FXML private TextField frontEventLocationField;
    @FXML private TextField frontMaxParticipantsField;
    @FXML private TextField frontAuthorIdField;
    @FXML private Button filterAllButton;
    @FXML private Button filterTeamsButton;
    @FXML private Button filterPlayersButton;
    @FXML private Button filterTournamentsButton;
    @FXML private Button filterMediaButton;
    @FXML private VBox frontFeedBox;
    @FXML private VBox frontAnnouncementsBox;
    @FXML private ListView<String> frontSavedList;
    @FXML private ListView<String> frontProfileList;
    @FXML private Label statusLabel;

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

    private boolean adminCollapsed = false;
    private boolean frontCollapsed = false;

    @FXML
    private void initialize() {
        configureTable();
        configureAnnouncementTable();
        configureCommentTable();
        configureFeedLists();
        configureStaticPanels();
        refreshAll();
        applySidebarState(adminSidebar, false);
        applySidebarState(frontSidebar, false);
        showBackOffice();
        showBackView(dashboardView, dashboardButton);
        showFrontView(frontFeedView, frontFeedButton);
        applyFrontFilter(FILTER_ALL, filterAllButton);
    }

    @FXML private void handleShowDashboard() { showBackOffice(); showBackView(dashboardView, dashboardButton); }
    @FXML private void handleShowPosts() { showBackOffice(); showBackView(postsView, postsButton); }
    @FXML private void handleShowAnnouncements() { showBackOffice(); showBackView(announcementsView, announcementsButton); }
    @FXML private void handleShowComments() { showBackOffice(); showBackView(commentsView, commentsButton); }
    @FXML private void handleShowAi() { showBackOffice(); showBackView(aiView, aiButton); }
    @FXML private void handleShowFrontOffice() { showFrontOffice(); showFrontView(frontFeedView, frontFeedButton); }
    @FXML private void handleShowBackOffice() { showBackOffice(); showBackView(postsView, postsButton); }
    @FXML private void handleShowFrontFeed() { showFrontOffice(); showFrontView(frontFeedView, frontFeedButton); }
    @FXML private void handleShowFrontSaved() { showFrontOffice(); showFrontView(frontSavedView, frontSavedButton); }
    @FXML private void handleShowFrontProfile() { showFrontOffice(); showFrontView(frontProfileView, frontProfileButton); }
    @FXML private void handleFrontEquipes() { showFrontOffice(); markFrontActive(frontEquipesButton); showInfo("Equipes"); }
    @FXML private void handleFrontTournois() { showFrontOffice(); markFrontActive(frontTournoisButton); showInfo("Tournois"); }
    @FXML private void handleFrontBoutique() { showFrontOffice(); markFrontActive(frontBoutiqueButton); showInfo("Boutique"); }
    @FXML private void handleFrontCommandes() { showFrontOffice(); markFrontActive(frontCommandesButton); showInfo("Commandes"); }
    @FXML private void handleAdminEquipes() { showBackOffice(); markAdminActive(adminEquipesButton); showInfo("Gestion Equipes"); }
    @FXML private void handleAdminTournoi() { showBackOffice(); markAdminActive(adminTournoiButton); showInfo("Gestion Tournoi"); }
    @FXML private void handleAdminBoutique() { showBackOffice(); markAdminActive(adminBoutiqueButton); showInfo("Gestion Boutique"); }
    @FXML private void handleAdminComptes() { showBackOffice(); markAdminActive(adminComptesButton); showInfo("Gestion Comptes"); }
    @FXML private void handleFilterAll() { applyFrontFilter(FILTER_ALL, filterAllButton); }
    @FXML private void handleFilterTeams() { applyFrontFilter(FILTER_TEAMS, filterTeamsButton); }
    @FXML private void handleFilterPlayers() { applyFrontFilter(FILTER_PLAYERS, filterPlayersButton); }
    @FXML private void handleFilterTournaments() { applyFrontFilter(FILTER_TOURNAMENTS, filterTournamentsButton); }
    @FXML private void handleFilterMedia() { applyFrontFilter(FILTER_MEDIA, filterMediaButton); }
    @FXML private void handleToggleAdminMenu() { adminCollapsed = !adminCollapsed; animateSidebar(adminSidebar, adminCollapsed); }
    @FXML private void handleToggleFrontMenu() { frontCollapsed = !frontCollapsed; animateSidebar(frontSidebar, frontCollapsed); }
    @FXML private void handleToggleAdminFilGroup() { toggleAdminFilSubmenu(); }
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
        try {
            FilActualite post = buildFrontForm();
            service.addEntity(post);
            refreshAll();
            clearFrontComposer();
            showFrontOffice();
            showFrontView(frontFeedView, frontFeedButton);
            applyFrontFilter(currentFilter, resolveFilterButton(currentFilter));
            showSuccess("Publication partagee dans le fil d'actualite.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }


    private void refreshAll() {
        try {
            posts.setAll(service.getData());
            postsTable.setItems(posts);
            announcements.setAll(announcementService.getData());
            if (announcementsTable != null) {
                announcementsTable.setItems(announcements);
            }
            commentaires.setAll(commentaireService.getData());
            if (commentsTable != null) {
                commentsTable.setItems(commentaires);
            }
            rebuildCommentsIndex();
            updateDashboard();
            updateProfilePanel();
            applyFrontFilter(currentFilter, resolveFilterButton(currentFilter));
            frontAnnouncementsItems.setAll(announcements);
            renderFrontAnnouncements();
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateDashboard() {
        totalPostsLabel.setText(String.valueOf(posts.size()));
        long events = posts.stream().filter(FilActualite::isEvent).count();
        long mediaPosts = posts.stream().filter(post -> post.getImagePath() != null || post.getVideoUrl() != null).count();
        eventsLabel.setText(String.valueOf(events));
        mediaLabel.setText(String.valueOf(mediaPosts));
        latestPostLabel.setText(posts.isEmpty() ? "Aucune publication pour le moment." : safeSummary(posts.get(0)));
    }

    private void updateProfilePanel() {
        profileItems.setAll(
                "Publications visibles: " + posts.size(),
                "Evenements actifs: " + posts.stream().filter(FilActualite::isEvent).count(),
                "Posts avec media: " + posts.stream().filter(post -> post.getImagePath() != null || post.getVideoUrl() != null).count(),
                "Derniere activite: " + (posts.isEmpty() ? "Aucune" : formatFrontDate(posts.get(0).getCreatedAt()))
        );
    }

    private void configureStaticPanels() {
        savedItems.setAll(
                "Commande #194 - validation en attente",
                "Commande #181 - expedition prevue",
                "Commande #160 - archivee"
        );
    }

    private void configureTable() {
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()));
        contentColumn.setCellValueFactory(data -> new SimpleStringProperty(safeSummary(data.getValue())));
        mediaColumn.setCellValueFactory(data -> new SimpleStringProperty(buildMediaSummary(data.getValue())));
        eventColumn.setCellValueFactory(data -> new SimpleStringProperty(buildEventSummary(data.getValue())));
        authorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthorId() == null ? "-" : "#" + data.getValue().getAuthorId()));
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
        commentAuthorColumn.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getAuthorId()));
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
        frontSavedList.setItems(savedItems);
        frontProfileList.setItems(profileItems);
    }

    private FilActualite buildBackForm() {
        FilActualite post = new FilActualite();
        post.setContent(trimToNull(contentArea.getText()));
        post.setImagePath(trimToNull(imageField.getText()));
        post.setVideoUrl(trimToNull(videoField.getText()));
        post.setEvent(eventCheckBox.isSelected());
        post.setEventTitle(trimToNull(eventTitleField.getText()));
        post.setEventDate(parseDateTime(eventDateField.getText()));
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
        post.setEvent(frontEventCheckBox.isSelected());
        post.setEventTitle(trimToNull(frontEventTitleField.getText()));
        post.setEventDate(parseDateTime(frontEventDateField.getText()));
        post.setEventLocation(trimToNull(frontEventLocationField.getText()));
        post.setMaxParticipants(parseInteger(frontMaxParticipantsField.getText()));
        post.setAuthorId(parseInteger(frontAuthorIdField.getText()));
        post.setCreatedAt(LocalDateTime.now());
        post.setMediaFilename(extractMediaFilename(post));
        return post;
    }

    private void fillBackForm(FilActualite post) {
        contentArea.setText(defaultText(post.getContent()));
        imageField.setText(defaultText(post.getImagePath()));
        videoField.setText(defaultText(post.getVideoUrl()));
        eventCheckBox.setSelected(post.isEvent());
        eventTitleField.setText(defaultText(post.getEventTitle()));
        eventDateField.setText(post.getEventDate() == null ? "" : post.getEventDate().toLocalDate().toString());
        eventLocationField.setText(defaultText(post.getEventLocation()));
        maxParticipantsField.setText(post.getMaxParticipants() == null ? "" : String.valueOf(post.getMaxParticipants()));
        authorIdField.setText(post.getAuthorId() == null ? "" : String.valueOf(post.getAuthorId()));
    }

    private void clearBackForm() {
        selectedPost = null;
        postsTable.getSelectionModel().clearSelection();
        contentArea.clear();
        imageField.clear();
        videoField.clear();
        eventCheckBox.setSelected(false);
        eventTitleField.clear();
        eventDateField.clear();
        eventLocationField.clear();
        maxParticipantsField.clear();
        authorIdField.clear();
    }

    private Announcement buildAnnouncementForm() {
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
        announcementTitleField.setText(defaultText(announcement.getTitle()));
        announcementTagField.setText(defaultText(announcement.getTag()));
        announcementLinkField.setText(defaultText(announcement.getLink()));
        announcementMediaTypeField.setText(defaultText(announcement.getMediaType()));
        announcementMediaFilenameField.setText(defaultText(announcement.getMediaFilename()));
        announcementContentArea.setText(defaultText(announcement.getContent()));
    }

    private void clearAnnouncementForm() {
        selectedAnnouncement = null;
        announcementsTable.getSelectionModel().clearSelection();
        announcementTitleField.clear();
        announcementTagField.clear();
        announcementLinkField.clear();
        announcementMediaTypeField.clear();
        announcementMediaFilenameField.clear();
        announcementContentArea.clear();
    }

    private Commentaire buildCommentForm() {
        Commentaire comment = new Commentaire();
        comment.setContent(trimToNull(commentContentArea.getText()));
        comment.setAuthorId(parseRequiredInteger(commentAuthorIdField.getText(), "L'identifiant auteur est obligatoire."));
        comment.setPostId(parseRequiredInteger(commentPostIdField.getText(), "L'identifiant de la publication est obligatoire."));
        comment.setCreatedAt(selectedComment == null ? LocalDateTime.now() : selectedComment.getCreatedAt());
        return comment;
    }


    private void fillCommentForm(Commentaire comment) {
        commentContentArea.setText(defaultText(comment.getContent()));
        commentAuthorIdField.setText(String.valueOf(comment.getAuthorId()));
        commentPostIdField.setText(String.valueOf(comment.getPostId()));
    }

    private void clearCommentForm() {
        selectedComment = null;
        commentsTable.getSelectionModel().clearSelection();
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
        frontEventDateField.clear();
        frontEventLocationField.clear();
        frontMaxParticipantsField.clear();
        frontAuthorIdField.clear();
    }

    private void selectById(int id) {
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
        for (Commentaire comment : commentaires) {
            if (comment.getId() == id) {
                commentsTable.getSelectionModel().select(comment);
                commentsTable.scrollTo(comment);
                selectedComment = comment;
                return;
            }
        }
    }

    private void showBackOffice() {
        backOfficeShell.setVisible(true);
        backOfficeShell.setManaged(true);
        frontOfficeShell.setVisible(false);
        frontOfficeShell.setManaged(false);
        markActive(switchToBackButton, switchToBackButton, switchToFrontButton);
        markAdminActive(null);
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
        markActive(activeButton, dashboardButton, postsButton, announcementsButton, commentsButton, aiButton);
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

    private void toggleAdminFilSubmenu() {
        if (adminFilSubmenu == null) {
            return;
        }
        boolean show = !adminFilSubmenu.isVisible();
        adminFilSubmenu.setVisible(show);
        adminFilSubmenu.setManaged(show);
        if (adminFilChevron != null) {
            adminFilChevron.setText(show ? "▾" : "▸");
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
        markActive(activeButton, filterAllButton, filterTeamsButton, filterPlayersButton, filterTournamentsButton, filterMediaButton);
    }

    private List<FilActualite> filterPosts(String filterKey) {
        List<FilActualite> filtered = new ArrayList<>();
        for (FilActualite post : posts) {
            boolean matches = switch (filterKey) {
                case FILTER_TEAMS -> contains(post, "equipe") || contains(post, "team") || contains(post, "club");
                case FILTER_PLAYERS -> contains(post, "joueur") || contains(post, "player") || contains(post, "mvp");
                case FILTER_TOURNAMENTS -> post.isEvent() || contains(post, "tournoi") || contains(post, "cup");
                case FILTER_MEDIA -> post.getImagePath() != null || post.getVideoUrl() != null;
                default -> true;
            };
            if (matches) {
                filtered.add(post);
            }
        }
        return filtered;
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

    private LocalDateTime parseDateTime(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        try {
            return LocalDate.parse(trimmed).atStartOfDay();
        } catch (Exception ex) {
            throw new IllegalArgumentException("La date doit etre au format AAAA-MM-JJ.");
        }
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
        return post.getDisplayTitle();
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

    private void showSuccess(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().remove("status-error");
        if (!statusLabel.getStyleClass().contains("status-success")) {
            statusLabel.getStyleClass().add("status-success");
        }
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().remove("status-success");
        if (!statusLabel.getStyleClass().contains("status-error")) {
            statusLabel.getStyleClass().add("status-error");
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Operation impossible");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String label) {
        if (statusLabel != null) {
            statusLabel.setText(label + " : section a venir.");
        }
    }

    private void renderFrontFeed() {
        if (frontFeedBox == null) {
            return;
        }
        frontFeedBox.getChildren().clear();
        for (FilActualite post : frontFeedItems) {
            frontFeedBox.getChildren().add(buildPostCard(post));
        }
    }

    private void renderFrontAnnouncements() {
        if (frontAnnouncementsBox == null) {
            return;
        }
        frontAnnouncementsBox.getChildren().clear();
        for (Announcement announcement : frontAnnouncementsItems) {
            frontAnnouncementsBox.getChildren().add(buildAnnouncementCard(announcement));
        }
    }

    private Node buildPostCard(FilActualite item) {
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
        Label nameLabel = new Label(item.getAuthorId() == null ? "Vous" : "ilyes");
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

        if (item.getContent() != null) {
            Label contentLabel = new Label(item.getContent());
            contentLabel.setWrapText(true);
            contentLabel.getStyleClass().add("post-content");
            card.getChildren().addAll(header, contentLabel);
        } else {
            card.getChildren().add(header);
        }

        HBox actions = new HBox(10);
        actions.getChildren().addAll(
                buildPill("Afficher resume IA"),
                buildPill("Original"),
                buildPill("EN"),
                buildPill("FR"),
                buildPill("AR")
        );
        card.getChildren().add(actions);

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

        ImageView imageView = buildPostImage(item.getImagePath());
        if (imageView != null) {
            card.getChildren().add(imageView);
        }

        if (item.getVideoUrl() != null) {
            Label video = new Label(item.getVideoUrl());
            video.getStyleClass().add("media-preview");
            card.getChildren().add(video);
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
                Label meta = new Label("Auteur #" + comment.getAuthorId() + " • " + formatFrontDate(comment.getCreatedAt()));
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
        return card;
    }

    private Node buildAnnouncementCard(Announcement announcement) {
        VBox card = new VBox(6);
        card.getStyleClass().add("side-inner-card");
        Label title = new Label(announcement.getDisplayTitle());
        title.getStyleClass().add("side-announce-title");
        Label meta = new Label(defaultText(announcement.getTag()) + " • " + formatFrontDate(announcement.getCreatedAt()));
        meta.getStyleClass().add("panel-text");
        card.getChildren().addAll(title, meta);
        return card;
    }

    private Label buildPill(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("reaction-pill");
        return label;
    }

    private void submitInlineComment(int postId, String text) {
        try {
            String content = trimToNull(text);
            if (content == null) {
                throw new IllegalArgumentException("Le commentaire est obligatoire.");
            }
            int authorId = parseRequiredInteger(frontAuthorIdField.getText(), "L'identifiant auteur est obligatoire.");
            Commentaire comment = new Commentaire();
            comment.setContent(content);
            comment.setAuthorId(authorId);
            comment.setPostId(postId);
            comment.setCreatedAt(LocalDateTime.now());
            commentaireService.addEntity(comment);
            refreshAll();
            showSuccess("Commentaire ajoute.");
        } catch (RuntimeException ex) {
            showError(ex.getMessage());
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
            Label nameLabel = new Label(item.getAuthorId() == null ? "Vous" : "ilyes");
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

            if (item.getContent() != null) {
                Label contentLabel = new Label(item.getContent());
                contentLabel.setWrapText(true);
                contentLabel.getStyleClass().add("post-content");
                card.getChildren().addAll(header, contentLabel);
            } else {
                card.getChildren().add(header);
            }

            HBox actions = new HBox(10);
            actions.getChildren().addAll(
                    buildPill("Afficher resume IA"),
                    buildPill("Original"),
                    buildPill("EN"),
                    buildPill("FR"),
                    buildPill("AR")
            );
            card.getChildren().add(actions);

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

            ImageView imageView = buildPostImage(item.getImagePath());
            if (imageView != null) {
                card.getChildren().add(imageView);
            }

            if (item.getVideoUrl() != null) {
                Label video = new Label(item.getVideoUrl());
                video.getStyleClass().add("media-preview");
                card.getChildren().add(video);
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
                    Label meta = new Label("Auteur #" + comment.getAuthorId() + " • " + formatFrontDate(comment.getCreatedAt()));
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

            Label title = new Label("Post #" + item.getPostId() + " • Auteur #" + item.getAuthorId());
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
}
