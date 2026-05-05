package edu.esportify.controllers;

import edu.esportify.entities.User;
import edu.esportify.entities.UserRole;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.UserService;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminAccountsController implements AdminContentController {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UserService userService = new UserService();

    @FXML private VBox searchShell;
    @FXML private HBox tableToolbar;
    @FXML private TextField keywordField;
    @FXML private Button searchButton;
    @FXML private Button resetButton;
    @FXML private Button refreshButton;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> firstNameColumn;
    @FXML private TableColumn<User, String> emailColumn;
    @FXML private TableColumn<User, String> phoneColumn;
    @FXML private TableColumn<User, String> roleColumn;
    @FXML private TableColumn<User, String> statusColumn;
    @FXML private TableColumn<User, String> createdAtColumn;
    @FXML private TableColumn<User, Void> actionsColumn;
    @FXML private Label infoLabel;

    @Override
    public void init(AdminLayoutController parentController) {
        configure();
        refreshTable();
    }

    private void configure() {
        ensureStyleClass(searchShell, "admin-search-shell", "admin-premium-search-shell");
        ensureStyleClass(tableToolbar, "admin-table-toolbar");
        ensureStyleClass(keywordField, "field", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(searchButton, "admin-glow-button");
        ensureStyleClass(resetButton, "admin-ghost-button");
        ensureStyleClass(refreshButton, "admin-ghost-button");

        if (!usersTable.getStyleClass().contains("admin-teams-table")) {
            usersTable.getStyleClass().add("admin-teams-table");
        }
        usersTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        usersTable.setPlaceholder(new Label("Aucun utilisateur disponible."));

        idColumn.setCellValueFactory(data -> new SimpleStringProperty("#" + data.getValue().getId()));
        idColumn.setCellFactory(param -> new AlignedTextCell(Pos.CENTER_LEFT, "admin-id-cell"));
        usernameColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getUsername())));
        usernameColumn.setCellFactory(param -> new IdentityCell());
        firstNameColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getFirstName())));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getEmail())));
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(safe(data.getValue().getPhoneNumber())));
        roleColumn.setCellValueFactory(data -> new SimpleStringProperty(displayRole(data.getValue().getRole())));
        roleColumn.setCellFactory(param -> new BadgeCell("admin-chip admin-chip-tag"));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isActive() ? "Actif" : "Inactif"));
        statusColumn.setCellFactory(param -> new BadgeCell("admin-chip admin-chip-status"));
        createdAtColumn.setCellValueFactory(data -> Bindings.createStringBinding(
                () -> data.getValue().getCreatedAt() == null ? "-" : data.getValue().getCreatedAt().format(DATE_FORMATTER)
        ));
        createdAtColumn.setCellFactory(param -> new AlignedTextCell(Pos.CENTER_LEFT, "admin-date-cell"));
        actionsColumn.setCellFactory(param -> new UserActionCell());

        keywordField.textProperty().addListener((obs, oldValue, newValue) -> applyFilter());
        updateInfo("Tableau des utilisateurs pret.");
    }

    @FXML
    private void onSearch() {
        applyFilter();
    }

    @FXML
    private void onResetFilters() {
        keywordField.clear();
        applyFilter();
        updateInfo("Filtre reinitialise.");
    }

    @FXML
    private void onRefresh() {
        refreshTable();
        updateInfo("Tableau des comptes actualise.");
    }

    private void refreshTable() {
        applyFilter();
    }

    private void applyFilter() {
        String keyword = safe(keywordField.getText()).trim().toLowerCase();
        List<User> users = userService.getData().stream()
                .filter(user -> keyword.isBlank() || matchesKeyword(user, keyword))
                .sorted(Comparator.comparingInt(User::getId).reversed())
                .toList();
        usersTable.setItems(FXCollections.observableArrayList(users));
    }

    private boolean matchesKeyword(User user, String keyword) {
        return safe(user.getUsername()).toLowerCase().contains(keyword)
                || safe(user.getFirstName()).toLowerCase().contains(keyword)
                || safe(user.getEmail()).toLowerCase().contains(keyword)
                || safe(user.getPhoneNumber()).toLowerCase().contains(keyword)
                || displayRole(user.getRole()).toLowerCase().contains(keyword);
    }

    private void changeRole(User user) {
        if (user == null) {
            return;
        }
        UserRole nextRole = nextRole(user.getRole());
        user.setRole(nextRole);
        userService.updateEntity(user.getId(), user);
        refreshTable();
        updateInfo("Role de " + safe(user.getUsername()) + " change en " + displayRole(nextRole) + ".");
    }

    private void toggleStatus(User user) {
        if (user == null) {
            return;
        }
        if (isCurrentAdmin(user)) {
            updateInfo("Le compte admin connecte ne peut pas etre desactive.");
            return;
        }
        user.setActive(!user.isActive());
        userService.updateEntity(user.getId(), user);
        refreshTable();
        updateInfo("Statut de " + safe(user.getUsername()) + " mis a jour.");
    }

    private void deleteUser(User user) {
        if (user == null) {
            return;
        }
        if (isCurrentAdmin(user)) {
            updateInfo("Le compte admin connecte ne peut pas etre supprime.");
            return;
        }
        userService.deleteEntity(user);
        refreshTable();
        updateInfo("Utilisateur " + safe(user.getUsername()) + " supprime.");
    }

    private void editUser(User user) {
        if (user == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modifier utilisateur");
        dialog.setHeaderText("Modifier les informations de " + safe(user.getUsername()));

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField usernameEditField = new TextField(safe(user.getUsername()));
        TextField firstNameEditField = new TextField(safe(user.getFirstName()));
        TextField emailEditField = new TextField(safe(user.getEmail()));
        TextField phoneEditField = new TextField(safe(user.getPhoneNumber()));
        Label validationLabel = new Label();
        validationLabel.getStyleClass().add("muted-label");
        validationLabel.setWrapText(true);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.add(new Label("Username"), 0, 0);
        form.add(usernameEditField, 1, 0);
        form.add(new Label("Prenom"), 0, 1);
        form.add(firstNameEditField, 1, 1);
        form.add(new Label("Email"), 0, 2);
        form.add(emailEditField, 1, 2);
        form.add(new Label("Telephone"), 0, 3);
        form.add(phoneEditField, 1, 3);
        form.add(validationLabel, 0, 4, 2, 1);

        dialog.getDialogPane().setContent(form);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String validationMessage = validateEditableUser(
                    user,
                    usernameEditField.getText(),
                    firstNameEditField.getText(),
                    emailEditField.getText(),
                    phoneEditField.getText()
            );
            if (!validationMessage.isBlank()) {
                validationLabel.setText(validationMessage);
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(result -> {
            if (result != saveButtonType) {
                return;
            }
            user.setUsername(safe(usernameEditField.getText()).trim());
            user.setFirstName(safe(firstNameEditField.getText()).trim());
            user.setEmail(safe(emailEditField.getText()).trim());
            user.setPhoneNumber(safe(phoneEditField.getText()).trim());
            userService.updateEntity(user.getId(), user);
            if (isCurrentAdmin(user)) {
                AppSession.getInstance().login(user);
            }
            refreshTable();
            updateInfo("Utilisateur " + safe(user.getUsername()) + " modifie avec succes.");
        });
    }

    private String validateEditableUser(User originalUser, String username, String firstName, String email, String phone) {
        String normalizedUsername = safe(username).trim();
        String normalizedFirstName = safe(firstName).trim();
        String normalizedEmail = safe(email).trim();
        String normalizedPhone = safe(phone).trim();

        if (normalizedUsername.length() < 3) {
            return "Le username doit contenir au moins 3 caracteres.";
        }
        if (normalizedFirstName.length() < 2) {
            return "Le prenom doit contenir au moins 2 caracteres.";
        }
        if (normalizedEmail.isBlank() || !normalizedEmail.contains("@")) {
            return "L'email est invalide.";
        }
        if (!normalizedPhone.isBlank() && !normalizedPhone.matches("[0-9+ ]{8,20}")) {
            return "Le numero de telephone est invalide.";
        }
        boolean usernameTaken = userService.getData().stream()
                .anyMatch(candidate -> candidate.getId() != originalUser.getId()
                        && safe(candidate.getUsername()).equalsIgnoreCase(normalizedUsername));
        if (usernameTaken) {
            return "Ce username est deja utilise.";
        }
        boolean emailTaken = userService.getData().stream()
                .anyMatch(candidate -> candidate.getId() != originalUser.getId()
                        && safe(candidate.getEmail()).equalsIgnoreCase(normalizedEmail));
        if (emailTaken) {
            return "Cet email est deja utilise.";
        }
        return "";
    }

    private UserRole nextRole(UserRole currentRole) {
        if (currentRole == UserRole.ADMIN) {
            return UserRole.MANAGER;
        }
        if (currentRole == UserRole.MANAGER) {
            return UserRole.USER;
        }
        return UserRole.ADMIN;
    }

    private boolean isCurrentAdmin(User user) {
        String currentUsername = safe(AppSession.getInstance().getUsername());
        String currentEmail = AppSession.getInstance().getCurrentUser() == null
                ? ""
                : safe(AppSession.getInstance().getCurrentUser().getEmail());
        return safe(user.getUsername()).equalsIgnoreCase(currentUsername)
                || safe(user.getEmail()).equalsIgnoreCase(currentEmail);
    }

    private String displayRole(UserRole role) {
        return role == null ? UserRole.USER.getDisplayName() : role.getDisplayName();
    }

    private void ensureStyleClass(javafx.scene.Node node, String... styleClasses) {
        if (node == null) {
            return;
        }
        for (String styleClass : styleClasses) {
            if (!node.getStyleClass().contains(styleClass)) {
                node.getStyleClass().add(styleClass);
            }
        }
    }

    private void updateInfo(String message) {
        if (infoLabel != null) {
            infoLabel.setText(message);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private final class UserActionCell extends TableCell<User, Void> {
        private final Button editButton = new Button("Modifier");
        private final Button roleButton = new Button("Changer role");
        private final Button statusButton = new Button("Activer");
        private final Button deleteButton = new Button("Supprimer");
        private final HBox box = new HBox(8, editButton, roleButton, statusButton, deleteButton);

        private UserActionCell() {
            editButton.getStyleClass().addAll("admin-inline-action", "is-info");
            roleButton.getStyleClass().addAll("admin-inline-action", "is-info");
            statusButton.getStyleClass().addAll("admin-inline-action", "is-neutral");
            deleteButton.getStyleClass().addAll("admin-inline-action", "is-danger");
            box.setAlignment(Pos.CENTER_LEFT);

            editButton.setOnAction(event -> {
                User user = getTableView().getItems().get(getIndex());
                editUser(user);
            });
            roleButton.setOnAction(event -> {
                User user = getTableView().getItems().get(getIndex());
                changeRole(user);
            });
            statusButton.setOnAction(event -> {
                User user = getTableView().getItems().get(getIndex());
                toggleStatus(user);
            });
            deleteButton.setOnAction(event -> {
                User user = getTableView().getItems().get(getIndex());
                deleteUser(user);
            });
        }

        @Override
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                setGraphic(null);
                return;
            }
            User user = getTableView().getItems().get(getIndex());
            statusButton.setText(user.isActive() ? "Desactiver" : "Activer");
            setGraphic(box);
        }
    }

    private final class BadgeCell extends TableCell<User, String> {
        private final String styleClasses;

        private BadgeCell(String styleClasses) {
            this.styleClasses = styleClasses;
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item.isBlank()) {
                setGraphic(null);
                return;
            }
            Label badge = new Label(item);
            badge.getStyleClass().addAll(styleClasses.split(" "));
            setGraphic(badge);
        }
    }

    private final class AlignedTextCell extends TableCell<User, String> {
        private final Pos alignment;
        private final String styleClass;

        private AlignedTextCell(Pos alignment, String styleClass) {
            this.alignment = alignment;
            this.styleClass = styleClass;
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null || item.isBlank()) {
                setText(null);
                setGraphic(null);
                return;
            }
            setAlignment(alignment);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
            setText(item);
            getStyleClass().removeAll("admin-id-cell", "admin-date-cell");
            if (!styleClass.isBlank() && !getStyleClass().contains(styleClass)) {
                getStyleClass().add(styleClass);
            }
        }
    }

    private final class IdentityCell extends TableCell<User, String> {
        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                setGraphic(null);
                return;
            }

            User user = getTableView().getItems().get(getIndex());
            Label initials = new Label(resolveInitials(user));
            initials.getStyleClass().addAll("admin-chip", "admin-chip-count");
            initials.setMinWidth(40);
            initials.setPrefWidth(40);
            initials.setAlignment(Pos.CENTER);

            Label userLabel = new Label(safe(user.getUsername()));
            userLabel.getStyleClass().add("admin-team-name");

            VBox content = new VBox(4, userLabel);
            HBox box = new HBox(12, initials, content);
            box.setAlignment(Pos.CENTER_LEFT);
            box.setFillHeight(true);
            setGraphic(box);
        }

        private String resolveInitials(User user) {
            String value = safe(user.getFirstName()).isBlank() ? safe(user.getUsername()) : safe(user.getFirstName());
            value = value.trim();
            if (value.isBlank()) {
                return "?";
            }
            String[] parts = value.split("\\s+|\\.|_|-");
            StringBuilder builder = new StringBuilder();
            for (String part : parts) {
                if (!part.isBlank()) {
                    builder.append(Character.toUpperCase(part.charAt(0)));
                }
                if (builder.length() == 2) {
                    break;
                }
            }
            return builder.isEmpty() ? value.substring(0, 1).toUpperCase() : builder.toString();
        }
    }
}
