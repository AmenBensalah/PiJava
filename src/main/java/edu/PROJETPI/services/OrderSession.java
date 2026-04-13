package edu.PROJETPI.services;

import edu.PROJETPI.entites.CartItem;
import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.entites.Produit;

import java.util.ArrayList;
import java.util.List;

public class OrderSession {
    private static final OrderSession INSTANCE = new OrderSession();

    private final List<CartItem> cartItems = new ArrayList<>();
    private Commande draftCommande;

    private OrderSession() {
    }

    public static OrderSession getInstance() {
        return INSTANCE;
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }

    public void addProduct(Produit produit, int quantite) {
        CartItem existing = findItem(produit.getId());
        if (existing != null) {
            existing.setQuantite(existing.getQuantite() + quantite);
            return;
        }
        cartItems.add(new CartItem(produit.getId(), produit.getNom(), quantite, produit.getPrix()));
    }

    public void updateQuantity(int produitId, int quantite) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantite doit etre positive.");
        }

        CartItem item = findItem(produitId);
        if (item != null) {
            item.setQuantite(quantite);
        }
    }

    public void removeProduct(int produitId) {
        cartItems.removeIf(item -> item.getProduitId() == produitId);
    }

    public void clearCart() {
        cartItems.clear();
        draftCommande = null;
    }

    public boolean isCartEmpty() {
        return cartItems.isEmpty();
    }

    public int getTotalItems() {
        return cartItems.stream().mapToInt(CartItem::getQuantite).sum();
    }

    public double getCartTotal() {
        return cartItems.stream().mapToDouble(CartItem::getSousTotal).sum();
    }

    public Commande getDraftCommande() {
        return draftCommande;
    }

    public void setDraftCommande(Commande draftCommande) {
        this.draftCommande = draftCommande;
    }

    public void resetAfterCheckout() {
        clearCart();
    }

    private CartItem findItem(int produitId) {
        return cartItems.stream()
                .filter(item -> item.getProduitId() == produitId)
                .findFirst()
                .orElse(null);
    }
}
