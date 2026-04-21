package edu.PROJETPI.entites;

import java.util.Date;

public class Commande {
    private int id;
    private Date dateCommande;
    private double total;
    private int clientId;
    private String statut;
    private String nom;
    private String prenom;
    private String telephone;
    private String adresse;
    private String paysLivraison;
    private String gouvernoratLivraison;
    private String codePostalLivraison;
    private String adresseLivraison;
    private String descriptionLivraison;

    public Commande() {
    }

    public Commande(int id, Date dateCommande, double total, int clientId) {
        this.id = id;
        this.dateCommande = dateCommande;
        this.total = total;
        this.clientId = clientId;
        this.statut = "EN_ATTENTE";
    }

    public Commande(int id, Date dateCommande, double total, int clientId, String statut, String nom, String prenom, String telephone, String adresse) {
        this(id, dateCommande, total, clientId);
        this.statut = statut;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.adresse = adresse;
    }

    public Commande(Date dateCommande, double total, int clientId) {
        this.dateCommande = dateCommande;
        this.total = total;
        this.clientId = clientId;
        this.statut = "EN_ATTENTE";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(Date dateCommande) {
        this.dateCommande = dateCommande;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getPaysLivraison() {
        return paysLivraison;
    }

    public void setPaysLivraison(String paysLivraison) {
        this.paysLivraison = paysLivraison;
    }

    public String getGouvernoratLivraison() {
        return gouvernoratLivraison;
    }

    public void setGouvernoratLivraison(String gouvernoratLivraison) {
        this.gouvernoratLivraison = gouvernoratLivraison;
    }

    public String getCodePostalLivraison() {
        return codePostalLivraison;
    }

    public void setCodePostalLivraison(String codePostalLivraison) {
        this.codePostalLivraison = codePostalLivraison;
    }

    public String getAdresseLivraison() {
        return adresseLivraison;
    }

    public void setAdresseLivraison(String adresseLivraison) {
        this.adresseLivraison = adresseLivraison;
    }

    public String getDescriptionLivraison() {
        return descriptionLivraison;
    }

    public void setDescriptionLivraison(String descriptionLivraison) {
        this.descriptionLivraison = descriptionLivraison;
    }
}
