package logic;

import database.UserDAO;
import models.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class UserManager {
    private UserDAO userDAO = new UserDAO();

    public User authenticate(String username, String password) throws SQLException {
        User user = userDAO.findByUsername(username);
        if (user != null && hashPassword(password).equals(user.getPasswordHash())) {
            userDAO.updateStatus(user.getId(), "online");
            return user;
        }
        return null;
    }

    public boolean register(String username, String password) throws SQLException {
        if (userDAO.findByUsername(username) != null) return false;
        return userDAO.createUser(username, hashPassword(password));
    }

    public boolean recoverPassword(String username, String newPassword) throws SQLException {
        User user = userDAO.findByUsername(username);
        if (user == null) return false;
        return userDAO.updatePassword(user.getId(), hashPassword(newPassword));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void setUserOffline(int userId) throws SQLException {
        userDAO.updateStatus(userId, "offline");
    }
}