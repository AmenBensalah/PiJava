package edu.PROJETPI.services;

import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.entites.Payment;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServicePaymentStatusIntegrationTest extends ServiceTestSupport {

    private final ServiceCommande serviceCommande = new ServiceCommande();
    private final ServicePayment servicePayment = new ServicePayment();

    @Test
    void addIfMissingForCommandeShouldCreatePaymentWhenCommandeIsMarkedPaid() throws SQLException {
        Commande commande = sampleCommande();
        commande.setTotal(249.0);
        int commandeId = serviceCommande.addAndReturnId(commande);

        commande.setId(commandeId);
        commande.setStatut("PAYEE");
        serviceCommande.update(commande);
        servicePayment.addIfMissingForCommande(
                commandeId,
                commande.getTotal(),
                commande.getDateCommande(),
                "paid"
        );

        List<Payment> payments = servicePayment.readAll();
        assertEquals(1, payments.size());
        assertEquals(commandeId, payments.get(0).getCommandeId());
        assertEquals(249.0, payments.get(0).getMontant());
        assertEquals("paid", payments.get(0).getStatus());
    }
}
