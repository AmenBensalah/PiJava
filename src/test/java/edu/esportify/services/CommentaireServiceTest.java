package edu.esportify.services;

import edu.esportify.entities.Commentaire;
import edu.esportify.entities.FilActualite;
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
public class CommentaireServiceTest {
    private static CommentaireService service;
    private static FilActualiteService postService;

    @BeforeAll
    static void setup() {
        MyConnection testConnection = new MyConnection(
                "jdbc:h2:mem:commentaire_test;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "sa",
                "",
                "org.h2.Driver"
        );
        service = new CommentaireService(testConnection);
        postService = new FilActualiteService(testConnection);
        service.clearAll();
        postService.clearAll();
    }

    @AfterEach
    void cleanUp() {
        service.clearAll();
        postService.clearAll();
    }

    @Test
    @Order(1)
    void testAjouterCommentaire() {
        int postId = seedPost();

        Commentaire comment = new Commentaire();
        comment.setAuthorId(3);
        comment.setPostId(postId);
        comment.setContent("Super publication !");
        service.addEntity(comment);

        List<Commentaire> data = service.getData();
        assertFalse(data.isEmpty());
        assertEquals("Super publication !", data.get(0).getContent());
        assertTrue(comment.getId() > 0);
    }

    @Test
    @Order(2)
    void testModifierCommentaire() {
        int postId = seedPost();

        Commentaire comment = new Commentaire();
        comment.setAuthorId(2);
        comment.setPostId(postId);
        comment.setContent("Ancien commentaire");
        service.addEntity(comment);

        Commentaire updated = new Commentaire();
        updated.setAuthorId(2);
        updated.setPostId(postId);
        updated.setContent("Commentaire modifie");
        service.updateEntity(comment.getId(), updated);

        Commentaire loaded = service.getData().stream()
                .filter(c -> c.getId() == comment.getId())
                .findFirst()
                .orElseThrow();
        assertEquals("Commentaire modifie", loaded.getContent());
    }

    @Test
    @Order(3)
    void testSupprimerCommentaire() {
        int postId = seedPost();

        Commentaire comment = new Commentaire();
        comment.setAuthorId(1);
        comment.setPostId(postId);
        comment.setContent("A supprimer");
        service.addEntity(comment);

        service.deleteById(comment.getId());
        assertTrue(service.getData().isEmpty());
    }

    @Test
    @Order(4)
    void testCommentaireInvalide() {
        Commentaire comment = new Commentaire();
        comment.setAuthorId(1);
        comment.setPostId(1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> service.addEntity(comment));
        assertEquals("Le contenu du commentaire est obligatoire.", exception.getMessage());
    }

    private int seedPost() {
        FilActualite post = new FilActualite();
        post.setContent("Post de test");
        post.setAuthorId(5);
        postService.addEntity(post);
        return post.getId();
    }
}
