package edu.ProjetPI.tools;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
<<<<<<< Updated upstream

public class MyConnection {

    private String url = System.getenv().getOrDefault(
            "PIJAVA_DB_URL",
            "jdbc:mysql://localhost:3306/esportify?serverTimezone=UTC"
    );
    private String login = System.getenv().getOrDefault("PIJAVA_DB_USER", "root");
    private String pwd = System.getenv().getOrDefault("PIJAVA_DB_PASSWORD", "");
=======
import java.sql.Statement;
import java.util.Properties;

public class MyConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/esportify?serverTimezone=UTC";
    private static final String DEFAULT_ROOT_URL = "jdbc:mysql://localhost:3306/";
    private static final String DEFAULT_DB_NAME = "esportify";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private final String rootUrl;
    private final String dbName;
    private final String url;
    private final String login;
    private final String pwd;
    private final String driver;
>>>>>>> Stashed changes

    private Connection cnx;
    public static MyConnection instance;

<<<<<<< Updated upstream
    private MyConnection(){
        try {
=======
    private MyConnection() {
        Properties properties = loadProperties("db.properties");
        this.rootUrl = setting("PIJAVA_DB_ROOT_URL", properties.getProperty("db.root.url"), DEFAULT_ROOT_URL);
        this.dbName = setting("PIJAVA_DB_NAME", properties.getProperty("db.name"), DEFAULT_DB_NAME);
        this.url = setting("PIJAVA_DB_URL", properties.getProperty("db.url"), DEFAULT_URL);
        this.login = setting("PIJAVA_DB_USER", properties.getProperty("db.user"), DEFAULT_USER);
        this.pwd = setting("PIJAVA_DB_PASSWORD", properties.getProperty("db.password"), DEFAULT_PASSWORD);
        this.driver = setting("PIJAVA_DB_DRIVER", properties.getProperty("db.driver"), DEFAULT_DRIVER);

        connect();
    }

    private void connect() {
        try {
            Class.forName(driver);
            createDatabaseIfMissing();
>>>>>>> Stashed changes
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("Connexion établie!");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public Connection getCnx() {
        try {
            if (cnx == null || cnx.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            cnx = null;
        }
        return cnx;
    }

    public static MyConnection getInstance(){
        if(instance == null){
            instance = new MyConnection();
        }
        return instance;
    }
<<<<<<< Updated upstream
=======

    private static Properties loadProperties(String resource) {
        Properties properties = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            System.out.println("Impossible de lire " + resource + ": " + e.getMessage());
        }
        return properties;
    }

    private void createDatabaseIfMissing() throws SQLException {
        try (Connection rootConnection = DriverManager.getConnection(rootUrl, login, pwd);
             Statement statement = rootConnection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + dbName.replace("`", "``") + "`");
        }
    }

    private static String setting(String envKey, String configuredValue, String defaultValue) {
        String env = EnvConfig.get(envKey);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        if (configuredValue != null && !configuredValue.isBlank()) {
            return configuredValue.trim();
        }
        return defaultValue;
    }
>>>>>>> Stashed changes
}
