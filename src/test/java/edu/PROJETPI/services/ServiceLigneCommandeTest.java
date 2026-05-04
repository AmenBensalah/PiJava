package edu.PROJETPI.services;

import edu.PROJETPI.entites.LigneCommande;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceLigneCommandeTest extends ServiceTestSupport {

    private final ServiceLigneCommande serviceLigneCommande = new ServiceLigneCommande();

    @Test
    void addAndReadAllShouldPersistLigneCommande() throws SQLException {
        int commandeId = insertCommande(sampleCommande());
        LigneCommande ligneCommande = new LigneCommande(commandeId, 2, 3, 49.90);

        serviceLigneCommande.add(ligneCommande);
        List<LigneCommande> lignes = serviceLigneCommande.readAll();

        assertEquals(1, lignes.size());
        assertEquals(commandeId, lignes.get(0).getCommandeId());
        assertEquals(2, lignes.get(0).getProduitId());
        assertEquals(3, lignes.get(0).getQuantite());
    }

    @Test
    void updateShouldModifyExistingLigneCommande() throws SQLException {
        int commandeId = insertCommande(sampleCommande());
        connection.createStatement().executeUpdate(
                "INSERT INTO lignecommande (commandeId, produitId, quantite, prixUnitaire) VALUES (" + commandeId + ", 2, 1, 39.90)"
        );

        LigneCommande existing = serviceLigneCommande.readAll().get(0);
        existing.setQuantite(5);
        existing.setPrixUnitaire(79.50);
        serviceLigneCommande.update(existing);

        LigneCommande updated = serviceLigneCommande.readAll().get(0);
        assertEquals(5, updated.getQuantite());
        assertEquals(79.50, updated.getPrixUnitaire());
    }

    @Test
    void deleteShouldRemoveLigneCommande() throws SQLException {
        int commandeId = insertCommande(sampleCommande());
        connection.createStatement().executeUpdate(
                "INSERT INTO lignecommande (commandeId, produitId, quantite, prixUnitaire) VALUES (" + commandeId + ", 4, 2, 19.90)"
        );

        int ligneId = serviceLigneCommande.readAll().get(0).getId();
        serviceLigneCommande.delete(ligneId);

        assertTrue(serviceLigneCommande.readAll().isEmpty());
    }
}
