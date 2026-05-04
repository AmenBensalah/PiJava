package edu.connexion3a77.entities;

import java.sql.Date;
import java.util.Objects;

public class Tournoi {
    private int id;
    private String nomTournoi;
    private String typeTournoi;
    private String nomJeu;
    private Date dateDebut;
    private Date dateFin;
    private int nombreParticipants;
    private double cashPrize;

    public Tournoi() {
    }

    public Tournoi(String nomTournoi, String typeTournoi, String nomJeu, Date dateDebut, Date dateFin,
                   int nombreParticipants, double cashPrize) {
        this.nomTournoi = nomTournoi;
        this.typeTournoi = typeTournoi;
        this.nomJeu = nomJeu;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nombreParticipants = nombreParticipants;
        this.cashPrize = cashPrize;
    }

    public Tournoi(int id, String nomTournoi, String typeTournoi, String nomJeu, Date dateDebut, Date dateFin,
                   int nombreParticipants, double cashPrize) {
        this.id = id;
        this.nomTournoi = nomTournoi;
        this.typeTournoi = typeTournoi;
        this.nomJeu = nomJeu;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nombreParticipants = nombreParticipants;
        this.cashPrize = cashPrize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomTournoi() {
        return nomTournoi;
    }

    public void setNomTournoi(String nomTournoi) {
        this.nomTournoi = nomTournoi;
    }

    public String getTypeTournoi() {
        return typeTournoi;
    }

    public void setTypeTournoi(String typeTournoi) {
        this.typeTournoi = typeTournoi;
    }

    public String getNomJeu() {
        return nomJeu;
    }

    public void setNomJeu(String nomJeu) {
        this.nomJeu = nomJeu;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public int getNombreParticipants() {
        return nombreParticipants;
    }

    public void setNombreParticipants(int nombreParticipants) {
        this.nombreParticipants = nombreParticipants;
    }

    public double getCashPrize() {
        return cashPrize;
    }

    public void setCashPrize(double cashPrize) {
        this.cashPrize = cashPrize;
    }

    @Override
    public String toString() {
        return "Tournoi{" +
                "id=" + id +
                ", nomTournoi='" + nomTournoi + '\'' +
                ", typeTournoi='" + typeTournoi + '\'' +
                ", nomJeu='" + nomJeu + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", nombreParticipants=" + nombreParticipants +
                ", cashPrize=" + cashPrize +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tournoi tournoi = (Tournoi) o;
        return id == tournoi.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
