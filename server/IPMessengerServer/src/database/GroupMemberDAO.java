
// GroupMemberDAO.java
package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupMemberDAO {

    public void inviteMember(int groupId, int userId) throws SQLException {
        String sql = "INSERT INTO GroupMembers (group_id, user_id, status) VALUES (?, ?, 'invited')";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    public void updateStatus(int groupId, int userId, String status) throws SQLException {
        String sql = "UPDATE GroupMembers SET status = ? WHERE group_id = ? AND user_id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, groupId);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        }
    }

    public List<Integer> getAcceptedMemberIds(int groupId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT user_id FROM GroupMembers WHERE group_id = ? AND status = 'accepted'";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) ids.add(rs.getInt("user_id"));
        }
        return ids;
    }

    public int countAcceptedMembers(int groupId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM GroupMembers WHERE group_id = ? AND status = 'accepted'";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    public List<Integer> getAcceptedGroupIds(int userId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT group_id FROM GroupMembers WHERE user_id = ? AND status = 'accepted'";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) ids.add(rs.getInt("group_id"));
        }
        return ids;
    }

    public void deleteMember(int groupId, int userId) throws SQLException {
        String sql = "DELETE FROM GroupMembers WHERE group_id = ? AND user_id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
}