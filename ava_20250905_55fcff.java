package com.example.controller;

import com.example.model.Item;
import com.example.model.ItemDetails;
import com.example.service.ItemService;
import com.example.service.ItemServiceImpl;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet("/item")
public class ItemController extends HttpServlet {
    private ItemService itemService;
    
    @Override
    public void init() {
        itemService = new ItemServiceImpl();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }
        
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "list";
        }
        
        try {
            switch (action) {
                case "new":
                    showNewForm(request, response);
                    break;
                case "insert":
                    insertItem(request, response);
                    break;
                case "delete":
                    deleteItem(request, response);
                    break;
                case "edit":
                    showEditForm(request, response);
                    break;
                case "update":
                    updateItem(request, response);
                    break;
                case "details":
                    showItemDetails(request, response);
                    break;
                case "newDetails":
                    showNewDetailsForm(request, response);
                    break;
                case "insertDetails":
                    insertItemDetails(request, response);
                    break;
                case "deleteDetails":
                    deleteItemDetails(request, response);
                    break;
                default:
                    listItems(request, response);
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    private void listItems(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Item> items = itemService.getAllItemsWithDetails();
        request.setAttribute("items", items);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("load-items.jsp");
        dispatcher.forward(request, response);
    }
    
    private void showNewForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("item-form.jsp");
        dispatcher.forward(request, response);
    }
    
    private void showEditForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        Item existingItem = itemService.getItemById(id);
        ItemDetails itemDetails = itemService.getItemDetailsByItemId(id);
        
        request.setAttribute("item", existingItem);
        request.setAttribute("itemDetails", itemDetails);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("item-form.jsp");
        dispatcher.forward(request, response);
    }
    
    private void insertItem(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        
        String name = request.getParameter("name");
        double price = Double.parseDouble(request.getParameter("price"));
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        
        Item newItem = new Item(name, price, quantity);
        boolean inserted = itemService.insertItem(newItem);
        
        if (inserted) {
            response.sendRedirect("item?action=list");
        } else {
            request.setAttribute("error", "Failed to insert item");
            RequestDispatcher dispatcher = request.getRequestDispatcher("item-form.jsp");
            dispatcher.forward(request, response);
        }
    }
    
    private void updateItem(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        String name = request.getParameter("name");
        double price = Double.parseDouble(request.getParameter("price"));
        int quantity = Integer.parseInt(request.getParameter("quantity"));
        String description = request.getParameter("description");
        Date issueDate = Date.valueOf(request.getParameter("issue_date"));
        Date expiryDate = Date.valueOf(request.getParameter("expiry_date"));
        
        Item item = new Item(id, name, price, quantity);
        ItemDetails itemDetails = new ItemDetails();
        itemDetails.setItemId(id);
        itemDetails.setDescription(description);
        itemDetails.setIssueDate(issueDate);
        itemDetails.setExpiryDate(expiryDate);
        
        boolean updated = itemService.updateItemWithDetails(item, itemDetails);
        
        if (updated) {
            response.sendRedirect("item?action=list");
        } else {
            request.setAttribute("error", "Failed to update item");
            RequestDispatcher dispatcher = request.getRequestDispatcher("item-form.jsp");
            dispatcher.forward(request, response);
        }
    }
    
    private void deleteItem(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        int id = Integer.parseInt(request.getParameter("id"));
        itemService.deleteItem(id);
        response.sendRedirect("item?action=list");
    }
    
    private void showItemDetails(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int itemId = Integer.parseInt(request.getParameter("itemId"));
        ItemDetails itemDetails = itemService.getItemDetailsByItemId(itemId);
        
        request.setAttribute("itemDetails", itemDetails);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("item-details.jsp");
        dispatcher.forward(request, response);
    }
    
    private void showNewDetailsForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        int itemId = Integer.parseInt(request.getParameter("itemId"));
        request.setAttribute("itemId", itemId);
        
        RequestDispatcher dispatcher = request.getRequestDispatcher("item-details-form.jsp");
        dispatcher.forward(request, response);
    }
    
    private void insertItemDetails(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, ServletException {
        
        int itemId = Integer.parseInt(request.getParameter("itemId"));
        String description = request.getParameter("description");
        Date issueDate = Date.valueOf(request.getParameter("issue_date"));
        Date expiryDate = Date.valueOf(request.getParameter("expiry_date"));
        
        ItemDetails itemDetails = new ItemDetails();
        itemDetails.setItemId(itemId);
        itemDetails.setDescription(description);
        itemDetails.setIssueDate(issueDate);
        itemDetails.setExpiryDate(expiryDate);
        
        boolean inserted = itemService.insertItemDetails(itemDetails);
        
        if (inserted) {
            response.sendRedirect("item?action=list");
        } else {
            request.setAttribute("error", "Failed to insert item details");
            RequestDispatcher dispatcher = request.getRequestDispatcher("item-details-form.jsp");
            dispatcher.forward(request, response);
        }
    }
    
    private void deleteItemDetails(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        int itemId = Integer.parseInt(request.getParameter("itemId"));
        itemService.deleteItemDetails(itemId);
        response.sendRedirect("item?action=list");
    }
}