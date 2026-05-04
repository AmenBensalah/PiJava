package edu.esportify.tools;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class MyConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/esportify?serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static final String POSTS_SCHEMA_SQL = """
            CREATE TABLE IF NOT EXISTS posts (
                id INT PRIMARY KEY AUTO_INCREMENT,
                content TEXT NULL,
                media_type VARCHAR(255) DEFAULT 'text',
                media_filename VARCHAR(255) NULL,
                created_at DATETIME NOT NULL,
                image_path VARCHAR(255) NULL,
                video_url VARCHAR(255) NULL,
                is_event TINYINT(1) NOT NULL DEFAULT 0,
                event_title VARCHAR(180) NULL,
                event_date DATETIME NULL,
                event_location VARCHAR(255) NULL,
                max_participants INT NULL,
                author_id INT NULL
            )
            """;
    private static final String ANNOUNCEMENTS_SCHEMA_SQL = """
            CREATE TABLE IF NOT EXISTS announcements (
                id INT PRIMARY KEY AUTO_INCREMENT,
                title VARCHAR(180) NOT NULL,
                content TEXT NULL,
                tag VARCHAR(60) NOT NULL,
                link VARCHAR(255) NULL,
                created_at DATETIME NOT NULL,
                media_type VARCHAR(255) NOT NULL DEFAULT 'text',
                media_filename VARCHAR(255) NULL
            )
            """;
    private static final String COMMENTAIRES_SCHEMA_SQL = """
            CREATE TABLE IF NOT EXISTS commentaires (
                id INT PRIMARY KEY AUTO_INCREMENT,
                author_id INT NOT NULL,
                post_id INT NOT NULL,
                content TEXT NOT NULL,
                created_at DATETIME NOT NULL
            )
            """;
    private static final String POST_LIKES_SCHEMA_SQL = """
            CREATE TABLE IF NOT EXISTS post_likes (
                post_id INT NOT NULL,
                user_id INT NOT NULL,
                created_at DATETIME NOT NULL,
                PRIMARY KEY (post_id, user_id)
            )
            """;
    private static final String POST_SAVES_SCHEMA_SQL = """
            CREATE TABLE IF NOT EXISTS post_saves (
                post_id INT NOT NULL,
                user_id INT NOT NULL,
                created_at DATETIME NOT NULL,
                PRIMARY KEY (post_id, user_id)
            )
            """;
    private static final String POST_SHARES_SCHEMA_SQL = """
            CREATE TABLE IF NOT EXISTS post_shares (
                id INT PRIMARY KEY AUTO_INCREMENT,
                post_id INT NOT NULL,
                user_id INT NOT NULL,
                created_at DATETIME NOT NULL
            )
            """;
    private static final String CONVERSATIONS_SCHEMA_SQL = """
            CREATE TABLE IF NOT EXISTS conversations (
                id INT PRIMARY KEY AUTO_INCREMENT,
                created_at DATETIME NOT NULL,
                updated_at DATETIME NOT NULL,
                last_message_id INT NULL
            )
            """;
    private static final String CONVERSATION_PARTICIPANTS_SCHEMA_SQL = """
            CREATE TABLE IF NOT EXISTS conversation_participants (
                conversation_id INT NOT NULL,
                user_id INT NOT NULL,
                joined_at DATETIME NOT NULL,
                PRIMARY KEY (conversation_id, user_id)
            )
            """;
    private static final String MESSAGES_SCHEMA_SQL = """
            CREATE TABLE IF NOT EXISTS messages (
                id INT PRIMARY KEY AUTO_INCREMENT,
                conversation_id INT NOT NULL,
                sender_id INT NOT NULL,
                content TEXT NULL,
                attachment_path VARCHAR(255) NULL,
                created_at DATETIME NOT NULL,
                seen_at DATETIME NULL
            )
            """;

    private final String url;
    private final String login;
    private final String pwd;
    private final String driver;
    private Connection cnx;

    private static MyConnection instance;

    private MyConnection() {
        Properties properties = loadProperties("db.properties");
        this.url = System.getProperty("db.url", properties.getProperty("db.url", DEFAULT_URL));
        this.login = System.getProperty("db.user", properties.getProperty("db.user", DEFAULT_USER));
        this.pwd = System.getProperty("db.password", properties.getProperty("db.password", DEFAULT_PASSWORD));
        this.driver = System.getProperty("db.driver", properties.getProperty("db.driver", DEFAULT_DRIVER));
        tryConnectAndInitialize();
    }

    public MyConnection(String url, String login, String pwd, String driver) {
        this.url = url;
        this.login = login;
        this.pwd = pwd;
        this.driver = driver;
        tryConnectAndInitialize();
    }

    public static synchronized MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
    }

    public synchronized Connection getCnx() {
        if (cnx == null) {
            tryConnectAndInitialize();
        } else {
            try {
                if (cnx.isClosed()) {
                    cnx = null;
                    tryConnectAndInitialize();
                }
            } catch (SQLException e) {
                cnx = null;
                tryConnectAndInitialize();
            }
        }
        return cnx;
    }

    public synchronized void initializeDatabase() {
        if (getCnx() == null) {
            return;
        }
        try (Statement st = cnx.createStatement()) {
            st.executeUpdate(POSTS_SCHEMA_SQL);
            st.executeUpdate(ANNOUNCEMENTS_SCHEMA_SQL);
            st.executeUpdate(COMMENTAIRES_SCHEMA_SQL);
            st.executeUpdate(POST_LIKES_SCHEMA_SQL);
            st.executeUpdate(POST_SAVES_SCHEMA_SQL);
            st.executeUpdate(POST_SHARES_SCHEMA_SQL);
            st.executeUpdate(CONVERSATIONS_SCHEMA_SQL);
            st.executeUpdate(CONVERSATION_PARTICIPANTS_SCHEMA_SQL);
            st.executeUpdate(MESSAGES_SCHEMA_SQL);
        } catch (SQLException e) {
            throw new IllegalStateException("Initialisation de la base impossible: " + e.getMessage(), e);
        }
    }

    private void tryConnectAndInitialize() {
        try {
            Class.forName(driver);
            cnx = DriverManager.getConnection(url, login, pwd);
            initializeCoreSchema();
            initializeDatabase();
            System.out.println("Connexion etablie !");
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Connexion base indisponible, bascule en mode local: " + e.getMessage());
            closeQuietly();
            cnx = null;
        }
    }

    private void initializeCoreSchema() throws SQLException {
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

    private Properties loadProperties(String resource) {
        Properties properties = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Lecture du fichier de configuration impossible: " + e.getMessage(), e);
        }
        return properties;
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
