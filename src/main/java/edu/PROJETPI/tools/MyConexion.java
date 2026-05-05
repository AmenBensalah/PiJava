package edu.PROJETPI.tools;

import edu.esportify.config.EnvConfig;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class MyConexion {
    private static final Properties DB_PROPERTIES = loadDbProperties();
    private static final String ROOT_URL = setting(
            "PIJAVA_DB_ROOT_URL",
            DB_PROPERTIES.getProperty("db.root.url"),
            "jdbc:mysql://localhost:3306/"
    );
    private static final String DB_NAME = resolveDatabaseName();
    private static final String URL = setting(
            "PIJAVA_DB_URL",
            DB_PROPERTIES.getProperty("db.url"),
            ROOT_URL + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
    );
    private static final String LOGIN = setting(
            "PIJAVA_DB_USER",
            DB_PROPERTIES.getProperty("db.user"),
            "root"
    );
    private static final String PASSWORD = setting(
            "PIJAVA_DB_PASSWORD",
            DB_PROPERTIES.getProperty("db.password"),
            ""
    );

    private static MyConexion instance;

    private final Connection cnx;

    private MyConexion() {
        try {
            cnx = DriverManager.getConnection(URL, LOGIN, PASSWORD);
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible de se connecter a la base de donnees.", e);
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
        createDatabaseIfMissing();

        if ("esportify".equalsIgnoreCase(DB_NAME)) {
            ensureProductionSchema();
            return;
        }

        resetTestDatabase();
    }

    private static void createDatabaseIfMissing() {
        try (Connection conn = DriverManager.getConnection(ROOT_URL, LOGIN, PASSWORD);
             Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible de creer la base de donnees.", e);
        }
    }

    private static void resetTestDatabase() {
        try (Connection conn = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             Statement st = conn.createStatement()) {
            st.executeUpdate("DROP TABLE IF EXISTS payment");
            st.executeUpdate("DROP TABLE IF EXISTS lignecommande");
            st.executeUpdate("DROP TABLE IF EXISTS commande");
            createSchemaTables(st);
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible d'initialiser les tables.", e);
        }
    }

    private static void ensureProductionSchema() {
        try (Connection conn = DriverManager.getConnection(URL, LOGIN, PASSWORD);
             Statement st = conn.createStatement()) {
            createSchemaTables(st);

            if (!columnExists(conn, "payment", "status")) {
                st.executeUpdate("ALTER TABLE payment ADD COLUMN status VARCHAR(255) NOT NULL DEFAULT 'paid'");
            }
            st.executeUpdate("UPDATE payment SET status = 'paid' WHERE status IS NULL OR TRIM(status) = ''");
        } catch (SQLException e) {
            throw new IllegalStateException("Impossible de verifier la table payment.", e);
        }
    }

    private static void createSchemaTables(Statement st) throws SQLException {
        st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS produit (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "nom VARCHAR(255) NOT NULL, " +
                        "prix INT NOT NULL DEFAULT 0, " +
                        "stock INT NOT NULL DEFAULT 0, " +
                        "description TEXT NULL, " +
                        "image VARCHAR(500) NULL, " +
                        "active TINYINT(1) NOT NULL DEFAULT 1, " +
                        "statut VARCHAR(50) NULL, " +
                        "owner_user_id INT NULL, " +
                        "owner_equipe_id INT NULL, " +
                        "categorie_id INT NULL, " +
                        "video_url VARCHAR(500) NULL, " +
                        "technical_specs TEXT NULL, " +
                        "install_difficulty VARCHAR(80) NULL)"
        );

        st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS commande (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "nom VARCHAR(255) NULL, " +
                        "prenom VARCHAR(255) NULL, " +
                        "adresse VARCHAR(255) NULL, " +
                        "quantite INT NULL, " +
                        "numtel INT NULL, " +
                        "statut VARCHAR(40) NOT NULL DEFAULT 'EN_ATTENTE', " +
                        "pays VARCHAR(120) NULL, " +
                        "gouvernerat VARCHAR(120) NULL, " +
                        "code_postal VARCHAR(40) NULL, " +
                        "adresse_detail VARCHAR(500) NULL, " +
                        "user_id INT NULL, " +
                        "identity_key VARCHAR(190) NULL, " +
                        "ai_blocked TINYINT(1) NOT NULL DEFAULT 0, " +
                        "ai_risk_score DOUBLE NULL, " +
                        "ai_block_reason VARCHAR(500) NULL, " +
                        "ai_blocked_at DATETIME NULL, " +
                        "ai_block_until DATETIME NULL)"
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
                        "amount DOUBLE NOT NULL, " +
                        "created_at DATETIME NOT NULL, " +
                        "status VARCHAR(255) NOT NULL DEFAULT 'paid', " +
                        "commande_id INT NOT NULL)"
        );
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private static String resolveDatabaseName() {
        String systemValue = trimToNull(System.getProperty("projetpi.db"));
        if (systemValue != null) {
            return systemValue;
        }

        String envValue = trimToNull(EnvConfig.get("PROJETPI_DB"));
        if (envValue != null) {
            return envValue;
        }

        String genericSystem = trimToNull(System.getProperty("db.name"));
        if (genericSystem != null) {
            return genericSystem;
        }

        String genericEnv = trimToNull(EnvConfig.get("PIJAVA_DB_NAME"));
        if (genericEnv != null) {
            return genericEnv;
        }

        String propertyDbName = trimToNull(DB_PROPERTIES.getProperty("db.name"));
        if (propertyDbName != null) {
            return propertyDbName;
        }

        String dbFromUrl = extractDbNameFromUrl(setting("PIJAVA_DB_URL", DB_PROPERTIES.getProperty("db.url"), null));
        return dbFromUrl == null ? "esportify" : dbFromUrl;
    }

    private static String extractDbNameFromUrl(String jdbcUrl) {
        String value = trimToNull(jdbcUrl);
        if (value == null) {
            return null;
        }
        int slashIndex = value.lastIndexOf('/');
        if (slashIndex < 0 || slashIndex == value.length() - 1) {
            return null;
        }
        String tail = value.substring(slashIndex + 1);
        int queryIndex = tail.indexOf('?');
        if (queryIndex >= 0) {
            tail = tail.substring(0, queryIndex);
        }
        return trimToNull(tail);
    }

    private static Properties loadDbProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException ignored) {
            // Optional file, defaults remain available.
        }
        return properties;
    }

    private static String setting(String envKey, String configuredValue, String defaultValue) {
        String env = trimToNull(EnvConfig.get(envKey));
        if (env != null) {
            return env;
        }
        String configured = trimToNull(configuredValue);
        if (configured != null) {
            return configured;
        }
        return defaultValue;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
