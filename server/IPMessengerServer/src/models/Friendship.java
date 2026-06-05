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
    // ... añadir getters y setters (puedes generarlos con tu IDE)
}