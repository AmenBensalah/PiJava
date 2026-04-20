package edu.projetJava.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProduitServiceTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    private ProduitService produitService;

    @BeforeEach
    void setUp() throws SQLException {
        // Comme le constructeur de ProduitService appelle MyDatabase.getInstance(),
        // nous allons injecter manuellement la connexion mockée pour le test.
        produitService = new ProduitService();
        
        // On utilise la réflexion ou on modifie le champ (plus simple ici de simuler l'injection)
        try {
            java.lang.reflect.Field field = ProduitService.class.getDeclaredField("connection");
            field.setAccessible(true);
            field.set(produitService, mockConnection);
        } catch (Exception e) {
            fail("Impossible d'injecter la connexion mockée");
        }
    }

    @Test
    void testExisteDeja_ShouldReturnTrue_WhenProductExists() throws SQLException {
        // 1. Préparation (Arrange)
        String productName = "Clavier Gamer";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1); // Simule qu'on a trouvé 1 produit

        // 2. Exécution (Act)
        boolean exists = produitService.existeDeja(productName);

        // 3. Vérification (Assert)
        assertTrue(exists, "Le produit devrait exister");
        verify(mockPreparedStatement).setString(1, productName);
    }

    @Test
    void testExisteDeja_ShouldReturnFalse_WhenProductDoesNotExist() throws SQLException {
        // 1. Préparation
        String productName = "ProduitInexistant";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(0); // Simule 0 produit trouvé

        // 2. Exécution
        boolean exists = produitService.existeDeja(productName);

        // 3. Vérification
        assertFalse(exists, "Le produit ne devrait pas exister");
    }
}
