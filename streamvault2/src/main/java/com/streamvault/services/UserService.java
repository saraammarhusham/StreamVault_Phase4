package com.streamvault.services;

import com.streamvault.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * UserService — subscriber dashboard queries.
 * Returns raw arrays/maps suitable for JSP rendering.
 */
public class UserService {

    // ── Active subscription ────────────────────────────────────────────────

    public static String[] getActiveSubscription(int userId) {
        String sql = "SELECT sp.plan_name, sp.price, sp.features, " +
                     "       sub.status, sub.start_date, sub.end_date " +
                     "FROM Subscriptions sub " +
                     "JOIN SubscriptionPlan sp ON sub.plan_id = sp.plan_id " +
                     "WHERE sub.user_id = ? AND sub.status = 'active' " +
                     "ORDER BY sub.start_date DESC LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new String[]{
                    rs.getString("plan_name"),
                    String.valueOf(rs.getDouble("price")),
                    rs.getString("features"),
                    rs.getString("status"),
                    String.valueOf(rs.getDate("start_date")),
                    rs.getString("end_date") != null ? String.valueOf(rs.getDate("end_date")) : "—"
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── Payment history ────────────────────────────────────────────────────

    public static List<String[]> getPaymentHistory(int userId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT p.payment_id, p.amount, p.payment_date, p.status, " +
                     "       sp.plan_name " +
                     "FROM Payments p " +
                     "JOIN Subscriptions sub ON p.subscription_id = sub.subscription_id " +
                     "JOIN SubscriptionPlan sp ON sub.plan_id = sp.plan_id " +
                     "WHERE sub.user_id = ? " +
                     "ORDER BY p.payment_date DESC LIMIT 20";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("payment_id")),
                    String.valueOf(rs.getDouble("amount")),
                    String.valueOf(rs.getDate("payment_date")),
                    rs.getString("status"),
                    rs.getString("plan_name")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Watch history (last 30 days) ───────────────────────────────────────

    public static List<String[]> getRecentWatchHistory(int userId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT c.content_id, c.title, c.content_type, " +
                     "       wh.watch_date, wh.progress_pct, wh.completed, wh.device_type " +
                     "FROM WatchHistory wh " +
                     "JOIN ContentItems c ON wh.content_id = c.content_id " +
                     "WHERE wh.user_id = ? " +
                     "  AND wh.watch_date >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                     "ORDER BY wh.watch_date DESC LIMIT 20";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("content_id")),
                    rs.getString("title"),
                    rs.getString("content_type"),
                    String.valueOf(rs.getTimestamp("watch_date")),
                    String.valueOf(rs.getInt("progress_pct")),
                    rs.getBoolean("completed") ? "Yes" : "No",
                    rs.getString("device_type")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── All subscription plans (for registration dropdown) ─────────────────

    public static List<String[]> getAllPlans() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT plan_id, plan_name, price, features FROM SubscriptionPlan ORDER BY price";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("plan_id")),
                    rs.getString("plan_name"),
                    String.valueOf(rs.getDouble("price")),
                    rs.getString("features")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
