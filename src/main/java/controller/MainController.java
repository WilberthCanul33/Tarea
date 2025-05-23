package controller;

import view.*;
import model.*;
import server.AgricolaClient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.swing.*;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

public class MainController {
    private LoginView loginView;
    private RegisterView registerView;
    private MainView mainView;
    private ProductDialog productDialog;
    private AuthController authController;
    private ProductController productController;
    private SaleController saleController;
    private AgricolaClient client;
    private User currentUser;
    
    // Datos temporales
    private Map<Integer, Double> cartItems = new HashMap<>(); // productId -> quantity
    private Map<Integer, Product> productsMap = new HashMap<>();

    public MainController() {
        // Inicializar el cliente del servidor
        client = new AgricolaClient();
        boolean connected = client.connect("localhost", 5555); // Cambiar por la dirección del servidor en producción
        
        if (!connected) {
            System.err.println("No se pudo conectar al servidor");
            System.exit(1);
        }
        
        // Inicializar controladores
        authController = new AuthController();
        productController = new ProductController();
        saleController = new SaleController();
        
        // Inicializar vistas
        loginView = new LoginView();
        registerView = new RegisterView();
        mainView = new MainView();
        productDialog = new ProductDialog(mainView, "Agregar Producto", true);
        
        // Configurar listeners
        setupListeners();
        
        // Mostrar la vista de login
        loginView.setVisible(true);
    }

    private void setupListeners() {
        // LoginView listeners
        loginView.addLoginListener(e -> handleLogin());
        loginView.addRegisterListener(e -> {
            loginView.setVisible(false);
            registerView.clearFields();
            registerView.setVisible(true);
        });
        
        // RegisterView listeners
        registerView.addRegisterListener(e -> handleRegister());
        registerView.addBackListener(e -> {
            registerView.setVisible(false);
            loginView.setVisible(true);
        });
        
        // MainView listeners
        mainView.addLogoutListener(e -> handleLogout());
        mainView.addCategoryChangeListener(e -> updateInventoryTable());
        mainView.addAddProductListener(e -> showAddProductDialog());
        mainView.addEditProductListener(e -> showEditProductDialog());
        mainView.addDeleteProductListener(e -> deleteProduct());
        mainView.addAddToCartListener(e -> addToCart());
        mainView.addRemoveFromCartListener(e -> removeFromCart());
        mainView.addCompleteSaleListener(e -> completeSale());
        
        // ProductDialog listeners
        productDialog.addSaveListener(e -> saveProduct());
        productDialog.addCancelListener(e -> productDialog.setVisible(false));
    }

    private void handleLogin() {
        String username = loginView.getUsername();
        String password = loginView.getPassword();
        
        if (username.isEmpty() || password.isEmpty()) {
            loginView.showError("Usuario y contraseña son requeridos");
            return;
        }
        
        String response = client.login(username, password);
        String[] parts = response.split("\\|");
        
        if (parts[0].equals("SUCCESS")) {
            currentUser = new User(
                Integer.parseInt(parts[1]), // id
                parts[2], // username
                password, // password (no lo tenemos hasheado en este ejemplo)
                parts[3], // full_name
                parts[4]  // role
            );
            
            loginView.setVisible(false);
            loginView.clearFields();
            
            // Cargar datos iniciales
            loadInitialData();
            mainView.setVisible(true);
        } else {
            loginView.showError(parts[1]);
        }
    }

    private void handleRegister() {
        String username = registerView.getUsername();
        String password = registerView.getPassword();
        String confirmPassword = registerView.getConfirmPassword();
        String fullName = registerView.getFullName();
        
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            registerView.showError("Todos los campos son requeridos");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            registerView.showError("Las contraseñas no coinciden");
            return;
        }
        
        String response = client.register(username, password, fullName, "USER");
        String[] parts = response.split("\\|");
        
        if (parts[0].equals("SUCCESS")) {
            registerView.showSuccess(parts[1]);
            registerView.setVisible(false);
            loginView.setVisible(true);
        } else {
            registerView.showError(parts[1]);
        }
    }

    private void handleLogout() {
        currentUser = null;
        cartItems.clear();
        productsMap.clear();
        mainView.setVisible(false);
        loginView.setVisible(true);
    }

    private void loadInitialData() {
        // Cargar categorías
        updateCategoryComboBox();
        
        // Cargar inventario
        updateInventoryTable();
        
        // Cargar productos para ventas
        updateProductsTable();
        
        // Cargar reportes
        updateReports();
    }

    private void updateCategoryComboBox() {
        String response = client.getProducts(null);
        Set<String> categories = new HashSet<>();
        
        if (response.startsWith("PRODUCTS")) {
            String[] parts = response.split("\\|");
            for (int i = 1; i < parts.length; i++) {
                String[] productData = parts[i].split(",");
                categories.add(productData[2]); // category
            }
        }
        
        JComboBox<String> comboBox = mainView.getCategoryComboBox();
        comboBox.removeAllItems();
        comboBox.addItem("Todas");
        for (String category : categories) {
            comboBox.addItem(category);
        }
    }

    private void updateInventoryTable() {
        String selectedCategory = (String) mainView.getCategoryComboBox().getSelectedItem();
        String category = selectedCategory.equals("Todas") ? null : selectedCategory;
        
        String response = client.getProducts(category);
        
        if (response.startsWith("PRODUCTS")) {
            String[] parts = response.split("\\|");
            String[] columnNames = {"ID", "Nombre", "Categoría", "Cantidad", "Unidad", "Precio", "Proveedor", "Expiración"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
            
            productsMap.clear();
            for (int i = 1; i < parts.length; i++) {
                String[] productData = parts[i].split(",");
                
                Product product = new Product(
                    Integer.parseInt(productData[0]), // id
                    productData[1], // name
                    productData[2], // category
                    Double.parseDouble(productData[3]), // quantity
                    productData[4], // unit
                    Double.parseDouble(productData[5]), // price
                    productData[6], // supplier
                    productData[7]  // expiration_date
                );
                
                productsMap.put(product.getId(), product);
                
                model.addRow(new Object[]{
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getQuantity() + " " + product.getUnit(),
                    "$" + String.format("%.2f", product.getPrice()),
                    product.getSupplier(),
                    product.getExpirationDate()
                });
            }
            
            mainView.getInventoryTable().setModel(model);
            
            // Actualizar resumen
            updateInventorySummary();
        }
    }

    private void updateInventorySummary() {
        String response = client.getInventorySummary();
        
        if (response.startsWith("INVENTORY_SUMMARY")) {
            String[] parts = response.split("\\|");
            StringBuilder summary = new StringBuilder("Resumen del Inventario: ");
            
            for (int i = 1; i < parts.length; i++) {
                String[] summaryPart = parts[i].split(":");
                if (i > 1) summary.append(" | ");
                summary.append(summaryPart[0]).append(": $").append(String.format("%.2f", Double.parseDouble(summaryPart[1])));
            }
            
            mainView.getInventorySummaryLabel().setText(summary.toString());
        }
    }

    private void updateProductsTable() {
        String response = client.getProducts(null);
        
        if (response.startsWith("PRODUCTS")) {
            String[] parts = response.split("\\|");
            String[] columnNames = {"ID", "Nombre", "Categoría", "Disponible", "Precio"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
            
            productsMap.clear();
            for (int i = 1; i < parts.length; i++) {
                String[] productData = parts[i].split(",");
                
                Product product = new Product(
                    Integer.parseInt(productData[0]), // id
                    productData[1], // name
                    productData[2], // category
                    Double.parseDouble(productData[3]), // quantity
                    productData[4], // unit
                    Double.parseDouble(productData[5]), // price
                    productData[6], // supplier
                    productData[7]  // expiration_date
                );
                
                productsMap.put(product.getId(), product);
                
                model.addRow(new Object[]{
                    product.getId(),
                    product.getName(),
                    product.getCategory(),
                    product.getQuantity() + " " + product.getUnit(),
                    "$" + String.format("%.2f", product.getPrice())
                });
            }
            
            mainView.getProductsTable().setModel(model);
        }
    }

    private void updateCartTable() {
        String[] columnNames = {"ID", "Nombre", "Cantidad", "Precio Unitario", "Subtotal"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        
        double total = 0;
        
        for (Map.Entry<Integer, Double> entry : cartItems.entrySet()) {
            Product product = productsMap.get(entry.getKey());
            if (product != null) {
                double subtotal = entry.getValue() * product.getPrice();
                total += subtotal;
                
                model.addRow(new Object[]{
                    product.getId(),
                    product.getName(),
                    entry.getValue() + " " + product.getUnit(),
                    "$" + String.format("%.2f", product.getPrice()),
                    "$" + String.format("%.2f", subtotal)
                });
            }
        }
        
        mainView.getCartTable().setModel(model);
        mainView.getTotalLabel().setText("Total: $" + String.format("%.2f", total));
    }

    private void updateReports() {
        // Actualizar valor total del inventario
        String inventoryResponse = client.getInventorySummary();
        if (inventoryResponse.startsWith("INVENTORY_SUMMARY")) {
            String[] parts = inventoryResponse.split("\\|");
            if (parts.length > 1) {
                String[] totalPart = parts[1].split(":");
                mainView.getTotalInventoryValueLabel().setText(
                    "Valor total del inventario: $" + String.format("%.2f", Double.parseDouble(totalPart[1])));
            }
        }
        
        // Actualizar ventas totales
        String salesResponse = client.getSales();
        if (salesResponse.startsWith("SALES")) {
            String[] parts = salesResponse.split("\\|");
            double totalSales = 0;
            
            String[] columnNames = {"ID", "Fecha", "Cliente", "Total", "Vendedor"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);
            
            for (int i = 1; i < parts.length; i++) {
                String[] saleData = parts[i].split(",");
                double total = Double.parseDouble(saleData[3]);
                totalSales += total;
                
                model.addRow(new Object[]{
                    saleData[0], // id
                    saleData[1], // date
                    saleData[2], // client
                    "$" + String.format("%.2f", total),
                    saleData[4]  // user
                });
            }
            
            mainView.getSalesHistoryTable().setModel(model);
            mainView.getTotalSalesLabel().setText(
                "Ventas totales: $" + String.format("%.2f", totalSales));
        }
    }

    private void showAddProductDialog() {
        productDialog.setTitle("Agregar Producto");
        productDialog.clearFields();
        productDialog.setVisible(true);
    }

    private void showEditProductDialog() {
        int selectedRow = mainView.getInventoryTable().getSelectedRow();
        if (selectedRow == -1) {
            mainView.showError("Seleccione un producto para editar");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) mainView.getInventoryTable().getModel();
        int productId = (int) model.getValueAt(selectedRow, 0);
        Product product = productsMap.get(productId);
        
        if (product != null) {
            productDialog.setTitle("Editar Producto");
            productDialog.setName(product.getName());
            productDialog.setCategory(product.getCategory());
            productDialog.setQuantity(product.getQuantity());
            productDialog.setUnit(product.getUnit());
            productDialog.setPrice(product.getPrice());
            productDialog.setSupplier(product.getSupplier());
            productDialog.setExpirationDate(product.getExpirationDate());
            productDialog.setVisible(true);
        }
    }

    private void saveProduct() {
        String name = productDialog.getName();
        String category = productDialog.getCategory();
        double quantity = productDialog.getQuantity();
        String unit = productDialog.getUnit();
        double price = productDialog.getPrice();
        String supplier = productDialog.getSupplier();
        String expirationDate = productDialog.getExpirationDate();
        
        if (name.isEmpty() || category.isEmpty() || unit.isEmpty()) {
            productDialog.showError("Nombre, categoría y unidad son requeridos");
            return;
        }
        
        if (quantity <= 0 || price <= 0) {
            productDialog.showError("Cantidad y precio deben ser mayores a cero");
            return;
        }
        
        String response;
        if (productDialog.getTitle().equals("Agregar Producto")) {
            response = client.addProduct(name, category, quantity, unit, price, 
                                       supplier.isEmpty() ? null : supplier, 
                                       expirationDate.isEmpty() ? null : expirationDate);
        } else {
            int selectedRow = mainView.getInventoryTable().getSelectedRow();
            DefaultTableModel model = (DefaultTableModel) mainView.getInventoryTable().getModel();
            int productId = (int) model.getValueAt(selectedRow, 0);
            
            response = client.updateProduct(productId, name, category, quantity, unit, price, 
                                         supplier.isEmpty() ? null : supplier, 
                                         expirationDate.isEmpty() ? null : expirationDate);
        }
        
        String[] parts = response.split("\\|");
        if (parts[0].equals("SUCCESS")) {
            productDialog.setVisible(false);
            updateInventoryTable();
            updateProductsTable();
            updateReports();
            mainView.showSuccess(parts[1]);
        } else {
            productDialog.showError(parts[1]);
        }
    }

    private void deleteProduct() {
        int selectedRow = mainView.getInventoryTable().getSelectedRow();
        if (selectedRow == -1) {
            mainView.showError("Seleccione un producto para eliminar");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) mainView.getInventoryTable().getModel();
        int productId = (int) model.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(mainView, 
            "¿Está seguro de eliminar este producto?", "Confirmar", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String response = client.deleteProduct(productId);
            String[] parts = response.split("\\|");
            
            if (parts[0].equals("SUCCESS")) {
                updateInventoryTable();
                updateProductsTable();
                updateReports();
                mainView.showSuccess(parts[1]);
            } else {
                mainView.showError(parts[1]);
            }
        }
    }

    private void addToCart() {
        int selectedRow = mainView.getProductsTable().getSelectedRow();
        if (selectedRow == -1) {
            mainView.showError("Seleccione un producto para agregar al carrito");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) mainView.getProductsTable().getModel();
        int productId = (int) model.getValueAt(selectedRow, 0);
        Product product = productsMap.get(productId);
        
        if (product == null) return;
        
        String input = JOptionPane.showInputDialog(mainView, 
            "Cantidad a agregar (" + product.getUnit() + "):", 
            "Agregar al Carrito", JOptionPane.PLAIN_MESSAGE);
        
        if (input != null && !input.isEmpty()) {
            try {
                double quantity = Double.parseDouble(input);
                
                if (quantity <= 0) {
                    mainView.showError("La cantidad debe ser mayor a cero");
                    return;
                }
                
                if (quantity > product.getQuantity()) {
                    mainView.showError("No hay suficiente stock disponible");
                    return;
                }
                
                // Agregar al carrito o actualizar cantidad
                cartItems.merge(productId, quantity, Double::sum);
                updateCartTable();
            } catch (NumberFormatException e) {
                mainView.showError("Ingrese una cantidad válida");
            }
        }
    }

    private void removeFromCart() {
        int selectedRow = mainView.getCartTable().getSelectedRow();
        if (selectedRow == -1) {
            mainView.showError("Seleccione un item para quitar del carrito");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) mainView.getCartTable().getModel();
        int productId = (int) model.getValueAt(selectedRow, 0);
        
        cartItems.remove(productId);
        updateCartTable();
    }

    private void completeSale() {
        if (cartItems.isEmpty()) {
            mainView.showError("El carrito está vacío");
            return;
        }
        
        String clientName = mainView.getClientNameField().getText();
        double total = 0;
        
        // Calcular total
        for (Map.Entry<Integer, Double> entry : cartItems.entrySet()) {
            Product product = productsMap.get(entry.getKey());
            if (product != null) {
                total += entry.getValue() * product.getPrice();
            }
        }
        
        // Confirmar venta
        int confirm = JOptionPane.showConfirmDialog(mainView, 
            "Total a cobrar: $" + String.format("%.2f", total) + "\n¿Confirmar venta?", 
            "Confirmar Venta", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            String response = client.addSale(
                clientName.isEmpty() ? null : clientName,
                total,
                currentUser.getId(),
                cartItems
            );
            
            String[] parts = response.split("\\|");
            if (parts[0].equals("SUCCESS")) {
                // Generar ticket
                generateReceipt(Integer.parseInt(parts[2]), total);
                
                // Limpiar carrito
                cartItems.clear();
                mainView.getClientNameField().setText("");
                updateCartTable();
                
                // Actualizar datos
                updateInventoryTable();
                updateProductsTable();
                updateReports();
                
                mainView.showSuccess("Venta completada exitosamente");
            } else {
                mainView.showError(parts[1]);
            }
        }
    }

    private void generateReceipt(int saleId, double total) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== Ticket de Venta ===\n");
        receipt.append("Número: ").append(saleId).append("\n");
        receipt.append("Fecha: ").append(new java.util.Date()).append("\n");
        receipt.append("Vendedor: ").append(currentUser.getFullName()).append("\n");
        receipt.append("--------------------------------\n");
        receipt.append("Productos:\n");
        
        for (Map.Entry<Integer, Double> entry : cartItems.entrySet()) {
            Product product = productsMap.get(entry.getKey());
            if (product != null) {
                receipt.append("- ").append(product.getName()).append(": ")
                      .append(entry.getValue()).append(" ").append(product.getUnit())
                      .append(" x $").append(String.format("%.2f", product.getPrice()))
                      .append(" = $").append(String.format("%.2f", entry.getValue() * product.getPrice()))
                      .append("\n");
            }
        }
        
        receipt.append("--------------------------------\n");
        receipt.append("Total: $").append(String.format("%.2f", total)).append("\n");
        receipt.append("=== Gracias por su compra ===");
        
        // Mostrar ticket
        JTextArea textArea = new JTextArea(receipt.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JButton saveButton = new JButton("Guardar Ticket");
        saveButton.addActionListener(e -> saveReceiptToFile(receipt.toString(), saleId));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(saveButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        JOptionPane.showMessageDialog(mainView, panel, "Ticket de Venta", JOptionPane.PLAIN_MESSAGE);
    }

    private void saveReceiptToFile(String receipt, int saleId) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Ticket");
        fileChooser.setSelectedFile(new File("ticket_venta_" + saleId + ".txt"));
        
        int userSelection = fileChooser.showSaveDialog(mainView);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter out = new PrintWriter(fileToSave)) {
                out.println(receipt);
                mainView.showSuccess("Ticket guardado exitosamente");
            } catch (IOException e) {
                mainView.showError("Error al guardar el ticket: " + e.getMessage());
            }
        }
    }
}