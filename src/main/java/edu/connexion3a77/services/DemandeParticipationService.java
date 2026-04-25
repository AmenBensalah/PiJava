package edu.connexion3a77.services;

import edu.connexion3a77.entities.DemandeParticipation;
import edu.connexion3a77.interfaces.IService;
import edu.connexion3a77.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DemandeParticipationService implements IService<DemandeParticipation> {
    private static final String TABLE_NAME = "demande_participation";

    public DemandeParticipationService() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String requete = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "tournoi_id INT NOT NULL, " +
                "description VARCHAR(255) NOT NULL, " +
                "niveau VARCHAR(100) NOT NULL, " +
                "statut VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE'" +
                ")";

        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            st.executeUpdate(requete);
            // If table already existed with unique key, drop it to allow multiple demandes per tournoi.
            try {
                st.executeUpdate("ALTER TABLE " + TABLE_NAME + " DROP INDEX uq_demande_tournoi");
            } catch (SQLException ignored) {
                // Index may not exist, ignore.
            }
            try {
                st.executeUpdate("ALTER TABLE " + TABLE_NAME + " ADD COLUMN statut VARCHAR(30) NOT NULL DEFAULT 'EN_ATTENTE'");
            } catch (SQLException ignored) {
                // Column may already exist, ignore.
            }
        } catch (SQLException e) {
            System.out.println("Erreur creation table demande_participation : " + e.getMessage());
        }
    }

    @Override
    public void addEntity(DemandeParticipation demandeParticipation) {
        if (!isDemandeValide(demandeParticipation)) {
            return;
        }

        if (existsSameDemande(demandeParticipation, null)) {
            System.out.println("Controle saisie: cette meme demande existe deja dans ce tournoi.");
            return;
        }

        String requete = "INSERT INTO " + TABLE_NAME + " (tournoi_id, description, niveau, statut) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, demandeParticipation.getTournoiId());
            pst.setString(2, demandeParticipation.getDescription());
            pst.setString(3, demandeParticipation.getNiveau());
            pst.setString(4, normalizedStatut(demandeParticipation.getStatut()));
            pst.executeUpdate();
            System.out.println("Demande de participation ajoutee avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void ajouter(DemandeParticipation demandeParticipation) {
        addEntity(demandeParticipation);
    }

    @Override
    public void deleteEntity(DemandeParticipation demandeParticipation) {
        String requete = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, demandeParticipation.getId());
            pst.executeUpdate();
            System.out.println("Demande de participation supprimee avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void supprimer(int id) {
        DemandeParticipation demandeParticipation = new DemandeParticipation();
        demandeParticipation.setId(id);
        deleteEntity(demandeParticipation);
    }

    @Override
    public void updateEntity(int id, DemandeParticipation demandeParticipation) {
        if (!isDemandeValide(demandeParticipation)) {
            return;
        }

        if (existsSameDemande(demandeParticipation, id)) {
            System.out.println("Controle saisie: cette meme demande existe deja dans ce tournoi.");
            return;
        }

        String requete = "UPDATE " + TABLE_NAME + " SET tournoi_id = ?, description = ?, niveau = ?, statut = ? WHERE id = ?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, demandeParticipation.getTournoiId());
            pst.setString(2, demandeParticipation.getDescription());
            pst.setString(3, demandeParticipation.getNiveau());
            pst.setString(4, normalizedStatut(demandeParticipation.getStatut()));
            pst.setInt(5, id);
            pst.executeUpdate();
            System.out.println("Demande de participation modifiee avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void modifier(DemandeParticipation demandeParticipation) {
        updateEntity(demandeParticipation.getId(), demandeParticipation);
    }

    @Override
    public List<DemandeParticipation> getData() {
        List<DemandeParticipation> data = new ArrayList<>();
        String requete = "SELECT * FROM " + TABLE_NAME;

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                DemandeParticipation demandeParticipation = new DemandeParticipation();
                demandeParticipation.setId(rs.getInt("id"));
                demandeParticipation.setTournoiId(rs.getInt("tournoi_id"));
                demandeParticipation.setDescription(rs.getString("description"));
                demandeParticipation.setNiveau(rs.getString("niveau"));
                demandeParticipation.setStatut(normalizedStatut(rs.getString("statut")));
                data.add(demandeParticipation);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }

    public List<DemandeParticipation> afficher() {
        return getData();
    }

    public List<DemandeParticipation> afficherParStatut(String statut) {
        String statutNormalise = normalizedStatut(statut);
        if (statutNormalise == null) {
            return getData();
        }

        List<DemandeParticipation> data = new ArrayList<>();
        String requete = "SELECT * FROM " + TABLE_NAME + " WHERE statut = ?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, statutNormalise);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                DemandeParticipation demandeParticipation = new DemandeParticipation();
                demandeParticipation.setId(rs.getInt("id"));
                demandeParticipation.setTournoiId(rs.getInt("tournoi_id"));
                demandeParticipation.setDescription(rs.getString("description"));
                demandeParticipation.setNiveau(rs.getString("niveau"));
                demandeParticipation.setStatut(normalizedStatut(rs.getString("statut")));
                data.add(demandeParticipation);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return data;
    }

    public void updateStatut(int id, String statut) {
        String statutNormalise = normalizedStatut(statut);
        if (statutNormalise == null) {
            System.out.println("Controle saisie: statut invalide.");
            return;
        }

        String requete = "UPDATE " + TABLE_NAME + " SET statut = ? WHERE id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, statutNormalise);
            pst.setInt(2, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private boolean isDemandeValide(DemandeParticipation demandeParticipation) {
        if (demandeParticipation == null) {
            System.out.println("Controle saisie: tous les champs doivent etre remplis.");
            return false;
        }

        if (demandeParticipation.getTournoiId() <= 0) {
            System.out.println("Controle saisie: tournoi_id est obligatoire.");
            return false;
        }

        if (demandeParticipation.getDescription() == null || demandeParticipation.getDescription().trim().isEmpty()) {
            System.out.println("Controle saisie: la description ne doit pas etre vide.");
            return false;
        }

        if (demandeParticipation.getNiveau() == null || demandeParticipation.getNiveau().trim().isEmpty()) {
            System.out.println("Controle saisie: le niveau ne doit pas etre vide.");
            return false;
        }

        if (normalizedStatut(demandeParticipation.getStatut()) == null) {
            System.out.println("Controle saisie: statut invalide.");
            return false;
        }

        return true;
    }

    private boolean existsSameDemande(DemandeParticipation demandeParticipation, Integer excludeId) {
        String requete = "SELECT id FROM " + TABLE_NAME +
                " WHERE tournoi_id = ? AND description = ? AND niveau = ?" +
                (excludeId != null ? " AND id <> ?" : "") +
                " LIMIT 1";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, demandeParticipation.getTournoiId());
            pst.setString(2, demandeParticipation.getDescription().trim());
            pst.setString(3, demandeParticipation.getNiveau().trim());
            if (excludeId != null) {
                pst.setInt(4, excludeId);
            }
            ResultSet rs = pst.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private String normalizedStatut(String statut) {
        if (statut == null || statut.trim().isEmpty()) {
            return DemandeParticipation.STATUT_EN_ATTENTE;
        }
        String normalized = statut.trim().toUpperCase();
        if (DemandeParticipation.STATUT_EN_ATTENTE.equals(normalized)
                || DemandeParticipation.STATUT_ACCEPTEE.equals(normalized)
                || DemandeParticipation.STATUT_REFUSEE.equals(normalized)) {
            return normalized;
        }
        return null;
    }

}
