package com.streamvault.servlets;

import com.streamvault.models.ContentItem;
import com.streamvault.models.Episode;
import com.streamvault.models.User;
import com.streamvault.services.ContentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * ContentDetailServlet — serves the content detail page and handles two
 * POST actions:
 *
 *   action=watch  → records watch history (existing behaviour)
 *   action=rate   → FIX 2: saves a user's rating for this content item
 *
 * ══════════════════════════════════════════════════════════════════
 *  FIX 2 — USER MOVIE RATING
 * ══════════════════════════════════════════════════════════════════
 *  Added a second POST branch (action=rate) that:
 *   1. Reads ratingValue (0.0–5.0) and optional reviewText from the form.
 *   2. Validates the range server-side (mirrors the DB CHECK constraint).
 *   3. Calls ContentService.submitRating() which does an
 *      INSERT … ON DUPLICATE KEY UPDATE — so a user can update their
 *      rating without errors.
 *   4. On success, redirects back to the content page with ?rated=true
 *      so the JSP can show a confirmation banner.
 *   5. On failure, sets an error attribute and re-forwards to the JSP.
 *
 *  Admin users can visit content pages normally; the rating form is
 *  hidden in the JSP for admins (role check done there), but the
 *  servlet itself does not block admins from rating — that is a UX
 *  decision, not a security one.
 * ══════════════════════════════════════════════════════════════════
 *
 * ══════════════════════════════════════════════════════════════════
 *  FIX 1 (admin) — doGet now also passes the user's existing rating
 *  (userRating attribute) so the JSP can pre-fill the star widget.
 * ══════════════════════════════════════════════════════════════════
 */
@WebServlet("/content")
public class ContentDetailServlet extends HttpServlet {

    // ══════════════════════════════════════════════════════════════════════
    //  GET — show content detail page
    // ══════════════════════════════════════════════════════════════════════

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String idStr = req.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            res.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        int contentId = Integer.parseInt(idStr);
        ContentItem item = ContentService.getContentById(contentId);

        if (item == null) {
            res.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        List<Episode>  episodes = ContentService.getEpisodes(contentId);
        List<String[]> reviews  = ContentService.getReviews(contentId);

        // ── FIX 2: load this user's existing rating (if any) ──────────────
        // Used by the JSP to pre-fill the star widget on revisit.
        User user = (User) session.getAttribute("user");
        String[] userRating = ContentService.getUserRating(user.getUserId(), contentId);

        req.setAttribute("item",       item);
        req.setAttribute("episodes",   episodes);
        req.setAttribute("reviews",    reviews);
        req.setAttribute("userRating", userRating); // FIX 2: may be null (not yet rated)
        req.getRequestDispatcher("/content-detail.jsp").forward(req, res);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  POST — two actions: "watch" (existing) and "rate" (new, FIX 2)
    // ══════════════════════════════════════════════════════════════════════

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User   user   = (User) session.getAttribute("user");
        String action = req.getParameter("action"); // "watch" | "rate"

        // ── FIX 2: route on action parameter ──────────────────────────────
        if ("rate".equals(action)) {
            handleRate(req, res, user);
        } else {
            // Default / "watch" — original behaviour preserved unchanged
            handleWatch(req, res, user);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PRIVATE — watch handler (original logic, unchanged)
    // ══════════════════════════════════════════════════════════════════════

    private void handleWatch(HttpServletRequest req, HttpServletResponse res,
                             User user) throws IOException {

        String contentIdStr = req.getParameter("contentId");
        String episodeIdStr = req.getParameter("episodeId");
        String progressStr  = req.getParameter("progress");
        String deviceType   = req.getParameter("device");

        if (contentIdStr == null || !contentIdStr.matches("\\d+")) {
            res.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        int contentId = Integer.parseInt(contentIdStr);
        int episodeId = (episodeIdStr != null && episodeIdStr.matches("\\d+"))
                ? Integer.parseInt(episodeIdStr) : 0;
        int progress  = (progressStr  != null && progressStr.matches("\\d+"))
                ? Integer.parseInt(progressStr)  : 0;

        ContentService.recordWatch(user.getUserId(), contentId, episodeId,
                progress, deviceType != null ? deviceType : "Web");

        res.sendRedirect(req.getContextPath() + "/content?id=" + contentId + "&playing=true");
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PRIVATE — FIX 2: rate handler (new)
    // ══════════════════════════════════════════════════════════════════════

    private void handleRate(HttpServletRequest req, HttpServletResponse res,
                            User user) throws ServletException, IOException {

        String contentIdStr  = req.getParameter("contentId");
        String ratingStr     = req.getParameter("ratingValue");
        String reviewText    = req.getParameter("reviewText");

        // ── Validate contentId ─────────────────────────────────────────────
        if (contentIdStr == null || !contentIdStr.matches("\\d+")) {
            res.sendRedirect(req.getContextPath() + "/home");
            return;
        }
        int contentId = Integer.parseInt(contentIdStr);

        // ── Validate rating value ──────────────────────────────────────────
        double rating;
        try {
            rating = Double.parseDouble(ratingStr);
        } catch (NumberFormatException e) {
            forwardWithError(req, res, contentId, user,
                    "Invalid rating value. Please select between 0.5 and 5.");
            return;
        }

        if (rating < 0.0 || rating > 5.0) {
            forwardWithError(req, res, contentId, user,
                    "Rating must be between 0 and 5.");
            return;
        }

        // ── Persist via ContentService ─────────────────────────────────────
        int result = ContentService.submitRating(user.getUserId(), contentId,
                rating, reviewText);

        if (result == -1) {
            forwardWithError(req, res, contentId, user,
                    "Could not save your rating. Please try again.");
            return;
        }

        // ── Success: redirect back with confirmation flag ──────────────────
        res.sendRedirect(req.getContextPath() + "/content?id=" + contentId + "&rated=true");
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PRIVATE — helper: reload page with error message
    // ══════════════════════════════════════════════════════════════════════

    private void forwardWithError(HttpServletRequest req, HttpServletResponse res,
                                  int contentId, User user,
                                  String errorMsg) throws ServletException, IOException {

        ContentItem    item      = ContentService.getContentById(contentId);
        List<Episode>  episodes  = ContentService.getEpisodes(contentId);
        List<String[]> reviews   = ContentService.getReviews(contentId);
        String[]       userRating = ContentService.getUserRating(user.getUserId(), contentId);

        req.setAttribute("item",        item);
        req.setAttribute("episodes",    episodes);
        req.setAttribute("reviews",     reviews);
        req.setAttribute("userRating",  userRating);
        req.setAttribute("ratingError", errorMsg);
        req.getRequestDispatcher("/content-detail.jsp").forward(req, res);
    }
}