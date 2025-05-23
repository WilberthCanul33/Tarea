package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginView extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginView() {
        setTitle("AgricolaPOS - Inicio de Sesión");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Título
        JLabel titleLabel = new JLabel("AgricolaPOS", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Subtítulo
        JLabel subtitleLabel = new JLabel("Sistema de Punto de Venta para Agricultores", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        gbc.gridy = 1;
        panel.add(subtitleLabel, gbc);
        
        // Campos de formulario
        gbc.gridwidth = 1;
        
        JLabel usernameLabel = new JLabel("Usuario:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(usernameLabel, gbc);
        
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);
        
        JLabel passwordLabel = new JLabel("Contraseña:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(passwordLabel, gbc);
        
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);
        
        // Botones
        loginButton = new JButton("Iniciar Sesión");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);
        
        JLabel orLabel = new JLabel("o", SwingConstants.CENTER);
        gbc.gridy = 5;
        panel.add(orLabel, gbc);
        
        registerButton = new JButton("Registrarse");
        gbc.gridy = 6;
        panel.add(registerButton, gbc);
        
        add(panel);
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
    }

    public void addRegisterListener(ActionListener listener) {
        registerButton.addActionListener(listener);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
    }
}
