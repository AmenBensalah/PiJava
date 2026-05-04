package edu.PROJETPI.entites;

public class Produit {
    private final int id;
    private final String nom;
    private final double prix;
    private final int stock;
    private final String description;

    public Produit(int id, String nom, double prix, int stock, String description) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.stock = stock;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public double getPrix() {
        return prix;
    }

    public int getStock() {
        return stock;
    }

    public String getDescription() {
        return description;
    }
}
