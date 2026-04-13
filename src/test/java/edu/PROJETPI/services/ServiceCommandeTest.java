package edu.PROJETPI.services;

import edu.PROJETPI.entites.Commande;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceCommandeTest extends ServiceTestSupport {

    private final ServiceCommande serviceCommande = new ServiceCommande();

    @Test
    void addAndReadAllShouldPersistCommande() throws SQLException {
        Commande commande = sampleCommande();

        int generatedId = serviceCommande.addAndReturnId(commande);
        List<Commande> commandes = serviceCommande.readAll();

        assertEquals(1, commandes.size());
        assertTrue(commandes.stream().anyMatch(item ->
                item.getId() == generatedId
                        && "Test".equals(item.getNom())
                        && "Client".equals(item.getPrenom())
                        && "EN_ATTENTE".equals(item.getStatut())));
    }

    @Test
    void updateShouldModifyExistingCommande() throws SQLException {
        Commande commande = sampleCommande();
        int id = serviceCommande.addAndReturnId(commande);

        commande.setId(id);
        commande.setStatut("PAYEE");
        commande.setNom("Nom modifie");
        commande.setTotal(299.50);
        serviceCommande.update(commande);

        Commande updated = serviceCommande.readAll().stream()
                .filter(item -> item.getId() == id)
                .findFirst()
                .orElseThrow();

        assertEquals("PAYEE", updated.getStatut());
        assertEquals("Nom modifie", updated.getNom());
        assertEquals(299.50, updated.getTotal());
    }

    @Test
    void deleteShouldRemoveCommandeAndRelatedData() throws SQLException {
        Commande commande = sampleCommande();
        int commandeId = serviceCommande.addAndReturnId(commande);

        connection.createStatement().executeUpdate(
                "INSERT INTO lignecommande (commandeId, produitId, quantite, prixUnitaire) VALUES (" + commandeId + ", 1, 2, 10.0)"
        );
        connection.createStatement().executeUpdate(
                "INSERT INTO payment (commandeId, montant, datePayment) VALUES (" + commandeId + ", 20.0, '2026-04-11')"
        );

        serviceCommande.delete(commandeId);

        assertTrue(serviceCommande.readAll().isEmpty());
        try (var rsLigne = connection.createStatement().executeQuery("SELECT COUNT(*) FROM lignecommande")) {
            rsLigne.next();
            assertEquals(0, rsLigne.getInt(1));
        }
        try (var rsPayment = connection.createStatement().executeQuery("SELECT COUNT(*) FROM payment")) {
            rsPayment.next();
            assertEquals(0, rsPayment.getInt(1));
        }
    }
}
