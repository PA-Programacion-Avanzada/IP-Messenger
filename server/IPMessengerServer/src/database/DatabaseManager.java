package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;  // ← Import correcto: java.sql.Statement, NO java.beans.Statement

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
        try { 
            if (connection != null && !connection.isClosed()) 
                connection.close(); 
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }
    
    public static void initializeDatabase() throws SQLException {
        String[] statements = {
            """
            CREATE TABLE IF NOT EXISTS Users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                status VARCHAR(20) DEFAULT 'offline',
                last_seen DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS Friendships (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                friend_id INTEGER NOT NULL,
                status VARCHAR(20) DEFAULT 'pending',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
                FOREIGN KEY (friend_id) REFERENCES Users(id) ON DELETE CASCADE,
                UNIQUE(user_id, friend_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS Groups (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name VARCHAR(100) NOT NULL,
                creator_id INTEGER NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (creator_id) REFERENCES Users(id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS GroupMembers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                group_id INTEGER NOT NULL,
                user_id INTEGER NOT NULL,
                status VARCHAR(20) DEFAULT 'invited',
                joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (group_id) REFERENCES Groups(id) ON DELETE CASCADE,
                FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
                UNIQUE(group_id, user_id)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS Messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                sender_id INTEGER NOT NULL,
                receiver_type VARCHAR(10) NOT NULL,
                receiver_id INTEGER NOT NULL,
                content TEXT NOT NULL,
                status VARCHAR(20) DEFAULT 'sent',
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (sender_id) REFERENCES Users(id) ON DELETE CASCADE
            )
            """,
            "CREATE INDEX IF NOT EXISTS idx_messages_sender ON Messages(sender_id)",
            "CREATE INDEX IF NOT EXISTS idx_messages_receiver ON Messages(receiver_id, receiver_type)",
            "CREATE INDEX IF NOT EXISTS idx_friendships_users ON Friendships(user_id, friend_id)",
            "CREATE INDEX IF NOT EXISTS idx_groupmembers_group ON GroupMembers(group_id)"
        };

        try (Statement stmt = connection.createStatement()) {
            for (String sql : statements) {
                stmt.execute(sql);
            }
        }
    }
}