package com.example.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;

@WebServlet("/allorders")
public class AllOrdersServlet extends HttpServlet {
    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        HttpSession session = req.getSession(true);
        List<String> orders = (List<String>) session.getAttribute("orders");
        if (orders == null) {
            orders = new ArrayList<>();
            session.setAttribute("orders", orders);
        }
        String clear = req.getParameter("clear");
        if ("1".equals(clear)) {
            orders.clear();
        }
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>All Orders</title></head><body>");
            out.println("<h1>All Session Orders</h1>");
            if (orders.isEmpty()) {
                out.println("<p>No orders yet.</p>");
            } else {
                out.println("<ul>");
                for (String it : orders) {
                    out.println("<li>" + escape(it) + "</li>");
                }
                out.println("</ul>");
            }
            out.println("<hr><p><a href='order.html'>Add more</a></p>");
            out.println("<form method='get' action='allorders'><input type='hidden' name='clear' value='1'/><button type='submit'>Clear Orders</button></form>");
            out.println("</body></html>");
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace(""", "&quot;").replace("'", "&#39;");
    }
}
