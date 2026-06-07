package core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import network.Protocol;

public class SessionData {
    private int userId;
    private String username;
    private final List<Map<String, Object>> friends = new ArrayList<>();
    private final List<Map<String, Object>> groups = new ArrayList<>();
    private final List<Map<String, Object>> users = new ArrayList<>();
    private final List<Map<String, Object>> pendingMessages = new ArrayList<>();
    private final List<Map<String, Object>> friendInvites = new ArrayList<>();

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Map<String, Object>> getFriends() {
        return friends;
    }

    public List<Map<String, Object>> getGroups() {
        return groups;
    }

    public List<Map<String, Object>> getUsers() {
        return users;
    }

    public List<Map<String, Object>> getPendingMessages() {
        return pendingMessages;
    }

    public List<Map<String, Object>> getFriendInvites() {
        return friendInvites;
    }

    public void absorb(Map<String, Object> message) {
        String status = String.valueOf(message.get("status"));
        switch (status) {
            case Protocol.RES_LOGIN_SUCCESS -> {
                userId = ((Number) message.get("userId")).intValue();
            }
            case Protocol.RES_FRIEND_LIST -> {
                friends.clear();
                Object rawFriends = message.get("friends");
                if (rawFriends instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> map) {
                            friends.add((Map<String, Object>) map);
                        }
                    }
                }
            }
            case Protocol.RES_GROUP_LIST -> {
                groups.clear();
                Object rawGroups = message.get("groups");
                if (rawGroups instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> map) {
                            groups.add((Map<String, Object>) map);
                        }
                    }
                }
            }
            case Protocol.RES_USER_LIST -> {
                users.clear();
                Object rawUsers = message.get("users");
                if (rawUsers instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> map) {
                            users.add((Map<String, Object>) map);
                        }
                    }
                }
            }
            case Protocol.RES_PENDING_MESSAGES -> {
                pendingMessages.clear();
                Object rawMessages = message.get("messages");
                if (rawMessages instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> map) {
                            pendingMessages.add((Map<String, Object>) map);
                        }
                    }
                }
            }
            case Protocol.RES_FRIEND_INVITE_LIST -> {
                friendInvites.clear();
                Object rawInvites = message.get("invites");
                if (rawInvites instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof Map<?, ?> map) {
                            friendInvites.add((Map<String, Object>) map);
                        }
                    }
                }
            }
            default -> {
            }
        }
    }
}
