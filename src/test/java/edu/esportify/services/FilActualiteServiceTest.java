package edu.esportify.services;

import edu.esportify.entities.FilActualite;
import edu.esportify.tools.MyConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FilActualiteServiceTest {
    private static FilActualiteService service;

    @BeforeAll
    static void setup() {
        MyConnection testConnection = new MyConnection(
                "jdbc:h2:mem:fil_actualite_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "sa",
                "",
                "org.h2.Driver"
        );
        service = new FilActualiteService(testConnection);
        service.clearAll();
    }

    @AfterEach
    void cleanUp() {
        service.clearAll();
    }

    @Test
    @Order(1)
    void testAjouterPublication() {
        FilActualite actualite = createValidPost();
        service.addEntity(actualite);

        List<FilActualite> actualites = service.getData();
        assertFalse(actualites.isEmpty());
        assertEquals("Publication de test reliee a Symfony.", actualites.get(0).getContent());
        assertTrue(actualite.getId() > 0);
    }

    @Test
    @Order(2)
    void testLirePublication() {
        FilActualite actualite = createValidPost();
        service.addEntity(actualite);

        FilActualite loaded = service.findById(actualite.getId()).orElse(null);
        assertNotNull(loaded);
        assertEquals("text", loaded.getMediaType());
        assertEquals(3, loaded.getAuthorId());
    }

    @Test
    @Order(3)
    void testModifierPublication() {
        FilActualite actualite = createValidPost();
        service.addEntity(actualite);

        FilActualite updated = createValidPost();
        updated.setContent("Publication modifiee depuis JavaFX.");
        updated.setVideoUrl("https://www.youtube.com/watch?v=abc123");
        service.updateEntity(actualite.getId(), updated);

        FilActualite loaded = service.findById(actualite.getId()).orElseThrow();
        assertEquals("Publication modifiee depuis JavaFX.", loaded.getContent());
        assertEquals("video", loaded.getMediaType());
    }

    @Test
    @Order(4)
    void testSupprimerPublication() {
        FilActualite actualite = createValidPost();
        service.addEntity(actualite);

        service.deleteById(actualite.getId());

        assertTrue(service.findById(actualite.getId()).isEmpty());
    }

    @Test
    @Order(5)
    void testDonneesInvalides() {
        FilActualite actualite = new FilActualite();
        actualite.setVideoUrl("notaurl");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.addEntity(actualite));
        assertEquals("Le lien de la video est invalide.", exception.getMessage());
    }

    @Test
    @Order(6)
    void testPublicationEvenement() {
        FilActualite actualite = new FilActualite();
        actualite.setEvent(true);
        actualite.setEventTitle("Tournoi LAN E-sportify");
        actualite.setEventLocation("Discord principal");
        actualite.setEventDate(LocalDateTime.now().plusDays(5));
        actualite.setMaxParticipants(16);
        actualite.setAuthorId(2);

        service.addEntity(actualite);

        FilActualite loaded = service.findById(actualite.getId()).orElseThrow();
        assertTrue(loaded.isEvent());
        assertEquals("event", loaded.getMediaType());
        assertEquals(16, loaded.getMaxParticipants());
    }

    private FilActualite createValidPost() {
        FilActualite actualite = new FilActualite();
        actualite.setContent("Publication de test reliee a Symfony.");
        actualite.setAuthorId(3);
        return actualite;
    }
}
