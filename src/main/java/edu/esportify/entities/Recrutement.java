package edu.esportify.entities;

import java.time.LocalDateTime;

public class Recrutement {
    private int id;
    private String nomRec;
    private String description;
    private String status;
    private LocalDateTime datePublication;
    private int equipeId;
    private String equipeNom;

    public Recrutement() {
        this.datePublication = LocalDateTime.now();
    }

    public Recrutement(String nomRec, String description, String status, int equipeId) {
        this();
        this.nomRec = nomRec;
        this.description = description;
        this.status = status;
        this.equipeId = equipeId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomRec() {
        return nomRec;
    }

    public void setNomRec(String nomRec) {
        this.nomRec = nomRec;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDateTime datePublication) {
        this.datePublication = datePublication;
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

    @Override
    public String toString() {
        return nomRec + " - " + status;
    }
}
