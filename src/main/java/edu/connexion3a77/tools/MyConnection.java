package edu.connexion3a77.tools;

import edu.esportify.config.EnvConfig;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MyConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/esportify?serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private final String url;
    private final String login;
    private final String pwd;
    private final String driver;

    private Connection cnx;
    public static MyConnection instance;

    private MyConnection() {
        Properties properties = loadProperties("db.properties");
        this.url = setting("PIJAVA_DB_URL", properties.getProperty("db.url"), DEFAULT_URL);
        this.login = setting("PIJAVA_DB_USER", properties.getProperty("db.user"), DEFAULT_USER);
        this.pwd = setting("PIJAVA_DB_PASSWORD", properties.getProperty("db.password"), DEFAULT_PASSWORD);
        this.driver = setting("PIJAVA_DB_DRIVER", properties.getProperty("db.driver"), DEFAULT_DRIVER);

        try {
            Class.forName(driver);
            cnx = DriverManager.getConnection(url, login, pwd);
            System.out.println("Connexion etablie!");
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
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
}
