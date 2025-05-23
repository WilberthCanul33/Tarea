package model;

public class SaleItem {
    private int id;
    private int saleId;
    private int productId;
    private double quantity;
    private double unitPrice;
    private Product product;

    public SaleItem(int id, int saleId, int productId, double quantity, double unitPrice) {
        this.id = id;
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters y Setters
    public int getId() { return id; }
    public int getSaleId() { return saleId; }
    public int getProductId() { return productId; }
    public double getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public Product getProduct() { return product; }
    
    public void setId(int id) { this.id = id; }
    public void setSaleId(int saleId) { this.saleId = saleId; }
    public void setProductId(int productId) { this.productId = productId; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setProduct(Product product) { this.product = product; }
}