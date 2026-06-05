package models;

import java.sql.Timestamp;

public class Friendship {
    private int id;
    private int userId;
    private int friendId;
    private String status; // "pending", "accepted", "rejected"
    private Timestamp createdAt;

    // constructor, getters, setters
    public Friendship() {}
    public Friendship(int id, int userId, int friendId, String status, Timestamp createdAt) {
        this.id = id; this.userId = userId; this.friendId = friendId;
        this.status = status; this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    // ... añadir getters y setters (puedes generarlos con tu IDE)
}