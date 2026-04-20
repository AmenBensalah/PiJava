package edu.projetJava.services;

import edu.projetJava.interfaces.IService;
import edu.projetJava.entities.Produit;
import edu.projetJava.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduitService implements IService<Produit> {

    private Connection connection;

    public ProduitService() {
        connection = MyDatabase.getInstance().getConnection();
    }

    @Override
    public void ajouter(Produit obj) throws SQLException {
        String sql = "insert into `produit` (`nom`,`prix`,`stock`,`description`,`image`,`active`,`statut`,`owner_user_id`,`owner_equipe_id`,`categorie_id`,`video_url`,`technical_specs`,`install_difficulty`) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, obj.getNom());
        ps.setInt(2, obj.getPrix());
        ps.setInt(3, obj.getStock());
        ps.setString(4, obj.getDescription());
        ps.setString(5, obj.getImage());
        ps.setBoolean(6, obj.getActive());
        ps.setString(7, obj.getStatut());
        if (obj.getOwnerUserId() == 0) ps.setNull(8, java.sql.Types.INTEGER); else ps.setInt(8, obj.getOwnerUserId());
        if (obj.getOwnerEquipeId() == 0) ps.setNull(9, java.sql.Types.INTEGER); else ps.setInt(9, obj.getOwnerEquipeId());
        if (obj.getCategorieId() == 0) ps.setNull(10, java.sql.Types.INTEGER); else ps.setInt(10, obj.getCategorieId());
        ps.setString(11, obj.getVideoUrl());
        ps.setString(12, obj.getTechnicalSpecs());
        ps.setString(13, obj.getInstallDifficulty());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Produit obj) throws SQLException {
        String sql = "update `produit` set `nom` = ?, `prix` = ?, `stock` = ?, `description` = ?, `image` = ?, `active` = ?, `statut` = ?, `owner_user_id` = ?, `owner_equipe_id` = ?, `categorie_id` = ?, `video_url` = ?, `technical_specs` = ?, `install_difficulty` = ? where `id` = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, obj.getNom());
        ps.setInt(2, obj.getPrix());
        ps.setInt(3, obj.getStock());
        ps.setString(4, obj.getDescription());
        ps.setString(5, obj.getImage());
        ps.setBoolean(6, obj.getActive());
        ps.setString(7, obj.getStatut());
        if (obj.getOwnerUserId() == 0) ps.setNull(8, java.sql.Types.INTEGER); else ps.setInt(8, obj.getOwnerUserId());
        if (obj.getOwnerEquipeId() == 0) ps.setNull(9, java.sql.Types.INTEGER); else ps.setInt(9, obj.getOwnerEquipeId());
        if (obj.getCategorieId() == 0) ps.setNull(10, java.sql.Types.INTEGER); else ps.setInt(10, obj.getCategorieId());
        ps.setString(11, obj.getVideoUrl());
        ps.setString(12, obj.getTechnicalSpecs());
        ps.setString(13, obj.getInstallDifficulty());
        ps.setInt(14, obj.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        Statement st = connection.createStatement();
        st.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
        String sql = "delete from `produit` where `id` = " + id;
        st.executeUpdate(sql);
        st.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
    }

    public void viderTouteLaTable() throws SQLException {
        Statement st = connection.createStatement();
        st.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
        st.executeUpdate("DELETE FROM `produit` ");
        st.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
    }

    @Override
    public List<Produit> recuperer() throws SQLException {
        String sql = "select * from `produit`";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        List<Produit> list = new ArrayList<>();
        while (rs.next()) {
            Produit obj = new Produit();
            obj.setId(rs.getInt("id"));
            obj.setNom(rs.getString("nom"));
            obj.setPrix(rs.getInt("prix"));
            obj.setStock(rs.getInt("stock"));
            obj.setDescription(rs.getString("description"));
            obj.setImage(rs.getString("image"));
            obj.setActive(rs.getBoolean("active"));
            obj.setStatut(rs.getString("statut"));
            obj.setOwnerUserId(rs.getInt("owner_user_id"));
            obj.setOwnerEquipeId(rs.getInt("owner_equipe_id"));
            obj.setCategorieId(rs.getInt("categorie_id"));
            obj.setVideoUrl(rs.getString("video_url"));
            obj.setTechnicalSpecs(rs.getString("technical_specs"));
            obj.setInstallDifficulty(rs.getString("install_difficulty"));
            list.add(obj);
        }
        return list;
    }

    public boolean existeDeja(String nom) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `produit` WHERE `nom` = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, nom);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }
}
