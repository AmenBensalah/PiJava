package edu.projetJava.services;

import edu.projetJava.interfaces.IService;
import edu.projetJava.entities.Categorie;
import edu.projetJava.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategorieService implements IService<Categorie> {

    private Connection connection;

    public CategorieService() {
        connection = MyDatabase.getInstance().getConnection();
        try {
            // Essaie de définir id comme clé primaire (échouera silencieusement si c'est déjà le cas)
            connection.createStatement().executeUpdate("ALTER TABLE `categorie` ADD PRIMARY KEY(`id`);");
        } catch (SQLException ignored) {}
        
        try {
            // Force l'auto-increment sur la colonne ID
            connection.createStatement().executeUpdate("ALTER TABLE `categorie` MODIFY `id` INT AUTO_INCREMENT;");
        } catch (SQLException ignored) {}
    }

    @Override
    public void ajouter(Categorie obj) throws SQLException {
        String sql = "insert into `categorie` (`nom`) values(?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, obj.getNom());
        ps.executeUpdate();
    }

    @Override
    public void modifier(Categorie obj) throws SQLException {
        String sql = "update `categorie` set `nom` = ? where `id` = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setString(1, obj.getNom());
        ps.setInt(2, obj.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "delete from `categorie` where `id` = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Categorie> recuperer() throws SQLException {
        String sql = "select * from `categorie`";
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        List<Categorie> list = new ArrayList<>();
        while (rs.next()) {
            Categorie obj = new Categorie();
            obj.setId(rs.getInt("id"));
            obj.setNom(rs.getString("nom"));
            list.add(obj);
        }
        return list;
    }
}
