package com.example.service;

import com.example.model.Item;
import com.example.model.ItemDetails;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

public class ItemServiceImpl implements ItemService {
    private DataSource dataSource;
    
    public ItemServiceImpl() {
        // Initialize dataSource (e.g., from connection pool)
    }
    
    @Override
    public List<Item> getAllItemsWithDetails() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT i.*, id.description, id.issue_date, id.expiry_date " +
                     "FROM item i LEFT JOIN item_details id ON i.id = id.item_id";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Item item = new Item();
                item.setId(rs.getInt("id"));
                item.setName(rs.getString("name"));
                item.setPrice(rs.getDouble("price"));
                item.setQuantity(rs.getInt("quantity"));
                
                // Check if item details exist
                if (rs.getString("description") != null) {
                    ItemDetails details = new ItemDetails();
                    details.setItemId(rs.getInt("id"));
                    details.setDescription(rs.getString("description"));
                    details.setIssueDate(rs.getDate("issue_date"));
                    details.setExpiryDate(rs.getDate("expiry_date"));
                    item.setItemDetails(details);
                }
                
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    @Override
    public Item getItemById(int id) {
        Item item = null;
        String sql = "SELECT * FROM item WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    item = new Item();
                    item.setId(rs.getInt("id"));
                    item.setName(rs.getString("name"));
                    item.setPrice(rs.getDouble("price"));
                    item.setQuantity(rs.getInt("quantity"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return item;
    }
    
    @Override
    public boolean insertItem(Item item) {
        String sql = "INSERT INTO item (id, name, price, quantity) VALUES (item_seq.NEXTVAL, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, item.getName());
            ps.setDouble(2, item.getPrice());
            ps.setInt(3, item.getQuantity());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateItem(Item item) {
        String sql = "UPDATE item SET name = ?, price = ?, quantity = ? WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, item.getName());
            ps.setDouble(2, item.getPrice());
            ps.setInt(3, item.getQuantity());
            ps.setInt(4, item.getId());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteItem(int id) {
        String sql = "DELETE FROM item WHERE id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public ItemDetails getItemDetailsByItemId(int itemId) {
        ItemDetails details = null;
        String sql = "SELECT * FROM item_details WHERE item_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    details = new ItemDetails();
                    details.setId(rs.getInt("id"));
                    details.setItemId(rs.getInt("item_id"));
                    details.setDescription(rs.getString("description"));
                    details.setIssueDate(rs.getDate("issue_date"));
                    details.setExpiryDate(rs.getDate("expiry_date"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return details;
    }
    
    @Override
    public boolean insertItemDetails(ItemDetails itemDetails) {
        String sql = "INSERT INTO item_details (id, item_id, description, issue_date, expiry_date) " +
                     "VALUES (item_details_seq.NEXTVAL, ?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, itemDetails.getItemId());
            ps.setString(2, itemDetails.getDescription());
            ps.setDate(3, new java.sql.Date(itemDetails.getIssueDate().getTime()));
            ps.setDate(4, new java.sql.Date(itemDetails.getExpiryDate().getTime()));
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateItemDetails(ItemDetails itemDetails) {
        String sql = "UPDATE item_details SET description = ?, issue_date = ?, expiry_date