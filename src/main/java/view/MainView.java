package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class MainView extends JFrame {
    private JTabbedPane tabbedPane;
    private JPanel inventoryPanel;
    private JPanel salesPanel;
    private JPanel reportsPanel;
    private JButton logoutButton;
    
    // Inventory components
    private JComboBox<String> categoryComboBox;
    private JTable inventoryTable;
    private JButton addProductButton;
    private JButton editProductButton;
    private JButton deleteProductButton;
    private JLabel inventorySummaryLabel;
    
    // Sales components
    private JTable productsTable;
    private JTable cartTable;
    private JButton addToCartButton;
    private JButton removeFromCartButton;
    private JButton completeSaleButton;
    private JTextField clientNameField;
    private JLabel totalLabel;
    
    // Reports components
    private JLabel totalInventoryValueLabel;
    private JLabel totalSalesLabel;
    private JTable salesHistoryTable;

    public MainView() {
        setTitle("AgricolaPOS - Sistema de Punto de Venta");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create tabs
        tabbedPane = new JTabbedPane();
        
        // Inventory Tab
        inventoryPanel = createInventoryPanel();
        tabbedPane.addTab("Inventario", inventoryPanel);
        
        // Sales Tab
        salesPanel = createSalesPanel();
        tabbedPane.addTab("Ventas", salesPanel);
        
        // Reports Tab
        reportsPanel = createReportsPanel();
        tabbedPane.addTab("Reportes", reportsPanel);
        
        // Logout button
        logoutButton = new JButton("Cerrar Sesión");
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.add(logoutButton);
        
        // Main layout
        setLayout(new BorderLayout());
        add(tabbedPane, BorderLayout.CENTER);
        add(logoutPanel, BorderLayout.SOUTH);
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Categoría:"));
        categoryComboBox = new JComboBox<>();
        filterPanel.add(categoryComboBox);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addProductButton = new JButton("Agregar Producto");
        editProductButton = new JButton("Editar Producto");
        deleteProductButton = new JButton("Eliminar Producto");
        buttonsPanel.add(addProductButton);
        buttonsPanel.add(editProductButton);
        buttonsPanel.add(deleteProductButton);
        
        // Summary label
        inventorySummaryLabel = new JLabel("Resumen del inventario cargando...");
        inventorySummaryLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Table
        inventoryTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        
        // Layout
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(filterPanel, BorderLayout.NORTH);
        northPanel.add(buttonsPanel, BorderLayout.CENTER);
        northPanel.add(inventorySummaryLabel, BorderLayout.SOUTH);
        
        panel.add(northPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createSalesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Products table
        productsTable = new JTable();
        JScrollPane productsScrollPane = new JScrollPane(productsTable);
        productsScrollPane.setPreferredSize(new Dimension(500, 300));
        
        // Cart table
        cartTable = new JTable();
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setPreferredSize(new Dimension(500, 200));
        
        // Buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        addToCartButton = new JButton("Agregar al Carrito");
        removeFromCartButton = new JButton("Quitar del Carrito");
        completeSaleButton = new JButton("Completar Venta");
        buttonsPanel.add(addToCartButton);
        buttonsPanel.add(removeFromCartButton);
        buttonsPanel.add(completeSaleButton);
        
        // Client info
        JPanel clientPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clientPanel.add(new JLabel("Cliente:"));
        clientNameField = new JTextField(20);
        clientPanel.add(clientNameField);
        
        // Total
        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        
        // Layout
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        centerPanel.add(productsScrollPane);
        
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(cartScrollPane, BorderLayout.CENTER);
        
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(clientPanel, BorderLayout.NORTH);
        southPanel.add(buttonsPanel, BorderLayout.CENTER);
        southPanel.add(totalLabel, BorderLayout.SOUTH);
        rightPanel.add(southPanel, BorderLayout.SOUTH);
        
        centerPanel.add(rightPanel);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        totalInventoryValueLabel = new JLabel("Valor total del inventario: cargando...");
        totalSalesLabel = new JLabel("Ventas totales: cargando...");
        summaryPanel.add(totalInventoryValueLabel);
        summaryPanel.add(totalSalesLabel);
        
        // Sales history table
        salesHistoryTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(salesHistoryTable);
        
        // Layout
        panel.add(summaryPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    // Getters for inventory components
    public JComboBox<String> getCategoryComboBox() { return categoryComboBox; }
    public JTable getInventoryTable() { return inventoryTable; }
    public JButton getAddProductButton() { return addProductButton; }
    public JButton getEditProductButton() { return editProductButton; }
    public JButton getDeleteProductButton() { return deleteProductButton; }
    public JLabel getInventorySummaryLabel() { return inventorySummaryLabel; }
    
    // Getters for sales components
    public JTable getProductsTable() { return productsTable; }
    public JTable getCartTable() { return cartTable; }
    public JButton getAddToCartButton() { return addToCartButton; }
    public JButton getRemoveFromCartButton() { return removeFromCartButton; }
    public JButton getCompleteSaleButton() { return completeSaleButton; }
    public JTextField getClientNameField() { return clientNameField; }
    public JLabel getTotalLabel() { return totalLabel; }
    
    // Getters for reports components
    public JLabel getTotalInventoryValueLabel() { return totalInventoryValueLabel; }
    public JLabel getTotalSalesLabel() { return totalSalesLabel; }
    public JTable getSalesHistoryTable() { return salesHistoryTable; }
    
    // General getters
    public JButton getLogoutButton() { return logoutButton; }
    public JTabbedPane getTabbedPane() { return tabbedPane; }

    // Listeners
    public void addLogoutListener(ActionListener listener) {
        logoutButton.addActionListener(listener);
    }
    
    public void addCategoryChangeListener(ActionListener listener) {
        categoryComboBox.addActionListener(listener);
    }
    
    public void addAddProductListener(ActionListener listener) {
        addProductButton.addActionListener(listener);
    }
    
    public void addEditProductListener(ActionListener listener) {
        editProductButton.addActionListener(listener);
    }
    
    public void addDeleteProductListener(ActionListener listener) {
        deleteProductButton.addActionListener(listener);
    }
    
    public void addAddToCartListener(ActionListener listener) {
        addToCartButton.addActionListener(listener);
    }
    
    public void addRemoveFromCartListener(ActionListener listener) {
        removeFromCartButton.addActionListener(listener);
    }
    
    public void addCompleteSaleListener(ActionListener listener) {
        completeSaleButton.addActionListener(listener);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }
}
