package com.example.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/order")
public class OrderServlet extends HttpServlet {
    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession(true);

        List<String> orders = (List<String>) session.getAttribute("orders");
        if (orders == null) {
            orders = new ArrayList<>();
            session.setAttribute("orders", orders);
        }

        String food = req.getParameter("food");
        if (food != null && !food.trim().isEmpty()) {
            orders.add(food.trim());
        }
        resp.setContentType("text/html; charset=UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Order Added</title></head><body>");
            out.println("<h1>Order Added</h1>");
            out.println("<p>Added: <strong>" + escape(food) + "</strong></p>");
            out.println("<p><a href='order.html'>Back</a> | <a href='allorders'>All Orders</a></p>");
            out.println("</body></html>");
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace(""", "&quot;").replace("'", "&#39;");
    }
}
