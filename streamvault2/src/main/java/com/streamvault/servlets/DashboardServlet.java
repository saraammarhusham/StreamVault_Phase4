package com.streamvault.servlets;

import com.streamvault.models.User;
import com.streamvault.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        int  uid  = user.getUserId();

        String[]       subscription  = UserService.getActiveSubscription(uid);
        List<String[]> payments      = UserService.getPaymentHistory(uid);
        List<String[]> watchHistory  = UserService.getRecentWatchHistory(uid);

        req.setAttribute("subscription", subscription);
        req.setAttribute("payments",     payments);
        req.setAttribute("watchHistory", watchHistory);
        req.getRequestDispatcher("/dashboard.jsp").forward(req, res);
    }
}
