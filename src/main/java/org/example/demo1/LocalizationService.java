package org.example.demo1;

import java.sql.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class LocalizationService {

    private static final String DB_NAME = System.getenv().getOrDefault("DB_NAME", "bmi_localization");
    private static final String DB_USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String DB_PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "local-dev-only");

    // Dynamically determine DB host
    private static String getDatabaseHost() {
        // Try reading from environment variable (for Docker)
        String host = System.getenv("DB_HOST");

        // Fallback to localhost if not set
        if (host == null || host.isEmpty()) {
            host = "localhost";
        }

        return host;
    }

    // Build DB connection URL dynamically
    private static String getDatabaseUrl() {
        String port = System.getenv().getOrDefault("DB_PORT", "3306");
        return "jdbc:mysql://" + getDatabaseHost() + ":" + port + "/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true";
    }

    public static Map<String, String> getLocalizedStrings(Locale locale) {
        String dbUrl = getDatabaseUrl();
        return getLocalizedStrings(locale, () -> DriverManager.getConnection(dbUrl, DB_USER, DB_PASSWORD));
    }

    static Map<String, String> getLocalizedStrings(Locale locale, ConnectionProvider provider) {
        String dbUrl = getDatabaseUrl();
        try (Connection conn = provider.open()) {
            return getLocalizedStrings(conn, locale);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + dbUrl);
            return new HashMap<>();
        }
    }

    @FunctionalInterface
    interface ConnectionProvider {
        Connection open() throws SQLException;
    }

    static Map<String, String> getLocalizedStrings(Connection conn, Locale locale) throws SQLException {
        Map<String, String> strings = new HashMap<>();
        String lang = locale.getLanguage();
        String query = "SELECT `key`, value FROM localization_strings WHERE language = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, lang);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    strings.put(rs.getString("key"), rs.getString("value"));
                }
            }
        }
        return strings;
    }
}
