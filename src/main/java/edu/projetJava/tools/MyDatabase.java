package edu.projetJava.tools;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MyDatabase {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/esportify?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private final String url;
    private final String user;
    private final String password;
    private final String driver;
    private Connection connection;
    private static MyDatabase instance;

    private MyDatabase() {
        Properties properties = loadProperties("db.properties");
        this.url = setting("PIJAVA_DB_URL", properties.getProperty("db.url"), DEFAULT_URL);
        this.user = setting("PIJAVA_DB_USER", properties.getProperty("db.user"), DEFAULT_USER);
        this.password = setting("PIJAVA_DB_PASSWORD", properties.getProperty("db.password"), DEFAULT_PASSWORD);
        this.driver = setting("PIJAVA_DB_DRIVER", properties.getProperty("db.driver"), DEFAULT_DRIVER);

        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected");
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("MyDatabase connection error: " + e.getMessage());
            connection = null;
        }
    }

    public static MyDatabase getInstance() {
        if (instance == null) {
            instance = new MyDatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private static Properties loadProperties(String resource) {
        Properties properties = new Properties();
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            System.err.println("Cannot read " + resource + ": " + e.getMessage());
        }
        return properties;
    }

    private static String setting(String envKey, String configuredValue, String defaultValue) {
        String env = System.getenv(envKey);
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        if (configuredValue != null && !configuredValue.isBlank()) {
            return configuredValue.trim();
        }
        return defaultValue;
    }
}
