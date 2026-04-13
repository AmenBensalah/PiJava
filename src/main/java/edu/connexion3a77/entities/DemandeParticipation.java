package edu.connexion3a77.entities;

import java.util.Objects;

public class DemandeParticipation {
    private int id;
    private int tournoiId;
    private String description;
    private String niveau;

    public DemandeParticipation() {
    }

    public DemandeParticipation(int tournoiId, String description, String niveau) {
        this.tournoiId = tournoiId;
        this.description = description;
        this.niveau = niveau;
    }

    public DemandeParticipation(int id, int tournoiId, String description, String niveau) {
        this.id = id;
        this.tournoiId = tournoiId;
        this.description = description;
        this.niveau = niveau;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTournoiId() {
        return tournoiId;
    }

    public void setTournoiId(int tournoiId) {
        this.tournoiId = tournoiId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNiveau() {
        return niveau;
    }

    public void setNiveau(String niveau) {
        this.niveau = niveau;
    }

    @Override
    public String toString() {
        return "DemandeParticipation{" +
                "id=" + id +
                ", tournoiId=" + tournoiId +
                ", description='" + description + '\'' +
                ", niveau='" + niveau + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DemandeParticipation that = (DemandeParticipation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
