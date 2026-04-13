package edu.esportify.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MyConnection {
    private final String url = System.getProperty(
            "db.url",
            "jdbc:mysql://localhost:3306/esportify?serverTimezone=UTC"
    );
    private final String login = System.getProperty("db.user", "root");
    private final String pwd = System.getProperty("db.password", "");

    private Connection cnx;
    private static MyConnection instance;

    private MyConnection() {
        try {
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("Connexion etablie !");
            initializeSchema();
        } catch (SQLException e) {
            System.out.println("Connexion base indisponible, bascule en mode local: " + e.getMessage());
            closeQuietly();
            cnx = null;
        }
    }

    public Connection getCnx() {
        return cnx;
    }

    public static MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    private void initializeSchema() throws SQLException {
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS equipe (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    nom_equipe VARCHAR(255) NOT NULL,
                    logo VARCHAR(255) NULL,
                    description TEXT NOT NULL,
                    date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    classement VARCHAR(50) NOT NULL,
                    tag VARCHAR(255) NULL,
                    region VARCHAR(100) NULL,
                    max_members INT NOT NULL DEFAULT 5,
                    is_private BOOLEAN NOT NULL DEFAULT FALSE,
                    is_active BOOLEAN NOT NULL DEFAULT TRUE,
                    discord_invite_url VARCHAR(255) NULL,
                    manager_username VARCHAR(100) NULL
                )
                """);
            st.executeUpdate("ALTER TABLE equipe ADD COLUMN IF NOT EXISTS manager_username VARCHAR(100) NULL");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS recrutement (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    nom_rec VARCHAR(255) NOT NULL,
                    description TEXT NOT NULL,
                    status VARCHAR(50) NOT NULL,
                    date_publication TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    equipe_id INT NOT NULL,
                    CONSTRAINT fk_recrutement_equipe
                        FOREIGN KEY (equipe_id) REFERENCES equipe(id)
                        ON DELETE CASCADE
                )
                """);
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS candidature (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    pseudo_joueur VARCHAR(100) NOT NULL,
                    niveau VARCHAR(50) NOT NULL,
                    role_prefere VARCHAR(50) NOT NULL,
                    region VARCHAR(100) NOT NULL,
                    disponibilite VARCHAR(100) NOT NULL,
                    motivation TEXT NOT NULL,
                    statut VARCHAR(50) NOT NULL DEFAULT 'En attente',
                    date_candidature TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    equipe_id INT NOT NULL,
                    CONSTRAINT fk_candidature_equipe
                        FOREIGN KEY (equipe_id) REFERENCES equipe(id)
                        ON DELETE CASCADE
                )
                """);
            st.executeUpdate("ALTER TABLE candidature ADD COLUMN IF NOT EXISTS pseudo_joueur VARCHAR(100) NULL");
            st.executeUpdate("ALTER TABLE candidature ADD COLUMN IF NOT EXISTS niveau VARCHAR(50) NULL");
            st.executeUpdate("ALTER TABLE candidature ADD COLUMN IF NOT EXISTS role_prefere VARCHAR(50) NULL");
            st.executeUpdate("ALTER TABLE candidature ADD COLUMN IF NOT EXISTS play_style VARCHAR(50) NULL");
            st.executeUpdate("ALTER TABLE candidature ADD COLUMN IF NOT EXISTS region VARCHAR(100) NULL");
            st.executeUpdate("ALTER TABLE candidature ADD COLUMN IF NOT EXISTS disponibilite VARCHAR(100) NULL");
            st.executeUpdate("ALTER TABLE candidature ADD COLUMN IF NOT EXISTS motivation TEXT NULL");
            st.executeUpdate("ALTER TABLE candidature ADD COLUMN IF NOT EXISTS reason TEXT NULL");
            st.executeUpdate("ALTER TABLE candidature ADD COLUMN IF NOT EXISTS statut VARCHAR(50) NULL");
            st.executeUpdate("ALTER TABLE candidature ADD COLUMN IF NOT EXISTS date_candidature TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP");
            st.executeUpdate("ALTER TABLE candidature ADD COLUMN IF NOT EXISTS equipe_id INT NULL");
            st.executeUpdate("ALTER TABLE candidature ADD COLUMN IF NOT EXISTS account_username VARCHAR(100) NULL");
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS manager_request (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    username VARCHAR(100) NOT NULL,
                    email VARCHAR(190) NOT NULL,
                    niveau VARCHAR(50) NULL,
                    motivation TEXT NOT NULL,
                    status VARCHAR(50) NOT NULL DEFAULT 'En attente',
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
            st.executeUpdate("ALTER TABLE manager_request ADD COLUMN IF NOT EXISTS niveau VARCHAR(50) NULL");
            st.executeUpdate("ALTER TABLE manager_request ADD COLUMN IF NOT EXISTS username VARCHAR(100) NULL");
            st.executeUpdate("ALTER TABLE manager_request ADD COLUMN IF NOT EXISTS email VARCHAR(190) NULL");
        }
    }

    private void closeQuietly() {
        if (cnx == null) {
            return;
        }
        try {
            cnx.close();
        } catch (SQLException ignored) {
        }
    }
}
