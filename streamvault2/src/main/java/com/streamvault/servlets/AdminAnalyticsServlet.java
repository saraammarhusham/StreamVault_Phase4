package com.streamvault.servlets;

import com.streamvault.services.AnalyticsService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin-analytics")
public class AdminAnalyticsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // ── Strict admin-only guard ────────────────────────────────────────
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String role = (String) session.getAttribute("role");
        if (!"admin".equals(role)) {
            res.sendError(HttpServletResponse.SC_FORBIDDEN,
                          "Access denied: admin only.");
            return;
        }

        // ── MySQL analytics ────────────────────────────────────────────────
        List<String[]> top10       = AnalyticsService.getTop10Content();
        List<String[]> revenue     = AnalyticsService.getRevenueByPlan();
        List<String[]> ratings     = AnalyticsService.getRatedContent();
        List<String[]> churnRisk   = AnalyticsService.getChurnRiskUsers();

        // ── MongoDB analytics (Phase 3) ────────────────────────────────────
        List<String[]> mongoTop10  = AnalyticsService.getMongoTop10();
        List<String[]> genreCountry= AnalyticsService.getMongoGenreByCountry();

        req.setAttribute("top10",        top10);
        req.setAttribute("revenue",      revenue);
        req.setAttribute("ratings",      ratings);
        req.setAttribute("churnRisk",    churnRisk);
        req.setAttribute("mongoTop10",   mongoTop10);
        req.setAttribute("genreCountry", genreCountry);
        req.getRequestDispatcher("/admin-analytics.jsp").forward(req, res);
    }
}
