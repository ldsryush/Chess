package dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static String databaseName;
    private static String user;
    private static String password;
    private static String connectionUrl;

    /*
     * Load the MySQL driver and initial database info from db.properties.
     */
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            loadPropertiesFromResources();
        } catch (Exception ex) {
            throw new RuntimeException("Unable to process db.properties. " + ex.getMessage());
        }
    }

    /**
     * Loads database configuration from a Properties object.
     * Used during testing to simulate database misconfiguration.
     */
    public static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        user = props.getProperty("db.user");
        password = props.getProperty("db.password");

        String host = props.getProperty("db.host");
        int port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }

    /**
     * Reloads db.properties from resources and applies settings.
     * Used to restore configuration after testing error scenarios.
     */
    public static void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {throw new Exception("Unable to load db.properties");}
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to reload db.properties. " + ex.getMessage());
        }
    }

    /**
     * Creates the database if it does not already exist.
     */
    public static void createDatabase() throws DataAccessException {
        try (var conn = DriverManager.getConnection(connectionUrl, user, password)) {
            String statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
            try (var preparedStatement = conn.prepareStatement(statement)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating database: " + e.getMessage());
        }
    }

    /**
     * Returns a connection to the configured database.
     */
    public static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(connectionUrl, user, password);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException e) {
            throw new DataAccessException("Error connecting to database: " + e.getMessage());
        }
    }
}