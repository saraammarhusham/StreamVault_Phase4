package com.streamvault.servlets;

import com.streamvault.services.AuthService;
import com.streamvault.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        List<String[]> plans = UserService.getAllPlans();
        req.setAttribute("plans", plans);
        req.getRequestDispatcher("/register.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String fullName = req.getParameter("fullName");
        String email    = req.getParameter("email");
        String password = req.getParameter("password");
        String country  = req.getParameter("country");
        String dob      = req.getParameter("dob");       // YYYY-MM-DD
        String planIdStr= req.getParameter("planId");

        // ── Validation ─────────────────────────────────────────────────────
        StringBuilder errors = new StringBuilder();
        if (fullName == null || fullName.isBlank())  errors.append("Full name is required. ");
        if (email    == null || email.isBlank())      errors.append("Email is required. ");
        if (password == null || password.length() < 8) errors.append("Password must be at least 8 characters. ");
        if (country  == null || country.isBlank())   errors.append("Country is required. ");
        if (dob      == null || dob.isBlank())        errors.append("Date of birth is required. ");
        if (planIdStr== null || planIdStr.isBlank())  errors.append("Please select a plan. ");

        if (errors.length() > 0) {
            req.setAttribute("error", errors.toString());
            req.setAttribute("plans", UserService.getAllPlans());
            req.getRequestDispatcher("/register.jsp").forward(req, res);
            return;
        }

        int planId = Integer.parseInt(planIdStr);
        int result = AuthService.register(fullName, email, password, country, dob, planId);

        switch (result) {
            case -2:
                req.setAttribute("error", "Password must be at least 8 characters.");
                break;
            case -3:
                req.setAttribute("error", "An account with this email already exists.");
                break;
            case -1:
                req.setAttribute("error", "Registration failed. Please try again.");
                break;
            default:
                // success
                req.setAttribute("success",
                    "Account created! You can now log in.");
                req.setAttribute("plans", UserService.getAllPlans());
                req.getRequestDispatcher("/login.jsp").forward(req, res);
                return;
        }

        req.setAttribute("plans", UserService.getAllPlans());
        req.getRequestDispatcher("/register.jsp").forward(req, res);
    }
}
