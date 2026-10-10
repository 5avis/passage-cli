package com.passage.service;

import com.passage.config.DBConnection;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseInitializer {

    private final DBConnection db = DBConnection.getInstance();

    public boolean isSchemaInitialized() {
        String sql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'ridesafe' AND table_name = 'STUDENTS'";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    public void initializeDatabase(boolean seed) throws SQLException {
        InputStream in = getClass().getClassLoader().getResourceAsStream("schema.sql");
        if (in == null) {
            throw new IllegalStateException("schema.sql resource not found in classpath.");
        }

        List<String> statements = parseSqlStatements(in);

        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {

            for (String sql : statements) {
                String trimmed = sql.trim();
                if (trimmed.isEmpty()) continue;

                // If not seeding and we hit TRUNCATE or INSERT, skip if schema already has data
                if (!seed && (trimmed.toUpperCase().startsWith("INSERT INTO") || trimmed.toUpperCase().startsWith("TRUNCATE"))) {
                    continue;
                }

                try {
                    stmt.execute(trimmed);
                } catch (SQLException e) {
                    // Ignore non-fatal warnings
                    if (!trimmed.toUpperCase().startsWith("DROP") && !trimmed.toUpperCase().startsWith("SET")) {
                        throw e;
                    }
                }
            }
        }
    }

    public void truncateAllData() throws SQLException {
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            stmt.execute("TRUNCATE TABLE SAFETY_ALERTS");
            stmt.execute("TRUNCATE TABLE JOURNEY_UPDATES");
            stmt.execute("TRUNCATE TABLE JOURNEYS");
            stmt.execute("TRUNCATE TABLE TRUSTED_CONTACTS");
            stmt.execute("TRUNCATE TABLE STUDENTS");
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private List<String> parseSqlStatements(InputStream in) {
        List<String> statements = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.startsWith("--") || trimmed.startsWith("#") || trimmed.isEmpty()) {
                    continue;
                }
                sb.append(line).append("\n");
                if (trimmed.endsWith(";")) {
                    statements.add(sb.toString().trim());
                    sb.setLength(0);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read schema.sql: " + e.getMessage(), e);
        }

        return statements;
    }
}
