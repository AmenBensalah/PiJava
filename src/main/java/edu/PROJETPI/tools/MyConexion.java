package edu.PROJETPI.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MyConexion {
    private static final String ROOT_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = resolveDatabaseName();
    private static final String URL = ROOT_URL + DB_NAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String LOGIN = "root";
    private static final String PASSWORD = "";

    private static MyConexion instance;

    private final Connection cnx;

    private MyConexion() {
        try {
            cnx = DriverManager.getConnection(URL, LOGIN, PASSWORD);
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible de se connecter a la base de donnees projetpi.", e);
        }
    }

    public static synchronized MyConexion getInstance() {
        if (instance == null) {
            instance = new MyConexion();
        }
        return instance;
    }

    public Connection getConnection() {
        return cnx;
    }

    public static void initDatabase() {
        try (Connection conn = DriverManager.getConnection(ROOT_URL, LOGIN, PASSWORD);
             Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible de creer la base de donnees projetpi.", e);
        }

        try (Connection conn = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             Statement st = conn.createStatement()) {
            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS commande (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "dateCommande DATE NOT NULL, " +
                            "total DOUBLE NOT NULL, " +
                            "clientId INT NOT NULL, " +
                            "statut VARCHAR(40) NOT NULL DEFAULT 'EN_ATTENTE', " +
                            "nom VARCHAR(120) NULL, " +
                            "prenom VARCHAR(120) NULL, " +
                            "telephone VARCHAR(40) NULL, " +
                            "adresse VARCHAR(255) NULL, " +
                            "paysLivraison VARCHAR(120) NULL, " +
                            "gouvernoratLivraison VARCHAR(120) NULL, " +
                            "codePostalLivraison VARCHAR(40) NULL, " +
                            "adresseLivraison VARCHAR(255) NULL, " +
                            "descriptionLivraison VARCHAR(500) NULL)"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS lignecommande (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "commandeId INT NOT NULL, " +
                            "produitId INT NOT NULL, " +
                            "quantite INT NOT NULL, " +
                            "prixUnitaire DOUBLE NOT NULL)"
            );

            st.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS payment (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "commandeId INT NOT NULL, " +
                            "montant DOUBLE NOT NULL, " +
                            "datePayment DATE NOT NULL)"
            );

            ensureColumnExists(conn, "commande", "dateCommande", "DATE NOT NULL");
            ensureColumnExists(conn, "commande", "total", "DOUBLE NOT NULL");
            ensureColumnExists(conn, "commande", "clientId", "INT NOT NULL");
            ensureColumnExists(conn, "commande", "statut", "VARCHAR(40) NOT NULL DEFAULT 'EN_ATTENTE'");
            ensureColumnExists(conn, "commande", "nom", "VARCHAR(120) NULL");
            ensureColumnExists(conn, "commande", "prenom", "VARCHAR(120) NULL");
            ensureColumnExists(conn, "commande", "telephone", "VARCHAR(40) NULL");
            ensureColumnExists(conn, "commande", "adresse", "VARCHAR(255) NULL");
            ensureColumnExists(conn, "commande", "paysLivraison", "VARCHAR(120) NULL");
            ensureColumnExists(conn, "commande", "gouvernoratLivraison", "VARCHAR(120) NULL");
            ensureColumnExists(conn, "commande", "codePostalLivraison", "VARCHAR(40) NULL");
            ensureColumnExists(conn, "commande", "adresseLivraison", "VARCHAR(255) NULL");
            ensureColumnExists(conn, "commande", "descriptionLivraison", "VARCHAR(500) NULL");
            ensureColumnExists(conn, "lignecommande", "commandeId", "INT NOT NULL");
            ensureColumnExists(conn, "lignecommande", "produitId", "INT NOT NULL");
            ensureColumnExists(conn, "lignecommande", "quantite", "INT NOT NULL");
            ensureColumnExists(conn, "lignecommande", "prixUnitaire", "DOUBLE NOT NULL");
            ensureColumnExists(conn, "payment", "commandeId", "INT NOT NULL");
            ensureColumnExists(conn, "payment", "montant", "DOUBLE NOT NULL");
            ensureColumnExists(conn, "payment", "datePayment", "DATE NOT NULL");
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'initialiser les tables projetpi.", e);
        }
    }

    private static void ensureColumnExists(Connection conn, String tableName, String columnName, String definition)
            throws SQLException {
        String query = "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = ?";

        try (PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, DB_NAME);
            pst.setString(2, tableName);
            pst.setString(3, columnName);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (Statement st = conn.createStatement()) {
                        st.executeUpdate(
                                "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition
                        );
                    }
                }
            }
        }
    }

    private static String resolveDatabaseName() {
        String propertyValue = System.getProperty("projetpi.db");
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }

        String envValue = System.getenv("PROJETPI_DB");
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return "projetpi";
    }
}
