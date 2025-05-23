package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class RegisterView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField fullNameField;
    private JButton registerButton;
    private JButton backButton;

    public RegisterView() {
        setTitle("AgricolaPOS - Registro");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Título
        JLabel titleLabel = new JLabel("Registro de Usuario", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Campos de formulario
        gbc.gridwidth = 1;
        
        JLabel usernameLabel = new JLabel("Usuario:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(usernameLabel, gbc);
        
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);
        
        JLabel passwordLabel = new JLabel("Contraseña:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(passwordLabel, gbc);
        
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);
        
        JLabel confirmPasswordLabel = new JLabel("Confirmar Contraseña:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(confirmPasswordLabel, gbc);
        
        confirmPasswordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(confirmPasswordField, gbc);
        
        JLabel fullNameLabel = new JLabel("Nombre Completo:");
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(fullNameLabel, gbc);
        
        fullNameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(fullNameField, gbc);
        
        // Botones
        registerButton = new JButton("Registrarse");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        panel.add(registerButton, gbc);
        
        backButton = new JButton("Volver al Login");
        gbc.gridy = 6;
        panel.add(backButton, gbc);
        
        add(panel);
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public String getConfirmPassword() {
        return new String(confirmPasswordField.getPassword());
    }

    public String getFullName() {
        return fullNameField.getText();
    }

    public void addRegisterListener(ActionListener listener) {
        registerButton.addActionListener(listener);
    }

    public void addBackListener(ActionListener listener) {
        backButton.addActionListener(listener);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        confirmPasswordField.setText("");
        fullNameField.setText("");
    }
}