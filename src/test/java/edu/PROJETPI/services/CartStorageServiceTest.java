package edu.PROJETPI.services;

import edu.PROJETPI.entites.CartItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartStorageServiceTest {

    @TempDir
    private Path tempDir;

    @Test
    void loadCartItemsShouldReturnSavedItemsAfterNewServiceInstance() {
        Path cartFile = tempDir.resolve("cart-items.ser");
        CartStorageService firstRunStorage = new CartStorageService(cartFile);
        firstRunStorage.saveCartItems(List.of(new CartItem(7, "Clavier", 2, 85.5)));

        CartStorageService nextRunStorage = new CartStorageService(cartFile);
        List<CartItem> restoredItems = nextRunStorage.loadCartItems();

        assertEquals(1, restoredItems.size());
        assertEquals(7, restoredItems.get(0).getProduitId());
        assertEquals("Clavier", restoredItems.get(0).getNomProduit());
        assertEquals(2, restoredItems.get(0).getQuantite());
        assertEquals(85.5, restoredItems.get(0).getPrixUnitaire());
    }

    @Test
    void clearShouldRemoveSavedCart() {
        Path cartFile = tempDir.resolve("cart-items.ser");
        CartStorageService cartStorageService = new CartStorageService(cartFile);
        cartStorageService.saveCartItems(List.of(new CartItem(4, "Souris", 1, 49.0)));

        cartStorageService.clear();

        assertTrue(cartStorageService.loadCartItems().isEmpty());
    }
}
