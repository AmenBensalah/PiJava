package edu.esportify.controllers;

import edu.esportify.entities.Candidature;
import edu.esportify.entities.Equipe;
import edu.esportify.entities.Recrutement;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class UserViewFactory {
    private UserViewFactory() {
    }

    public static VBox createUserTeamCard(Equipe equipe, Runnable onView, Runnable onApply) {
        ImageView imageView = new ImageView(resolveTeamImage(equipe));
        imageView.setFitWidth(220);
        imageView.setFitHeight(160);
        imageView.setPreserveRatio(false);

        Label name = new Label(value(equipe.getNomEquipe()));
        name.getStyleClass().add("section-title");
        Label description = createMuted(value(equipe.getDescription()));
        Label rank = createChip(value(equipe.getClassement()));
        Label status = createChip("Recrutement ferme");
        Label region = createChip(value(equipe.getRegion()));
        Label active = createChip(equipe.isActive() ? "Active" : "Inactive");

        Button viewButton = new Button("Voir profil");
        viewButton.getStyleClass().add("classic-button");
        viewButton.setOnAction(event -> onView.run());

        Button applyButton = new Button("Rejoindre equipe");
        applyButton.getStyleClass().add("classic-button");
        applyButton.setOnAction(event -> onApply.run());

        HBox chipsTop = new HBox(8, rank, status);
        HBox chipsBottom = new HBox(8, region, active);
        HBox actions = new HBox(8, viewButton, applyButton);

        VBox card = new VBox(12, imageView, name, chipsTop, description, chipsBottom, actions);
        card.setPrefWidth(250);
        card.getStyleClass().add("coord-card");
        return card;
    }

    public static HBox createRosterRow(String joueur, String role, String date) {
        Label joueurLabel = createSection(joueur);
        Label roleLabel = createChip(role);
        Label dateLabel = createSection(date);
        Button profil = new Button("Profil");
        profil.getStyleClass().add("classic-button");
        HBox row = new HBox(18, joueurLabel, roleLabel, dateLabel, profil);
        row.getStyleClass().add("recruitment-row");
        return row;
    }

    public static VBox createRecruitmentCard(Recrutement recrutement) {
        VBox card = new VBox(8, createSection(recrutement.getNomRec()), createMuted(recrutement.getDescription()), createChip(recrutement.getStatus()));
        card.getStyleClass().add("mini-card");
        return card;
    }

    public static VBox createUserApplicationCard(Candidature candidature, Runnable onEdit, Runnable onDelete) {
        Label title = createSection(value(candidature.getEquipeNom()));
        Label role = createMuted("Role: " + value(candidature.getRolePrefere()));
        Label level = createMuted("Niveau: " + value(candidature.getNiveau()));
        Label status = createChip(value(candidature.getStatut()));
        Button editButton = new Button("Modifier");
        editButton.getStyleClass().add("classic-button");
        editButton.setOnAction(event -> onEdit.run());

        Button deleteButton = new Button("Supprimer");
        deleteButton.getStyleClass().add("manager-delete-button");
        deleteButton.setOnAction(event -> onDelete.run());

        HBox actions = new HBox(10, editButton, deleteButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(8, title, role, level, status, actions);
        card.getStyleClass().add("coord-card");
        return card;
    }

    public static Label createMuted(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("muted-label");
        label.setWrapText(true);
        return label;
    }

    public static Label createChip(String text) {
        Label label = new Label(text == null || text.isBlank() ? "N/A" : text);
        label.getStyleClass().add("chip-active");
        return label;
    }

    public static Label createSection(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("card-title");
        return label;
    }

    public static Image resolveTeamImage(Equipe equipe) {
        String logo = equipe == null ? null : equipe.getLogo();
        if (logo != null && !logo.isBlank()) {
            try {
                return new Image(logo);
            } catch (Exception ignored) {
            }
        }
        return new Image(String.valueOf(UserViewFactory.class.getResource("/images/hero-team-reference.png")));
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }
}
