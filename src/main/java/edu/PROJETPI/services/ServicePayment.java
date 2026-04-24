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
    private static final String DEFAULT_STATUS = "paid";
    private final Connection cnx = MyConexion.getInstance().getConnection();

    @Override
    public void add(Payment payment) throws SQLException {
        String query = "INSERT INTO payment (amount, created_at, status, commande_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setDouble(1, payment.getMontant());
            pst.setTimestamp(2, new java.sql.Timestamp(payment.getDatePayment().getTime()));
            pst.setString(3, normalizeStatus(payment.getStatus()));
            pst.setInt(4, payment.getCommandeId());
            pst.executeUpdate();
        }
    }

    @Override
    public void update(Payment payment) throws SQLException {
        String query = "UPDATE payment SET amount = ?, created_at = ?, status = ?, commande_id = ? WHERE id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setDouble(1, payment.getMontant());
            pst.setTimestamp(2, new java.sql.Timestamp(payment.getDatePayment().getTime()));
            pst.setString(3, normalizeStatus(payment.getStatus()));
            pst.setInt(4, payment.getCommandeId());
            pst.setInt(5, payment.getId());
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
            query.append(" AND created_at >= ?");
        }
        if (dateFin != null) {
            query.append(" AND created_at < ?");
        }
        query.append(" ORDER BY created_at DESC, id DESC");

        try (PreparedStatement pst = cnx.prepareStatement(query.toString())) {
            int index = 1;
            if (dateDebut != null) {
                pst.setTimestamp(index++, new java.sql.Timestamp(dateDebut.getTime()));
            }
            if (dateFin != null) {
                java.time.LocalDate localDate = new Date(dateFin.getTime()).toLocalDate().plusDays(1);
                pst.setTimestamp(index, java.sql.Timestamp.valueOf(localDate.atStartOfDay()));
            }
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapPayment(rs));
                }
            }
        }

        return payments;
    }

    public boolean existsByCommandeId(int commandeId) throws SQLException {
        String query = "SELECT COUNT(*) FROM payment WHERE commande_id = ?";
        try (PreparedStatement pst = cnx.prepareStatement(query)) {
            pst.setInt(1, commandeId);
            try (ResultSet rs = pst.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void addIfMissingForCommande(int commandeId, double montant, java.util.Date paymentDate, String status) throws SQLException {
        if (existsByCommandeId(commandeId)) {
            return;
        }

        Payment payment = new Payment(commandeId, montant, paymentDate);
        payment.setStatus(normalizeStatus(status));
        add(payment);
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return DEFAULT_STATUS;
        }

        return status.trim();
    }

    private Payment mapPayment(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getInt("id"),
                rs.getInt("commande_id"),
                rs.getDouble("amount"),
                rs.getTimestamp("created_at"),
                rs.getString("status")
        );
    }
}
