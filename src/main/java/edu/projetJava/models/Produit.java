package edu.projetJava.models;

public class Produit {
    private int id;
    private String nom;
    private int prix;
    private int stock;
    private String description;
    private String image;
    private boolean active;
    private String statut;
    private int ownerUserId;
    private int ownerEquipeId;
    private int categorieId;
    private String videoUrl;
    private String technicalSpecs;
    private String installDifficulty;

    public Produit() {}

    public Produit(int id, String nom, int prix, int stock, String description, String image, boolean active, String statut, int ownerUserId, int ownerEquipeId, int categorieId, String videoUrl, String technicalSpecs, String installDifficulty) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.stock = stock;
        this.description = description;
        this.image = image;
        this.active = active;
        this.statut = statut;
        this.ownerUserId = ownerUserId;
        this.ownerEquipeId = ownerEquipeId;
        this.categorieId = categorieId;
        this.videoUrl = videoUrl;
        this.technicalSpecs = technicalSpecs;
        this.installDifficulty = installDifficulty;
    }

    public Produit(String nom, int prix, int stock, String description, String image, boolean active, String statut, int ownerUserId, int ownerEquipeId, int categorieId, String videoUrl, String technicalSpecs, String installDifficulty) {
        this.nom = nom;
        this.prix = prix;
        this.stock = stock;
        this.description = description;
        this.image = image;
        this.active = active;
        this.statut = statut;
        this.ownerUserId = ownerUserId;
        this.ownerEquipeId = ownerEquipeId;
        this.categorieId = categorieId;
        this.videoUrl = videoUrl;
        this.technicalSpecs = technicalSpecs;
        this.installDifficulty = installDifficulty;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getPrix() {
        return prix;
    }

    public void setPrix(int prix) {
        this.prix = prix;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public int getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(int ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public int getOwnerEquipeId() {
        return ownerEquipeId;
    }

    public void setOwnerEquipeId(int ownerEquipeId) {
        this.ownerEquipeId = ownerEquipeId;
    }

    public int getCategorieId() {
        return categorieId;
    }

    public void setCategorieId(int categorieId) {
        this.categorieId = categorieId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getTechnicalSpecs() {
        return technicalSpecs;
    }

    public void setTechnicalSpecs(String technicalSpecs) {
        this.technicalSpecs = technicalSpecs;
    }

    public String getInstallDifficulty() {
        return installDifficulty;
    }

    public void setInstallDifficulty(String installDifficulty) {
        this.installDifficulty = installDifficulty;
    }

    @Override
    public String toString() {
        return "Produit{" +
                " id=" + id + "" +
                ", nom='" + nom + "'" +
                ", prix=" + prix + "" +
                ", stock=" + stock + "" +
                ", description='" + description + "'" +
                ", image='" + image + "'" +
                ", active=" + active + "" +
                ", statut='" + statut + "'" +
                ", ownerUserId=" + ownerUserId + "" +
                ", ownerEquipeId=" + ownerEquipeId + "" +
                ", categorieId=" + categorieId + "" +
                ", videoUrl='" + videoUrl + "'" +
                ", technicalSpecs='" + technicalSpecs + "'" +
                ", installDifficulty='" + installDifficulty + "'" +
                "}";
    }
}
