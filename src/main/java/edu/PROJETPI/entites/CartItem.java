package edu.PROJETPI.entites;

import java.io.Serial;
import java.io.Serializable;

public class CartItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int produitId;
    private final String nomProduit;
    private int quantite;
    private final double prixUnitaire;

    public CartItem(int produitId, String nomProduit, int quantite, double prixUnitaire) {
        this.produitId = produitId;
        this.nomProduit = nomProduit;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int getProduitId() {
        return produitId;
    }

    public String getNomProduit() {
        return nomProduit;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public double getSousTotal() {
        return quantite * prixUnitaire;
    }
}
