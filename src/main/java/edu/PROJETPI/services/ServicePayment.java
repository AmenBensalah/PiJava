package edu.PROJETPI.services;

import edu.PROJETPI.entites.Payment;
import edu.PROJETPI.interfaces.IServicePayment;
import edu.PROJETPI.tools.MyConexion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServicePayment implements IServicePayment {
    private final Connection cnx = MyConexion.getInstance().getConnection();

    @Override
    public void add(Payment payment) throws SQLException {
        String query = "INSERT INTO payment (commandeId, montant, datePayment) VALUES (?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, payment.getCommandeId());
            pst.setDouble(2, payment.getMontant());
            pst.setDate(3, new Date(payment.getDatePayment().getTime()));
            pst.executeUpdate();
        }
    }

    @Override
    public void update(Payment payment) throws SQLException {
        String query = "UPDATE payment SET commandeId = ?, montant = ?, datePayment = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, payment.getCommandeId());
            pst.setDouble(2, payment.getMontant());
            pst.setDate(3, new Date(payment.getDatePayment().getTime()));
            pst.setInt(4, payment.getId());
            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM payment WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    @Override
    public List<Payment> readAll() throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM payment";
        try (PreparedStatement pst = cnx.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                payments.add(mapPayment(rs));
            }
        }

        return payments;
    }

    @Override
    public List<Payment> readByPeriod(java.util.Date dateDebut, java.util.Date dateFin) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM payment WHERE 1=1");

        if (dateDebut != null) {
            query.append(" AND datePayment >= ?");
        }
        if (dateFin != null) {
            query.append(" AND datePayment <= ?");
        }
        query.append(" ORDER BY datePayment DESC, id DESC");

        try (PreparedStatement pst = cnx.prepareStatement(query.toString())) {
            int index = 1;
            if (dateDebut != null) {
                pst.setDate(index++, new Date(dateDebut.getTime()));
            }
            if (dateFin != null) {
                pst.setDate(index, new Date(dateFin.getTime()));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapPayment(rs));
                }
            }
        }

        return payments;
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getInt("id"),
                rs.getInt("commandeId"),
                rs.getDouble("montant"),
                rs.getDate("datePayment")
        );
    }
}
