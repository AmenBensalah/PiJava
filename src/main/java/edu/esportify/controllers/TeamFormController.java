package edu.esportify.controllers;

import edu.esportify.entities.Candidature;
import edu.esportify.entities.Equipe;
import edu.esportify.entities.Recrutement;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.CandidatureService;
import edu.esportify.services.EquipeService;
import edu.esportify.services.RecrutementService;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class TeamFormController implements ManagerContentController {
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
    private final RecrutementService recrutementService = new RecrutementService();
    private final CandidatureService candidatureService = new CandidatureService();

    private ManagerLayoutController parentController;
    private Equipe currentEquipe;
    private String selectedLogoPath;

    @FXML private Label heroTitleLabel;
    @FXML private VBox formShell;
    @FXML private TextField nomField;
    @FXML private TextField classementField;
    @FXML private TextField tagField;
    @FXML private ComboBox<String> regionBox;
    @FXML private TextField maxMembersField;
    @FXML private TextField logoField;
    @FXML private TextField discordField;
    @FXML private ComboBox<String> visibiliteBox;
    @FXML private ComboBox<String> statutBox;
    @FXML private TextArea descriptionArea;
    @FXML private Label infoLabel;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    @FXML private ImageView spotlightImageView;
    @FXML private Label spotlightTitleLabel;
    @FXML private Label spotlightTagLabel;
    @FXML private Label spotlightDescriptionLabel;

    @FXML
    private void initialize() {
        ensureStyleClass(formShell, "coord-card", "form-shell", "admin-premium-form-shell");
        ensureStyleClass(nomField, "field", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(classementField, "field", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(tagField, "field", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(regionBox, "dark-combo", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(maxMembersField, "field", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(logoField, "field", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(discordField, "field", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(visibiliteBox, "dark-combo", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(statutBox, "dark-combo", "admin-search-field", "admin-elevated-field");
        ensureStyleClass(descriptionArea, "text-area", "admin-editor-area");
        ensureStyleClass(cancelButton, "admin-ghost-button");
        ensureStyleClass(saveButton, "admin-glow-button");

        regionBox.getItems().setAll(CONTINENTS);
        regionBox.setValue("Europe");
        visibiliteBox.getItems().setAll("Publique", "Privee");
        statutBox.getItems().setAll("Active", "Inactive");
        visibiliteBox.setValue("Publique");
        statutBox.setValue("Active");
        spotlightImageView.setImage(loadImage("/images/esportify-card.jpg"));
    }

    @Override
    public void init(ManagerLayoutController parentController) {
        this.parentController = parentController;
        currentEquipe = AppSession.getInstance().getSelectedEquipe();
        boolean editMode = currentEquipe != null;
        heroTitleLabel.setText(editMode ? "Modifier mon equipe" : "Creer mon equipe");
        saveButton.setText(editMode ? "Enregistrer" : "Creer l'equipe");
        spotlightTitleLabel.setText(editMode ? safe(currentEquipe.getNomEquipe()) : "Aucun resultat");
        spotlightTagLabel.setText(editMode ? safe(currentEquipe.getTag()) : "N/A");
        spotlightDescriptionLabel.setText(editMode
                ? safe(currentEquipe.getDescription())
                : "Aucune equipe ne correspond aux filtres actifs.");

        if (editMode) {
            nomField.setText(safe(currentEquipe.getNomEquipe()));
            classementField.setText(safe(currentEquipe.getClassement()));
            tagField.setText(safe(currentEquipe.getTag()));
            regionBox.setValue(resolveRegion(currentEquipe.getRegion()));
            maxMembersField.setText(String.valueOf(currentEquipe.getMaxMembers()));
            logoField.setText(safe(currentEquipe.getLogo()));
            discordField.setText(safe(currentEquipe.getDiscordInviteUrl()));
            visibiliteBox.setValue(currentEquipe.isPrivate() ? "Privee" : "Publique");
            statutBox.setValue(currentEquipe.isActive() ? "Active" : "Inactive");
            descriptionArea.setText(safe(currentEquipe.getDescription()));
            selectedLogoPath = currentEquipe.getLogo();
        }
    }

    @FXML
    private void onUploadLogo() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir un logo");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        Window window = saveButton.getScene() == null ? null : saveButton.getScene().getWindow();
        File file = chooser.showOpenDialog(window);
        if (file != null) {
            selectedLogoPath = file.toURI().toString();
            logoField.setText(file.getName());
            spotlightImageView.setImage(new Image(selectedLogoPath));
        }
    }

    @FXML
    private void onCancel() {
        if (AppSession.getInstance().getSelectedEquipe() == null) {
            clearFields();
            infoLabel.setText("");
        } else {
            parentController.showTeamDashboard();
        }
    }

    @FXML
    private void onSave() {
        clearValidationState();
        String nom = safe(nomField.getText()).trim();
        String classement = safe(classementField.getText()).trim();
        String tag = safe(tagField.getText()).trim();
        String discord = safe(discordField.getText()).trim();
        String description = safe(descriptionArea.getText()).trim();

        ValidationResult validation = validateFields(nom, classement, tag, description, discord);
        if (validation != null) {
            showValidationError(validation);
            return;
        }

        int maxMembers;
        try {
            maxMembers = Integer.parseInt(safe(maxMembersField.getText()).trim());
        } catch (NumberFormatException e) {
            showValidationError(new ValidationResult("Max members doit etre un nombre.", maxMembersField));
            return;
        }
        if (maxMembers <= 0) {
            showValidationError(new ValidationResult("Max members doit etre superieur a 0.", maxMembersField));
            return;
        }

        Equipe equipe = currentEquipe == null ? new Equipe() : currentEquipe;
        equipe.setNomEquipe(nom);
        equipe.setClassement(classement);
        equipe.setTag(tag);
        equipe.setRegion(safe(regionBox.getValue()).trim());
        equipe.setMaxMembers(maxMembers);
        equipe.setLogo(selectedLogoPath != null && !selectedLogoPath.isBlank() ? selectedLogoPath : safe(logoField.getText()).trim());
        equipe.setDiscordInviteUrl(discord);
        equipe.setPrivate("Privee".equalsIgnoreCase(visibiliteBox.getValue()));
        equipe.setActive("Active".equalsIgnoreCase(statutBox.getValue()));
        equipe.setDescription(description);
        equipe.setManagerUsername(AppSession.getInstance().getUsername());

        if (currentEquipe == null || currentEquipe.getId() <= 0) {
            equipeService.addEntity(equipe);
            seedTeamData();
        } else {
            equipeService.updateEntity(currentEquipe.getId(), equipe);
        }

        infoLabel.setText("");
        parentController.refreshSelectedEquipe();
        parentController.showTeamDashboard();
    }

    private ValidationResult validateFields(String nom, String classement, String tag, String description, String discord) {
        if (nom.isBlank()) {
            return new ValidationResult("Le nom de l'equipe est obligatoire.", nomField);
        }
        if (nom.length() < TEAM_NAME_MIN || nom.length() > TEAM_NAME_MAX) {
            return new ValidationResult("Le nom de l'equipe doit contenir entre 3 et 50 caracteres.", nomField);
        }
        if (equipeService.existsByName(nom, currentEquipe == null ? null : currentEquipe.getId())) {
            return new ValidationResult("Une equipe avec ce nom existe deja.", nomField);
        }
        if (classement.isBlank()) {
            return new ValidationResult("Le classement est obligatoire.", classementField);
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
        if (description.isBlank()) {
            return new ValidationResult("La description est obligatoire.", descriptionArea);
        }
        if (description.length() <= LONG_TEXT_MIN) {
            return new ValidationResult("La description doit contenir plus de 10 caracteres.", descriptionArea);
        }
        if (!discord.isBlank() && !isValidDiscordLink(discord)) {
            return new ValidationResult("Le lien Discord doit commencer par http://, https:// ou discord.gg/.", discordField);
        }
        if (currentEquipe == null && equipeService.managerHasAnotherTeam(AppSession.getInstance().getUsername(), null)) {
            return new ValidationResult("Ce manager possede deja une equipe.", nomField);
        }
        return null;
    }

    private void seedTeamData() {
        Equipe created = equipeService.getByManagerUsername(AppSession.getInstance().getUsername());
        if (created == null) {
            return;
        }

        if (recrutementService.getByEquipe(created.getId()).isEmpty()) {
            Recrutement recrutement = new Recrutement(
                    "Roster principal",
                    "Nous cherchons un joueur motive pour completer le roster.",
                    "Ouvert",
                    created.getId()
            );
            recrutementService.addEntity(recrutement);
        }

        if (candidatureService.getByEquipe(created.getId()).isEmpty()) {
            Candidature candidature = new Candidature();
            candidature.setPseudoJoueur("Nova");
            candidature.setNiveau("Diamond");
            candidature.setRolePrefere("Support");
            candidature.setRegion(safe(created.getRegion()));
            candidature.setDisponibilite("Soirs");
            candidature.setMotivation("Je veux rejoindre un roster stable.");
            candidature.setEquipeId(created.getId());
            candidature.setStatut("En attente");
            candidature.setAccountUsername("nova.user");
            candidatureService.addEntity(candidature);
        }
    }

    private void clearFields() {
        nomField.clear();
        classementField.clear();
        tagField.clear();
        regionBox.setValue("Europe");
        maxMembersField.clear();
        logoField.clear();
        discordField.clear();
        descriptionArea.clear();
        visibiliteBox.setValue("Publique");
        statutBox.setValue("Active");
    }

    private Image loadImage(String path) {
        return new Image(String.valueOf(getClass().getResource(path)));
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

    private void clearValidationState() {
        clearInvalidStyle(nomField, classementField, tagField, maxMembersField, logoField, discordField, descriptionArea, regionBox, visibiliteBox, statutBox);
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
