package edu.PROJETPI.services;

import edu.PROJETPI.entites.CartItem;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CartStorageService {
    private static final Path STORAGE_PATH = Paths.get(
            System.getProperty("user.home"),
            ".projetpi",
            "cart-items.ser"
    );

    public List<CartItem> loadCartItems() {
        if (!Files.exists(STORAGE_PATH)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream inputStream = new ObjectInputStream(Files.newInputStream(STORAGE_PATH))) {
            Object savedData = inputStream.readObject();
            if (savedData instanceof List<?> savedList) {
                List<CartItem> items = new ArrayList<>();
                for (Object item : savedList) {
                    if (item instanceof CartItem cartItem) {
                        items.add(cartItem);
                    }
                }
                return items;
            }
        } catch (IOException | ClassNotFoundException e) {
            deleteStorageQuietly();
        }

        return new ArrayList<>();
    }

    public void saveCartItems(List<CartItem> cartItems) {
        try {
            Files.createDirectories(STORAGE_PATH.getParent());
            try (ObjectOutputStream outputStream = new ObjectOutputStream(Files.newOutputStream(STORAGE_PATH))) {
                outputStream.writeObject(new ArrayList<>(cartItems));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de sauvegarder le panier localement.", e);
        }
    }

    public void clear() {
        deleteStorageQuietly();
    }

    private void deleteStorageQuietly() {
        try {
            Files.deleteIfExists(STORAGE_PATH);
        } catch (IOException ignored) {
        }
    }
}
