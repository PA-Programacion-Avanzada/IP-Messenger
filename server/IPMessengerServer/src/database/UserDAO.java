package database;

import models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public List<User> findAllExcept(int excludeId) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, password, status, last_seen FROM Users WHERE id != ? ORDER BY username";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, excludeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("status"),
                        rs.getTimestamp("last_seen")
                ));
            }
        }
        return users;
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

    public int incrementFailedAttempts(String username) throws SQLException {
        String updateSql = "UPDATE Users SET failed_attempt_count = COALESCE(failed_attempt_count, 0) + 1, last_failed_attempt = CURRENT_TIMESTAMP WHERE username = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(updateSql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
        return getFailedAttempts(username);
    }

    public int getFailedAttempts(String username) throws SQLException {
        String sql = "SELECT COALESCE(failed_attempt_count, 0) AS failed_attempt_count FROM Users WHERE username = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("failed_attempt_count");
            }
        }
        return 0;
    }

    public void resetFailedAttempts(String username) throws SQLException {
        String sql = "UPDATE Users SET failed_attempt_count = 0, last_failed_attempt = NULL WHERE username = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        }
    }
}