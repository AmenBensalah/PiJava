package edu.ProjetPI.entities;

public class User {
    private int id;
    private String fullName;
    private String pseudo;
    private String email;
    private String password;
    private String role;

    public User() {
    }

    public User(int id, String fullName, String pseudo, String email, String password, String role) {
        this.id = id;
        this.fullName = fullName;
        this.pseudo = pseudo;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(String fullName, String pseudo, String email, String password, String role) {
        this.fullName = fullName;
        this.pseudo = pseudo;
        this.email = email;
        this.password = password;
        this.role = role;
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
}
