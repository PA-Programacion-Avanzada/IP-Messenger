package database;

import models.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageDAO {

    public void saveMessage(Message msg) throws SQLException {
        String sql = "INSERT INTO Messages (sender_id, receiver_type, receiver_id, content, status) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, msg.getSenderId());
            stmt.setString(2, msg.getReceiverType());
            stmt.setInt(3, msg.getReceiverId());
            stmt.setString(4, msg.getContent());
            stmt.setString(5, msg.getStatus());
            stmt.executeUpdate();
        }
    }

    public List<Message> getPendingMessagesForUser(int userId) throws SQLException {
        List<Message> pending = new ArrayList<>();
        String sql = "SELECT * FROM Messages WHERE receiver_type = 'user' AND receiver_id = ? AND status = 'pending' ORDER BY timestamp ASC";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Message m = new Message();
                m.setId(rs.getInt("id"));
                m.setSenderId(rs.getInt("sender_id"));
                m.setReceiverType(rs.getString("receiver_type"));
                m.setReceiverId(rs.getInt("receiver_id"));
                m.setContent(rs.getString("content"));
                m.setStatus(rs.getString("status"));
                m.setTimestamp(rs.getTimestamp("timestamp"));
                pending.add(m);
            }
        }
        return pending;
    }

    public void markAsRead(int messageId) throws SQLException {
        String sql = "UPDATE Messages SET status = 'read' WHERE id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, messageId);
            stmt.executeUpdate();
        }
    }

    public void markAsDelivered(int messageId) throws SQLException {
        String sql = "UPDATE Messages SET status = 'delivered' WHERE id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, messageId);
            stmt.executeUpdate();
        }
    }

    public List<Message> getGroupMessages(int groupId, int limit) throws SQLException {
    List<Message> msgs = new ArrayList<>();
    // Cambiamos DESC por ASC para que los mensajes antiguos aparezcan primero
    String sql = "SELECT * FROM (SELECT * FROM Messages WHERE receiver_type = 'group' AND receiver_id = ? ORDER BY timestamp DESC LIMIT ?) ORDER BY timestamp ASC";
    
    try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
        stmt.setInt(1, groupId);
        stmt.setInt(2, limit);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Message m = new Message();
            m.setId(rs.getInt("id"));
            m.setSenderId(rs.getInt("sender_id"));
            m.setContent(rs.getString("content"));
            m.setTimestamp(rs.getTimestamp("timestamp"));
            msgs.add(m);
        }
    }
    return msgs;
}
}