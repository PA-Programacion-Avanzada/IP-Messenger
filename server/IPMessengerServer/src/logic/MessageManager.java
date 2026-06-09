package logic;

import database.MessageDAO;
import models.Message;
import network.JSONParser;

import java.sql.SQLException;
import java.util.List;

public class MessageManager {
    private MessageDAO messageDAO = new MessageDAO();

    public void saveTemporaryMessage(int senderId, int receiverId, String content) throws SQLException {
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverType("temp");                 // mensajes temporales no deben mezclarse con chat de amigos
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setStatus("pending");                     // <‑‑ marca como pendiente
        // el timestamp se asigna automáticamente por la BD (DEFAULT CURRENT_TIMESTAMP)
        messageDAO.saveMessage(msg);
    }

    public void deleteTemporaryMessagesBetweenUsers(int userA, int userB) throws SQLException {
        messageDAO.deleteTemporaryMessagesBetweenUsers(userA, userB);
    }

    public void sendFriendMessage(int senderId, int receiverId, String content) throws SQLException {
        sendFriendMessage(senderId, receiverId, content, "pending");
    }

    public void sendFriendMessage(int senderId, int receiverId, String content, String status) throws SQLException {
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverType("user");
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setStatus(status);
        messageDAO.saveMessage(msg);
    }

    public void sendGroupMessage(int senderId, int groupId, String content) throws SQLException {
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverType("group");
        msg.setReceiverId(groupId);
        msg.setContent(content);
        msg.setStatus("sent");
        messageDAO.saveMessage(msg);
    }

    public List<Message> getPendingMessages(int userId) throws SQLException {
        return messageDAO.getPendingMessagesForUser(userId);
    }

    public void markMessageRead(int messageId) throws SQLException {
        messageDAO.markAsRead(messageId);
    }

    public List<Message> getFriendHistory(int userId, int friendId, int limit) throws SQLException {
        return messageDAO.getFriendMessages(userId, friendId, limit);
    }
    public List<Message> getGroupMessages(int groupId, int limit) throws SQLException {
        return messageDAO.getGroupMessages(groupId, limit);
    }

    public List<Message> getGroupMessages(int groupId) throws SQLException {
        // Llamamos al método original usando un límite estándar (ej. 100 mensajes)
        return messageDAO.getGroupMessages(groupId, 100);
    }
}
