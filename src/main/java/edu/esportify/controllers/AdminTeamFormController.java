package edu.esportify.controllers;

import edu.esportify.entities.Equipe;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.EquipeService;
import edu.projetJava.controllers.BackTeamsDashboardController;
import java.time.LocalDateTime;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AdminTeamFormController implements AdminContentController {
    private static final String DEFAULT_LOGO = "/images/default-team-logo.png";
    private static final String INVALID_STYLE_CLASS = "validation-error";
    private static final int TEAM_NAME_MIN = 3;
    private static final int TEAM_NAME_MAX = 50;
    private static final int TAG_MIN = 2;
    private static final int TAG_MAX = 5;
    private static final int LONG_TEXT_MIN = 10;
    private static final String[] CONTINENTS = {
            "Afrique",
            "Amerique",
            "Asie",
            "Europe",
            "Oceanie",
            "Antarctique"
    };

    private final EquipeService equipeService = new EquipeService();

    private AdminLayoutController parentController;
    private BackTeamsDashboardController legacyHost;
    private Equipe currentEquipe;

    @FXML private Label heroTitleLabel;
    @FXML private Label heroSubtitleLabel;
    @FXML private Label selectedIdLabel;
    @FXML private TextField nameField;
    @FXML private TextField tagField;
    @FXML private ComboBox<String> regionBox;
    @FXML private TextField classementField;
    @FXML private TextField maxMembersField;
    @FXML private TextField discordField;
    @FXML private TextField logoField;
    @FXML private ComboBox<String> visibilityBox;
    @FXML private ComboBox<String> statusBox;
    @FXML private TextArea descriptionArea;
    @FXML private Label infoLabel;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;
    @FXML private ImageView spotlightImageView;
    @FXML private Label spotlightTitleLabel;
    @FXML private Label spotlightTagLabel;
    @FXML private Label spotlightMetaLabel;
    @FXML private Label spotlightDescriptionLabel;

    @FXML
    private void initialize() {
        applyDarkFieldStyles();
        regionBox.getItems().setAll(CONTINENTS);
        regionBox.setValue("Europe");
        visibilityBox.getItems().setAll("Publique", "Privee");
        statusBox.getItems().setAll("Active", "Inactive");
        visibilityBox.setValue("Publique");
        statusBox.setValue("Active");
        registerPreviewBindings();
        updateSpotlight(null);
    }

    @Override
    public void init(AdminLayoutController parentController) {
        this.parentController = parentController;
        this.currentEquipe = AppSession.getInstance().getSelectedEquipe();

        boolean editMode = currentEquipe != null;
        heroTitleLabel.setText(editMode ? "MODIFIER L'EQUIPE" : "CREER UNE EQUIPE");
        heroSubtitleLabel.setText(editMode
                ? "Ajustez les informations du groupe avec une presentation propre au back-office."
                : "Ajoutez une nouvelle equipe avec les standards visuels de l'administration.");
        selectedIdLabel.setText(editMode && currentEquipe.getId() > 0 ? "#" + currentEquipe.getId() : "Nouveau");
        saveButton.setText(editMode ? "Enregistrer" : "Creer l'equipe");
        deleteButton.setVisible(editMode);
        deleteButton.setManaged(editMode);

        if (editMode) {
            loadEquipe(currentEquipe);
        } else {
            clearFields();
            refreshSpotlightFromInputs();
        }
    }

    public void setLegacyHost(BackTeamsDashboardController legacyHost) {
        this.legacyHost = legacyHost;
    }

    @FXML
    private void onBack() {
        AppSession.getInstance().setSelectedEquipe(null);
        navigateBackToTeams();
    }

    @FXML
    private void onNewTeam() {
        currentEquipe = null;
        AppSession.getInstance().setSelectedEquipe(null);
        selectedIdLabel.setText("Nouveau");
        heroTitleLabel.setText("CREER UNE EQUIPE");
        heroSubtitleLabel.setText("Ajoutez une nouvelle equipe avec les standards visuels de l'administration.");
        saveButton.setText("Creer l'equipe");
        deleteButton.setVisible(false);
        deleteButton.setManaged(false);
        clearFields();
        refreshSpotlightFromInputs();
        infoLabel.setText("Mode creation active.");
    }

    @FXML
    private void onSave() {
        clearValidationState();
        String name = safe(nameField.getText()).trim();
        String tag = safe(tagField.getText()).trim();
        String classement = safe(classementField.getText()).trim();
        String description = safe(descriptionArea.getText()).trim();
        String discord = safe(discordField.getText()).trim();
        String manager = resolveManagerUsername();

        ValidationResult validation = validateFields(name, tag, classement, description, discord, manager);
        if (validation != null) {
            showValidationError(validation);
            return;
        }

        int maxMembers;
        try {
            maxMembers = Integer.parseInt(safe(maxMembersField.getText()).trim());
        } catch (NumberFormatException e) {
            showValidationError(new ValidationResult("Max membres doit etre un nombre.", maxMembersField));
            return;
        }
        if (maxMembers <= 0) {
            showValidationError(new ValidationResult("Max membres doit etre superieur a 0.", maxMembersField));
            return;
        }

        Equipe equipe = currentEquipe == null ? new Equipe() : currentEquipe;
        if (equipe.getDateCreation() == null) {
            equipe.setDateCreation(LocalDateTime.now());
        }
        equipe.setNomEquipe(name);
        equipe.setTag(tag);
        equipe.setRegion(safe(regionBox.getValue()).trim());
        equipe.setClassement(classement);
        equipe.setManagerUsername(manager);
        equipe.setMaxMembers(maxMembers);
        equipe.setDiscordInviteUrl(discord);
        equipe.setLogo(safe(logoField.getText()).trim());
        equipe.setPrivate("Privee".equalsIgnoreCase(visibilityBox.getValue()));
        equipe.setActive("Active".equalsIgnoreCase(statusBox.getValue()));
        equipe.setDescription(description);

        if (currentEquipe == null || currentEquipe.getId() <= 0) {
            equipeService.addEntity(equipe);
        } else {
            equipeService.updateEntity(currentEquipe.getId(), equipe);
        }
        AppSession.getInstance().setSelectedEquipe(null);
        navigateBackToTeams();
    }

    private ValidationResult validateFields(String name, String tag, String classement, String description, String discord, String manager) {
        if (name.isBlank()) {
            return new ValidationResult("Le nom de l'equipe est obligatoire.", nameField);
        }
        if (name.length() < TEAM_NAME_MIN || name.length() > TEAM_NAME_MAX) {
            return new ValidationResult("Le nom de l'equipe doit contenir entre 3 et 50 caracteres.", nameField);
        }
        if (equipeService.existsByName(name, currentEquipe == null ? null : currentEquipe.getId())) {
            return new ValidationResult("Une equipe avec ce nom existe deja.", nameField);
        }
        if (tag.isBlank()) {
            return new ValidationResult("Le tag est obligatoire.", tagField);
        }
        if (tag.length() < TAG_MIN || tag.length() > TAG_MAX) {
            return new ValidationResult("Le tag doit contenir entre 2 et 5 caracteres.", tagField);
        }
        if (equipeService.existsByTag(tag, currentEquipe == null ? null : currentEquipe.getId())) {
            return new ValidationResult("Ce tag est deja utilise par une autre equipe.", tagField);
        }
        if (classement.isBlank()) {
            return new ValidationResult("Le classement est obligatoire.", classementField);
        }
        if (description.isBlank()) {
            return new ValidationResult("La description est obligatoire.", descriptionArea);
        }
        if (description.length() <= LONG_TEXT_MIN) {
            return new ValidationResult("La description doit contenir plus de 10 caracteres.", descriptionArea);
        }
        if (!discord.isBlank() && !isValidDiscordLink(discord)) {
            return new ValidationResult("Le lien Discord doit commencer par http://, https:// ou discord.gg/.", discordField);
        }
        if (currentEquipe == null && equipeService.managerHasAnotherTeam(manager, null)) {
            return new ValidationResult("Ce manager possede deja une equipe.", nameField);
        }
        return null;
    }

    @FXML
    private void onDelete() {
        if (currentEquipe == null) {
            return;
        }
        equipeService.deleteEntity(currentEquipe);
        AppSession.getInstance().setSelectedEquipe(null);
        navigateBackToTeams();
    }

    private void loadEquipe(Equipe equipe) {
        nameField.setText(safe(equipe.getNomEquipe()));
        tagField.setText(safe(equipe.getTag()));
        regionBox.setValue(resolveRegion(equipe.getRegion()));
        classementField.setText(safe(equipe.getClassement()));
        maxMembersField.setText(equipe.getMaxMembers() > 0 ? String.valueOf(equipe.getMaxMembers()) : "5");
        discordField.setText(safe(equipe.getDiscordInviteUrl()));
        logoField.setText(safe(equipe.getLogo()));
        visibilityBox.setValue(equipe.isPrivate() ? "Privee" : "Publique");
        statusBox.setValue(equipe.isActive() ? "Active" : "Inactive");
        descriptionArea.setText(safe(equipe.getDescription()));
        updateSpotlight(equipe);
        infoLabel.setText("Edition du groupe " + safe(equipe.getNomEquipe()) + ".");
    }

    private void clearFields() {
        nameField.clear();
        tagField.clear();
        regionBox.setValue("Europe");
        classementField.clear();
        maxMembersField.setText("5");
        discordField.clear();
        logoField.clear();
        visibilityBox.setValue("Publique");
        statusBox.setValue("Active");
        descriptionArea.clear();
    }

    private void registerPreviewBindings() {
        nameField.textProperty().addListener((obs, oldValue, newValue) -> refreshSpotlightFromInputs());
        tagField.textProperty().addListener((obs, oldValue, newValue) -> refreshSpotlightFromInputs());
        regionBox.valueProperty().addListener((obs, oldValue, newValue) -> refreshSpotlightFromInputs());
        classementField.textProperty().addListener((obs, oldValue, newValue) -> refreshSpotlightFromInputs());
        maxMembersField.textProperty().addListener((obs, oldValue, newValue) -> refreshSpotlightFromInputs());
        discordField.textProperty().addListener((obs, oldValue, newValue) -> refreshSpotlightFromInputs());
        logoField.textProperty().addListener((obs, oldValue, newValue) -> refreshSpotlightFromInputs());
        visibilityBox.valueProperty().addListener((obs, oldValue, newValue) -> refreshSpotlightFromInputs());
        statusBox.valueProperty().addListener((obs, oldValue, newValue) -> refreshSpotlightFromInputs());
        descriptionArea.textProperty().addListener((obs, oldValue, newValue) -> refreshSpotlightFromInputs());
    }

    private void refreshSpotlightFromInputs() {
        updateSpotlight(buildPreviewEquipe());
    }

    private Equipe buildPreviewEquipe() {
        String name = safe(nameField.getText()).trim();
        String tag = safe(tagField.getText()).trim();
        String region = safe(regionBox.getValue()).trim();
        String description = safe(descriptionArea.getText()).trim();
        String logo = safe(logoField.getText()).trim();
        String status = safe(statusBox.getValue()).trim();

        boolean empty = name.isBlank()
                && tag.isBlank()
                && region.isBlank()
                && description.isBlank()
                && logo.isBlank();
        if (empty) {
            return null;
        }

        Equipe preview = new Equipe();
        preview.setNomEquipe(name.isBlank() ? "Nouvelle equipe" : name);
        preview.setTag(tag.isBlank() ? "N/A" : tag);
        preview.setRegion(region.isBlank() ? "Europe" : region);
        preview.setDescription(description.isBlank()
                ? "Preparez une fiche equipe complete avec un rendu elegant et des donnees propres."
                : description);
        preview.setLogo(logo);
        preview.setActive(!"Inactive".equalsIgnoreCase(status));
        return preview;
    }

    private void updateSpotlight(Equipe equipe) {
        spotlightTitleLabel.setText(equipe == null ? "Nouvelle equipe" : safe(equipe.getNomEquipe()));
        spotlightTagLabel.setText(equipe == null ? "N/A" : safe(equipe.getTag()));
        spotlightMetaLabel.setText(equipe == null
                ? "Configuration admin"
                : safe(equipe.getRegion()) + " • " + (equipe.isActive() ? "Active" : "Inactive"));
        spotlightDescriptionLabel.setText(equipe == null
                ? "Preparez une fiche equipe complete avec un rendu elegant et des donnees propres."
                : safe(equipe.getDescription()));
        spotlightImageView.setImage(loadLogo(equipe == null ? null : equipe.getLogo()));
    }

    private String resolveManagerUsername() {
        if (currentEquipe != null) {
            String existingManager = safe(currentEquipe.getManagerUsername()).trim();
            if (!existingManager.isBlank()) {
                return existingManager;
            }
        }

        String sessionUsername = safe(AppSession.getInstance().getUsername()).trim();
        return sessionUsername.isBlank() ? "admin" : sessionUsername;
    }

    private void navigateBackToTeams() {
        if (parentController != null) {
            parentController.showTeams();
        } else if (legacyHost != null) {
            legacyHost.showTeams();
        }
    }

    private Image loadLogo(String path) {
        try {
            if (path != null && !path.isBlank()) {
                return new Image(path, true);
            }
        } catch (IllegalArgumentException ignored) {
        }
        return new Image(String.valueOf(getClass().getResource(DEFAULT_LOGO)));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String resolveRegion(String region) {
        String safeRegion = safe(region).trim();
        for (String continent : CONTINENTS) {
            if (continent.equalsIgnoreCase(safeRegion)) {
                return continent;
            }
        }
        return "Europe";
    }

    private void applyDarkFieldStyles() {
        String fieldStyle = String.join("",
                "-fx-background-color: linear-gradient(to bottom, rgba(255,255,255,0.08), rgba(255,255,255,0.03)),",
                "linear-gradient(to right, rgba(11,18,33,0.98), rgba(14,20,39,0.96));",
                "-fx-background-insets: 0;",
                "-fx-background-radius: 18;",
                "-fx-border-radius: 18;",
                "-fx-border-color: rgba(110,191,255,0.18);",
                "-fx-text-fill: #f5f9ff;",
                "-fx-prompt-text-fill: #90a3bf;",
                "-fx-highlight-fill: rgba(86,199,255,0.34);",
                "-fx-highlight-text-fill: #ffffff;",
                "-fx-control-inner-background: rgba(11,18,33,0.98);",
                "-fx-text-box-border: rgba(110,191,255,0.18);",
                "-fx-focus-color: transparent;",
                "-fx-faint-focus-color: transparent;",
                "-fx-padding: 0 16;"
        );

        String comboStyle = String.join("",
                "-fx-background-color: linear-gradient(to bottom, rgba(255,255,255,0.08), rgba(255,255,255,0.03)),",
                "linear-gradient(to right, rgba(11,18,33,0.98), rgba(14,20,39,0.96));",
                "-fx-background-insets: 0;",
                "-fx-background-radius: 18;",
                "-fx-border-radius: 18;",
                "-fx-border-color: rgba(110,191,255,0.18);",
                "-fx-text-fill: #f5f9ff;",
                "-fx-prompt-text-fill: #90a3bf;",
                "-fx-control-inner-background: rgba(11,18,33,0.98);",
                "-fx-mark-color: #a9d9ff;",
                "-fx-focus-color: transparent;",
                "-fx-faint-focus-color: transparent;",
                "-fx-padding: 0 16;"
        );

        String areaStyle = String.join("",
                "-fx-background-color: linear-gradient(to bottom, rgba(255,255,255,0.04), rgba(255,255,255,0.02)),",
                "linear-gradient(to bottom right, rgba(14,19,34,0.98), rgba(20,24,40,0.96));",
                "-fx-background-insets: 0;",
                "-fx-background-radius: 22;",
                "-fx-border-radius: 22;",
                "-fx-border-color: rgba(101,192,255,0.16);",
                "-fx-text-fill: #f5f9ff;",
                "-fx-prompt-text-fill: #93a5bf;",
                "-fx-highlight-fill: rgba(86,199,255,0.34);",
                "-fx-highlight-text-fill: #ffffff;",
                "-fx-control-inner-background: rgba(14,19,34,0.98);",
                "-fx-focus-color: transparent;",
                "-fx-faint-focus-color: transparent;",
                "-fx-padding: 12 14;"
        );

        applyStyle(nameField, fieldStyle);
        applyStyle(tagField, fieldStyle);
        applyStyle(classementField, fieldStyle);
        applyStyle(maxMembersField, fieldStyle);
        applyStyle(discordField, fieldStyle);
        applyStyle(logoField, fieldStyle);
        applyStyle(regionBox, comboStyle);
        applyStyle(visibilityBox, comboStyle);
        applyStyle(statusBox, comboStyle);
        applyStyle(descriptionArea, areaStyle);
    }

    private void applyStyle(TextInputControl control, String style) {
        if (control != null) {
            control.setStyle(style);
        }
    }

    private void applyStyle(ComboBox<?> control, String style) {
        if (control != null) {
            control.setStyle(style);
        }
    }

    private void clearValidationState() {
        clearInvalidStyle(nameField, tagField, classementField, maxMembersField, discordField, logoField, descriptionArea, regionBox, visibilityBox, statusBox);
        infoLabel.setText("");
    }

    private void showValidationError(ValidationResult validation) {
        if (validation == null) {
            return;
        }
        if (validation.control != null && !validation.control.getStyleClass().contains(INVALID_STYLE_CLASS)) {
            validation.control.getStyleClass().add(INVALID_STYLE_CLASS);
        }
        infoLabel.setText(validation.message);
    }

    private void clearInvalidStyle(Control... controls) {
        for (Control control : controls) {
            if (control != null) {
                control.getStyleClass().remove(INVALID_STYLE_CLASS);
            }
        }
    }

    private boolean isValidDiscordLink(String value) {
        String lower = value.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://") || lower.startsWith("discord.gg/");
    }

    private record ValidationResult(String message, Control control) {
    }
}
