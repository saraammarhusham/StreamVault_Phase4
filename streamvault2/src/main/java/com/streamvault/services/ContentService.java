package com.streamvault.services;

import com.streamvault.db.DatabaseConnection;
import com.streamvault.models.ContentItem;
import com.streamvault.models.Episode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContentService {

    // ══════════════════════════════════════════════════════════════════════
    //  BROWSE / FILTER CONTENT
    // ══════════════════════════════════════════════════════════════════════

    public static List<ContentItem> browseContent(String search, String genre,
                                                  String type, String language,
                                                  String sortBy, int page, int pageSize) {
        List<ContentItem> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
                "SELECT c.content_id, c.title, c.content_type, c.release_year, " +
                        "       c.duration_minutes, c.language, c.age_rating, c.poster_url, " +
                        "       s.name AS studio_name, " +
                        "       ROUND(COALESCE(AVG(r.rating), 0), 1) AS avg_rating, " +
                        "       GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS genres " +
                        "FROM ContentItems c " +
                        "JOIN Studios s ON c.studio_id = s.studio_id " +
                        "LEFT JOIN ContentGenre cg ON c.content_id = cg.content_id " +
                        "LEFT JOIN Genres g ON cg.genre_id = g.genre_id " +
                        "LEFT JOIN ReviewsRatings r ON c.content_id = r.content_id " +
                        "WHERE c.is_available = 1 "
        );

        List<Object> params = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            sql.append("AND (c.title LIKE ? OR c.synopsis LIKE ?) ");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }

        if (type != null && !type.isBlank()) {
            sql.append("AND c.content_type = ? ");
            params.add(type);
        }

        if (language != null && !language.isBlank()) {
            sql.append("AND c.language = ? ");
            params.add(language);
        }

        if (genre != null && !genre.isBlank()) {
            sql.append("AND EXISTS ( " +
                    "SELECT 1 FROM ContentGenre cg2 " +
                    "JOIN Genres g2 ON cg2.genre_id = g2.genre_id " +
                    "WHERE cg2.content_id = c.content_id " +
                    "AND g2.name = ? " +
                    ") ");
            params.add(genre);
        }

        sql.append("GROUP BY c.content_id, c.title, c.content_type, c.release_year, " +
                "c.duration_minutes, c.language, c.age_rating, c.poster_url, s.name ");

        if ("rating".equalsIgnoreCase(sortBy)) {
            sql.append("ORDER BY avg_rating DESC ");
        } else {
            sql.append("ORDER BY c.title ASC ");
        }

        int offset = (page - 1) * pageSize;
        sql.append("LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add(offset);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ContentItem item = new ContentItem();
                item.setContentId(rs.getInt("content_id"));
                item.setTitle(rs.getString("title"));
                item.setContentType(rs.getString("content_type"));
                item.setReleaseYear(rs.getInt("release_year"));
                item.setDurationMinutes(rs.getInt("duration_minutes"));
                item.setLanguage(rs.getString("language"));
                item.setAgeRating(rs.getString("age_rating"));
                item.setPosterUrl(rs.getString("poster_url"));
                item.setStudioName(rs.getString("studio_name"));
                item.setAvgRating(rs.getDouble("avg_rating"));
                item.setGenres(rs.getString("genres"));
                list.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  GET SINGLE CONTENT ITEM
    // ══════════════════════════════════════════════════════════════════════

    public static ContentItem getContentById(int contentId) {
        String sql =
                "SELECT c.content_id, c.title, c.content_type, c.synopsis, " +
                        "       c.release_year, c.duration_minutes, c.language, c.age_rating, c.poster_url, " +
                        "       s.name AS studio_name, " +
                        "       ROUND(COALESCE(AVG(r.rating), 0), 1) AS avg_rating, " +
                        "       GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS genres " +
                        "FROM ContentItems c " +
                        "JOIN Studios s ON c.studio_id = s.studio_id " +
                        "LEFT JOIN ContentGenre cg ON c.content_id = cg.content_id " +
                        "LEFT JOIN Genres g ON cg.genre_id = g.genre_id " +
                        "LEFT JOIN ReviewsRatings r ON c.content_id = r.content_id " +
                        "WHERE c.content_id = ? " +
                        "GROUP BY c.content_id, c.title, c.content_type, c.synopsis, " +
                        "c.release_year, c.duration_minutes, c.language, c.age_rating, c.poster_url, s.name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, contentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ContentItem item = new ContentItem();
                item.setContentId(rs.getInt("content_id"));
                item.setTitle(rs.getString("title"));
                item.setContentType(rs.getString("content_type"));
                item.setSynopsis(rs.getString("synopsis"));
                item.setReleaseYear(rs.getInt("release_year"));
                item.setDurationMinutes(rs.getInt("duration_minutes"));
                item.setLanguage(rs.getString("language"));
                item.setAgeRating(rs.getString("age_rating"));
                item.setPosterUrl(rs.getString("poster_url"));
                item.setStudioName(rs.getString("studio_name"));
                item.setAvgRating(rs.getDouble("avg_rating"));
                item.setGenres(rs.getString("genres"));
                return item;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  EPISODES
    // ══════════════════════════════════════════════════════════════════════

    public static List<Episode> getEpisodes(int contentId) {
        List<Episode> list = new ArrayList<>();

        String sql =
                "SELECT episode_id, season_number, episode_number, title, " +
                        "duration_minutes, synopsis " +
                        "FROM Episodes " +
                        "WHERE content_id = ? " +
                        "ORDER BY season_number, episode_number";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, contentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Episode ep = new Episode();
                ep.setEpisodeId(rs.getInt("episode_id"));
                ep.setContentId(contentId);
                ep.setSeasonNumber(rs.getInt("season_number"));
                ep.setEpisodeNumber(rs.getInt("episode_number"));
                ep.setTitle(rs.getString("title"));
                ep.setDurationMinutes(rs.getInt("duration_minutes"));
                ep.setSynopsis(rs.getString("synopsis"));
                list.add(ep);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  REVIEWS
    // ══════════════════════════════════════════════════════════════════════

    public static List<String[]> getReviews(int contentId) {
        List<String[]> reviews = new ArrayList<>();

        String sql =
                "SELECT u.full_name, r.rating, r.review_text, r.created_at " +
                        "FROM ReviewsRatings r " +
                        "JOIN Users u ON r.user_id = u.user_id " +
                        "WHERE r.content_id = ? " +
                        "ORDER BY r.created_at DESC LIMIT 20";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, contentId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                reviews.add(new String[]{
                        rs.getString("full_name"),
                        String.valueOf(rs.getDouble("rating")),
                        rs.getString("review_text"),
                        String.valueOf(rs.getTimestamp("created_at"))
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reviews;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  FIX 2 — USER MOVIE RATING
    //
    //  submitRating() — INSERT or UPDATE a rating row for (user, content).
    //  The ReviewsRatings table has a UNIQUE constraint on (user_id, content_id)
    //  so we use INSERT … ON DUPLICATE KEY UPDATE to handle re-ratings.
    //
    //  getUserRating() — fetches a user's existing rating for a content item
    //  so the JSP can pre-fill the rating widget when the user revisits the
    //  content detail page.
    //
    //  Return codes for submitRating():
    //    1  = inserted (new rating)
    //    2  = updated  (user changed their rating)
    //   -1  = failure
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Insert or update a rating + optional review text for a content item.
     *
     * @param userId     the logged-in user's ID
     * @param contentId  the content being rated
     * @param rating     0.0 – 5.0 (validated here; rejected if out of range)
     * @param reviewText optional review text; may be null or blank
     * @return 1 (inserted), 2 (updated), or -1 (error)
     */
    public static int submitRating(int userId, int contentId,
                                   double rating, String reviewText) {
        // ── Validate rating range (mirrors DB CHECK constraint) ────────────
        if (rating < 0.0 || rating > 5.0) {
            System.err.println("[ContentService] submitRating: rating out of range: " + rating);
            return -1;
        }

        // ── INSERT … ON DUPLICATE KEY UPDATE ──────────────────────────────
        // The UNIQUE(user_id, content_id) constraint means a second rating
        // from the same user simply updates the existing row instead of
        // throwing a duplicate-key error.
        String sql =
                "INSERT INTO ReviewsRatings (user_id, content_id, rating, review_text, created_at) "
                        + "VALUES (?, ?, ?, ?, NOW()) "
                        + "ON DUPLICATE KEY UPDATE "
                        + "    rating      = VALUES(rating), "
                        + "    review_text = VALUES(review_text), "
                        + "    created_at  = NOW()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, contentId);
            ps.setDouble(3, rating);

            if (reviewText != null && !reviewText.isBlank()) {
                ps.setString(4, reviewText.trim());
            } else {
                ps.setNull(4, Types.VARCHAR);
            }

            int affected = ps.executeUpdate();
            // MySQL ON DUPLICATE KEY UPDATE: 1 row = insert, 2 rows = update
            return (affected == 1) ? 1 : 2;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Returns the user's existing rating for a content item, or null if
     * they have not rated it yet.
     *
     * Returns String[] { rating, review_text } so the JSP can pre-fill
     * the rating form.
     */
    public static String[] getUserRating(int userId, int contentId) {
        String sql =
                "SELECT rating, review_text "
                        + "FROM ReviewsRatings "
                        + "WHERE user_id = ? AND content_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, contentId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new String[]{
                        String.valueOf(rs.getDouble("rating")),
                        rs.getString("review_text")
                };
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // user has not rated this content yet
    }

    // ══════════════════════════════════════════════════════════════════════
    //  WATCH HISTORY
    // ══════════════════════════════════════════════════════════════════════

    public static int recordWatch(int userId, int contentId, int episodeId,
                                  int progressPct, String deviceType) {
        String sql =
                "INSERT INTO WatchHistory " +
                        "(user_id, content_id, episode_id, watch_date, progress_pct, device_type, completed) " +
                        "VALUES (?, ?, ?, NOW(), ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setInt(2, contentId);

            if (episodeId > 0) {
                ps.setInt(3, episodeId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setInt(4, progressPct);
            ps.setString(5, deviceType != null ? deviceType : "Web");
            ps.setBoolean(6, progressPct >= 95);

            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  GENRE & LANGUAGE LOOKUPS
    // ══════════════════════════════════════════════════════════════════════

    public static List<String> getAllGenres() {
        List<String> list = new ArrayList<>();

        String sql = "SELECT name FROM Genres ORDER BY name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("name"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<String> getAllLanguages() {
        List<String> list = new ArrayList<>();

        String sql =
                "SELECT DISTINCT language " +
                        "FROM ContentItems " +
                        "WHERE language IS NOT NULL " +
                        "ORDER BY language";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("language"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }
    // ══════════════════════════════════════════════════════════════════════
    //  CONTENT MANAGER CRUD
    // ══════════════════════════════════════════════════════════════════════

    public static List<ContentItem> getAllContentForManagement() {
        List<ContentItem> list = new ArrayList<>();

        String sql =
                "SELECT c.content_id, c.studio_id, c.title, c.content_type, c.synopsis, " +
                        "       c.release_year, c.duration_minutes, c.language, c.age_rating, " +
                        "       c.is_available, c.poster_url, s.name AS studio_name " +
                        "FROM ContentItems c " +
                        "JOIN Studios s ON c.studio_id = s.studio_id " +
                        "ORDER BY c.content_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ContentItem item = new ContentItem();
                item.setContentId(rs.getInt("content_id"));
                item.setTitle(rs.getString("title"));
                item.setContentType(rs.getString("content_type"));
                item.setSynopsis(rs.getString("synopsis"));
                item.setReleaseYear(rs.getInt("release_year"));
                item.setDurationMinutes(rs.getInt("duration_minutes"));
                item.setLanguage(rs.getString("language"));
                item.setAgeRating(rs.getString("age_rating"));
                item.setPosterUrl(rs.getString("poster_url"));
                item.setStudioName(rs.getString("studio_name"));
                list.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean addContent(int studioId, String title, String contentType,
                                     String synopsis, int releaseYear, int durationMinutes,
                                     String language, String ageRating, boolean isAvailable,
                                     String posterUrl) {
        String sql =
                "INSERT INTO ContentItems " +
                        "(studio_id, title, content_type, synopsis, release_year, duration_minutes, " +
                        "language, age_rating, is_available, poster_url) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studioId);
            ps.setString(2, title);
            ps.setString(3, contentType);
            ps.setString(4, synopsis);
            ps.setInt(5, releaseYear);
            ps.setInt(6, durationMinutes);
            ps.setString(7, language);
            ps.setString(8, ageRating);
            ps.setBoolean(9, isAvailable);
            ps.setString(10, posterUrl);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateContent(int contentId, int studioId, String title,
                                        String contentType, String synopsis,
                                        int releaseYear, int durationMinutes,
                                        String language, String ageRating,
                                        boolean isAvailable, String posterUrl) {
        String sql =
                "UPDATE ContentItems SET " +
                        "studio_id = ?, title = ?, content_type = ?, synopsis = ?, " +
                        "release_year = ?, duration_minutes = ?, language = ?, age_rating = ?, " +
                        "is_available = ?, poster_url = ? " +
                        "WHERE content_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, studioId);
            ps.setString(2, title);
            ps.setString(3, contentType);
            ps.setString(4, synopsis);
            ps.setInt(5, releaseYear);
            ps.setInt(6, durationMinutes);
            ps.setString(7, language);
            ps.setString(8, ageRating);
            ps.setBoolean(9, isAvailable);
            ps.setString(10, posterUrl);
            ps.setInt(11, contentId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteContent(int contentId) {
        String sql = "DELETE FROM ContentItems WHERE content_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, contentId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}