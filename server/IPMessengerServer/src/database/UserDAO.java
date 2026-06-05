package database;

import models.User;
import java.sql.*;

public class UserDAO {

    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, status, last_seen FROM Users WHERE username = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("status"),
                    rs.getTimestamp("last_seen")
                );
            }
        }
        return null;
    }

    public boolean createUser(String username, String passwordHash) throws SQLException {
        String sql = "INSERT INTO Users (username, password, status, last_seen) VALUES (?, ?, 'offline', CURRENT_TIMESTAMP)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updatePassword(int userId, String newHash) throws SQLException {
        String sql = "UPDATE Users SET password = ? WHERE id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, newHash);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int userId, String status) throws SQLException {
        String sql = "UPDATE Users SET status = ?, last_seen = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT id, username, password, status, last_seen FROM Users WHERE id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("id"), rs.getString("username"), rs.getString("password"),
                                rs.getString("status"), rs.getTimestamp("last_seen"));
            }
        }
        return null;
    }
}