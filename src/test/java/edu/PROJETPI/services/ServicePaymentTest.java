package edu.PROJETPI.services;

import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.entites.Payment;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicePaymentTest extends ServiceTestSupport {

    private final ServicePayment servicePayment = new ServicePayment();

    @Test
    void addAndReadAllShouldPersistPayment() throws SQLException {
        int commandeId = insertCommande(sampleCommande());
        Payment payment = new Payment(commandeId, 350.0, java.sql.Date.valueOf("2026-04-11"));

        servicePayment.add(payment);
        List<Payment> payments = servicePayment.readAll();

        assertEquals(1, payments.size());
        assertEquals(commandeId, payments.get(0).getCommandeId());
        assertEquals(350.0, payments.get(0).getMontant());
    }

    @Test
    void updateShouldModifyExistingPayment() throws SQLException {
        int commandeId = insertCommande(sampleCommande());
        connection.createStatement().executeUpdate(
                "INSERT INTO payment (commandeId, montant, datePayment) VALUES (" + commandeId + ", 500.0, '2026-04-11')"
        );

        Payment existing = servicePayment.readAll().get(0);
        existing.setMontant(650.0);
        existing.setDatePayment(java.sql.Date.valueOf("2026-04-12"));
        servicePayment.update(existing);

        Payment updated = servicePayment.readAll().get(0);
        assertEquals(650.0, updated.getMontant());
        assertEquals(java.sql.Date.valueOf("2026-04-12"), updated.getDatePayment());
    }

    @Test
    void deleteShouldRemovePayment() throws SQLException {
        int commandeId = insertCommande(sampleCommande());
        connection.createStatement().executeUpdate(
                "INSERT INTO payment (commandeId, montant, datePayment) VALUES (" + commandeId + ", 120.0, '2026-04-11')"
        );

        int paymentId = servicePayment.readAll().get(0).getId();
        servicePayment.delete(paymentId);

        assertTrue(servicePayment.readAll().isEmpty());
    }

    @Test
    void readByPeriodShouldReturnOnlyPaymentsInsideRange() throws SQLException {
        int commandeId1 = insertCommande(sampleCommande());
        Commande commande2 = sampleCommande();
        commande2.setDateCommande(java.sql.Date.valueOf("2026-04-15"));
        int commandeId2 = insertCommande(commande2);

        connection.createStatement().executeUpdate(
                "INSERT INTO payment (commandeId, montant, datePayment) VALUES (" + commandeId1 + ", 120.0, '2026-04-11')"
        );
        connection.createStatement().executeUpdate(
                "INSERT INTO payment (commandeId, montant, datePayment) VALUES (" + commandeId2 + ", 220.0, '2026-04-15')"
        );

        List<Payment> payments = servicePayment.readByPeriod(
                java.sql.Date.valueOf("2026-04-12"),
                java.sql.Date.valueOf("2026-04-16")
        );

        assertEquals(1, payments.size());
        assertEquals(commandeId2, payments.get(0).getCommandeId());
        assertEquals(220.0, payments.get(0).getMontant());
    }
}
