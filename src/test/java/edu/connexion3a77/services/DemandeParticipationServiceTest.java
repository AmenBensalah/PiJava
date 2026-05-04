package edu.connexion3a77.services;

import edu.connexion3a77.entities.DemandeParticipation;
import edu.connexion3a77.entities.Tournoi;
import edu.connexion3a77.tools.MyConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DemandeParticipationServiceTest {

    private static final String TEST_TOURNOI_PREFIX = "UT_TOURNOI_DP_";
    private static final String TEST_DEMANDE_PREFIX = "UT_DEMANDE_DP_";

    private TournoiService tournoiService;
    private DemandeParticipationService demandeParticipationService;
    private int testTournoiId;
    private String testDemandeDescription;

    @BeforeEach
    void setUp() throws SQLException {
        tournoiService = new TournoiService();
        demandeParticipationService = new DemandeParticipationService();

        String tournoiNom = TEST_TOURNOI_PREFIX + UUID.randomUUID();
        Tournoi tournoi = new Tournoi(
                tournoiNom,
                "Ligue",
                "Valorant",
                Date.valueOf(LocalDate.now().plusDays(6)),
                Date.valueOf(LocalDate.now().plusDays(8)),
                16,
                4000.0
        );
        tournoiService.ajouter(tournoi);
        testTournoiId = getTournoiIdByNom(tournoiNom);
        testDemandeDescription = TEST_DEMANDE_PREFIX + UUID.randomUUID();
    }

    @AfterEach
    void tearDown() throws SQLException {
        PreparedStatement pstDemande = MyConnection.getInstance().getCnx()
                .prepareStatement("DELETE FROM participation_request WHERE description LIKE ?");
        pstDemande.setString(1, TEST_DEMANDE_PREFIX + "%");
        pstDemande.executeUpdate();

        PreparedStatement pstTournoi = MyConnection.getInstance().getCnx()
                .prepareStatement("DELETE FROM tournoi WHERE nom_tournoi LIKE ?");
        pstTournoi.setString(1, TEST_TOURNOI_PREFIX + "%");
        pstTournoi.executeUpdate();
    }

    @Test
    @Order(1)
    void ajouterDemandeParticipation() {
        DemandeParticipation demande = new DemandeParticipation(
                testTournoiId,
                testDemandeDescription,
                "Intermediaire"
        );
        demandeParticipationService.ajouter(demande);

        List<DemandeParticipation> demandes = demandeParticipationService.afficher();
        boolean exists = demandes.stream().anyMatch(d -> testDemandeDescription.equals(d.getDescription()));
        assertTrue(exists, "La demande ajoutee doit exister dans la base.");
    }

    @Test
    @Order(2)
    void afficherDemandesParticipation() {
        DemandeParticipation demande = new DemandeParticipation(
                testTournoiId,
                testDemandeDescription,
                "Debutant"
        );
        demandeParticipationService.ajouter(demande);

        List<DemandeParticipation> demandes = demandeParticipationService.afficher();
        assertNotNull(demandes, "La liste retournee par afficher() ne doit pas etre null.");
        assertFalse(demandes.isEmpty(), "La liste retournee par afficher() ne doit pas etre vide.");
    }

    @Test
    @Order(3)
    void modifierDemandeParticipation() throws SQLException {
        DemandeParticipation demande = new DemandeParticipation(
                testTournoiId,
                testDemandeDescription,
                "Debutant"
        );
        demandeParticipationService.ajouter(demande);

        int demandeId = getDemandeIdByDescription(testDemandeDescription);
        assertTrue(demandeId > 0, "La demande inseree doit avoir un id valide.");

        DemandeParticipation demandeModifiee = new DemandeParticipation(
                demandeId,
                testTournoiId,
                testDemandeDescription,
                "Expert"
        );
        demandeParticipationService.modifier(demandeModifiee);

        String niveau = getDemandeNiveauById(demandeId);
        assertEquals("Expert", niveau, "Le niveau doit etre mis a jour.");
    }

    @Test
    @Order(4)
    void supprimerDemandeParticipation() throws SQLException {
        DemandeParticipation demande = new DemandeParticipation(
                testTournoiId,
                testDemandeDescription,
                "Avance"
        );
        demandeParticipationService.ajouter(demande);

        int demandeId = getDemandeIdByDescription(testDemandeDescription);
        assertTrue(demandeId > 0, "La demande inseree doit avoir un id valide.");

        demandeParticipationService.supprimer(demandeId);

        boolean exists = existsDemandeById(demandeId);
        assertFalse(exists, "La demande supprimee ne doit plus exister dans la base.");
    }

    private int getTournoiIdByNom(String nomTournoi) throws SQLException {
        PreparedStatement pst = MyConnection.getInstance().getCnx()
                .prepareStatement("SELECT id FROM tournoi WHERE nom_tournoi = ? ORDER BY id DESC LIMIT 1");
        pst.setString(1, nomTournoi);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1;
    }

    private int getDemandeIdByDescription(String description) throws SQLException {
        PreparedStatement pst = MyConnection.getInstance().getCnx()
                .prepareStatement("SELECT id FROM participation_request WHERE description = ? ORDER BY id DESC LIMIT 1");
        pst.setString(1, description);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1;
    }

    private String getDemandeNiveauById(int id) throws SQLException {
        PreparedStatement pst = MyConnection.getInstance().getCnx()
                .prepareStatement("SELECT niveau FROM participation_request WHERE id = ?");
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getString("niveau");
        }
        return "";
    }

    private boolean existsDemandeById(int id) throws SQLException {
        PreparedStatement pst = MyConnection.getInstance().getCnx()
                .prepareStatement("SELECT id FROM participation_request WHERE id = ?");
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        return rs.next();
    }
}
