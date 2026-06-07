package logic;

import database.FriendshipDAO;
import database.UserDAO;
import models.User;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendshipManager {
    private FriendshipDAO friendshipDAO = new FriendshipDAO();
    private UserDAO userDAO = new UserDAO();

    public boolean sendFriendRequest(int userId, int friendId) throws SQLException {
        if (userId == friendId) return false;
        if (friendshipDAO.areFriends(userId, friendId)) return false;
        String currentStatus = friendshipDAO.getFriendshipStatus(userId, friendId);
        if ("pending".equals(currentStatus) || "accepted".equals(currentStatus)) {
            return false;
        }
        String reverseStatus = friendshipDAO.getFriendshipStatus(friendId, userId);
        if ("pending".equals(reverseStatus)) {
            acceptRequest(friendId, userId);
            return true;
        }
        friendshipDAO.sendRequest(userId, friendId);
        return true;
    }

    public void acceptRequest(int userId, int friendId) throws SQLException {
        friendshipDAO.updateStatus(userId, friendId, "accepted");
        if (friendshipDAO.friendshipExists(friendId, userId)) {
            friendshipDAO.updateStatus(friendId, userId, "accepted");
        } else {
            friendshipDAO.createFriendship(friendId, userId, "accepted");
        }
    }

    public boolean rejectRequest(int userId, int friendId) throws SQLException {
        friendshipDAO.updateStatus(userId, friendId, "rejected");
        if (friendshipDAO.friendshipExists(friendId, userId)) {
            friendshipDAO.updateStatus(friendId, userId, "rejected");
        }
        return true;
    }

    public List<Map<String, Object>> getPendingRequests(int userId) throws SQLException {
        List<Map<String, Object>> requests = new ArrayList<>();
        for (int requesterId : friendshipDAO.getIncomingRequestIds(userId)) {
            User requester = userDAO.findById(requesterId);
            if (requester != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("requesterId", requester.getId());
                map.put("requesterName", requester.getUsername());
                map.put("incoming", true);
                requests.add(map);
            }
        }
        for (int targetId : friendshipDAO.getOutgoingRequestIds(userId)) {
            User target = userDAO.findById(targetId);
            if (target != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("requesterId", target.getId());
                map.put("requesterName", target.getUsername());
                map.put("incoming", false);
                requests.add(map);
            }
        }
        return requests;    }

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