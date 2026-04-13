package edu.esportify.services;

import edu.esportify.entities.Recrutement;
import edu.esportify.interfaces.IService;
import edu.esportify.tools.MyConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RecrutementService implements IService<Recrutement> {
    private static final List<Recrutement> LOCAL_DATA = new ArrayList<>();
    private static int nextId = 1;

    @Override
    public void addEntity(Recrutement recrutement) {
        if (!hasConnection()) {
            if (recrutement.getId() <= 0) {
                recrutement.setId(nextId++);
            } else {
                nextId = Math.max(nextId, recrutement.getId() + 1);
            }
            LOCAL_DATA.removeIf(item -> item.getId() == recrutement.getId());
            LOCAL_DATA.add(recrutement);
            return;
        }
        String requete = """
                INSERT INTO recrutement (nom_rec, description, status, date_publication, equipe_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, recrutement.getNomRec());
            pst.setString(2, recrutement.getDescription());
            pst.setString(3, recrutement.getStatus());
            pst.setTimestamp(4, Timestamp.valueOf(recrutement.getDatePublication()));
            pst.setInt(5, recrutement.getEquipeId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur ajout recrutement: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteEntity(Recrutement recrutement) {
        if (!hasConnection()) {
            LOCAL_DATA.removeIf(item -> item.getId() == recrutement.getId());
            return;
        }
        String requete = "DELETE FROM recrutement WHERE id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, recrutement.getId());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression recrutement: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateEntity(int id, Recrutement recrutement) {
        if (!hasConnection()) {
            recrutement.setId(id);
            for (int i = 0; i < LOCAL_DATA.size(); i++) {
                if (LOCAL_DATA.get(i).getId() == id) {
                    LOCAL_DATA.set(i, recrutement);
                    return;
                }
            }
            LOCAL_DATA.add(recrutement);
            nextId = Math.max(nextId, id + 1);
            return;
        }
        String requete = """
                UPDATE recrutement
                SET nom_rec = ?, description = ?, status = ?, date_publication = ?, equipe_id = ?
                WHERE id = ?
                """;
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setString(1, recrutement.getNomRec());
            pst.setString(2, recrutement.getDescription());
            pst.setString(3, recrutement.getStatus());
            pst.setTimestamp(4, Timestamp.valueOf(recrutement.getDatePublication()));
            pst.setInt(5, recrutement.getEquipeId());
            pst.setInt(6, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur modification recrutement: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Recrutement> getData() {
        if (!hasConnection()) {
            return LOCAL_DATA.stream()
                    .sorted(Comparator.comparingInt(Recrutement::getId).reversed())
                    .toList();
        }
        List<Recrutement> data = new ArrayList<>();
        String requete = """
                SELECT r.*, e.nom_equipe
                FROM recrutement r
                JOIN equipe e ON r.equipe_id = e.id
                ORDER BY r.id DESC
                """;
        try {
            Statement st = MyConnection.getInstance().getCnx().createStatement();
            ResultSet rs = st.executeQuery(requete);
            while (rs.next()) {
                data.add(mapRecrutement(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture recrutements: " + e.getMessage(), e);
        }
        return data;
    }

    public List<Recrutement> getByEquipe(int equipeId) {
        if (!hasConnection()) {
            return LOCAL_DATA.stream()
                    .filter(recrutement -> recrutement.getEquipeId() == equipeId)
                    .sorted(Comparator.comparingInt(Recrutement::getId).reversed())
                    .toList();
        }
        List<Recrutement> data = new ArrayList<>();
        String requete = """
                SELECT r.*, e.nom_equipe
                FROM recrutement r
                JOIN equipe e ON r.equipe_id = e.id
                WHERE r.equipe_id = ?
                ORDER BY r.id DESC
                """;
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, equipeId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                data.add(mapRecrutement(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lecture recrutements equipe: " + e.getMessage(), e);
        }
        return data;
    }

    public void deleteByEquipe(int equipeId) {
        if (!hasConnection()) {
            LOCAL_DATA.removeIf(recrutement -> recrutement.getEquipeId() == equipeId);
            return;
        }
        String requete = "DELETE FROM recrutement WHERE equipe_id = ?";
        try {
            PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(requete);
            pst.setInt(1, equipeId);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erreur suppression recrutements equipe: " + e.getMessage(), e);
        }
    }

    private Recrutement mapRecrutement(ResultSet rs) throws SQLException {
        Recrutement recrutement = new Recrutement();
        recrutement.setId(rs.getInt("id"));
        recrutement.setNomRec(rs.getString("nom_rec"));
        recrutement.setDescription(rs.getString("description"));
        recrutement.setStatus(rs.getString("status"));
        recrutement.setDatePublication(rs.getTimestamp("date_publication").toLocalDateTime());
        recrutement.setEquipeId(rs.getInt("equipe_id"));
        recrutement.setEquipeNom(rs.getString("nom_equipe"));
        return recrutement;
    }

    private boolean hasConnection() {
        return MyConnection.getInstance().getCnx() != null;
    }
}
