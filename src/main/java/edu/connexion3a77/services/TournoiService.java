package edu.connexion3a77.services;

import edu.connexion3a77.entities.Tournoi;
import edu.connexion3a77.interfaces.IService;
import edu.connexion3a77.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TournoiService implements IService<Tournoi> {
    private static final String TABLE_NAME = "tournoi";

    public TournoiService() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String requete = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "nom_tournoi VARCHAR(255) NOT NULL, " +
                "type_tournoi VARCHAR(255) NOT NULL, " +
                "nom_jeu VARCHAR(255) NOT NULL, " +
                "date_debut DATE NOT NULL, " +
                "date_fin DATE NOT NULL, " +
                "nombre_participants INT NOT NULL, " +
                "cash_prize DOUBLE NOT NULL" +
                ")";

        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            st.executeUpdate(requete);
        } catch (SQLException e) {
            System.out.println("Erreur creation table tournoi : " + e.getMessage());
        }
    }

    @Override
    public void addEntity(Tournoi tournoi) {
        if (!isTournoiValide(tournoi)) {
            return;
        }

        String requete = "INSERT INTO " + TABLE_NAME + " (nom_tournoi, type_tournoi, nom_jeu, date_debut, date_fin, nombre_participants, cash_prize) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, tournoi.getNomTournoi());
            pst.setString(2, tournoi.getTypeTournoi());
            pst.setString(3, tournoi.getNomJeu());
            pst.setDate(4, tournoi.getDateDebut());
            pst.setDate(5, tournoi.getDateFin());
            pst.setInt(6, tournoi.getNombreParticipants());
            pst.setDouble(7, tournoi.getCashPrize());
            pst.executeUpdate();
            System.out.println("Tournoi ajoute avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void ajouter(Tournoi tournoi) {
        addEntityIfNotExists(tournoi);
    }

    public void addEntityIfNotExists(Tournoi tournoi) {
        String requete = "SELECT id FROM " + TABLE_NAME + " WHERE nom_tournoi = ? AND type_tournoi = ? AND nom_jeu = ? AND date_debut = ? AND date_fin = ? LIMIT 1";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, tournoi.getNomTournoi());
            pst.setString(2, tournoi.getTypeTournoi());
            pst.setString(3, tournoi.getNomJeu());
            pst.setDate(4, tournoi.getDateDebut());
            pst.setDate(5, tournoi.getDateFin());

            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                System.out.println("Tournoi deja existant, ajout ignore.");
                return;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return;
        }

        addEntity(tournoi);
    }

    @Override
    public void deleteEntity(Tournoi tournoi) {
        String requete = "DELETE FROM " + TABLE_NAME + " WHERE id = ?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, tournoi.getId());
            pst.executeUpdate();
            System.out.println("Tournoi supprime avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void supprimer(int id) {
        Tournoi tournoi = new Tournoi();
        tournoi.setId(id);
        deleteEntity(tournoi);
    }

    @Override
    public void updateEntity(int id, Tournoi tournoi) {
        if (!isTournoiValide(tournoi)) {
            return;
        }

        String requete = "UPDATE " + TABLE_NAME + " SET nom_tournoi = ?, type_tournoi = ?, nom_jeu = ?, date_debut = ?, date_fin = ?, nombre_participants = ?, cash_prize = ? WHERE id = ?";

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, tournoi.getNomTournoi());
            pst.setString(2, tournoi.getTypeTournoi());
            pst.setString(3, tournoi.getNomJeu());
            pst.setDate(4, tournoi.getDateDebut());
            pst.setDate(5, tournoi.getDateFin());
            pst.setInt(6, tournoi.getNombreParticipants());
            pst.setDouble(7, tournoi.getCashPrize());
            pst.setInt(8, id);
            pst.executeUpdate();
            System.out.println("Tournoi modifie avec succes !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void modifier(Tournoi tournoi) {
        updateEntity(tournoi.getId(), tournoi);
    }

    private boolean isTournoiValide(Tournoi tournoi) {
        if (tournoi == null) {
            System.out.println("Controle saisie: tous les champs doivent etre remplis.");
            return false;
        }

        if (tournoi.getNomTournoi() == null || tournoi.getNomTournoi().trim().isEmpty()
                || tournoi.getTypeTournoi() == null || tournoi.getTypeTournoi().trim().isEmpty()
                || tournoi.getNomJeu() == null || tournoi.getNomJeu().trim().isEmpty()) {
            System.out.println("Controle saisie: tous les champs texte doivent etre remplis.");
            return false;
        }

        if (tournoi.getNombreParticipants() < 4) {
            System.out.println("Controle saisie: le nombre de participants doit etre au moins 4.");
            return false;
        }

        if (tournoi.getCashPrize() < 0) {
            System.out.println("Controle saisie: le cash prize ne doit pas etre negatif.");
            return false;
        }

        if (tournoi.getDateDebut() == null || tournoi.getDateFin() == null) {
            System.out.println("Controle saisie: les dates debut et fin sont obligatoires.");
            return false;
        }

        LocalDate dateActuelle = LocalDate.now();
        LocalDate dateDebut = tournoi.getDateDebut().toLocalDate();
        LocalDate dateFin = tournoi.getDateFin().toLocalDate();

        if (dateDebut.isBefore(dateActuelle)) {
            System.out.println("Controle saisie: la date de debut ne doit pas etre avant la date actuelle.");
            return false;
        }

        if (dateFin.isBefore(dateDebut)) {
            System.out.println("Controle saisie: la date de fin ne doit pas etre avant la date de debut.");
            return false;
        }

        return true;
    }

    @Override
    public List<Tournoi> getData() {
        List<Tournoi> data = new ArrayList<>();
        String requete = "SELECT * FROM " + TABLE_NAME;

        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                Tournoi tournoi = new Tournoi();
                tournoi.setId(rs.getInt("id"));
                tournoi.setNomTournoi(rs.getString("nom_tournoi"));
                tournoi.setTypeTournoi(rs.getString("type_tournoi"));
                tournoi.setNomJeu(rs.getString("nom_jeu"));
                tournoi.setDateDebut(rs.getDate("date_debut"));
                tournoi.setDateFin(rs.getDate("date_fin"));
                tournoi.setNombreParticipants(rs.getInt("nombre_participants"));
                tournoi.setCashPrize(rs.getDouble("cash_prize"));
                data.add(tournoi);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return data;
    }

    public List<Tournoi> afficher() {
        return getData();
    }
}
