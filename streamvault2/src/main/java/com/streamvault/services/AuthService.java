package com.streamvault.services;

import com.streamvault.db.DatabaseConnection;
import com.streamvault.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

/**
 * AuthService — handles all authentication and user-registration logic.
 * Uses BCrypt (cost=12) for password hashing.
 * All queries use PreparedStatement to prevent SQL injection.
 *
 * ══════════════════════════════════════════════════════════════════
 *  FIX 1 — ADMIN LOGIN (broken BCrypt hash in seed data)
 * ══════════════════════════════════════════════════════════════════
 *  Root cause: the SQL seed file stored a 59-character truncated BCrypt
 *  hash for all three pre-seeded users.  A valid BCrypt hash is always
 *  exactly 60 characters.  BCrypt.checkpw() returns false (or throws)
 *  against a malformed hash, so every login attempt failed.
 *
 *  Fix applied here:
 *   • verifyPassword() now guards against malformed hashes and logs a
 *     clear error instead of silently returning false.
 *   • A new fixSeedPasswords() helper re-hashes the three seed accounts
 *     at application startup (called once from a ServletContextListener
 *     or, more simply, on the first login attempt for those accounts).
 *   • login() detects the broken-hash condition and auto-heals it when
 *     the user supplies the correct legacy password.
 *
 *  You can also fix it permanently in MySQL Workbench by running:
 *      CALL fix_seed_passwords();   -- see bottom of this file's comment
 *  or use the fixSeedPasswords() method once via a setup endpoint.
 * ══════════════════════════════════════════════════════════════════
 */
public class AuthService {

    // ── Known seed passwords (used ONLY to auto-heal broken hashes) ────────
    // These match the plaintext passwords documented in streamvault_database.sql
    private static final String[][] SEED_CREDENTIALS = {
            { "admin@streamvault.com",  "Admin@1234" },
            { "sara@streamvault.com",   "Sara@1234"  },
            { "heba@streamvault.com",   "Heba@1234"  }
    };

    // ══════════════════════════════════════════════════════════════════════
    //  PASSWORD HELPERS
    // ══════════════════════════════════════════════════════════════════════

    /** Hash a plain-text password with BCrypt cost=12. */
    public static String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt(12));
    }

    /**
     * Return true if plainText matches the stored BCrypt hash.
     *
     * FIX 1a — guards against the truncated-hash bug:
     *   A valid BCrypt hash is always 60 characters.  If the stored hash
     *   is shorter, BCrypt.checkpw() may throw an IllegalArgumentException.
     *   We catch that and return false so the caller can handle it cleanly.
     */
    public static boolean verifyPassword(String plainText, String storedHash) {
        if (storedHash == null || storedHash.length() < 60) {
            // ── FIX 1a: malformed hash detected — do not crash ────────────
            System.err.println("[AuthService] WARNING: stored hash is malformed "
                    + "(length=" + (storedHash == null ? "null" : storedHash.length()) + "). "
                    + "Run AuthService.fixSeedPasswords() to repair seed accounts.");
            return false;
        }
        try {
            return BCrypt.checkpw(plainText, storedHash);
        } catch (IllegalArgumentException e) {
            System.err.println("[AuthService] BCrypt.checkpw threw: " + e.getMessage());
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  FIX 1b — AUTO-HEAL SEED ACCOUNTS
    //  Call this once (e.g. from a @WebListener ServletContextListener
    //  contextInitialized method, or from a one-time admin setup page).
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Re-hashes the three seed accounts whose BCrypt hashes were truncated
     * in the SQL seed file.  Safe to call multiple times — it only updates
     * rows whose password_hash is shorter than 60 characters.
     */
    public static void fixSeedPasswords() {
        String checkSql  = "SELECT password_hash FROM Users WHERE email = ?";
        String updateSql = "UPDATE Users SET password_hash = ? WHERE email = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            for (String[] cred : SEED_CREDENTIALS) {
                String email    = cred[0];
                String password = cred[1];

                // Check current hash length
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setString(1, email);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        String hash = rs.getString("password_hash");
                        if (hash == null || hash.length() < 60) {
                            // Hash is broken — replace it
                            String newHash = hashPassword(password);
                            try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                                upd.setString(1, newHash);
                                upd.setString(2, email);
                                upd.executeUpdate();
                                System.out.println("[AuthService] Repaired hash for: " + email);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[AuthService] fixSeedPasswords() failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LOGIN
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Authenticates a user by email + password.
     * Returns the User object on success, null on failure.
     *
     * FIX 1c — role is now explicitly read and stored on the User object
     *   so that LoginServlet's "admin".equals(user.getRole()) check works
     *   correctly for ALL roles including content_manager and admin.
     *
     * FIX 1d — auto-heal: if the stored hash is malformed AND the email
     *   belongs to a known seed account, we re-hash and retry once.
     *   This means the admin can log in immediately without any manual
     *   SQL fix — the first successful login self-repairs the row.
     */
    public static User login(String email, String password) {
        String sql = "SELECT user_id, full_name, email, password_hash, country, role, is_active "
                + "FROM Users WHERE email = ?";

        String normalizedEmail = email.trim().toLowerCase();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, normalizedEmail);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String  storedHash = rs.getString("password_hash");
                boolean isActive   = rs.getBoolean("is_active");
                String  role       = rs.getString("role");   // FIX 1c

                if (!isActive) {
                    System.out.println("[AuthService] Login rejected — account inactive: " + normalizedEmail);
                    return null;
                }

                // ── FIX 1d: detect broken hash and auto-heal seed accounts ─
                if (storedHash == null || storedHash.length() < 60) {
                    storedHash = tryHealBrokenHash(conn, normalizedEmail, password);
                    if (storedHash == null) return null; // not a seed account or wrong password
                }

                if (verifyPassword(password, storedHash)) {
                    User u = new User();
                    u.setUserId(rs.getInt("user_id"));
                    u.setFullName(rs.getString("full_name"));
                    u.setEmail(rs.getString("email"));
                    u.setCountry(rs.getString("country"));
                    u.setRole(role);    // FIX 1c — always set role
                    u.setActive(true);
                    return u;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * FIX 1d helper — if the stored hash is broken, check whether the
     * supplied password matches the known seed password for this email,
     * re-hash it, persist it, and return the new valid hash.
     * Returns null if the email is not a seed account or password is wrong.
     */
    private static String tryHealBrokenHash(Connection conn,
                                            String email, String password) {
        for (String[] cred : SEED_CREDENTIALS) {
            if (cred[0].equalsIgnoreCase(email)) {
                if (cred[1].equals(password)) {
                    // Correct seed password — generate a proper hash and save it
                    String newHash = hashPassword(password);
                    String updateSql = "UPDATE Users SET password_hash = ? WHERE email = ?";
                    try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                        upd.setString(1, newHash);
                        upd.setString(2, email);
                        upd.executeUpdate();
                        System.out.println("[AuthService] Auto-healed broken hash for: " + email);
                        return newHash;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                return null; // seed email but wrong password
            }
        }
        return null; // not a seed account
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REGISTRATION
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Registers a new subscriber.
     * Steps:
     *   1. Hash password
     *   2. INSERT into Users
     *   3. INSERT into Subscriptions
     *
     * Returns the new user_id, or negative error code:
     *   -1  generic failure
     *   -2  password too short
     *   -3  duplicate email
     */
    public static int register(String fullName, String email, String password,
                               String country, String dob, int planId) {
        if (password == null || password.length() < 8) return -2;

        String insertUser = "INSERT INTO Users (full_name, email, password_hash, "
                + "country, date_of_birth, role, is_active) "
                + "VALUES (?, ?, ?, ?, ?, 'subscriber', 1)";

        String insertSub  = "INSERT INTO Subscriptions (user_id, plan_id, status, start_date) "
                + "VALUES (?, ?, 'active', CURDATE())";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String hash = hashPassword(password);

            PreparedStatement ps1 = conn.prepareStatement(insertUser,
                    Statement.RETURN_GENERATED_KEYS);
            ps1.setString(1, fullName.trim());
            ps1.setString(2, email.trim().toLowerCase());
            ps1.setString(3, hash);
            ps1.setString(4, country.trim());
            ps1.setString(5, dob);
            ps1.executeUpdate();

            ResultSet keys = ps1.getGeneratedKeys();
            if (!keys.next()) throw new SQLException("No user_id generated.");
            int newUserId = keys.getInt(1);

            PreparedStatement ps2 = conn.prepareStatement(insertSub);
            ps2.setInt(1, newUserId);
            ps2.setInt(2, planId);
            ps2.executeUpdate();

            conn.commit();
            return newUserId;

        } catch (SQLIntegrityConstraintViolationException e) {
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

    // ══════════════════════════════════════════════════════════════════════
    //  EMAIL UNIQUENESS CHECK
    // ══════════════════════════════════════════════════════════════════════

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