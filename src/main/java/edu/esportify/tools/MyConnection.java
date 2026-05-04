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
        this.url = properties.getProperty("db.url", DEFAULT_URL);
        this.login = properties.getProperty("db.user", DEFAULT_USER);
        this.pwd = properties.getProperty("db.password", DEFAULT_PASSWORD);
        this.driver = properties.getProperty("db.driver", DEFAULT_DRIVER);
    }

    public MyConnection(String url, String login, String pwd, String driver) {
        this.url = url;
        this.login = login;
        this.pwd = pwd;
        this.driver = driver;
    }

    public synchronized Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                Class.forName(driver);
                cnx = DriverManager.getConnection(url, login, pwd);
            }
            return cnx;
        } catch (SQLException | ClassNotFoundException e) {
            throw new IllegalStateException("Connexion JDBC impossible: " + e.getMessage(), e);
        }
    }

    public synchronized void initializeDatabase() {
        try (Statement st = getCnx().createStatement()) {
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

    public static synchronized MyConnection getInstance() {
        if (instance == null) {
            instance = new MyConnection();
        }
        return instance;
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
}
