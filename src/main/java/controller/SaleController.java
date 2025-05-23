package controller;

import model.Sale;
import model.SaleItem;
import model.DatabaseConnection;
import model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleController {
    public boolean createSale(Sale sale, List<SaleItem> items) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Insertar la venta
            String saleSql = "INSERT INTO sales(sale_date, client_name, total_amount, user_id) VALUES(?, ?, ?, ?)";
            PreparedStatement saleStmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
            
            saleStmt.setString(1, new java.sql.Timestamp(sale.getSaleDate().getTime()).toString());
            saleStmt.setString(2, sale.getClientName());
            saleStmt.setDouble(3, sale.getTotalAmount());
            saleStmt.setInt(4, sale.getUserId());
            
            int affectedRows = saleStmt.executeUpdate();
            if (affectedRows == 0) {
                conn.rollback();
                return false;
            }
            
            // Obtener el ID de la venta insertada
            ResultSet generatedKeys = saleStmt.getGeneratedKeys();
            int saleId;
            if (generatedKeys.next()) {
                saleId = generatedKeys.getInt(1);
            } else {
                conn.rollback();
                return false;
            }
            
            // Insertar los items de la venta
            String itemSql = "INSERT INTO sale_items(sale_id, product_id, quantity, unit_price) VALUES(?, ?, ?, ?)";
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            
            for (SaleItem item : items) {
                itemStmt.setInt(1, saleId);
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setDouble(3, item.getQuantity());
                itemStmt.setDouble(4, item.getUnitPrice());
                itemStmt.addBatch();
                
                // Actualizar el inventario
                if (!new ProductController().updateProductQuantity(item.getProductId(), -item.getQuantity())) {
                    conn.rollback();
                    return false;
                }
            }
            
            int[] itemResults = itemStmt.executeBatch();
            for (int result : itemResults) {
                if (result == Statement.EXECUTE_FAILED) {
                    conn.rollback();
                    return false;
                }
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Sale> getAllSales() {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.*, u.full_name as user_name FROM sales s JOIN users u ON s.user_id = u.id ORDER BY s.sale_date DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                List<SaleItem> items = getSaleItems(rs.getInt("id"));
                
                sales.add(new Sale(
                    rs.getInt("id"),
                    rs.getTimestamp("sale_date"),
                    rs.getString("client_name"),
                    rs.getDouble("total_amount"),
                    rs.getInt("user_id"),
                    items
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    private List<SaleItem> getSaleItems(int saleId) {
        List<SaleItem> items = new ArrayList<>();
        String sql = "SELECT si.*, p.name as product_name FROM sale_items si " +
                      "JOIN products p ON si.product_id = p.id WHERE si.sale_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, saleId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                SaleItem item = new SaleItem(
                    rs.getInt("id"),
                    rs.getInt("sale_id"),
                    rs.getInt("product_id"),
                    rs.getDouble("quantity"),
                    rs.getDouble("unit_price")
                );
                
                Product product = new Product(
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    "", 0, "", 0, "", ""
                );
                item.setProduct(product);
                
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public double getTotalSales() {
        String sql = "SELECT SUM(total_amount) as total FROM sales";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}