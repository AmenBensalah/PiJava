package edu.connexion3a77.services;

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
class TournoiServiceTest {

    private static final String TEST_PREFIX = "UT_";
    private TournoiService tournoiService;

    @BeforeEach
    void setUp() {
        tournoiService = new TournoiService();
    }

    @AfterEach
    void tearDown() throws SQLException {
        String deleteSql = "DELETE FROM tournoi WHERE nom_tournoi LIKE ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(deleteSql);
        pst.setString(1, TEST_PREFIX + "%");
        pst.executeUpdate();
    }

    @Test
    @Order(1)
    void ajouterTournoi() {
        Tournoi tournoi = buildTestTournoi("AJOUT");
        tournoiService.ajouter(tournoi);

        List<Tournoi> data = tournoiService.afficher();
        boolean exists = data.stream()
                .anyMatch(t -> tournoi.getNomTournoi().equals(t.getNomTournoi()));

        assertTrue(exists, "Le tournoi ajoute doit exister dans la base.");
    }

    @Test
    @Order(2)
    void afficherTournois() {
        Tournoi tournoi = buildTestTournoi("AFFICHER");
        tournoiService.ajouter(tournoi);

        List<Tournoi> data = tournoiService.afficher();

        assertNotNull(data, "La liste retournee par afficher() ne doit pas etre null.");
        assertFalse(data.isEmpty(), "La liste retournee par afficher() ne doit pas etre vide.");
    }

    @Test
    @Order(3)
    void modifierTournoi() throws SQLException {
        Tournoi tournoi = buildTestTournoi("MODIFIER");
        tournoiService.ajouter(tournoi);

        int id = getIdByNomTournoi(tournoi.getNomTournoi());
        assertTrue(id > 0, "Le tournoi insere doit avoir un id valide.");

        Tournoi modifie = new Tournoi(
                id,
                tournoi.getNomTournoi(),
                tournoi.getTypeTournoi(),
                tournoi.getNomJeu(),
                Date.valueOf(LocalDate.now().plusDays(10)),
                Date.valueOf(LocalDate.now().plusDays(12)),
                24,
                9000.0
        );

        tournoiService.modifier(modifie);

        int participants = getNombreParticipantsById(id);
        assertEquals(24, participants, "Le nombre de participants doit etre mis a jour.");
    }

    @Test
    @Order(4)
    void supprimerTournoi() throws SQLException {
        Tournoi tournoi = buildTestTournoi("SUPPRIMER");
        tournoiService.ajouter(tournoi);

        int id = getIdByNomTournoi(tournoi.getNomTournoi());
        assertTrue(id > 0, "Le tournoi insere doit avoir un id valide.");

        tournoiService.supprimer(id);

        boolean exists = existsById(id);
        assertFalse(exists, "Le tournoi supprime ne doit plus exister dans la base.");
    }

    private Tournoi buildTestTournoi(String tag) {
        String uniqueNom = TEST_PREFIX + tag + "_" + UUID.randomUUID();
        return new Tournoi(
                uniqueNom,
                "Ligue",
                "Valorant",
                Date.valueOf(LocalDate.now().plusDays(5)),
                Date.valueOf(LocalDate.now().plusDays(7)),
                16,
                5000.0
        );
    }

    private int getIdByNomTournoi(String nomTournoi) throws SQLException {
        String sql = "SELECT id FROM tournoi WHERE nom_tournoi = ? ORDER BY id DESC LIMIT 1";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setString(1, nomTournoi);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1;
    }

    private int getNombreParticipantsById(int id) throws SQLException {
        String sql = "SELECT nombre_participants FROM tournoi WHERE id = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            return rs.getInt("nombre_participants");
        }
        return -1;
    }

    private boolean existsById(int id) throws SQLException {
        String sql = "SELECT id FROM tournoi WHERE id = ?";
        PreparedStatement pst = MyConnection.getInstance().getCnx().prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        return rs.next();
    }
}
