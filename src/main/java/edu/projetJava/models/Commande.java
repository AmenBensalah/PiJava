package edu.projetJava.models;

public class Commande {
    private int id;
    private int produitId;
    private String nomClient;
    private String emailClient;
    private String methodePaiement;
    private int quantite;
    private double prixTotal;
    private String dateCommande;

    public Commande() {
    }

    public Commande(int produitId, String nomClient, String emailClient, String methodePaiement, int quantite, double prixTotal, String dateCommande) {
        this.produitId = produitId;
        this.nomClient = nomClient;
        this.emailClient = emailClient;
        this.methodePaiement = methodePaiement;
        this.quantite = quantite;
        this.prixTotal = prixTotal;
        this.dateCommande = dateCommande;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getProduitId() { return produitId; }
    public void setProduitId(int produitId) { this.produitId = produitId; }
    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }
    public String getEmailClient() { return emailClient; }
    public void setEmailClient(String emailClient) { this.emailClient = emailClient; }
    public String getMethodePaiement() { return methodePaiement; }
    public void setMethodePaiement(String methodePaiement) { this.methodePaiement = methodePaiement; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
    public double getPrixTotal() { return prixTotal; }
    public void setPrixTotal(double prixTotal) { this.prixTotal = prixTotal; }
    public String getDateCommande() { return dateCommande; }
    public void setDateCommande(String dateCommande) { this.dateCommande = dateCommande; }
}
