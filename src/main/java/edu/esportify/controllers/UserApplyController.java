package edu.esportify.controllers;

import edu.esportify.entities.Candidature;
import edu.esportify.entities.Equipe;
import edu.esportify.navigation.AppSession;
import edu.esportify.services.CandidatureService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

public class UserApplyController implements UserContentController {
    private static final String INVALID_STYLE_CLASS = "validation-error";
    private static final int LONG_TEXT_MIN = 10;

    private final CandidatureService candidatureService = new CandidatureService();
    private UserLayoutController parentController;
    private Equipe equipe;

    @FXML private Label heroTitleLabel;
    @FXML private TextField pseudoField;
    @FXML private ComboBox<String> niveauBox;
    @FXML private ComboBox<String> roleBox;
    @FXML private ComboBox<String> regionBox;
    @FXML private ComboBox<String> disponibiliteBox;
    @FXML private TextArea motivationArea;
    @FXML private ImageView previewImageView;
    @FXML private Label previewTagLabel;
    @FXML private Label previewDescriptionLabel;
    @FXML private Label infoLabel;

    @FXML
    private void initialize() {
        niveauBox.getItems().setAll("Debutant", "Intermediaire", "Diamond", "Master");
        roleBox.getItems().setAll("Flex", "Support", "DPS", "Tank");
        regionBox.getItems().setAll("Europe", "MENA", "NA");
        disponibiliteBox.getItems().setAll("Soir", "Week-end", "Journee");
        niveauBox.setValue("Intermediaire");
        roleBox.setValue("Flex");
        regionBox.setValue("Europe");
        disponibiliteBox.setValue("Soir");
    }

    @Override
    public void init(UserLayoutController parentController) {
        this.parentController = parentController;
        equipe = AppSession.getInstance().getSelectedEquipe();
        if (equipe == null) {
            parentController.showTeams();
            return;
        }
        heroTitleLabel.setText("Postuler a " + value(equipe.getNomEquipe()));
        previewImageView.setImage(UserViewFactory.resolveTeamImage(equipe));
        previewTagLabel.setText(value(equipe.getTag()));
        previewDescriptionLabel.setText(value(equipe.getDescription()));
        Candidature existing = candidatureService.getData().stream()
                .filter(item -> item.getEquipeId() == equipe.getId())
                .filter(item -> AppSession.getInstance().getUsername().equalsIgnoreCase(value(item.getAccountUsername())))
                .findFirst()
                .orElse(null);

        if (existing == null) {
            pseudoField.setText(AppSession.getInstance().getUsername());
            motivationArea.clear();
            infoLabel.setText("Complete le formulaire pour envoyer ta candidature.");
            return;
        }

        pseudoField.setText(value(existing.getPseudoJoueur()));
        niveauBox.setValue(value(existing.getNiveau()).isBlank() ? "Intermediaire" : existing.getNiveau());
        roleBox.setValue(value(existing.getRolePrefere()).isBlank() ? "Flex" : existing.getRolePrefere());
        regionBox.setValue(value(existing.getRegion()).isBlank() ? "Europe" : existing.getRegion());
        disponibiliteBox.setValue(value(existing.getDisponibilite()).isBlank() ? "Soir" : existing.getDisponibilite());
        motivationArea.setText(value(existing.getMotivation()));
        infoLabel.setText("Une candidature existe deja pour cette equipe. Tu peux la modifier.");
    }

    @FXML
    private void onCancel() {
        parentController.showTeamDetail(equipe);
    }

    @FXML
    private void onSubmit() {
        clearValidationState();
        String pseudo = value(pseudoField.getText()).trim();
        String motivation = value(motivationArea.getText()).trim();

        if (pseudo.isBlank()) {
            showValidationError("Le pseudo est obligatoire.", pseudoField);
            return;
        }
        if (motivation.isBlank()) {
            showValidationError("La motivation est obligatoire.", motivationArea);
            return;
        }
        if (motivation.length() <= LONG_TEXT_MIN) {
            showValidationError("La motivation doit contenir plus de 10 caracteres.", motivationArea);
            return;
        }
        Candidature existing = candidatureService.getData().stream()
                .filter(item -> item.getEquipeId() == equipe.getId())
                .filter(item -> AppSession.getInstance().getUsername().equalsIgnoreCase(value(item.getAccountUsername())))
                .findFirst()
                .orElse(null);
        Candidature accepted = candidatureService.getAcceptedForUser(AppSession.getInstance().getUsername());
        if (accepted != null && accepted.getEquipeId() != equipe.getId()) {
            showValidationError("Tu appartiens deja a une equipe. Quitte ton equipe actuelle avant de postuler ailleurs.", pseudoField);
            return;
        }
        if (existing == null && candidatureService.existsForUserAndEquipe(AppSession.getInstance().getUsername(), equipe.getId(), null)) {
            showValidationError("Une candidature existe deja pour cette equipe.", pseudoField);
            return;
        }
        Candidature candidature = existing == null ? new Candidature() : existing;

        candidature.setPseudoJoueur(pseudo);
        candidature.setNiveau(value(niveauBox.getValue()));
        candidature.setRolePrefere(value(roleBox.getValue()));
        candidature.setRegion(value(regionBox.getValue()));
        candidature.setDisponibilite(value(disponibiliteBox.getValue()));
        candidature.setMotivation(motivation);
        candidature.setEquipeId(equipe.getId());
        candidature.setEquipeNom(equipe.getNomEquipe());
        candidature.setAccountUsername(AppSession.getInstance().getUsername());
        candidature.setStatut("En attente");

        if (candidature.getId() > 0) {
            candidatureService.updateEntity(candidature.getId(), candidature);
        } else {
            candidatureService.addEntity(candidature);
        }
        parentController.showCandidatures();
    }

    private void clearValidationState() {
        clearInvalidStyle(pseudoField, niveauBox, roleBox, regionBox, disponibiliteBox, motivationArea);
        infoLabel.setText("");
    }

    private void showValidationError(String message, Control control) {
        if (control != null && !control.getStyleClass().contains(INVALID_STYLE_CLASS)) {
            control.getStyleClass().add(INVALID_STYLE_CLASS);
        }
        infoLabel.setText(message);
    }

    private void clearInvalidStyle(Control... controls) {
        for (Control control : controls) {
            if (control != null) {
                control.getStyleClass().remove(INVALID_STYLE_CLASS);
            }
        }
    }

    private String value(String value) {
        return value == null ? "" : value;
    }
}
