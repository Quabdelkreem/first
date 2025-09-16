package com.example.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@WebServlet(urlPatterns = {"/items", "/items/details"})
public class ItemController extends HttpServlet {
    private static class Item {
        String id;
        String name;
        Map<String, String> details = new HashMap<>();
    }

    private static final Map<String, Item> ITEMS = new ConcurrentHashMap<>();
    private static final AtomicLong SEQ = new AtomicLong(1);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // List items as JSON: [{id,name,hasDetails}]
        resp.setContentType("application/json; charset=UTF-8");
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Item it : ITEMS.values()) {
            if (!first) sb.append(',');
            first = false;
            sb.append("{")
              .append("\"id\":\"").append(it.id).append("\"")
              .append(",\"name\":\"").append(escapeJson(it.name)).append("\"")
              .append(",\"hasDetails\":").append(it.details != null && !it.details.isEmpty())
              .append("}");
        }
        sb.append("]");
        try (PrintWriter out = resp.getWriter()) {
            out.print(sb.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/items".equals(path)) {
            // Create item (JSON body: {name})
            String body = req.getReader().lines().reduce("", (a,b)->a+b);
            String name = extractJson(body, "name");
            if (name == null || name.isBlank()) {
                resp.sendError(400, "Name required");
                return;
            }
            Item it = new Item();
            it.id = String.valueOf(SEQ.getAndIncrement());
            it.name = name.trim();
            ITEMS.put(it.id, it);
            resp.setStatus(201);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().print("{\"id\":\"" + it.id + "\"}");
            return;
        }
        if ("/items/details".equals(path)) {
            // Add/Update details (JSON: itemId,key,value)
            String body = req.getReader().lines().reduce("", (a,b)->a+b);
            String itemId = extractJson(body, "itemId");
            String key = extractJson(body, "key");
            String value = extractJson(body, "value");
            Item it = ITEMS.get(itemId);
            if (it == null) { resp.sendError(404, "Item not found"); return; }
            if (key == null || key.isBlank()) { resp.sendError(400, "Key required"); return; }
            if (value == null) value = "";
            it.details.put(key.trim(), value);
            resp.setContentType("application/json; charset=UTF-8");
            resp.getWriter().print("{\"ok\":true}");
            return;
        }
        resp.sendError(404);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Delete item (and its details)
        String id = req.getParameter("id");
        if (id == null) { resp.sendError(400, "id required"); return; }
        Item it = ITEMS.remove(id);
        if (it == null) { resp.sendError(404, "Item not found"); return; }
        // details removed with the item since in-memory
        resp.setStatus(204);
    }

    private String extractJson(String body, String key) {
        // very small and naive JSON extraction (sufficient for demo)
        String pattern = "\"" + key + "\"\s*:\s*\"";
        int i = body.indexOf('"' + key + '"');
        if (i < 0) return null;
        int colon = body.indexOf(':', i);
        int startQuote = body.indexOf('"', colon+1);
        int endQuote = body.indexOf('"', startQuote+1);
        if (startQuote < 0 || endQuote < 0) return null;
        return body.substring(startQuote+1, endQuote);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace(""", "\\"");
    }
}
