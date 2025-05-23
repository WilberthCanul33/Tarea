package model;

import java.util.Date;
import java.util.List;

public class Sale {
    private int id;
    private Date saleDate;
    private String clientName;
    private double totalAmount;
    private int userId;
    private List<SaleItem> items;

    public Sale(int id, Date saleDate, String clientName, double totalAmount, int userId, List<SaleItem> items) {
        this.id = id;
        this.saleDate = saleDate;
        this.clientName = clientName;
        this.totalAmount = totalAmount;
        this.userId = userId;
        this.items = items;
    }

    // Getters y Setters
    public int getId() { return id; }
    public Date getSaleDate() { return saleDate; }
    public String getClientName() { return clientName; }
    public double getTotalAmount() { return totalAmount; }
    public int getUserId() { return userId; }
    public List<SaleItem> getItems() { return items; }
    
    public void setId(int id) { this.id = id; }
    public void setSaleDate(Date saleDate) { this.saleDate = saleDate; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setItems(List<SaleItem> items) { this.items = items; }
}