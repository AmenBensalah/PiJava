package edu.PROJETPI.interfaces;

import edu.PROJETPI.entites.LigneCommande;

import java.sql.SQLException;
import java.util.List;

public interface IServiceLigneCommande {
    void add(LigneCommande ligneCommande) throws SQLException;

    void update(LigneCommande ligneCommande) throws SQLException;

    void delete(int id) throws SQLException;

    List<LigneCommande> readAll() throws SQLException;
}
