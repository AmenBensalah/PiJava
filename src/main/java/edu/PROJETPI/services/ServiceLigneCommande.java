package edu.PROJETPI.services;

import edu.PROJETPI.entites.LigneCommande;
import edu.PROJETPI.interfaces.IServiceLigneCommande;
import edu.PROJETPI.tools.MyConexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceLigneCommande implements IServiceLigneCommande {
    private final Connection cnx = MyConexion.getInstance().getConnection();

    @Override
    public void add(LigneCommande ligneCommande) throws SQLException {
        String query = "INSERT INTO lignecommande (commandeId, produitId, quantite, prixUnitaire) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, ligneCommande.getCommandeId());
            pst.setInt(2, ligneCommande.getProduitId());
            pst.setInt(3, ligneCommande.getQuantite());
            pst.setDouble(4, ligneCommande.getPrixUnitaire());
            pst.executeUpdate();
        }
    }

    @Override
    public void update(LigneCommande ligneCommande) throws SQLException {
        String query = "UPDATE lignecommande SET commandeId = ?, produitId = ?, quantite = ?, prixUnitaire = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, ligneCommande.getCommandeId());
            pst.setInt(2, ligneCommande.getProduitId());
            pst.setInt(3, ligneCommande.getQuantite());
            pst.setDouble(4, ligneCommande.getPrixUnitaire());
            pst.setInt(5, ligneCommande.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM lignecommande WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    @Override
    public List<LigneCommande> readAll() throws SQLException {
        List<LigneCommande> lignes = new ArrayList<>();
        String query = "SELECT * FROM lignecommande";

        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                lignes.add(new LigneCommande(
                        rs.getInt("id"),
                        rs.getInt("commandeId"),
                        rs.getInt("produitId"),
                        rs.getInt("quantite"),
                        rs.getDouble("prixUnitaire")
                ));
            }
        }

        return lignes;
    }
}
