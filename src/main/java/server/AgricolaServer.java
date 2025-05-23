package server;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.concurrent.*;

import model.DatabaseConnection;

public class AgricolaServer {
    // Usar puerto de variable de entorno (Render lo proporciona)
    private static final int DEFAULT_PORT = 5555;
    private static final int THREAD_POOL_SIZE = 10;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private boolean running;

    public AgricolaServer() {
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    public void start() {
        try {
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", String.valueOf(DEFAULT_PORT)));
            serverSocket = new ServerSocket(port);
            running = true;
            System.out.println("Servidor iniciado en el puerto " + port);
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nueva conexión: " + clientSocket.getInetAddress());
                
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Error en el servidor: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        threadPool.shutdown();
        
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar el servidor: " + e.getMessage());
        }
        
        System.out.println("Servidor detenido");
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        
        @Override
public void run() {
    try {
        InputStream rawInput = clientSocket.getInputStream();
        BufferedInputStream bufferedInput = new BufferedInputStream(rawInput);
        bufferedInput.mark(5); // Marcamos para poder resetear
        
        byte[] header = new byte[4];
        int bytesRead = bufferedInput.read(header);
        
        if (bytesRead == 4) {
            String headerStr = new String(header);
            if (headerStr.equals("HEAD") || headerStr.startsWith("GET") || 
                headerStr.startsWith("POST") || headerStr.startsWith("HTTP")) {
                // Es una petición HTTP
                handleHttpRequest(bufferedInput);
                return;
            }
        }
        
        // Es tu protocolo personalizado
        bufferedInput.reset(); // Volvemos al inicio del stream
        handleCustomProtocol(bufferedInput);
    } catch (IOException e) {
        System.err.println("Error en el manejador del cliente: " + e.getMessage());
    } finally {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
}

        private void handleHttpRequest(InputStream input) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            OutputStream output = clientSocket.getOutputStream();
            
            // Leer toda la petición HTTP (no necesitamos el contenido)
            while (!reader.readLine().isEmpty()); // Leer hasta línea vacía
            
            // Respuesta mínima para health check
            String response = "HTTP/1.1 200 OK\r\n" +
                             "Content-Type: text/plain\r\n" +
                             "Connection: close\r\n" +
                             "\r\n" +
                             "OK";
            
            output.write(response.getBytes());
            output.flush();
            System.out.println("Respondido health check HTTP");
        }

        private void handleCustomProtocol(InputStream input) {
            try (ObjectInputStream ois = new ObjectInputStream(input);
                 ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {
                
                while (true) {
                    String request = (String) ois.readObject();
                    System.out.println("Recibido (protocolo personalizado): " + request);
                    
                    String response = processRequest(request);
                    
                    oos.writeObject(response);
                    oos.flush();
                }
            } catch (EOFException e) {
                System.out.println("Cliente desconectado: " + clientSocket.getInetAddress());
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error en protocolo personalizado: " + e.getMessage());
            }
        }

        private String processRequest(String request) {
            // Implementar lógica de procesamiento según el protocolo personalizado
            // Formato: COMANDO|PARAM1|PARAM2|...
            String[] parts = request.split("\\|");
            String command = parts[0];
            
            try (Connection conn = DatabaseConnection.getConnection()) {
                switch (command) {
                    case "LOGIN":
                        return handleLogin(parts[1], parts[2]);
                    case "REGISTER":
                        return handleRegister(parts[1], parts[2], parts[3], parts[4]);
                    case "GET_PRODUCTS":
                        return handleGetProducts(parts.length > 1 ? parts[1] : null);
                    case "ADD_PRODUCT":
                        return handleAddProduct(parts);
                    case "UPDATE_PRODUCT":
                        return handleUpdateProduct(parts);
                    case "DELETE_PRODUCT":
                        return handleDeleteProduct(parts[1]);
                    case "GET_INVENTORY_SUMMARY":
                        return handleGetInventorySummary();
                    case "GET_SALES":
                        return handleGetSales();
                    case "ADD_SALE":
                        return handleAddSale(parts);
                    default:
                        return "ERROR|Comando no reconocido";
                }
            } catch (SQLException e) {
                return "ERROR|Error de base de datos: " + e.getMessage();
            }
        }

        private String handleLogin(String username, String password) throws SQLException {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return "SUCCESS|" + rs.getInt("id") + "|" + rs.getString("username") + "|" + 
                           rs.getString("full_name") + "|" + rs.getString("role");
                } else {
                    return "ERROR|Usuario o contraseña incorrectos";
                }
            }
        }

        private String handleRegister(String username, String password, String fullName, String role) throws SQLException {
            String checkSql = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = DatabaseConnection.getConnection().prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                if (checkStmt.executeQuery().next()) {
                    return "ERROR|El nombre de usuario ya existe";
                }
            }
            
            String sql = "INSERT INTO users(username, password, full_name, role) VALUES(?, ?, ?, ?)";
            try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, fullName);
                pstmt.setString(4, role);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    return "SUCCESS|Usuario registrado exitosamente";
                } else {
                    return "ERROR|No se pudo registrar el usuario";
                }
            }
        }

        private String handleGetProducts(String category) throws SQLException {
            StringBuilder result = new StringBuilder("PRODUCTS");
            
            String sql = category != null ? 
                "SELECT * FROM products WHERE category = ?" : 
                "SELECT * FROM products";
                
            try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
                if (category != null) {
                    pstmt.setString(1, category);
                }
                
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    result.append("|").append(rs.getInt("id"))
                          .append(",").append(rs.getString("name"))
                          .append(",").append(rs.getString("category"))
                          .append(",").append(rs.getDouble("quantity"))
                          .append(",").append(rs.getString("unit"))
                          .append(",").append(rs.getDouble("price"))
                          .append(",").append(rs.getString("supplier") != null ? rs.getString("supplier") : "")
                          .append(",").append(rs.getString("expiration_date") != null ? rs.getString("expiration_date") : "");
                }
            }
            
            return result.toString();
        }

        private String handleAddProduct(String[] parts) throws SQLException {
            String sql = "INSERT INTO products(name, category, quantity, unit, price, supplier, expiration_date) " +
                         "VALUES(?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, parts[1]); // name
                pstmt.setString(2, parts[2]); // category
                pstmt.setDouble(3, Double.parseDouble(parts[3])); // quantity
                pstmt.setString(4, parts[4]); // unit
                pstmt.setDouble(5, Double.parseDouble(parts[5])); // price
                pstmt.setString(6, parts.length > 6 ? parts[6] : null); // supplier
                pstmt.setString(7, parts.length > 7 ? parts[7] : null); // expiration_date
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    return "SUCCESS|Producto agregado exitosamente";
                } else {
                    return "ERROR|No se pudo agregar el producto";
                }
            }
        }

        private String handleUpdateProduct(String[] parts) throws SQLException {
            String sql = "UPDATE products SET name = ?, category = ?, quantity = ?, unit = ?, " +
                         "price = ?, supplier = ?, expiration_date = ? WHERE id = ?";
            
            try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, parts[1]); // name
                pstmt.setString(2, parts[2]); // category
                pstmt.setDouble(3, Double.parseDouble(parts[3])); // quantity
                pstmt.setString(4, parts[4]); // unit
                pstmt.setDouble(5, Double.parseDouble(parts[5])); // price
                pstmt.setString(6, parts.length > 6 ? parts[6] : null); // supplier
                pstmt.setString(7, parts.length > 7 ? parts[7] : null); // expiration_date
                pstmt.setInt(8, Integer.parseInt(parts[8])); // id
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    return "SUCCESS|Producto actualizado exitosamente";
                } else {
                    return "ERROR|No se pudo actualizar el producto";
                }
            }
        }

        private String handleDeleteProduct(String idStr) throws SQLException {
            String sql = "DELETE FROM products WHERE id = ?";
            
            try (PreparedStatement pstmt = DatabaseConnection.getConnection().prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(idStr));
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    return "SUCCESS|Producto eliminado exitosamente";
                } else {
                    return "ERROR|No se pudo eliminar el producto";
                }
            }
        }

        private String handleGetInventorySummary() throws SQLException {
            StringBuilder result = new StringBuilder("INVENTORY_SUMMARY");
            
            // Total value
            String totalSql = "SELECT SUM(quantity * price) as total FROM products";
            try (Statement stmt = DatabaseConnection.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(totalSql)) {
                if (rs.next()) {
                    result.append("|TOTAL:").append(rs.getDouble("total"));
                }
            }
            
            // By category
            String categorySql = "SELECT category, SUM(quantity * price) as total FROM products GROUP BY category";
            try (Statement stmt = DatabaseConnection.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(categorySql)) {
                while (rs.next()) {
                    result.append("|").append(rs.getString("category"))
                          .append(":").append(rs.getDouble("total"));
                }
            }
            
            return result.toString();
        }

        private String handleGetSales() throws SQLException {
            StringBuilder result = new StringBuilder("SALES");
            
            String sql = "SELECT s.*, u.full_name as user_name FROM sales s JOIN users u ON s.user_id = u.id ORDER BY s.sale_date DESC";
            try (Statement stmt = DatabaseConnection.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    result.append("|").append(rs.getInt("id"))
                          .append(",").append(rs.getTimestamp("sale_date"))
                          .append(",").append(rs.getString("client_name") != null ? rs.getString("client_name") : "")
                          .append(",").append(rs.getDouble("total_amount"))
                          .append(",").append(rs.getString("user_name"));
                }
            }
            
            return result.toString();
        }

        private String handleAddSale(String[] parts) throws SQLException {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false);
                
                // Insert sale
                String saleSql = "INSERT INTO sales(sale_date, client_name, total_amount, user_id) VALUES(?, ?, ?, ?)";
                PreparedStatement saleStmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS);
                
                saleStmt.setString(1, new java.sql.Timestamp(System.currentTimeMillis()).toString());
                saleStmt.setString(2, parts[1]); // client name
                saleStmt.setDouble(3, Double.parseDouble(parts[2])); // total amount
                saleStmt.setInt(4, Integer.parseInt(parts[3])); // user id
                
                int affectedRows = saleStmt.executeUpdate();
                if (affectedRows == 0) {
                    conn.rollback();
                    return "ERROR|No se pudo registrar la venta";
                }
                
                // Get sale ID
                int saleId;
                try (ResultSet generatedKeys = saleStmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        saleId = generatedKeys.getInt(1);
                    } else {
                        conn.rollback();
                        return "ERROR|No se pudo obtener el ID de la venta";
                    }
                }
                
                // Insert sale items (parts[4] contains items in format productId:quantity:price,productId:quantity:price,...)
                String[] items = parts[4].split(",");
                String itemSql = "INSERT INTO sale_items(sale_id, product_id, quantity, unit_price) VALUES(?, ?, ?, ?)";
                PreparedStatement itemStmt = conn.prepareStatement(itemSql);
                
                for (String item : items) {
                    String[] itemParts = item.split(":");
                    int productId = Integer.parseInt(itemParts[0]);
                    double quantity = Double.parseDouble(itemParts[1]);
                    double unitPrice = Double.parseDouble(itemParts[2]);
                    
                    itemStmt.setInt(1, saleId);
                    itemStmt.setInt(2, productId);
                    itemStmt.setDouble(3, quantity);
                    itemStmt.setDouble(4, unitPrice);
                    itemStmt.addBatch();
                    
                    // Update inventory
                    String updateSql = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                    updateStmt.setDouble(1, quantity);
                    updateStmt.setInt(2, productId);
                    
                    if (updateStmt.executeUpdate() == 0) {
                        conn.rollback();
                        return "ERROR|No se pudo actualizar el inventario para el producto ID " + productId;
                    }
                }
                
                int[] itemResults = itemStmt.executeBatch();
                for (int result : itemResults) {
                    if (result == Statement.EXECUTE_FAILED) {
                        conn.rollback();
                        return "ERROR|Error al registrar los items de la venta";
                    }
                }
                
                conn.commit();
                return "SUCCESS|Venta registrada exitosamente|" + saleId;
            } catch (SQLException e) {
                if (conn != null) conn.rollback();
                throw e;
            } finally {
                if (conn != null) conn.setAutoCommit(true);
            }
        }
    }

    public static void main(String[] args) {
        AgricolaServer server = new AgricolaServer();
        server.start();
    }
}