package edu.esportify.entities;

import java.time.LocalDateTime;

public class Candidature {
    private int id;
    private String pseudoJoueur;
    private String niveau;
    private String rolePrefere;
    private String region;
    private String disponibilite;
    private String motivation;
    private String statut;
    private LocalDateTime dateCandidature;
    private int equipeId;
    private String equipeNom;
    private String accountUsername;

    public Candidature() {
        this.dateCandidature = LocalDateTime.now();
        this.statut = "En attente";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPseudoJoueur() {
        return pseudoJoueur;
    }

    public void setPseudoJoueur(String pseudoJoueur) {
        this.pseudoJoueur = pseudoJoueur;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    public String getRolePrefere() {
        return rolePrefere;
    }

    public void setRolePrefere(String rolePrefere) {
        this.rolePrefere = rolePrefere;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDisponibilite() {
        return disponibilite;
    }

    public void setDisponibilite(String disponibilite) {
        this.disponibilite = disponibilite;
    }

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(String motivation) {
        this.motivation = motivation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCandidature() {
        return dateCandidature;
    }

    public void setDateCandidature(LocalDateTime dateCandidature) {
        this.dateCandidature = dateCandidature;
    }

    public int getEquipeId() {
        return equipeId;
    }

    public void setEquipeId(int equipeId) {
        this.equipeId = equipeId;
    }

    public String getEquipeNom() {
        return equipeNom;
    }

    public void setEquipeNom(String equipeNom) {
        this.equipeNom = equipeNom;
    }

    public String getAccountUsername() {
        return accountUsername;
    }

    public void setAccountUsername(String accountUsername) {
        this.accountUsername = accountUsername;
    }
}
