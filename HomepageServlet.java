package com.example.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/homepage")
public class HomepageServlet extends HttpServlet {
    private static final String COOKIE_NAME = "favPlace";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    private void process(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        String favPlace = req.getParameter("favPlace");
        if (favPlace != null && !favPlace.trim().isEmpty()) {
            String v = URLEncoder.encode(favPlace.trim(), StandardCharsets.UTF_8);
            Cookie c = new Cookie(COOKIE_NAME, v);
            c.setMaxAge(60*60*24*30); // 30 days
            c.setPath("/");
            c.setHttpOnly(true);
            resp.addCookie(c);
        } else {
            Cookie[] cookies = req.getCookies();
            if (cookies != null) {
                for (Cookie ck : cookies) {
                    if (COOKIE_NAME.equals(ck.getName())) {
                        favPlace = URLDecoder.decode(ck.getValue(), StandardCharsets.UTF_8);
                        break;
                    }
                }
            }
        }

        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Homepage</title></head><body>");
            out.println("<h1>Homepage</h1>");
            if (favPlace == null || favPlace.isBlank()) {
                out.println("<p>No favorite place saved.</p>");
            } else {
                out.println("<p>Your favorite place: <strong>" + escape(favPlace) + "</strong></p>");
            }
            out.println("<p><a href='start.html'>Edit favorite place</a></p>");
            out.println("</body></html>");
        }
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace(""", "&quot;").replace("'", "&#39;");
    }
}
