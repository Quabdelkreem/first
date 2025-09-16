package com.example.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet(urlPatterns = {"/account/signup", "/account/login", "/account/logout", "/account/forgot", "/account/delete"})
public class AccountServlet extends HttpServlet {
    static class User {
        String username;
        String email;
        String passwordHash;
    }
    private static final Map<String, User> USERS = new ConcurrentHashMap<>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();
        switch (path) {
            case "/account/signup": handleSignup(req, resp); break;
            case "/account/login": handleLogin(req, resp); break;
            case "/account/logout": handleLogout(req, resp); break;
            case "/account/forgot": handleForgot(req, resp); break;
            case "/account/delete": handleDelete(req, resp); break;
            default: resp.sendError(404);
        }
    }

    private void handleSignup(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String u = req.getParameter("username");
        String e = req.getParameter("email");
        String p = req.getParameter("password");
        if (u == null || e == null || p == null || u.isBlank() || e.isBlank() || p.isBlank()) {
            redirectWithMessage(resp, "signup.html", "All fields are required");
            return;
        }
        if (USERS.containsKey(u)) {
            redirectWithMessage(resp, "signup.html", "Username already exists");
            return;
        }
        User user = new User();
        user.username = u.trim();
        user.email = e.trim();
        user.passwordHash = hash(p);
        USERS.put(user.username, user);

        // create session & cookie
        HttpSession session = req.getSession(true);
        session.setAttribute("uid", user.username);
        Cookie sid = new Cookie("sid", session.getId());
        sid.setHttpOnly(true); sid.setPath("/");
        resp.addCookie(sid);

        resp.sendRedirect("../items.html");
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String u = req.getParameter("username");
        String p = req.getParameter("password");
        String remember = req.getParameter("remember");
        User user = (u != null) ? USERS.get(u) : null;
        if (user == null || !hash(p).equals(user.passwordHash)) {
            redirectWithMessage(resp, "login.html", "Invalid credentials");
            return;
        }
        HttpSession session = req.getSession(true);
        session.setAttribute("uid", user.username);

        // session cookie
        Cookie sid = new Cookie("sid", session.getId());
        sid.setHttpOnly(true); sid.setPath("/");
        resp.addCookie(sid);

        // remember-me (very simplified demo)
        if ("1".equals(remember)) {
            Cookie rm = new Cookie("remember", user.username);
            rm.setMaxAge(60*60*24*30);
            rm.setPath("/");
            resp.addCookie(rm);
        }
        resp.sendRedirect("../items.html");
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        Cookie sid = new Cookie("sid", ""); sid.setPath("/"); sid.setMaxAge(0); resp.addCookie(sid);
        Cookie rm = new Cookie("remember", ""); rm.setPath("/"); rm.setMaxAge(0); resp.addCookie(rm);
        resp.sendRedirect("../login.html");
    }

    private void handleForgot(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // demo only: pretend an email is sent
        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Forgot</title></head><body>");
            out.println("<p>If the email exists, a reset link was sent (demo).</p>");
            out.println("<p><a href='../login.html'>Back to login</a></p>");
            out.println("</body></html>");
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null) { resp.sendRedirect("../login.html"); return; }
        String uid = (String) session.getAttribute("uid");
        if (uid != null) USERS.remove(uid);
        session.invalidate();
        Cookie sid = new Cookie("sid", ""); sid.setPath("/"); sid.setMaxAge(0); resp.addCookie(sid);
        Cookie rm = new Cookie("remember", ""); rm.setPath("/"); rm.setMaxAge(0); resp.addCookie(rm);
        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Account Deleted</title></head><body>");
            out.println("<p>Account deleted (demo in-memory).</p>");
            out.println("<p><a href='../signup.html'>Sign up</a></p>");
            out.println("</body></html>");
        }
    }

    private String hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception ex) {
            return s; // fallback (demo)
        }
    }

    private void redirectWithMessage(HttpServletResponse resp, String page, String msg) throws IOException {
        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Redirect</title>");
            out.println("<meta http-equiv='refresh' content='2;url=" + page + "'>");
            out.println("</head><body>");
            out.println("<p>" + escape(msg) + "</p>");
            out.println("<p>Redirecting...</p>");
            out.println("</body></html>");
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace(""", "&quot;").replace("'", "&#39;");
    }
}
