package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class AgricolaClient {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public boolean connect(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            return false;
        }
    }

    public String sendRequest(String request) {
        try {
            output.writeObject(request);
            output.flush();
            return (String) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al enviar solicitud: " + e.getMessage());
            return "ERROR|Error de comunicación con el servidor";
        }
    }

    public void disconnect() {
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error al desconectar: " + e.getMessage());
        }
    }

    // Métodos específicos de la aplicación
    public String login(String username, String password) {
        return sendRequest("LOGIN|" + username + "|" + password);
    }

    public String register(String username, String password, String fullName, String role) {
        return sendRequest("REGISTER|" + username + "|" + password + "|" + fullName + "|" + role);
    }

    public String getProducts(String category) {
        return category != null ? 
            sendRequest("GET_PRODUCTS|" + category) : 
            sendRequest("GET_PRODUCTS");
    }

    public String addProduct(String name, String category, double quantity, String unit, 
                           double price, String supplier, String expirationDate) {
        return sendRequest("ADD_PRODUCT|" + name + "|" + category + "|" + quantity + "|" + 
                          unit + "|" + price + "|" + (supplier != null ? supplier : "") + "|" + 
                          (expirationDate != null ? expirationDate : ""));
    }

    public String updateProduct(int id, String name, String category, double quantity, String unit, 
                              double price, String supplier, String expirationDate) {
        return sendRequest("UPDATE_PRODUCT|" + name + "|" + category + "|" + quantity + "|" + 
                          unit + "|" + price + "|" + (supplier != null ? supplier : "") + "|" + 
                          (expirationDate != null ? expirationDate : "") + "|" + id);
    }

    public String deleteProduct(int id) {
        return sendRequest("DELETE_PRODUCT|" + id);
    }

    public String getInventorySummary() {
        return sendRequest("GET_INVENTORY_SUMMARY");
    }

    public String getSales() {
        return sendRequest("GET_SALES");
    }

    public String addSale(String clientName, double totalAmount, int userId, Map<Integer, Double> cartItems) {
        StringBuilder itemsStr = new StringBuilder();
        for (Map.Entry<Integer, Double> entry : cartItems.entrySet()) {
            if (itemsStr.length() > 0) {
                itemsStr.append(",");
            }
            itemsStr.append(entry.getKey()).append(":").append(entry.getValue());
        }
        
        return sendRequest("ADD_SALE|" + (clientName != null ? clientName : "") + "|" + 
                          totalAmount + "|" + userId + "|" + itemsStr.toString());
    }
}