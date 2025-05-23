package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ProductDialog extends JDialog {
    private JTextField nameField;
    private JComboBox<String> categoryComboBox;
    private JTextField quantityField;
    private JComboBox<String> unitComboBox;
    private JTextField priceField;
    private JTextField supplierField;
    private JTextField expirationDateField;
    private JButton saveButton;
    private JButton cancelButton;

    public ProductDialog(JFrame parent, String title, boolean modal) {
        super(parent, title, modal);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Nombre
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Nombre:"), gbc);
        
        nameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(nameField, gbc);
        
        // Categoría
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Categoría:"), gbc);
        
        categoryComboBox = new JComboBox<>(new String[]{"Semillas", "Fertilizantes", "Pesticidas", "Herramientas"});
        gbc.gridx = 1;
        panel.add(categoryComboBox, gbc);
        
        // Cantidad
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Cantidad:"), gbc);
        
        quantityField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(quantityField, gbc);
        
        // Unidad
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Unidad:"), gbc);
        
        unitComboBox = new JComboBox<>(new String[]{"kg", "l", "unidad"});
        gbc.gridx = 1;
        panel.add(unitComboBox, gbc);
        
        // Precio
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Precio:"), gbc);
        
        priceField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(priceField, gbc);
        
        // Proveedor
        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(new JLabel("Proveedor:"), gbc);
        
        supplierField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(supplierField, gbc);
        
        // Fecha de expiración
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(new JLabel("Fecha de Expiración (YYYY-MM-DD):"), gbc);
        
        expirationDateField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(expirationDateField, gbc);
        
        // Botones
        saveButton = new JButton("Guardar");
        cancelButton = new JButton("Cancelar");
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.add(saveButton);
        buttonsPanel.add(cancelButton);
        
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        panel.add(buttonsPanel, gbc);
        
        add(panel);
    }

    public void clearFields() {
        nameField.setText("");
        categoryComboBox.setSelectedIndex(0);
        quantityField.setText("");
        unitComboBox.setSelectedIndex(0);
        priceField.setText("");
        supplierField.setText("");
        expirationDateField.setText("");
    }

    // Getters
    public String getName() { return nameField.getText(); }
    public String getCategory() { return (String) categoryComboBox.getSelectedItem(); }
    public double getQuantity() { 
        try {
            return Double.parseDouble(quantityField.getText()); 
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public String getUnit() { return (String) unitComboBox.getSelectedItem(); }
    public double getPrice() { 
        try {
            return Double.parseDouble(priceField.getText()); 
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public String getSupplier() { return supplierField.getText(); }
    public String getExpirationDate() { return expirationDateField.getText(); }
    
    // Setters
    public void setName(String name) { nameField.setText(name); }
    public void setCategory(String category) { categoryComboBox.setSelectedItem(category); }
    public void setQuantity(double quantity) { quantityField.setText(String.valueOf(quantity)); }
    public void setUnit(String unit) { unitComboBox.setSelectedItem(unit); }
    public void setPrice(double price) { priceField.setText(String.valueOf(price)); }
    public void setSupplier(String supplier) { supplierField.setText(supplier); }
    public void setExpirationDate(String expirationDate) { expirationDateField.setText(expirationDate); }
    
    // Listeners
    public void addSaveListener(ActionListener listener) {
        saveButton.addActionListener(listener);
    }
    
    public void addCancelListener(ActionListener listener) {
        cancelButton.addActionListener(listener);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}