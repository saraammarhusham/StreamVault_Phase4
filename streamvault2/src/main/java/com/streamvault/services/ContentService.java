package com.streamvault.services;

import com.streamvault.db.DatabaseConnection;
import com.streamvault.models.ContentItem;
import com.streamvault.models.Episode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContentService {

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
}