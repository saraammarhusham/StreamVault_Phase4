package com.streamvault.servlets;

import com.streamvault.services.AnalyticsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * AdminAnalyticsServlet — serves the admin analytics dashboard.
 *
 * ══════════════════════════════════════════════════════════════════
 *  FIX 1 — ADMIN ACCESS GUARD
 * ══════════════════════════════════════════════════════════════════
 *  The original guard was correct in structure but could silently
 *  fail if the "role" session attribute was never set (e.g. if an
 *  older session existed before the fix was applied).
 *
 *  Changes made:
 *   1. Guard now also validates that "role" is non-null before the
 *      equals() check — prevents a NullPointerException if the
 *      session is stale.
 *   2. Non-admin authenticated users are redirected to /home instead
 *      of receiving a 403 — better UX for accidental URL access.
 *   3. Unauthenticated users are redirected to /login (unchanged).
 * ══════════════════════════════════════════════════════════════════
 */
@WebServlet("/admin-analytics")
public class AdminAnalyticsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // ── FIX 1: strict admin-only guard ────────────────────────────────
        HttpSession session = req.getSession(false);

        // Step 1: must be logged in
        if (session == null || session.getAttribute("user") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Step 2: role attribute must exist and be exactly "admin"
        String role = (String) session.getAttribute("role");
        if (role == null || !role.equals("admin")) {
            // Logged-in but not admin — redirect to subscriber home
            // (do NOT expose a 403 message that reveals this URL exists)
            res.sendRedirect(req.getContextPath() + "/home");
            return;
        }
        // ── End of FIX 1 guard ────────────────────────────────────────────

        // ── MySQL analytics ────────────────────────────────────────────────
        List<String[]> top10      = AnalyticsService.getTop10Content();
        List<String[]> revenue    = AnalyticsService.getRevenueByPlan();
        List<String[]> ratings    = AnalyticsService.getRatedContent();
        List<String[]> churnRisk  = AnalyticsService.getChurnRiskUsers();

        // ── MongoDB analytics (Phase 3) ────────────────────────────────────
        List<String[]> mongoTop10   = AnalyticsService.getMongoTop10();
        List<String[]> genreCountry = AnalyticsService.getMongoGenreByCountry();

        req.setAttribute("top10",        top10);
        req.setAttribute("revenue",      revenue);
        req.setAttribute("ratings",      ratings);
        req.setAttribute("churnRisk",    churnRisk);
        req.setAttribute("mongoTop10",   mongoTop10);
        req.setAttribute("genreCountry", genreCountry);
        req.getRequestDispatcher("/admin-analytics.jsp").forward(req, res);
    }
}