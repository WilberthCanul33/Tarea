package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection;
    private static final String DB_URL = "jdbc:sqlite:db/agricola.db";

    public static Connection getConnection() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DB_URL);
                createTables();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

    private static void createTables() {
        try {
            // Tabla de usuarios
            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "full_name TEXT NOT NULL, " +
                "role TEXT NOT NULL)"
            );

            // Tabla de productos
            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "category TEXT NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "price REAL NOT NULL, " +
                "supplier TEXT, " +
                "expiration_date TEXT)"
            );

            // Tabla de ventas
            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS sales (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sale_date TEXT NOT NULL, " +
                "client_name TEXT, " +
                "total_amount REAL NOT NULL, " +
                "user_id INTEGER NOT NULL, " +
                "FOREIGN KEY(user_id) REFERENCES users(id))"
            );

            // Tabla de items de venta
            connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS sale_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sale_id INTEGER NOT NULL, " +
                "product_id INTEGER NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "unit_price REAL NOT NULL, " +
                "FOREIGN KEY(sale_id) REFERENCES sales(id), " +
                "FOREIGN KEY(product_id) REFERENCES products(id))"
            );

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}