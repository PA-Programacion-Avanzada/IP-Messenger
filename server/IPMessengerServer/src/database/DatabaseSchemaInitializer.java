package database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseSchemaInitializer {
    private DatabaseSchemaInitializer() {
    }

    public static void initialize() throws SQLException {
        Connection connection = DatabaseManager.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    status VARCHAR(20) DEFAULT 'offline',
                    last_seen DATETIME DEFAULT CURRENT_TIMESTAMP
                )
                """);
            stmt.execute("""
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
                """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Groups (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name VARCHAR(100) NOT NULL,
                    creator_id INTEGER NOT NULL,
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (creator_id) REFERENCES Users(id) ON DELETE CASCADE
                )
                """);
            stmt.execute("""
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
                """);
            stmt.execute("""
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
                """);
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_sender ON Messages(sender_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_messages_receiver ON Messages(receiver_id, receiver_type)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_friendships_users ON Friendships(user_id, friend_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_groupmembers_group ON GroupMembers(group_id)");
        }
    }
}
