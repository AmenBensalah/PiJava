package edu.PROJETPI.services;

import edu.PROJETPI.entites.CartItem;
import edu.PROJETPI.entites.Commande;
import edu.PROJETPI.entites.Produit;

import java.util.ArrayList;
import java.util.List;

public class OrderSession {
    public enum CheckoutMode {
        STRIPE,
        CASH_ON_DELIVERY
    }

    private static final OrderSession INSTANCE = new OrderSession();

    private final List<CartItem> cartItems = new ArrayList<>();
    private final CartStorageService cartStorageService = new CartStorageService();
    private Commande draftCommande;
    private Commande confirmedCommande;
    private List<CartItem> confirmedCartItems = new ArrayList<>();
    private double confirmedCartTotal;
    private int confirmedCommandeId;
    private CheckoutMode checkoutMode = CheckoutMode.STRIPE;

    private OrderSession() {
        cartItems.addAll(cartStorageService.loadCartItems());
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
            persistCart();
            return;
        }
        cartItems.add(new CartItem(produit.getId(), produit.getNom(), quantite, produit.getPrix()));
        persistCart();
    }

    public void updateQuantity(int produitId, int quantite) {
        if (quantite <= 0) {
            throw new IllegalArgumentException("La quantite doit etre positive.");
        }

        CartItem item = findItem(produitId);
        if (item != null) {
            item.setQuantite(quantite);
            persistCart();
        }
    }

    public void removeProduct(int produitId) {
        cartItems.removeIf(item -> item.getProduitId() == produitId);
        persistCart();
    }

    public void clearCart() {
        cartItems.clear();
        draftCommande = null;
        checkoutMode = CheckoutMode.STRIPE;
        cartStorageService.clear();
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

    public CheckoutMode getCheckoutMode() {
        return checkoutMode;
    }

    public void setCheckoutMode(CheckoutMode checkoutMode) {
        this.checkoutMode = checkoutMode == null ? CheckoutMode.STRIPE : checkoutMode;
    }

    public void resetAfterCheckout() {
        clearCart();
    }

    public void rememberConfirmedOrder(Commande commande, List<CartItem> items, double total, int commandeId) {
        confirmedCommande = commande;
        confirmedCartItems = items == null ? new ArrayList<>() : new ArrayList<>(items);
        confirmedCartTotal = total;
        confirmedCommandeId = commandeId;
    }

    public Commande getConfirmedCommande() {
        return confirmedCommande;
    }

    public List<CartItem> getConfirmedCartItems() {
        return new ArrayList<>(confirmedCartItems);
    }

    public double getConfirmedCartTotal() {
        return confirmedCartTotal;
    }

    public int getConfirmedCommandeId() {
        return confirmedCommandeId;
    }

    private CartItem findItem(int produitId) {
        return cartItems.stream()
                .filter(item -> item.getProduitId() == produitId)
                .findFirst()
                .orElse(null);
    }

    private void persistCart() {
        cartStorageService.saveCartItems(cartItems);
    }
}
