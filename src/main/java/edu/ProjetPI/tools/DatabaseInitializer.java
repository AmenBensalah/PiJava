package edu.ProjetPI.tools;

import edu.ProjetPI.entities.User;
import edu.ProjetPI.services.UserService;
import edu.ProjetPI.tools.MyConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initialize() {
        Connection connection;
        try {
            connection = MyConnection.getInstance().getCnx();
        } catch (RuntimeException e) {
            throw new IllegalStateException("Database connection failed. " + e.getMessage(), e);
        }

        if (connection == null) {
            throw new IllegalStateException(
                    "Database connection failed. MyConnection returned null connection unexpectedly."
            );
        }

        String createTable = "CREATE TABLE IF NOT EXISTS users ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "full_name VARCHAR(120) NOT NULL,"
                + "pseudo VARCHAR(80),"
                + "email VARCHAR(120) NOT NULL UNIQUE,"
                + "password VARCHAR(255) NOT NULL,"
                + "role VARCHAR(20) NOT NULL"
                + ")";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTable);
            ensurePseudoColumn(connection, statement);
            migrateExistingRoles(statement);
            seedAdmin();
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to initialize database: " + e.getMessage(), e);
        }
    }

    private static void ensurePseudoColumn(Connection connection, Statement statement) throws SQLException {
        if (!columnExists(connection, "users", "pseudo")) {
            statement.executeUpdate("ALTER TABLE users ADD COLUMN pseudo VARCHAR(80)");
        }
        statement.executeUpdate(
                "UPDATE users SET pseudo = LOWER(SUBSTRING_INDEX(email, '@', 1)) "
                        + "WHERE pseudo IS NULL OR TRIM(pseudo) = ''"
        );
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(connection.getCatalog(), null, tableName, columnName)) {
            return columns.next();
        }
    }

    private static void migrateExistingRoles(Statement statement) throws SQLException {
        statement.executeUpdate("UPDATE users SET role = 'ROLE_ADMIN' WHERE role = 'ADMIN'");
        statement.executeUpdate("UPDATE users SET role = 'ROLE_JOUEUR' WHERE role = 'USER'");
    }

    private static void seedAdmin() {
        UserService userService = new UserService();
        if (userService.findByEmail("admin@admin.com").isEmpty()) {
            userService.add(new User("Administrator", "admin", "admin@admin.com", "admin123", "ROLE_ADMIN"));
        }
    }
}
