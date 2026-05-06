package com.streamvault.servlets;

import com.streamvault.models.ContentItem;
import com.streamvault.services.ContentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/manage-content")
public class ManageContentServlet extends HttpServlet {

    private boolean allowed(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;

        String role = (String) session.getAttribute("role");
        return "content_manager".equals(role) || "admin".equals(role);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (!allowed(req)) {
            res.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        List<ContentItem> contentList = ContentService.getAllContentForManagement();
        req.setAttribute("contentList", contentList);

        req.getRequestDispatcher("/WEB-INF/manage-content.jsp").forward(req, res);
    }
}