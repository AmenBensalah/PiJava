package edu.esportify.services;

import edu.esportify.entities.Announcement;
import edu.esportify.tools.MyConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AnnouncementServiceTest {
    private static AnnouncementService service;

    @BeforeAll
    static void setup() {
        MyConnection testConnection = new MyConnection(
                "jdbc:h2:mem:announcement_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "sa",
                "",
                "org.h2.Driver"
        );
        service = new AnnouncementService(testConnection);
        service.clearAll();
    }

    @AfterEach
    void cleanUp() {
        service.clearAll();
    }

    @Test
    @Order(1)
    void testAjouterAnnonce() {
        Announcement announcement = createValidAnnouncement();
        service.addEntity(announcement);

        List<Announcement> data = service.getData();
        assertFalse(data.isEmpty());
        assertEquals("Annonce de test", data.get(0).getTitle());
        assertTrue(announcement.getId() > 0);
    }

    @Test
    @Order(2)
    void testModifierAnnonce() {
        Announcement announcement = createValidAnnouncement();
        service.addEntity(announcement);

        Announcement updated = createValidAnnouncement();
        updated.setTitle("Annonce modifiee");
        updated.setLink("https://example.com/annonce");
        service.updateEntity(announcement.getId(), updated);

        Announcement loaded = service.getData().stream()
                .filter(a -> a.getId() == announcement.getId())
                .findFirst()
                .orElseThrow();
        assertEquals("Annonce modifiee", loaded.getTitle());
        assertEquals("https://example.com/annonce", loaded.getLink());
    }

    @Test
    @Order(3)
    void testSupprimerAnnonce() {
        Announcement announcement = createValidAnnouncement();
        service.addEntity(announcement);

        service.deleteById(announcement.getId());
        assertTrue(service.getData().isEmpty());
    }

    @Test
    @Order(4)
    void testAnnonceInvalide() {
        Announcement announcement = new Announcement();
        announcement.setTag("promo");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.addEntity(announcement));
        assertEquals("Le titre est obligatoire.", exception.getMessage());
    }

    private Announcement createValidAnnouncement() {
        Announcement announcement = new Announcement();
        announcement.setTitle("Annonce de test");
        announcement.setTag("promo");
        return announcement;
    }
}
