package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static Connection connection;

    public static void connect(String dbUrl) throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(dbUrl);
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void close() {
        try { if (connection != null && !connection.isClosed()) connection.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }
}