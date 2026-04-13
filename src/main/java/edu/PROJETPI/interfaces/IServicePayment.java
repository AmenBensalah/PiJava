package edu.PROJETPI.interfaces;

import edu.PROJETPI.entites.Payment;

import java.sql.SQLException;
import java.util.List;

public interface IServicePayment {
    void add(Payment payment) throws SQLException;

    void update(Payment payment) throws SQLException;

    void delete(int id) throws SQLException;

    List<Payment> readAll() throws SQLException;
}
