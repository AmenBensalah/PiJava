package edu.esportify.entities;

import java.time.LocalDateTime;

public class Equipe {
    private int id;
    private String nomEquipe;
    private String logo;
    private String description;
    private LocalDateTime dateCreation;
    private String classement;
    private String tag;
    private String region;
    private int maxMembers;
    private boolean isPrivate;
    private boolean isActive;
    private String discordInviteUrl;
    private String managerUsername;
    private LocalDateTime bannedUntil;
    private String banReason;
    private String banDetails;
    private String bannedByAdmin;

    public Equipe() {
        this.dateCreation = LocalDateTime.now();
        this.maxMembers = 5;
        this.isActive = true;
    }

    public Equipe(String nomEquipe, String description, String classement, String tag, String region) {
        this();
        this.nomEquipe = nomEquipe;
        this.description = description;
        this.classement = classement;
        this.tag = tag;
        this.region = region;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomEquipe() {
        return nomEquipe;
    }

    public void setNomEquipe(String nomEquipe) {
        this.nomEquipe = nomEquipe;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getClassement() {
        return classement;
    }

    public void setClassement(String classement) {
        this.classement = classement;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getDiscordInviteUrl() {
        return discordInviteUrl;
    }

    public void setDiscordInviteUrl(String discordInviteUrl) {
        this.discordInviteUrl = discordInviteUrl;
    }

    public String getManagerUsername() {
        return managerUsername;
    }

    public void setManagerUsername(String managerUsername) {
        this.managerUsername = managerUsername;
    }

    public LocalDateTime getBannedUntil() {
        return bannedUntil;
    }

    public void setBannedUntil(LocalDateTime bannedUntil) {
        this.bannedUntil = bannedUntil;
    }

    public String getBanReason() {
        return banReason;
    }

    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }

    public String getBanDetails() {
        return banDetails;
    }

    public void setBanDetails(String banDetails) {
        this.banDetails = banDetails;
    }

    public String getBannedByAdmin() {
        return bannedByAdmin;
    }

    public void setBannedByAdmin(String bannedByAdmin) {
        this.bannedByAdmin = bannedByAdmin;
    }

    public boolean isCurrentlyBanned() {
        return bannedUntil != null && bannedUntil.isAfter(LocalDateTime.now());
    }

    public boolean isAvailable() {
        return isActive && !isCurrentlyBanned();
    }

    public void clearBan() {
        bannedUntil = null;
        banReason = null;
        banDetails = null;
        bannedByAdmin = null;
    }

    @Override
    public String toString() {
        return nomEquipe + " [" + tag + "]";
    }
}
