package edu.ProjetPI.controllers;

import edu.ProjetPI.entities.User;
import edu.ProjetPI.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.event.ActionEvent;

import java.util.Comparator;
import java.util.Locale;

public class AdminDashboardController {

    private final UserService userService = new UserService();
    private final ObservableList<User> masterData = FXCollections.observableArrayList();
    private FilteredList<User> filteredUsers;
    private SortedList<User> sortedUsers;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label totalCountLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortByCombo;

    @FXML
    private ComboBox<String> sortDirectionCombo;

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableColumn<User, Integer> idColumn;

    @FXML
    private TableColumn<User, String> nameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> pseudoColumn;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, User> statusColumn;

    @FXML
    private TableColumn<User, User> warnColumn;

    @FXML
    private TableColumn<User, Void> editColumn;

    @FXML
    private TableColumn<User, Void> deleteColumn;

    private javafx.animation.Timeline autoRefreshTimeline;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        pseudoColumn.setCellValueFactory(new PropertyValueFactory<>("pseudo"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setCellFactory(column -> new TableCell<>() {
            private final Label roleLabel = new Label();
            private final StackPane wrapper = new StackPane(roleLabel);

            {
                wrapper.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                roleLabel.getStyleClass().removeAll("role-pill", "role-joueur", "role-manager", "role-admin");
                roleLabel.getStyleClass().add("role-pill");
                switch (item) {
                    case "ROLE_ADMIN" -> {
                        roleLabel.setText("Admin");
                        roleLabel.getStyleClass().add("role-admin");
                    }
                    case "ROLE_MANAGER" -> {
                        roleLabel.setText("Manager");
                        roleLabel.getStyleClass().add("role-manager");
                    }
                    default -> {
                        roleLabel.setText("Joueur");
                        roleLabel.getStyleClass().add("role-joueur");
                    }
                }
                setGraphic(wrapper);
            }
        });

        statusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            private final Label statusLabel = new Label();
            private final StackPane wrapper = new StackPane(statusLabel);

            {
                wrapper.setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                statusLabel.getStyleClass().removeAll("role-pill", "role-admin", "role-manager", "role-joueur");
                statusLabel.getStyleClass().add("role-pill");

                if (item.getWarningSentAt() != null) {
                    java.time.Duration diff = java.time.Duration.between(item.getWarningSentAt(), java.time.LocalDateTime.now());
                    if (diff.toMinutes() > 2) {
                        statusLabel.setText("Banni");
                        statusLabel.setStyle("-fx-background-color: rgba(255, 0, 0, 0.2); -fx-text-fill: #ff0000;");
                    } else {
                        statusLabel.setText("En Sursis");
                        statusLabel.setStyle("-fx-background-color: rgba(255, 165, 2, 0.2); -fx-text-fill: #ffa502;");
                    }
                } else {
                    boolean isActive = false;
                    if (item.getLastLogin() != null) {
                        java.time.Duration diff = java.time.Duration.between(item.getLastLogin(), java.time.LocalDateTime.now());
                        if (diff.toMinutes() <= 60) {
                            isActive = true;
                        }
                    }

                    if (isActive) {
                        statusLabel.setText("Actif");
                        statusLabel.setStyle("-fx-background-color: rgba(46, 213, 115, 0.2); -fx-text-fill: #2ed573;");
                    } else {
                        statusLabel.setText("Inactif");
                        statusLabel.setStyle("-fx-background-color: rgba(255, 71, 87, 0.2); -fx-text-fill: #ff4757;");
                    }
                }
                setGraphic(wrapper);
            }
        });

        warnColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue()));
        warnColumn.setCellFactory(column -> new TableCell<>() {
            private final Button actionButton = new Button();

            {
                actionButton.getStyleClass().add("action-edit-pill");
                actionButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (actionButton.getText().startsWith("Avertir")) {
                        handleWarnUser(user);
                    } else if (actionButton.getText().startsWith("Debannir") || actionButton.getText().startsWith("Annuler")) {
                        handleUnbanUser(user);
                    }
                });
            }

            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                User currentUser = DashboardSession.getCurrentUser();
                if (currentUser != null && currentUser.getId() == item.getId()) {
                    actionButton.setText("Avertir");
                    actionButton.setDisable(true);
                    actionButton.setStyle("-fx-background-color: rgba(200, 200, 200, 0.2); -fx-text-fill: #aaaaaa;");
                    setGraphic(actionButton);
                    return;
                }

                if (item.getWarningSentAt() != null) {
                    java.time.Duration diff = java.time.Duration.between(item.getWarningSentAt(), java.time.LocalDateTime.now());
                    if (diff.toMinutes() > 2) {
                        actionButton.setText("Debannir (" + diff.toMinutes() + "m)");
                        actionButton.setDisable(false);
                        actionButton.setStyle("-fx-background-color: rgba(46, 213, 115, 0.2); -fx-text-fill: #2ed573;");
                    } else {
                        actionButton.setText("Annuler (" + diff.toMinutes() + "m)");
                        actionButton.setDisable(false);
                        actionButton.setStyle("-fx-background-color: rgba(164, 176, 190, 0.2); -fx-text-fill: #a4b0be;");
                    }
                } else {
                    boolean isActive = false;
                    if (item.getLastLogin() != null) {
                        java.time.Duration diff = java.time.Duration.between(item.getLastLogin(), java.time.LocalDateTime.now());
                        if (diff.toMinutes() <= 60) {
                            isActive = true;
                        }
                    }

                    if (isActive) {
                        actionButton.setText("Avertir");
                        actionButton.setDisable(true);
                        actionButton.setStyle("-fx-background-color: rgba(200, 200, 200, 0.2); -fx-text-fill: #aaaaaa;");
                    } else {
                        actionButton.setText("Avertir");
                        actionButton.setDisable(false);
                        actionButton.setStyle("-fx-background-color: rgba(255, 165, 2, 0.2); -fx-text-fill: #ffa502;");
                    }
                }
                setGraphic(actionButton);
            }
        });

        editColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");

            {
                editButton.getStyleClass().add("action-edit-pill");
                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleOpenEditDialog(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : editButton);
            }
        });

        deleteColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("Supprimer");

            {
                deleteButton.getStyleClass().add("action-delete-pill");
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    userTable.getSelectionModel().select(user);
                    handleDelete();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        });

        if (DashboardSession.getCurrentUser() != null) {
            welcomeLabel.setText(DashboardSession.getCurrentUser().getFullName());
        }

        setupFilteringAndSorting();
        refreshTable();
    }

    @FXML
    public void handleRefresh() {
        refreshTable();
        messageLabel.setText("Table actualisee.");
    }

    @FXML
    public void handleOpenCreateDialog() {
        UserFormSession.prepareCreate();
        SceneManager.switchScene("/edu/ProjetPI/views/user-form.fxml", "Ajouter compte");
    }

    @FXML
    public void handleResetFilters() {
        searchField.clear();
        sortByCombo.getSelectionModel().select("ID");
        sortDirectionCombo.getSelectionModel().select("Ascendant");
        applyFiltersAndSort();
        messageLabel.setText("Filtres reinitialises.");
    }

    private void handleOpenEditDialog(User user) {
        UserFormSession.prepareEdit(user);
        SceneManager.switchScene("/edu/ProjetPI/views/user-form.fxml", "Modifier compte");
    }

    private void handleWarnUser(User user) {
        try {
            edu.ProjetPI.services.BrevoEmailService brevoService = new edu.ProjetPI.services.BrevoEmailService();
            brevoService.sendInactivityWarning(user.getEmail(), user.getPseudo());
            userService.setWarningSentAt(user.getId());
            messageLabel.setText("Avertissement envoye a " + user.getEmail() + ". Le compte a rebours est lance.");
            refreshTable();
        } catch (Exception e) {
            messageLabel.setText("Erreur d'envoi: " + e.getMessage());
        }
    }

    private void handleUnbanUser(User user) {
        try {
            userService.clearWarningSentAt(user.getId());
            messageLabel.setText("Le compte de " + user.getPseudo() + " a ete debanni.");
            refreshTable();
        } catch (Exception e) {
            messageLabel.setText("Erreur de debannissement: " + e.getMessage());
        }
    }

    @FXML
    public void handleDelete() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            messageLabel.setText("Select a user to delete.");
            return;
        }
        if (DashboardSession.getCurrentUser() != null && selectedUser.getId() == DashboardSession.getCurrentUser().getId()) {
            messageLabel.setText("Use Mon Profil if you want to delete your own account.");
            return;
        }

        try {
            userService.delete(selectedUser.getId());
            messageLabel.setText("User deleted successfully.");
            refreshTable();
        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    public void handleViewProfile() {
        SceneManager.switchScene("/edu/ProjetPI/views/profile.fxml", "Mon Profil");
    }

    @FXML
    public void goToFrontOffice(ActionEvent event) {
        SceneManager.switchScene("/ajoutProduit.fxml", "E-SPORTIFY : Boutique");
    }

    @FXML
    public void goToMailing(ActionEvent event) {
        SceneManager.switchScene("/backMailing.fxml", "Boutique Admin - Mailing");
    }

    @FXML
    public void goToCatalogue(ActionEvent event) {
        SceneManager.switchScene("/backListProduit.fxml", "Gestion des produits");
    }

    @FXML
    public void goToAdminCategorie(ActionEvent event) {
        SceneManager.switchScene("/backListCategorie.fxml", "Boutique Admin - Categories");
    }

    @FXML
    public void goToPayments(ActionEvent event) {
        edu.PROJETPI.AdminDashboardController.openOn(edu.PROJETPI.AdminDashboardController.InitialSection.PAIEMENTS);
        SceneManager.switchScene("/admin-dashboard-view.fxml", "Liste des paiements");
    }

    @FXML
    public void goToRevenuePrediction(ActionEvent event) {
        edu.PROJETPI.AdminDashboardController.openOn(edu.PROJETPI.AdminDashboardController.InitialSection.PREDICTION_CA);
        SceneManager.switchScene("/admin-dashboard-view.fxml", "Prediction chiffre d'affaires");
    }

    @FXML
    public void goToCommandes(ActionEvent event) {
        edu.PROJETPI.AdminDashboardController.openOn(edu.PROJETPI.AdminDashboardController.InitialSection.COMMANDES);
        SceneManager.switchScene("/admin-dashboard-view.fxml", "Liste des commandes");
    }

    @FXML
    public void handleLogout() {
        DashboardSession.clear();
        SceneManager.switchScene("/edu/ProjetPI/views/login.fxml", "Login");
    }

    private void refreshTable() {
        masterData.setAll(userService.getAll());
        applyFiltersAndSort();
    }

    private void setupFilteringAndSorting() {
        filteredUsers = new FilteredList<>(masterData, user -> true);
        sortedUsers = new SortedList<>(filteredUsers);
        userTable.setItems(sortedUsers);

        sortByCombo.setItems(FXCollections.observableArrayList("ID", "Nom", "Pseudo", "Email", "Role"));
        sortDirectionCombo.setItems(FXCollections.observableArrayList("Ascendant", "Descendant"));
        sortByCombo.getSelectionModel().select("ID");
        sortDirectionCombo.getSelectionModel().select("Ascendant");

        searchField.textProperty().addListener((obs, oldValue, newValue) -> applyFiltersAndSort());

        autoRefreshTimeline = new javafx.animation.Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(5), ev -> {
            userTable.refresh();
        }));
        autoRefreshTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        autoRefreshTimeline.play();

        sortByCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyFiltersAndSort());
        sortDirectionCombo.valueProperty().addListener((obs, oldValue, newValue) -> applyFiltersAndSort());
        filteredUsers.addListener((ListChangeListener<User>) change -> updateTotalCount());
    }

    private void applyFiltersAndSort() {
        if (filteredUsers == null || sortedUsers == null) {
            return;
        }

        String query = normalize(searchField.getText());
        filteredUsers.setPredicate(user -> matchesSearch(user, query));

        Comparator<User> comparator = buildComparator(sortByCombo.getValue());
        if ("Descendant".equals(sortDirectionCombo.getValue())) {
            comparator = comparator.reversed();
        }
        sortedUsers.setComparator(comparator);
        updateTotalCount();
    }

    private Comparator<User> buildComparator(String sortBy) {
        if (sortBy == null) {
            return Comparator.comparingInt(User::getId);
        }

        return switch (sortBy) {
            case "Nom" -> Comparator.comparing(user -> normalize(user.getFullName()));
            case "Pseudo" -> Comparator.comparing(user -> normalize(user.getPseudo()));
            case "Email" -> Comparator.comparing(user -> normalize(user.getEmail()));
            case "Role" -> Comparator.comparing(user -> normalize(user.getRole()));
            default -> Comparator.comparingInt(User::getId);
        };
    }

    private boolean matchesSearch(User user, String query) {
        if (query.isEmpty()) {
            return true;
        }

        String id = String.valueOf(user.getId());
        return id.contains(query)
                || normalize(user.getFullName()).contains(query)
                || normalize(user.getPseudo()).contains(query)
                || normalize(user.getEmail()).contains(query)
                || normalize(user.getRole()).contains(query)
                || normalize(formatRoleLabel(user.getRole())).contains(query);
    }

    private String formatRoleLabel(String role) {
        return switch (role) {
            case "ROLE_ADMIN" -> "Admin";
            case "ROLE_MANAGER" -> "Manager";
            default -> "Joueur";
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private void updateTotalCount() {
        totalCountLabel.setText("Total:" + filteredUsers.size());
    }

}
