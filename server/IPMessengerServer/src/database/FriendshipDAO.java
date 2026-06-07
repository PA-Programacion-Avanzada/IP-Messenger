package database;

import models.Friendship;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendshipDAO {

    public void sendRequest(int userId, int friendId) throws SQLException {
        String sql = "INSERT INTO Friendships (user_id, friend_id, status) VALUES (?, ?, 'pending')";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.executeUpdate();
        }
    }

    public void updateStatus(int userId, int friendId, String status) throws SQLException {
        String sql = "UPDATE Friendships SET status = ? WHERE user_id = ? AND friend_id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, userId);
            stmt.setInt(3, friendId);
            stmt.executeUpdate();
        }
    }

    public List<Integer> getFriendsIds(int userId) throws SQLException {
        List<Integer> friends = new ArrayList<>();
        String sql = "SELECT friend_id FROM Friendships WHERE user_id = ? AND status = 'accepted'";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) friends.add(rs.getInt("friend_id"));
        }
        return friends;
    }

    public List<Integer> getIncomingRequestIds(int userId) throws SQLException {
        List<Integer> requests = new ArrayList<>();
        String sql = "SELECT user_id FROM Friendships WHERE friend_id = ? AND status = 'pending'";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) requests.add(rs.getInt("user_id"));
        }
        return requests;
    }

    public List<Integer> getOutgoingRequestIds(int userId) throws SQLException {
        List<Integer> requests = new ArrayList<>();
        String sql = "SELECT friend_id FROM Friendships WHERE user_id = ? AND status = 'pending'";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) requests.add(rs.getInt("friend_id"));
        }
        return requests;
    }

    public String getFriendshipStatus(int userId, int friendId) throws SQLException {
        String sql = "SELECT status FROM Friendships WHERE user_id = ? AND friend_id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("status");
            }
        }
        return null;
    }

    public boolean friendshipExists(int userId, int friendId) throws SQLException {
        String sql = "SELECT 1 FROM Friendships WHERE user_id = ? AND friend_id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public boolean createFriendship(int userId, int friendId, String status) throws SQLException {
        String sql = "INSERT INTO Friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, friendId);
            stmt.setString(3, status);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean areFriends(int userId, int friendId) throws SQLException {
        String sql = "SELECT 1 FROM Friendships WHERE ((user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)) AND status = 'accepted'";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId); stmt.setInt(2, friendId);
            stmt.setInt(3, friendId); stmt.setInt(4, userId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}