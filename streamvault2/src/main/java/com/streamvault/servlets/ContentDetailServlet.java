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

@WebServlet("/content")
public class ContentDetailServlet extends HttpServlet {

    /** Show content details + episodes + reviews */
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

        req.setAttribute("item",     item);
        req.setAttribute("episodes", episodes);
        req.setAttribute("reviews",  reviews);
        req.getRequestDispatcher("/content-detail.jsp").forward(req, res);
    }

    /** Handle "Play Now" button — records watch history */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        User user = (User) session.getAttribute("user");

        String contentIdStr  = req.getParameter("contentId");
        String episodeIdStr  = req.getParameter("episodeId");
        String progressStr   = req.getParameter("progress");
        String deviceType    = req.getParameter("device");

        if (contentIdStr == null || !contentIdStr.matches("\\d+")) {
            res.sendRedirect(req.getContextPath() + "/home");
            return;
        }

        int contentId  = Integer.parseInt(contentIdStr);
        int episodeId  = (episodeIdStr != null && episodeIdStr.matches("\\d+"))
                         ? Integer.parseInt(episodeIdStr) : 0;
        int progress   = (progressStr  != null && progressStr.matches("\\d+"))
                         ? Integer.parseInt(progressStr)  : 0;

        ContentService.recordWatch(user.getUserId(), contentId, episodeId,
                                   progress, deviceType != null ? deviceType : "Web");

        res.sendRedirect(req.getContextPath() + "/content?id=" + contentId + "&playing=true");
    }
}
