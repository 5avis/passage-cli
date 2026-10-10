package com.passage.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Thread-safe Database Connection Manager for MariaDB / MySQL.
 * Reads configuration from db.properties and supports environment variable overrides/fallbacks.
 */
public class DBConnection {

    private static final String DEFAULT_PROPERTIES_FILE = "db.properties";

    private static volatile DBConnection instance;
    private static final Object LOCK = new Object();

    private String url;
    private String user;
    private String password;
    private boolean initialized = false;

    private DBConnection() {
        loadConfiguration();
    }

    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Loads database configuration with fallback hierarchy:
     * 1. Environment variables (DB_URL, DB_USER, DB_PASSWORD)
     * 2. System properties (db.url, db.user, db.password)
     * 3. src/main/resources/db.properties file
     * 4. Smart local defaults
     */
    private synchronized void loadConfiguration() {
        Properties props = new Properties();

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // Will fallback to defaults/environment variables
        }

        // 1. URL resolution
        String resolvedUrl = System.getenv("DB_URL");
        if (resolvedUrl == null || resolvedUrl.isBlank()) {
            resolvedUrl = System.getProperty("db.url");
        }
        if (resolvedUrl == null || resolvedUrl.isBlank()) {
            resolvedUrl = props.getProperty("db.url", "jdbc:mariadb://localhost:3306/ridesafe?createDatabaseIfNotExist=true");
        }
        this.url = resolvedUrl.trim();

        // 2. User resolution
        String resolvedUser = System.getenv("DB_USER");
        if (resolvedUser == null || resolvedUser.isBlank()) {
            resolvedUser = System.getProperty("db.user");
        }
        if (resolvedUser == null || resolvedUser.isBlank()) {
            resolvedUser = props.getProperty("db.user", "root");
        }
        this.user = resolvedUser.trim();

        // 3. Password resolution
        String resolvedPassword = System.getenv("DB_PASSWORD");
        if (resolvedPassword == null) {
            resolvedPassword = System.getProperty("db.password");
        }
        if (resolvedPassword == null) {
            resolvedPassword = props.getProperty("db.password", "");
        }
        this.password = resolvedPassword;

        registerDrivers();
        this.initialized = true;
    }

    private void registerDrivers() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                // Let DriverManager detect through ServiceLoader
            }
        }
    }

    /**
     * Obtains a new active JDBC connection in a thread-safe manner.
     * If the initial connection fails with an empty password, attempts the common local fallback ("maria").
     */
    public Connection getConnection() throws SQLException {
        if (!initialized) {
            loadConfiguration();
        }

        try {
            return DriverManager.getConnection(this.url, this.user, this.password);
        } catch (SQLException e) {
            // Intelligent fallback: if password was empty and authentication failed, try common local password if available
            if ((this.password == null || this.password.isEmpty()) && e.getErrorCode() == 1045) {
                try {
                    Connection conn = DriverManager.getConnection(this.url, this.user, "maria");
                    this.password = "maria"; // Remember working fallback password
                    return conn;
                } catch (SQLException ignored) {
                    // Re-throw original exception if fallback also fails
                }
            }
            throw e;
        }
    }

    public synchronized void setCredentials(String url, String user, String password) {
        if (url != null && !url.isBlank()) this.url = url.trim();
        if (user != null && !user.isBlank()) this.user = user.trim();
        if (password != null) this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed() && conn.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }
}
