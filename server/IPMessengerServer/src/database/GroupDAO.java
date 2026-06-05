// GroupDAO.java
package database;

import models.Group;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GroupDAO {

    public int createGroup(String name, int creatorId) throws SQLException {
        String sql = "INSERT INTO Groups (name, creator_id) VALUES (?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setInt(2, creatorId);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }
        return -1;
    }

    public Group findById(int groupId) throws SQLException {
        String sql = "SELECT * FROM Groups WHERE id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Group(rs.getInt("id"), rs.getString("name"), rs.getInt("creator_id"), rs.getTimestamp("created_at"));
            }
        }
        return null;
    }

    public void deleteGroup(int groupId) throws SQLException {
        String sql = "DELETE FROM Groups WHERE id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.executeUpdate();
        }
    }
}
