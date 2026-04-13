package edu.PROJETPI;

import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.services.ServiceCommande;
import edu.PROJETPI.tools.AlertUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.SQLException;

public class EditCommandeStatusController {

    @FXML
    private Label commandeInfoLabel;
    @FXML
    private ComboBox<String> statutComboBox;

    private final ServiceCommande serviceCommande = new ServiceCommande();
    private Commande commande;
    private Runnable onSaved;

    @FXML
    private void initialize() {
        statutComboBox.setItems(FXCollections.observableArrayList("EN_ATTENTE", "VALIDEE", "PAYEE", "ANNULEE"));
    }

    public void setCommande(Commande commande) {
        this.commande = commande;
        commandeInfoLabel.setText(
                "Commande #" + commande.getId()
                        + " | Client " + safe(commande.getPrenom()) + " " + safe(commande.getNom())
                        + " | Statut actuel: " + safe(commande.getStatut())
        );
        statutComboBox.setValue(commande.getStatut() == null ? "EN_ATTENTE" : commande.getStatut());
    }

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    @FXML
    private void saveStatus() {
        if (commande == null) {
            AlertUtils.showError("Aucune commande selectionnee.");
            return;
        }

        try {
            commande.setStatut(statutComboBox.getValue() == null ? "EN_ATTENTE" : statutComboBox.getValue());
            serviceCommande.update(commande);
            if (onSaved != null) {
                onSaved.run();
            }
            AlertUtils.showSuccess("Statut de la commande modifie avec succes.");
            closeWindow();
        } catch (SQLException e) {
            AlertUtils.showError("Erreur lors de la mise a jour du statut : " + e.getMessage());
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) statutComboBox.getScene().getWindow();
        stage.close();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
