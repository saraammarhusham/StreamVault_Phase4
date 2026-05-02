package com.streamvault.services;

import com.streamvault.db.DatabaseConnection;
import com.streamvault.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

/**
 * AuthService — handles all authentication and user-registration logic.
 * Uses BCrypt (cost=12) for password hashing.
 * All queries use PreparedStatement to prevent SQL injection.
 */
public class AuthService {

    // ── Password helpers ───────────────────────────────────────────────────

    /** Hash a plain-text password with BCrypt cost=12. */
    public static String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(12));
    }

    /** Return true if plainText matches the stored BCrypt hash. */
    public static boolean verifyPassword(String plainText, String storedHash) {
        return BCrypt.checkpw(plainText, storedHash);
    }

    // ── Login ──────────────────────────────────────────────────────────────

    /**
     * Authenticates a user by email + password.
     * Returns the User object on success, null on failure.
     */
    public static User login(String email, String password) {
        String sql = "SELECT user_id, full_name, email, password_hash, country, role, is_active "
                   + "FROM Users WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                boolean isActive  = rs.getBoolean("is_active");

                if (!isActive) return null; // account deactivated

                if (verifyPassword(password, storedHash)) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setCountry(rs.getString("country"));
                    u.setRole(rs.getString("role"));
                    u.setActive(true);
                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── Registration ───────────────────────────────────────────────────────

    /**
     * Registers a new subscriber.
     * Steps:
     *   1. Hash password
     *   2. INSERT into Users
     *   3. INSERT into Subscriptions
     *
     * Returns the new user_id, or -1 on failure.
     */
    public static int register(String fullName, String email, String password,
                               String country, String dob, int planId) {
        if (password == null || password.length() < 8) return -2; // password too short

        String insertUser = "INSERT INTO Users (full_name, email, password_hash, "
                          + "country, date_of_birth, role, is_active) "
                          + "VALUES (?, ?, ?, ?, ?, 'subscriber', 1)";

        String insertSub  = "INSERT INTO Subscriptions (user_id, plan_id, status, start_date) "
                          + "VALUES (?, ?, 'active', CURDATE())";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // BEGIN TRANSACTION

            // Step 1 – hash password
            String hash = hashPassword(password);

            // Step 2 – insert user
            PreparedStatement ps1 = conn.prepareStatement(insertUser,
                                        Statement.RETURN_GENERATED_KEYS);
            ps1.setString(1, fullName.trim());
            ps1.setString(2, email.trim().toLowerCase());
            ps1.setString(3, hash);
            ps1.setString(4, country.trim());
            ps1.setString(5, dob);          // "YYYY-MM-DD"
            ps1.executeUpdate();

            ResultSet keys = ps1.getGeneratedKeys();
            if (!keys.next()) throw new SQLException("No user_id generated.");
            int newUserId = keys.getInt(1);

            // Step 3 – insert subscription
            PreparedStatement ps2 = conn.prepareStatement(insertSub);
            ps2.setInt(1, newUserId);
            ps2.setInt(2, planId);
            ps2.executeUpdate();

            conn.commit(); // COMMIT
            return newUserId;

        } catch (SQLIntegrityConstraintViolationException e) {
            // duplicate email
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            return -3;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return -1;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    // ── Email uniqueness check ─────────────────────────────────────────────

    public static boolean emailExists(String email) {
        String sql = "SELECT 1 FROM Users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
