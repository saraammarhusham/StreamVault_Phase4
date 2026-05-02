package com.streamvault.servlets;

import com.streamvault.models.ContentItem;
import com.streamvault.services.ContentService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final int PAGE_SIZE = 12;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // ── Auth guard ─────────────────────────────────────────────────────
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // ── Read filter params ─────────────────────────────────────────────
        String search   = req.getParameter("search");
        String genre    = req.getParameter("genre");
        String type     = req.getParameter("type");
        String language = req.getParameter("language");
        String sortBy   = req.getParameter("sort");   // "rating" | "title"
        String pageStr  = req.getParameter("page");
        int    page     = (pageStr != null && pageStr.matches("\\d+"))
                          ? Integer.parseInt(pageStr) : 1;

        // ── Query ──────────────────────────────────────────────────────────
        List<ContentItem> items = ContentService.browseContent(
            search, genre, type, language, sortBy, page, PAGE_SIZE);

        List<String> genres    = ContentService.getAllGenres();
        List<String> languages = ContentService.getAllLanguages();

        // ── Forward to JSP ─────────────────────────────────────────────────
        req.setAttribute("items",     items);
        req.setAttribute("genres",    genres);
        req.setAttribute("languages", languages);
        req.setAttribute("search",    search);
        req.setAttribute("genre",     genre);
        req.setAttribute("type",      type);
        req.setAttribute("language",  language);
        req.setAttribute("sort",      sortBy);
        req.setAttribute("page",      page);
        req.setAttribute("pageSize",  PAGE_SIZE);
        req.getRequestDispatcher("/home.jsp").forward(req, res);
    }
}
