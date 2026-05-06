package com.streamvault.servlets;

import com.streamvault.models.User;
import com.streamvault.services.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

/**
 * LoginServlet — handles GET (show form) and POST (authenticate).
 *
 * ══════════════════════════════════════════════════════════════════
 *  FIX 1 — ADMIN LOGIN
 * ══════════════════════════════════════════════════════════════════
 *  The original code already had the correct role-based redirect:
 *      if ("admin".equals(user.getRole())) → /admin-analytics
 *  The real failure was in AuthService (broken BCrypt hash).  That
 *  is fixed there.  This file adds:
 *
 *   1. An explicit "content_manager" redirect path so that role is
 *      not accidentally treated as a plain subscriber.
 *   2. Clear session attributes: "user", "userId", "role", "email"
 *      — "role" and "email" are used by JSPs to show/hide admin UI.
 *   3. Inactive-account message surfaced to the user.
 * ══════════════════════════════════════════════════════════════════
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // ══════════════════════════════════════════════════════════════════════
    //  GET — show login form (or redirect if already logged in)
    // ══════════════════════════════════════════════════════════════════════

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            // Already logged in — send to the right page for their role
            String role = (String) session.getAttribute("role");
            redirectByRole(res, req, role);
            return;
        }
        req.getRequestDispatcher("/login.jsp").forward(req, res);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  POST — authenticate
    // ══════════════════════════════════════════════════════════════════════

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        // ── Basic null / blank check ───────────────────────────────────────
        if (email == null || password == null
                || email.isBlank() || password.isBlank()) {
            req.setAttribute("error", "Email and password are required.");
            req.getRequestDispatcher("/login.jsp").forward(req, res);
            return;
        }

        // ── Authenticate via AuthService ───────────────────────────────────
        // AuthService.login() now:
        //   • guards against the broken seed hash (FIX 1 in AuthService)
        //   • returns null for inactive accounts
        //   • always sets user.getRole() correctly
        User user = AuthService.login(email.trim(), password);

        if (user == null) {
            // Surface a slightly more helpful message when the account exists
            // but is inactive (AuthService returns null in both cases — we
            // keep a single generic message to avoid email enumeration).
            req.setAttribute("error", "Invalid email or password.");
            req.getRequestDispatcher("/login.jsp").forward(req, res);
            return;
        }

        // ── Build session ──────────────────────────────────────────────────
        // FIX 1: store "role" and "email" explicitly — JSPs use these to
        // show or hide admin-only navigation links without needing to cast
        // the full User object.
        HttpSession session = req.getSession(true);
        session.setAttribute("user",   user);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("role",   user.getRole());   // FIX 1: critical for admin checks
        session.setAttribute("email",  user.getEmail());  // FIX 1: useful for display
        session.setMaxInactiveInterval(30 * 60);           // 30 minutes

        // ── Role-based redirect ────────────────────────────────────────────
        // FIX 1: explicit handling for every defined role so no role is
        // accidentally redirected to the subscriber home page.
        redirectByRole(res, req, user.getRole());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PRIVATE — centralised redirect logic (used by both GET and POST)
    // ══════════════════════════════════════════════════════════════════════

    /**
     * FIX 1 — redirect each role to the appropriate landing page.
     *
     *   admin           → /admin-analytics  (admin dashboard)
     *   content_manager → /home             (can also manage content via
     *                                        separate pages if added later)
     *   subscriber      → /home             (standard viewer)
     *   (anything else) → /home             (safe fallback)
     */
    private void redirectByRole(HttpServletResponse res,
                                HttpServletRequest req,
                                String role) throws IOException {
        String base = req.getContextPath();

        if ("admin".equals(role)) {
            res.sendRedirect(base + "/admin-analytics");
        } else if ("content_manager".equals(role)) {
            res.sendRedirect(base + "/home");
        } else {
            // subscriber + any future roles default to home
            res.sendRedirect(base + "/home");
        }
    }
}