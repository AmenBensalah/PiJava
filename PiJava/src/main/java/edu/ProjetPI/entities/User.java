package edu.ProjetPI.entities;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String fullName;
    private String pseudo;
    private String email;
    private String password;
    private String role;
    private String faceDescriptorJson;
    private LocalDateTime lastLogin;
    private LocalDateTime warningSentAt;

    public User() {
    }

    public User(int id, String fullName, String pseudo, String email, String password, String role) {
        this(id, fullName, pseudo, email, password, role, null);
    }

    public User(int id, String fullName, String pseudo, String email, String password, String role, String faceDescriptorJson) {
        this.id = id;
        this.fullName = fullName;
        this.pseudo = pseudo;
        this.email = email;
        this.password = password;
        this.role = role;
        this.faceDescriptorJson = faceDescriptorJson;
    }

    public User(String fullName, String pseudo, String email, String password, String role) {
        this(fullName, pseudo, email, password, role, null);
    }

    public User(String fullName, String pseudo, String email, String password, String role, String faceDescriptorJson) {
        this.fullName = fullName;
        this.pseudo = pseudo;
        this.email = email;
        this.password = password;
        this.role = role;
        this.faceDescriptorJson = faceDescriptorJson;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFaceDescriptorJson() {
        return faceDescriptorJson;
    }

    public void setFaceDescriptorJson(String faceDescriptorJson) {
        this.faceDescriptorJson = faceDescriptorJson;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public LocalDateTime getWarningSentAt() {
        return warningSentAt;
    }

    public void setWarningSentAt(LocalDateTime warningSentAt) {
        this.warningSentAt = warningSentAt;
    }
}
