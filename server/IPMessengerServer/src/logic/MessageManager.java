package logic;

import database.MessageDAO;
import models.Message;
import network.JSONParser;
import java.sql.SQLException;
import java.util.List;

public class MessageManager {
    private MessageDAO messageDAO = new MessageDAO();

    public void sendFriendMessage(int senderId, int receiverId, String content) throws SQLException {
        Message msg = new Message();
        msg.setSenderId(senderId);
        msg.setReceiverType("user");
        msg.setReceiverId(receiverId);
        msg.setContent(content);
        msg.setStatus("pending"); // inicialmente pendiente; luego se cambiará a delivered si el destinatario está online
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

    public List<Message> getGroupMessages(int groupId, int limit) throws SQLException {
        return messageDAO.getGroupMessages(groupId, limit);
    }
}