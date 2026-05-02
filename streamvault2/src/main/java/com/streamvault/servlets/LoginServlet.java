package com.streamvault.servlets;

import com.streamvault.models.User;
import com.streamvault.services.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        // If already logged in, redirect to home
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            res.sendRedirect(req.getContextPath() + "/home");
            return;
        }
        req.getRequestDispatcher("/login.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String email    = req.getParameter("email");
        String password = req.getParameter("password");

        // Basic null check
        if (email == null || password == null || email.isBlank() || password.isBlank()) {
            req.setAttribute("error", "Email and password are required.");
            req.getRequestDispatcher("/login.jsp").forward(req, res);
            return;
        }

        User user = AuthService.login(email, password);

        if (user == null) {
            req.setAttribute("error", "Invalid email or password.");
            req.getRequestDispatcher("/login.jsp").forward(req, res);
            return;
        }

        // Create session
        HttpSession session = req.getSession(true);
        session.setAttribute("user",   user);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("role",   user.getRole());
        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        // Role-based redirect
        if ("admin".equals(user.getRole())) {
            res.sendRedirect(req.getContextPath() + "/admin-analytics");
        } else {
            res.sendRedirect(req.getContextPath() + "/home");
        }
    }
}
