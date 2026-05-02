package com.streamvault.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection — single-responsibility utility that opens and closes
 * JDBC connections to the streamvault MySQL database.
 *
 * Change DB_USER / DB_PASSWORD to match your local MySQL credentials.
 */
public class DatabaseConnection {

    // ── CHANGE DB_PASSWORD to match your MySQL installation ───────────────
    //    DB name is "streamvault" — matches the streamvault_database.sql file
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/streamvault"
                                            + "?useSSL=false"
                                            + "&serverTimezone=UTC"
                                            + "&allowPublicKeyRetrieval=true"
                                            + "&characterEncoding=UTF-8";
    private static final String DB_USER     = "root";          // ← your MySQL username
    private static final String DB_PASSWORD = "777888777888";  // ← YOUR MySQL password here
    // ───────────────────────────────────────────────────────────────────────

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found. Add mysql-connector-j to pom.xml", e);
        }
    }

    /**
     * Opens a fresh connection to the streamvault database.
     * Use inside try-with-resources so it closes automatically.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /** Convenience close — safe to call with null. */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
        }
    }
}
