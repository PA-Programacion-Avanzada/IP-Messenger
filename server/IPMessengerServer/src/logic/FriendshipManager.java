package logic;

import database.FriendshipDAO;
import database.UserDAO;
import models.User;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FriendshipManager {
    private FriendshipDAO friendshipDAO = new FriendshipDAO();
    private UserDAO userDAO = new UserDAO();

    public boolean sendFriendRequest(int userId, int friendId) throws SQLException {
        if (userId == friendId) return false;
        friendshipDAO.sendRequest(userId, friendId);
        return true;
    }

    public void acceptRequest(int userId, int friendId) throws SQLException {
        friendshipDAO.updateStatus(userId, friendId, "accepted");
        // también la inversa: si la solicitud fue de friendId a userId, también aceptar
        friendshipDAO.updateStatus(friendId, userId, "accepted");
    }

    public void rejectRequest(int userId, int friendId) throws SQLException {
        friendshipDAO.updateStatus(userId, friendId, "rejected");
    }

    public List<User> getFriends(int userId) throws SQLException {
        List<Integer> friendIds = friendshipDAO.getFriendsIds(userId);
        List<User> friends = new ArrayList<>();
        for (int id : friendIds) {
            User u = userDAO.findById(id);
            if (u != null) friends.add(u);
        }
        return friends;
    }

    public boolean areFriends(int userId1, int userId2) throws SQLException {
        return friendshipDAO.areFriends(userId1, userId2);
    }
}