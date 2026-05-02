package com.streamvault.services;

import com.streamvault.db.DatabaseConnection;
import com.streamvault.db.MongoDBConnection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

/**
 * AnalyticsService — SQL + MongoDB analytics for the Admin page.
 */
public class AnalyticsService {

    // ── MySQL: Top 10 content by view count ───────────────────────────────

    public static List<String[]> getTop10Content() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT c.title, COUNT(w.history_id) AS total_views " +
                     "FROM ContentItems c " +
                     "JOIN WatchHistory w ON c.content_id = w.content_id " +
                     "GROUP BY c.content_id, c.title " +
                     "ORDER BY total_views DESC LIMIT 10";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new String[]{ rs.getString("title"),
                                       String.valueOf(rs.getInt("total_views")) });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── MySQL: Revenue by subscription plan ───────────────────────────────

    public static List<String[]> getRevenueByPlan() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT sp.plan_name, " +
                     "       SUM(p.amount) AS total_revenue, " +
                     "       COUNT(p.payment_id) AS payment_count " +
                     "FROM Payments p " +
                     "JOIN Subscriptions s  ON p.subscription_id = s.subscription_id " +
                     "JOIN SubscriptionPlan sp ON s.plan_id = sp.plan_id " +
                     "WHERE p.status = 'completed' " +
                     "GROUP BY sp.plan_name " +
                     "ORDER BY total_revenue DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("plan_name"),
                    String.valueOf(rs.getDouble("total_revenue")),
                    String.valueOf(rs.getInt("payment_count"))
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── MySQL: Content rating ranking with RANK() ─────────────────────────

    public static List<String[]> getRatedContent() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT title, avg_rating, rating_rank FROM ( " +
                     "  SELECT c.title, " +
                     "         ROUND(AVG(r.rating), 2) AS avg_rating, " +
                     "         RANK() OVER (ORDER BY AVG(r.rating) DESC) AS rating_rank " +
                     "  FROM ContentItems c " +
                     "  JOIN ReviewsRatings r ON c.content_id = r.content_id " +
                     "  GROUP BY c.content_id, c.title " +
                     ") ranked LIMIT 10";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("title"),
                    String.valueOf(rs.getDouble("avg_rating")),
                    String.valueOf(rs.getInt("rating_rank"))
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── MySQL: Churn-risk users (inactive 30+ days) ───────────────────────

    public static List<String[]> getChurnRiskUsers() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT u.user_id, u.full_name, u.email, u.country, " +
                     "       MAX(wh.watch_date) AS last_watch " +
                     "FROM Users u " +
                     "LEFT JOIN WatchHistory wh ON u.user_id = wh.user_id " +
                     "WHERE u.is_active = 1 " +
                     "GROUP BY u.user_id, u.full_name, u.email, u.country " +
                     "HAVING last_watch IS NULL " +
                     "    OR last_watch < DATE_SUB(NOW(), INTERVAL 30 DAY) " +
                     "ORDER BY last_watch ASC LIMIT 20";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("user_id")),
                    rs.getString("full_name"),
                    rs.getString("email"),
                    rs.getString("country"),
                    rs.getString("last_watch") != null ? rs.getString("last_watch") : "Never"
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── MongoDB: Top 10 content by completed views ────────────────────────

    public static List<String[]> getMongoTop10() {
        List<String[]> list = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection<Document> col = db.getCollection("watch_history");

            List<Bson> pipeline = Arrays.asList(
                match(eq("completed", true)),
                group("$content_id", sum("completed_views", 1)),
                sort(descending("completed_views")),
                limit(10)
            );

            for (Document doc : col.aggregate(pipeline)) {
                list.add(new String[]{
                    String.valueOf(doc.get("_id")),
                    String.valueOf(doc.getInteger("completed_views", 0))
                });
            }
        } catch (Exception e) {
            // MongoDB may not be running — return empty gracefully
            list.add(new String[]{"MongoDB unavailable", "0"});
        }
        return list;
    }

    // ── MongoDB: Genre popularity by country ──────────────────────────────

    public static List<String[]> getMongoGenreByCountry() {
        List<String[]> list = new ArrayList<>();
        try {
            MongoDatabase db = MongoDBConnection.getDatabase();
            MongoCollection<Document> col = db.getCollection("watch_history");

            List<Bson> pipeline = Arrays.asList(
                lookup("users",   "user_id",    "_id", "user_info"),
                unwind("$user_info"),
                lookup("content", "content_id", "_id", "content_info"),
                unwind("$content_info"),
                unwind("$content_info.genres"),
                group(new Document("country", "$user_info.country")
                          .append("genre", "$content_info.genres"),
                      sum("total_views", 1)),
                sort(descending("total_views")),
                limit(20)
            );

            for (Document doc : col.aggregate(pipeline)) {
                Document id = (Document) doc.get("_id");
                list.add(new String[]{
                    id.getString("country"),
                    id.getString("genre"),
                    String.valueOf(doc.getInteger("total_views", 0))
                });
            }
        } catch (Exception e) {
            list.add(new String[]{"MongoDB unavailable", "—", "0"});
        }
        return list;
    }

    // ── helper (Mongo aggregation stages not in static import) ────────────
    private static Bson lookup(String from, String local, String foreign, String as) {
        return new Document("$lookup", new Document("from", from)
                .append("localField", local)
                .append("foreignField", foreign)
                .append("as", as));
    }
    private static Bson unwind(String path) {
        return new Document("$unwind", path);
    }
}
