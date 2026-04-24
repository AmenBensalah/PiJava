package edu.PROJETPI.entites;

import java.util.Date;

public class Payment {
    private int id;
    private int commandeId;
    private double montant;
    private Date datePayment;
    private String status;

    public Payment() {}

    public Payment(int id, int commandeId, double montant, Date datePayment) {
        this.id = id;
        this.commandeId = commandeId;
        this.montant = montant;
        this.datePayment = datePayment;
    }

    public Payment(int id, int commandeId, double montant, Date datePayment, String status) {
        this(id, commandeId, montant, datePayment);
        this.status = status;
    }

    public Payment(int commandeId, double montant, Date datePayment) {
        this.commandeId = commandeId;
        this.montant = montant;
        this.datePayment = datePayment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(int commandeId) {
        this.commandeId = commandeId;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public Date getDatePayment() {
        return datePayment;
    }

    public void setDatePayment(Date datePayment) {
        this.datePayment = datePayment;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", commandeId=" + commandeId +
                ", montant=" + montant +
                ", datePayment=" + datePayment +
                ", status='" + status + '\'' +
                '}';
    }
}
