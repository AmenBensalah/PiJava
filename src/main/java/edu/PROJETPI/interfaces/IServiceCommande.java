package edu.PROJETPI.interfaces;

import edu.PROJETPI.entites.Commande;

import java.sql.SQLException;
import java.util.List;

public interface IServiceCommande {
    void add(Commande commande) throws SQLException;

    void update(Commande commande) throws SQLException;

    void delete(int id) throws SQLException;

    List<Commande> readAll() throws SQLException;
}
