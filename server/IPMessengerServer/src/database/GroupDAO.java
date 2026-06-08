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

    // =========================================================================
    // MÉTODOS OPTIMIZADOS PARA EL HISTORIAL DE CHAT (SOPORTA SOBRECARGA)
    // =========================================================================

    /**
     * Guarda un mensaje enviado a un grupo en la base de datos.
     * Utiliza 'synchronized' para crear una fila de espera en el procesador,
     * evitando errores de 'database is locked' si 10 o más personas
     * mandan mensajes exactamente al mismo milisegundo.
     */
    public synchronized void saveGroupMessage(int groupId, int senderId, String senderName, String content) throws SQLException {
        // Asegurar de forma rústica que la tabla exista en el archivo SQLite
        String createTableSql = "CREATE TABLE IF NOT EXISTS GroupMessages (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                "group_id INTEGER, " +
                                "sender_id INTEGER, " +
                                "sender_name TEXT, " +
                                "content TEXT, " +
                                "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP)";
        
        try (Statement s = DatabaseManager.getConnection().createStatement()) {
            s.execute(createTableSql);
        }

        // Insertar el nuevo mensaje en el historial de forma segura
        String sql = "INSERT INTO GroupMessages (group_id, sender_id, sender_name, content) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            stmt.setInt(2, senderId);
            stmt.setString(3, senderName);
            stmt.setString(4, content);
            stmt.executeUpdate();
        }
    }

    /**
     * Obtiene el historial completo de mensajes de un grupo en orden cronológico.
     * Construye un String con formato JSON plano listo para transmitirse por el socket.
     */
    public String getGroupHistoryJson(int groupId) throws SQLException {
        String sql = "SELECT sender_name, content, timestamp FROM GroupMessages WHERE group_id = ? ORDER BY timestamp ASC";
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");
        
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, groupId);
            ResultSet rs = stmt.executeQuery();
            boolean first = true;
            
            while (rs.next()) {
                if (!first) {
                    jsonBuilder.append(",");
                }
                first = false;
                
                // Limpieza manual de caracteres especiales para no romper el string JSON
                String cleanContent = rs.getString("content")
                                        .replace("\\", "\\\\")
                                        .replace("\"", "\\\"");
                
                String sender = rs.getString("sender_name")
                                  .replace("\\", "\\\\")
                                  .replace("\"", "\\\"");
                                  
                String time = rs.getString("timestamp");
                
                jsonBuilder.append(String.format(
                    "{\"sender\":\"%s\",\"content\":\"%s\",\"timestamp\":\"%s\"}",
                    sender, cleanContent, time
                ));
            }
        }
        
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }
}