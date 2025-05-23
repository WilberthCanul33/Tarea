package model;

public class Product {
    private int id;
    private String name;
    private String category;
    private double quantity;
    private String unit;
    private double price;
    private String supplier;
    private String expirationDate;

    public Product(int id, String name, String category, double quantity, String unit, 
                  double price, String supplier, String expirationDate) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.price = price;
        this.supplier = supplier;
        this.expirationDate = expirationDate;
    }

    // Getters y Setters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public double getPrice() { return price; }
    public String getSupplier() { return supplier; }
    public String getExpirationDate() { return expirationDate; }
    
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setPrice(double price) { this.price = price; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public void setExpirationDate(String expirationDate) { this.expirationDate = expirationDate; }
}