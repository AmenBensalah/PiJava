package edu.projetJava.services;

import edu.projetJava.entities.Categorie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CategorieServiceTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    private CategorieService categorieService;

    @BeforeEach
    void setUp() throws SQLException {
        categorieService = new CategorieService();
        try {
            java.lang.reflect.Field field = CategorieService.class.getDeclaredField("connection");
            field.setAccessible(true);
            field.set(categorieService, mockConnection);
        } catch (Exception e) {
            fail("Impossible d'injecter la connexion mockée");
        }
    }

    @Test
    void testAjouterCategorie() throws SQLException {
        // Arrange
        Categorie cat = new Categorie();
        cat.setNom("Informatique");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        categorieService.ajouter(cat);

        // Assert
        verify(mockPreparedStatement).setString(1, "Informatique");
        verify(mockPreparedStatement).executeUpdate();
    }
}
